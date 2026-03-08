package com.mars.normalization.model;

import java.time.OffsetDateTime;
import java.util.List;

public class TopicEnvironmentV1 {
    private String topic;
    private OffsetDateTime event_time;
    private Source source;
    private List<Measurement> measurements;
    private String status;

    public static class Source {
        private String system;
        private String segment;

        public Source() {}

        public Source(String system, String segment) {
            this.system = system;
            this.segment = segment;
        }

        public String getSystem() {
            return system;
        }

        public void setSystem(String system) {
            this.system = system;
        }

        public String getSegment() {
            return segment;
        }

        public void setSegment(String segment) {
            this.segment = segment;
        }

        @Override
        public String toString() {
            return "Source{" +
                    "system='" + system + '\'' +
                    ", segment='" + segment + '\'' +
                    '}';
        }
    }

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

    public TopicEnvironmentV1() {}

    public TopicEnvironmentV1(String topic, OffsetDateTime event_time, Source source, List<Measurement> measurements, String status) {
        this.topic = topic;
        this.event_time = event_time;
        this.source = source;
        this.measurements = measurements;
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

    public Source getSource() {
        return source;
    }

    public void setSource(Source source) {
        this.source = source;
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
        return "TopicEnvironmentV1{" +
                "topic='" + topic + '\'' +
                ", event_time=" + event_time +
                ", source=" + source +
                ", measurements=" + measurements +
                ", status='" + status + '\'' +
                '}';
    }
}
