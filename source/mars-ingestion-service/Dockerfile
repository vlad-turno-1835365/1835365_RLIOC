# Mars IoT Tester - Multi-stage build for easy deployment
FROM eclipse-temurin:17-jdk-alpine AS builder
WORKDIR /app

# Install Maven
RUN apk add --no-cache maven

COPY pom.xml .
COPY src ./src
RUN mvn package -DskipTests

# Runtime image
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=builder /app/target/tester-1.0.jar app.jar
EXPOSE 8081
ENTRYPOINT ["java", "-jar", "app.jar"]
