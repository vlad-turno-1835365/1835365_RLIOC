package com.mars.ingestion;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Main Spring Boot application class for Mars Ingestion Service
 * This service ingests Mars IoT telemetry data through scheduled API calls and streaming
 */
@SpringBootApplication
@ComponentScan(basePackages = {"com.mars.ingestion", "com.mars.kafka", "com.mars.normalization"})
@EnableScheduling
@EnableAsync
public class MarsIngestionApplication {
    public static void main(String[] args) {
        SpringApplication.run(MarsIngestionApplication.class, args);
    }
}
