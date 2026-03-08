package com.mars.tester.model;

import java.time.OffsetDateTime;
import java.util.UUID;

public class NormalizedEvent {
    private String event_id;
    private OffsetDateTime timestamp;
    private String sensor_name;
    private double value;
    private String unit;
    private EventStatus status;

    public NormalizedEvent() {
        this.event_id = UUID.randomUUID().toString();
    }

    public NormalizedEvent(String event_id, OffsetDateTime timestamp, String sensor_name, double value) {
        this.event_id = event_id;
        this.timestamp = timestamp;
        this.sensor_name = sensor_name;
        this.value = value;
    }

    public NormalizedEvent(String event_id, OffsetDateTime timestamp, String sensor_name, double value, String unit, EventStatus status) {
        this.event_id = event_id;
        this.timestamp = timestamp;
        this.sensor_name = sensor_name;
        this.value = value;
        this.unit = unit;
        this.status = status;
    }

    public String getEvent_id() {
        return event_id;
    }

    public void setEvent_id(String event_id) {
        this.event_id = event_id;
    }

    public OffsetDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(OffsetDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public String getSensor_name() {
        return sensor_name;
    }

    public void setSensor_name(String sensor_name) {
        this.sensor_name = sensor_name;
    }

    public double getValue() {
        return value;
    }

    public void setValue(double value) {
        this.value = value;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public EventStatus getStatus() {
        return status;
    }

    public void setStatus(EventStatus status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "NormalizedEvent{" +
                "event_id='" + event_id + '\'' +
                ", timestamp=" + timestamp +
                ", sensor_name='" + sensor_name + '\'' +
                ", value=" + value +
                ", unit='" + unit + '\'' +
                ", status='" + status + '\'' +
                '}';
    }
}
