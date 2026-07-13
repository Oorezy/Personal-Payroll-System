FROM maven:3.9.9-eclipse-temurin-21 AS builder
LABEL authors="OOREOLUWA-IBITOWA"

WORKDIR /build

COPY pom.xml .


COPY AuthenticationResource ./AuthenticationResource
COPY AuthenticationService ./AuthenticationService
COPY GatewayService ./GatewayService
COPY DiscoveryService ./DiscoveryService
COPY IntrotechService ./IntrotechService


RUN --mount=type=cache,target=/root/.m2 \
    mvn clean package -pl DiscoveryService -am -DskipTests

FROM eclipse-temurin:21-jdk-jammy

WORKDIR /app

COPY --from=builder /build/DiscoveryService/target/*.jar app.jar
EXPOSE 7000
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -DHOME_DIR=/INTROTECH -Dspring.config.location=/INTROTECH/configs/ -Dspring.config.name=discovery-service -Dspring.profiles.active=prod -jar app.jar"]