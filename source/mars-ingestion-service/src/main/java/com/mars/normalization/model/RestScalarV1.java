package com.mars.normalization.model;

import java.time.OffsetDateTime;

public class RestScalarV1 {
    private String sensor_id;
    private OffsetDateTime captured_at;
    private String metric;
    private double value;
    private String unit;
    private String status;

    public RestScalarV1() {}

    public RestScalarV1(String sensor_id, OffsetDateTime captured_at, String metric, double value, String unit, String status) {
        this.sensor_id = sensor_id;
        this.captured_at = captured_at;
        this.metric = metric;
        this.value = value;
        this.unit = unit;
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

    public String getMetric() {
        return metric;
    }

    public void setMetric(String metric) {
        this.metric = metric;
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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "RestScalarV1{" +
                "sensor_id='" + sensor_id + '\'' +
                ", captured_at=" + captured_at +
                ", metric='" + metric + '\'' +
                ", value=" + value +
                ", unit='" + unit + '\'' +
                ", status='" + status + '\'' +
                '}';
    }
}
