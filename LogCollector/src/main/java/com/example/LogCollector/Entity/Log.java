package com.example.LogCollector.Entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "log")
public class Log {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "log_level")
    @Enumerated(EnumType.STRING)
    private LogLevel logLevel;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String message;

    @Column(columnDefinition = "TEXT")
    private String stackTrace;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "build_id", nullable = false)
    private Build build;

    public Log() {}

    public Log(Build build, LogLevel logLevel, String message) {
        this.build = build;
        this.logLevel = logLevel;
        this.message = message;
    }

    @PrePersist
    public void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
