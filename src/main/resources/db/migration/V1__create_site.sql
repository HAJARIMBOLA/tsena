CREATE TABLE site (
    id BIGSERIAL PRIMARY KEY,
    nom VARCHAR(255) NOT NULL,
    localisation VARCHAR(255) NOT NULL,
    actif BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE INDEX idx_site_nom ON site (nom);
