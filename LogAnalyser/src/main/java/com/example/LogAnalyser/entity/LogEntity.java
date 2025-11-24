package com.example.LogAnalyser.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonBackReference;
@Data
@Entity
@Table(name = "logs")
public class LogEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String logLevel;
    private String message;
    private String stackTrace;

    @ManyToOne
    @JoinColumn(name = "build_id")

    private BuildEntity build;

    @ManyToOne
    @JoinColumn(name = "stage_id")
    private StageEntity stage;

    private LocalDateTime createdAt;

    // Getters + Setters
}
