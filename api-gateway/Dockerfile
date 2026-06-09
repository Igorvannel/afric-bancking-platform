# ─── Stage 1 : Build ────────────────────────────────────────────────────────
FROM maven:3.9.6-eclipse-temurin-21-alpine AS builder

WORKDIR /build

# Copie du POM parent
COPY pom.xml .

# Copie des POMs de TOUS les modules (nécessaire pour la résolution Maven)
COPY config-server/pom.xml config-server/
COPY eureka-server/pom.xml eureka-server/
COPY api-gateway/pom.xml api-gateway/
COPY auth-service/pom.xml auth-service/
COPY accounting-service/pom.xml accounting-service/

# Téléchargement des dépendances pour TOUS les modules (optimisation cache)
RUN mvn dependency:go-offline -B


# Copie des sources du service uniquement
COPY api-gateway/src ./api-gateway/src

# Build uniquement ce service
RUN mvn -pl api-gateway clean package -DskipTests

# ─── Stage 2 : Runtime ──────────────────────────────────────────────────────
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Utilisateur non-root pour la sécurité
RUN addgroup -S afric && adduser -S afric -G afric
USER afric

COPY --from=builder /build/api-gateway/target/*.jar app.jar

EXPOSE 8080

HEALTHCHECK --interval=30s --timeout=10s --start-period=40s --retries=3 \
  CMD wget -qO- http://localhost:8080/actuator/health || exit 1

ENTRYPOINT ["java", \
  "-XX:+UseContainerSupport", \
  "-XX:MaxRAMPercentage=75.0", \
  "-Djava.security.egd=file:/dev/./urandom", \
  "-jar", "app.jar"]
