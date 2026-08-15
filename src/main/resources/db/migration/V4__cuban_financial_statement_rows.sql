-- V4__cuban_financial_statement_rows.sql
-- Cuban Financial Statement Rows Configuration
-- Resolution 340/2004 Compliance - MC.8: Estados Financieros

-- Insert rows for Balance General (BS-001)
-- Activo Circulante section
INSERT INTO financial_statement_row (model_id, row_number, label, parent_row_id, row_type, account_codes_pattern, sign_multiplier, is_bold, is_italic, indent_level)
SELECT fm.id, 1, 'ACTIVO CIRCULANTE', NULL, 'HEADER', NULL, 1, TRUE, FALSE, 0
FROM financial_statement_model fm WHERE fm.code = 'BS-001';

INSERT INTO financial_statement_row (model_id, row_number, label, parent_row_id, row_type, account_codes_pattern, sign_multiplier, is_bold, is_italic, indent_level)
SELECT fm.id, 2, 'Efectivo y Equivalentes', NULL, 'DATA', '1.01.*,1.02.*', 1, FALSE, FALSE, 1
FROM financial_statement_model fm WHERE fm.code = 'BS-001';

INSERT INTO financial_statement_row (model_id, row_number, label, parent_row_id, row_type, account_codes_pattern, sign_multiplier, is_bold, is_italic, indent_level)
SELECT fm.id, 3, 'Cuentas por Cobrar', NULL, 'DATA', '1.03.*,1.04.*', 1, FALSE, FALSE, 1
FROM financial_statement_model fm WHERE fm.code = 'BS-001';

INSERT INTO financial_statement_row (model_id, row_number, label, parent_row_id, row_type, account_codes_pattern, sign_multiplier, is_bold, is_italic, indent_level)
SELECT fm.id, 4, 'Inventarios', NULL, 'DATA', '1.05.*', 1, FALSE, FALSE, 1
FROM financial_statement_model fm WHERE fm.code = 'BS-001';

INSERT INTO financial_statement_row (model_id, row_number, label, parent_row_id, row_type, account_codes_pattern, sign_multiplier, is_bold, is_italic, indent_level)
SELECT fm.id, 5, 'Total Activo Circulante', NULL, 'SUBTOTAL', NULL, 1, TRUE, FALSE, 0
FROM financial_statement_model fm WHERE fm.code = 'BS-001';

-- Activo No Circulante section
INSERT INTO financial_statement_row (model_id, row_number, label, parent_row_id, row_type, account_codes_pattern, sign_multiplier, is_bold, is_italic, indent_level)
SELECT fm.id, 6, 'ACTIVO NO CIRCULANTE', NULL, 'HEADER', NULL, 1, TRUE, FALSE, 0
FROM financial_statement_model fm WHERE fm.code = 'BS-001';

INSERT INTO financial_statement_row (model_id, row_number, label, parent_row_id, row_type, account_codes_pattern, sign_multiplier, is_bold, is_italic, indent_level)
SELECT fm.id, 7, 'Propiedades, Planta y Equipo', NULL, 'DATA', '1.06.*', 1, FALSE, FALSE, 1
FROM financial_statement_model fm WHERE fm.code = 'BS-001';

INSERT INTO financial_statement_row (model_id, row_number, label, parent_row_id, row_type, account_codes_pattern, sign_multiplier, is_bold, is_italic, indent_level)
SELECT fm.id, 8, 'Activos Intangibles', NULL, 'DATA', '1.07.*', 1, FALSE, FALSE, 1
FROM financial_statement_model fm WHERE fm.code = 'BS-001';

INSERT INTO financial_statement_row (model_id, row_number, label, parent_row_id, row_type, account_codes_pattern, sign_multiplier, is_bold, is_italic, indent_level)
SELECT fm.id, 9, 'Total Activo No Circulante', NULL, 'SUBTOTAL', NULL, 1, TRUE, FALSE, 0
FROM financial_statement_model fm WHERE fm.code = 'BS-001';

-- Total Activo
INSERT INTO financial_statement_row (model_id, row_number, label, parent_row_id, row_type, account_codes_pattern, sign_multiplier, is_bold, is_italic, indent_level)
SELECT fm.id, 10, 'TOTAL ACTIVO', NULL, 'TOTAL', NULL, 1, TRUE, TRUE, 0
FROM financial_statement_model fm WHERE fm.code = 'BS-001';

