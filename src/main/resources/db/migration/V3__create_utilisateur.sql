CREATE TABLE utilisateur (
    id BIGSERIAL PRIMARY KEY,
    nom VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL,
    mot_de_passe VARCHAR(255) NOT NULL,
    role VARCHAR(20) NOT NULL CHECK (role IN ('ADMIN', 'EMPLOYE')),
    actif BOOLEAN NOT NULL DEFAULT TRUE,
    CONSTRAINT uk_utilisateur_email UNIQUE (email)
);
