FROM maven:3.9.11-eclipse-temurin-21 AS build

WORKDIR /workspace
COPY pom.xml ./
RUN mvn -q -DskipTests dependency:go-offline
COPY src ./src
RUN mvn -q -DskipTests package

FROM eclipse-temurin:21-jre

WORKDIR /app
RUN groupadd --system cofrete && useradd --system --gid cofrete cofrete
COPY --from=build /workspace/target/core-api-0.0.1-SNAPSHOT.jar /app/core-api.jar

ENV SPRING_PROFILES_ACTIVE=local
EXPOSE 8080
USER cofrete

ENTRYPOINT ["java", "-jar", "/app/core-api.jar"]
