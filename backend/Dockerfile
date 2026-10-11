# Multi-stage build for Spring Boot Backend
FROM eclipse-temurin:21-jdk-alpine AS builder

WORKDIR /app

# Copy Maven wrapper and configuration
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./

# Convert line endings if needed and make wrapper executable
RUN tr -d '\r' < mvnw > mvnw_fixed && mv mvnw_fixed mvnw && chmod +x mvnw

# Download dependencies (fail gracefully if offline goal cannot resolve all plugins)
RUN ./mvnw dependency:go-offline -B -ntp || true

# Copy source code and package application
COPY src ./src
RUN ./mvnw clean package -DskipTests -B -ntp

# Runtime stage
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Create non-root user for security
RUN addgroup -S appgroup && adduser -S appuser -G appgroup

# Copy compiled JAR
COPY --from=builder /app/target/*.jar app.jar
RUN chown appuser:appgroup app.jar

USER appuser

EXPOSE 8080

# Tune memory for Fargate (container has 1GB total, JVM allocated max 512MB)
ENV JAVA_OPTS="-Xms256m -Xmx512m -XX:+UseG1GC"

ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar app.jar"]
