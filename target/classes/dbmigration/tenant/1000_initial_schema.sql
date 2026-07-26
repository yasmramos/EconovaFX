-- Migración inicial para base de datos de empresa (tenant)
-- Este script se ejecuta automáticamente al crear una nueva empresa

-- Tabla de usuarios
CREATE TABLE IF NOT EXISTS users (
    id BIGINT PRIMARY KEY,
    username VARCHAR(100) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    full_name VARCHAR(255),
    email VARCHAR(255),
    role VARCHAR(50) NOT NULL,
    status VARCHAR(20) DEFAULT 'ACTIVE',
    version BIGINT DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Usuario administrador por defecto (password: admin123 - BCrypt)
INSERT INTO users (id, username, password_hash, full_name, role, status)
SELECT 1, 'admin', '$2a$10$YgU9X8hKqQZ5V5J5V5J5V5J5V5J5V5J5V5J5V5J5V5J5V5J5V5J', 'Administrador', 'ADMIN', 'ACTIVE'
WHERE NOT EXISTS (SELECT 1 FROM users WHERE username = 'admin');

-- El resto de tablas se crean automáticamente vía Ebean DDL Generation
-- configurado en DatabaseConfig para cada tenant
