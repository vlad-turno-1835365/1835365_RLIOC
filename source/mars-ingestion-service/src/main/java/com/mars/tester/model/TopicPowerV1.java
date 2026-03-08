package com.mars.tester.model;

import java.time.OffsetDateTime;

public class TopicPowerV1 {
    private String topic;
    private OffsetDateTime event_time;
    private String subsystem;
    private double power_kw;
    private double voltage_v;
    private double current_a;
    private double cumulative_kwh;

    public TopicPowerV1() {}

    public TopicPowerV1(String topic, OffsetDateTime event_time, String subsystem, double power_kw, double voltage_v, double current_a, double cumulative_kwh) {
        this.topic = topic;
        this.event_time = event_time;
        this.subsystem = subsystem;
        this.power_kw = power_kw;
        this.voltage_v = voltage_v;
        this.current_a = current_a;
        this.cumulative_kwh = cumulative_kwh;
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

    public String getSubsystem() {
        return subsystem;
    }

    public void setSubsystem(String subsystem) {
        this.subsystem = subsystem;
    }

    public double getPower_kw() {
        return power_kw;
    }

    public void setPower_kw(double power_kw) {
        this.power_kw = power_kw;
    }

    public double getVoltage_v() {
        return voltage_v;
    }

    public void setVoltage_v(double voltage_v) {
        this.voltage_v = voltage_v;
    }

    public double getCurrent_a() {
        return current_a;
    }

    public void setCurrent_a(double current_a) {
        this.current_a = current_a;
    }

    public double getCumulative_kwh() {
        return cumulative_kwh;
    }

    public void setCumulative_kwh(double cumulative_kwh) {
        this.cumulative_kwh = cumulative_kwh;
    }

    @Override
    public String toString() {
        return "TopicPowerV1{" +
                "topic='" + topic + '\'' +
                ", event_time=" + event_time +
                ", subsystem='" + subsystem + '\'' +
                ", power_kw=" + power_kw +
                ", voltage_v=" + voltage_v +
                ", current_a=" + current_a +
                ", cumulative_kwh=" + cumulative_kwh +
                '}';
    }
}