-- Pasivo Circulante section
INSERT INTO financial_statement_row (model_id, row_number, label, parent_row_id, row_type, account_codes_pattern, sign_multiplier, is_bold, is_italic, indent_level)
SELECT fm.id, 11, 'PASIVO CIRCULANTE', NULL, 'HEADER', NULL, 1, TRUE, FALSE, 0
FROM financial_statement_model fm WHERE fm.code = 'BS-001';

INSERT INTO financial_statement_row (model_id, row_number, label, parent_row_id, row_type, account_codes_pattern, sign_multiplier, is_bold, is_italic, indent_level)
SELECT fm.id, 12, 'Cuentas por Pagar', NULL, 'DATA', '2.01.*', -1, FALSE, FALSE, 1
FROM financial_statement_model fm WHERE fm.code = 'BS-001';

INSERT INTO financial_statement_row (model_id, row_number, label, parent_row_id, row_type, account_codes_pattern, sign_multiplier, is_bold, is_italic, indent_level)
SELECT fm.id, 13, 'Obligaciones a Corto Plazo', NULL, 'DATA', '2.02.*', -1, FALSE, FALSE, 1
FROM financial_statement_model fm WHERE fm.code = 'BS-001';

INSERT INTO financial_statement_row (model_id, row_number, label, parent_row_id, row_type, account_codes_pattern, sign_multiplier, is_bold, is_italic, indent_level)
SELECT fm.id, 14, 'Total Pasivo Circulante', NULL, 'SUBTOTAL', NULL, -1, TRUE, FALSE, 0
FROM financial_statement_model fm WHERE fm.code = 'BS-001';

-- Pasivo No Circulante section
INSERT INTO financial_statement_row (model_id, row_number, label, parent_row_id, row_type, account_codes_pattern, sign_multiplier, is_bold, is_italic, indent_level)
SELECT fm.id, 15, 'PASIVO NO CIRCULANTE', NULL, 'HEADER', NULL, 1, TRUE, FALSE, 0
FROM financial_statement_model fm WHERE fm.code = 'BS-001';

INSERT INTO financial_statement_row (model_id, row_number, label, parent_row_id, row_type, account_codes_pattern, sign_multiplier, is_bold, is_italic, indent_level)
SELECT fm.id, 16, 'Obligaciones a Largo Plazo', NULL, 'DATA', '2.03.*', -1, FALSE, FALSE, 1
FROM financial_statement_model fm WHERE fm.code = 'BS-001';

INSERT INTO financial_statement_row (model_id, row_number, label, parent_row_id, row_type, account_codes_pattern, sign_multiplier, is_bold, is_italic, indent_level)
SELECT fm.id, 17, 'Total Pasivo No Circulante', NULL, 'SUBTOTAL', NULL, -1, TRUE, FALSE, 0
FROM financial_statement_model fm WHERE fm.code = 'BS-001';

-- Total Pasivo
INSERT INTO financial_statement_row (model_id, row_number, label, parent_row_id, row_type, account_codes_pattern, sign_multiplier, is_bold, is_italic, indent_level)
SELECT fm.id, 18, 'TOTAL PASIVO', NULL, 'TOTAL', NULL, -1, TRUE, TRUE, 0
FROM financial_statement_model fm WHERE fm.code = 'BS-001';

-- Patrimonio section
INSERT INTO financial_statement_row (model_id, row_number, label, parent_row_id, row_type, account_codes_pattern, sign_multiplier, is_bold, is_italic, indent_level)
SELECT fm.id, 19, 'PATRIMONIO', NULL, 'HEADER', NULL, 1, TRUE, FALSE, 0
FROM financial_statement_model fm WHERE fm.code = 'BS-001';

INSERT INTO financial_statement_row (model_id, row_number, label, parent_row_id, row_type, account_codes_pattern, sign_multiplier, is_bold, is_italic, indent_level)
SELECT fm.id, 20, 'Capital Social', NULL, 'DATA', '3.01.*', -1, FALSE, FALSE, 1
FROM financial_statement_model fm WHERE fm.code = 'BS-001';

