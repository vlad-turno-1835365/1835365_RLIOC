package com.mars.tester.model;

import java.time.OffsetDateTime;

public class RestParticulateV1 {
    private String sensor_id;
    private OffsetDateTime captured_at;
    private double pm1_ug_m3;
    private double pm25_ug_m3;
    private double pm10_ug_m3;
    private String status;

    public RestParticulateV1() {}

    public RestParticulateV1(String sensor_id, OffsetDateTime captured_at, double pm1_ug_m3, double pm25_ug_m3, double pm10_ug_m3, String status) {
        this.sensor_id = sensor_id;
        this.captured_at = captured_at;
        this.pm1_ug_m3 = pm1_ug_m3;
        this.pm25_ug_m3 = pm25_ug_m3;
        this.pm10_ug_m3 = pm10_ug_m3;
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

    public double getPm1_ug_m3() {
        return pm1_ug_m3;
    }

    public void setPm1_ug_m3(double pm1_ug_m3) {
        this.pm1_ug_m3 = pm1_ug_m3;
    }

    public double getPm25_ug_m3() {
        return pm25_ug_m3;
    }

    public void setPm25_ug_m3(double pm25_ug_m3) {
        this.pm25_ug_m3 = pm25_ug_m3;
    }

    public double getPm10_ug_m3() {
        return pm10_ug_m3;
    }

    public void setPm10_ug_m3(double pm10_ug_m3) {
        this.pm10_ug_m3 = pm10_ug_m3;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "RestParticulateV1{" +
                "sensor_id='" + sensor_id + '\'' +
                ", captured_at=" + captured_at +
                ", pm1_ug_m3=" + pm1_ug_m3 +
                ", pm25_ug_m3=" + pm25_ug_m3 +
                ", pm10_ug_m3=" + pm10_ug_m3 +
                ", status='" + status + '\'' +
                '}';
    }
}
