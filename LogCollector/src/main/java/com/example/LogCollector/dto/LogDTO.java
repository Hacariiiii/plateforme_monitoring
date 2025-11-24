package com.example.LogCollector.dto;

import java.time.LocalDateTime;
import com.fasterxml.jackson.annotation.JsonFormat;


public class LogDTO {
    private Long id;
    private String logLevel;
    private String message;
    private String stackTrace;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime timestamp;

    public LogDTO() {}

    public LogDTO(Long id, String logLevel, String message, String stackTrace, LocalDateTime createdAt) {
        this.id = id;
        this.logLevel = logLevel;
        this.message = message;
        this.stackTrace = stackTrace;
        this.createdAt = createdAt;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getLogLevel() { return logLevel; }
    public void setLogLevel(String logLevel) { this.logLevel = logLevel; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public String getStackTrace() { return stackTrace; }
    public void setStackTrace(String stackTrace) { this.stackTrace = stackTrace; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
}