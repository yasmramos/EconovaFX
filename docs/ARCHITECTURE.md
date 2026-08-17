# 🏗️ EconoNova FX - Architecture Documentation

## System Overview

EconoNova FX is a modern accounting system built with **JavaFX 17** for the user interface and **Ebean ORM 17** for data persistence, designed to comply with Cuban accounting standards (Resolution 340/2004).

## 📐 Architecture Pattern

The application follows a **Layered Architecture** with clear separation of concerns:

```
┌─────────────────────────────────────────┐
│          Presentation Layer             │
│         (JavaFX UI Components)          │
├─────────────────────────────────────────┤
│         Controller Layer                │
│      (FXML Controllers, Handlers)       │
├─────────────────────────────────────────┤
│          Service Layer                  │
│    (Business Logic, Validation)         │
├─────────────────────────────────────────┤
│        Repository Layer                 │
│     (Ebean ORM, Data Access)            │
├─────────────────────────────────────────┤
│         Database Layer                  │
│        (H2 / MySQL / PostgreSQL)        │
└─────────────────────────────────────────┘
```

## 🎯 Modular Architecture

The application uses a **modular package structure** organized by business domain:

### Module Structure

Each module follows the pattern: `com.econovafx.modules.<module>.<layer>`

| Module | Package | Purpose |
|--------|---------|---------|
| **Accounting Core** | `com.econovafx.modules.accounting.*` | Chart of accounts, journal entries, periods, transactions |
| **Security** | `com.econovafx.modules.security.*` | User management, authentication, authorization |
| **Core Services** | `com.econovafx.modules.core.*` | Company management, backup, configuration |
| **Billing** | `com.econovafx.modules.billing.*` | Invoice management, billing documents |
| **Payroll** | `com.econovafx.modules.payroll.*` | Employee payroll processing |
| **Inventory** | `com.econovafx.modules.inventory.*` | Stock management, warehouses |
| **Receivables** | `com.econovafx.modules.receivables.*` | Accounts receivable, customer payments |
| **Payables** | `com.econovafx.modules.payables.*` | Accounts payable, supplier payments |
| **Bank** | `com.econovafx.modules.bank.*` | Bank accounts, reconciliation |
| **Cash** | `com.econovafx.modules.cash.*` | Cash management, petty cash |
| **Assets** | `com.econovafx.modules.assets.*` | Current assets management |
| **Fixed Assets** | `com.econovafx.modules.fixedassets.*` | Fixed assets, depreciation |
| **Reporting** | `com.econovafx.modules.reporting.*` | Financial reports, consolidation |

### Layer Structure Within Modules

Each module contains:

- **model** - Entity classes (JPA/Ebean entities)
- **repository** - Data access layer (Ebean repositories)
- **service** - Business logic layer
- **validation** - Business rule validation
- **ui.controller** - JavaFX controllers (where applicable)

Example:
```
com.econovafx.modules.accounting/
├── model/
│   ├── Account.java
│   ├── Transaction.java
│   └── AccountingPeriod.java
├── repository/
│   ├── AccountRepository.java
│   └── TransactionRepository.java
├── service/
│   ├── AccountService.java
│   ├── TransactionService.java
│   └── FinancialStatementService.java
├── validation/
│   └── AccountingValidator.java
└── ui/
    └── controller/
        └── AccountingController.java
```

## 🔧 Technical Stack

| Layer | Technology | Purpose |
|-------|-----------|---------|
| **UI** | JavaFX 17 | Modern desktop interface |
| **DI** | Avaje Inject 12.6 | Lightweight dependency injection |
| **ORM** | Ebean 17.11.0 | High-performance object-relational mapping |
| **Database (Dev/Test)** | H2 2.2.224 | Embedded database |
| **Database (Production)** | PostgreSQL 42.7.3 | Production database |
| **PDF Export** | Apache PDFBox 2.0.29 | PDF document generation |
| **Excel Export** | Apache POI 5.2.5 | Excel spreadsheet export |
| **Icons** | Ikonli 12.4.0 | Material Design icons |
| **Security** | jBCrypt 0.4 | Password hashing |
| **Logging** | Logback 1.4.14 | SLF4J logging framework |
| **Testing** | JUnit 5 + AssertJ | Unit and integration testing |
| **Build** | Maven 3.9+ | Dependency management and build |

## 📦 Package Structure

