package com.example.LogCollector.dto;

import java.time.LocalDateTime;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonFormat;


public class BuildDTO {
    private Long id;
    private Integer buildNumber;
    private String status;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime startTime;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime endTime;
    private Long duration;
    private String triggeredBy;
    private Integer logCount;
    private Long pipelineId;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;
    private List<LogDTO> logs;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime updatedAt;
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public BuildDTO() {}

    public BuildDTO(Long id, Integer buildNumber, String status, LocalDateTime startTime,
                LocalDateTime endTime, Long duration, String triggeredBy,
                Long pipelineId, LocalDateTime createdAt,
                List<LogDTO> logs) {

    this.id = id;
    this.buildNumber = buildNumber;
    this.status = status;
    this.startTime = startTime;
    this.endTime = endTime;
    this.duration = duration;
    this.triggeredBy = triggeredBy;
    this.pipelineId = pipelineId;
    this.createdAt = createdAt;
    this.logs = logs;

    // logCount dérivé automatiquement
    this.logCount = (logs != null ? logs.size() : 0);
}


    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Integer getBuildNumber() { return buildNumber; }
    public void setBuildNumber(Integer buildNumber) { this.buildNumber = buildNumber; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDateTime getStartTime() { return startTime; }
    public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }

    public LocalDateTime getEndTime() { return endTime; }
    public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }

    public Long getDuration() { return duration; }
    public void setDuration(Long duration) { this.duration = duration; }

    public String getTriggeredBy() { return triggeredBy; }
    public void setTriggeredBy(String triggeredBy) { this.triggeredBy = triggeredBy; }

    public Integer getLogCount() { return logCount; }
    public void setLogCount(Integer logCount) { this.logCount = logCount; }

    public Long getPipelineId() { return pipelineId; }
    public void setPipelineId(Long pipelineId) { this.pipelineId = pipelineId; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public List<LogDTO> getLogs() { return logs; }
    public void setLogs(List<LogDTO> logs) { this.logs = logs; }

}