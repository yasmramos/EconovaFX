# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

### Added
- Initial project setup with JavaFX 17 and Ebean ORM 17
- Core accounting modules: Chart of Accounts, Journal Entries, Periods
- User management with role-based access control
- Third-party management (customers, suppliers, employees)
- Dashboard with key accounting metrics
- Exchange rate management
- H2 database integration for development
- Comprehensive test suite (385+ tests)
- Git hooks for commit validation (Conventional Commits)
- CI/CD pipeline with GitHub Actions
- Multi-company financial statement consolidation (Resolution 340/2004 II.18)
- Date-range filtering in financial statements with POSTED transaction filtering
- Financial reporting module with Balance Sheet, Income Statement, and Trial Balance generation
- Export to PDF and Excel formats (Apache PDFBox, Apache POI)
- PostgreSQL production database support

### Changed
- Updated .gitignore with comprehensive Java/Maven patterns
- Enhanced documentation structure
- Migrated from generic package structure to modular architecture (`com.econovafx.modules.<module>`)
- Updated test count from 268 to 385+ tests

### Fixed
- Java version consistency across README and pom.xml (Java 17 LTS)
- FinancialStatementService.calculateAccountBalances() now correctly filters by date range
- FinancialReportingService placeholder methods replaced with real implementation
- Documentation links corrected to point to existing files in docs/

### Deprecated
- Generic directory structure replaced by modular packages
- Transient implementation summaries moved to permanent documentation

## [0.2.0] - 2024-12-15

### Added
- Financial reporting consolidation for multi-tenant environments
- Date-range filtering for all financial statements
- Transaction status filtering (POSTED only)
- ConsolidationService with multi-tenant orchestration
- ConsolidatedStatementResult and ConsolidatedRow models
- 11 new unit tests for financial reporting (100% pass rate)

### Changed
- FinancialStatementService refactored to use TransactionRepository for balance calculations
- FinancialReportingService methods implemented with real transaction-based calculations
- Resolution 340/2004 compliance documentation updated (Requirement II.18 marked COMPLETE)

### Fixed
- Placeholder methods in FinancialReportingService replaced with actual implementations
- Account balance calculation now uses transaction history instead of static Account.getBalance()
- Multi-tenant context management with proper ThreadLocal cleanup

## [0.1.0] - 2024-08-12

### Added
- Initial release
- Basic accounting functionality
- Chart of accounts management
- Journal entry creation and validation
- Period control (open/close)
- User authentication and authorization
- Third-party registry
- Basic reporting capabilities

---

## Version History

### Versioning Scheme
- **MAJOR.MINOR.PATCH** (e.g., 1.0.0)
- MAJOR: Breaking changes
- MINOR: New features (backward compatible)
- PATCH: Bug fixes (backward compatible)

### Release Notes Template

```markdown
## [X.Y.Z] - YYYY-MM-DD

### Added
- New features

### Changed
- Changes in existing functionality

### Deprecated
- Soon-to-be removed features

### Removed
- Removed features

### Fixed
- Bug fixes

### Security
- Security improvements
```
