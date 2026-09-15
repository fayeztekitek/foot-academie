# Task List: Nadi — Football Academy Management App

## Sprint 0 — Cadrage & socle technique

- [ ] Task 1: Initialize Spring Boot backend project
  - Acceptance: `mvn spring-boot:run` starts server on port 8080, `/health` returns 200
  - Verify: `curl http://localhost:8080/actuator/health`
  - Files: `backend/pom.xml`, `backend/src/main/java/com/nadi/NadiApplication.java`, `backend/src/main/resources/application.yml`
  - Scope: M

- [ ] Task 2: Initialize React/Vite frontend project
  - Acceptance: `npm run dev` serves React app on port 5173, routes render placeholder pages
  - Verify: Visit `/login`, `/dashboard`, `/players` — all render
  - Files: `frontend/package.json`, `frontend/vite.config.js`, `frontend/src/`
  - Scope: M

- [ ] Task 3: Docker-compose infrastructure
  - Acceptance: `docker-compose up db` starts PostgreSQL; `docker-compose up` builds full stack
  - Verify: `docker-compose ps` shows all services healthy
  - Files: `docker-compose.yml`, `docker-compose.prod.yml`, `backend/Dockerfile`, `frontend/Dockerfile`, `nginx/nginx.conf`
  - Scope: L

- [ ] Task 4: JWT Authentication system
  - Acceptance: POST `/api/auth/login` with valid credentials returns JWT; invalid returns 401; protected endpoint requires valid token
  - Verify: Test with curl: login → use token → call protected endpoint; no token → 403
  - Files: `backend/src/.../security/`, `backend/src/.../model/Utilisateur.java`, `backend/src/.../controller/AuthController.java`
  - Scope: L

- [ ] Task 5: Frontend auth integration
  - Acceptance: Login form submits to API, stores JWT, redirects to dashboard; protected pages redirect to login if no token
  - Verify: Manual login flow works end-to-end in browser
  - Files: `frontend/src/pages/Login.jsx`, `frontend/src/api/auth.js`, `frontend/src/hooks/useAuth.js`, `frontend/src/App.jsx`
  - Scope: M

### Checkpoint: Sprint 0
- [ ] `docker-compose up` runs full stack (backend + DB + frontend)
- [ ] Login with admin user returns valid JWT
- [ ] `/health` endpoint responds
- [ ] H2 dev profile works without Docker
- [ ] PostgreSQL prod profile works with Docker

---

## Sprint 1 — Joueurs & parents

- [ ] Task 6: Categorie entity + CRUD API
  - Acceptance: CRUD endpoints for categories with pagination and search
  - Verify: `GET /api/categories` returns paginated list; `POST` creates; `PUT` updates; `DELETE` removes
  - Files: `backend/src/.../model/Categorie.java`, `backend/src/.../controller/CategorieController.java`
  - Scope: M

- [ ] Task 7: Parent entity + CRUD API
  - Acceptance: CRUD endpoints; PARENT role can only read own profile
  - Verify: Admin can list all parents; parent token only returns own data
  - Files: `backend/src/.../model/Parent.java`, `backend/src/.../controller/ParentController.java`
  - Scope: M

- [ ] Task 8: Joueur entity + CRUD API
  - Acceptance: CRUD endpoints with filters (categorie, parent, search); pagination > 100
  - Verify: Create player linked to parent and category; list with filters works
  - Files: `backend/src/.../model/Joueur.java`, `backend/src/.../controller/JoueurController.java`
  - Scope: L

- [ ] Task 9: Frontend — Joueurs screen
  - Acceptance: Players list with real API data, search, category filter, pagination, create form
  - Verify: Add a player via form → appears in list → persists after page refresh
  - Files: `frontend/src/pages/Players.jsx`, `frontend/src/components/PlayerForm.jsx`
  - Scope: L

- [ ] Task 10: Frontend — Parents management
  - Acceptance: Parents list and create form connected to API
  - Verify: Create parent → appears in list
  - Files: `frontend/src/pages/Parents.jsx`, `frontend/src/components/ParentForm.jsx`
  - Scope: M

### Checkpoint: Sprint 1
- [ ] Can create a parent, then create a player linked to that parent
- [ ] Refreshing the page preserves all data (H2 file persistence)
- [ ] Category filter on players screen shows real categories from DB

---

## Sprint 2 — Catégories, entraîneurs & créneaux

- [ ] Task 11: Entraineur entity + CRUD API
- [ ] Task 12: Creneau entity + CRUD API (with overlap validation)
- [ ] Task 13: Frontend — Entraîneurs screen
- [ ] Task 14: Frontend — Catégories & créneaux screen (dynamic grid)
- [ ] Task 15: Frontend — Calendar/Training view (auto-generated from DB)

### Checkpoint: Sprint 2
- [ ] Adding/removing a slot updates the calendar immediately
- [ ] Deleting a coach with active slots is blocked

---

## Sprint 3 — Paiements & facturation

- [ ] Task 16: Paiement entity + formula system + auto-generation
- [ ] Task 17: Frontend — Paiements screen (filter, mark paid, overdue)
- [ ] Task 18: CSV/Excel export endpoint

### Checkpoint: Sprint 3
- [ ] Marking a payment "payé" updates player status on dashboard
- [ ] Monthly export contains all transactions

---

## Sprint 4 — Conformité, documents & alertes

- [ ] Task 19: Document entity + upload API
- [ ] Task 20: Alert engine (daily expiry check)
- [ ] Task 21: RGPD consent register + PDF export
- [ ] Task 22: Frontend — Dashboard compliance panel + document upload

### Checkpoint: Sprint 4
- [ ] Document expiring in <30 days triggers visible alert on dashboard
- [ ] Consent register exports as PDF

---

## Sprint 5 — Portail parent mobile & PWA

- [ ] Task 23: Parent-scoped API hardening (integration tests)
- [ ] Task 24: PWA manifest + service worker
- [ ] Task 25: Offline data caching (IndexedDB)
- [ ] Task 26: Push notifications (FCM)
- [ ] Task 27: Payment gateway stub (Paymee/ClicToPay sandbox)

### Checkpoint: Sprint 5
- [ ] Parent installs PWA on home screen
- [ ] Planning visible offline
- [ ] Payment reminder notification received

---

## Sprint 6 — Reporting, sécurité & durcissement

- [ ] Task 28: Dashboard statistics + charts + exports
- [ ] Task 29: Security audit + hardening + audit logging
- [ ] Task 30: PostgreSQL backup + DR

### Checkpoint: Sprint 6
- [ ] Parent account cannot access another parent's data (penetration test)
- [ ] Stats dashboard shows real data

---

## Sprint 7 — Recette, formation & mise en production

- [ ] Task 31: Functional acceptance testing
- [ ] Task 32: Excel data import utility
- [ ] Task 33: User documentation (admin, coach, parent guides)
- [ ] Task 34: Production deployment + monitoring

### Checkpoint: Sprint 7
- [ ] Application accessible in production
- [ ] Documentation delivered
- [ ] Training session completed
