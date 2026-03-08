package com.mars.tester.model;

import java.time.OffsetDateTime;

public class TopicThermalLoopV1 {
    private String topic;
    private OffsetDateTime event_time;
    private String loop;
    private double temperature_c;
    private double flow_l_min;
    private String status;

    public TopicThermalLoopV1() {}

    public TopicThermalLoopV1(String topic, OffsetDateTime event_time, String loop, double temperature_c, double flow_l_min, String status) {
        this.topic = topic;
        this.event_time = event_time;
        this.loop = loop;
        this.temperature_c = temperature_c;
        this.flow_l_min = flow_l_min;
        this.status = status;
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

    public String getLoop() {
        return loop;
    }

    public void setLoop(String loop) {
        this.loop = loop;
    }

    public double getTemperature_c() {
        return temperature_c;
    }

    public void setTemperature_c(double temperature_c) {
        this.temperature_c = temperature_c;
    }

    public double getFlow_l_min() {
        return flow_l_min;
    }

    public void setFlow_l_min(double flow_l_min) {
        this.flow_l_min = flow_l_min;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "TopicThermalLoopV1{" +
                "topic='" + topic + '\'' +
                ", event_time=" + event_time +
                ", loop='" + loop + '\'' +
                ", temperature_c=" + temperature_c +
                ", flow_l_min=" + flow_l_min +
                ", status='" + status + '\'' +
                '}';
    }
}
