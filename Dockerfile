# Stage 1: Build the project using Maven
FROM maven:3.8.6-openjdk-21 as build

WORKDIR /app

# Copy the pom.xml and the src directory
COPY pom.xml .
COPY src ./src

# Resolve and compile dependencies and build the project
RUN mvn clean package -DskipTests

# Stage 2: Create the runnable Java image
FROM eclipse-temurin:21-jre-alpine

WORKDIR /opt/app

# Copy only the executable jar file from the previous stage
COPY --from=build /app/target/sm-shop-3.2.3.jar /opt/app/app.jar

# Expose port 8080 to the outside world
EXPOSE 8080

# Default command to run the app
CMD ["java", "-jar", "app.jar"]