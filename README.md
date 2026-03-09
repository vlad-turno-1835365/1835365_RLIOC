# Mars IoT Tester

# One-Command Setup
```bash
# Clone and run everything
git clone <your-repo-url>
cd mars-tester
docker-compose up --build
```

# To run the tests
```bash
# Option 1: If you have Java 17+ installed
mvn compile
java -cp target/classes com.mars.tester.MarsApiTester

# Option 2: Using Docker (builds test container)
docker-compose -f docker-compose.test.yml up --build
```

# Available APIs:

# Mars Simulator (Port 8080)
- `GET /health` - Health check
- `GET /api/discovery` - Complete API schema discovery
- `GET /api/sensors` - List all sensors
- `GET /api/actuators` - List all actuators with states
- `GET /api/telemetry/topics` - List telemetry topics
- `POST /api/actuators/{actuator_name}` - Control actuators

# Mars Tester (Port 8081)
- `GET /api/health` - Tester health check
- `GET /api/sensors` - Proxy to simulator sensors
- `GET /api/actuators` - Proxy to simulator actuators
- `GET /api/telemetry/topics` - Proxy to simulator topics
- `GET /api/discovery` - Proxy to simulator discovery
- `POST /api/actuators/{actuator}` - Proxy to simulator actuator control

# To run tests inside the container
```bash
# Execute tests in the running container
docker-compose exec mars-tester java -cp app.jar com.mars.tester.MarsApiTester
```

# To run tests locally (Java 17+ required)
```bash
# Compile and run tests
mvn compile
java -cp target/classes com.mars.tester.MarsApiTester
```

# Available Devices

# 8 Sensors
- greenhouse_temperature
- entrance_humidity
- co2_hall
- hydroponic_ph
- water_tank_level
- corridor_pressure
- air_quality_pm25
- air_quality_voc

# 4 Actuators
- cooling_fan
- entrance_humidifier
- hall_ventilation
- habitat_heater

# 7 Telemetry Topics
- mars/telemetry/solar_array
- mars/telemetry/radiation
- mars/telemetry/life_support
- mars/telemetry/thermal_loop
- mars/telemetry/power_bus
- mars/telemetry/power_consumption
- mars/telemetry/airlock

# Troubleshooting

# Service Not Starting
```bash
# Check logs
docker-compose logs mars-simulator
docker-compose logs mars-tester

# Restart services
docker-compose restart
```

# Build Issues
```bash
# Clean rebuild
docker-compose down
docker-compose build --no-cache
docker-compose up
```

# Local Development Setup
```bash
# Install dependencies
mvn install

# Run Spring Boot application
mvn spring-boot:run

# Run tests
mvn test
```

# Building Docker Image
```bash
# Build only the tester image
docker build -t mars-iot-tester .

# Run with existing simulator
docker run -p 8081:8081 --env MARS_SIMULATOR_URL=http://localhost:8080 mars-iot-tester
```

