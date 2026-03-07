package com.mars.tester;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;

@Service
public class MarsTelemetryService {
    
    private static final Logger logger = LoggerFactory.getLogger(MarsTelemetryService.class);
    
    private final HttpClient client = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    @Value("${mars.simulator.url:http://localhost:8080}")
    private String simulatorUrl;

    private final AtomicInteger messageCount = new AtomicInteger(0);

    @PostConstruct
    public void init() {
        logger.info("🛰️  Mars Telemetry Service initialized - Starting to monitor telemetry streams...");
    }

    @Scheduled(fixedRate = 10000) // Check connection every 10 seconds
    public void monitorTelemetryStreams() {
        logger.info("📡 Telemetry Status - Total messages received: {}", messageCount.get());
    }

    @Async
    @Scheduled(fixedRate = 30000) // Reconnect every 30 seconds if needed
    public void connectToSolarArrayStream() {
        try {
            logger.info("🌞 Connecting to Solar Array telemetry stream...");
            connectToStream("/api/telemetry/stream/mars/telemetry/solar_array", "Solar Array");
        } catch (Exception e) {
            logger.error("❌ Error connecting to Solar Array stream: {}", e.getMessage());
        }
    }

    @Async
    @Scheduled(fixedRate = 35000) // Stagger the connections
    public void connectToRadiationStream() {
        try {
            logger.info("☢️  Connecting to Radiation telemetry stream...");
            connectToStream("/api/telemetry/stream/mars/telemetry/radiation", "Radiation");
        } catch (Exception e) {
            logger.error("❌ Error connecting to Radiation stream: {}", e.getMessage());
        }
    }

    @Async
    @Scheduled(fixedRate = 40000) // Stagger the connections
    public void connectToLifeSupportStream() {
        try {
            logger.info("🫁 Connecting to Life Support telemetry stream...");
            connectToStream("/api/telemetry/stream/mars/telemetry/life_support", "Life Support");
        } catch (Exception e) {
            logger.error("❌ Error connecting to Life Support stream: {}", e.getMessage());
        }
    }

    @Async
    @Scheduled(fixedRate = 45000) // Stagger the connections
    public void connectToThermalLoopStream() {
        try {
            logger.info("🌡️  Connecting to Thermal Loop telemetry stream...");
            connectToStream("/api/telemetry/stream/mars/telemetry/thermal_loop", "Thermal Loop");
        } catch (Exception e) {
            logger.error("❌ Error connecting to Thermal Loop stream: {}", e.getMessage());
        }
    }

    @Async
    @Scheduled(fixedRate = 50000) // Stagger the connections
    public void connectToPowerBusStream() {
        try {
            logger.info("⚡ Connecting to Power Bus telemetry stream...");
            connectToStream("/api/telemetry/stream/mars/telemetry/power_bus", "Power Bus");
        } catch (Exception e) {
            logger.error("❌ Error connecting to Power Bus stream: {}", e.getMessage());
        }
    }

    @Async
    @Scheduled(fixedRate = 55000) // Stagger the connections
    public void connectToPowerConsumptionStream() {
        try {
            logger.info("🔋 Connecting to Power Consumption telemetry stream...");
            connectToStream("/api/telemetry/stream/mars/telemetry/power_consumption", "Power Consumption");
        } catch (Exception e) {
            logger.error("❌ Error connecting to Power Consumption stream: {}", e.getMessage());
        }
    }

    @Async
    @Scheduled(fixedRate = 60000) // Stagger the connections
    public void connectToAirlockStream() {
        try {
            logger.info("🚪 Connecting to Airlock telemetry stream...");
            connectToStream("/api/telemetry/stream/mars/telemetry/airlock", "Airlock");
        } catch (Exception e) {
            logger.error("❌ Error connecting to Airlock stream: {}", e.getMessage());
        }
    }

    private void connectToStream(String streamPath, String streamName) {
        try {
            String url = simulatorUrl + streamPath;
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .timeout(Duration.ofSeconds(30))
                    .header("Accept", "text/event-stream")
                    .header("Cache-Control", "no-cache")
                    .GET()
                    .build();

            CompletableFuture<HttpResponse<String>> response = client.sendAsync(request, 
                    HttpResponse.BodyHandlers.ofString());

            response.thenAccept(r -> {
                if (r.statusCode() == 200) {
                    logger.info("✅ Connected to {} stream successfully", streamName);
                    processStreamData(r.body(), streamName);
                } else {
                    logger.warn("⚠️  Failed to connect to {} stream - Status: {}", streamName, r.statusCode());
                }
            }).exceptionally(e -> {
                logger.error("❌ Exception in {} stream: {}", streamName, e.getMessage());
                return null;
            });

        } catch (Exception e) {
            logger.error("❌ Failed to initiate {} stream connection: {}", streamName, e.getMessage());
        }
    }

    private void processStreamData(String data, String streamName) {
        String[] lines = data.split("\n");
        for (String line : lines) {
            if (line.startsWith("data: ") && line.length() > 6) {
                String jsonData = line.substring(6);
                logger.info("📊 [{}] Message #{}: {}", streamName, messageCount.incrementAndGet(), jsonData);
            } else if (line.startsWith("event: ")) {
                String eventType = line.substring(7);
                logger.info("📡 [{}] Event: {}", streamName, eventType);
            } else if (line.contains("heartbeat") || line.contains("ping")) {
                logger.debug("💓 [{}] Heartbeat received", streamName);
            }
        }
    }

    @Scheduled(fixedRate = 60000) // Every minute, log summary
    public void logTelemetrySummary() {
        int count = messageCount.get();
        if (count > 0) {
            logger.info("📈 Telemetry Summary - Last minute: {} messages received", count);
            // Reset counter for next minute
            messageCount.set(0);
        }
    }
}
