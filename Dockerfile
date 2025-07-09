# Multi-stage Dockerfile for Test Automation Framework

# Stage 1: Build stage
FROM maven:3.9.5-eclipse-temurin-11 AS builder

# Set working directory
WORKDIR /app

# Copy pom.xml and download dependencies
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy source code
COPY src/ ./src/
COPY config/ ./config/

# Build the application
RUN mvn clean compile test-compile

# Stage 2: Runtime stage
FROM eclipse-temurin:11-jre-alpine

# Install dependencies
RUN apk add --no-cache \
    curl \
    bash \
    chromium \
    chromium-chromedriver \
    firefox \
    && rm -rf /var/cache/apk/*

# Install Maven
RUN curl -fsSL https://archive.apache.org/dist/maven/maven-3/3.9.5/binaries/apache-maven-3.9.5-bin.tar.gz \
    | tar -xzC /opt \
    && ln -s /opt/apache-maven-3.9.5 /opt/maven

# Set environment variables
ENV JAVA_HOME=/opt/java/openjdk
ENV MAVEN_HOME=/opt/maven
ENV PATH=$MAVEN_HOME/bin:$PATH
ENV CHROME_BIN=/usr/bin/chromium-browser
ENV CHROMEDRIVER_PATH=/usr/bin/chromedriver

# Create app user
RUN addgroup -g 1000 appuser && \
    adduser -u 1000 -G appuser -D appuser

# Set working directory
WORKDIR /app

# Copy built application from builder stage
COPY --from=builder /app .
COPY --from=builder /root/.m2 /home/appuser/.m2

# Create necessary directories
RUN mkdir -p logs reports/allure-results reports/extent-report reports/screenshots \
    && chown -R appuser:appuser /app /home/appuser

# Switch to app user
USER appuser

# Create entrypoint script
COPY --chown=appuser:appuser <<'EOF' /app/entrypoint.sh
#!/bin/bash
set -e

# Default values
ENVIRONMENT=${ENVIRONMENT:-qa}
BROWSER=${BROWSER:-chrome}
TEST_SUITE=${TEST_SUITE:-sanity}
HEADLESS=${HEADLESS:-true}
THREAD_COUNT=${THREAD_COUNT:-3}

echo "Starting test execution..."
echo "Environment: $ENVIRONMENT"
echo "Browser: $BROWSER"
echo "Test Suite: $TEST_SUITE"
echo "Headless: $HEADLESS"
echo "Thread Count: $THREAD_COUNT"

# Set system properties
JAVA_OPTS="-Xmx2g -XX:+UseG1GC"
MAVEN_OPTS="-Xmx2g -XX:+UseG1GC"

# Export environment variables
export ENVIRONMENT
export BROWSER
export HEADLESS
export THREAD_COUNT

# Run tests based on suite
case $TEST_SUITE in
    sanity)
        echo "Running sanity tests..."
        mvn test -Psanity -Dthread.count=$THREAD_COUNT
        ;;
    regression)
        echo "Running regression tests..."
        mvn test -Pregression -Dthread.count=$THREAD_COUNT
        ;;
    all)
        echo "Running all tests..."
        mvn test -Psanity -Dthread.count=$THREAD_COUNT
        mvn test -Pregression -Dthread.count=$THREAD_COUNT
        ;;
    *)
        echo "Unknown test suite: $TEST_SUITE"
        echo "Available suites: sanity, regression, all"
        exit 1
        ;;
esac

# Generate reports
echo "Generating reports..."
mvn allure:report || echo "Allure report generation failed"

echo "Test execution completed!"
EOF

RUN chmod +x /app/entrypoint.sh

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=5s --retries=3 \
    CMD curl -f http://localhost:8080/health || exit 1

# Expose ports (if needed for reporting server)
EXPOSE 8080

# Set entrypoint
ENTRYPOINT ["/app/entrypoint.sh"]

# Default command
CMD ["sanity"]