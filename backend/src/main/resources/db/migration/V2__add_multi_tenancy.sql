-- Nadi Academie - Multi-Tenancy Migration Script
-- Run this BEFORE starting the application with ddl-auto: update

-- Step 1: Create academie table
CREATE TABLE IF NOT EXISTS academie (
    id BIGSERIAL PRIMARY KEY,
    slug VARCHAR(255) NOT NULL UNIQUE,
    nom VARCHAR(255) NOT NULL,
    logo_url VARCHAR(500),
    adresse VARCHAR(500),
    ville VARCHAR(100),
    telephone VARCHAR(50),
    email VARCHAR(255),
    active BOOLEAN DEFAULT TRUE,
    date_activation DATE,
    date_expiration DATE,
    plan VARCHAR(20) DEFAULT 'FREE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Step 2: Add tenant_id to all tables
DO $$
BEGIN
    -- utilisateur
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name='utilisateur' AND column_name='tenant_id') THEN
        ALTER TABLE utilisateur ADD COLUMN tenant_id BIGINT NOT NULL DEFAULT 1;
    END IF;

    -- categorie
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name='categorie' AND column_name='tenant_id') THEN
        ALTER TABLE categorie ADD COLUMN tenant_id BIGINT NOT NULL DEFAULT 1;
    END IF;

    -- parent
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name='parent' AND column_name='tenant_id') THEN
        ALTER TABLE parent ADD COLUMN tenant_id BIGINT NOT NULL DEFAULT 1;
    END IF;

    -- joueur
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name='joueur' AND column_name='tenant_id') THEN
        ALTER TABLE joueur ADD COLUMN tenant_id BIGINT NOT NULL DEFAULT 1;
    END IF;

    -- entraineur
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name='entraineur' AND column_name='tenant_id') THEN
        ALTER TABLE entraineur ADD COLUMN tenant_id BIGINT NOT NULL DEFAULT 1;
    END IF;

    -- creneau
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name='creneau' AND column_name='tenant_id') THEN
        ALTER TABLE creneau ADD COLUMN tenant_id BIGINT NOT NULL DEFAULT 1;
    END IF;

    -- paiement
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name='paiement' AND column_name='tenant_id') THEN
        ALTER TABLE paiement ADD COLUMN tenant_id BIGINT NOT NULL DEFAULT 1;
    END IF;

    -- absence
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name='absence' AND column_name='tenant_id') THEN
        ALTER TABLE absence ADD COLUMN tenant_id BIGINT NOT NULL DEFAULT 1;
    END IF;

    -- evenement
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name='evenement' AND column_name='tenant_id') THEN
        ALTER TABLE evenement ADD COLUMN tenant_id BIGINT NOT NULL DEFAULT 1;
    END IF;

    -- convocation
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name='convocation' AND column_name='tenant_id') THEN
        ALTER TABLE convocation ADD COLUMN tenant_id BIGINT NOT NULL DEFAULT 1;
    END IF;

    -- document
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name='document' AND column_name='tenant_id') THEN
        ALTER TABLE document ADD COLUMN tenant_id BIGINT NOT NULL DEFAULT 1;
    END IF;

    -- notification
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name='notification' AND column_name='tenant_id') THEN
        ALTER TABLE notification ADD COLUMN tenant_id BIGINT NOT NULL DEFAULT 1;
    END IF;

    -- device_token
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name='device_token' AND column_name='tenant_id') THEN
        ALTER TABLE device_token ADD COLUMN tenant_id BIGINT NOT NULL DEFAULT 1;
    END IF;

    -- consentement_rgpd
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name='consentement_rgpd' AND column_name='tenant_id') THEN
        ALTER TABLE consentement_rgpd ADD COLUMN tenant_id BIGINT NOT NULL DEFAULT 1;
    END IF;

    -- audit_log
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name='audit_log' AND column_name='tenant_id') THEN
        ALTER TABLE audit_log ADD COLUMN tenant_id BIGINT NOT NULL DEFAULT 1;
    END IF;
END $$;

