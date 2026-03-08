package com.mars.tester;

import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/api")
public class MarsApiController {

    private static final Logger log = LoggerFactory.getLogger(MarsApiController.class);
    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${mars.simulator.url:http://localhost:8080}")
    private String simulatorUrl;

    @GetMapping("/health")
    public String health() {
        return "Mars Tester API is running! Simulator: " + simulatorUrl;
    }

    @GetMapping("/sensors")
    public String getSensors() {
        log.info("Fetching sensors from {}", simulatorUrl + "/api/sensors");
        return restTemplate.getForObject(simulatorUrl + "/api/sensors", String.class);
    }

    @GetMapping("/actuators")
    public String getActuators() {
        return restTemplate.getForObject(simulatorUrl + "/api/actuators", String.class);
    }

    @GetMapping("/telemetry/topics")
    public String getTopics() {
        return restTemplate.getForObject(simulatorUrl + "/api/telemetry/topics", String.class);
    }

    @GetMapping("/discovery")
    public String discovery() {
        return restTemplate.getForObject(simulatorUrl + "/api/discovery", String.class);
    }

    @PostMapping("/actuators/{actuator}")
    public String setActuator(@PathVariable String actuator, @RequestBody String state) {
        String url = simulatorUrl + "/api/actuators/" + actuator;
        return restTemplate.postForObject(url, state, String.class);
    }
}
