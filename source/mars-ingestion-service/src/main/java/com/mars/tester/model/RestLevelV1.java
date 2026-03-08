package com.mars.tester.model;

import java.time.OffsetDateTime;

public class RestLevelV1 {
    private String sensor_id;
    private OffsetDateTime captured_at;
    private double level_pct;
    private double level_liters;
    private String status;

    public RestLevelV1() {}

    public RestLevelV1(String sensor_id, OffsetDateTime captured_at, double level_pct, double level_liters, String status) {
        this.sensor_id = sensor_id;
        this.captured_at = captured_at;
        this.level_pct = level_pct;
        this.level_liters = level_liters;
        this.status = status;
    }

    public String getSensor_id() {
        return sensor_id;
    }

    public void setSensor_id(String sensor_id) {
        this.sensor_id = sensor_id;
    }

    public OffsetDateTime getCaptured_at() {
        return captured_at;
    }

    public void setCaptured_at(OffsetDateTime captured_at) {
        this.captured_at = captured_at;
    }

    public double getLevel_pct() {
        return level_pct;
    }

    public void setLevel_pct(double level_pct) {
        this.level_pct = level_pct;
    }

    public double getLevel_liters() {
        return level_liters;
    }

    public void setLevel_liters(double level_liters) {
        this.level_liters = level_liters;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "RestLevelV1{" +
                "sensor_id='" + sensor_id + '\'' +
                ", captured_at=" + captured_at +
                ", level_pct=" + level_pct +
                ", level_liters=" + level_liters +
                ", status='" + status + '\'' +
                '}';
    }
}
