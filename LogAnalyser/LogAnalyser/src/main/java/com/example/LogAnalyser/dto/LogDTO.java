package com.example.LogAnalyser.dto;

import java.time.LocalDateTime;

import lombok.Data;
@Data
public class LogDTO {

    private Long id;
    private String logLevel;
    private String message;
    private String stackTrace;
    private LocalDateTime createdAt;

    // Getters + Setters
}