-- Step 3: Add unique constraints for email per tenant
DO $$
BEGIN
    -- utilisateur: email + tenant_id unique
    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname='uk_utilisateur_email_tenant') THEN
        ALTER TABLE utilisateur DROP CONSTRAINT IF EXISTS uk_utilisateur_email;
        ALTER TABLE utilisateur ADD CONSTRAINT uk_utilisateur_email_tenant UNIQUE (email, tenant_id);
    END IF;

    -- parent: email + tenant_id unique
    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname='uk_parent_email_tenant') THEN
        ALTER TABLE parent DROP CONSTRAINT IF EXISTS uk_parent_email;
        ALTER TABLE parent ADD CONSTRAINT uk_parent_email_tenant UNIQUE (email, tenant_id);
    END IF;

    -- entraineur: email + tenant_id unique
    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname='uk_entraineur_email_tenant') THEN
        ALTER TABLE entraineur DROP CONSTRAINT IF EXISTS uk_entraineur_email;
        ALTER TABLE entraineur ADD CONSTRAINT uk_entraineur_email_tenant UNIQUE (email, tenant_id);
    END IF;

    -- creneau: terrain + jour + heure + tenant unique
    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname='uk_creneau_unique_tenant') THEN
        ALTER TABLE creneau DROP CONSTRAINT IF EXISTS uk_creneau_terrain_jour_heure;
        ALTER TABLE creneau ADD CONSTRAINT uk_creneau_unique_tenant UNIQUE (terrain, jour_semaine, heure_debut, tenant_id);
    END IF;
END $$;

-- Step 4: Add foreign key constraints for tenant_id
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname='fk_utilisateur_tenant') THEN
        ALTER TABLE utilisateur ADD CONSTRAINT fk_utilisateur_tenant FOREIGN KEY (tenant_id) REFERENCES academie(id);
    END IF;

    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname='fk_categorie_tenant') THEN
        ALTER TABLE categorie ADD CONSTRAINT fk_categorie_tenant FOREIGN KEY (tenant_id) REFERENCES academie(id);
    END IF;

    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname='fk_parent_tenant') THEN
        ALTER TABLE parent ADD CONSTRAINT fk_parent_tenant FOREIGN KEY (tenant_id) REFERENCES academie(id);
    END IF;

    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname='fk_joueur_tenant') THEN
        ALTER TABLE joueur ADD CONSTRAINT fk_joueur_tenant FOREIGN KEY (tenant_id) REFERENCES academie(id);
    END IF;

    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname='fk_entraineur_tenant') THEN
        ALTER TABLE entraineur ADD CONSTRAINT fk_entraineur_tenant FOREIGN KEY (tenant_id) REFERENCES academie(id);
    END IF;

    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname='fk_creneau_tenant') THEN
        ALTER TABLE creneau ADD CONSTRAINT fk_creneau_tenant FOREIGN KEY (tenant_id) REFERENCES academie(id);
    END IF;

    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname='fk_paiement_tenant') THEN
        ALTER TABLE paiement ADD CONSTRAINT fk_paiement_tenant FOREIGN KEY (tenant_id) REFERENCES academie(id);
    END IF;

    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname='fk_absence_tenant') THEN
        ALTER TABLE absence ADD CONSTRAINT fk_absence_tenant FOREIGN KEY (tenant_id) REFERENCES academie(id);
    END IF;

    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname='fk_evenement_tenant') THEN
        ALTER TABLE evenement ADD CONSTRAINT fk_evenement_tenant FOREIGN KEY (tenant_id) REFERENCES academie(id);
    END IF;

    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname='fk_convocation_tenant') THEN
        ALTER TABLE convocation ADD CONSTRAINT fk_convocation_tenant FOREIGN KEY (tenant_id) REFERENCES academie(id);
    END IF;

    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname='fk_document_tenant') THEN
        ALTER TABLE document ADD CONSTRAINT fk_document_tenant FOREIGN KEY (tenant_id) REFERENCES academie(id);
    END IF;

    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname='fk_notification_tenant') THEN
        ALTER TABLE notification ADD CONSTRAINT fk_notification_tenant FOREIGN KEY (tenant_id) REFERENCES academie(id);
    END IF;

    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname='fk_device_token_tenant') THEN
        ALTER TABLE device_token ADD CONSTRAINT fk_device_token_tenant FOREIGN KEY (tenant_id) REFERENCES academie(id);
    END IF;

    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname='fk_consentement_rgpd_tenant') THEN
        ALTER TABLE consentement_rgpd ADD CONSTRAINT fk_consentement_rgpd_tenant FOREIGN KEY (tenant_id) REFERENCES academie(id);
    END IF;

    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname='fk_audit_log_tenant') THEN
        ALTER TABLE audit_log ADD CONSTRAINT fk_audit_log_tenant FOREIGN KEY (tenant_id) REFERENCES academie(id);
    END IF;
