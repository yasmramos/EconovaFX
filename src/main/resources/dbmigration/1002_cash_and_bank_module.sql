-- 1002_cash_and_bank_module.sql
-- Cash and Bank Module (Effective Module)
-- Resolution 340/2004 Compliance - Cuba

-- Table: bank_accounts
CREATE TABLE IF NOT EXISTS bank_accounts (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    account_number VARCHAR(50) NOT NULL UNIQUE,
    account_name VARCHAR(200) NOT NULL,
    bank_name VARCHAR(200),
    currency_code VARCHAR(3) NOT NULL DEFAULT 'CUP',
    account_type VARCHAR(50) NOT NULL,
    accounting_account_code VARCHAR(20),
    initial_balance DECIMAL(19,6) DEFAULT 0.00,
    current_balance DECIMAL(19,6) DEFAULT 0.00,
    status VARCHAR(20) DEFAULT 'ACTIVE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100)
);

-- Table: cash_boxes
CREATE TABLE IF NOT EXISTS cash_boxes (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    box_code VARCHAR(20) NOT NULL UNIQUE,
    box_name VARCHAR(200) NOT NULL,
    location VARCHAR(200),
    currency_code VARCHAR(3) NOT NULL DEFAULT 'CUP',
    accounting_account_code VARCHAR(20),
    initial_balance DECIMAL(19,6) DEFAULT 0.00,
    current_balance DECIMAL(19,6) DEFAULT 0.00,
    status VARCHAR(20) DEFAULT 'OPEN',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100)
);

-- Table: cash_movement_types
CREATE TABLE IF NOT EXISTS cash_movement_types (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    type_code VARCHAR(20) NOT NULL UNIQUE,
    type_name VARCHAR(100) NOT NULL,
    movement_type VARCHAR(20) NOT NULL,
    description TEXT,
    requires_accounting_entry BOOLEAN DEFAULT TRUE,
    is_active BOOLEAN DEFAULT TRUE
);

-- Table: cash_movements
CREATE TABLE IF NOT EXISTS cash_movements (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    movement_type_id BIGINT NOT NULL,
    cash_box_id BIGINT,
    bank_account_id BIGINT,
    amount DECIMAL(19,6) NOT NULL,
    currency_code VARCHAR(3) NOT NULL,
    description TEXT NOT NULL,
    movement_date DATE NOT NULL,
    reference_number VARCHAR(50),
    transaction_id BIGINT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    FOREIGN KEY (movement_type_id) REFERENCES cash_movement_types(id),
    FOREIGN KEY (cash_box_id) REFERENCES cash_boxes(id),
    FOREIGN KEY (bank_account_id) REFERENCES bank_accounts(id),
    FOREIGN KEY (transaction_id) REFERENCES transaction(id)
);

-- Create indexes
CREATE INDEX IF NOT EXISTS idx_bank_accounts_status ON bank_accounts(status);
CREATE INDEX IF NOT EXISTS idx_cash_boxes_status ON cash_boxes(status);
CREATE INDEX IF NOT EXISTS idx_cash_movements_date ON cash_movements(movement_date);
