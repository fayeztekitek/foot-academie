# Implementation Plan: Nadi — Football Academy Management App

## Overview

Full-stack application (React/Vite PWA + Spring Boot REST API + PostgreSQL) for managing a football academy in Tunisia. Supports three roles (ADMIN, COACH, PARENT) with strict role-based access control, RGPD/INPDP compliance, TND-native billing, and mobile-first PWA offline support. Built incrementally across 8 sprints per the provided spec.

## Architecture Decisions

- **Monorepo**: Single repo with `backend/` (Spring Boot) and `frontend/` (React/Vite) subdirectories, plus `docker-compose.yml` at root.
- **Database**: H2 file-based in `dev` profile, PostgreSQL in `prod` profile. Same JPA entities, profile-driven DataSource config.
- **Auth**: Stateless JWT (Access + Refresh tokens), BCrypt password hashing. Role embedded in token claims.
- **API style**: RESTful, resource-oriented, pagination via `Pageable`, standard HTTP verbs.
- **Frontend**: React 18 + Vite, React Router v6, Tailwind CSS, React Query for data fetching/caching, `vite-plugin-pwa` for service worker/manifest.
- **Docker**: Multi-stage build for backend JAR, Nginx reverse proxy serving frontend static files + proxying `/api` to backend.
- **i18n**: `react-i18next` with French as default, Arabic locale stubbed from Sprint 0.
- **No prototype reference**: All UI built from prompt descriptions (Dashboard, Joueurs, Paiements, Entraînements, Entraîneurs, Catégories & créneaux, Portail parent).

## Dependency Graph

```
Sprint 0 (Foundation)
  ├── Spring Boot project init + Docker config
  ├── JWT Auth + Utilisateur entity + roles
  └── /health endpoint
        │
Sprint 1 (Joueurs & Parents)
  ├── Categorie, Parent, Joueur entities + CRUD APIs
  ├── Frontend: Joueurs list + search + filters
  └── Frontend: New player/parent forms
        │
Sprint 2 (Catégories, Entraîneurs, Créneaux)
  ├── Entraineur, Creneau entities + CRUD APIs
  ├── Frontend: Entraîneurs screen
  ├── Frontend: Catégories & créneaux screen
  └── Frontend: Calendar generated from DB
        │
Sprint 3 (Paiements)
  ├── Paiement entity + formulas + auto-generation
  ├── Frontend: Paiements screen + unpaid tracking
  └── CSV/Excel export
        │
Sprint 4 (Conformité & Documents)
  ├── Document entity + upload + alert engine
  ├── RGPD consent register + PDF export
  └── Frontend: Dashboard compliance panel
        │
Sprint 5 (Portail Parent & PWA)
  ├── Parent-scoped API endpoints
  ├── PWA manifest + service worker + offline cache
  ├── Online payment integration (Paymee/ClicToPay stub)
  └── Push notifications (FCM)
        │
Sprint 6 (Reporting & Security Hardening)
  ├── Dashboard stats + exports
  ├── Audit logging + security review
  └── PostgreSQL backup + DR plan
        │
Sprint 7 (Recette & Deployment)
  ├── Functional acceptance testing
  ├── User documentation
  ├── Data import (Excel)
  └── Production deploy + monitoring
```

## Sprint 0 — Detailed Task Breakdown

### Task 1: Initialize Spring Boot backend project
- Spring Boot 3.x with Java 17+
- Dependencies: Spring Web, Spring Data JPA, Spring Security, H2, PostgreSQL driver, Lombok, validation, Spring Boot Actuator
- Layered structure: controller / service / repository / model / dto / config / security / exception
- application.yml with `dev` and `prod` profiles
- H2 datasource for dev, PostgreSQL for prod
- `/health` endpoint via Actuator

### Task 2: Initialize React/Vite frontend project
- `npm create vite@latest frontend -- --template react`
- Install: react-router-dom, @tanstack/react-query, tailwindcss, react-i18next, vite-plugin-pwa
- Folder structure: src/pages, src/components, src/api, src/hooks, src/i18n, src/types
- Basic routing: `/login`, `/dashboard`, `/players`, `/payments`, `/training`, `/coaches`, `/categories`, `/parent`
- i18n setup with French locale

### Task 3: Docker-compose infrastructure
- `docker-compose.yml` with:
  - `db`: PostgreSQL 16 container with volume
  - `backend`: multi-stage build (Maven build + JRE runtime)
  - `frontend` + `nginx`: build Vite, serve via Nginx, proxy `/api` to backend
