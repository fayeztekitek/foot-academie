# Plan de Reprise d'Activité (PRA) — Nadi

## 1. Périodicité des sauvegardes

| Type | Fréquence | Rétention | Stockage |
|------|-----------|-----------|----------|
| Daily | Tous les jours à 02:00 | 30 jours | Local + cloud |
| Weekly | Dimanche à 03:00 | 90 jours | Cloud |
| Manual | Avant chaque mise à jour | Indéfini | Cloud |

## 2. Procédure de restauration

### Restauration complète
```bash
# Arrêter le backend
docker-compose stop backend

# Restaurer la base
./scripts/restore.sh /backups/postgres/nadi_daily_YYYYMMDD_HHMMSS.sql.gz

# Redémarrer
docker-compose up -d backend
```

### Restauration partielle (une table)
```bash
pg_restore -h localhost -p 5432 -U nadi -d nadi \
    --data-only --table=paiement \
    /backups/postgres/nadi_daily_YYYYMMDD_HHMMSS.sql.gz
```

## 3. RTO / RPO

| Métrique | Objectif |
|----------|----------|
| RTO (Recovery Time Objective) | < 30 minutes |
| RPO (Recovery Point Objective) | < 24 heures (backup daily) |

## 4. Contacts

| Rôle | Nom | Contact |
|------|-----|---------|
| Admin technique | [À définir] | [À définir] |
| Hébergeur | [À définir] | [À définir] |
| DBA | [À définir] | [À définir] |

## 5. Vérification mensuelle

- [ ] Tester la restauration sur un environnement de staging
- [ ] Vérifier l'intégrité des backups (pg_restore --list)
- [ ] Vérifier l'espace disque des backups
- [ ] Mettre à jour les contacts PRA
