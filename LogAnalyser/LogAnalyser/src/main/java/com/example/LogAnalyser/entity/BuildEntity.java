package com.example.LogAnalyser.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonManagedReference;

@Entity
@Data
@Table(name = "builds")
public class BuildEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)

    private Long id; // ID venant du Collector

    private Integer buildNumber;
    private String status;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String triggeredBy;

    @ManyToOne
    @JoinColumn(name = "pipeline_id")
    private PipelineEntity pipeline;

    @OneToMany(mappedBy = "build", cascade = CascadeType.ALL)
    private List<StageEntity> stages;

    @OneToMany(mappedBy = "build", cascade = CascadeType.ALL)
    private List<LogEntity> logs;

    private LocalDateTime createdAt;



    // Getters + Setters
}
