package com.mars.tester.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Normalizer class that converts various Mars sensor models into a standardized NormalizedEvent format.
 * This provides a unified interface for processing different types of sensor data.
 */
public class EventNormalizer {

    /**
     * Normalize RestScalarV1 model to NormalizedEvent
     */
    public static NormalizedEvent normalize(RestScalarV1 scalar) {
        if (scalar == null) return null;
        
        NormalizedEvent event = new NormalizedEvent();
        event.setTimestamp(scalar.getCaptured_at());
        event.setSensor_name(scalar.getSensor_id());
        event.setValue(scalar.getValue());
        event.setUnit(scalar.getUnit());
        
        // Map status string to EventStatus enum
        try {
            event.setStatus(EventStatus.fromValue(scalar.getStatus()));
        } catch (IllegalArgumentException e) {
            event.setStatus(EventStatus.UNKNOWN);
        }
        
        return event;
    }

    /**
     * Normalize RestChemistryV1 model to NormalizedEvent(s)
     * Returns multiple events for each measurement in the chemistry data
     */
    public static List<NormalizedEvent> normalize(RestChemistryV1 chemistry) {
        if (chemistry == null) return new ArrayList<>();
        
        List<NormalizedEvent> events = new ArrayList<>();
        
        for (RestChemistryV1.Measurement measurement : chemistry.getMeasurements()) {
            NormalizedEvent event = new NormalizedEvent();
            event.setTimestamp(chemistry.getCaptured_at());
            event.setSensor_name(chemistry.getSensor_id() + "_" + measurement.getMetric());
            event.setValue(measurement.getValue());
            event.setUnit(measurement.getUnit());
            
            // Map status string to EventStatus enum
            try {
                event.setStatus(EventStatus.fromValue(chemistry.getStatus()));
            } catch (IllegalArgumentException e) {
                event.setStatus(EventStatus.UNKNOWN);
            }
            
            events.add(event);
        }
        
        return events;
    }

    /**
     * Normalize RestLevelV1 model to NormalizedEvent
     * Creates two events: one for percentage and one for liters
     */
    public static List<NormalizedEvent> normalize(RestLevelV1 level) {
        if (level == null) return new ArrayList<>();
        
        List<NormalizedEvent> events = new ArrayList<>();
        
        // Event for percentage level
        NormalizedEvent pctEvent = new NormalizedEvent();
        pctEvent.setTimestamp(level.getCaptured_at());
        pctEvent.setSensor_name(level.getSensor_id() + "_percentage");
        pctEvent.setValue(level.getLevel_pct());
        pctEvent.setUnit("%");
        
        try {
            pctEvent.setStatus(EventStatus.fromValue(level.getStatus()));
        } catch (IllegalArgumentException e) {
            pctEvent.setStatus(EventStatus.UNKNOWN);
        }
        
        events.add(pctEvent);
        
        // Event for liters level
        NormalizedEvent litersEvent = new NormalizedEvent();
        litersEvent.setTimestamp(level.getCaptured_at());
        litersEvent.setSensor_name(level.getSensor_id() + "_liters");
        litersEvent.setValue(level.getLevel_liters());
        litersEvent.setUnit("L");
        
        try {
            litersEvent.setStatus(EventStatus.fromValue(level.getStatus()));
        } catch (IllegalArgumentException e) {
            litersEvent.setStatus(EventStatus.UNKNOWN);
        }
        
        events.add(litersEvent);
        
        return events;
    }

    /**
     * Normalize RestParticulateV1 model to NormalizedEvent(s)
     * Creates three events: one for each particulate size (PM1, PM2.5, PM10)
     */
    public static List<NormalizedEvent> normalize(RestParticulateV1 particulate) {
        if (particulate == null) return new ArrayList<>();
        
        List<NormalizedEvent> events = new ArrayList<>();
        
        // PM1 event
        NormalizedEvent pm1Event = new NormalizedEvent();
        pm1Event.setTimestamp(particulate.getCaptured_at());
        pm1Event.setSensor_name(particulate.getSensor_id() + "_pm1");
        pm1Event.setValue(particulate.getPm1_ug_m3());
        pm1Event.setUnit("μg/m³");
        
        try {
            pm1Event.setStatus(EventStatus.fromValue(particulate.getStatus()));
        } catch (IllegalArgumentException e) {
            pm1Event.setStatus(EventStatus.UNKNOWN);
        }
        
        events.add(pm1Event);
        
        // PM2.5 event
        NormalizedEvent pm25Event = new NormalizedEvent();
        pm25Event.setTimestamp(particulate.getCaptured_at());
        pm25Event.setSensor_name(particulate.getSensor_id() + "_pm25");
        pm25Event.setValue(particulate.getPm25_ug_m3());
        pm25Event.setUnit("μg/m³");
        
        try {
            pm25Event.setStatus(EventStatus.fromValue(particulate.getStatus()));
        } catch (IllegalArgumentException e) {
            pm25Event.setStatus(EventStatus.UNKNOWN);
        }
        
        events.add(pm25Event);
        
        // PM10 event
        NormalizedEvent pm10Event = new NormalizedEvent();
        pm10Event.setTimestamp(particulate.getCaptured_at());
        pm10Event.setSensor_name(particulate.getSensor_id() + "_pm10");
        pm10Event.setValue(particulate.getPm10_ug_m3());
        pm10Event.setUnit("μg/m³");
        
        try {
            pm10Event.setStatus(EventStatus.fromValue(particulate.getStatus()));
        } catch (IllegalArgumentException e) {
            pm10Event.setStatus(EventStatus.UNKNOWN);
        }
        
        events.add(pm10Event);
        
        return events;
    }