END $$;

-- Step 5: Add indexes for tenant_id
CREATE INDEX IF NOT EXISTS idx_utilisateur_tenant ON utilisateur(tenant_id);
CREATE INDEX IF NOT EXISTS idx_categorie_tenant ON categorie(tenant_id);
CREATE INDEX IF NOT EXISTS idx_parent_tenant ON parent(tenant_id);
CREATE INDEX IF NOT EXISTS idx_joueur_tenant ON joueur(tenant_id);
CREATE INDEX IF NOT EXISTS idx_entraineur_tenant ON entraineur(tenant_id);
CREATE INDEX IF NOT EXISTS idx_creneau_tenant ON creneau(tenant_id);
CREATE INDEX IF NOT EXISTS idx_paiement_tenant ON paiement(tenant_id);
CREATE INDEX IF NOT EXISTS idx_absence_tenant ON absence(tenant_id);
CREATE INDEX IF NOT EXISTS idx_evenement_tenant ON evenement(tenant_id);
CREATE INDEX IF NOT EXISTS idx_convocation_tenant ON convocation(tenant_id);
CREATE INDEX IF NOT EXISTS idx_document_tenant ON document(tenant_id);
CREATE INDEX IF NOT EXISTS idx_notification_tenant ON notification(tenant_id);
CREATE INDEX IF NOT EXISTS idx_device_token_tenant ON device_token(tenant_id);
CREATE INDEX IF NOT EXISTS idx_consentement_rgpd_tenant ON consentement_rgpd(tenant_id);
CREATE INDEX IF NOT EXISTS idx_audit_log_tenant ON audit_log(tenant_id);

-- Step 6: Insert default tenant if not exists
INSERT INTO academie (id, slug, nom, ville, active, plan)
VALUES (1, 'nadi-default', 'Nadi Académie', 'Tunis', TRUE, 'PRO')
ON CONFLICT (slug) DO NOTHING;

-- Step 7: Update existing data to belong to tenant 1
UPDATE utilisateur SET tenant_id = 1 WHERE tenant_id IS NULL;
UPDATE categorie SET tenant_id = 1 WHERE tenant_id IS NULL;
UPDATE parent SET tenant_id = 1 WHERE tenant_id IS NULL;
UPDATE joueur SET tenant_id = 1 WHERE tenant_id IS NULL;
UPDATE entraineur SET tenant_id = 1 WHERE tenant_id IS NULL;
UPDATE creneau SET tenant_id = 1 WHERE tenant_id IS NULL;
UPDATE paiement SET tenant_id = 1 WHERE tenant_id IS NULL;
UPDATE absence SET tenant_id = 1 WHERE tenant_id IS NULL;
UPDATE evenement SET tenant_id = 1 WHERE tenant_id IS NULL;
UPDATE convocation SET tenant_id = 1 WHERE tenant_id IS NULL;
UPDATE document SET tenant_id = 1 WHERE tenant_id IS NULL;
UPDATE notification SET tenant_id = 1 WHERE tenant_id IS NULL;
UPDATE device_token SET tenant_id = 1 WHERE tenant_id IS NULL;
UPDATE consentement_rgpd SET tenant_id = 1 WHERE tenant_id IS NULL;
UPDATE audit_log SET tenant_id = 1 WHERE tenant_id IS NULL;

SELECT 'Migration completed successfully!' AS status;
