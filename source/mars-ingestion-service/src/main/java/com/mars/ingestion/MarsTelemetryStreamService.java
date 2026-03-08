package com.mars.ingestion;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.time.Duration;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.mars.normalization.model.*;
import com.mars.kafka.service.MarsEventPublisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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

    @Autowired
    private MarsEventPublisher eventPublisher;

    private final ExecutorService executor = Executors.newCachedThreadPool();
    private volatile boolean running = true;
    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
            .disable(com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);

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
        // Start all telemetry streams with appropriate model mapping
        connectToPowerStreamAsync("/api/telemetry/stream/mars/telemetry/solar_array", "Solar Array");
        connectToPowerStreamAsync("/api/telemetry/stream/mars/telemetry/power_bus", "Power Bus");
        connectToPowerStreamAsync("/api/telemetry/stream/mars/telemetry/power_consumption", "Power Consumption");
        connectToEnvironmentStreamAsync("/api/telemetry/stream/mars/telemetry/radiation", "Radiation");
        connectToEnvironmentStreamAsync("/api/telemetry/stream/mars/telemetry/life_support", "Life Support");
        connectToThermalLoopStreamAsync("/api/telemetry/stream/mars/telemetry/thermal_loop", "Thermal Loop");
        connectToAirlockStreamAsync("/api/telemetry/stream/mars/telemetry/airlock", "Airlock");
    }

    private void connectToPowerStreamAsync(String streamPath, String streamName) {
        CompletableFuture.runAsync(() -> {
            while (running) {
                try {
                    connectToPowerStream(streamPath, streamName);
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

    private void connectToEnvironmentStreamAsync(String streamPath, String streamName) {
        CompletableFuture.runAsync(() -> {
            while (running) {
                try {
                    connectToEnvironmentStream(streamPath, streamName);
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

    private void connectToThermalLoopStreamAsync(String streamPath, String streamName) {
        CompletableFuture.runAsync(() -> {
            while (running) {
                try {
                    connectToThermalLoopStream(streamPath, streamName);
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

    private void connectToAirlockStreamAsync(String streamPath, String streamName) {
        CompletableFuture.runAsync(() -> {
            while (running) {
                try {
                    connectToAirlockStream(streamPath, streamName);
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

    private void connectToPowerStream(String streamPath, String streamName) {
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
                        try {
                            TopicPowerV1 powerData = objectMapper.readValue(jsonData, TopicPowerV1.class);
                            logger.info("⚡ [{}] #{}: {} - {} kW", streamName, ++messageCount, 
                                powerData.getSubsystem(), powerData.getPower_kw());
                            eventPublisher.publishPowerEvent(powerData);
                        } catch (Exception e) {
                            logger.warn("⚠️  Failed to parse {} data: {}", streamName, e.getMessage());
                            logger.debug("Raw data: {}", jsonData);
                        }
                    } else if (line.startsWith("event: ")) {
                        String eventType = line.substring(7);
                        logger.debug("📡 [{}] Event: {}", streamName, eventType);
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

    private void connectToEnvironmentStream(String streamPath, String streamName) {
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
                        try {
                            TopicEnvironmentV1 envData = objectMapper.readValue(jsonData, TopicEnvironmentV1.class);
                            logger.info("🌍 [{}] #{}: {} - {}", streamName, ++messageCount, 
                                envData.getSource().getSystem(), envData.getStatus());
                            eventPublisher.publishEnvironmentEvents(envData);
                        } catch (Exception e) {
                            logger.warn("⚠️  Failed to parse {} data: {}", streamName, e.getMessage());
                            logger.debug("Raw data: {}", jsonData);
                        }
                    } else if (line.startsWith("event: ")) {
                        String eventType = line.substring(7);
                        logger.debug("📡 [{}] Event: {}", streamName, eventType);
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

    private void connectToThermalLoopStream(String streamPath, String streamName) {
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
                        try {
                            TopicThermalLoopV1 thermalData = objectMapper.readValue(jsonData, TopicThermalLoopV1.class);
                            logger.info("🌡️  [{}] #{}: {} - {}°C", streamName, ++messageCount, 
                                thermalData.getLoop(), thermalData.getTemperature_c());
                            eventPublisher.publishThermalLoopEvents(thermalData);
                        } catch (Exception e) {
                            logger.warn("⚠️  Failed to parse {} data: {}", streamName, e.getMessage());
                            logger.debug("Raw data: {}", jsonData);
                        }
                    } else if (line.startsWith("event: ")) {
                        String eventType = line.substring(7);
                        logger.debug("📡 [{}] Event: {}", streamName, eventType);
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

    private void connectToAirlockStream(String streamPath, String streamName) {
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
                        try {
                            TopicAirlockV1 airlockData = objectMapper.readValue(jsonData, TopicAirlockV1.class);
                            logger.info("🚪 [{}] #{}: {} - {}", streamName, ++messageCount, 
                                airlockData.getAirlock_id(), airlockData.getLast_state());
                            eventPublisher.publishAirlockEvent(airlockData);
                        } catch (Exception e) {
                            logger.warn("⚠️  Failed to parse {} data: {}", streamName, e.getMessage());
                            logger.debug("Raw data: {}", jsonData);
                        }
                    } else if (line.startsWith("event: ")) {
                        String eventType = line.substring(7);
                        logger.debug("📡 [{}] Event: {}", streamName, eventType);
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
