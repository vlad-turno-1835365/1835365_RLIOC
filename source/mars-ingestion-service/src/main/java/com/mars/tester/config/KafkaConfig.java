package com.mars.tester.config;

import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * Kafka configuration for Mars IoT event producer
 */
@Configuration
public class KafkaConfig {

    @Value("${spring.kafka.bootstrap-servers:localhost:9092}")
    private String bootstrapServers;

    @Value("${mars.kafka.producer.acks:all}")
    private String acks;

    @Value("${mars.kafka.producer.retries:3}")
    private int retries;

    @Value("${mars.kafka.producer.batch-size:16384}")
    private int batchSize;

    @Value("${mars.kafka.producer.linger-ms:1}")
    private int lingerMs;

    @Value("${mars.kafka.producer.buffer-memory:33554432}")
    private int bufferMemory;

    @Value("${mars.kafka.producer.key-serializer:org.apache.kafka.common.serialization.StringSerializer}")
    private String keySerializer;

    @Value("${mars.kafka.producer.value-serializer:org.apache.kafka.common.serialization.StringSerializer}")
    private String valueSerializer;

    @Bean
    public ProducerFactory<String, String> producerFactory() {
        Map<String, Object> configProps = new HashMap<>();
        
        // Server configuration
        configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        
        // Reliability configuration
        configProps.put(ProducerConfig.ACKS_CONFIG, acks);
        configProps.put(ProducerConfig.RETRIES_CONFIG, retries);
        configProps.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, true);
        
        // Performance configuration
        configProps.put(ProducerConfig.BATCH_SIZE_CONFIG, batchSize);
        configProps.put(ProducerConfig.LINGER_MS_CONFIG, lingerMs);
        configProps.put(ProducerConfig.BUFFER_MEMORY_CONFIG, bufferMemory);
        
        // Serialization configuration
        configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, keySerializer);
        configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, valueSerializer);
        
        // Compression for better performance
        configProps.put(ProducerConfig.COMPRESSION_TYPE_CONFIG, "snappy");
        
        // Timeout configurations
        configProps.put(ProducerConfig.REQUEST_TIMEOUT_MS_CONFIG, 30000);
        configProps.put(ProducerConfig.DELIVERY_TIMEOUT_MS_CONFIG, 120000);
        
        return new DefaultKafkaProducerFactory<>(configProps);
    }

    @Bean
    public KafkaTemplate<String, String> kafkaTemplate() {
        return new KafkaTemplate<>(producerFactory());
    }

    /**
     * Additional configuration for development vs production environments
     */
    @Bean
    public Map<String, Object> additionalProducerConfigs() {
        Map<String, Object> configs = new HashMap<>();
        
        // Development-specific settings
        if (isDevelopmentEnvironment()) {
            configs.put(ProducerConfig.MAX_IN_FLIGHT_REQUESTS_PER_CONNECTION, 1);
            configs.put(ProducerConfig.RETRIES_CONFIG, 5);
        }
        
        // Production-specific settings
        if (isProductionEnvironment()) {
            configs.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, true);
            configs.put(ProducerConfig.MAX_BLOCK_MS_CONFIG, 60000);
        }
        
        return configs;
    }

    private boolean isDevelopmentEnvironment() {
        String env = System.getProperty("spring.profiles.active", "dev");
        return env.contains("dev") || env.contains("test");
    }

    private boolean isProductionEnvironment() {
        String env = System.getProperty("spring.profiles.active", "dev");
        return env.contains("prod");
    }
}
