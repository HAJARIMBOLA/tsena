ALTER TABLE stock_site
    ADD COLUMN prix_unitaire NUMERIC(19, 2);

UPDATE stock_site
SET prix_unitaire = produit.prix_unitaire
FROM produit
WHERE stock_site.produit_id = produit.id;

ALTER TABLE stock_site
    ALTER COLUMN prix_unitaire SET NOT NULL,
    ADD CHECK (prix_unitaire > 0);
