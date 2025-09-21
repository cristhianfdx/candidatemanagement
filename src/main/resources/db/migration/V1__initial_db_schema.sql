-- V1__initial_db_schema.sql

DROP TABLE IF EXISTS `candidates`;

CREATE TABLE IF NOT EXISTS `candidates` (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    firstname VARCHAR(100) NOT NULL,
    lastname VARCHAR(100) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    age INT NOT NULL,
    birth_date DATE NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

INSERT INTO `candidates` (firstname, lastname, email, age, birth_date)
VALUES
('Juan', 'Pérez', 'juanp@mail.com' ,30, '1995-01-15'),
('María', 'Gómez', 'mg@mail.com', 25, '1998-06-20');