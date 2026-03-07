package com.mars.tester.model;

import java.util.List;
import java.util.Map;

public class DiscoveryResponse {
    private String schema_version;
    private String schema_policy;
    private List<RestSensor> rest_sensors;
    private List<TelemetryTopic> telemetry_topics;
    private List<ActuatorInfo> actuators;

    public static class RestSensor {
        private String sensor_id;
        private String path;
        private String schema_id;

        public String getSensor_id() { return sensor_id; }
        public void setSensor_id(String sensor_id) { this.sensor_id = sensor_id; }
        
        public String getPath() { return path; }
        public void setPath(String path) { this.path = path; }
        
        public String getSchema_id() { return schema_id; }
        public void setSchema_id(String schema_id) { this.schema_id = schema_id; }

        @Override
        public String toString() {
            return "RestSensor{" +
                    "sensor_id='" + sensor_id + '\'' +
                    ", path='" + path + '\'' +
                    ", schema_id='" + schema_id + '\'' +
                    '}';
        }
    }

    public static class TelemetryTopic {
        private String topic;
        private String schema_id;
        private String transport;

        public String getTopic() { return topic; }
        public void setTopic(String topic) { this.topic = topic; }
        
        public String getSchema_id() { return schema_id; }
        public void setSchema_id(String schema_id) { this.schema_id = schema_id; }
        
        public String getTransport() { return transport; }
        public void setTransport(String transport) { this.transport = transport; }

        @Override
        public String toString() {
            return "TelemetryTopic{" +
                    "topic='" + topic + '\'' +
                    ", schema_id='" + schema_id + '\'' +
                    ", transport='" + transport + '\'' +
                    '}';
        }
    }

    public static class ActuatorInfo {
        private String actuator_id;
        private String path;
        private String schema_id;

        public String getActuator_id() { return actuator_id; }
        public void setActuator_id(String actuator_id) { this.actuator_id = actuator_id; }
        
        public String getPath() { return path; }
        public void setPath(String path) { this.path = path; }
        
        public String getSchema_id() { return schema_id; }
        public void setSchema_id(String schema_id) { this.schema_id = schema_id; }

        @Override
        public String toString() {
            return "ActuatorInfo{" +
                    "actuator_id='" + actuator_id + '\'' +
                    ", path='" + path + '\'' +
                    ", schema_id='" + schema_id + '\'' +
                    '}';
        }
    }

    public DiscoveryResponse() {}

    public String getSchema_version() {
        return schema_version;
    }

    public void setSchema_version(String schema_version) {
        this.schema_version = schema_version;
    }

    public String getSchema_policy() {
        return schema_policy;
    }

    public void setSchema_policy(String schema_policy) {
        this.schema_policy = schema_policy;
    }

    public List<RestSensor> getRest_sensors() {
        return rest_sensors;
    }

    public void setRest_sensors(List<RestSensor> rest_sensors) {
        this.rest_sensors = rest_sensors;
    }

    public List<TelemetryTopic> getTelemetry_topics() {
        return telemetry_topics;
    }

    public void setTelemetry_topics(List<TelemetryTopic> telemetry_topics) {
        this.telemetry_topics = telemetry_topics;
    }

    public List<ActuatorInfo> getActuators() {
        return actuators;
    }

    public void setActuators(List<ActuatorInfo> actuators) {
        this.actuators = actuators;
    }

    @Override
    public String toString() {
        return "DiscoveryResponse{" +
                "schema_version='" + schema_version + '\'' +
                ", schema_policy='" + schema_policy + '\'' +
                ", rest_sensors=" + rest_sensors +
                ", telemetry_topics=" + telemetry_topics +
                ", actuators=" + actuators +
                '}';
    }
}
