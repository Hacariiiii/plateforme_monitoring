package com.example.LogAnalyser.controller;

import com.example.LogAnalyser.dto.BuildMessageDTO;
import com.example.LogAnalyser.entity.BuildEntity;
import com.example.LogAnalyser.entity.LogEntity;
import com.example.LogAnalyser.entity.PipelineEntity;
import com.example.LogAnalyser.repository.BuildRepository;
import com.example.LogAnalyser.repository.LogRepository;
import com.example.LogAnalyser.repository.PipelineRepository;
import com.example.LogAnalyser.service.BuildAnalyzerService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;



    // ============================================================
    // 🟢 TEST : Vérifier si le microservice Analyzer marche
    // ============================================================
   @RestController
@RequestMapping("/api/analyzer")
public class AnalyzerController {

    private final BuildAnalyzerService analyzerService;
    private final BuildRepository buildRepository;
        private final LogRepository logRepository;


    public AnalyzerController(BuildAnalyzerService analyzerService, BuildRepository buildRepository, LogRepository logRepository) {
        this.analyzerService = analyzerService;
        this.buildRepository = buildRepository;
        this.logRepository = logRepository;
    }

@PostMapping("/builds/analyze")
public ResponseEntity<Map<String, Object>> analyzeBuild(@RequestBody BuildMessageDTO messageDTO) {
    if (messageDTO == null || messageDTO.getData() == null || messageDTO.getData().isEmpty()) {
        return ResponseEntity.badRequest()
                .body(Map.of("status", "error", "message", "Build data missing"));
    }

    // Analyse + sauvegarde et récupération du résumé
    Map<String, Object> analysisResult = analyzerService.processBuild(messageDTO.getData());

    return ResponseEntity.ok(analysisResult);
}

    @GetMapping("/builds")
public ResponseEntity<List<BuildEntity>> getAllBuilds() {
    return ResponseEntity.ok(buildRepository.findAll());
}
  // GET pour récupérer les logs d'un build spécifique
    @GetMapping("/builds/{id}/logs")
    public ResponseEntity<List<LogEntity>> getBuildLogs(@PathVariable Long id) {
        return ResponseEntity.ok(logRepository.findByBuildId(id));
    }
}
