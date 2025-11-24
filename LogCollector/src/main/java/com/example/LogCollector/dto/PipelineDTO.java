package com.example.LogCollector.dto;


import java.util.List;
import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDateTime;

public class PipelineDTO {
    private Long id;
    private String name;
    private String jenkinsUrl;
    private Integer totalBuilds;

    @JsonFormat(pattern="yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
    @JsonFormat(pattern="yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;

    private List<BuildDTO> builds;

    public PipelineDTO() {}

    public PipelineDTO(Long id, String name, String jenkinsUrl, Integer totalBuilds,
                       LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.name = name;
        this.jenkinsUrl = jenkinsUrl;
        this.totalBuilds = totalBuilds;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getJenkinsUrl() { return jenkinsUrl; }
    public void setJenkinsUrl(String jenkinsUrl) { this.jenkinsUrl = jenkinsUrl; }

    public Integer getTotalBuilds() { return totalBuilds; }
    public void setTotalBuilds(Integer totalBuilds) { this.totalBuilds = totalBuilds; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public List<BuildDTO> getBuilds() { return builds; }
    public void setBuilds(List<BuildDTO> builds) { this.builds = builds; }
}