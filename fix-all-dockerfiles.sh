#!/bin/bash

# Liste des services
services=("config-server" "eureka-server" "api-gateway" "auth-service" "accounting-service")

# Association des ports
declare -A ports=(
  ["config-server"]=8888
  ["eureka-server"]=8761
  ["api-gateway"]=8080
  ["auth-service"]=8081
  ["accounting-service"]=8082
)

for service in "${services[@]}"; do
  port=${ports[$service]}
  
  echo "📝 Correction du Dockerfile pour $service (port $port)"
  
  cat > ${service}/Dockerfile << 'DOCKERFILE_HEAD'
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

DOCKERFILE_HEAD

  # Partie spécifique au service
  cat >> ${service}/Dockerfile << DOCKERFILE_MID

# Copie des sources du service uniquement
COPY ${service}/src ./${service}/src

# Build uniquement ce service
RUN mvn -pl ${service} clean package -DskipTests

# ─── Stage 2 : Runtime ──────────────────────────────────────────────────────
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Utilisateur non-root pour la sécurité
RUN addgroup -S afric && adduser -S afric -G afric
USER afric

COPY --from=builder /build/${service}/target/*.jar app.jar

EXPOSE ${port}

HEALTHCHECK --interval=30s --timeout=10s --start-period=40s --retries=3 \\
  CMD wget -qO- http://localhost:${port}/actuator/health || exit 1

ENTRYPOINT ["java", \\
  "-XX:+UseContainerSupport", \\
  "-XX:MaxRAMPercentage=75.0", \\
  "-Djava.security.egd=file:/dev/./urandom", \\
  "-jar", "app.jar"]
DOCKERFILE_MID

  echo "✅ Dockerfile corrigé pour $service"
done

echo ""
echo "🎉 Tous les Dockerfiles ont été mis à jour !"
echo ""
echo "📌 Prochaines étapes :"
echo "   docker compose down -v"
echo "   docker compose build --no-cache"
echo "   docker compose up -d"
