# Stage 1: Build the application
FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /app

# Copy pom.xml and source code
COPY pom.xml .
COPY src ./src

# Package the application (skipping tests for faster initial build)
RUN mvn clean package -DskipTests

# Stage 2: Run the application
FROM eclipse-temurin:21-jre-jammy
WORKDIR /app

# Copy the built jar from Stage 1
COPY --from=build /app/target/*.jar app.jar

# Expose the internal port your Spring Boot app runs on
EXPOSE 8080

# Run the application with production-optimized memory settings
ENTRYPOINT ["java", "-jar", "-Dspring.profiles.active=prod", "app.jar"]