# Multi-stage build for Spring Boot Backend
ARG APP_VERSION=1.0.0-dev

FROM eclipse-temurin:21-jdk-alpine AS builder
ARG APP_VERSION

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

ARG APP_VERSION=1.0.0-dev
LABEL org.opencontainers.image.title="gym-management-backend" \
      org.opencontainers.image.version="${APP_VERSION}"

WORKDIR /app

# Create non-root user for security
RUN addgroup -S appgroup && adduser -S appuser -G appgroup

# Copy compiled JAR and create writable log directory for non-root user
COPY --from=builder /app/target/*.jar app.jar
RUN mkdir -p /app/logs && chown -R appuser:appgroup /app

USER appuser

EXPOSE 8080

# Expose runtime version metadata
ENV APP_VERSION=${APP_VERSION}

# Tune memory for Fargate (container has 1GB total, JVM allocated max 512MB heap + 160MB metaspace)
ENV JAVA_OPTS="-Xms256m -Xmx512m -XX:MaxMetaspaceSize=160m -XX:+UseG1GC"

ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar app.jar"]
