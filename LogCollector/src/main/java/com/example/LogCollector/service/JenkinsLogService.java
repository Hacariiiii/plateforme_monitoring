package com.example.LogCollector.service;

import com.example.LogCollector.Entity.*;
import com.example.LogCollector.dto.*;
import com.example.LogCollector.repository.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class JenkinsLogService {

    @Autowired
    private PipelineRepository pipelineRepository;

    @Autowired
    private BuildRepository buildRepository;

    @Autowired
    private LogRepository logRepository;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @Value("${jenkins.url}")
    private String jenkinsUrl;

    @Value("${jenkins.username}")
    private String jenkinsUsername;

    @Value("${jenkins.api-key}")
    private String jenkinsApiKey;

    @Value("${kafka.topic.name:jenkins-logs}")
    private String kafkaTopic;

    /**
     * Webhook collection - Called when Jenkins sends build info
     */
public BuildDTO collectAndSaveLogs(String jobName, Integer buildNumber, String buildStatus) {
    try {
        System.out.println("🔄 Starting log collection for job: " + jobName + " #" + buildNumber);

        // 1️⃣ Vérifier ou créer le pipeline
        Pipeline pipeline = pipelineRepository.findByName(jobName)
                .orElseGet(() -> {
                    Pipeline newPipeline = new Pipeline(jobName, jenkinsUrl + "/job/" + jobName);
                    return pipelineRepository.save(newPipeline);
                });
        System.out.println("✓ Pipeline ID: " + pipeline.getId());

        // 2️⃣ Vérifier si le build existe déjà
        Optional<Build> existingBuild = buildRepository.findByPipelineAndBuildNumber(pipeline, buildNumber);
        if (existingBuild.isPresent()) {
            System.out.println("⚠️ Build #" + buildNumber + " already exists, skipping");

            // Forcer le chargement des logs pour le DTO
            Build existing = existingBuild.get();
            existing.getLogs().size();
            return convertBuildToDTO(existing);
        }

        // 3️⃣ Récupérer les logs Jenkins
        String consoleUrl = jenkinsUrl + "/job/" + jobName + "/" + buildNumber + "/consoleText";
        HttpHeaders headers = createAuthHeaders();
        HttpEntity<String> entity = new HttpEntity<>(headers);
        ResponseEntity<String> consoleResponse = restTemplate.exchange(consoleUrl, HttpMethod.GET, entity, String.class);
        String consoleLogs = consoleResponse.getBody();
        System.out.println("✓ Console logs retrieved, size: " + consoleLogs.length());

        // 4️⃣ Déterminer le status final
        BuildStatus finalStatus = (buildStatus == null || buildStatus.equals("null"))
                ? parseBuildStatus(consoleLogs)
                : BuildStatus.valueOf(buildStatus);

        // 5️⃣ Créer et sauvegarder le build
        Build build = new Build(pipeline, buildNumber, finalStatus);
        build.setTriggeredBy("Jenkins Webhook");
        build.setCreatedAt(LocalDateTime.now());
        Build savedBuild = buildRepository.save(build);

        // 6️⃣ Sauvegarder les logs associés
        parseLogs(savedBuild, consoleLogs);

        // 6.1️⃣ Forcer Hibernate à charger les logs
        Build buildWithLogs = buildRepository.findById(savedBuild.getId())
                .orElseThrow(() -> new RuntimeException("Build non trouvé après sauvegarde"));
        buildWithLogs.getLogs().size(); // force fetch

        System.out.println("✓ Build saved with ID: " + buildWithLogs.getId());

        // 7️⃣ Convertir en DTO
        BuildDTO buildDTO = convertBuildToDTO(buildWithLogs);
        System.out.println("DEBUG: Logs dans BuildDTO avant envoi à Analyzer : " + buildDTO.getLogs().size());

        // 8️⃣ Envoyer automatiquement à Analyzer
        sendToAnalyzer(buildDTO);

        System.out.println("✅ Log collection and Analyzer push completed");

        return buildDTO;

    } catch (Exception e) {
        System.err.println("❌ Error collecting logs: " + e.getMessage());
        e.printStackTrace();
        throw new RuntimeException("Failed to collect Jenkins log: " + e.getMessage());
    }
}


public void sendToAnalyzer(BuildDTO build) {
    BuildMessageDTO dto = new BuildMessageDTO();
    List<Map<String, Object>> dataList = new ArrayList<>();

    Map<String, Object> buildMap = new HashMap<>();
    buildMap.put("pipelineId", build.getPipelineId());
    buildMap.put("buildNumber", build.getBuildNumber());
    buildMap.put("status", build.getStatus());
    buildMap.put("triggeredBy", build.getTriggeredBy());
    buildMap.put("startTime", build.getStartTime());
    buildMap.put("endTime", build.getEndTime());

    // Transformation explicite des logs en Map
    List<Map<String, Object>> logMaps = new ArrayList<>();
    if (build.getLogs() != null) {
        for (LogDTO log : build.getLogs()) {
            Map<String, Object> logMap = new HashMap<>();
            logMap.put("id", log.getId());
            logMap.put("logLevel", log.getLogLevel());
            logMap.put("message", log.getMessage());
            logMap.put("stackTrace", log.getStackTrace());
            logMap.put("createdAt", log.getCreatedAt());
            logMaps.add(logMap);
        }
    }
    buildMap.put("logs", logMaps);

    dataList.add(buildMap);
    dto.setData(dataList);

    // DEBUG
    System.out.println("DEBUG: DTO ready to send: " + dto.getData());

    // Envoi
    RestTemplate restTemplate = new RestTemplate();
    try {
        restTemplate.postForEntity("http://localhost:8082/api/analyzer/builds/analyze", dto, Map.class);
        System.out.println("✅ Build envoyé à Analyzer : " + build.getBuildNumber());
    } catch (Exception e) {
        System.err.println("❌ Erreur lors de l'envoi à Analyzer : " + e.getMessage());
    }
}



// === Conversion BuildDTO -> BuildMessageDTO pour Analyzer ===
public BuildMessageDTO convertToBuildMessageDTO(BuildDTO build) {
    BuildMessageDTO dto = new BuildMessageDTO();
    List<Map<String, Object>> dataList = new ArrayList<>();

    Map<String, Object> buildMap = new HashMap<>();
    buildMap.put("pipelineId", build.getPipelineId());
    buildMap.put("buildNumber", build.getBuildNumber());
    buildMap.put("status", build.getStatus());
    buildMap.put("triggeredBy", build.getTriggeredBy());
    buildMap.put("startTime", build.getStartTime());
    buildMap.put("endTime", build.getEndTime());
List<Map<String, Object>> logMaps = new ArrayList<>();
if (build.getLogs() != null) {
    for (LogDTO log : build.getLogs()) {
        Map<String, Object> logMap = new HashMap<>();
        logMap.put("id", log.getId());
        logMap.put("logLevel", log.getLogLevel());
        logMap.put("message", log.getMessage());
        logMap.put("stackTrace", log.getStackTrace());
        logMap.put("createdAt", log.getCreatedAt() != null ? log.getCreatedAt().toString() : null);
        logMaps.add(logMap);
    }
}
buildMap.put("logs", logMaps); 

    dataList.add(buildMap);
    dto.setData(dataList);

    return dto;
}



private void parseLogs(Build build, String consoleLogs) {
    String[] lines = consoleLogs.split("\n");
    List<Log> logsToAdd = new ArrayList<>();
    for (String line : lines) {
        if (line.trim().isEmpty()) continue;
        LogLevel level = determineLogLevel(line);
        Log logEntry = new Log(build, level, line);
        logRepository.save(logEntry); // <-- sauvegarde individuelle

        logsToAdd.add(logEntry);
    }

    if (build.getLogs() == null) {
        build.setLogs(new ArrayList<>());
    }
    build.getLogs().addAll(logsToAdd);

    // ⚠️ Assurer que build est resauvegardé pour mettre à jour la collection
    buildRepository.save(build);

    System.out.println("✓ Saved " + logsToAdd.size() + " log entries for build #" + build.getBuildNumber());
}



    private LogLevel determineLogLevel(String line) {
        if (line.contains("[ERROR]") || line.contains("ERROR") ||
                line.contains("FAILURE") || line.contains("Failed") ||
                line.contains("Exception")) {
            return LogLevel.ERROR;
        } else if (line.contains("[WARN]") || line.contains("WARNING")) {
            return LogLevel.WARN;
        } else if (line.contains("[DEBUG]")) {
            return LogLevel.DEBUG;
        }
        return LogLevel.INFO;
    }

    private BuildStatus parseBuildStatus(String consoleLogs) {
        if (consoleLogs.contains("Finished: SUCCESS")) {
            return BuildStatus.SUCCESS;
        } else if (consoleLogs.contains("Finished: FAILURE")) {
            return BuildStatus.FAILURE;
        } else if (consoleLogs.contains("Finished: UNSTABLE")) {
            return BuildStatus.UNSTABLE;
        }
        return BuildStatus.UNKNOWN;
    }

    private HttpHeaders createAuthHeaders() {
        HttpHeaders headers = new HttpHeaders();
        String auth = jenkinsUsername + ":" + jenkinsApiKey;
        String encodedAuth = Base64.getEncoder().encodeToString(auth.getBytes());
        headers.set("Authorization", "Basic " + encodedAuth);
        return headers;
    }

    // ==================== BASIC GETTERS ====================

    public List<PipelineDTO> getAllPipelines() {
        return pipelineRepository.findAll()
                .stream()
                .map(this::convertPipelineToDTO)
                .collect(Collectors.toList());
    }

    public PipelineDTO getPipelineById(Long id) {
        return pipelineRepository.findById(id)
                .map(this::convertPipelineToDTO)
                .orElse(null);
    }

    public PipelineDTO getPipelineByName(String name) {
        return pipelineRepository.findByName(name)
                .map(this::convertPipelineToDTO)
                .orElse(null);
    }

    public List<BuildDTO> getBuildsByPipeline(Long pipelineId) {
        Pipeline pipeline = pipelineRepository.findById(pipelineId).orElse(null);
        if (pipeline == null) return new ArrayList<>();
        return buildRepository.findByPipelineOrderByCreatedAtDesc(pipeline)
                .stream()
                .map(this::convertBuildToDTO)
                .collect(Collectors.toList());
    }

    public BuildDTO getBuildById(Long buildId) {
        return buildRepository.findById(buildId)
                .map(this::convertBuildToDTO)
                .orElse(null);
    }

    public List<LogDTO> getLogsByBuild(Long buildId) {
        Build build = buildRepository.findById(buildId).orElse(null);
        if (build == null) return new ArrayList<>();
        return logRepository.findByBuildOrderByCreatedAtDesc(build)
                .stream()
                .map(this::convertLogToDTO)
                .collect(Collectors.toList());
    }

    public List<LogDTO> getErrorLogsByBuild(Long buildId) {
        Build build = buildRepository.findById(buildId).orElse(null);
        if (build == null) return new ArrayList<>();
        return logRepository.findByBuildAndLogLevel(build, LogLevel.ERROR)
                .stream()
                .map(this::convertLogToDTO)
                .collect(Collectors.toList());
    }

    // ==================== SMART METHODS (جديد) ====================

    /**
     * 1️⃣ آخر Pipeline مع الـ builds (بدون logs)
     * GET /api/jenkins-logs/smart/pipeline/last-summary
     */
    public Map<String, Object> getLastPipelineSummary() {
        try {
            System.out.println("📊 Fetching last pipeline summary...");

            Pipeline lastPipeline = pipelineRepository.findAll()
                    .stream()
                    .max((p1, p2) -> p1.getCreatedAt().compareTo(p2.getCreatedAt()))
                    .orElse(null);

            if (lastPipeline == null) {
                System.out.println("❌ No pipelines found");
                return Map.of("status", "error", "message", "No pipelines found");
            }

            System.out.println("✓ Found last pipeline: " + lastPipeline.getName());

            List<BuildDTO> builds = buildRepository.findByPipelineOrderByCreatedAtDesc(lastPipeline)
                    .stream()
                    .map(build -> {
                        BuildDTO dto = convertBuildToDTO(build);
                        dto.setLogs(null);
                        return dto;
                    })
                    .collect(Collectors.toList());

            System.out.println("✓ Found " + builds.size() + " builds");

            PipelineDTO pipelineDTO = convertPipelineToDTO(lastPipeline);
            pipelineDTO.setBuilds(builds);

            Map<String, Object> result = new HashMap<>();
            result.put("status", "success");
            result.put("data", pipelineDTO);
            result.put("timestamp", LocalDateTime.now());

            try {
                kafkaTemplate.send(kafkaTopic, "last_pipeline_summary",
                        objectMapper.writeValueAsString(result));
                System.out.println("✅ Sent to Kafka: last_pipeline_summary");
            } catch (Exception e) {
                System.err.println("⚠️ Kafka error: " + e.getMessage());
            }

            System.out.println("✅ getLastPipelineSummary completed successfully");
            return result;
        } catch (Exception e) {
            System.err.println("❌ Error in getLastPipelineSummary: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Error: " + e.getMessage());
        }
    }

    /**
     * 2️⃣ آخر Build مع logs المهمة (ERROR, WARN) + Pipeline info
     * GET /api/jenkins-logs/smart/build/last-important
     */
    public Map<String, Object> getLastBuildWithImportantLogs() {
        try {
            System.out.println("📊 Fetching last build with important logs...");

            Build lastBuild = buildRepository.findFirstByOrderByCreatedAtDesc()
                    .orElse(null);

            if (lastBuild == null) {
                System.out.println("❌ No builds found");
                return Map.of("status", "error", "message", "No builds found");
            }

            System.out.println("✓ Found last build: #" + lastBuild.getBuildNumber());

            BuildDTO buildDTO = convertBuildToDTO(lastBuild);

            List<LogDTO> importantLogs = logRepository.findByBuild(lastBuild)
                    .stream()
                    .filter(log -> log.getLogLevel() == LogLevel.ERROR ||
                            log.getLogLevel() == LogLevel.WARN)
                    .map(this::convertLogToDTO)
                    .collect(Collectors.toList());

            System.out.println("✓ Found " + importantLogs.size() + " important logs");

            buildDTO.setLogs(importantLogs);
            buildDTO.setLogCount(importantLogs.size());

            Pipeline pipeline = lastBuild.getPipeline();
            Map<String, Object> pipelineInfo = Map.of(
                    "id", pipeline.getId(),
                    "name", pipeline.getName(),
                    "totalBuilds", buildRepository.findByPipeline(pipeline).size()
            );

            Map<String, Object> result = new HashMap<>();
            result.put("status", "success");
            result.put("pipeline", pipelineInfo);
            result.put("data", buildDTO);
            result.put("timestamp", LocalDateTime.now());

            try {
                kafkaTemplate.send(kafkaTopic, "last_build",
                        objectMapper.writeValueAsString(result));
                System.out.println("✅ Sent to Kafka: last_build_important");
            } catch (Exception e) {
                System.err.println("⚠️ Kafka error: " + e.getMessage());
            }

            System.out.println("✅ getLastBuildWithImportantLogs completed successfully");
            return result;
        } catch (Exception e) {
            System.err.println("❌ Error in getLastBuildWithImportantLogs: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Error: " + e.getMessage());
        }
    }

    /**
     * 3️⃣ كل الـ Pipelines مع Builds (بدون logs)
     * GET /api/jenkins-logs/smart/pipelines/all-summary
     */
    public Map<String, Object> getAllPipelinesSummary() {
        try {
            System.out.println("📊 Fetching all pipelines summary...");

            List<Pipeline> allPipelines = pipelineRepository.findAll();
            System.out.println("✓ Found " + allPipelines.size() + " pipelines");

            List<PipelineDTO> pipelines = allPipelines
                    .stream()
                    .map(pipeline -> {
                        PipelineDTO dto = convertPipelineToDTO(pipeline);

                        List<BuildDTO> builds = buildRepository.findByPipelineOrderByCreatedAtDesc(pipeline)
                                .stream()
                                .map(build -> {
                                    BuildDTO buildDTO = convertBuildToDTO(build);
                                    buildDTO.setLogs(null);
                                    return buildDTO;
                                })
                                .collect(Collectors.toList());

                        dto.setBuilds(builds);
                        return dto;
                    })
                    .collect(Collectors.toList());

            Map<String, Object> result = new HashMap<>();
            result.put("status", "success");
            result.put("totalPipelines", pipelines.size());
            result.put("data", pipelines);
            result.put("timestamp", LocalDateTime.now());

            try {
                kafkaTemplate.send(kafkaTopic, "all_pipelines_summary",
                        objectMapper.writeValueAsString(result));
                System.out.println("✅ Sent to Kafka: all_pipelines_summary");
            } catch (Exception e) {
                System.err.println("⚠️ Kafka error: " + e.getMessage());
            }

            System.out.println("✅ getAllPipelinesSummary completed successfully");
            return result;
        } catch (Exception e) {
            System.err.println("❌ Error in getAllPipelinesSummary: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Error: " + e.getMessage());
        }
    }

    // ==================== KAFKA METHODS ====================

    public Map<String, Object> getAllPipelinesWithBuildsAndLogs() {
        try {
            System.out.println("📊 Fetching all pipelines with builds and logs...");

            List<PipelineDTO> pipelines = pipelineRepository.findAll()
                    .stream()
                    .map(pipeline -> {
                        PipelineDTO dto = convertPipelineToDTO(pipeline);
                        List<BuildDTO> builds = buildRepository.findByPipelineOrderByCreatedAtDesc(pipeline)
                                .stream()
                                .map(build -> {
                                    BuildDTO buildDTO = convertBuildToDTO(build);
                                    List<LogDTO> logs = logRepository.findByBuildOrderByCreatedAtDesc(build)
                                            .stream()
                                            .map(this::convertLogToDTO)
                                            .collect(Collectors.toList());
                                    buildDTO.setLogs(logs);
                                    buildDTO.setLogCount(logs.size());
                                    return buildDTO;
                                })
                                .collect(Collectors.toList());
                        dto.setBuilds(builds);
                        return dto;
                    })
                    .collect(Collectors.toList());

            Map<String, Object> result = new HashMap<>();
            result.put("status", "success");
            result.put("totalPipelines", pipelines.size());
            result.put("data", pipelines);
            result.put("timestamp", LocalDateTime.now());

            try {
                kafkaTemplate.send(kafkaTopic, "all_pipelines", objectMapper.writeValueAsString(result));
            } catch (Exception e) {
                System.err.println("⚠️ Kafka error: " + e.getMessage());
            }

            return result;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error: " + e.getMessage());
        }
    }

    public Map<String, Object> getAllBuildsWithLogsData() {
        try {
            List<BuildDTO> builds = buildRepository.findAll()
                    .stream()
                    .map(build -> {
                        BuildDTO buildDTO = convertBuildToDTO(build);
                        List<LogDTO> logs = logRepository.findByBuildOrderByCreatedAtDesc(build)
                                .stream()
                                .map(this::convertLogToDTO)
                                .collect(Collectors.toList());
                        buildDTO.setLogs(logs);
                        buildDTO.setLogCount(logs.size());
                        return buildDTO;
                    })
                    .collect(Collectors.toList());

            Map<String, Object> result = new HashMap<>();
            result.put("status", "success");
            result.put("totalBuilds", builds.size());
            result.put("data", builds);
            result.put("timestamp", LocalDateTime.now());

            try {
                kafkaTemplate.send(kafkaTopic, "all_builds", objectMapper.writeValueAsString(result));
            } catch (Exception e) {
                System.err.println("⚠️ Kafka error: " + e.getMessage());
            }

            return result;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error: " + e.getMessage());
        }
    }
  public Map<String, Object> getLastBuildWithLogsData() {
        Map<String, Object> result = new HashMap<>();
        result.put("timestamp", LocalDateTime.now());

        try {
            var lastBuild = buildRepository.findAll()
                    .stream()
                    .max(Comparator.comparing(Build::getCreatedAt))
                    .orElse(null);

            if (lastBuild == null) {
                result.put("status", "success");
                result.put("data", Collections.emptyList());
                result.put("message", "No builds found");
            } else {
                var buildDTO = convertBuildToDTO(lastBuild);
                List<LogDTO> logs = logRepository.findByBuildOrderByCreatedAtDesc(lastBuild)
                        .stream()
                        .map(this::convertLogToDTO)
                        .collect(Collectors.toList());
                buildDTO.setLogs(logs);
                buildDTO.setLogCount(logs.size());

                result.put("status", "success");
                result.put("data", buildDTO);
            }
        } catch (Exception e) {
            result.put("status", "error");
            result.put("data", Collections.emptyList());
            result.put("message", e.getMessage());
            e.printStackTrace();
        }

        return result;
    }



    public Map<String, Object> getLastPipelineWithBuildsAndLogs() {
        try {
            Pipeline lastPipeline = pipelineRepository.findAll()
                    .stream()
                    .max((p1, p2) -> p1.getCreatedAt().compareTo(p2.getCreatedAt()))
                    .orElse(null);

            if (lastPipeline == null) {
                return Map.of("status", "error", "message", "No pipelines found");
            }

            PipelineDTO pipelineDTO = convertPipelineToDTO(lastPipeline);
            List<BuildDTO> builds = buildRepository.findByPipelineOrderByCreatedAtDesc(lastPipeline)
                    .stream()
                    .map(build -> {
                        BuildDTO buildDTO = convertBuildToDTO(build);
                        List<LogDTO> logs = logRepository.findByBuildOrderByCreatedAtDesc(build)
                                .stream()
                                .map(this::convertLogToDTO)
                                .collect(Collectors.toList());
                        buildDTO.setLogs(logs);
                        buildDTO.setLogCount(logs.size());
                        return buildDTO;
                    })
                    .collect(Collectors.toList());

            pipelineDTO.setBuilds(builds);

            Map<String, Object> result = new HashMap<>();
            result.put("status", "success");
            result.put("data", pipelineDTO);
            result.put("timestamp", LocalDateTime.now());

            try {
                kafkaTemplate.send(kafkaTopic, "last_pipeline", objectMapper.writeValueAsString(result));
            } catch (Exception e) {
                System.err.println("⚠️ Kafka error: " + e.getMessage());
            }

            return result;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error: " + e.getMessage());
        }
    }

    public Map<String, Object> getAllBuildsWithoutLogs() {
        try {
            List<BuildDTO> builds = buildRepository.findAll()
                    .stream()
                    .map(build -> {
                        BuildDTO dto = convertBuildToDTO(build);
                        dto.setLogCount(build.getLogs() != null ? build.getLogs().size() : 0);
                        dto.setLogs(null);
                        return dto;
                    })
                    .collect(Collectors.toList());

            Map<String, Object> result = new HashMap<>();
            result.put("status", "success");
            result.put("totalBuilds", builds.size());
            result.put("data", builds);
            result.put("timestamp", LocalDateTime.now());

            try {
                kafkaTemplate.send(kafkaTopic, "all_builds_no_logs", objectMapper.writeValueAsString(result));
            } catch (Exception e) {
                System.err.println("⚠️ Kafka error: " + e.getMessage());
            }

            return result;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error: " + e.getMessage());
        }
    }

    // ==================== CONVERTERS (DTO) ====================

    private PipelineDTO convertPipelineToDTO(Pipeline pipeline) {
        return new PipelineDTO(
                pipeline.getId(),
                pipeline.getName(),
                pipeline.getJenkinsUrl(),
                pipeline.getBuilds() != null ? pipeline.getBuilds().size() : 0,
                pipeline.getCreatedAt(),
                pipeline.getUpdatedAt()
        );
    }

private BuildDTO convertBuildToDTO(Build build) {
    // Force Hibernate à charger la collection
    List<Log> logs = build.getLogs();
    if (logs != null) logs.size(); // force le fetch

    List<LogDTO> logDTOs = new ArrayList<>();
    if (logs != null) {
        for (Log log : logs) {
            LogDTO dto = new LogDTO();
            dto.setId(log.getId());
             dto.setLogLevel(log.getLogLevel() != null ? log.getLogLevel().toString() : null);
            dto.setMessage(log.getMessage());
            dto.setStackTrace(log.getStackTrace());
            dto.setCreatedAt(log.getCreatedAt());
            logDTOs.add(dto);
        }
    }

    BuildDTO dto = new BuildDTO();
    dto.setId(build.getId());
    dto.setBuildNumber(build.getBuildNumber());
    dto.setStatus(build.getStatus().toString());
    dto.setStartTime(build.getStartTime());
    dto.setEndTime(build.getEndTime());
    dto.setTriggeredBy(build.getTriggeredBy());
    dto.setPipelineId(build.getPipeline().getId());
    dto.setCreatedAt(build.getCreatedAt());
    dto.setLogs(logDTOs);

    // DEBUG
    System.out.println("DEBUG: BuildDTO logs size = " + logDTOs.size());
    for (LogDTO l : logDTOs) {
        System.out.println("Log: " + l.getMessage());
    }

    return dto;
}



    private LogDTO convertLogToDTO(Log log) {
        return new LogDTO(
                log.getId(),
                log.getLogLevel().toString(),
                log.getMessage(),
                log.getStackTrace(),
                log.getCreatedAt()
        );
    }
}