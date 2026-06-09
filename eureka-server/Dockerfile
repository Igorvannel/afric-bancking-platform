# ─── Stage 1 : Build ────────────────────────────────────────────────────────
FROM maven:3.9.6-eclipse-temurin-21-alpine AS builder

WORKDIR /build

# Copie du POM parent
COPY pom.xml .

# Copie des POMs de TOUS les modules
COPY config-server/pom.xml config-server/
COPY eureka-server/pom.xml eureka-server/
COPY api-gateway/pom.xml api-gateway/
COPY auth-service/pom.xml auth-service/
COPY accounting-service/pom.xml accounting-service/

# Téléchargement des dépendances
RUN mvn dependency:go-offline -B

# Copie des sources
COPY eureka-server/src ./eureka-server/src

# Build - Génère le JAR dans target/
RUN mvn -pl eureka-server clean package -DskipTests -B

# Debug: lister le contenu de target
RUN echo "=== Contenu du dossier target ===" && ls -la /build/eureka-server/target/

# ─── Stage 2 : Runtime ──────────────────────────────────────────────────────
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Utilisateur non-root
RUN addgroup -S afric && adduser -S afric -G afric
USER afric

# Copier le JAR généré (attention: copier le fichier spécifique)
COPY --from=builder /build/eureka-server/target/eureka-server-*.jar app.jar

# Vérifier que le JAR est copié
RUN test -f app.jar && echo "JAR copié avec succès" || echo "JAR manquant"

EXPOSE 8761

HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
  CMD wget -qO- http://localhost:8761/actuator/health || exit 1

ENTRYPOINT ["java", \
  "-XX:+UseContainerSupport", \
  "-XX:MaxRAMPercentage=75.0", \
  "-Djava.security.egd=file:/dev/./urandom", \
  "-jar", "app.jar"]
