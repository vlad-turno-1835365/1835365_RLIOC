package com.mars.tester.model;

public enum EventStatus {
    OK("ok"),
    WARNING("warning"),
    UNKNOWN("unknown"),
    IDLE("IDLE"),
    PRESSURIZING("PRESSURIZING"),
    DEPRESSURIZING("DEPRESSURIZING");

    private final String value;

    EventStatus(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static EventStatus fromValue(String value) {
        for (EventStatus status : EventStatus.values()) {
            if (status.value.equalsIgnoreCase(value)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown status value: " + value);
    }

    @Override
    public String toString() {
        return value;
    }
}
