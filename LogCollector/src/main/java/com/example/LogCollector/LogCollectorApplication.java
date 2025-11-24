package com.example.LogCollector;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class LogCollectorApplication {

    public static void main(String[] args) {
        SpringApplication.run(LogCollectorApplication.class, args);
    }


}

