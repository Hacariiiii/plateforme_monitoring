package com.example.LogCollector.Entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Entity
@Table(name = "build")
public class Build {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "build_number", nullable = false)
    private Integer buildNumber;

    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    private BuildStatus status;

    @Column(name = "start_time")
    private LocalDateTime startTime;

    @Column(name = "end_time")
    private LocalDateTime endTime;

    @Column(name = "duration")
    private Long duration;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "triggered_by")
    private String triggeredBy;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pipeline_id", nullable = false)
    private Pipeline pipeline;

    @OneToMany(mappedBy = "build", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Log> logs;

    public Build() {}

    public Build(Pipeline pipeline, Integer buildNumber, BuildStatus status) {
        this.pipeline = pipeline;
        this.buildNumber = buildNumber;
        this.status = status;
    }

    @PrePersist
    public void onCreate() {
        createdAt = LocalDateTime.now();
        startTime = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    public void onUpdate() {
        updatedAt = LocalDateTime.now();

        if (startTime != null && endTime != null) {
            duration = java.time.Duration.between(startTime, endTime).toSeconds();
        }
    }
}