- `.env` for DB credentials, JWT secret
- Docker profiles: `dev` (local H2, no Docker DB) and `prod` (Docker PostgreSQL)

### Task 4: JWT Authentication system
- Utilisateur entity: id, email, motDePasseHash, role (enum: ADMIN/COACH/PARENT), actif
- BCrypt password hashing
- Login endpoint: `POST /api/auth/login` → returns access token + refresh token
- Refresh endpoint: `POST /api/auth/refresh`
- Logout endpoint: `POST /api/auth/logout` (token blacklist in-memory or Redis stub)
- SecurityFilterChain: JWT validation on all `/api/**` except `/api/auth/**`
- Role-based method security annotations
- Seed data: one ADMIN user for initial login

### Task 5: Frontend auth integration
- Login page with email/password form
- Auth context/hook: store JWT, attach to API requests via interceptor
- Protected route wrapper: redirect to `/login` if unauthenticated
- Axios/fetch instance with base URL + auth header injection
- Basic Dashboard shell (empty cards, role-aware navigation)

## Sprint 1 — Detailed Task Breakdown

### Task 6: Categorie entity + CRUD API
- Entity: id, nom, description, ageMin, ageMax
- Repository: JPA repository with search by nom
- Service: CRUD with validation
- Controller: `GET/POST/PUT/DELETE /api/categories` with pagination
- Role check: ADMIN only for write, all authenticated for read

### Task 7: Parent entity + CRUD API
- Entity: id, prenom, nom, telephone, email, consentementRGPD (boolean + date), utilisateurId (1:1)
- DTO:ParentCreateRequest, ParentResponse
- Controller: `GET/POST/PUT/DELETE /api/parents`
- Parent-scoped: PARENT role can only read their own profile

### Task 8: Joueur entity + CRUD API
- Entity: id, prenom, nom, dateNaissance, categorieId (FK), parentId (FK), statutPaiement (enum), photoUrl
- Controller: `GET/POST/PUT/DELETE /api/players`
- Filters: by categorieId, by parentId, search by nom/prenom
- Pagination for lists > 100

### Task 9: Frontend — Joueurs screen
- Players list table with search bar, category filter dropdown
- Connect to `GET /api/players` with React Query
- Pagination component
- "Nouveau joueur" modal/form: calls `POST /api/players`
- Link to parent selection (existing or new)
- Category filter populated from `GET /api/categories`

### Task 10: Frontend — Parents management
- Parents list (admin view)
- "Nouveau parent" form: calls `POST /api/parents`
- Parent detail view showing linked children

## Sprint 2 — Detailed Task Breakdown

### Task 11: Entraineur entity + CRUD API
- Entity: id, prenom, nom, specialite, telephone, email, utilisateurId (FK, optional)
- Many-to-Many with Categorie
- Controller: `GET/POST/PUT/DELETE /api/coaches`
- Constraint: deleting a coach blocks if linked to active creneaux (or requires reassignment)

### Task 12: Creneau entity + CRUD API
- Entity: id, jourSemaine (enum LUNDI-DIMANCHE), heureDebut, heureFin, categorieId (FK), entraineurId (FK), terrain
- Validation: no time overlap for same terrain, coach not double-booked
- Controller: `GET/POST/PUT/DELETE /api/slots`

### Task 13: Frontend — Entraîneurs screen
- Coaches list with specialties and linked categories
- Add/edit coach form with category multi-select

### Task 14: Frontend — Catégories & créneaux screen
- Categories list with CRUD
- Weekly schedule grid: columns = days, rows = time slots
- Slots fetched from API, rendered dynamically
- Add slot form: select category, coach, day, time, terrain

### Task 15: Frontend — Calendar/Training view
- Weekly calendar component generated from `/api/slots`
- Color-coded by category
- Coach and terrain info displayed

## Sprint 3 — Detailed Task Breakdown

### Task 16: Paiement entity + formula system
- Entity: id, joueurId, parentId, montant, devise (default TND), dateEcheance, datePaiement, statut (EN_ATTENTE/ PAYE/ EN_RETARD), moyenPaiement
- Formula enum: MENSUEL / TRIMESTRIEL / ANNUEL
- Auto-generation: on player inscription, create payment schedule based on formula
- Controller: `GET/POST/PUT /api/payments` (mark as paid, list by status)

### Task 17: Frontend — Paiements screen
- Payments table: filter by status (unpaid, paid, overdue)
- Mark as paid action
- Player payment status updated on dashboard
- Overdue highlights

### Task 18: CSV/Excel export
- `GET /api/reports/payments?month=YYYY-MM` → CSV or XLSX
- Columns: date, player, parent, amount, status, method
- Admin-only endpoint

