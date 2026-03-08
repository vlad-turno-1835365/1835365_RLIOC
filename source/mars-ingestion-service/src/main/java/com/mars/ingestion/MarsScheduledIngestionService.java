package com.mars.ingestion;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mars.normalization.model.*;
import com.mars.kafka.service.MarsEventPublisher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Scheduled service for Mars IoT sensor data ingestion
 * Periodically polls sensor data from Mars simulator and maps to response models
 */
@Service
public class MarsScheduledIngestionService {
    
    private static final Logger log = LoggerFactory.getLogger(MarsScheduledIngestionService.class);
    private static final HttpClient client = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();
    
    private static final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule())
            .disable(com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
            .disable(com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);

    @Value("${mars.simulator.url:http://localhost:8080}")
    private String simulatorUrl;

    @Autowired
    private MarsEventPublisher eventPublisher;

    private int ingestionCount = 0;

    /**
     * Scheduled method that runs every 30 seconds to ingest Mars IoT sensor data
     */
    @Scheduled(fixedRate = 30000) // Run every 30 seconds
    public void performScheduledIngestion() {
        ingestionCount++;
        log.info("\n🚀 Mars IoT Ingestion Run #{} - {}", ingestionCount, java.time.LocalTime.now());
        log.info("==================================================");

        try {
            // Check simulator health
            checkSimulatorHealth();
            
            // Only proceed with sensor polling if health check passed
            pollAndMapSensorData();
            
            log.info("✅ Ingestion run #{} completed successfully!", ingestionCount);
            
        } catch (Exception e) {
            log.error("❌ Ingestion run #{} failed: {}", ingestionCount, e.getMessage());
            if (e.getCause() != null) {
                log.error("   Caused by: {}", e.getCause().getMessage());
            }
            log.error("   Stack trace: {}", e.getClass().getSimpleName());
            
            // If health check failed, skip sensor polling for this run
            if (e.getMessage().contains("health check failed") || e.getMessage().contains("empty health response")) {
                log.warn("   ⚠️ Skipping sensor polling due to health check failure");
            }
        }
    }

    /**
     * Check simulator health status
     */
    private void checkSimulatorHealth() throws Exception {
        log.info("1. Simulator Health Check:");
        String response = sendGet(simulatorUrl + "/health");
        log.info("   Response: {}", response);
        
        // Validate health response
        if (response == null || response.trim().isEmpty()) {
            throw new Exception("Simulator returned empty health response");
        }
        
        // Check if response contains "ok" (case-insensitive)
        if (!response.toLowerCase().contains("ok")) {
            throw new Exception("Simulator health check failed: " + response);
        }
        
        log.info("   ✅ Simulator is healthy");
    }

    /**
     * Poll and map sensor data from all available sensors
     */
    private void pollAndMapSensorData() throws Exception {
        log.info("2. Sensor Data Ingestion:");
        
        // All available sensors to poll
        String[] sensors = {
            "greenhouse_temperature",
            "entrance_humidity", 
            "co2_hall",
            "hydroponic_ph",
            "water_tank_level",
            "corridor_pressure",
            "air_quality_pm25",
            "air_quality_voc"
        };
        
        for (int i = 0; i < sensors.length; i++) {
            log.info("   2.{} {}: {}", i + 1, sensors[i]);
            try {
                String response = sendGet(simulatorUrl + "/api/sensors/" + sensors[i]);
                
                if (response == null || response.trim().isEmpty()) {
                    log.warn("      Warning: Empty response received");
                } else {
                    // Map response to appropriate model based on sensor type
                    Object sensorData = mapSensorResponse(sensors[i], response);
                    log.info("      Mapped: {}", sensorData);
                    
                    // Publish sensor data to Kafka based on sensor type
                    switch (sensors[i]) {
                        case "greenhouse_temperature":
                        case "entrance_humidity":
                        case "co2_hall":
                        case "corridor_pressure":
                            eventPublisher.publishScalarEvent((RestScalarV1) sensorData);
                            break;
                        case "hydroponic_ph":
                        case "air_quality_voc":
                            eventPublisher.publishChemistryEvents((RestChemistryV1) sensorData);
                            break;
                        case "air_quality_pm25":
                            eventPublisher.publishParticulateEvents((RestParticulateV1) sensorData);
                            break;
                        case "water_tank_level":
                            eventPublisher.publishLevelEvents((RestLevelV1) sensorData);
                            break;
                    }
                }
            } catch (Exception e) {
                log.error("      Error ingesting {}: {}", sensors[i], e.getMessage());
            }
        }
    }
    
    /**
     * Map sensor response to appropriate model class
     */
    private Object mapSensorResponse(String sensorName, String jsonResponse) throws Exception {
        switch (sensorName) {
            case "greenhouse_temperature":
            case "entrance_humidity":
            case "co2_hall":
            case "corridor_pressure":
                return objectMapper.readValue(jsonResponse, RestScalarV1.class);
            case "hydroponic_ph":
            case "air_quality_voc":
                return objectMapper.readValue(jsonResponse, RestChemistryV1.class);
            case "air_quality_pm25":
                return objectMapper.readValue(jsonResponse, RestParticulateV1.class);
            case "water_tank_level":
                return objectMapper.readValue(jsonResponse, RestLevelV1.class);
            default:
                throw new IllegalArgumentException("Unknown sensor: " + sensorName);
        }
    }

    /**
     * Send HTTP GET request
     */
    private String sendGet(String url) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofSeconds(5))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        return response.body();
    }
}
