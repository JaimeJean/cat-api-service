-- V1__create_breeds_and_images.sql

CREATE TABLE breeds (
    id            BIGSERIAL    PRIMARY KEY,
    external_id   VARCHAR(10)  NOT NULL UNIQUE,
    name          VARCHAR(100) NOT NULL,
    origin        VARCHAR(100),
    temperament   VARCHAR(255),
    description   TEXT,
    created_at    TIMESTAMP    NOT NULL DEFAULT now(),
    updated_at    TIMESTAMP    NOT NULL DEFAULT now()
);

CREATE TABLE images (
    id            BIGSERIAL    PRIMARY KEY,
    external_id   VARCHAR(20)  NOT NULL UNIQUE,
    url           VARCHAR(500) NOT NULL,
    breed_id      BIGINT       REFERENCES breeds(id) ON DELETE SET NULL,
    category      VARCHAR(20)  NOT NULL CHECK (category IN ('BREED', 'HAT', 'GLASSES')),
    created_at    TIMESTAMP    NOT NULL DEFAULT now()
);

-- Índices para as consultas paginadas
CREATE INDEX idx_breeds_temperament ON breeds (temperament);
CREATE INDEX idx_breeds_origin      ON breeds (origin);
CREATE INDEX idx_images_breed_id    ON images (breed_id);
CREATE INDEX idx_images_category    ON images (category);