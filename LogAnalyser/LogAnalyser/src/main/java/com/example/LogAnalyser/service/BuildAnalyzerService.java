package com.example.LogAnalyser.service;

import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.LogAnalyser.entity.BuildEntity;
import com.example.LogAnalyser.entity.LogEntity;
import com.example.LogAnalyser.entity.PipelineEntity;
import com.example.LogAnalyser.repository.BuildRepository;
import com.example.LogAnalyser.repository.LogRepository;
import com.example.LogAnalyser.repository.PipelineRepository;

@Service
public class BuildAnalyzerService {

    // Assure-toi d'avoir tes repositories injectés ici
    private final PipelineRepository pipelineRepository;
    private final BuildRepository buildRepository;
    private final LogRepository logRepository;

    public BuildAnalyzerService(PipelineRepository pipelineRepository,
                                BuildRepository buildRepository,
                                LogRepository logRepository) {
        this.pipelineRepository = pipelineRepository;
        this.buildRepository = buildRepository;
        this.logRepository = logRepository;
    }

  @Transactional
public Map<String, Object> processBuild(List<Map<String, Object>> dataList) {
    Map<String, Object> result = new HashMap<>();
    List<String> savedBuilds = new ArrayList<>();

    if (dataList == null || dataList.isEmpty()) {
        result.put("status", "error");
        result.put("message", "Pas de builds à traiter (dataList vide ou null)");
        return result;
    }

    for (Map<String, Object> data : dataList) {
        Object pipelineObj = data.get("pipelineId");
        if (pipelineObj == null) continue;

String pipelineId = data.get("pipelineId").toString();

        Integer buildNumber = (data.get("buildNumber") instanceof Number)
                ? ((Number) data.get("buildNumber")).intValue()
                : null;

        String status = (String) data.get("status");
        String triggeredBy = data.get("triggeredBy") != null ? data.get("triggeredBy").toString() : null;
        String startTimeStr = data.get("startTime") != null ? data.get("startTime").toString() : null;

        // Récupération des logs
        List<Map<String, Object>> logs = new ArrayList<>();
        Object logsObj = data.get("logs");
        if (logsObj instanceof List<?>) {
            for (Object o : (List<?>) logsObj) {
                if (o instanceof Map<?, ?>) {
                    logs.add((Map<String, Object>) o);
                }
            }
        }

        System.out.println("Logs reçus pour build #" + buildNumber + ": " + logs.size());

        // Vérifier / créer le pipeline
        PipelineEntity pipeline = pipelineRepository.findById(pipelineId)
                .orElseGet(() -> {
                    PipelineEntity p = new PipelineEntity();
                    p.setId(pipelineId);
                    p.setName("Pipeline " + pipelineId);
                    p.setCreatedAt(LocalDateTime.now());
                    return pipelineRepository.save(p);
                });

        // Sauvegarde du build
        BuildEntity buildEntity = new BuildEntity();
        buildEntity.setBuildNumber(buildNumber);
        buildEntity.setStatus(status);
        buildEntity.setTriggeredBy(triggeredBy);
        buildEntity.setPipeline(pipeline);

        if (startTimeStr != null && !startTimeStr.equals("null")) {
            try {
                buildEntity.setStartTime(LocalDateTime.parse(startTimeStr));
            } catch (Exception e) {
                System.err.println("⚠️ startTime invalide pour build #" + buildNumber);
            }
        }
        buildEntity.setCreatedAt(LocalDateTime.now());

        buildRepository.saveAndFlush(buildEntity);

        // Sauvegarde des logs
        if (!logs.isEmpty()) {
            List<LogEntity> entities = new ArrayList<>();
            for (Map<String, Object> log : logs) {
                LogEntity logEntity = new LogEntity();
                logEntity.setMessage(String.valueOf(log.get("message")));
                logEntity.setLogLevel(String.valueOf(log.get("logLevel")));
                logEntity.setCreatedAt(LocalDateTime.now());
                logEntity.setBuild(buildEntity);
                entities.add(logEntity);
            }
            logRepository.saveAll(entities);
            System.out.println("Logs sauvegardés: " + entities.size() + " pour build #" + buildNumber);
        }

        savedBuilds.add("Build " + buildNumber + " pipeline " + pipelineId + " sauvegardé avec " + logs.size() + " logs");
    }

    result.put("status", "success");
    result.put("savedBuilds", savedBuilds);
    result.put("message", "Analyse terminée pour " + savedBuilds.size() + " build(s)");
    return result;
}
}