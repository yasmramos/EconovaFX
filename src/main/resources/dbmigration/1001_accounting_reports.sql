-- 1001_accounting_reports.sql
-- Accounting Reports and Financial Statements Module
-- Resolution 340/2004 Compliance
-- This migration creates tables for financial statement models, rows, and report definitions

-- Table for financial statement models (configurable templates)
CREATE TABLE IF NOT EXISTS financial_statement_model (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    code VARCHAR(20) NOT NULL UNIQUE,
    name VARCHAR(100) NOT NULL,
    model_type VARCHAR(20) NOT NULL CHECK (model_type IN ('BALANCE_SHEET', 'INCOME_STATEMENT', 'CASH_FLOW')),
    description TEXT,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(50),
    updated_by VARCHAR(50)
);

-- Table for financial statement rows configuration
CREATE TABLE IF NOT EXISTS financial_statement_row (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    model_id BIGINT NOT NULL,
    row_number INT NOT NULL,
    label VARCHAR(200) NOT NULL,
    parent_row_id BIGINT,
    row_type VARCHAR(20) NOT NULL CHECK (row_type IN ('HEADER', 'SUBTOTAL', 'TOTAL', 'DATA')),
    account_codes_pattern VARCHAR(500),
    sign_multiplier INT DEFAULT 1 CHECK (sign_multiplier IN (1, -1)),
    is_bold BOOLEAN DEFAULT FALSE,
    is_italic BOOLEAN DEFAULT FALSE,
    indent_level INT DEFAULT 0,
    FOREIGN KEY (model_id) REFERENCES financial_statement_model(id) ON DELETE CASCADE,
    FOREIGN KEY (parent_row_id) REFERENCES financial_statement_row(id) ON DELETE SET NULL
);

-- Table for report definitions (saved report configurations)
CREATE TABLE IF NOT EXISTS report_definition (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    code VARCHAR(20) NOT NULL UNIQUE,
    name VARCHAR(100) NOT NULL,
    report_type VARCHAR(50) NOT NULL CHECK (report_type IN ('TRIAL_BALANCE', 'GENERAL_LEDGER', 'VOUCHER_HISTORY', 'FINANCIAL_STATEMENT')),
    parameters_json TEXT,
    default_filters_json TEXT,
    is_public BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(50),
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(50)
);

-- Table for historical accounting periods (archived data)
CREATE TABLE IF NOT EXISTS accounting_period_archive (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    period_year INT NOT NULL,
    period_month INT NOT NULL,
    account_code VARCHAR(20) NOT NULL,
    initial_balance DECIMAL(18,2) DEFAULT 0,
    debit_total DECIMAL(18,2) DEFAULT 0,
    credit_total DECIMAL(18,2) DEFAULT 0,
    final_balance DECIMAL(18,2) DEFAULT 0,
    currency_code VARCHAR(3) DEFAULT 'CUP',
    archived_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY unique_period_account (period_year, period_month, account_code)
);

-- Add columns to accounting_period for module closure validation
ALTER TABLE accounting_period
ADD COLUMN IF NOT EXISTS cash_module_closed BOOLEAN DEFAULT FALSE,
ADD COLUMN IF NOT EXISTS inventory_module_closed BOOLEAN DEFAULT FALSE,
ADD COLUMN IF NOT EXISTS payables_receivables_closed BOOLEAN DEFAULT FALSE,
ADD COLUMN IF NOT EXISTS fixed_assets_closed BOOLEAN DEFAULT FALSE,
ADD COLUMN IF NOT EXISTS payroll_closed BOOLEAN DEFAULT FALSE;

-- Insert default financial statement models (idempotent using INSERT IGNORE or conditional logic)
INSERT INTO financial_statement_model (code, name, model_type, description) VALUES
('BS-001', 'Balance General Estándar', 'BALANCE_SHEET', 'Modelo estándar según normas contables cubanas'),
('IS-001', 'Estado de Resultados', 'INCOME_STATEMENT', 'Modelo estándar de ganancias y pérdidas'),
('CF-001', 'Estado de Flujos de Efectivo', 'CASH_FLOW', 'Flujo de efectivo método directo')
ON DUPLICATE KEY UPDATE name=name;

-- Insert default report definitions
INSERT INTO report_definition (code, name, report_type, default_filters_json) VALUES
('TB-001', 'Balance de Comprobación', 'TRIAL_BALANCE', '{"includeZeroBalances": false, "showSubaccounts": true}'),
('GL-001', 'Libro Mayor', 'GENERAL_LEDGER', '{"showDetails": true, "groupByAccount": true}'),
('VH-001', 'Fichero Histórico de Comprobantes', 'VOUCHER_HISTORY', '{"includeReversed": false, "yearsBack": 3}')
ON DUPLICATE KEY UPDATE name=name;

-- Create indexes for performance
CREATE INDEX IF NOT EXISTS idx_financial_statement_row_model ON financial_statement_row(model_id);
CREATE INDEX IF NOT EXISTS idx_report_definition_type ON report_definition(report_type);
CREATE INDEX IF NOT EXISTS idx_period_archive_year_month ON accounting_period_archive(period_year, period_month);
