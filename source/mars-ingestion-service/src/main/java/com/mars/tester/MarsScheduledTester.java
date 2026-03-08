package com.mars.tester;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mars.tester.model.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class MarsScheduledTester {
    
    private static final HttpClient client = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();
    
    private static final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule())
            .disable(com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
            .disable(com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);

    @Value("${mars.simulator.url:http://localhost:8080}")
    private String simulatorUrl;

    private int testCount = 0;

    @Scheduled(fixedRate = 5000) // Run every 5 seconds
    public void runScheduledTests() {
        testCount++;
        System.out.println("\n🚀 Mars IoT Test Run #" + testCount + " - " + java.time.LocalTime.now());
        System.out.println("==================================================");

        try {
            testHealth();
            testDiscovery();
            testSensors();
            testTopics();
            testActuators();
            testActuatorControl();
            
            System.out.println("✅ Test run #" + testCount + " completed successfully!\n");
        } catch (Exception e) {
            System.err.println("❌ Test run #" + testCount + " failed: " + e.getMessage());
            if (e.getCause() != null) {
                System.err.println("   Caused by: " + e.getCause().getMessage());
            }
            System.err.println("   Stack trace: " + e.getClass().getSimpleName());
        }
    }

    private void testHealth() throws Exception {
        System.out.println("1. Health check:");
        String response = sendGet(simulatorUrl + "/health");
        System.out.println("   Response: " + response);
    }

    private void testDiscovery() throws Exception {
        System.out.println("2. API Discovery:");
        String response = sendGet(simulatorUrl + "/api/discovery");
        
        // Map response to DiscoveryResponse model
        DiscoveryResponse discoveryResponse = objectMapper.readValue(response, DiscoveryResponse.class);
        System.out.println("   Mapped Response: " + discoveryResponse);
    }

    private void testSensors() throws Exception {
        System.out.println("3. REST Sensors:");
        
        // Test all 8 available sensors
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
            System.out.println("   3." + (i + 1) + " " + sensors[i] + ":");
            try {
                String response = sendGet(simulatorUrl + "/api/sensors/" + sensors[i]);
                
                if (response == null || response.trim().isEmpty()) {
                    System.out.println("      Warning: Empty response received");
                } else {
                    // Map response to appropriate model based on sensor type
                    Object sensorData = mapSensorResponse(sensors[i], response);
                    System.out.println("      " + sensorData);
                }
            } catch (Exception e) {
                System.out.println("      Error testing " + sensors[i] + ": " + e.getMessage());
            }
        }
    }
    
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
                return jsonResponse; // fallback to raw string
        }
    }

    private void testTopics() throws Exception {
        System.out.println("4. Telemetry Topics:");
        String response = sendGet(simulatorUrl + "/api/telemetry/topics");
        
        // Map response to TopicsListResponse model
        TopicsListResponse topicsList = objectMapper.readValue(response, TopicsListResponse.class);
        System.out.println("   Mapped Response: " + topicsList);
        
        // Test streaming for each topic type
        testPowerTopicsStreaming();
        testEnvironmentTopicsStreaming();
        testThermalLoopTopicsStreaming();
        testAirlockTopicsStreaming();
    }

    private void testActuators() throws Exception {
        System.out.println("5. Actuators:");
        String response = sendGet(simulatorUrl + "/api/actuators");
        
        // Map response to ActuatorList model
        ActuatorList actuatorList = objectMapper.readValue(response, ActuatorList.class);
        System.out.println("   Mapped Response: " + actuatorList);
    }

    private void testActuatorControl() throws Exception {
        System.out.println("6. Actuator Control (cooling_fan):");

        // Turn ON
        ActuatorRequest onRequest = new ActuatorRequest("ON");
        String onJson = objectMapper.writeValueAsString(onRequest);
        String onResponse = sendPost(simulatorUrl + "/api/actuators/cooling_fan", onJson);
        
        // Map response to ActuatorResponse model
        ActuatorResponse onResponseMapped = objectMapper.readValue(onResponse, ActuatorResponse.class);
        System.out.println("   ON Mapped Response: " + onResponseMapped);

        Thread.sleep(500); // Wait 0.5s

        // Turn OFF
        ActuatorRequest offRequest = new ActuatorRequest("OFF");
        String offJson = objectMapper.writeValueAsString(offRequest);
        String offResponse = sendPost(simulatorUrl + "/api/actuators/cooling_fan", offJson);
        
        // Map response to ActuatorResponse model
        ActuatorResponse offResponseMapped = objectMapper.readValue(offResponse, ActuatorResponse.class);
        System.out.println("   OFF Mapped Response: " + offResponseMapped);
    }

    private String sendGet(String url) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofSeconds(5))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        return response.body();
    }

    private String sendPost(String url, String body) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofSeconds(5))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        return response.body();
    }

    private void testPowerTopicsStreaming() throws Exception {
        System.out.println("   4.1 Power Topics Streaming:");
        String[] powerTopics = {"mars/telemetry/solar_array", "mars/telemetry/power_bus", "mars/telemetry/power_consumption"};
        
        for (String topic : powerTopics) {
            System.out.println("     Testing topic: " + topic);
            try {
                TopicPowerV1 powerData = streamTopicData(topic, TopicPowerV1.class);
                System.out.println("       Mapped Power Data: " + powerData);
            } catch (Exception e) {
                System.out.println("       Error streaming " + topic + ": " + e.getMessage());
            }
        }
    }

    private void testEnvironmentTopicsStreaming() throws Exception {
        System.out.println("   4.2 Environment Topics Streaming:");
        String[] envTopics = {"mars/telemetry/radiation", "mars/telemetry/life_support"};
        
        for (String topic : envTopics) {
            System.out.println("     Testing topic: " + topic);
            try {
                TopicEnvironmentV1 envData = streamTopicData(topic, TopicEnvironmentV1.class);
                System.out.println("       Mapped Environment Data: " + envData);
            } catch (Exception e) {
                System.out.println("       Error streaming " + topic + ": " + e.getMessage());
            }
        }
    }

    private void testThermalLoopTopicsStreaming() throws Exception {
        System.out.println("   4.3 Thermal Loop Topics Streaming:");
        String thermalTopic = "mars/telemetry/thermal_loop";
        
        System.out.println("     Testing topic: " + thermalTopic);
        try {
            TopicThermalLoopV1 thermalData = streamTopicData(thermalTopic, TopicThermalLoopV1.class);
            System.out.println("       Mapped Thermal Data: " + thermalData);
        } catch (Exception e) {
            System.out.println("       Error streaming " + thermalTopic + ": " + e.getMessage());
        }
    }

    private void testAirlockTopicsStreaming() throws Exception {
        System.out.println("   4.4 Airlock Topics Streaming:");
        String airlockTopic = "mars/telemetry/airlock";
        
        System.out.println("     Testing topic: " + airlockTopic);
        try {
            TopicAirlockV1 airlockData = streamTopicData(airlockTopic, TopicAirlockV1.class);
            System.out.println("       Mapped Airlock Data: " + airlockData);
        } catch (Exception e) {
            System.out.println("       Error streaming " + airlockTopic + ": " + e.getMessage());
        }
    }

    private <T> T streamTopicData(String topic, Class<T> targetClass) throws Exception {
        String streamUrl = simulatorUrl + "/api/telemetry/stream/" + topic;
        
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(streamUrl))
                .timeout(Duration.ofSeconds(3))
                .GET()
                .build();

        // For SSE, we'll read the first event and parse it
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        String responseBody = response.body();
        
        // Parse SSE data: look for "data:" line and extract JSON
        String[] lines = responseBody.split("\n");
        for (String line : lines) {
            if (line.startsWith("data: ")) {
                String jsonData = line.substring(6); // Remove "data: " prefix
                return objectMapper.readValue(jsonData, targetClass);
            }
        }
        
        throw new Exception("No data event found in SSE stream");
    }
}
