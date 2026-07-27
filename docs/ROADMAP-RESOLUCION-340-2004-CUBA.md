# Roadmap for Compliance with Cuban Resolution 340/2004
## EconovaFX - Accounting-Financial Systems Certification

---

## Executive Summary

This roadmap outlines the implementation plan for EconovaFX to achieve compliance with **Resolution 340/2004** issued by the Ministry of Finance and Prices of Cuba, published in the Official Gazette No. 2 on January 19, 2005.

**Objective:** Obtain certification for the degree of adaptation to Cuban Accounting Standards for accounting-financial systems supported on information technologies.

**Certification Validity:** 3 years (renewable)

---

## Current Status Assessment

### ✅ Completed Modules (Foundation)
- Core accounting structure
- Basic third-party management
- User authentication and authorization
- General dashboard

### ⚠️ Partially Implemented (Require Adjustments)
- Accounting module (needs Cuban chart of accounts integration)
- Third-party module (needs tax classification for Cuba)
- Period management (needs Cuban fiscal year rules)

### ❌ Not Implemented (Critical for Certification)
- Complete inventory module with average cost method
- Cash and banks module with reconciliation
- Fixed assets module
- Payroll module with Cuban social security calculations
- Billing module with consecutive numbering
- Financial statements generator (Cuban format)
- Audit trail system
- Backup/restore logging

---

## Implementation Phases

### Phase 1: Core Compliance Foundation (Weeks 1-4)
**Priority: CRITICAL**

