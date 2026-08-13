-- Migration V2: Cash and Bank Module (Effective Module)
-- Resolution 340/2004 Compliance - Cuba

-- Table: bank_accounts
CREATE TABLE bank_accounts (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    account_number VARCHAR(50) NOT NULL UNIQUE,
    account_name VARCHAR(200) NOT NULL,
    bank_name VARCHAR(200),
    currency_code VARCHAR(3) NOT NULL DEFAULT 'CUP',
    account_type VARCHAR(50) NOT NULL, -- CHECKING, SAVINGS, OTHER
    accounting_account_code VARCHAR(20), -- Associated accounting account
    initial_balance DECIMAL(19,6) DEFAULT 0.00,
    current_balance DECIMAL(19,6) DEFAULT 0.00,
    status VARCHAR(20) DEFAULT 'ACTIVE', -- ACTIVE, CLOSED
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100)
);

-- Table: cash_boxes
CREATE TABLE cash_boxes (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    box_code VARCHAR(20) NOT NULL UNIQUE,
    box_name VARCHAR(200) NOT NULL,
    location VARCHAR(200),
    currency_code VARCHAR(3) NOT NULL DEFAULT 'CUP',
    accounting_account_code VARCHAR(20), -- Associated accounting account
    initial_balance DECIMAL(19,6) DEFAULT 0.00,
    current_balance DECIMAL(19,6) DEFAULT 0.00,
    status VARCHAR(20) DEFAULT 'OPEN', -- OPEN, CLOSED
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100)
);

-- Table: cash_movement_types
CREATE TABLE cash_movement_types (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    type_code VARCHAR(20) NOT NULL UNIQUE,
    type_name VARCHAR(100) NOT NULL,
    movement_type VARCHAR(20) NOT NULL, -- INFLOW, OUTFLOW, TRANSFER
    description TEXT,
    requires_accounting_entry BOOLEAN DEFAULT TRUE,
    is_active BOOLEAN DEFAULT TRUE
);

-- Table: cash_movements
CREATE TABLE cash_movements (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    movement_number VARCHAR(50) NOT NULL UNIQUE,
    movement_date DATE NOT NULL,
    movement_type_id BIGINT NOT NULL,
    source_type VARCHAR(20) NOT NULL, -- BANK_ACCOUNT, CASH_BOX
    source_id BIGINT NOT NULL,
    destination_type VARCHAR(20), -- BANK_ACCOUNT, CASH_BOX (for transfers)
    destination_id BIGINT,
    amount DECIMAL(19,6) NOT NULL,
    currency_code VARCHAR(3) NOT NULL,
    exchange_rate DECIMAL(19,6) DEFAULT 1.00,
    amount_in_base_currency DECIMAL(19,6) NOT NULL,
    description TEXT,
    document_number VARCHAR(100), -- Primary document number (required by resolution)
    counterparty VARCHAR(200), -- Client, supplier, or other
    accounting_account_code VARCHAR(20),
    cost_center_code VARCHAR(50),
    status VARCHAR(20) DEFAULT 'PENDING', -- PENDING, POSTED, CANCELLED
    is_posted BOOLEAN DEFAULT FALSE,
    posted_at TIMESTAMP,
    posted_by VARCHAR(100),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100),
    FOREIGN KEY (movement_type_id) REFERENCES cash_movement_types(id)
);

-- Table: bank_reconciliations
CREATE TABLE bank_reconciliations (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    bank_account_id BIGINT NOT NULL,
    reconciliation_date DATE NOT NULL,
    statement_date DATE,
    statement_number VARCHAR(50),
    opening_balance DECIMAL(19,6) NOT NULL,
    closing_balance_statement DECIMAL(19,6) NOT NULL,
    closing_balance_system DECIMAL(19,6) NOT NULL,
    difference DECIMAL(19,6) DEFAULT 0.00,
    reconciliation_method VARCHAR(50), -- ADJUSTED_BALANCE, DIRECT_METHOD
    status VARCHAR(20) DEFAULT 'IN_PROGRESS', -- IN_PROGRESS, COMPLETED
    notes TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100),
    FOREIGN KEY (bank_account_id) REFERENCES bank_accounts(id)
);

-- Table: reconciliation_items
CREATE TABLE reconciliation_items (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    reconciliation_id BIGINT NOT NULL,
    cash_movement_id BIGINT,
    item_type VARCHAR(50) NOT NULL, -- SYSTEM_ITEM, STATEMENT_ITEM
    item_date DATE,
    item_number VARCHAR(100),
    description TEXT,
    amount DECIMAL(19,6) NOT NULL,
    is_reconciled BOOLEAN DEFAULT FALSE,
    reconciliation_difference DECIMAL(19,6) DEFAULT 0.00,
    notes TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    FOREIGN KEY (reconciliation_id) REFERENCES bank_reconciliations(id),
    FOREIGN KEY (cash_movement_id) REFERENCES cash_movements(id)
);

-- Table: cash_flow_projections
CREATE TABLE cash_flow_projections (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    projection_date DATE NOT NULL,
    category VARCHAR(100) NOT NULL, -- OPERATING, INVESTING, FINANCING
    subcategory VARCHAR(100),
    description TEXT,
    expected_amount DECIMAL(19,6) NOT NULL,
    actual_amount DECIMAL(19,6) DEFAULT 0.00,
    currency_code VARCHAR(3) NOT NULL,
    is_realized BOOLEAN DEFAULT FALSE,
    related_movement_id BIGINT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100),
    FOREIGN KEY (related_movement_id) REFERENCES cash_movements(id)
);

-- Indexes for performance
CREATE INDEX idx_bank_accounts_status ON bank_accounts(status);
CREATE INDEX idx_cash_boxes_status ON cash_boxes(status);
CREATE INDEX idx_cash_movements_date ON cash_movements(movement_date);
CREATE INDEX idx_cash_movements_status ON cash_movements(status);
CREATE INDEX idx_cash_movements_source ON cash_movements(source_type, source_id);
CREATE INDEX idx_bank_reconciliations_date ON bank_reconciliations(reconciliation_date);
CREATE INDEX idx_cash_flow_projection_date ON cash_flow_projections(projection_date);

-- Initial data for movement types
INSERT INTO cash_movement_types (type_code, type_name, movement_type, description, requires_accounting_entry, is_active) VALUES
('DEPOSIT', 'Deposit', 'INFLOW', 'Cash or check deposit to bank account', TRUE, TRUE),
('WITHDRAWAL', 'Withdrawal', 'OUTFLOW', 'Cash withdrawal from bank account', TRUE, TRUE),
('TRANSFER_IN', 'Transfer In', 'INFLOW', 'Transfer received from another account', TRUE, TRUE),
('TRANSFER_OUT', 'Transfer Out', 'OUTFLOW', 'Transfer sent to another account', TRUE, TRUE),
('PAYMENT', 'Payment', 'OUTFLOW', 'Payment to supplier or third party', TRUE, TRUE),
('RECEIPT', 'Receipt', 'INFLOW', 'Receipt from customer or third party', TRUE, TRUE),
('ADJUSTMENT_IN', 'Adjustment In', 'INFLOW', 'Positive adjustment', TRUE, TRUE),
('ADJUSTMENT_OUT', 'Adjustment Out', 'OUTFLOW', 'Negative adjustment', TRUE, TRUE);
