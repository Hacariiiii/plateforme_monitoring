package com.example.LogAnalyser.repository;

import com.example.LogAnalyser.entity.BuildEntity;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface BuildRepository extends JpaRepository<BuildEntity, Long> {
        @Query("SELECT b FROM BuildEntity b LEFT JOIN FETCH b.logs WHERE b.id = :id")
    Optional<BuildEntity> findByIdWithLogs(@Param("id") Long id);
}
