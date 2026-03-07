package com.mars.tester;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;

@Service
public class MarsTelemetryStreamService {
    
    private static final Logger logger = LoggerFactory.getLogger(MarsTelemetryStreamService.class);
    
    public MarsTelemetryStreamService() {
        logger.info("🏗️ MarsTelemetryStreamService constructor called");
    }
    
    @Value("${mars.simulator.url:http://localhost:8080}")
    private String simulatorUrl;

    private final ExecutorService executor = Executors.newCachedThreadPool();
    private volatile boolean running = true;

    @PostConstruct
    public void init() {
        logger.info("🛰️  Mars Telemetry Stream Service initialized");
        startTelemetryStreams();
    }

    @PreDestroy
    public void cleanup() {
        logger.info("🛑 Shutting down telemetry streams...");
        running = false;
        executor.shutdown();
    }

    private void startTelemetryStreams() {
        // Start all telemetry streams
        connectToStreamAsync("/api/telemetry/stream/mars/telemetry/solar_array", "Solar Array");
        connectToStreamAsync("/api/telemetry/stream/mars/telemetry/radiation", "Radiation");
        connectToStreamAsync("/api/telemetry/stream/mars/telemetry/life_support", "Life Support");
        connectToStreamAsync("/api/telemetry/stream/mars/telemetry/thermal_loop", "Thermal Loop");
        connectToStreamAsync("/api/telemetry/stream/mars/telemetry/power_bus", "Power Bus");
        connectToStreamAsync("/api/telemetry/stream/mars/telemetry/power_consumption", "Power Consumption");
        connectToStreamAsync("/api/telemetry/stream/mars/telemetry/airlock", "Airlock");
    }

    private void connectToStreamAsync(String streamPath, String streamName) {
        CompletableFuture.runAsync(() -> {
            while (running) {
                try {
                    connectToStream(streamPath, streamName);
                    Thread.sleep(5000); // Wait 5 seconds before reconnecting
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                } catch (Exception e) {
                    logger.error("❌ Error in {} stream, reconnecting in 5s: {}", streamName, e.getMessage());
                    try {
                        Thread.sleep(5000);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            }
        }, executor);
    }

    private void connectToStream(String streamPath, String streamName) {
        HttpURLConnection connection = null;
        BufferedReader reader = null;
        
        try {
            URL url = new URI(simulatorUrl + streamPath).toURL();
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Accept", "text/event-stream");
            connection.setRequestProperty("Cache-Control", "no-cache");
            connection.setConnectTimeout(10000);
            connection.setReadTimeout(30000);
            connection.connect();

            if (connection.getResponseCode() == 200) {
                logger.info("✅ Connected to {} telemetry stream", streamName);
                
                reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String line;
                int messageCount = 0;
                
                while (running && (line = reader.readLine()) != null) {
                    if (line.startsWith("data: ") && line.length() > 6) {
                        String jsonData = line.substring(6);
                        logger.info("📊 [{}] #{}: {}", streamName, ++messageCount, jsonData);
                    } else if (line.startsWith("event: ")) {
                        String eventType = line.substring(7);
                        logger.info("📡 [{}] Event: {}", streamName, eventType);
                    } else if (line.contains("heartbeat") || line.contains("ping")) {
                        logger.debug("💓 [{}] Heartbeat", streamName);
                    }
                }
            } else {
                logger.warn("⚠️  Failed to connect to {} - HTTP {}", streamName, connection.getResponseCode());
            }
            
        } catch (Exception e) {
            logger.error("❌ {} stream error: {}", streamName, e.getMessage());
        } finally {
            if (reader != null) {
                try { reader.close(); } catch (Exception e) { /* ignore */ }
            }
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    @Scheduled(fixedRate = 30000) // Every 30 seconds
    public void logStreamStatus() {
        logger.info("📡 Telemetry streams status: Active connections monitoring 7 Mars habitat systems");
    }
}
