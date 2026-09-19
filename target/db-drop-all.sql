-- drop all foreign keys
alter table accounts drop constraint if exists fk_accounts_parent_account_id;
drop index if exists ix_accounts_parent_account_id;

alter table business_units drop constraint if exists fk_business_units_company_id;
drop index if exists ix_business_units_company_id;

alter table closing_entries drop constraint if exists fk_closing_entries_transaction_id;
drop index if exists ix_closing_entries_transaction_id;

alter table customer_invoices drop constraint if exists fk_customer_invoices_customer_id;
drop index if exists ix_customer_invoices_customer_id;

alter table customer_invoices drop constraint if exists fk_customer_invoices_accounting_entry_id;

alter table customer_invoices drop constraint if exists fk_customer_invoices_original_invoice_id;
drop index if exists ix_customer_invoices_original_invoice_id;

alter table customer_invoices drop constraint if exists fk_customer_invoices_currency_id;
drop index if exists ix_customer_invoices_currency_id;

alter table customer_payments drop constraint if exists fk_customer_payments_customer_id;
drop index if exists ix_customer_payments_customer_id;

alter table customer_payments drop constraint if exists fk_customer_payments_accounting_entry_id;

alter table depreciation_records drop constraint if exists fk_depreciation_records_fixed_asset_id;
drop index if exists ix_depreciation_records_fixed_asset_id;

alter table payroll_employees drop constraint if exists fk_payroll_employees_third_party_id;

alter table exchange_differences drop constraint if exists fk_exchange_differences_third_party_id;
drop index if exists ix_exchange_differences_third_party_id;

alter table exchange_differences drop constraint if exists fk_exchange_differences_currency_id;
drop index if exists ix_exchange_differences_currency_id;

alter table exchange_rates drop constraint if exists fk_exchange_rates_from_currency_id;
drop index if exists ix_exchange_rates_from_currency_id;

alter table exchange_rates drop constraint if exists fk_exchange_rates_to_currency_id;
drop index if exists ix_exchange_rates_to_currency_id;

alter table exchange_rates drop constraint if exists fk_exchange_rates_created_by_user;
drop index if exists ix_exchange_rates_created_by_user;

alter table report_lines drop constraint if exists fk_report_lines_report_id;
drop index if exists ix_report_lines_report_id;

alter table financial_statement_row drop constraint if exists fk_financial_statement_row_model_id;
drop index if exists ix_financial_statement_row_model_id;

alter table financial_statement_row drop constraint if exists fk_financial_statement_row_parent_row_id;
drop index if exists ix_financial_statement_row_parent_row_id;

alter table fixed_assets drop constraint if exists fk_fixed_assets_account_id;
drop index if exists ix_fixed_assets_account_id;

alter table fixed_assets drop constraint if exists fk_fixed_assets_depreciation_account_id;
drop index if exists ix_fixed_assets_depreciation_account_id;

alter table inventory_categories drop constraint if exists fk_inventory_categories_parent_id;
drop index if exists ix_inventory_categories_parent_id;

alter table inventory_items drop constraint if exists fk_inventory_items_category_id;
drop index if exists ix_inventory_items_category_id;

alter table inventory_items drop constraint if exists fk_inventory_items_supplier_id;
drop index if exists ix_inventory_items_supplier_id;

alter table inventory_movements drop constraint if exists fk_inventory_movements_item_id;
drop index if exists ix_inventory_movements_item_id;

alter table inventory_movements drop constraint if exists fk_inventory_movements_warehouse_id;
drop index if exists ix_inventory_movements_warehouse_id;

alter table inventory_movements drop constraint if exists fk_inventory_movements_third_party_id;
drop index if exists ix_inventory_movements_third_party_id;

alter table inventory_movements drop constraint if exists fk_inventory_movements_transaction_id;
drop index if exists ix_inventory_movements_transaction_id;

alter table payroll_batches drop constraint if exists fk_payroll_batches_period_id;
drop index if exists ix_payroll_batches_period_id;

alter table payroll_concept_values drop constraint if exists fk_payroll_concept_values_detail_id;
drop index if exists ix_payroll_concept_values_detail_id;

alter table payroll_concept_values drop constraint if exists fk_payroll_concept_values_concept_id;
drop index if exists ix_payroll_concept_values_concept_id;

alter table payroll_details drop constraint if exists fk_payroll_details_batch_id;
drop index if exists ix_payroll_details_batch_id;

alter table payroll_details drop constraint if exists fk_payroll_details_employee_id;
drop index if exists ix_payroll_details_employee_id;

alter table reconciliation_item drop constraint if exists fk_reconciliation_item_reconciliation_id;
drop index if exists ix_reconciliation_item_reconciliation_id;

alter table role_permissions drop constraint if exists fk_role_permissions_role_id;
drop index if exists ix_role_permissions_role_id;

