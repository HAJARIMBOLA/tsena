CREATE TABLE utilisateur_site (
    utilisateur_id BIGINT NOT NULL REFERENCES utilisateur (id) ON DELETE CASCADE,
    site_id BIGINT NOT NULL REFERENCES site (id) ON DELETE CASCADE,
    PRIMARY KEY (utilisateur_id, site_id)
);

CREATE INDEX idx_utilisateur_site_site_id ON utilisateur_site (site_id);