```
src/
├── main/
│   ├── java/com/econovafx/
│   │   ├── Main.java                        # Application entry point
│   │   ├── module-info.java                 # Java module descriptor
│   │   └── modules/                         # Modular architecture
│   │       ├── accounting/                  # Accounting core module
│   │       │   ├── model/                   # Entity classes
│   │       │   ├── repository/              # Data access layer
│   │       │   ├── service/                 # Business logic
│   │       │   ├── validation/              # Business validators
│   │       │   └── ui/
│   │       │       └── controller/          # JavaFX controllers
│   │       ├── security/                    # Security module
│   │       ├── core/                        # Core services module
│   │       ├── billing/                     # Billing module
│   │       ├── payroll/                     # Payroll module
│   │       ├── inventory/                   # Inventory module
│   │       ├── receivables/                 # Accounts receivable
│   │       ├── payables/                    # Accounts payable
│   │       ├── bank/                        # Bank management
│   │       ├── cash/                        # Cash management
│   │       ├── assets/                      # Current assets
│   │       ├── fixedassets/                 # Fixed assets
│   │       └── reporting/                   # Financial reporting
│   └── resources/
│       ├── db/                              # Database scripts
│       ├── fxml/                            # FXML view files
│       ├── css/                             # Stylesheets
│       └── logback.xml                      # Logging configuration
└── test/
    └── java/com/econovafx/                  # Test classes
```

## 🔄 Data Flow

### Example: Creating a Journal Entry

```
User Input (UI)
    ↓
FXML Controller (Validation)
    ↓
JournalEntryService (Business Logic)
    ↓
Validation Rules (Double-entry, Period Open, etc.)
    ↓
JournalEntryRepository (Ebean ORM)
    ↓
Database (H2/MySQL)
    ↓
Response → UI Update
```

## 🔐 Security Model

### Authentication Flow
1. User enters credentials in login screen
2. `AuthService` validates against database
3. JWT token generated (future enhancement)
4. User session established with role permissions
5. Navigation restricted based on role

### Authorization Matrix

| Module | Admin | Accountant | Auditor | Viewer |
|--------|-------|------------|---------|--------|
| Chart of Accounts | CRUD | R | R | R |
| Journal Entries | CRUD | CRUD | R | R |
| Period Control | CRUD | R/O | R | R |
| User Management | CRUD | - | - | - |
| Reports | CRUD | CRUD | CRUD | R |

*CRUD = Create, Read, Update, Delete | R = Read | O = Open/Close*

## 🗄️ Database Schema

### Key Entities

1. **Account** (`acc_account`)
   - id, code, name, type, parent_id, level

2. **JournalEntry** (`acc_journal_entry`)
   - id, entry_number, date, period_id, status, created_by

3. **JournalEntryLine** (`acc_entry_line`)
   - id, entry_id, account_id, debit, credit, description

4. **AccountingPeriod** (`acc_period`)
   - id, year, month, status (Open/Closed), opened_at, closed_at

5. **User** (`sec_user`)
   - id, username, password_hash, email, role_id, active

6. **ThirdParty** (`crm_third_party`)
   - id, type, name, tax_id, email, phone, address

## 🚀 Deployment Architecture

### Development
```
┌──────────────┐
│  JavaFX App  │
│   (Desktop)  │
└──────┬───────┘
       │
       ↓
┌──────────────┐
│   H2 DB      │
│  (Embedded)  │
└──────────────┘
```

### Production (Future)
```
┌──────────────┐
│  JavaFX App  │
│   (Desktop)  │
└──────┬───────┘
       │
       ↓
┌──────────────┐      ┌──────────────┐
│  MySQL/      │──────│   Backup &   │
│  PostgreSQL  │      │   Recovery   │
└──────────────┘      └──────────────┘
```

## 📊 Performance Considerations

- **Lazy Loading**: Ebean lazy loading for related entities
- **Caching**: Ebean L2 cache for frequently accessed data
- **Batch Operations**: Bulk inserts/updates for large datasets
- **Indexing**: Strategic database indexes on foreign keys and search columns
- **Connection Pooling**: HikariCP for efficient database connections

## 🔮 Future Enhancements

1. **Multi-database Support**: MySQL, PostgreSQL production drivers
2. **API Layer**: REST API for web/mobile integration
3. **Cloud Sync**: Optional cloud backup and synchronization
4. **Advanced Reporting**: JasperReports integration
5. **Multi-company**: Support for multiple company databases
6. **Budgeting Module**: Budget creation and tracking
7. **Fixed Assets**: Asset depreciation and management
8. **Payroll**: Employee payroll processing

## 📝 Design Principles

- **SOLID**: Single Responsibility, Open/Closed, Liskov Substitution, Interface Segregation, Dependency Inversion
- **DRY**: Don't Repeat Yourself - reusable components and utilities
- **KISS**: Keep It Simple, Stupid - avoid unnecessary complexity
- **YAGNI**: You Ain't Gonna Need It - implement only what's necessary
- **Convention over Configuration**: Sensible defaults reduce boilerplate

## 🧪 Testing Strategy

- **Unit Tests**: Service and utility classes (JUnit 5)
- **Integration Tests**: Repository layer with test database
- **UI Tests**: JavaFX UI component testing (TestFX - future)
- **Performance Tests**: Load testing for critical operations

---

**Last Updated**: August 2024  
**Version**: 0.1.0  
**Maintainer**: yasmramos
