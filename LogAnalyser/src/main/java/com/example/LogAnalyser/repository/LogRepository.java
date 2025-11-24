package com.example.LogAnalyser.repository;

import com.example.LogAnalyser.entity.LogEntity;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface LogRepository extends JpaRepository<LogEntity, Long> {
    List<LogEntity> findByBuildId(Long buildId);
}
