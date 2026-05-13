FROM maven:3.9.11-eclipse-temurin-21 AS build

WORKDIR /workspace
COPY pom.xml ./
RUN mvn -q -DskipTests dependency:go-offline
COPY src ./src
RUN mvn -q -DskipTests package

FROM eclipse-temurin:21-jre

WORKDIR /app
RUN apt-get update \
    && apt-get install -y --no-install-recommends curl \
    && rm -rf /var/lib/apt/lists/* \
    && groupadd --system cofrete \
    && useradd --system --gid cofrete cofrete
COPY --from=build /workspace/target/finance-worker-0.0.1-SNAPSHOT.jar /app/finance-worker.jar

ENV SPRING_PROFILES_ACTIVE=local
EXPOSE 8081
USER cofrete

HEALTHCHECK --interval=30s --timeout=5s --start-period=30s --retries=3 \
    CMD curl -fsS http://localhost:8081/actuator/health || exit 1

ENTRYPOINT ["java", "-jar", "/app/finance-worker.jar"]
