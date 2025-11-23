package com.example.LogAnalyser.dto;

import java.time.LocalDateTime;
import java.util.List;

import lombok.Data;
@Data
public class BuildDTO {

    private Long id;
    private int buildNumber;
    private String status;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String triggeredBy;
    private int logCount;

    private String pipelineId;

    private List<StageDTO> stages;
    private List<LogDTO> logs;

    // Getters + Setters
}
