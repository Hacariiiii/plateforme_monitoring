package com.example.LogAnalyser.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.util.List;
@Data
@Entity
@Table(name = "stages")
public class StageEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String stageName; // build / test / deploy
    private String status;    // success / failed / running

    @ManyToOne
    @JoinColumn(name = "build_id")
    private BuildEntity build;

    @OneToMany(mappedBy = "stage", cascade = CascadeType.ALL)
    private List<LogEntity> logs;

    // Getters + Setters
}