alter table sales_invoices drop constraint if exists fk_sales_invoices_billing_series_id;
drop index if exists ix_sales_invoices_billing_series_id;

alter table sales_invoices drop constraint if exists fk_sales_invoices_third_party_id;
drop index if exists ix_sales_invoices_third_party_id;

alter table sales_invoices drop constraint if exists fk_sales_invoices_currency_id;
drop index if exists ix_sales_invoices_currency_id;

alter table sales_invoice_lines drop constraint if exists fk_sales_invoice_lines_sales_invoice_id;
drop index if exists ix_sales_invoice_lines_sales_invoice_id;

alter table sales_invoice_lines drop constraint if exists fk_sales_invoice_lines_item_id;
drop index if exists ix_sales_invoice_lines_item_id;

alter table sales_invoice_lines drop constraint if exists fk_sales_invoice_lines_tax_rate_id;
drop index if exists ix_sales_invoice_lines_tax_rate_id;

alter table supplier_invoices drop constraint if exists fk_supplier_invoices_supplier_id;
drop index if exists ix_supplier_invoices_supplier_id;

alter table supplier_invoices drop constraint if exists fk_supplier_invoices_accounting_entry_id;

alter table supplier_invoices drop constraint if exists fk_supplier_invoices_original_invoice_id;
drop index if exists ix_supplier_invoices_original_invoice_id;

alter table supplier_invoices drop constraint if exists fk_supplier_invoices_currency_id;
drop index if exists ix_supplier_invoices_currency_id;

alter table supplier_payments drop constraint if exists fk_supplier_payments_supplier_id;
drop index if exists ix_supplier_payments_supplier_id;

alter table supplier_payments drop constraint if exists fk_supplier_payments_accounting_entry_id;

alter table supplier_payments drop constraint if exists fk_supplier_payments_currency_id;
drop index if exists ix_supplier_payments_currency_id;

alter table third_parties drop constraint if exists fk_third_parties_account_id;
drop index if exists ix_third_parties_account_id;

alter table transactions drop constraint if exists fk_transactions_third_party_id;
drop index if exists ix_transactions_third_party_id;

alter table transaction_entries drop constraint if exists fk_transaction_entries_transaction_id;
drop index if exists ix_transaction_entries_transaction_id;

alter table transaction_entries drop constraint if exists fk_transaction_entries_account_id;
drop index if exists ix_transaction_entries_account_id;

alter table users drop constraint if exists fk_users_company_id;
drop index if exists ix_users_company_id;

alter table users drop constraint if exists fk_users_preferred_currency_id;
drop index if exists ix_users_preferred_currency_id;

alter table user_companies drop constraint if exists fk_user_companies_users;
drop index if exists ix_user_companies_users;

alter table user_companies drop constraint if exists fk_user_companies_companies;
drop index if exists ix_user_companies_companies;

alter table warehouses drop constraint if exists fk_warehouses_manager_id;
drop index if exists ix_warehouses_manager_id;

-- drop all
drop table if exists accounts;

drop table if exists accounting_periods;

drop table if exists audit_logs;

drop table if exists bank_account;

drop table if exists bank_reconciliation;

drop table if exists billing_series;

drop table if exists business_units;

drop table if exists cash_box;

drop table if exists cash_movement;

drop table if exists closing_entries;

drop table if exists companies;

drop table if exists currencies;

drop table if exists customer_invoices;

drop table if exists customer_payments;

drop table if exists depreciation_records;

drop table if exists payroll_employees;

drop table if exists exchange_differences;

drop table if exists exchange_rates;

drop table if exists financial_reports;

drop table if exists report_lines;

drop table if exists financial_statement_model;

drop table if exists financial_statement_row;

drop table if exists fixed_assets;

drop table if exists fixed_asset_categories;

drop table if exists inventory_categories;

drop table if exists inventory_items;

drop table if exists inventory_movements;

drop table if exists payroll_batches;

drop table if exists payroll_concepts;

drop table if exists payroll_concept_values;

drop table if exists payroll_details;

drop table if exists payroll_periods;

drop table if exists reconciliation_item;

drop table if exists report_definition;

drop table if exists roles;

drop table if exists role_permissions;

drop table if exists sales_invoices;

drop table if exists sales_invoice_lines;

drop table if exists supplier_invoices;

drop table if exists supplier_payments;

drop table if exists system_configuration;

drop table if exists tax_rates;

drop table if exists third_parties;

drop table if exists transactions;

drop table if exists transaction_entries;

drop table if exists users;

drop table if exists user_companies;

drop table if exists warehouses;

drop index if exists idx_audit_user;
drop index if exists idx_audit_operation;
drop index if exists idx_audit_entity;
drop index if exists idx_audit_timestamp;
