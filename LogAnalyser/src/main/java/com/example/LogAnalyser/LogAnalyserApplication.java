package com.example.LogAnalyser;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class LogAnalyserApplication {

	public static void main(String[] args) {
		SpringApplication.run(LogAnalyserApplication.class, args);
	}

}
