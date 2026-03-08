package com.mars.kafka.service;

import com.mars.normalization.model.*;
import com.mars.normalization.EventNormalizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Integration service that normalizes Mars sensor data and publishes it to Kafka.
 * This service acts as a bridge between the raw sensor models and the Kafka event stream.
 */
@Service
public class MarsEventPublisher {

    private static final Logger logger = LoggerFactory.getLogger(MarsEventPublisher.class);

    @Autowired
    private MarsEventKafkaProducer kafkaProducer;

    /**
     * Process and publish a single scalar sensor event
     */
    public CompletableFuture<Void> publishScalarEvent(RestScalarV1 scalarData) {
        if (scalarData == null) {
            logger.warn("Received null scalar data, skipping");
            return CompletableFuture.completedFuture(null);
        }

        NormalizedEvent event = EventNormalizer.normalize(scalarData);
        return kafkaProducer.publishEvent(event);
    }

    /**
     * Process and publish chemistry sensor events
     */
    public CompletableFuture<Void> publishChemistryEvents(RestChemistryV1 chemistryData) {
        if (chemistryData == null) {
            logger.warn("Received null chemistry data, skipping");
            return CompletableFuture.completedFuture(null);
        }

        List<NormalizedEvent> events = EventNormalizer.normalize(chemistryData);
        return kafkaProducer.publishEvents(events);
    }

    /**
     * Process and publish level sensor events
     */
    public CompletableFuture<Void> publishLevelEvents(RestLevelV1 levelData) {
        if (levelData == null) {
            logger.warn("Received null level data, skipping");
            return CompletableFuture.completedFuture(null);
        }

        List<NormalizedEvent> events = EventNormalizer.normalize(levelData);
        return kafkaProducer.publishEvents(events);
    }

    /**
     * Process and publish particulate sensor events
     */
    public CompletableFuture<Void> publishParticulateEvents(RestParticulateV1 particulateData) {
        if (particulateData == null) {
            logger.warn("Received null particulate data, skipping");
            return CompletableFuture.completedFuture(null);
        }

        List<NormalizedEvent> events = EventNormalizer.normalize(particulateData);
        return kafkaProducer.publishEvents(events);
    }

    /**
     * Process and publish power telemetry event
     */
    public CompletableFuture<Void> publishPowerEvent(TopicPowerV1 powerData) {
        if (powerData == null) {
            logger.warn("Received null power data, skipping");
            return CompletableFuture.completedFuture(null);
        }

        NormalizedEvent event = EventNormalizer.normalize(powerData);
        return kafkaProducer.publishEvent(event);
    }

    /**
     * Process and publish environment telemetry events
     */
    public CompletableFuture<Void> publishEnvironmentEvents(TopicEnvironmentV1 environmentData) {
        if (environmentData == null) {
            logger.warn("Received null environment data, skipping");
            return CompletableFuture.completedFuture(null);
        }

        List<NormalizedEvent> events = EventNormalizer.normalize(environmentData);
        return kafkaProducer.publishEvents(events);
    }

    /**
     * Process and publish thermal loop telemetry events
     */
    public CompletableFuture<Void> publishThermalLoopEvents(TopicThermalLoopV1 thermalData) {
        if (thermalData == null) {
            logger.warn("Received null thermal loop data, skipping");
            return CompletableFuture.completedFuture(null);
        }

        List<NormalizedEvent> events = EventNormalizer.normalize(thermalData);
        return kafkaProducer.publishEvents(events);
    }

    /**
     * Process and publish airlock telemetry event
     */
    public CompletableFuture<Void> publishAirlockEvent(TopicAirlockV1 airlockData) {
        if (airlockData == null) {
            logger.warn("Received null airlock data, skipping");
            return CompletableFuture.completedFuture(null);
        }

        NormalizedEvent event = EventNormalizer.normalize(airlockData);
        return kafkaProducer.publishEvent(event);
    }

    /**
     * Process and publish actuator response event
     */
    public CompletableFuture<Void> publishActuatorEvent(ActuatorResponse actuatorData) {
        if (actuatorData == null) {
            logger.warn("Received null actuator data, skipping");
            return CompletableFuture.completedFuture(null);
        }

        NormalizedEvent event = EventNormalizer.normalize(actuatorData);
        return kafkaProducer.publishEvent(event);
    }

    /**
     * Publish a pre-normalized event directly to Kafka
     */
    public CompletableFuture<Void> publishNormalizedEvent(NormalizedEvent event) {
        if (event == null) {
            logger.warn("Received null normalized event, skipping");
            return CompletableFuture.completedFuture(null);
        }

        return kafkaProducer.publishEvent(event);
    }

    /**
     * Publish multiple pre-normalized events to Kafka
     */
    public CompletableFuture<Void> publishNormalizedEvents(List<NormalizedEvent> events) {
        if (events == null || events.isEmpty()) {
            logger.warn("Received null or empty normalized events list, skipping");
            return CompletableFuture.completedFuture(null);
        }

        return kafkaProducer.publishEvents(events);
    }

    /**
     * Get the status of the Kafka producer
     */
    public boolean isPublisherEnabled() {
        return kafkaProducer.isProducerEnabled();
    }

    /**
     * Get the default Kafka topic
     */
    public String getDefaultTopic() {
        return kafkaProducer.getDefaultTopic();
    }

    /**
     * Test the complete pipeline by creating and publishing a test event
     */
    public CompletableFuture<Void> publishTestEvent() {
        logger.info("🧪 Publishing test event to verify Kafka pipeline");
        
        NormalizedEvent testEvent = kafkaProducer.createTestEvent(
            "test_sensor", 
            42.0, 
            "test_unit"
        );
        
        return kafkaProducer.publishEvent(testEvent)
            .whenComplete((result, throwable) -> {
                if (throwable != null) {
                    logger.error("❌ Test event publication failed: {}", throwable.getMessage());
                } else {
                    logger.info("✅ Test event published successfully");
                }
            });
    }
}
