-- Creación de la tabla de empresas (Master Database)
-- Esta tabla se almacena en la base de datos maestra y gestiona el acceso a las BDs por empresa

CREATE TABLE IF NOT EXISTS companies (
    id BIGINT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    code VARCHAR(50) UNIQUE NOT NULL,
    nif VARCHAR(50) UNIQUE,
    address VARCHAR(500),
    phone VARCHAR(50),
    email VARCHAR(255),
    database_url VARCHAR(500) NOT NULL,
    database_user VARCHAR(100),
    status VARCHAR(20) DEFAULT 'ACTIVE',
    version BIGINT DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Insertar empresa por defecto si no existe
INSERT INTO companies (id, name, code, nif, database_url, status)
SELECT 1, 'Empresa Demo', 'DEMO', '000000000', 'jdbc:h2:./db/econova_demo', 'ACTIVE'
WHERE NOT EXISTS (SELECT 1 FROM companies WHERE code = 'DEMO');
