package com.mars.normalization.model;

public class ActuatorRequest {
    private String state;

    public ActuatorRequest() {}

    public ActuatorRequest(String state) {
        this.state = state;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    @Override
    public String toString() {
        return "ActuatorRequest{" +
                "state='" + state + '\'' +
                '}';
    }
}