INSERT INTO financial_statement_row (model_id, row_number, label, parent_row_id, row_type, account_codes_pattern, sign_multiplier, is_bold, is_italic, indent_level)
SELECT fm.id, 21, 'Resultados Acumulados', NULL, 'DATA', '3.02.*', -1, FALSE, FALSE, 1
FROM financial_statement_model fm WHERE fm.code = 'BS-001';

INSERT INTO financial_statement_row (model_id, row_number, label, parent_row_id, row_type, account_codes_pattern, sign_multiplier, is_bold, is_italic, indent_level)
SELECT fm.id, 22, 'Total Patrimonio', NULL, 'SUBTOTAL', NULL, -1, TRUE, FALSE, 0
FROM financial_statement_model fm WHERE fm.code = 'BS-001';

-- Total Pasivo + Patrimonio
INSERT INTO financial_statement_row (model_id, row_number, label, parent_row_id, row_type, account_codes_pattern, sign_multiplier, is_bold, is_italic, indent_level)
SELECT fm.id, 23, 'TOTAL PASIVO Y PATRIMONIO', NULL, 'TOTAL', NULL, -1, TRUE, TRUE, 0
FROM financial_statement_model fm WHERE fm.code = 'BS-001';

-- Insert rows for Estado de Resultados (IS-001)
INSERT INTO financial_statement_row (model_id, row_number, label, parent_row_id, row_type, account_codes_pattern, sign_multiplier, is_bold, is_italic, indent_level)
SELECT fm.id, 1, 'INGRESOS DE ACTIVIDADES ORDINARIAS', NULL, 'HEADER', NULL, 1, TRUE, FALSE, 0
FROM financial_statement_model fm WHERE fm.code = 'IS-001';

INSERT INTO financial_statement_row (model_id, row_number, label, parent_row_id, row_type, account_codes_pattern, sign_multiplier, is_bold, is_italic, indent_level)
SELECT fm.id, 2, 'Ventas de Bienes', NULL, 'DATA', '4.01.*', -1, FALSE, FALSE, 1
FROM financial_statement_model fm WHERE fm.code = 'IS-001';

INSERT INTO financial_statement_row (model_id, row_number, label, parent_row_id, row_type, account_codes_pattern, sign_multiplier, is_bold, is_italic, indent_level)
SELECT fm.id, 3, 'Prestación de Servicios', NULL, 'DATA', '4.02.*', -1, FALSE, FALSE, 1
FROM financial_statement_model fm WHERE fm.code = 'IS-001';

INSERT INTO financial_statement_row (model_id, row_number, label, parent_row_id, row_type, account_codes_pattern, sign_multiplier, is_bold, is_italic, indent_level)
SELECT fm.id, 4, 'Total Ingresos', NULL, 'SUBTOTAL', NULL, -1, TRUE, FALSE, 0
FROM financial_statement_model fm WHERE fm.code = 'IS-001';

INSERT INTO financial_statement_row (model_id, row_number, label, parent_row_id, row_type, account_codes_pattern, sign_multiplier, is_bold, is_italic, indent_level)
SELECT fm.id, 5, 'COSTO DE VENTAS', NULL, 'HEADER', NULL, 1, TRUE, FALSE, 0
FROM financial_statement_model fm WHERE fm.code = 'IS-001';

INSERT INTO financial_statement_row (model_id, row_number, label, parent_row_id, row_type, account_codes_pattern, sign_multiplier, is_bold, is_italic, indent_level)
SELECT fm.id, 6, 'Costo de Mercancías Vendidas', NULL, 'DATA', '5.01.*', 1, FALSE, FALSE, 1
FROM financial_statement_model fm WHERE fm.code = 'IS-001';

INSERT INTO financial_statement_row (model_id, row_number, label, parent_row_id, row_type, account_codes_pattern, sign_multiplier, is_bold, is_italic, indent_level)
SELECT fm.id, 7, 'Utilidad Bruta', NULL, 'SUBTOTAL', NULL, 1, TRUE, FALSE, 0
FROM financial_statement_model fm WHERE fm.code = 'IS-001';

