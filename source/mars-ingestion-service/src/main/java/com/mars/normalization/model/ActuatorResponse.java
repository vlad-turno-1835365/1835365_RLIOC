package com.mars.normalization.model;

import java.time.OffsetDateTime;

public class ActuatorResponse {
    private String actuator;
    private String state;
    private OffsetDateTime updated_at;

    public ActuatorResponse() {}

    public ActuatorResponse(String actuator, String state, OffsetDateTime updated_at) {
        this.actuator = actuator;
        this.state = state;
        this.updated_at = updated_at;
    }

    public String getActuator() {
        return actuator;
    }

    public void setActuator(String actuator) {
        this.actuator = actuator;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public OffsetDateTime getUpdated_at() {
        return updated_at;
    }

    public void setUpdated_at(OffsetDateTime updated_at) {
        this.updated_at = updated_at;
    }

    @Override
    public String toString() {
        return "ActuatorResponse{" +
                "actuator='" + actuator + '\'' +
                ", state='" + state + '\'' +
                ", updated_at=" + updated_at +
                '}';
    }
}
