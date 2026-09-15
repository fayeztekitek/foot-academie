# Documentation Technique — Nadi

## 1. Prérequis

| Outil | Version | Usage |
|-------|---------|-------|
| Java | 17+ | Backend Spring Boot |
| Maven | 3.8+ | Build backend |
| Node.js | 20+ | Frontend React |
| Docker | 24+ | Conteneurisation |
| Docker Compose | 2.20+ | Orchestration |
| PostgreSQL | 16 | Base de données production |

## 2. Installation locale

### Backend
```bash
cd backend
mvn clean install
mvn spring-boot:run
# API disponible sur http://localhost:8080/api
```

### Frontend
```bash
cd frontend
npm install
npm run dev
# UI disponible sur http://localhost:5173
```

### Docker (recommandé)
```bash
docker-compose up -d
# PostgreSQL: localhost:5432
# Backend: localhost:8080
# Frontend: localhost:80
```

## 3. Variables d'environnement

| Variable | Description | Défaut |
|----------|-------------|--------|
| `SPRING_PROFILES_ACTIVE` | Profil Spring (dev/prod) | dev |
| `DB_HOST` | Hôte PostgreSQL | localhost |
| `DB_PORT` | Port PostgreSQL | 5432 |
| `DB_NAME` | Nom de la base | nadi |
| `DB_USERNAME` | Utilisateur PostgreSQL | nadi |
| `DB_PASSWORD` | Mot de passe PostgreSQL | nadi |
| `JWT_SECRET` | Secret pour les tokens JWT | (à changer en prod) |

## 4. Bascule H2 ↔ PostgreSQL

Aucun changement de code requis. La bascule se fait via le profil Spring :

```bash
# Développement (H2)
SPRING_PROFILES_ACTIVE=dev mvn spring-boot:run

# Production (PostgreSQL)
SPRING_PROFILES_ACTIVE=prod DB_HOST=db DB_NAME=nadi mvn spring-boot:run
```

Le fichier `application.yml` contient les configurations pour chaque profil :
- `dev` : H2 fichier, console H2 activée, SQL affiché
- `prod` : PostgreSQL, ddl-auto=validate, SQL masqué

## 5. Architecture API

### Authentification
- `POST /api/auth/login` → retourne access + refresh tokens
- `POST /api/auth/refresh` → rafraîchit l'access token

### Endpoints principaux
| Ressource | Endpoints | Rôles |
|-----------|-----------|-------|
| Joueurs | GET/POST/PUT/DELETE `/api/players` | ADMIN, COACH (write), PARENT (read own) |
| Parents | GET/POST/PUT/DELETE `/api/parents` | ADMIN (write), PARENT (read own) |
| Catégories | GET/POST/PUT/DELETE `/api/categories` | ADMIN (write), all (read) |
| Entraîneurs | GET/POST/PUT/DELETE `/api/coaches` | ADMIN (write), all (read) |
| Créneaux | GET/POST/PUT/DELETE `/api/slots` | ADMIN, COACH (write), all (read) |
| Paiements | GET/POST `/api/payments` | ADMIN (all), PARENT (own) |
| Documents | GET/POST `/api/documents` | ADMIN (write), PARENT (read own) |
| Notifications | GET/POST `/api/notifications` | all (own) |
| Dashboard | GET `/api/dashboard/stats` | ADMIN |
| Stats | GET `/api/stats/detailed` | ADMIN |
| Audit | GET `/api/audit` | ADMIN |
| Import | POST `/api/import/players`, `/api/import/payments` | ADMIN |
| RGPD | GET/POST `/api/rgpd` | ADMIN (write), PARENT (read own) |
| Rapports | GET `/api/reports/payments` | ADMIN |

### Contrôle d'accès
- Tous les endpoints sont protégés par JWT
- PARENT : accès limité à ses propres données (vérifié côté serveur)
- COACH : accès en lecture aux joueurs de ses catégories
- ADMIN : accès complet

## 6. Structure du projet

```
foot-academie/
├── backend/
│   ├── src/main/java/com/nadi/
│   │   ├── config/        # Security, Audit, DataSeeder, Scheduled
│   │   ├── controller/    # REST controllers
│   │   ├── dto/           # Request/Response DTOs
│   │   ├── exception/     # Global exception handler
│   │   ├── model/         # JPA entities + enums
│   │   ├── repository/    # Spring Data repositories
│   │   ├── security/      # JWT, UserDetailsService
│   │   └── service/       # Business logic
│   ├── src/test/java/     # Integration tests
│   ├── Dockerfile
│   └── pom.xml
├── frontend/
│   ├── src/
│   │   ├── api/           # Axios API clients
│   │   ├── components/    # Reusable components
│   │   ├── hooks/         # Custom hooks (useAuth, useOnlineStatus)
│   │   ├── i18n/          # Translations
│   │   ├── pages/         # Page components
│   │   └── App.jsx        # Router + providers
│   ├── Dockerfile
│   ├── nginx.conf
│   └── vite.config.js
├── scripts/
│   ├── backup.sh          # PostgreSQL backup
│   └── restore.sh         # PostgreSQL restore
├── docs/
│   ├── UTILISATEUR.md     # User guide
│   ├── TECHNIQUE.md       # This file
│   └── PRA.md             # Disaster Recovery
├── docker-compose.yml
├── nginx.prod.conf
└── .env
```

## 7. Déploiement production

```bash
# 1. Cloner le repo
git clone <repo-url> && cd foot-academie

# 2. Configurer les variables
cp .env .env.prod
# Éditer .env.prod avec les vrais secrets

# 3. Build et démarrer
docker-compose --env-file .env.prod up -d --build

# 4. Vérifier
docker-compose ps
curl http://localhost/api/actuator/health
```

## 8. Monitoring

- Actuator health : `GET /api/actuator/health`
- Logs : `docker-compose logs -f backend`
- Backup automatique : service Docker `backup` (cron 02:00 daily)
- Audit logs : table `audit_log` en base

## 9. Tests

```bash
# Backend tests
cd backend && mvn test

# Frontend lint
cd frontend && npm run lint
```