INSERT INTO financial_statement_row (model_id, row_number, label, parent_row_id, row_type, account_codes_pattern, sign_multiplier, is_bold, is_italic, indent_level)
SELECT fm.id, 8, 'GASTOS DE OPERACIÓN', NULL, 'HEADER', NULL, 1, TRUE, FALSE, 0
FROM financial_statement_model fm WHERE fm.code = 'IS-001';

INSERT INTO financial_statement_row (model_id, row_number, label, parent_row_id, row_type, account_codes_pattern, sign_multiplier, is_bold, is_italic, indent_level)
SELECT fm.id, 9, 'Gastos de Administración', NULL, 'DATA', '5.02.*', 1, FALSE, FALSE, 1
FROM financial_statement_model fm WHERE fm.code = 'IS-001';

INSERT INTO financial_statement_row (model_id, row_number, label, parent_row_id, row_type, account_codes_pattern, sign_multiplier, is_bold, is_italic, indent_level)
SELECT fm.id, 10, 'Gastos de Ventas', NULL, 'DATA', '5.03.*', 1, FALSE, FALSE, 1
FROM financial_statement_model fm WHERE fm.code = 'IS-001';

INSERT INTO financial_statement_row (model_id, row_number, label, parent_row_id, row_type, account_codes_pattern, sign_multiplier, is_bold, is_italic, indent_level)
SELECT fm.id, 11, 'Utilidad en Operaciones', NULL, 'SUBTOTAL', NULL, 1, TRUE, FALSE, 0
FROM financial_statement_model fm WHERE fm.code = 'IS-001';

INSERT INTO financial_statement_row (model_id, row_number, label, parent_row_id, row_type, account_codes_pattern, sign_multiplier, is_bold, is_italic, indent_level)
SELECT fm.id, 12, 'RESULTADO NETO DEL EJERCICIO', NULL, 'TOTAL', NULL, 1, TRUE, TRUE, 0
FROM financial_statement_model fm WHERE fm.code = 'IS-001';

-- Insert rows for Estado de Flujos de Efectivo (CF-001)
-- Note: Full cash flow classification requires transaction-level tagging.
-- This provides the structure; actual implementation may need enhancement.
INSERT INTO financial_statement_row (model_id, row_number, label, parent_row_id, row_type, account_codes_pattern, sign_multiplier, is_bold, is_italic, indent_level)
SELECT fm.id, 1, 'FLUJOS DE EFECTIVO DE ACTIVIDADES DE OPERACIÓN', NULL, 'HEADER', NULL, 1, TRUE, FALSE, 0
FROM financial_statement_model fm WHERE fm.code = 'CF-001';

INSERT INTO financial_statement_row (model_id, row_number, label, parent_row_id, row_type, account_codes_pattern, sign_multiplier, is_bold, is_italic, indent_level)
SELECT fm.id, 2, 'Cobros de Clientes', NULL, 'DATA', '1.01.*,4.01.*', 1, FALSE, FALSE, 1
FROM financial_statement_model fm WHERE fm.code = 'CF-001';

INSERT INTO financial_statement_row (model_id, row_number, label, parent_row_id, row_type, account_codes_pattern, sign_multiplier, is_bold, is_italic, indent_level)
SELECT fm.id, 3, 'Pagos a Proveedores', NULL, 'DATA', '2.01.*,5.01.*', -1, FALSE, FALSE, 1
FROM financial_statement_model fm WHERE fm.code = 'CF-001';

INSERT INTO financial_statement_row (model_id, row_number, label, parent_row_id, row_type, account_codes_pattern, sign_multiplier, is_bold, is_italic, indent_level)
SELECT fm.id, 4, 'Pago de Gastos de Operación', NULL, 'DATA', '5.02.*,5.03.*', -1, FALSE, FALSE, 1
FROM financial_statement_model fm WHERE fm.code = 'CF-001';

INSERT INTO financial_statement_row (model_id, row_number, label, parent_row_id, row_type, account_codes_pattern, sign_multiplier, is_bold, is_italic, indent_level)
SELECT fm.id, 5, 'Flujo Neto de Actividades de Operación', NULL, 'SUBTOTAL', NULL, 1, TRUE, FALSE, 0
FROM financial_statement_model fm WHERE fm.code = 'CF-001';

