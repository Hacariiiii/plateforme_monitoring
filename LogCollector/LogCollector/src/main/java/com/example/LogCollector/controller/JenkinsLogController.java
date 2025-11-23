package com.example.LogCollector.controller;

import com.example.LogCollector.service.JenkinsLogService;
import com.example.LogCollector.dto.PipelineDTO;
import com.example.LogCollector.dto.BuildDTO;
import com.example.LogCollector.dto.LogDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/jenkins-logs")
@CrossOrigin(origins = "*")
public class JenkinsLogController {

    @Autowired
    private JenkinsLogService logService;

    @Value("${webhook.secret-token}")
    private String webhookSecretToken;

    // ==================== WEBHOOK ====================

    /**
     * WEBHOOK ENDPOINT - Called by Jenkins
     * POST /api/jenkins-logs/webhook?jobName=project5&buildNumber=10&buildStatus=SUCCESS&token=xxx
     */
    @PostMapping("/webhook")
    public ResponseEntity<?> webhookCollectLog(
            @RequestParam String jobName,
            @RequestParam Integer buildNumber,
            @RequestParam String buildStatus,
            @RequestParam String token) {
        try {
            if (!token.equals(webhookSecretToken)) {
                System.err.println("❌ Invalid webhook token");
                Map<String, String> error = new HashMap<>();
                error.put("status", "error");
                error.put("message", "Invalid webhook token");
                return ResponseEntity.status(403).body(error);
            }

            System.out.println("✓ Webhook received - Job: " + jobName + ", Build: " + buildNumber);

            BuildDTO build = logService.collectAndSaveLogs(jobName, buildNumber, buildStatus);

            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "Build logs collected and saved");
            response.put("data", build);

            System.out.println("✅ Webhook processed successfully");
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            System.err.println("❌ Webhook error: " + e.getMessage());
            Map<String, String> error = new HashMap<>();
            error.put("status", "error");
            error.put("message", e.getMessage());
            return ResponseEntity.status(500).body(error);
        }
    }

    // ==================== BASIC ENDPOINTS ====================

    /**
     * GET all pipelines (simple list)
     * GET /api/jenkins-logs/pipelines
     */
    @GetMapping("/pipelines")
    public ResponseEntity<?> getAllPipelines() {
        try {
            List<PipelineDTO> pipelines = logService.getAllPipelines();
            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("totalPipelines", pipelines.size());
            response.put("data", pipelines);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("status", "error");
            error.put("message", e.getMessage());
            return ResponseEntity.status(500).body(error);
        }
    }

    /**
     * GET pipeline by name
     * GET /api/jenkins-logs/pipelines/search?name=project5
     */
    @GetMapping("/pipelines/search")
    public ResponseEntity<?> getPipelineByName(@RequestParam String name) {
        try {
            PipelineDTO pipeline = logService.getPipelineByName(name);
            if (pipeline == null) {
                return ResponseEntity.notFound().build();
            }
            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("data", pipeline);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("status", "error");
            error.put("message", e.getMessage());
            return ResponseEntity.status(500).body(error);
        }
    }

    /**
     * GET all builds for a pipeline
     * GET /api/jenkins-logs/pipelines/{pipelineId}/builds
     */
    @GetMapping("/pipelines/{pipelineId}/builds")
    public ResponseEntity<?> getBuildsByPipeline(@PathVariable Long pipelineId) {
        try {
            List<BuildDTO> builds = logService.getBuildsByPipeline(pipelineId);
            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("pipelineId", pipelineId);
            response.put("totalBuilds", builds.size());
            response.put("data", builds);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("status", "error");
            error.put("message", e.getMessage());
            return ResponseEntity.status(500).body(error);
        }
    }

    /**
     * GET build details by ID
     * GET /api/jenkins-logs/builds/{buildId}
     */
    @GetMapping("/builds/{buildId}")
    public ResponseEntity<?> getBuildById(@PathVariable Long buildId) {
        try {
            BuildDTO build = logService.getBuildById(buildId);
            if (build == null) {
                return ResponseEntity.notFound().build();
            }
            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("data", build);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("status", "error");
            error.put("message", e.getMessage());
            return ResponseEntity.status(500).body(error);
        }
    }

    /**
     * GET all logs for a build
     * GET /api/jenkins-logs/builds/{buildId}/logs
     */
    @GetMapping("/builds/{buildId}/logs")
    public ResponseEntity<?> getLogsByBuild(@PathVariable Long buildId) {
        try {
            List<LogDTO> logs = logService.getLogsByBuild(buildId);
            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("buildId", buildId);
            response.put("totalLogs", logs.size());
            response.put("data", logs);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("status", "error");
            error.put("message", e.getMessage());
            return ResponseEntity.status(500).body(error);
        }
    }

    /**
     * GET error logs only for a build
     * GET /api/jenkins-logs/builds/{buildId}/errors
     */
    @GetMapping("/builds/{buildId}/errors")
    public ResponseEntity<?> getErrorLogsByBuild(@PathVariable Long buildId) {
        try {
            List<LogDTO> errors = logService.getErrorLogsByBuild(buildId);
            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("buildId", buildId);
            response.put("totalErrors", errors.size());
            response.put("data", errors);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("status", "error");
            error.put("message", e.getMessage());
            return ResponseEntity.status(500).body(error);
        }
    }

    // ==================== HEALTH & INFO ====================

    /**
     * HEALTH CHECK
     * GET /api/jenkins-logs/health
     */
    @GetMapping("/health")
    public ResponseEntity<?> health() {
        try {
            List<PipelineDTO> pipelines = logService.getAllPipelines();

            Map<String, Object> response = new HashMap<>();
            response.put("status", "UP");
            response.put("service", "Jenkins Log Collector");
            response.put("totalPipelines", pipelines.size());
            response.put("timestamp", java.time.LocalDateTime.now().toString());
            response.put("pipelines", pipelines);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("status", "DOWN");
            error.put("message", e.getMessage());
            return ResponseEntity.status(500).body(error);
        }
    }

    /**
     * API INFO
     * GET /api/jenkins-logs/info
     */
    @GetMapping("/info")
    public ResponseEntity<?> info() {
        Map<String, Object> info = new HashMap<>();
        info.put("service", "Jenkins Log Collector Microservice");
        info.put("version", "2.0.0");
        info.put("structure", "Pipeline -> Build -> Logs");
        info.put("endpoints", new HashMap<String, String>() {{
            put("Webhook", "POST /api/jenkins-logs/webhook?jobName={name}&buildNumber={num}&buildStatus={status}&token={token}");
            put("Get All Pipelines", "GET /api/jenkins-logs/pipelines");
            put("Get Pipeline by Name", "GET /api/jenkins-logs/pipelines/search?name={name}");
            put("Get Builds for Pipeline", "GET /api/jenkins-logs/pipelines/{pipelineId}/builds");
            put("Get Build Details", "GET /api/jenkins-logs/builds/{buildId}");
            put("Get Build Logs", "GET /api/jenkins-logs/builds/{buildId}/logs");
            put("Get Error Logs", "GET /api/jenkins-logs/builds/{buildId}/errors");
            put("===== KAFKA ENDPOINTS =====", "");
            put("Get All Pipelines with ALL Builds and Logs", "GET /api/jenkins-logs/kafka/pipelines/all");
            put("Get All Builds with ALL Logs", "GET /api/jenkins-logs/kafka/builds/all");
            put("Get All Builds WITHOUT Logs", "GET /api/jenkins-logs/kafka/builds/no-logs");
            put("Get Last Build with ALL Logs", "GET /api/jenkins-logs/kafka/builds/last");
            put("Get Last Pipeline with ALL Builds and Logs", "GET /api/jenkins-logs/kafka/pipelines/last");
            put("===== SMART ENDPOINTS =====", "");
            put("Last Pipeline Summary (No Logs)", "GET /api/jenkins-logs/smart/pipeline/last-summary");
            put("Last Build Important Logs", "GET /api/jenkins-logs/smart/build/last-important");
            put("All Pipelines Summary (No Logs)", "GET /api/jenkins-logs/smart/pipelines/all-summary");
            put("Health Check", "GET /api/jenkins-logs/health");
            put("API Info", "GET /api/jenkins-logs/info");
        }});

        return ResponseEntity.ok(info);
    }

    // ==================== KAFKA ENDPOINTS (Advanced) ====================

    /**
     * 1. GET all pipelines with ALL builds and ALL logs
     * GET /api/jenkins-logs/kafka/pipelines/all
     */
    @GetMapping("/kafka/pipelines/all")
    public ResponseEntity<?> getAllPipelinesWithKafka() {
        try {
            System.out.println("📍 GET /kafka/pipelines/all");
            Map<String, Object> result = logService.getAllPipelinesWithBuildsAndLogs();
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of(
                    "status", "error",
                    "message", e.getMessage()
            ));
        }
    }

    /**
     * 2. GET all builds with ALL logs
     * GET /api/jenkins-logs/kafka/builds/all
     */
    @GetMapping("/kafka/builds/all")
    public ResponseEntity<?> getAllBuildsWithKafka() {
        try {
            System.out.println("📍 GET /kafka/builds/all");
            Map<String, Object> result = logService.getAllBuildsWithLogsData();
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of(
                    "status", "error",
                    "message", e.getMessage()
            ));
        }
    }

    /**
     * 3. GET all builds WITHOUT logs
     * GET /api/jenkins-logs/kafka/builds/no-logs
     */
    @GetMapping("/kafka/builds/no-logs")
    public ResponseEntity<?> getAllBuildsWithoutLogsKafka() {
        try {
            System.out.println("📍 GET /kafka/builds/no-logs");
            Map<String, Object> result = logService.getAllBuildsWithoutLogs();
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of(
                    "status", "error",
                    "message", e.getMessage()
            ));
        }
    }

    /**
     * 4. GET LAST build with ALL logs
     * GET /api/jenkins-logs/kafka/builds/last
     */


    @GetMapping("/builds/last")
    public ResponseEntity<Map<String, Object>> getLastBuild() {
        Map<String, Object> result = logService.getLastBuildWithLogsData();
        return ResponseEntity.ok(result);
    }

    /**
     * 5. GET LAST pipeline with ALL builds and ALL logs
     * GET /api/jenkins-logs/kafka/pipelines/last
     */
    @GetMapping("/kafka/pipelines/last")
    public ResponseEntity<?> getLastPipelineWithKafka() {
        try {
            System.out.println("📍 GET /kafka/pipelines/last");
            Map<String, Object> result = logService.getLastPipelineWithBuildsAndLogs();
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of(
                    "status", "error",
                    "message", e.getMessage()
            ));
        }
    }
    

    // ==================== SMART ENDPOINTS (جديد) ====================

    /**
     * 1️⃣ Last Pipeline Summary (بدون logs)
     * GET /api/jenkins-logs/smart/pipeline/last-summary
     * ✅ آخر pipeline مع builds الخاصة بها (بدون logs)
     */
    @GetMapping("/smart/pipeline/last-summary")
    public ResponseEntity<?> getLastPipelineSummary() {
        try {
            System.out.println("📍 GET /smart/pipeline/last-summary");
            Map<String, Object> result = logService.getLastPipelineSummary();
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of(
                    "status", "error",
                    "message", e.getMessage()
            ));
        }
    }

    /**
     * 2️⃣ Last Build with Important Logs Only (ERROR, WARN)
     * GET /api/jenkins-logs/smart/build/last-important
     * ✅ آخر build مع logs المهمة فقط + Pipeline info
     */
    @GetMapping("/smart/build/last-important")
    public ResponseEntity<?> getLastBuildWithImportantLogs() {
        try {
            System.out.println("📍 GET /smart/build/last-important");
            Map<String, Object> result = logService.getLastBuildWithImportantLogs();
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of(
                    "status", "error",
                    "message", e.getMessage()
            ));
        }
    }

    /**
     * 3️⃣ All Pipelines Summary (بدون logs)
     * GET /api/jenkins-logs/smart/pipelines/all-summary
     * ✅ كل pipelines مع builds الخاصة بها (بدون logs)
     */
    @GetMapping("/smart/pipelines/all-summary")
    public ResponseEntity<?> getAllPipelinesSummary() {
        try {
            System.out.println("📍 GET /smart/pipelines/all-summary");
            Map<String, Object> result = logService.getAllPipelinesSummary();
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of(
                    "status", "error",
                    "message", e.getMessage()
            ));
        }
    }
    
}