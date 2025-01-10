# Stage 1: Build stage
FROM maven:3.8.5-openjdk-17 AS build
COPY . .
RUN mvn clean package -DskipTests

# Stage 2: Runtime stage
FROM openjdk:17.0.1-jdk-slim

# Set the working directory
WORKDIR /app

# Copy the JAR file from the build stage
COPY --from=build /target/GymTrainer-0.0.1-SNAPSHOT.jar GymTrainer.jar

# Expose the port
EXPOSE 8080

# Entrypoint command to run the Java application
ENTRYPOINT ["java", "-jar", "GymTrainer.jar"]
