# 🏦 Afric Banking Platform

> Plateforme bancaire microservices construite avec **Java 21 + Spring Cloud 2023 + MongoDB Replica Set**

[![Java](https://img.shields.io/badge/Java-21-orange?logo=openjdk)](https://openjdk.org/projects/jdk/21/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.5-brightgreen?logo=spring)](https://spring.io/projects/spring-boot)
[![Spring Cloud](https://img.shields.io/badge/Spring%20Cloud-2023.0.1-brightgreen?logo=spring)](https://spring.io/projects/spring-cloud)
[![MongoDB](https://img.shields.io/badge/MongoDB-7.0-green?logo=mongodb)](https://www.mongodb.com/)
[![Docker](https://img.shields.io/badge/Docker-Compose-blue?logo=docker)](https://docs.docker.com/compose/)

---

## 📐 Architecture

```
                          ┌─────────────────────────────────────────┐
                          │           CLIENT (REST / Postman)        │
                          └──────────────────┬──────────────────────┘
                                             │ HTTP :8080
                                             ▼
                          ┌─────────────────────────────────────────┐
                          │            API GATEWAY  :8080            │
                          │   • Routage dynamique via Eureka         │
                          │   • Validation JWT (Bearer token)        │
                          │   • Propagation X-User-Id header         │
                          └────────────┬──────────────┬─────────────┘
                                       │              │
                    ┌──────────────────┘              └──────────────────┐
                    ▼                                                     ▼
     ┌──────────────────────────┐                    ┌──────────────────────────┐
     │     AUTH SERVICE  :8081  │                    │ ACCOUNTING SERVICE :8082  │
     │  • POST /api/register    │                    │  • GET  /api/account      │
     │  • POST /api/login       │                    │  • POST /api/account/credit│
     │  • GET  /api/user        │                    │  • POST /api/account/debit │
     │  • Spring Security + JWT │                    │  • @Transactional          │
     └────────────┬─────────────┘                    └──────────┬───────────────┘
                  │                                              │
                  └──────────────────────┬───────────────────────┘
                                         ▼
                          ┌─────────────────────────────────────────┐
                          │       MONGODB REPLICA SET (rs0)          │
                          │   mongo1 (PRIMARY) :27017                │
                          │   mongo2 (SECONDARY) :27018              │
                          │   mongo3 (SECONDARY) :27019              │
                          │                                          │
                          │   Collections :                          │
                          │   • users                                │
                          │   • account                              │
                          │   • accounting_journal                   │
                          └─────────────────────────────────────────┘

                  ┌────────────────────────────────────────────────┐
                  │           INFRASTRUCTURE SPRING CLOUD           │
                  │  Eureka Server  :8761  (Service Registry)       │
                  │  Config Server  :8888  (Config centralisé)      │
                  └────────────────────────────────────────────────┘
```

---

## 🗂️ Structure du projet

```
afric-banking-platform/
│
├── 📦 config-server/          # Spring Cloud Config Server
├── 📦 eureka-server/          # Netflix Eureka (Service Registry)
├── 📦 api-gateway/            # Spring Cloud Gateway + JWT Filter
│
├── 📦 auth-service/           # Microservice Auth
│   └── src/main/java/com/afric/auth/
│       ├── config/            # SecurityConfig, MongoAuditingConfig
│       ├── controller/        # AuthController
│       ├── document/          # User (@Document MongoDB)
│       ├── dto/               # RegisterRequest, LoginRequest, AuthResponse, UserResponse
│       ├── exception/         # GlobalExceptionHandler
│       ├── filter/            # JwtAuthenticationFilter
│       ├── repository/        # UserRepository
│       ├── service/           # AuthService, CustomUserDetailsService
│       └── util/              # JwtUtil
│
├── 📦 accounting-service/     # Microservice Comptabilité
│   └── src/main/java/com/afric/accounting/
│       ├── config/            # SecurityConfig, MongoAuditingConfig
│       ├── controller/        # AccountingController
│       ├── document/          # Account, AccountingJournal (@Document)
│       ├── dto/               # TransactionRequest, AccountResponse, TransactionResponse
│       ├── exception/         # AccountNotFoundException, InsufficientFundsException
│       ├── repository/        # AccountRepository, AccountingJournalRepository
│       └── service/           # AccountingService
│
├── 🐳 docker-compose.yml      # Orchestration complète
├── 🍃 mongo-init/
│   └── init-replica.sh        # Init Replica Set + index MongoDB
├── 🔑 .env.example
└── 📖 README.md
```

---

## 🚀 Démarrage rapide

### Prérequis

| Outil | Version minimum |
|-------|----------------|
| Docker | 24+ |
| Docker Compose | 2.20+ |
| (optionnel) Java | 21+ |
| (optionnel) Maven | 3.9+ |

### 1. Cloner et configurer

```bash
git clone https://github.com/your-org/afric-banking-platform.git
cd afric-banking-platform

# Configurer les variables d'environnement
cp .env .env
# Éditer .env : changer JWT_SECRET en production !
```

### 2. Lancer toute la plateforme

```bash
docker-compose up --build -d
```

> ⏱️ **Premier démarrage** : environ 3-5 minutes (build Maven + init MongoDB)

### 3. Vérifier l'état des services

```bash
# Status de tous les containers
docker-compose ps

# Logs en temps réel
docker-compose logs -f api-gateway auth-service accounting-service

# Health checks individuels
curl http://localhost:8761          # Eureka Dashboard
curl http://localhost:8888/actuator/health  # Config Server
curl http://localhost:8080/actuator/health  # Gateway
```

### 4. Arrêter la plateforme

```bash
docker-compose down

# Avec suppression des volumes MongoDB (reset complet)
docker-compose down -v
```

---

## 🔌 API Reference

**Base URL :** `http://localhost:8080`

### Authentification

#### `POST /api/register` — Créer un compte
```bash
curl -X POST http://localhost:8080/api/register \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Jean Hugues",
    "email": "jean.hugues@afric.cm",
    "password": "SecurePass123!"
  }'
```

**Réponse `201 Created` :**
```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiJ9...",
  "refreshToken": "eyJhbGciOiJIUzI1NiJ9...",
  "tokenType": "Bearer",
  "expiresIn": 86400,
  "user": {
    "id": "665abc123def456789",
    "name": "Jean Hugues",
    "email": "jean.hugues@afric.cm",
    "createdAt": "2024-06-09T10:00:00Z"
  }
}
```

---

#### `POST /api/login` — Se connecter
```bash
curl -X POST http://localhost:8080/api/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "jean.hugues@afric.cm",
    "password": "SecurePass123!"
  }'
```

---

#### `GET /api/user` — Profil utilisateur connecté 🔒
```bash
curl http://localhost:8080/api/user \
  -H "Authorization: Bearer <ACCESS_TOKEN>"
```

---

### Comptabilité

#### `POST /api/account/credit` — Créditer un compte 🔒
```bash
curl -X POST http://localhost:8080/api/account/credit \
  -H "Authorization: Bearer <ACCESS_TOKEN>" \
  -H "Content-Type: application/json" \
  -d '{
    "amount": 50000.00,
    "description": "Dépôt initial"
  }'
```

**Réponse `200 OK` :**
```json
{
  "journalId": "665xyz789",
  "accountId": "665abc123",
  "direction": "CREDIT",
  "amount": 50000.0,
  "balanceBefore": 0.0,
  "balanceAfter": 50000.0,
  "createdAt": "2024-06-09T10:05:00Z"
}
```

---

#### `POST /api/account/debit` — Débiter un compte 🔒
```bash
curl -X POST http://localhost:8080/api/account/debit \
  -H "Authorization: Bearer <ACCESS_TOKEN>" \
  -H "Content-Type: application/json" \
  -d '{
    "amount": 15000.00,
    "description": "Retrait"
  }'
```

> ⚠️ Retourne `422 Unprocessable Entity` si le solde est insuffisant.

---

## 🔒 Sécurité

### Flow d'authentification JWT

```
Client → [POST /api/login] → Gateway → Auth Service
                                         ↓
                              Vérification credentials
                                         ↓
                              Génération JWT (HS256)
                                         ↓
Client ← [200 + {accessToken}] ←────────┘

Client → [GET /api/user + Bearer token] → Gateway
                                            ↓
                                    Validation JWT
                                    Extraction userId
                                    Injection X-User-Id header
                                            ↓
                                      Auth Service
                                            ↓
Client ← [200 + UserResponse] ←────────────┘
```

### Caractéristiques de sécurité
- ✅ Tokens JWT **HS256** signés et horodatés
- ✅ Mots de passe **BCrypt** (strength=12)
- ✅ Sessions **Stateless** (pas de session côté serveur)
- ✅ Routes publiques explicitement déclarées
- ✅ Headers **X-User-Id** propagés internement (non exposés au client)
- ✅ Utilisateurs **non-root** dans les containers Docker
- ✅ Validation des inputs (`@Valid`, annotations Bean Validation)

---

## 💾 Modèle de données

### Collection `users`
| Champ | Type | Description |
|-------|------|-------------|
| `_id` | ObjectId | Identifiant unique |
| `name` | String | Nom complet |
| `email` | String (unique) | Email de connexion |
| `password` | String | Hash BCrypt |
| `createdAt` | Timestamp | Date de création (auto) |
| `updatedAt` | Timestamp | Date de modification (auto) |
| `createdBy` | String | Audit (auto) |
| `updatedBy` | String | Audit (auto) |

### Collection `account`
| Champ | Type | Description |
|-------|------|-------------|
| `_id` | ObjectId | Identifiant unique |
| `userId` | String (unique) | Référence utilisateur |
| `accountNumber` | String (unique) | Numéro de compte (AF + 12 chars) |
| `balance` | Double | Solde courant |
| `currency` | String | Devise (XAF par défaut) |

### Collection `accounting_journal`
| Champ | Type | Description |
|-------|------|-------------|
| `_id` | ObjectId | Identifiant unique |
| `accountId` | String | Référence compte |
| `direction` | Enum | `CREDIT` ou `DEBIT` |
| `amount` | Double | Montant mouvementé |
| `balanceBefore` | Double | Solde avant opération |
| `balanceAfter` | Double | Solde après opération |

---

## 🧪 Tests avec Postman

Importer la collection Postman fournie ou suivre ce workflow :

1. **Register** → copier `accessToken`
2. **Login** (optionnel, re-génère un token)
3. Définir variable `TOKEN = accessToken`
4. **GET /api/user** avec `Authorization: Bearer {{TOKEN}}`
5. **POST /api/account/credit** avec `amount: 100000`
6. **POST /api/account/debit** avec `amount: 30000`
7. Tester **solde insuffisant** avec `amount: 999999999`

---

## 🐳 Infrastructure MongoDB Replica Set

Le Replica Set `rs0` est constitué de **3 nœuds** pour garantir :
- **Haute disponibilité** : bascule automatique si le PRIMARY tombe
- **Transactions ACID multi-documents** : requises par `@Transactional`
- **Lecture cohérente** : toutes les lectures depuis le PRIMARY

```
rs0
 ├── mongo1 (PRIMARY, priority=2)   → :27017
 ├── mongo2 (SECONDARY, priority=1) → :27018
 └── mongo3 (SECONDARY, priority=1) → :27019
```

> 📌 **Pourquoi un Replica Set ?** MongoDB ne supporte les transactions multi-documents que sur un Replica Set (pas en mode standalone). L'annotation `@Transactional` sur `credit()` et `debit()` en dépend directement.

---

## 🔧 Développement local (sans Docker)

```bash
# Démarrer uniquement MongoDB en Replica Set
docker-compose up -d mongo1 mongo2 mongo3 mongo-init

# Lancer les services dans l'ordre
cd eureka-server  && mvn spring-boot:run &
cd config-server  && mvn spring-boot:run &
cd auth-service   && mvn spring-boot:run &
cd accounting-service && mvn spring-boot:run &
cd api-gateway    && mvn spring-boot:run &
```

---

## 📊 Ports exposés

| Service | Port | URL |
|---------|------|-----|
| API Gateway | 8080 | http://localhost:8080 |
| Auth Service | 8081 | http://localhost:8081 |
| Accounting Service | 8082 | http://localhost:8082 |
| Eureka Dashboard | 8761 | http://localhost:8761 |
| Config Server | 8888 | http://localhost:8888 |
| MongoDB PRIMARY | 27017 | mongodb://localhost:27017 |

---

## 🛠️ Stack technique

| Technologie | Version | Usage |
|-------------|---------|-------|
| Java | 21 (LTS) | Langage principal |
| Spring Boot | 3.2.5 | Framework applicatif |
| Spring Cloud | 2023.0.1 | Gateway, Eureka, Config |
| Spring Security | 6.x | Authentification & autorisation |
| MongoDB | 7.0 | Base de données NoSQL |
| JJWT | 0.12.5 | Génération/validation JWT |
| Lombok | latest | Réduction du boilerplate |
| Docker | 24+ | Containerisation |
| Docker Compose | 2.20+ | Orchestration locale |

---

*Développé dans le cadre du Test Technique par Igor Sibemou*
