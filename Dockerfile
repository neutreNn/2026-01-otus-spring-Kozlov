# syntax=docker/dockerfile:1.7

FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /workspace/hw17

COPY hw17/pom.xml .
RUN mvn -B dependency:go-offline

COPY hw17/src ./src
RUN mvn -B -DskipTests package

FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

RUN addgroup -S spring && adduser -S spring -G spring

COPY --from=build /workspace/hw17/target/hw17-docker-library-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8080

USER spring:spring

ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75.0", "-jar", "/app/app.jar"]
