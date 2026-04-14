# ---- build phase -------
FROM maven:3.9.11-eclipse-temurin-21 AS builder

WORKDIR /build

# Now copy the full source code
COPY . .

# Build only ykw-auth-service (no need to rebuild other services)
RUN mvn -q -pl ykw-auth-service -am -DskipTests package

# ---- runtime phase ----
FROM eclipse-temurin:21-jre-alpine

# Create a non-root user (never run containers as root in prod)
RUN addgroup -S appuser && adduser -S appuser -G appuser

WORKDIR /app

# Copy only the built jar for ykw-auth-service
COPY --from=builder /build/ykw-auth-service/target/*.jar app.jar

# Give ownership to non-root user
RUN chown appuser:appuser app.jar

# Switch to safer user
USER appuser

# JVM will size heap based on container limits instead of host machine
# MaxRAMPercentage=75 means it will use up to 75% of container memory
ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0"

EXPOSE 8080

# Using sh -c so JAVA_OPTS gets expanded properly
ENTRYPOINT ["sh", "-c", "java $JAVA_TOOL_OPTIONS $JAVA_OPTS -jar app.jar"]