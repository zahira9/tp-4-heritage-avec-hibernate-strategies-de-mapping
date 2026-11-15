-- Version 2.0 : enrichissement des salles et index unique de recherche utilisateur.
ALTER TABLE salles ADD COLUMN IF NOT EXISTS description VARCHAR(255);
CREATE UNIQUE INDEX IF NOT EXISTS idx_utilisateur_email ON utilisateurs(email);
