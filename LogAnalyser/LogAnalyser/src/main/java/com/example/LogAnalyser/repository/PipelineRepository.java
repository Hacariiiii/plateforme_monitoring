package com.example.LogAnalyser.repository;

import com.example.LogAnalyser.entity.PipelineEntity;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PipelineRepository extends JpaRepository<PipelineEntity, String> {
        Optional<PipelineEntity> findByName(String name);
}
