ARG MAVEN_IMAGE=maven:3.9.9-eclipse-temurin-21
ARG JRE_IMAGE=eclipse-temurin:21-jre

FROM ${MAVEN_IMAGE} AS build
WORKDIR /workspace

COPY pom.xml ./
RUN mvn -q -DskipTests dependency:go-offline

COPY src ./src
RUN mvn -q -DskipTests package

FROM ${JRE_IMAGE}
WORKDIR /app

ENV SPRING_PROFILES_ACTIVE=dev \
    SERVER_PORT=8080 \
    FILE_STORAGE_ROOT=/app/uploads

RUN mkdir -p /app/uploads
COPY --from=build /workspace/target/*.jar /app/fixledger-backend.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/fixledger-backend.jar"]