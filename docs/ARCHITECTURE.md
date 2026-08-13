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

## 🎯 Core Modules

### 1. **Accounting Core** (`com.econovafx.core.accounting`)
- **Chart of Accounts**: Hierarchical account structure (Asset, Liability, Equity, Revenue, Expense)
- **Journal Entries**: Double-entry bookkeeping with validation
- **Period Control**: Monthly/Yearly period management with open/close controls
- **Transaction Management**: Draft, Validated, Posted, Cancelled states

### 2. **User Management** (`com.econovafx.core.security`)
- **Authentication**: Local authentication (LDAP/AD ready)
- **Authorization**: Role-based access control (RBAC)
- **Roles**: Administrator, Accountant, Auditor, Viewer
- **Audit Trail**: Complete activity logging

### 3. **Third Parties** (`com.econovafx.core.thirdparty`)
- **Customer Management**: Client registry and tracking
- **Supplier Management**: Vendor information
- **Employee Records**: Staff accounting data
- **Contact Information**: Fiscal and contact details

### 4. **Dashboard & Reporting** (`com.econovafx.ui.dashboard`)
- **KPIs**: Key accounting metrics
- **Trial Balance**: Account balance reports
- **Financial Statements**: Basic financial reports
- **Export**: PDF, Excel, CSV formats

### 5. **Exchange Rates** (`com.econovafx.core.forex`)
- **Rate Management**: Active exchange rates
- **Historical Rates**: Rate history tracking
- **Multi-currency**: Automatic conversion in transactions

## 🔧 Technical Stack

| Layer | Technology | Purpose |
|-------|-----------|---------|
| **UI** | JavaFX 17 | Modern desktop interface |
| **DI** | Avaje Inject 12.6 | Lightweight dependency injection |
| **ORM** | Ebean 17.11.0 | High-performance object-relational mapping |
| **Database** | H2 2.2.224 | Embedded database (dev/test) |
| **Logging** | Logback 1.4.14 | SLF4J logging framework |
| **Testing** | JUnit 5 + AssertJ | Unit and integration testing |
| **Build** | Maven 3.9+ | Dependency management and build |

## 📦 Package Structure

```
src/
├── main/
│   ├── java/com/econovafx/
│   │   ├── Main.java                    # Application entry point
│   │   ├── config/                      # Configuration classes
│   │   ├── core/                        # Core business logic
│   │   │   ├── accounting/              # Accounting modules
│   │   │   ├── security/                # User & auth
│   │   │   ├── thirdparty/              # Customers/Suppliers
│   │   │   └── forex/                   # Exchange rates
│   │   ├── repository/                  # Data access layer
│   │   ├── service/                     # Business services
│   │   ├── ui/                          # JavaFX UI components
│   │   │   ├── views/                   # FXML views
│   │   │   ├── controllers/             # FXML controllers
│   │   │   ├── components/              # Reusable UI components
│   │   │   └── dashboard/               # Dashboard views
│   │   └── util/                        # Utilities and helpers
│   └── resources/
│       ├── db/                          # Database scripts
│       ├── fxml/                        # FXML view files
│       ├── css/                         # Stylesheets
│       └── logback.xml                  # Logging configuration
└── test/
    └── java/com/econovafx/              # Test classes
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
