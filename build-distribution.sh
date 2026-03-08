#!/bin/bash

# Mars IoT Tester - Distribution Build Script
# This script creates a complete distribution package for sharing

echo "🏗️  Building Mars IoT Tester Distribution Package..."
echo "=================================================="

# Clean previous builds
echo "🧹 Cleaning previous builds..."
mvn clean
docker-compose down

# Build the JAR
echo "📦 Building Java application..."
mvn package -DskipTests

# Build Docker image
echo "🐳 Building Docker image..."
docker build -t mars-iot-tester:latest .

# Test the setup
echo "🧪 Testing the complete setup..."
docker-compose up -d

echo "⏳ Waiting for services to start..."
sleep 15

# Run tests
echo "🚀 Running API tests..."
java -cp target/classes com.mars.tester.MarsApiTester

echo ""
echo "✅ Distribution package ready!"
echo ""
echo "📋 What your colleagues need to do:"
echo "   1. Ensure Docker is installed"
echo "   2. Run: docker-compose up --build"
echo "   3. Access APIs at:"
echo "      - Simulator: http://localhost:8080"
echo "      - Tester: http://localhost:8081"
echo "   4. Run tests with: java -cp target/classes com.mars.tester.MarsApiTester"
echo ""
echo "📦 Distribution includes:"
echo "   ✅ Docker image (mars-iot-tester:latest)"
echo "   ✅ JAR file (target/tester-1.0.jar)"
echo "   ✅ Docker Compose configuration"
echo "   ✅ Complete documentation"
