# Mars IoT Tester - Quick Test Runner (PowerShell)
# This script runs the complete test suite against the running simulator

Write-Host "🚀 Mars IoT Tester - Running API Tests..." -ForegroundColor Green
Write-Host "==========================================" -ForegroundColor Yellow

# Check if Docker is running
try {
    docker info | Out-Null
} catch {
    Write-Host "❌ Docker is not running. Please start Docker first." -ForegroundColor Red
    exit 1
}

# Check if containers are running
$containers = docker-compose ps
if (-not ($containers -match "Up")) {
    Write-Host "🔧 Starting Mars IoT environment..." -ForegroundColor Blue
    docker-compose up -d
    
    Write-Host "⏳ Waiting for services to be ready..." -ForegroundColor Blue
    Start-Sleep -Seconds 10
}

# Run the tests using the standalone test class
Write-Host "🧪 Executing API tests..." -ForegroundColor Blue
docker-compose exec -T mars-tester java -cp "/app/BOOT-INF/classes:/app/BOOT-INF/lib/*" com.mars.tester.MarsApiTester

Write-Host ""
Write-Host "✅ Test execution completed!" -ForegroundColor Green
Write-Host ""
Write-Host "📊 Access the APIs directly:" -ForegroundColor Cyan
Write-Host "   Simulator: http://localhost:8080" -ForegroundColor White
Write-Host "   Tester:    http://localhost:8081" -ForegroundColor White
Write-Host ""
Write-Host "🔧 To stop the environment: docker-compose down" -ForegroundColor Yellow
