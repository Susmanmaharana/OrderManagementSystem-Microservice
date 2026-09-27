# Build context: repository root
FROM maven:3.8.8-eclipse-temurin-8 AS build
WORKDIR /workspace

COPY pom.xml .
COPY order-service/pom.xml order-service/
COPY inventory-service/pom.xml inventory-service/
COPY payment-service/pom.xml payment-service/
COPY notification-service/pom.xml notification-service/
COPY api-gateway/pom.xml api-gateway/

COPY api-gateway/src api-gateway/src

RUN --mount=type=cache,target=/root/.m2 \
    mvn -pl api-gateway -am package -DskipTests -B

FROM eclipse-temurin:8-jre-jammy
WORKDIR /app
COPY --from=build /workspace/api-gateway/target/api-gateway-*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
