# syntax=docker/dockerfile:1

FROM maven:3.9.9-eclipse-temurin-21 AS build
WORKDIR /workspace

COPY pom.xml ./
RUN --mount=type=cache,target=/root/.m2 mvn -q -DskipTests dependency:go-offline

COPY src ./src
RUN --mount=type=cache,target=/root/.m2 mvn -q -DskipTests package

FROM eclipse-temurin:21-jre
WORKDIR /app

ENV SPRING_PROFILES_ACTIVE=dev \
    SERVER_PORT=8080 \
    FILE_STORAGE_ROOT=/app/uploads

RUN apt-get update \
    && apt-get install -y --no-install-recommends curl \
    && rm -rf /var/lib/apt/lists/* \
    && mkdir -p /app/uploads
COPY --from=build /workspace/target/*.jar /app/fixledger-backend.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/fixledger-backend.jar"]