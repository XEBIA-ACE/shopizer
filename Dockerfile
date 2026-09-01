# Use a multi-stage build to reduce image size
# Stage 1: Build the application using Maven
FROM maven:3.8.7-eclipse-temurin-21 AS build

WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

# Stage 2: Run the application with the specified JDK version
FROM eclipse-temurin:21-jre-alpine

# Set deployment directory
WORKDIR /app

# Copy the dependencies and the jar file from the first stage
COPY --from=build /app/target/shopizer.jar /app/shopizer.jar

# Expose the application port
EXPOSE 8080

# Set the startup command to run the jar
CMD ["java", "-jar", "shopizer.jar"]