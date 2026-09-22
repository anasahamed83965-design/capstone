# Babysitter Booking backend (Spring Boot) - multi-stage build.
# Works on Railway, Render, or any Docker host. No local Java/Maven needed.
# Required env vars at runtime: DB_URL, DB_USERNAME, DB_PASSWORD.
# Optional: PORT (platforms like Railway/Render inject it automatically),
# JWT_SECRET, CORS_ALLOWED_ORIGINS, H2_CONSOLE_ENABLED=false.

# ---- Build stage: compile + package (tests run in CI, skipped here for speed) ----
FROM maven:3.9.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY backend/pom.xml ./pom.xml
RUN mvn -B dependency:go-offline
COPY backend/src ./src
RUN mvn -B clean package -DskipTests

# ---- Run stage: minimal JRE image ----
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/target/babysitter-booking-*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
