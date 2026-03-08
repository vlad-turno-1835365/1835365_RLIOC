package com.mars.normalization.model;

import java.time.OffsetDateTime;

public class TopicAirlockV1 {
    private String topic;
    private OffsetDateTime event_time;
    private String airlock_id;
    private double cycles_per_hour;
    private String last_state;

    public TopicAirlockV1() {}

    public TopicAirlockV1(String topic, OffsetDateTime event_time, String airlock_id, double cycles_per_hour, String last_state) {
        this.topic = topic;
        this.event_time = event_time;
        this.airlock_id = airlock_id;
        this.cycles_per_hour = cycles_per_hour;
        this.last_state = last_state;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public OffsetDateTime getEvent_time() {
        return event_time;
    }

    public void setEvent_time(OffsetDateTime event_time) {
        this.event_time = event_time;
    }

    public String getAirlock_id() {
        return airlock_id;
    }

    public void setAirlock_id(String airlock_id) {
        this.airlock_id = airlock_id;
    }

    public double getCycles_per_hour() {
        return cycles_per_hour;
    }

    public void setCycles_per_hour(double cycles_per_hour) {
        this.cycles_per_hour = cycles_per_hour;
    }

    public String getLast_state() {
        return last_state;
    }

    public void setLast_state(String last_state) {
        this.last_state = last_state;
    }

    @Override
    public String toString() {
        return "TopicAirlockV1{" +
                "topic='" + topic + '\'' +
                ", event_time=" + event_time +
                ", airlock_id='" + airlock_id + '\'' +
                ", cycles_per_hour=" + cycles_per_hour +
                ", last_state='" + last_state + '\'' +
                '}';
    }
}
