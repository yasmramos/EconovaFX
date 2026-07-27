# Roadmap for Compliance with Cuban Resolution 340/2004

## Overview
This roadmap outlines the implementation plan to achieve full compliance with **Resolution No. 340/2004** issued by the **Ministry of Finance and Prices of Cuba** (MINFIN) on January 19, 2005.

**Objective:** Obtain certification for the EconovaFX accounting-financial system as adapted to Cuban Accounting Standards.

---

## Phase 1: General Requirements & System Configuration ✅ (IN PROGRESS)

### 1.1 General System Requirements (Resolución Section II - GENERALES)
- [x] **Integralidad**: Automatic information exchange between modules
- [ ] **Validation**: All fields must be validated by range and type
- [ ] **Audit Trail**: System must log all processes for the legally established retention period
- [ ] **Import/Export**: Capability to import/export databases from/to linked companies
- [ ] **Backup Logs**: Track all backup and restore operations
- [ ] **Multi-user Support**: Function in both single-user and multi-user modes
- [ ] **Documentation**: User Manual and Exploitation Manual (or combined)
- [ ] **Error Messages**: Clear, precise messages in easy-to-understand language
- [ ] **Online Help**: Context-sensitive help for every process
- [ ] **Reports**: Must support screen, printer, and file output
- [ ] **Document Reference**: All data entry screens must include primary document number field
- [ ] **Report Formatting**: Date range, print date, page numbering, report title, entity name
- [ ] **Multi-currency**: If supported, must comply with Cuban Accounting Standards
- [ ] **Reindexing**: Automatic and optional reindexing processes for all modules

### 1.2 Entity Configuration
- [ ] Company fiscal data (NIT, address, resolution number for billing)
- [ ] Chart of Accounts aligned with Cuban Accounting Standards
- [ ] Fiscal periods configuration with lock capabilities

### 1.3 Security & Access Control
- [ ] Role-based access control
- [ ] User activity logging (audit trail)
- [ ] Password policies

**Deliverable:** `docs/PHASE-1-SPECIFICATION.md`

---

## Phase 2: Accounting Module (MÓDULO DE CONTABILIDAD)

### 2.1 Core Processes
- [ ] **Opening Balances**: From Trial Balance, conditioned to no prior closure
- [ ] **Balance Validation**: Opening closure only allowed when balances are squared
- [ ] **Operations**:
  - Voucher entry screen with corrections
  - Automatic voucher squaring during entry
  - Exit without completing all voucher lines
  - Delete vouchers before posting to Ledger
  - Prevent deletion of accounts/subaccounts with movements or balances
  - Optional posting to Ledger
  - Voucher status tracking (Posted, Not Posted, Not Squared)
  - View vouchers from last 3 fiscal years minimum
- [ ] **Posting Rules**:
  - Automatic consecutive numbering
  - Only squared vouchers can be posted
  - Date must match active accounting period
- [ ] **Reports**:
  - Trial Balance (with/without opening balances)
  - General Ledger by account/subaccount
  - Account history with document details
  - Daily journals (optional consolidated or detailed)
  - Balance Sheet and Financial Statements
- [ ] **Closures**:
  - Monthly: Conditioned to all other modules being closed for the period
  - Annual: Conditioned to monthly closure of last fiscal period

**Dependency:** Phase 1 must be complete

---

## Phase 3: Third Parties Module (MÓDULO DE TERCEROS)

### 3.1 Client/Supplier Management
- [ ] Unified client/supplier records
- [ ] Payment deadline tracking
- [ ] Aging analysis by predefined ranges
- [ ] Sub-ledger by concept, showing balances and payment reports
- [ ] Operation registry with consecutive numbering per batch

### 3.2 Operations
- [ ] Transfer to Accounts Receivable with original invoice data
- [ ] Transfer from Effects to Accounts Receivable maintaining original dates
- [ ] One voucher per operation type per batch

**Dependency:** Phase 1

---

## Phase 4: Billing Module (MÓDULO DE FACTURACIÓN)

### 4.1 Invoice Requirements
- [ ] Automatic consecutive numbering
- [ ] Multiple payment conditions
- [ ] Various discount types
- [ ] Customer payment deadline registration
- [ ] Due date tracking
- [ ] Inventory and customer account impact
- [ ] **No modification after issuance** (must cancel and reissue)
- [ ] Support for customer order processing
- [ ] Order modification/cancellation capability

### 4.2 Reports
- [ ] Customer payment deadlines list
- [ ] Customer due dates list
- [ ] Pending customer orders
- [ ] Customer invoices per period
- [ ] Cancelled invoices list
- [ ] Operations list for any requested period

**Dependency:** Phase 3

---

