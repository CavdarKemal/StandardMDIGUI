# Multi-stage build for StandardMDIGUI
# =====================================

# Stage 1: Build
FROM maven:3.9-eclipse-temurin-23 AS builder

WORKDIR /app

# Copy pom.xml first for dependency caching
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy source code and build
COPY src ./src
COPY deployment ./deployment
RUN mvn clean package -DskipTests -q

# Stage 2: Runtime with GUI support
FROM eclipse-temurin:23-jdk

# Install X11 libraries for Swing GUI
RUN apt-get update && apt-get install -y --no-install-recommends \
    libxext6 \
    libxrender1 \
    libxtst6 \
    libxi6 \
    libfreetype6 \
    fontconfig \
    fonts-dejavu-core \
    && rm -rf /var/lib/apt/lists/*

# Create app user
RUN groupadd -r appuser && useradd -r -g appuser appuser

WORKDIR /app

# Copy built artifacts from builder stage
COPY --from=builder /app/target/standard-mdi-gui-app-*.jar ./app.jar
COPY --from=builder /app/target/lib ./lib
COPY --from=builder /app/deployment/start.sh ./start.sh

# Create config directory for volume mount (must be owned by appuser)
RUN mkdir -p /app/config && chown -R appuser:appuser /app && chmod +x start.sh

USER appuser

# Environment variables for GUI
ENV DISPLAY=:0
ENV _JAVA_OPTIONS="-Dawt.useSystemAAFontSettings=on -Dswing.aatext=true"

# Default command
CMD ["java", "-jar", "app.jar"]
