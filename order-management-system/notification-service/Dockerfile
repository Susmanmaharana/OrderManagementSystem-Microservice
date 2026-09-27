# Build context: repository root
FROM maven:3.8.8-eclipse-temurin-8 AS build
WORKDIR /workspace

COPY pom.xml .
COPY order-service/pom.xml order-service/
COPY inventory-service/pom.xml inventory-service/
COPY payment-service/pom.xml payment-service/
COPY notification-service/pom.xml notification-service/
COPY api-gateway/pom.xml api-gateway/

COPY notification-service/src notification-service/src

RUN --mount=type=cache,target=/root/.m2 \
    mvn -pl notification-service -am package -DskipTests -B

FROM eclipse-temurin:8-jre-jammy
WORKDIR /app
COPY --from=build /workspace/notification-service/target/notification-service-*.jar app.jar
EXPOSE 8084
ENTRYPOINT ["java", "-jar", "app.jar"]
