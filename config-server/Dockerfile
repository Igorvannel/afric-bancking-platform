FROM maven:3.9.6-eclipse-temurin-21-alpine AS builder

WORKDIR /build

COPY pom.xml .
COPY config-server/pom.xml config-server/
COPY eureka-server/pom.xml eureka-server/
COPY api-gateway/pom.xml api-gateway/
COPY auth-service/pom.xml auth-service/
COPY accounting-service/pom.xml accounting-service/

RUN mvn dependency:go-offline -B

COPY config-server/src ./config-server/src

RUN mvn -pl config-server clean package -DskipTests spring-boot:repackage -B

# Vérifier le JAR (version compatible alpine)
RUN ls -la /build/config-server/target/

FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

RUN addgroup -S afric && adduser -S afric -G afric
USER afric

COPY --from=builder /build/config-server/target/config-server-*.jar app.jar

EXPOSE 8888

ENTRYPOINT ["java", "-jar", "app.jar"]
