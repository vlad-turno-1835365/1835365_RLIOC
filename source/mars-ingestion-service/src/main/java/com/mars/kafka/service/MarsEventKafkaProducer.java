package com.mars.kafka.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mars.normalization.model.NormalizedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureCallback;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Kafka producer service that publishes NormalizedEvent objects to Kafka topics.
 * Provides both single and batch publishing capabilities with error handling and logging.
 */
@Service
public class MarsEventKafkaProducer {

    private static final Logger logger = LoggerFactory.getLogger(MarsEventKafkaProducer.class);

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @Value("${mars.kafka.topic.mars-events:mars-telemetry-events}")
    private String marsEventsTopic;

    @Value("${mars.kafka.producer.enabled:true}")
    private boolean producerEnabled;

    /**
     * Publish a single NormalizedEvent to Kafka
     * 
     * @param event The normalized event to publish
     * @return CompletableFuture that completes when the message is sent
     */
    public CompletableFuture<Void> publishEvent(NormalizedEvent event) {
        if (!producerEnabled) {
            logger.debug("Kafka producer is disabled. Skipping event: {}", event);
            return CompletableFuture.completedFuture(null);
        }

        if (event == null) {
            logger.warn("Attempting to publish null event, skipping");
            return CompletableFuture.completedFuture(null);
        }

        try {
            String eventJson = objectMapper.writeValueAsString(event);
            String key = event.getSensor_name();
            
            logger.info("📤 Publishing event to Kafka topic '{}': sensor={}, value={}, timestamp={}", 
                marsEventsTopic, event.getSensor_name(), event.getValue(), event.getTimestamp());

            // Use CompletableFuture.runAsync to handle the async operation
            return CompletableFuture.runAsync(() -> {
                try {
                    kafkaTemplate.send(marsEventsTopic, key, eventJson).get();
                    logger.info("✅ Successfully published event to Kafka topic '{}'", marsEventsTopic);
                } catch (Exception e) {
                    logger.error("❌ Failed to publish event to Kafka: topic={}, sensor={}, error={}", 
                        marsEventsTopic, event.getSensor_name(), e.getMessage(), e);
                    throw new RuntimeException(e);
                }
            });

        } catch (JsonProcessingException e) {
            logger.error("❌ Failed to serialize event to JSON: sensor={}, error={}", 
                event.getSensor_name(), e.getMessage(), e);
            return CompletableFuture.failedFuture(e);
        }
    }

    /**
     * Publish multiple NormalizedEvents to Kafka
     * 
     * @param events List of normalized events to publish
     * @return CompletableFuture that completes when all messages are sent
     */
    public CompletableFuture<Void> publishEvents(List<NormalizedEvent> events) {
        if (!producerEnabled) {
            logger.debug("Kafka producer is disabled. Skipping {} events", events.size());
            return CompletableFuture.completedFuture(null);
        }

        if (events == null || events.isEmpty()) {
            logger.debug("Attempting to publish empty or null event list, skipping");
            return CompletableFuture.completedFuture(null);
        }

        logger.info("📤 Publishing {} events to Kafka topic '{}'", events.size(), marsEventsTopic);

        @SuppressWarnings("unchecked")
        CompletableFuture<Void>[] futures = events.stream()
            .map(this::publishEvent)
            .toArray(CompletableFuture[]::new);

        return CompletableFuture.allOf(futures)
            .whenComplete((result, throwable) -> {
                if (throwable != null) {
                    logger.error("❌ Some events failed to publish to Kafka: {}", throwable.getMessage());
                } else {
                    logger.info("✅ Successfully published {} events to Kafka topic '{}'", events.size(), marsEventsTopic);
                }
            });
    }

    /**
     * Publish a single event with a specific topic override
     * 
     * @param event The normalized event to publish
     * @param topicOverride Custom topic name (overrides default)
     * @return CompletableFuture that completes when the message is sent
     */
    public CompletableFuture<Void> publishEventToTopic(NormalizedEvent event, String topicOverride) {
        if (!producerEnabled) {
            logger.debug("Kafka producer is disabled. Skipping event: {}", event);
            return CompletableFuture.completedFuture(null);
        }

        if (event == null || topicOverride == null || topicOverride.trim().isEmpty()) {
            logger.warn("Invalid parameters for publishing: event={}, topic={}", event, topicOverride);
            return CompletableFuture.completedFuture(null);
        }

        try {
            String eventJson = objectMapper.writeValueAsString(event);
            String key = event.getSensor_name();
            
            logger.info("📤 Publishing event to custom Kafka topic '{}': sensor={}, value={}", 
                topicOverride, event.getSensor_name(), event.getValue());

            // Use CompletableFuture.runAsync to handle the async operation
            return CompletableFuture.runAsync(() -> {
                try {
                    kafkaTemplate.send(topicOverride, key, eventJson).get();
                    logger.info("✅ Successfully published event to custom Kafka topic '{}'", topicOverride);
                } catch (Exception e) {
                    logger.error("❌ Failed to publish event to custom Kafka topic: topic={}, sensor={}, error={}", 
                        topicOverride, event.getSensor_name(), e.getMessage(), e);
                    throw new RuntimeException(e);
                }
            });

        } catch (JsonProcessingException e) {
            logger.error("❌ Failed to serialize event to JSON: sensor={}, error={}", 
                event.getSensor_name(), e.getMessage(), e);
            return CompletableFuture.failedFuture(e);
        }
    }

    /**
     * Get current Kafka producer status
     * 
     * @return true if producer is enabled and configured
     */
    public boolean isProducerEnabled() {
        return producerEnabled;
    }

    /**
     * Get the default topic name
     * 
     * @return Default Kafka topic for Mars events
     */
    public String getDefaultTopic() {
        return marsEventsTopic;
    }

    /**
     * Utility method to create a test event for validation
     * 
     * @param sensorName Name of the sensor
     * @param value Sensor value
     * @param unit Unit of measurement
     * @return Test NormalizedEvent
     */
    public NormalizedEvent createTestEvent(String sensorName, double value, String unit) {
        NormalizedEvent event = new NormalizedEvent();
        event.setSensor_name(sensorName);
        event.setValue(value);
        event.setUnit(unit);
        event.setTimestamp(OffsetDateTime.now());
        event.setStatus(com.mars.normalization.model.EventStatus.OK);
        return event;
    }
}
