package com.mars.tester;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class MarsScheduledTester {
    
    private static final HttpClient client = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

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
        System.out.println("   Response: " + response);
    }

    private void testSensors() throws Exception {
        System.out.println("3. REST Sensors (greenhouse_temperature):");
        String response = sendGet(simulatorUrl + "/api/sensors/greenhouse_temperature");
        System.out.println("   Response: " + response);
    }

    private void testTopics() throws Exception {
        System.out.println("4. Telemetry Topics:");
        String response = sendGet(simulatorUrl + "/api/telemetry/topics");
        System.out.println("   Response: " + response);
    }

    private void testActuators() throws Exception {
        System.out.println("5. Actuators:");
        String response = sendGet(simulatorUrl + "/api/actuators");
        System.out.println("   Response: " + response);
    }

    private void testActuatorControl() throws Exception {
        System.out.println("6. Actuator Control (cooling_fan):");

        // Turn ON
        String onResponse = sendPost(simulatorUrl + "/api/actuators/cooling_fan", "{\"state\": \"ON\"}");
        System.out.println("   ON: " + onResponse);

        Thread.sleep(500); // Wait 0.5s

        // Turn OFF
        String offResponse = sendPost(simulatorUrl + "/api/actuators/cooling_fan", "{\"state\": \"OFF\"}");
        System.out.println("   OFF: " + offResponse);
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
}
