# Go-Live Checklist — Nadi

## Pre-Deployment
- [ ] Copy `.env.prod.example` → `.env.prod` and fill in:
  - [ ] `DB_PASSWORD` — strong random password
  - [ ] `JWT_SECRET` — 64+ char random string
  - [ ] `DOMAIN` — your domain name
- [ ] Configure DNS A record → server IP
- [ ] Open ports 80, 443 on server firewall

## Deploy
```bash
# 1. Clone repo on server
git clone <repo-url> && cd foot-academie

# 2. Configure environment
cp .env.prod.example .env.prod
nano .env.prod  # fill in real values

# 3. Start with SSL
docker compose -f docker-compose.prod.yml up -d --build

# 4. Get SSL certificate (first time only)
docker compose -f docker-compose.prod.yml run --rm certbot certonly \
  --webroot --webroot-path=/var/www/certbot \
  -d your-domain.tn --agree-tos -m admin@your-domain.tn

# 5. Restart nginx to pick up cert
docker compose -f docker-compose.prod.yml restart nginx
```

## Verify
- [ ] `https://your-domain.tn` loads the login page
- [ ] `https://your-domain.tn/api/actuator/health` returns `{"status":"UP"}`
- [ ] Login with admin credentials
- [ ] Create a test player + parent
- [ ] Verify RGPD consent page loads
- [ ] Test PDF export
- [ ] Check backup is running (look for `/backups/nadi/*.sql.gz`)

## Security
- [ ] Rate limiting active (try 6 rapid logins → 429)
- [ ] CORS configured for production domain only
- [ ] Security headers present (X-Frame-Options, HSTS, etc.)
- [ ] Actuator endpoints restricted (not public)
- [ ] No secrets in git history

## Monitoring
- [ ] Health checks passing in `docker compose ps`
- [ ] Backup cron running daily at 02:00
- [ ] Logs accessible via `docker compose -f docker-compose.prod.yml logs -f backend`

## Backup & Recovery
- [ ] Test restore: `./scripts/restore.sh /backups/nadi/nadi_YYYYMMDD_020001.sql.gz`
- [ ] Verify data integrity after restore
- [ ] Backup retention: 30 days automatic

## Rollback Plan
```bash
# If something breaks:
docker compose -f docker-compose.prod.yml down
# Restore from latest backup:
./scripts/restore.sh /backups/nadi/nadi_LATEST.sql.gz
# Re-deploy previous version:
git checkout <previous-tag>
docker compose -f docker-compose.prod.yml up -d --build
```
