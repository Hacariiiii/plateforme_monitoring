package com.example.LogCollector.repository;
import com.example.LogCollector.Entity.Pipeline;
import com.example.LogCollector.Entity.Pipeline;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface PipelineRepository extends JpaRepository<Pipeline, Long> {
    Optional<Pipeline> findByName(String name);
    boolean existsByName(String name);
    Pipeline findTopByOrderByCreatedAtDesc();

}
