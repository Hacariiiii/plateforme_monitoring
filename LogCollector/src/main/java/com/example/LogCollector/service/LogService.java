package com.example.LogCollector.service;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.example.LogCollector.Entity.Build;
import com.example.LogCollector.Entity.Log;
import com.example.LogCollector.repository.BuildRepository;

@Service
public class LogService {

    @Autowired
    private BuildRepository buildRepository;

    @Autowired
    private RestTemplate restTemplate;

    private final String analyzerUrl = "http://localhost:8082/api/analyzer/builds/analyze";

    /**
     * Récupère le dernier build et ses logs, les convertit en JSON et envoie à Analyzer
     */
    public void sendLastBuildLogsToAnalyzer() {
        Optional<Build> lastBuildOpt = buildRepository.findTopByOrderByCreatedAtDesc();

        if (lastBuildOpt.isEmpty()) {
            System.out.println("❌ Aucun build trouvé dans la base.");
            return;
        }

        Build build = lastBuildOpt.get();

        // Force Hibernate à charger les logs
        if (build.getLogs() != null) build.getLogs().size();

        Map<String, Object> buildMap = new HashMap<>();
        buildMap.put("id", build.getId());
        buildMap.put("buildNumber", build.getBuildNumber());
        buildMap.put("status", build.getStatus().toString());
        buildMap.put("triggeredBy", build.getTriggeredBy());
        buildMap.put("startTime", build.getStartTime());
        buildMap.put("endTime", build.getEndTime());
        buildMap.put("duration", build.getDuration());
        buildMap.put("createdAt", build.getCreatedAt());
        buildMap.put("pipelineId", build.getPipeline().getId());

        List<Map<String, Object>> logsList = new ArrayList<>();
        if (build.getLogs() != null) {
            for (Log log : build.getLogs()) {
                Map<String, Object> logMap = new HashMap<>();
                logMap.put("id", log.getId());
                logMap.put("logLevel", log.getLogLevel() != null ? log.getLogLevel().toString() : null);
                logMap.put("message", log.getMessage());
                logMap.put("stackTrace", log.getStackTrace());
                logMap.put("createdAt", log.getCreatedAt());
                logsList.add(logMap);
            }
        }

        buildMap.put("logs", logsList);

        Map<String, Object> payload = new HashMap<>();
        payload.put("data", List.of(buildMap));

        // Envoi vers Analyzer
        try {
            restTemplate.postForEntity(analyzerUrl, payload, String.class);
            System.out.println("✅ Last build logs sent to Analyzer: Build #" + build.getBuildNumber());
        } catch (Exception e) {
            System.err.println("❌ Failed to send logs to Analyzer: " + e.getMessage());
        }
    }
}
