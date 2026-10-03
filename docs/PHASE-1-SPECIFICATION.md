# Phase 1 Specification: General Requirements and Accounting Module Enhancements

## Resolution 340/2004 - Cuba Compliance
**Ministry of Finance and Prices (MFP) - Republic of Cuba**
**Date:** December 8, 2004 | **Official Gazette:** January 19, 2005

---

## 1. GENERAL REQUIREMENTS (Sección GENERALES)

### 1.1 Integralidad (Integrity)
- [ ] **Automatic information exchange between modules**: Comprobantes transferred automatically to accounting module from other modules
- [ ] **Conditional process execution**: Cannot close accounting period if other modules are not closed
- [ ] **Multi-user and single-user support**: System must work in both modes

### 1.2 Data Validation
- [ ] All fields containing data must be validated according to range and type
- [ ] Clear and precise error messages in easy-to-understand language

### 1.3 Audit Trails
- [ ] **Process traces**: Keep logs of all processes for the legally established conservation period
- [ ] **Backup/Restore traces**: Log all backup and restore operations

### 1.4 Import/Export Capability
- [ ] Import/export databases from/to other companies in the same system
- [ ] Import/export databases from/to other systems

### 1.5 Installation
- [ ] General or modular installation capability
- [ ] Conditional module installation based on dependencies
- [ ] Multi-company support under same database

### 1.6 Documentation
- [ ] **User Manual**: Detailed explanation of each option and process
- [ ] **Exploitation Manual**: Complete system functioning including interrelations and database structures
- [ ] Permanent documentation updates sent to clients

### 1.7 Reports
- [ ] Output options: screen, printer, or deferred (batch)
- [ ] Reprint capability with page range selection
- [ ] All reports must include:
  - Period date
  - Print date
  - Page numbering
  - Report title
  - Company name

### 1.8 Primary Document Tracking
- [ ] All data entry screens must have a field for primary document number

### 1.9 Reindexation
- [ ] Automatic and optional reindexation processes in all modules

### 1.10 Help System
- [ ] Online help for every process explaining its functionality

### 1.11 Multi-currency Support
- [ ] If supporting multiple currencies, must comply with Cuban Accounting Standards
- [ ] Base currency: CUP (Cuban Peso)
- [ ] Support for MLP (Moneda Libremente Convertible) and USD as needed

### 1.12 Consolidation
- [ ] Financial statements consolidation option

---

## 2. ACCOUNTING MODULE REQUIREMENTS (MÓDULO DE CONTABILIDAD)

### 2.1 Fundamental Processes
- [x] Opening balances in General Ledger and closure of opening
- [x] Operations (Comprobantes)
- [x] Reports
- [x] Monthly and annual closing

### 2.2 Opening Balances
- [ ] Source must be a Balance de Comprobación (Trial Balance)
- [ ] Conditioned to no prior closure
- [ ] Other balance updates only through Comprobantes de Operaciones
- [ ] Closure conditioned to balanced Trial Balance

### 2.3 Operations Process
- [ ] **Data entry screen** for journal entries with corrections
- [ ] **Automatic balancing** during entry
- [ ] Ability to exit without completing all entries
- [ ] Delete Comprobante before posting to Ledger
- [ ] **Cannot delete accounts/subaccounts with movements or balances** during fiscal year
- [ ] Post to Ledger via explicit option
- [ ] Status information:
  - Posted to Ledger
  - Not posted to Ledger
  - Not balanced
- [ ] View Comprobantes from last 3 fiscal years minimum
- [ ] **Posting requirements**:
  - Automatic consecutive numbering
  - Balanced Comprobantes only
  - Date must match open accounting period
  - Preview print/display before posting
  - Include in Historical File after posting

### 2.4 Reports (Informes)
- [ ] **Comprobante Edition**: Detail of operations for any period
- [ ] **Ledger Consultation**: Show for each account:
  - Initial balance
  - Monthly balances from January to current month
  - Balance to date
- [ ] **Historical File Listing**: By account range and month range showing:
  - Account code
  - Account description
  - Initial balance for requested range
  - For each Comprobante: number, date, source module, operation detail, debit/credit amount, resulting balance
  - Total debits and credits
- [ ] **Trial Balance**: At account and subaccount level
- [ ] **Historical Comprobante File**: For any period

### 2.5 Closing Processes

#### Monthly Closing Conditions:
- [ ] New period must be immediately after current period
- [ ] All other modules must be closed first
- [ ] No pending Comprobantes to post
- [ ] Backup warning before closing

#### Annual Closing Conditions:
- [ ] Last month of period must be closed
- [ ] Nominal accounts must be closed
- [ ] All required Financial Statements must be issued
- [ ] Backup warning before closing

### 2.6 Period Reopening
- [x] **IMPOSSIBLE to reopen a closed period** (Resolution requirement)

