# ---- build phase -------
FROM maven:3.9.11-eclipse-temurin-21 AS builder

WORKDIR /build

# Copy only pom.xml first (dependency caching)
COPY pom.xml .
RUN mvn -q -e -DskipTests dependency:go-offline

# Copy source code
COPY src ./src

# Build the application
RUN mvn -q -DskipTests package

# ---- runtime phase ----
FROM eclipse-temurin:21-jdk-jammy

# Create non-root user (security best practice)
# otherwise if hackers get root access, system will gets compromise
RUN useradd -ms /bin/bash appuser

WORKDIR /app

# Copy built jar from builder stage
COPY --from=builder /build/target/*.jar app.jar

# Set ownership
RUN chown appuser:appuser app.jar

# Switch to non-root user
USER appuser

# JVM tuning for containers
# with these options containers memeory will be consumed upto 75 %, example
# if you run the container with 1g memory, it will consumer 750MB
ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0"

EXPOSE 8080

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]