#!/bin/bash

# Build Script

mvn clean
docker-compose down

# Build JAR
mvn package -DskipTests

# Build Docker 
docker build -t mars-iot-tester:latest .

# Setup test
docker-compose up -d

# Wait for services startup
sleep 15

# Run tests
java -cp target/classes com.mars.tester.MarsApiTester

echo ""
echo "Package ready :)"
echo "To do list:"
echo "1) Run docker-compose up --build"
echo "2) Access APIs at:"
echo "   - Simulator: http://localhost:8080"
echo "   - Tester: http://localhost:8081"
echo "3) Run tests with java -cp target/classes com.mars.tester.MarsApiTester"
echo ""

