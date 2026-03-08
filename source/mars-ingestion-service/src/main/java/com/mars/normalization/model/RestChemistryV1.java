package com.mars.normalization.model;

import java.time.OffsetDateTime;
import java.util.List;

public class RestChemistryV1 {
    private String sensor_id;
    private OffsetDateTime captured_at;
    private List<Measurement> measurements;
    private String status;

    public static class Measurement {
        private String metric;
        private double value;
        private String unit;

        public Measurement() {}

        public Measurement(String metric, double value, String unit) {
            this.metric = metric;
            this.value = value;
            this.unit = unit;
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

        @Override
        public String toString() {
            return "Measurement{" +
                    "metric='" + metric + '\'' +
                    ", value=" + value +
                    ", unit='" + unit + '\'' +
                    '}';
        }
    }

    public RestChemistryV1() {}

    public RestChemistryV1(String sensor_id, OffsetDateTime captured_at, List<Measurement> measurements, String status) {
        this.sensor_id = sensor_id;
        this.captured_at = captured_at;
        this.measurements = measurements;
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

    public List<Measurement> getMeasurements() {
        return measurements;
    }

    public void setMeasurements(List<Measurement> measurements) {
        this.measurements = measurements;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "RestChemistryV1{" +
                "sensor_id='" + sensor_id + '\'' +
                ", captured_at=" + captured_at +
                ", measurements=" + measurements +
                ", status='" + status + '\'' +
                '}';
    }
}
