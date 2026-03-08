package com.mars.tester;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

public class MarsApiTester {
    private static final HttpClient client = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    private static final String BASE_URL = "http://localhost:8080"; // Simulator port

    public static void main(String[] args) throws Exception {
        System.out.println("🚀 Testing Mars IoT Simulator APIs...\n");

        testHealth();
        testDiscovery();
        testSensors();
        testTopics();
        testActuators();
        testActuatorControl();

        System.out.println("\n✅ All tests completed!");
    }

    private static void testHealth() throws Exception {
        System.out.println("1. Health check:");
        String response = sendGet(BASE_URL + "/health");
        System.out.println("   Response: " + response + "\n");
    }

    private static void testDiscovery() throws Exception {
        System.out.println("2. API Discovery:");
        String response = sendGet(BASE_URL + "/api/discovery");
        System.out.println("   Response: " + response + "\n");
    }

    private static void testSensors() throws Exception {
        System.out.println("3. REST Sensors:");
        String response = sendGet(BASE_URL + "/api/sensors/greenhouse_temperature");
        System.out.println("   Response: " + response + "\n");
    }

    private static void testTopics() throws Exception {
        System.out.println("4. Telemetry Topics:");
        String response = sendGet(BASE_URL + "/api/telemetry/topics");
        System.out.println("   Response: " + response + "\n");
    }

    private static void testActuators() throws Exception {
        System.out.println("5. Actuators:");
        String response = sendGet(BASE_URL + "/api/actuators");
        System.out.println("   Response: " + response + "\n");
    }

    private static void testActuatorControl() throws Exception {
        System.out.println("6. Actuator Control (cooling_fan):");

        // Turn ON
        String onResponse = sendPost(BASE_URL + "/api/actuators/cooling_fan", "{\"state\": \"ON\"}");
        System.out.println("   ON: " + onResponse);

        Thread.sleep(1000); // Wait 1s

        // Turn OFF
        String offResponse = sendPost(BASE_URL + "/api/actuators/cooling_fan", "{\"state\": \"OFF\"}");
        System.out.println("   OFF: " + offResponse + "\n");
    }

    private static String sendGet(String url) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofSeconds(5))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        return response.body();
    }

    private static String sendPost(String url, String body) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofSeconds(5))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        return response.body();
    }

    // Simple JSON parsers for demo
    private static int countDevices(String json) {
        return json.contains("sensors") || json.contains("actuators") ? 10 : 0;
    }

    private static String extractSensorCount(String json) {
        return String.valueOf(json.split("id").length - 1);
    }

    private static String extractFirstSensor(String json) {
        String[] parts = json.split("\"id\"");
        if (parts.length > 1) {
            return parts[1].split("\"")[1];
        }
        return "No sensors found";
    }

    private static int countTopics(String json) {
        return (int) (json.length() / 100.0); // Rough estimate
    }

    private static String extractActuatorList(String json) {
        return json.replaceAll("[\\[\\]{}]", "").substring(0, 100) + "...";
    }
}