INSERT INTO financial_statement_row (model_id, row_number, label, parent_row_id, row_type, account_codes_pattern, sign_multiplier, is_bold, is_italic, indent_level)
SELECT fm.id, 6, 'FLUJOS DE EFECTIVO DE ACTIVIDADES DE INVERSIÓN', NULL, 'HEADER', NULL, 1, TRUE, FALSE, 0
FROM financial_statement_model fm WHERE fm.code = 'CF-001';

INSERT INTO financial_statement_row (model_id, row_number, label, parent_row_id, row_type, account_codes_pattern, sign_multiplier, is_bold, is_italic, indent_level)
SELECT fm.id, 7, 'Compra de Propiedades, Planta y Equipo', NULL, 'DATA', '1.06.*', -1, FALSE, FALSE, 1
FROM financial_statement_model fm WHERE fm.code = 'CF-001';

INSERT INTO financial_statement_row (model_id, row_number, label, parent_row_id, row_type, account_codes_pattern, sign_multiplier, is_bold, is_italic, indent_level)
SELECT fm.id, 8, 'Venta de Activos Fijos', NULL, 'DATA', '1.06.*,4.03.*', 1, FALSE, FALSE, 1
FROM financial_statement_model fm WHERE fm.code = 'CF-001';

INSERT INTO financial_statement_row (model_id, row_number, label, parent_row_id, row_type, account_codes_pattern, sign_multiplier, is_bold, is_italic, indent_level)
SELECT fm.id, 9, 'Flujo Neto de Actividades de Inversión', NULL, 'SUBTOTAL', NULL, 1, TRUE, FALSE, 0
FROM financial_statement_model fm WHERE fm.code = 'CF-001';

INSERT INTO financial_statement_row (model_id, row_number, label, parent_row_id, row_type, account_codes_pattern, sign_multiplier, is_bold, is_italic, indent_level)
SELECT fm.id, 10, 'FLUJOS DE EFECTIVO DE ACTIVIDADES DE FINANCIAMIENTO', NULL, 'HEADER', NULL, 1, TRUE, FALSE, 0
FROM financial_statement_model fm WHERE fm.code = 'CF-001';

INSERT INTO financial_statement_row (model_id, row_number, label, parent_row_id, row_type, account_codes_pattern, sign_multiplier, is_bold, is_italic, indent_level)
SELECT fm.id, 11, 'Préstamos Recibidos', NULL, 'DATA', '2.03.*', 1, FALSE, FALSE, 1
FROM financial_statement_model fm WHERE fm.code = 'CF-001';

INSERT INTO financial_statement_row (model_id, row_number, label, parent_row_id, row_type, account_codes_pattern, sign_multiplier, is_bold, is_italic, indent_level)
SELECT fm.id, 12, 'Pago de Préstamos', NULL, 'DATA', '2.03.*', -1, FALSE, FALSE, 1
FROM financial_statement_model fm WHERE fm.code = 'CF-001';

INSERT INTO financial_statement_row (model_id, row_number, label, parent_row_id, row_type, account_codes_pattern, sign_multiplier, is_bold, is_italic, indent_level)
SELECT fm.id, 13, 'Aportes de Capital', NULL, 'DATA', '3.01.*', 1, FALSE, FALSE, 1
FROM financial_statement_model fm WHERE fm.code = 'CF-001';

INSERT INTO financial_statement_row (model_id, row_number, label, parent_row_id, row_type, account_codes_pattern, sign_multiplier, is_bold, is_italic, indent_level)
SELECT fm.id, 14, 'Flujo Neto de Actividades de Financiamiento', NULL, 'SUBTOTAL', NULL, 1, TRUE, FALSE, 0
FROM financial_statement_model fm WHERE fm.code = 'CF-001';

INSERT INTO financial_statement_row (model_id, row_number, label, parent_row_id, row_type, account_codes_pattern, sign_multiplier, is_bold, is_italic, indent_level)
SELECT fm.id, 15, 'INCREMENTO NETO EN EFECTIVO Y EQUIVALENTES', NULL, 'TOTAL', NULL, 1, TRUE, TRUE, 0
FROM financial_statement_model fm WHERE fm.code = 'CF-001';
