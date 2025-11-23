package com.example.LogCollector.repository;

import com.example.LogCollector.Entity.Build;
import com.example.LogCollector.Entity.Log;
import com.example.LogCollector.Entity.LogLevel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface LogRepository extends JpaRepository<Log, Long> {
    List<Log> findByBuildOrderByCreatedAtDesc(Build build);
    List<Log> findByBuildAndLogLevel(Build build, LogLevel level);
    List<Log> findByBuild(Build build);
}