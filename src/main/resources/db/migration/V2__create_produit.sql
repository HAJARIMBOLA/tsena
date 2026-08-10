CREATE TABLE produit (
    id BIGSERIAL PRIMARY KEY,
    nom VARCHAR(255) NOT NULL,
    categorie VARCHAR(255) NOT NULL,
    unite VARCHAR(20) NOT NULL CHECK (unite IN ('KG', 'TONNE', 'SAC')),
    prix_unitaire NUMERIC(19, 2) NOT NULL CHECK (prix_unitaire > 0),
    actif BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE INDEX idx_produit_nom ON produit (nom);
CREATE INDEX idx_produit_categorie ON produit (categorie);
