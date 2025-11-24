package com.example.LogCollector.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;

import com.example.LogCollector.service.LogService;

public class LastBuildLogController {
     @Autowired
    private LogService lastBuildLogService;

    @PostMapping("/send-last-build-logs")
    public ResponseEntity<String> sendLastBuildLogs() {
        lastBuildLogService.sendLastBuildLogsToAnalyzer();
        return ResponseEntity.ok("Last build logs sent to Analyzer");
    }
}
