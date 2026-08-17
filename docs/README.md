# EconovaFX Documentation Index

This directory contains comprehensive documentation for the EconovaFX accounting system.

## 📚 Documentation Overview

### Core Documentation

| Document | Description | Language |
|----------|-------------|----------|
| [ARCHITECTURE.md](ARCHITECTURE.md) | System architecture, modular structure, technical stack | English |
| [USER_GUIDE.md](USER_GUIDE.md) | User manual and feature documentation | Spanish |
| [DOCKER.md](DOCKER.md) | Docker deployment and containerization guide | Spanish |
| [FINANCIAL_REPORTING.md](FINANCIAL_REPORTING.md) | Financial reporting and multi-company consolidation | English |

### Resolution 340/2004 Compliance

| Document | Description | Language |
|----------|-------------|----------|
| [PHASE-1-SPECIFICATION.md](PHASE-1-SPECIFICATION.md) | Phase 1 implementation specification | English |
| [RESOLUTION-340-2004-GAP-ANALYSIS.md](RESOLUTION-340-2004-GAP-ANALYSIS.md) | Gap analysis for Resolution 340/2004 compliance | English |
| [RESOLUTION_340_2004_DETAILED_ANALYSIS.md](RESOLUTION_340_2004_DETAILED_ANALYSIS.md) | Detailed implementation evidence | English |
| [ROADMAP-RESOLUCION-340-2004-CUBA.md](ROADMAP-RESOLUCION-340-2004-CUBA.md) | Implementation roadmap for Cuban regulations | Spanish |

### Project Documentation (Root Directory)

| Document | Description | Language |
|----------|-------------|----------|
| [README.md](../README.md) | Project overview and getting started | Spanish |
| [CHANGELOG.md](../CHANGELOG.md) | Version history and changes | English |
| [COMMIT_GUIDELINES.md](../COMMIT_GUIDELINES.md) | Commit message conventions | Spanish |
| [LICENSE](../LICENSE) | Project license | English |

---

## 🏗️ Architecture Documentation

The [ARCHITECTURE.md](ARCHITECTURE.md) document covers:

- **Layered Architecture**: Presentation → Controller → Service → Repository → Database
- **Modular Structure**: `com.econovafx.modules.<module>` packages
- **Technical Stack**: JavaFX 17, Ebean ORM 17, PostgreSQL, Apache PDFBox/POI
- **Security Model**: RBAC with jBCrypt password hashing
- **Data Flow**: Example workflows for common operations

### Modules

The system is organized into the following modules:

1. **accounting** - Chart of accounts, journal entries, periods, transactions
2. **security** - User management, authentication, authorization
3. **core** - Company management, backup, configuration
4. **billing** - Invoice management, billing documents
5. **payroll** - Employee payroll processing
6. **inventory** - Stock management, warehouses
7. **receivables** - Accounts receivable, customer payments
8. **payables** - Accounts payable, supplier payments
9. **bank** - Bank accounts, reconciliation
10. **cash** - Cash management, petty cash
11. **assets** - Current assets management
12. **fixedassets** - Fixed assets, depreciation
13. **reporting** - Financial reports, multi-company consolidation

---

## 📊 Financial Reporting

The [FINANCIAL_REPORTING.md](FINANCIAL_REPORTING.md) document details:

### Date-Range Filtering
- Transaction-based balance calculations
- POSTED transaction filtering (excludes DRAFT)
- Account type sign conventions
- Period-specific vs cumulative balances

### Multi-Tenant Consolidation
- Multi-company financial statement consolidation
- Thread-safe tenant context management
- Aggregation by row identity
- Intercompany eliminations framework
- Resolution 340/2004 Requirement II.18 compliance

### Testing Coverage
- 11 unit tests for financial reporting (100% pass rate)
- FinancialStatementServiceTest (4 tests)
- ConsolidationServiceTest (7 tests)

---

## 🇨🇺 Resolution 340/2004 Compliance

### Gap Analysis

The [RESOLUTION-340-2004-GAP-ANALYSIS.md](RESOLUTION-340-2004-GAP-ANALYSIS.md) provides:
- Complete requirement breakdown
- Implementation status for each requirement
- Evidence references to code locations
- Outstanding items and roadmap

### Detailed Analysis

The [RESOLUTION_340_2004_DETAILED_ANALYSIS.md](RESOLUTION_340_2004_DETAILED_ANALYSIS.md) includes:
- Implementation evidence for each requirement
- Code snippets and file references
- Test results and coverage
- Compliance certification

### Roadmap

The [ROADMAP-RESOLUCION-340-2004-CUBA.md](ROADMAP-RESOLUCION-340-2004-CUBA.md) outlines:
- Implementation phases
- Timeline and milestones
- Priority requirements
- Future enhancements

---

## 🚀 Deployment

### Docker

The [DOCKER.md](DOCKER.md) guide covers:
- Docker image creation
- Container orchestration
- Environment configuration
- Production deployment considerations

---

## 👥 User Guide

The [USER_GUIDE.md](USER_GUIDE.md) provides:
- Installation instructions
- Feature walkthrough
- Common workflows
- Troubleshooting tips

---

## 📝 Contributing

Before contributing, please review:

1. [COMMIT_GUIDELINES.md](../COMMIT_GUIDELINES.md) - Commit message format (Conventional Commits in English)
2. [ARCHITECTURE.md](ARCHITECTURE.md) - System architecture and coding standards
3. Code style - Follow existing patterns in the codebase

### Commit Format

All commits must follow the Conventional Commits format in English:

```
type(scope): description in English without final period
```

Examples:
- `feat(accounting): add journal entry validation`
- `fix(reporting): resolve date filtering in balance sheet`
- `docs(readme): update module status table`
- `test(consolidation): add multi-tenant tests`

---

## 📊 Project Statistics

- **Tests**: 385+ unit tests (JUnit 5)
- **Modules**: 13 business modules
- **Packages**: Modular architecture with clear separation
- **Database Support**: H2 (dev/test), PostgreSQL (production)
- **Export Formats**: PDF (Apache PDFBox), Excel (Apache POI)

---

## 🔗 Quick Links

- **GitHub Repository**: [yasmramos/EconovaFX](https://github.com/yasmramos/EconovaFX)
- **Main Branch**: `develop`
- **Issue Tracker**: GitHub Issues
- **License**: MIT License

---

**Last Updated**: December 2025  
**Maintainer**: yasmramos  
**Contact**: yasmramos95@gmail.com
