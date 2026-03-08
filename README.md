# Mars IoT Tester

A comprehensive test suite for the Mars IoT Simulator APIs. This project provides automated testing for all Mars habitat IoT endpoints including sensors, actuators, and telemetry streams.

## 🚀 Quick Start for Your Colleagues

### Prerequisites
- Docker installed on your machine
- Docker Compose (usually comes with Docker)

### One-Command Setup

```bash
# Clone and run everything
git clone <your-repo-url>
cd mars-tester
docker-compose up --build
```

That's it! The system will automatically:
1. Start the Mars IoT Simulator (port 8080)
2. Build and start the Mars Tester (port 8081)

### Running the Tests

After the services are running, execute the tests locally:

```bash
# Option 1: If you have Java 17+ installed
mvn compile
java -cp target/classes com.mars.tester.MarsApiTester

# Option 2: Using Docker (builds test container)
docker-compose -f docker-compose.test.yml up --build
```

### Access Points

Once running, you can access:

- **Mars Simulator**: http://localhost:8080
- **Mars Tester API**: http://localhost:8081
- **Tester Health Check**: http://localhost:8081/api/health

## 📋 Available APIs

### Mars Simulator (Port 8080)
- `GET /health` - Health check
- `GET /api/discovery` - Complete API schema discovery
- `GET /api/sensors` - List all sensors
- `GET /api/actuators` - List all actuators with states
- `GET /api/telemetry/topics` - List telemetry topics
- `POST /api/actuators/{actuator_name}` - Control actuators

### Mars Tester (Port 8081)
- `GET /api/health` - Tester health check
- `GET /api/sensors` - Proxy to simulator sensors
- `GET /api/actuators` - Proxy to simulator actuators
- `GET /api/telemetry/topics` - Proxy to simulator topics
- `GET /api/discovery` - Proxy to simulator discovery
- `POST /api/actuators/{actuator}` - Proxy to simulator actuator control

## 🧪 Running Tests

### Option 1: Run Tests Inside Container
```bash
# Execute tests in the running container
docker-compose exec mars-tester java -cp app.jar com.mars.tester.MarsApiTester
```

### Option 2: Run Tests Locally (if you have Java 17+)
```bash
# Compile and run tests
mvn compile
java -cp target/classes com.mars.tester.MarsApiTester
```

## 📊 Test Results

The test suite validates:
1. ✅ Health check connectivity
2. ✅ API discovery schema
3. ✅ REST sensor endpoints (8 sensors)
4. ✅ Telemetry topic endpoints (7 topics)
5. ✅ Actuator status endpoints (4 actuators)
6. ✅ Actuator control functionality

## 🏗️ Architecture

```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   Your Browser │────│  Mars Tester    │────│ Mars Simulator  │
│                 │    │   (Port 8081)   │    │   (Port 8080)   │
└─────────────────┘    └─────────────────┘    └─────────────────┘
```

## 🌟 Available IoT Devices

### Sensors (8 total)
- greenhouse_temperature
- entrance_humidity
- co2_hall
- hydroponic_ph
- water_tank_level
- corridor_pressure
- air_quality_pm25
- air_quality_voc

### Actuators (4 total)
- cooling_fan
- entrance_humidifier
- hall_ventilation
- habitat_heater

### Telemetry Topics (7 total)
- mars/telemetry/solar_array
- mars/telemetry/radiation
- mars/telemetry/life_support
- mars/telemetry/thermal_loop
- mars/telemetry/power_bus
- mars/telemetry/power_consumption
- mars/telemetry/airlock

## 🔧 Troubleshooting

### Port Conflicts
If ports 8080 or 8081 are occupied, modify them in `docker-compose.yml`:
```yaml
ports:
  - "8082:8080"  # Change simulator to 8082
  - "8083:8081"  # Change tester to 8083
```

### Service Not Starting
```bash
# Check logs
docker-compose logs mars-simulator
docker-compose logs mars-tester

# Restart services
docker-compose restart
```

### Build Issues
```bash
# Clean rebuild
docker-compose down
docker-compose build --no-cache
docker-compose up
```

## 📝 Development

### Local Development Setup
```bash
# Install dependencies
mvn install

# Run Spring Boot application
mvn spring-boot:run

# Run tests
mvn test
```

### Building Docker Image
```bash
# Build only the tester image
docker build -t mars-iot-tester .

# Run with existing simulator
docker run -p 8081:8081 --env MARS_SIMULATOR_URL=http://localhost:8080 mars-iot-tester
```

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch
3. Add tests for new functionality
4. Submit a pull request

## 📄 License

[Your License Information]

---

**Note**: This tester requires the `mars-iot-simulator:multiarch_v1` Docker image to be available. Make sure your colleagues have access to this image or provide it in a shared registry.