#### 1.1 General Requirements - System-Wide
- [ ] **Integral Information Exchange**
  - Implement automatic data exchange between modules
  - Create inter-module dependency validation (e.g., prevent period close if other modules aren't closed)
  - Support single-user and multi-user modes

- [ ] **Data Validation**
  - Implement field validation by range and type across all modules
  - Add real-time validation feedback

- [ ] **Audit Trail System**
  - Implement process logging for all accounting operations
  - Configure retention period according to Cuban law (minimum 10 years)
  - Create audit trail viewer with filters

- [ ] **Import/Export Capabilities**
  - Develop data import from other companies in same system
  - Develop data export to external systems
  - Support standard formats (CSV, XML, JSON)

- [ ] **Backup/Restore Logging**
  - Log all backup operations with timestamp and user
  - Log all restore operations with details
  - Maintain logs for required retention period

- [ ] **Installation System**
  - Create modular installer (general or by modules)
  - Implement module dependency checking
  - Support multi-company installation under same database

- [ ] **Documentation**
  - Create User Manual (detailed explanation of each option and process)
  - Create Exploitation Manual (system functioning, process interrelations, database structures)
  - Implement documentation update mechanism for clients

- [ ] **System Compatibility**
  - Document supported operating systems
  - Test on Windows, Linux environments

- [ ] **Report Output Options**
  - Implement screen display for all reports
  - Implement printer output
  - Implement deferred/background processing

- [ ] **Primary Document Number Field**
  - Add document number field to all data entry screens
  - Make it mandatory for accounting entries

- [ ] **Report Features**
  - Add reprint capability
  - Add page range selection
  - Add page numbering on all reports
  - Include report title, entity name, period date, and print date

- [ ] **Automatic Reindexing**
  - Implement automatic reindexing process in all modules
  - Add optional manual reindexing

- [ ] **Error Messages**
  - Review all error messages for clarity and precision
  - Use simple, understandable language
  - Implement context-sensitive help

- [ ] **Online Help System**
  - Add F1 help to all processes
  - Include adequate explanation of functionality

- [ ] **Basic Reports**
  - Nominalize all basic reports required by accounting information fund
  - Program reports as menu options
  - Add parameter screens for custom reports

- [ ] **Multi-currency Support**
  - Ensure multi-currency operations comply with Cuban Accounting Standards
  - Implement historical exchange rates
  - Add currency revaluation processes

- [ ] **Consolidation**
  - Implement financial statements consolidation option
  - Support multiple companies consolidation

#### 1.2 Accounting Module Enhancements
- [ ] **Opening Balances Process**
  - Create opening balances screen sourced from Trial Balance
  - Prevent closing if Trial Balance is not balanced
  - Allow updates only through Operation Vouchers

- [ ] **Operation Vouchers Process**
  - Implement voucher entry screen with correction capabilities
  - Add automatic voucher balancing during entry
  - Allow exit before completing all voucher lines
  - Enable voucher deletion before posting to Ledger
  - Prevent deletion of accounts/subaccounts with movements or balances
  - Implement voucher transfer to Ledger via option
  - Show voucher status: Posted, Not Posted, Not Balanced
  - Enable viewing vouchers from last 3 fiscal years minimum
  - Implement auto-consecutive numbering for posting
  - Validate voucher date matches active accounting period
  - Print/display voucher batch before posting
  - Create historical file of posted operations

- [ ] **Reports**
  - Voucher edition with operation details for any period
  - Ledger queries showing initial balance, monthly balances from January to current month, and YTD balance
  - Historical file listing with: account code, description, initial balance, voucher details (number, date, module, description, debit/credit, resulting balance), total debits and credits
  - Trial Balance at account and subaccount level
  - Historical voucher file for any period

- [ ] **Monthly and Annual Closing**
  - Monthly close conditions:
    - New period must be immediately after current period
    - All other modules must be closed first
    - No pending vouchers to post
    - Display backup warning
  - Annual close conditions:
    - Last month of period must be closed
    - Nominal accounts must be closed
    - All Financial Statements must be issued
    - Display backup warning
  - Prevent reopening of closed periods

- [ ] **Financial Statements**
  - Implement parameter configuration for statement generation
  - Generate models according to Cuban standards:
    - Balance Sheet
    - Income Statement
    - Cash Flow Statement
    - Changes in Equity
  - Follow row/column concepts per Cuban standards
  - Implement proper report structures

### Phase 2: Inventory Module (Weeks 5-8)
**Priority: HIGH**

- [ ] **Master Files**
  - Product master with all attributes (code, description, unit of measure, cost, etc.)
  - Warehouse master
  - Product-Warehouse relationship

- [ ] **Initial Load Process**
  - Register existing balances in warehouses
  - Close load only when balances match Ledger
  - Prevent additional product inclusion after close (only via movement option)

- [ ] **Movement Processes**
  - Capture entries, exits, and transfers with:
    - Warehouse code
    - Document number and type
    - Sequence number (to guarantee chronological update)
    - Document date
    - Product code
    - Unit of measure
    - Quantity
    - Final existence per document
    - Amount (auto-calculated)
    - Existence in warehouse after movement
    - Difference (auto-calculated) between sub-ledger and warehouse reported existence
    - Cost center affected by exit/devolution
    - Counterpart account for accounting

- [ ] **Posting Process**
  - Transfer movements to sub-ledger (only those without differences)
  - Update average price automatically
  - Emit list of updated movements
  - Emit list of differences for investigation

- [ ] **Operation Registry**
  - Emit numbered registry of posted movement batches separated by type (entries, exits, devolutions, transfers)

- [ ] **Accounting Operations**
  - Create Operation Voucher for each movement type in posted batch
  - Transfer vouchers to Accounting Module
  - Transfer sales/purchases to Cobros y Pagos Module

- [ ] **Reports**
  - Products listing (by code sequence, warehouse-code, etc.)
  - Balances listing (by warehouse, account, etc.)
  - Movement history
  - Single product and its movements
  - Sub-ledger showing all products with: initial balance, operations, final balance (quantity and amount), code, description, unit of measure, document, and other qualitative data
  - Idle products report
  - Slow-moving products (based on defined parameters)
  - Blind count (100% and 10% with defined algorithms)

- [ ] **Closing Processes**
  - Monthly close conditions:
    - Transfer pending vouchers to Accounting Module
    - Transfer pending sales/purchases to Cobros y Pagos Module
  - Annual close conditions:
    - Monthly close must be completed
    - Emit and transfer zero-balance adjustment vouchers to Accounting Module

### Phase 3: Cobros y Pagos Module (Weeks 9-11)
**Priority: HIGH**

- [ ] **Opening Process**
  - Capture all supplier invoices and documents representing pending payment obligations
  - Capture all customer invoices and documents representing pending collection rights
  - Independent close conditioned to matching corresponding Ledger balances

- [ ] **Operations**
  - **Sales** (automatic from Billing Module or manual entry):
    - Customer
    - Invoice number (validate consecutive)
    - Invoice date
    - Payment term granted to customer
    - Total amount
    - Pending amount
    - Accounting
  
  - **Purchases** (manual entry from received documents):
    - Supplier
    - Document number
    - Date
    - Total amount
    - Pending amount
    - Accounting
  
  - **Collections, Payments, Liquidation of Advance Payments/Collections**:
    - Customer or Supplier
    - Document
    - Date
    - Total amount
    - Balance from partial collections/payments
    - Accounting
    - Details of invoices covered by document
  
  - **Transfer to Effects Receivable** (with original invoice data)
  - **Transfer from Effects Receivable to Accounts Receivable** (maintaining original invoice date for aging analysis)

- [ ] **Voucher Creation**
  - Create one voucher per operation type in captured batch
  - Transfer to Accounting Module

- [ ] **Reports**
  - Operations Registry (one registry per operation type with consecutive numbering)
  - Aging Analysis (by customer/supplier, showing pending documents analyzed by predefined age ranges; use invoice date if it matches payment term, otherwise use payment term date)
  - Sub-ledgers (one per rights/obligations concept, analyzed by associated accounts, showing debtor/creditor balance and period collections/payments report)

### Phase 4: Billing Module (Weeks 12-13)
**Priority: HIGH**

- [ ] **Invoice Generation**
  - Automatic consecutive numbering
  - Define different payment conditions
  - Apply various discount types
  - Include all control information required by law

- [ ] **Control Aspects**
  - Register payment terms for customer collections
  - Track customer debt due dates
  - Affect corresponding inventory and customer accounts
  - Prevent modification of issued invoices (must cancel and reissue)
  - Work based on previously registered customer orders
  - Modify or cancel orders

- [ ] **Reports**
  - Customer payment terms listing
  - Customer debt due dates listing
  - Pending customer orders
  - Customer invoices in a period
  - Cancelled invoices
  - Operations listing for any requested period

### Phase 5: Fixed Assets Module (Weeks 14-16)
**Priority: MEDIUM**

- [ ] **Data Capture Screens**
  - Include all reference, control, and quantitative data for Fixed Assets and depreciations
  - Include technical characteristics of each asset

- [ ] **Initial Load**
  - Register existing Fixed Assets data when starting module
  - Close only when asset values and depreciations match Ledger balances

- [ ] **Operations**
  - Handle updates to any concept identifying each Fixed Asset

- [ ] **Movements**
  - Register all inherent movements to this activity

- [ ] **Basic Units and Control Modules**
  - Independent handling for assets with specific characteristics
  - Handle creation and specific treatments

- [ ] **Posting**
  - Transfer movements and updates to master file

- [ ] **Accounting Operations**
  - Depreciation calculation
  - Create vouchers for depreciation and each movement concept
  - Transfer vouchers to Accounting Module
  - Transfer collections from sales and payments from purchases to Cobros y Pagos Module

- [ ] **Reports**
  - Fully depreciated fixed assets
  - Listing by responsibility area (with value and depreciation totals by area and grand total)
  - Sub-ledger by Fixed Asset
  - Additions and Disposals reports
  - Transfers (rented and sent for repair)
  - Revaluation (by Appraisals)
  - General Repairs that increase asset value
  - Listing by inventory number sequence
  - Balances by account-subaccount

- [ ] **Closing**
  - Monthly close conditions:
    - Depreciation process must be executed
    - Next month number must be current month + 1
  - Annual close conditions:
    - Last month of fiscal period must be closed
    - Delete all yearly movements (after backup) except rented assets and assets sent for repair

### Phase 6: Payroll Module (Weeks 17-19)
**Priority: MEDIUM**

- [ ] **Master Files Creation**
  - Workers master file
  - Discounts that are not retentions
  - Retentions (by type and worker)
  - Special Contribution to Social Security
  - Vacations

- [ ] **Payroll Operations**
  - Data capture for payroll and mini-payroll preparation
  - Apply balancing mechanism to detect errors or fraud
  - Automatic calculation including:
    - 9.09% (vacation accrual)
    - Labor Force Utilization tax
    - Social Security Contribution
    - Applied retentions from master file
  - Use factors from corresponding master files
  - Automatic update of Vacations Sub-ledger from 9.09% applied to salary and wage payrolls and vacation enjoyment time registered in Vacation Payroll

- [ ] **Document Emission**
  - Salary Payroll and Mini-payroll with expense and collection vouchers
  - Wage Payroll and Mini-payroll with expense and collection vouchers
  - Vacation and Subsidy Payroll with corresponding vouchers

- [ ] **Retentions Process**
  - Adjustments to retentions
  - Create vouchers for retention adjustments and applied retentions
  - List applied and non-applied retentions and discounts
  - Create corresponding Operation Vouchers

- [ ] **Accounting Operations**
  - Create one voucher per payroll/mini-payroll type
  - Transfer vouchers to Accounting Module
  - Transfer deductions to be discounted to Cobros y Pagos Module

- [ ] **Reports**
  - Alert listing for workers with more than 20 accumulated days
  - Vacations Sub-ledger (time and amount)

### Phase 7: Testing & Documentation (Weeks 20-22)
**Priority: CRITICAL**

- [ ] **Integration Testing**
  - Test inter-module data exchange
  - Verify period close dependencies
  - Test audit trail completeness
  - Verify backup/restore logging

- [ ] **User Acceptance Testing**
  - Test with sample Cuban company data
  - Validate all reports against Cuban standards
  - Test multi-user concurrent operations

- [ ] **Documentation Finalization**
  - Complete User Manual (Spanish)
  - Complete Exploitation Manual (Spanish)
  - Include database structure documentation
  - Prepare update distribution mechanism

- [ ] **Security Testing**
  - Test user access controls
  - Verify data integrity
  - Test system recovery procedures

### Phase 8: Certification Process (Weeks 23-24)
**Priority: CRITICAL**

- [ ] **Prepare Certification Request**
  - Complete Form DCSC-01 (Solicitud de Dictamen)
  - Prepare executable version of system
  - Prepare exploitation manual
  - Prepare test data set

- [ ] **Select Certifying Entity**
  - Identify authorized certifying entities approved by Ministry of Finance and Prices
  - Verify entity meets requirements (Form DCSC-02)
  - Establish contractual relationship

- [ ] **Submit for Certification**
  - Submit request to certifying entity
  - Provide executable system version
  - Provide manuals and test data
  - Coordinate review schedule

- [ ] **Review Process Support**
  - Support entity review team
  - Address observations and recommendations
  - Implement required adjustments

- [ ] **Obtain Certification**
  - Receive favorable opinion (Dictamen)
  - Submit to Ministry of Informatics and Communications designated entity
  - Send copy to Directorate of Accounting Standards (Ministry of Finance and Prices)
  - Archive copy in certifying entity

---

## Technical Requirements Checklist

### Database Requirements
- [ ] Support for Cuban Chart of Accounts structure
- [ ] Multi-company support under same database
- [ ] Historical data retention (minimum 10 years)
- [ ] Automatic indexing and optimization

### Security Requirements
- [ ] User authentication with password encryption
- [ ] Role-based access control
- [ ] Session management
- [ ] Audit trail for all critical operations
- [ ] Data backup and recovery procedures

### Performance Requirements
- [ ] Response time < 3 seconds for standard operations
- [ ] Support for concurrent users (define maximum based on license)
- [ ] Batch processing for heavy operations
- [ ] Efficient report generation

### Compliance Requirements
- [ ] All messages in Spanish
- [ ] All reports in Spanish with Cuban format
- [ ] Currency formatting according to Cuban standards (CUP, USD, EUR as needed)
- [ ] Date format: DD/MM/YYYY
- [ ] Number format: decimal comma, thousands separator

---

## Risk Management

### High Risks
1. **Changes in Cuban Accounting Standards** - Monitor official publications
2. **Certification Delays** - Start process early, maintain communication with certifying entity
3. **Data Migration Issues** - Thorough testing with production-like data

### Mitigation Strategies
- Regular consultation with Cuban accounting experts
- Early engagement with certifying entities
- Comprehensive testing strategy
- Phased implementation approach

---

## Success Criteria

1. **Technical Compliance:** All elements in Resolution 340/2004 Section II are implemented
2. **Functional Compliance:** System produces all required reports in Cuban format
3. **Certification:** Obtain favorable opinion (Dictamen) from authorized entity
4. **Operational Readiness:** System can be deployed in Cuban companies

---

## Maintenance & Updates

### Post-Certification Obligations
- Notify Directorate of Accounting Standards of any corrections or updates:
  - Causes of correction/update
  - Modifications made to certified version
- Maintain certification through periodic renewals (every 3 years)
- Keep documentation updated and distributed to clients

### Version Control
- Maintain version history
- Document all changes
- Test updates before release
- Communicate changes to clients

---

## Contact Information

**For Certification Process:**
- Directorate of Accounting Standards
- Ministry of Finance and Prices, Cuba

**For Technical Support:**
- EconovaFX Development Team
- Email: [support@econovafx.com](mailto:support@econovafx.com)

---

*Last Updated: July 2025*
*Version: 1.0*
*Status: In Development*
