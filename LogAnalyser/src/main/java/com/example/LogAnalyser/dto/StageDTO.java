package com.example.LogAnalyser.dto;

import java.util.List;

import lombok.Data;
@Data
public class StageDTO {

    private Long id;
    private String stageName;
    private String status;

    private List<LogDTO> logs;

    // Getters + Setters

 
    
}
