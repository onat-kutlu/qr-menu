# Build stage
FROM maven:3.9.4-eclipse-temurin-21 AS builder

WORKDIR /app
COPY pom.xml .
COPY src ./src

RUN mvn clean package -DskipTests

# Runtime stage
FROM eclipse-temurin:21-jre-jammy

WORKDIR /app

RUN apt-get update && apt-get install -y --no-install-recommends curl tzdata && rm -rf /var/lib/apt/lists/*

# Create application user
RUN useradd -m -u 1000 appuser

# Create storage directories
RUN mkdir -p /app/storage/logs /app/storage/menus /app/storage/backups && \
    chown -R appuser:appuser /app

# Copy JAR from builder
COPY --from=builder --chown=appuser:appuser /app/target/qr-menu-*.jar app.jar

# Switch to non-root user
USER appuser

EXPOSE 8080

HEALTHCHECK --interval=30s --timeout=3s --start-period=40s --retries=3 \
    CMD curl -f http://127.0.0.1:8080/actuator/health || exit 1

ENTRYPOINT ["java", "-jar", "app.jar"]
