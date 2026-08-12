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
- Comprehensive test suite (268+ tests)
- Git hooks for commit validation (Conventional Commits)
- CI/CD pipeline with GitHub Actions

### Changed
- Updated .gitignore with comprehensive Java/Maven patterns
- Enhanced documentation structure

### Fixed
- Java version consistency across README and pom.xml (Java 17 LTS)

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
