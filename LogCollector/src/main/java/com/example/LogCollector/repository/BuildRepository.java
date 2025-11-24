package com.example.LogCollector.repository;

import com.example.LogCollector.Entity.Build;
import com.example.LogCollector.Entity.Pipeline;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface BuildRepository extends JpaRepository<Build, Long> {
    List<Build> findByPipelineOrderByCreatedAtDesc(Pipeline pipeline);
    Optional<Build> findByPipelineAndBuildNumber(Pipeline pipeline, Integer buildNumber);
    List<Build> findByPipeline(Pipeline pipeline);
    List<Build> findByPipelineId(Long pipelineId);
    List<Build> findAllByOrderByCreatedAtDesc();

    // ✅ جديد - آخر build
    Optional<Build> findFirstByOrderByCreatedAtDesc();
    Optional<Build> findTopByOrderByCreatedAtDesc();
}