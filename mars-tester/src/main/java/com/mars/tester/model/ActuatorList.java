package com.mars.tester.model;

import java.util.Map;

public class ActuatorList {
    private Map<String, String> actuators;

    public ActuatorList() {}

    public ActuatorList(Map<String, String> actuators) {
        this.actuators = actuators;
    }

    public Map<String, String> getActuators() {
        return actuators;
    }

    public void setActuators(Map<String, String> actuators) {
        this.actuators = actuators;
    }

    @Override
    public String toString() {
        return "ActuatorList{" +
                "actuators=" + actuators +
                '}';
    }
}