## Phase 5: Fixed Assets Module (MÓDULO DE ACTIVOS FIJOS TANGIBLES)

### 5.1 Core Processes
- [ ] Initial load (must match Ledger balances before closing)
- [ ] Operations (updates to asset data)
- [ ] Movements (all inherent transactions)
- [ ] Posting to master file
- [ ] Accounting operations (depreciation, vouchers, transfers)
- [ ] Basic Units and Control Modules (independent processing)

### 5.2 Data Requirements
- [ ] All reference, control, and quantitative data for assets and depreciation
- [ ] Technical characteristics of each asset

### 5.3 Reports
- [ ] Fully depreciated assets
- [ ] List by responsibility area (with totals)
- [ ] Sub-ledger by asset
- [ ] Additions and disposals reports
- [ ] Transfers (rented and sent for repair)
- [ ] Revaluation (appraisals)
- [ ] General repairs increasing asset value
- [ ] Consecutive inventory number list
- [ ] Balances by account-subaccount

### 5.4 Closures
- [ ] **Monthly**: Conditioned to depreciation execution; next month = current + 1
- [ ] **Annual**: Conditioned to last month closure; delete year movements (except rented/repaired assets)

**Dependency:** Phase 2

---

## Phase 6: Payroll Module (MÓDULO DE NOMINAS)

### 6.1 Master Files
- [ ] Workers
- [ ] Non-retention discounts
- [ ] Retentions by type and worker
- [ ] Special Social Security Contribution
- [ ] Vacations

### 6.2 Payroll Operations
- [ ] Data capture for payroll and mini-payroll with fraud detection mechanism
- [ ] Automatic calculation including:
  - 9.09% vacation accrual
  - Workforce Utilization tax
  - Social Security Contribution
  - Applied retentions from master file
- [ ] Automatic Vacation Sub-ledger update
- [ ] Payroll emission with expense and collection vouchers:
  - Salary payroll and mini-payroll
  - Wage payroll and mini-payroll
  - Vacation and subsidy payroll

### 6.3 Retentions
- [ ] Adjustment processing
- [ ] Voucher creation for adjustments and applied retentions
- [ ] Lists of applied/unapplied retentions and discounts

### 6.4 Accounting Operations
- [ ] One voucher per payroll type
- [ ] Transfer to Accounting Module
- [ ] Transfer of deductions to Payments Module

### 6.5 Reports
- [ ] Alert for workers with >20 accumulated days
- [ ] Vacation sub-ledger (time and amount)

**Dependency:** Phase 2

---

## Phase 7: Cash & Banks Module (MÓDULO DE COBROS Y PAGOS)

### 7.1 Integration Points
- [ ] Receive transfers from Billing, Fixed Assets, Payroll
- [ ] Process collections and payments
- [ ] Bank reconciliation

**Dependency:** Phases 3, 4, 5, 6

---

## Phase 8: Certification Preparation

### 8.1 Documentation
- [ ] User Manual (detailed, easy to understand)
- [ ] Exploitation Manual (complete system functioning, interrelations, database structures)
- [ ] Update mechanism for documentation

### 8.2 Testing & Validation
- [ ] Internal audit of all requirements
- [ ] Test data set preparation
- [ ] Executable version packaging

### 8.3 Submission (Model DCSC-01)
- [ ] Application to authorized entity
- [ ] Contract establishment
- [ ] Deliver executable version, exploitation manual, and test data set

### 8.4 Audit Report
- [ ] General system data
- [ ] Conclusions
- [ ] Recommendations
- [ ] Full compliance report

**Final Deliverable:** Certification from MINFIN-authorized entity

---

## Implementation Priority

| Priority | Phase | Estimated Effort | Critical Path |
|----------|-------|------------------|---------------|
| 🔴 P0 | Phase 1 | 2 weeks | YES |
| 🔴 P0 | Phase 2 | 4 weeks | YES |
| 🟠 P1 | Phase 3 | 2 weeks | YES |
| 🟠 P1 | Phase 4 | 3 weeks | YES |
| 🟡 P2 | Phase 5 | 3 weeks | NO |
| 🟡 P2 | Phase 6 | 3 weeks | NO |
| 🟢 P3 | Phase 7 | 2 weeks | NO |
| 🟢 P3 | Phase 8 | 4 weeks | YES |

**Total Estimated Time:** 23 weeks (~6 months)

---

## Notes

- All code, comments, and documentation must be in **English**
- Commits must follow **Conventional Commits** format in English
- Development branch: `develop`
- All changes must pass existing tests before merging
- Each phase requires complete testing and documentation before proceeding

---

*Last Updated: 2025*
*Based on: Resolution No. 340/2004, Ministry of Finance and Prices, Republic of Cuba*
*Official Gazette No. 2, January 19, 2005*
