CREATE TABLE stock_site (
    id BIGSERIAL PRIMARY KEY,
    site_id BIGINT NOT NULL REFERENCES site (id) ON DELETE RESTRICT,
    produit_id BIGINT NOT NULL REFERENCES produit (id) ON DELETE RESTRICT,
    quantite_disponible NUMERIC(19, 3) NOT NULL CHECK (quantite_disponible >= 0),
    CONSTRAINT uk_stock_site_site_produit UNIQUE (site_id, produit_id)
);

CREATE INDEX idx_stock_site_produit_id ON stock_site (produit_id);
