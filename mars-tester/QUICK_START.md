# 🚀 Mars IoT Tester - Quick Start Guide for Colleagues

## ⚡ One-Command Setup

```bash
git clone <your-repo-url>
cd mars-tester
docker-compose up --build
```

## 📋 What This Provides

✅ **Mars IoT Simulator** - Complete Mars habitat simulation  
✅ **API Tester** - Automated testing suite  
✅ **Web Interface** - Access APIs via browser  
✅ **Full Documentation** - Complete API reference  

## 🌐 Access Points

Once running, visit these URLs:

- **Mars Simulator**: http://localhost:8080
- **Mars Tester**: http://localhost:8081
- **Health Check**: http://localhost:8081/api/health

## 🧪 Running Tests

After services are running:

### Option 1: Local Java (Recommended)
```bash
# Requires Java 17+
mvn compile
java -cp target/classes com.mars.tester.MarsApiTester
```

### Option 2: Direct API Calls
```bash
# Test individual endpoints
curl http://localhost:8080/health
curl http://localhost:8080/api/sensors
curl http://localhost:8080/api/actuators
```

## 📊 Available IoT Devices

### Sensors (8)
- greenhouse_temperature
- entrance_humidity  
- co2_hall
- hydroponic_ph
- water_tank_level
- corridor_pressure
- air_quality_pm25
- air_quality_voc

### Actuators (4)
- cooling_fan
- entrance_humidifier
- hall_ventilation  
- habitat_heater

### Telemetry Topics (7)
- mars/telemetry/solar_array
- mars/telemetry/radiation
- mars/telemetry/life_support
- mars/telemetry/thermal_loop
- mars/telemetry/power_bus
- mars/telemetry/power_consumption
- mars/telemetry/airlock

## 🔧 Troubleshooting

### Port Already in Use?
```bash
# Stop existing services
docker-compose down

# Or change ports in docker-compose.yml
ports:
  - "8082:8080"  # Simulator
  - "8083:8081"  # Tester
```

### Services Not Starting?
```bash
# Check logs
docker-compose logs mars-simulator
docker-compose logs mars-tester

# Restart everything
docker-compose down
docker-compose up --build
```

### Docker Issues?
```bash
# Verify Docker is running
docker --version
docker-compose --version

# Restart Docker daemon (if needed)
# Restart Docker Desktop or restart Docker service
```

## 📚 API Documentation

### Core Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/health` | Health check |
| GET | `/api/discovery` | Complete API schema |
| GET | `/api/sensors` | List all sensors |
| GET | `/api/actuators` | List all actuators |
| GET | `/api/telemetry/topics` | List telemetry topics |
| POST | `/api/actuators/{name}` | Control actuator |

### Example: Control an Actuator
```bash
# Turn ON cooling fan
curl -X POST http://localhost:8080/api/actuators/cooling_fan \
  -H "Content-Type: application/json" \
  -d '{"state": "ON"}'

# Turn OFF cooling fan  
curl -X POST http://localhost:8080/api/actuators/cooling_fan \
  -H "Content-Type: application/json" \
  -d '{"state": "OFF"}'
```

## 🛑 Stopping the Environment

```bash
# Stop all services
docker-compose down

# Stop and remove images (clean slate)
docker-compose down --rmi all
```

## 📞 Need Help?

1. Check the [full README.md](README.md) for detailed documentation
2. Verify Docker is installed and running
3. Ensure ports 8080/8081 are available
4. Check the logs with `docker-compose logs`

---

**Enjoy exploring the Mars IoT Simulator! 🚀🔴**
