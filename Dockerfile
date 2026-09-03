FROM eclipse-temurin:11-jre-alpine

WORKDIR /app

COPY .mvn/ .mvn
COPY mvnw pom.xml ./
COPY src ./src

RUN apk add --no-cache maven \
    && ./mvnw dependency:resolve \
    && ./mvnw package -DskipTests

FROM eclipse-temurin:11-jre-alpine

WORKDIR /app

COPY --from=0 /app/target/*.jar app.jar

EXPOSE 8080

CMD ["java", "-jar", "app.jar"]