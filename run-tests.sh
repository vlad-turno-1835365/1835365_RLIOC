#!/bin/bash

# Mars IoT Tester - Quick Test Runner
# This script runs the complete test suite against the running simulator

echo "🚀 Mars IoT Tester - Running API Tests..."
echo "=========================================="

# Check if Docker is running
if ! docker info > /dev/null 2>&1; then
    echo "❌ Docker is not running. Please start Docker first."
    exit 1
fi

# Check if containers are running
if ! docker-compose ps | grep -q "Up"; then
    echo "🔧 Starting Mars IoT environment..."
    docker-compose up -d
    
    echo "⏳ Waiting for services to be ready..."
    sleep 10
fi

# Run the tests using the standalone test class
echo "🧪 Executing API tests..."
docker-compose exec -T mars-tester java -cp /app/BOOT-INF/classes:/app/BOOT-INF/lib/* com.mars.tester.MarsApiTester

echo ""
echo "✅ Test execution completed!"
echo ""
echo "📊 Access the APIs directly:"
echo "   Simulator: http://localhost:8080"
echo "   Tester:    http://localhost:8081"
echo ""
echo "🔧 To stop the environment: docker-compose down"
