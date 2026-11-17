CREATE DATABASE IF NOT EXISTS pharmacy_db;
USE pharmacy_db;

CREATE TABLE IF NOT EXISTS medicaments (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    nom_commercial VARCHAR(150) NOT NULL,
    dci VARCHAR(150),
    dosage VARCHAR(50),
    forme VARCHAR(50),
    prix_unitaire DECIMAL(10,2) NOT NULL,
    sur_ordonnance BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE TABLE IF NOT EXISTS stocks_lots (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    medicament_id BIGINT NOT NULL,
    numero_lot VARCHAR(50) NOT NULL,
    quantite INT NOT NULL,
    date_peremption DATE NOT NULL,
    seuil_alerte INT DEFAULT 10,
    CONSTRAINT fk_stock_med FOREIGN KEY (medicament_id) REFERENCES medicaments(id)
);

CREATE TABLE IF NOT EXISTS ordonnances (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    nom_prescripteur VARCHAR(100),
    date_prescription TIMESTAMP NOT NULL,
    statut VARCHAR(30) NOT NULL DEFAULT 'EN_ATTENTE'
);

CREATE TABLE IF NOT EXISTS ordonnance_medicaments (
    ordonnance_id BIGINT NOT NULL,
    medicament_id BIGINT NOT NULL,
    PRIMARY KEY (ordonnance_id, medicament_id),
    CONSTRAINT fk_ordonnance_medicaments_ordonnance
        FOREIGN KEY (ordonnance_id) REFERENCES ordonnances(id)
);

CREATE TABLE IF NOT EXISTS ventes (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    date_vente TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    ordonnance_id BIGINT NULL,
    total DECIMAL(10,2) NOT NULL
);

CREATE TABLE IF NOT EXISTS ligne_ventes (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    vente_id BIGINT NOT NULL,
    medicament_id BIGINT NOT NULL,
    quantite INT NOT NULL,
    prix_total DECIMAL(10,2) NOT NULL,
    CONSTRAINT fk_ligne_vente FOREIGN KEY (vente_id) REFERENCES ventes(id),
    CONSTRAINT fk_ligne_med FOREIGN KEY (medicament_id) REFERENCES medicaments(id)
);