### 2.7 Financial Statements
- [ ] Parameter configuration:
  - Models to generate
  - Row/column concepts per current standards
  - Report structures per standards
  - Transfer balances from Accounting Module databases
  - Manual data entry for non-registered data
  - Generate totals/subtotals
- [ ] Automatic generation of all predefined models
- [ ] Reports: Emission of models, historical files, print and screen display

---

## 3. CURRENT STATUS ANALYSIS

### Already Implemented ✅
1. **AccountingPeriod Entity**: 
   - Status tracking (OPEN, CLOSED, LOCKED)
   - Type tracking (MONTHLY, ANNUAL, CUSTOM)
   - Closure metadata (closedBy, closedDate, closingNotes)
   
2. **AccountingPeriodService**:
   - Period creation with validation
   - Monthly and annual period management
   - Dependent module validation before closing
   - Lock mechanism (prevents reopening)
   - Date validation for transactions

3. **ThirdParty Entity**: 
   - Basic structure exists but needs Cuba-specific adjustments

### Required Changes ⚠️

#### 3.1 ThirdParty Model - Remove DGII References
```java
// REMOVE: Dominican Republic references
// Current line 14: "Compliant with Dominican Republic DGII Resolution 340-2004"
// Current line 43: country = "República Dominicana"
// Current enum names use Spanish comments for DR tax system

// REPLACE WITH: Cuba ONAT compliance
// - Tax classification for Cuban entities
// - Country default = "Cuba"
// - Identification types: NIT (Número de Identificación Tributaria), Cédula, etc.
```

#### 3.2 AccountingPeriod Enhancements
```java
// ADD: Validation that prevents reopening closed periods (already implemented ✅)
// ADD: Ensure sequential period closure (month-by-month)
// ADD: Integration with all modules for closure validation
```

#### 3.3 Comprobante (Journal Entry) Enhancements
```java
// NEED TO IMPLEMENT:
// - Automatic consecutive numbering per period
// - Status tracking (Draft, Posted, Historical)
// - Preview before posting
// - Historical file preservation (3+ years)
// - Cannot delete accounts with balances
```

#### 3.4 Audit Trail Implementation
```java
// NEED TO IMPLEMENT:
// - Process logging for all critical operations
// - Backup/Restore logging
// - User action tracking with timestamps
// - Retention period configuration
```

---

## 4. ACTION PLAN - PHASE 1

### Week 1: Foundation Updates
1. **Update ThirdParty model** for Cuba compliance
   - Change country default to "Cuba"
   - Update tax classification enum for Cuban tax system
   - Add NIT (Número de Identificación Tributaria) validation
   
2. **Create Company Configuration entity**
   - Add mandatory fields: NIT, Address, Billing Resolution Number
   - Configure base currency (CUP)
   - Set fiscal year parameters

3. **Enhance AuditLog system**
   - Implement comprehensive logging
   - Add backup/restore tracking
   - Configure retention policies

### Week 2: Accounting Module Strengthening
1. **Implement Comprobante numbering system**
   - Automatic consecutive per period
   - Prevent gaps in sequence
   
2. **Add preview functionality** before posting
   
3. **Strengthen period closure validation**
   - Sequential month verification
   - All-modules-closed verification
   
4. **Implement account protection**
   - Prevent deletion of accounts with balances/movements

### Week 3: Reporting Framework
1. **Standardize report headers**
   - Company name
   - Period dates
   - Print date
   - Page numbering
   
2. **Add reprint with page range** capability

3. **Implement output options** (screen, printer, file)

### Week 4: Documentation & Testing
1. **Create User Manual structure**
2. **Create Exploitation Manual structure**
3. **Online help framework**
4. **Comprehensive testing of all Phase 1 requirements**

---

## 5. CUBA-SPECIFIC CONSIDERATIONS

### Tax Authority: ONAT (Oficina Nacional de Administración Tributaria)
- Formerly MFP (Ministerio de Finanzas y Precios)
- Certification required every 3 years
- Must pass audit by authorized entity

### Currency Considerations
- **Base Currency**: CUP (Cuban Peso)
- **Convertible Currency**: MLP (Moneda Libremente Convertible)
- **Foreign Currency**: USD, EUR (for international operations)
- Exchange rate tracking required per Cuban Accounting Standards

### Fiscal Year
- Typically calendar year (January - December)
- Special periods may require authorization

---

## 6. NEXT STEPS

After completing Phase 1, proceed to:
- **Phase 2**: Cash & Bank Module (Conciliación Bancaria, Caja Chica)
- **Phase 3**: Inventory Module (Valuation methods, Kardex)
- **Phase 4**: Billing Module (Invoice sequencing, DGII-style reporting for Cuba)
- **Phase 5**: Fixed Assets Module
- **Phase 6**: Payroll Module
- **Phase 7**: Financial Statements Generator
- **Phase 8**: Certification Process Preparation

---

**Document Version**: 1.0  
**Last Updated**: 2025  
**Status**: In Progress  
**Author**: EconovaFX Development Team