    /**
     * Normalize TopicPowerV1 model to NormalizedEvent
     */
    public static NormalizedEvent normalize(TopicPowerV1 power) {
        if (power == null) return null;
        
        NormalizedEvent event = new NormalizedEvent();
        event.setTimestamp(power.getEvent_time());
        event.setSensor_name(power.getSubsystem());
        event.setValue(power.getPower_kw());
        event.setUnit("kW");
        
        // Set default status since TopicPowerV1 doesn't have status field
        event.setStatus(EventStatus.OK);
        
        return event;
    }

    /**
     * Normalize TopicEnvironmentV1 model to NormalizedEvent(s)
     * Creates events for each environmental measurement
     */
    public static List<NormalizedEvent> normalize(TopicEnvironmentV1 environment) {
        if (environment == null) return new ArrayList<>();
        
        List<NormalizedEvent> events = new ArrayList<>();
        
        // Process measurements list
        if (environment.getMeasurements() != null) {
            for (TopicEnvironmentV1.Measurement measurement : environment.getMeasurements()) {
                NormalizedEvent event = new NormalizedEvent();
                event.setTimestamp(environment.getEvent_time());
                event.setSensor_name(environment.getTopic() + "_" + measurement.getMetric());
                event.setValue(measurement.getValue());
                event.setUnit(measurement.getUnit());
                
                // Map status string to EventStatus enum
                try {
                    event.setStatus(EventStatus.fromValue(environment.getStatus()));
                } catch (IllegalArgumentException e) {
                    event.setStatus(EventStatus.UNKNOWN);
                }
                
                events.add(event);
            }
        }
        
        return events;
    }

    /**
     * Normalize TopicThermalLoopV1 model to NormalizedEvent(s)
     * Creates events for temperature and flow rate
     */
    public static List<NormalizedEvent> normalize(TopicThermalLoopV1 thermal) {
        if (thermal == null) return new ArrayList<>();
        
        List<NormalizedEvent> events = new ArrayList<>();
        
        // Temperature event
        NormalizedEvent tempEvent = new NormalizedEvent();
        tempEvent.setTimestamp(thermal.getEvent_time());
        tempEvent.setSensor_name(thermal.getTopic() + "_temperature");
        tempEvent.setValue(thermal.getTemperature_c());
        tempEvent.setUnit("°C");
        
        try {
            tempEvent.setStatus(EventStatus.fromValue(thermal.getStatus()));
        } catch (IllegalArgumentException e) {
            tempEvent.setStatus(EventStatus.UNKNOWN);
        }
        
        events.add(tempEvent);
        
        // Flow rate event
        NormalizedEvent flowEvent = new NormalizedEvent();
        flowEvent.setTimestamp(thermal.getEvent_time());
        flowEvent.setSensor_name(thermal.getTopic() + "_flow_rate");
        flowEvent.setValue(thermal.getFlow_l_min());
        flowEvent.setUnit("L/min");
        
        try {
            flowEvent.setStatus(EventStatus.fromValue(thermal.getStatus()));
        } catch (IllegalArgumentException e) {
            flowEvent.setStatus(EventStatus.UNKNOWN);
        }
        
        events.add(flowEvent);
        
        return events;
    }

    /**
     * Normalize TopicAirlockV1 model to NormalizedEvent
     */
    public static NormalizedEvent normalize(TopicAirlockV1 airlock) {
        if (airlock == null) return null;
        
        NormalizedEvent event = new NormalizedEvent();
        event.setTimestamp(airlock.getEvent_time());
        event.setSensor_name(airlock.getAirlock_id());
        event.setValue(airlock.getCycles_per_hour());
        event.setUnit("cycles/hour");
        
        // Map last_state to EventStatus
        if (airlock.getLast_state() != null) {
            try {
                event.setStatus(EventStatus.fromValue(airlock.getLast_state()));
            } catch (IllegalArgumentException e) {
                event.setStatus(EventStatus.UNKNOWN);
            }
        } else {
            event.setStatus(EventStatus.UNKNOWN);
        }
        
        return event;
    }

    /**
     * Normalize ActuatorResponse model to NormalizedEvent
     */
    public static NormalizedEvent normalize(ActuatorResponse actuator) {
        if (actuator == null) return null;
        
        NormalizedEvent event = new NormalizedEvent();
        event.setTimestamp(actuator.getUpdated_at());
        event.setSensor_name(actuator.getActuator());
        event.setValue(actuator.getState().equals("ON") ? 1.0 : 0.0);
        event.setUnit("state");
        
        // Map actuator state to EventStatus
        if (actuator.getState().equals("ON")) {
            event.setStatus(EventStatus.OK);
        } else if (actuator.getState().equals("OFF")) {
            event.setStatus(EventStatus.IDLE);
        } else {
            event.setStatus(EventStatus.UNKNOWN);
        }
        
        return event;
    }
}
