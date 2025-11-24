package com.example.LogAnalyser.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
@Data
@Entity
@Table(name = "pipelines")
public class PipelineEntity {

    @Id
    private String id;

    private String name;

    private LocalDateTime createdAt;

 }
