package com.example.LogAnalyser.controller;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.LogAnalyser.entity.BuildEntity;
import com.example.LogAnalyser.entity.LogEntity;
import com.example.LogAnalyser.repository.BuildRepository;

import lombok.extern.java.Log;

@RestController
@RequestMapping("/api/builds")
public class LogController {

   
    @Autowired
    private BuildRepository buildRepository;

    @GetMapping("/{id}/logs")
public List<LogEntity> getLogs(@PathVariable Long id) {
    return buildRepository.findById(id)
            .map(build -> {
                build.getLogs().size(); // force Hibernate à charger les logs
                return build.getLogs();
            })
            .orElse(Collections.emptyList());
}

}