## Sprint 4 — Detailed Task Breakdown

### Task 19: Document entity + upload API
- Entity: id, joueurId, type (enum: CERTIFICAT_MEDICAL, LICENCE_FTF, AUTORISATION_PARENTALE, CONSENTEMENT_IMAGE), fichierUrl, dateExpiration, statut (VALIDE/EXPIRE/EN_COURS)
- Multipart upload endpoint: `POST /api/documents/upload`
- Storage: local filesystem (dev) / S3-compatible (prod stub)

### Task 20: Alert engine
- Scheduled task: daily check for documents expiring within 30 days
- Create Notification entries for each expiry
- Dashboard endpoint: `GET /api/alerts/compliance` returning expiring docs

### Task 21: RGPD consent register
- Consent log entity: id, parentId, type, timestamp, ipAddress, exported
- `GET /api/rgpd/consent-register` → exportable as PDF
- PDF generation: iText or similar

### Task 22: Frontend — Dashboard compliance panel
- Alert cards showing expiring documents with countdown
- Document upload from mobile (camera/gallery)
- Consent register view for admin

## Sprint 5 — Detailed Task Breakdown

### Task 23: Parent-scoped API hardening
- Every endpoint returning player/payment/document data validates:
  - If PARENT role: only returns data for children where `joueur.parentId = currentParent.id`
  - Tested via integration tests (parent A cannot see parent B's data)

### Task 24: PWA manifest + service worker
- `manifest.json`: name "Nadi", icons, theme color, display: standalone
- Service worker: cache-first for static assets, network-first for API
- Offline fallback page
- Install prompt handling

### Task 25: Offline data caching
- Cache planning (slots) and parent dashboard data in IndexedDB
- React Query persistence adapter
- Sync indicator in UI

### Task 26: Push notifications (FCM)
- Firebase integration for push token registration
- Backend: `POST /api/notifications/register-device`
- Trigger notifications on: payment reminder, schedule change, competition alert

### Task 27: Payment gateway stub
- Integrate Paymee or ClicToPay API (sandbox)
- `POST /api/payments/initiate` → returns payment URL
- Webhook endpoint for payment confirmation
- Fallback: generate downloadable invoice PDF

## Sprint 6 — Detailed Task Breakdown

### Task 28: Dashboard statistics
- `GET /api/stats/dashboard`: attendance rate, revenue by month, players per category
- Charts: attendance trend, revenue trend, category distribution
- Exportable as PDF/CSV

### Task 29: Security audit + hardening
- Review every endpoint for role-based access
- Add audit logging for sensitive data access (who, when, what)
- Rate limiting on auth endpoints
- CORS configuration review
- SQL injection / XSS checks

### Task 30: PostgreSQL backup + DR
- Automated daily pg_dump cron job
- Backup retention policy (30 days)
- Restore procedure documented

## Sprint 7 — Detailed Task Breakdown

### Task 31: Functional acceptance testing
- Walk through all user stories with stakeholder
- Test all 3 roles end-to-end
- Mobile responsiveness validation
- Offline mode validation

### Task 32: Data import
- Excel/CSV import utility for historical player/parent/payment data
- Validation and error reporting during import

### Task 33: User documentation
- Admin guide: managing players, payments, documents
- Coach guide: viewing schedule, marking attendance
- Parent guide: installing PWA, viewing planning, making payments

### Task 34: Production deployment
- Docker-compose.prod.yml with SSL/TLS (Let's Encrypt)
- Domain configuration
- Monitoring: health checks, log aggregation
- Go-live checklist

## Risks and Mitigations

| Risk | Impact | Mitigation |
|------|--------|------------|
| No prototype UI reference | Medium | Build clean UI from prompt descriptions; use Tailwind for rapid prototyping |
| Payment gateway integration complexity | High | Start with invoice PDF stub in Sprint 3; real gateway in Sprint 5 |
| RGPD/INPDP compliance ambiguity | Medium | Implement conservative consent model; document assumptions for legal review |
| PWA offline complexity | Medium | Cache only read-heavy data (planning, dashboard); keep mutations online-only |
| Coach deleting with active slots | Low | Business rule: block deletion, require reassignment first |

## Open Questions

1. Payment gateway: Paymee vs ClicToPay vs other? (Sandbox integration acceptable for Sprint 5?)
2. Photo storage: local filesystem vs S3? (Recommend local for dev, configurable for prod)
3. How many categories/players expected? (Affects pagination thresholds)
4. Arabic i18n: full translation needed at launch or just stubbed?
5. FCM project: existing Firebase project or create new?
