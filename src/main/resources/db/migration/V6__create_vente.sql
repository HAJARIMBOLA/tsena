CREATE TABLE vente (
    id BIGSERIAL PRIMARY KEY,
    site_id BIGINT NOT NULL REFERENCES site (id) ON DELETE RESTRICT,
    produit_id BIGINT NOT NULL REFERENCES produit (id) ON DELETE RESTRICT,
    utilisateur_id BIGINT NOT NULL REFERENCES utilisateur (id) ON DELETE RESTRICT,
    quantite NUMERIC(19, 3) NOT NULL CHECK (quantite > 0),
    montant_total NUMERIC(19, 2) NOT NULL CHECK (montant_total > 0),
    date_vente TIMESTAMP NOT NULL
);

CREATE INDEX idx_vente_produit_id ON vente (produit_id);
CREATE INDEX idx_vente_utilisateur_id ON vente (utilisateur_id);
CREATE INDEX idx_vente_site_date ON vente (site_id, date_vente);
