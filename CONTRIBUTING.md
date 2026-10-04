# Contributing to EconovaFX

Thank you for your interest in contributing! This document defines the conventions every contributor must follow. These rules are **mandatory** and enforced on pull requests.

## Branch Strategy

- All feature work and pull requests target the **`develop`** branch.
- `main` is reserved for stable releases only.
- Keep your branch up to date with `develop` before opening a PR (`git rebase develop` or merge).

## Commit Message Format (Conventional Commits)

All commits **must** follow the [Conventional Commits](https://www.conventionalcommits.org/) specification, written **in English**:

```
<type>[optional scope]: <description>

[optional body]
```

### Allowed types

| Type       | Purpose                                                |
|------------|--------------------------------------------------------|
| `feat`     | A new feature                                          |
| `fix`      | A bug fix                                              |
| `docs`     | Documentation-only changes                             |
| `style`    | Formatting, missing semicolons, etc. (no logic change) |
| `refactor` | Code change that neither fixes a bug nor adds a feature|
| `perf`     | Performance improvement                                |
| `test`     | Adding or fixing tests                                 |
| `build`    | Build system or dependency changes (e.g. `pom.xml`)    |
| `ci`       | CI configuration and scripts                           |
| `chore`    | Maintenance tasks (no source or test changes)          |
| `revert`   | Reverting a previous commit                            |

### Rules

- Use the **imperative mood** in the description ("add", not "added").
- Do **not** capitalize the description; no trailing period.
- Scope is optional but encouraged: the module name (e.g. `accounting`, `billing`, `inventory`).
- Breaking changes: append `!` after the type/scope (`feat(reporting)!: ...`) or add `BREAKING CHANGE:` in the footer.
- The pre-commit hook rejects messages that do not match this format.

### Examples

```text
feat(billing): add sequential invoice numbering per series
fix(accounting): prevent unbalanced journal entry on multi-currency posting
docs(readme): update GraalVM native build instructions
build(gluonfx): set native executable name to econovafx
ci(native-image): migrate workflow to unified GraalVM distribution scheme
test(inventory): cover stock reservation under concurrent tenants
```

## Language Policy: English Only

To keep the codebase consistent and accessible to all contributors:

- **Code**: identifiers (classes, methods, variables, packages) must be in English.
- **Comments**: inline comments, block comments, and TODO/FIXME notes must be in English.
- **Javadoc**: all Javadoc must be written in English.
- **Documentation**: README, guides, and any Markdown files must be in English.
- **Commit messages and PR titles/descriptions**: English.
- **User-facing strings**: application UI text may remain localized (Spanish) as it is end-user content, not developer documentation.

When touching existing files that contain non-English comments, translate them to English as part of your change.

## Development Workflow

1. Clone and install hooks:
   ```bash
   git clone https://github.com/yasmramos/EconovaFX.git
   cd EconovaFX
   git checkout develop
   git config core.hooksPath .githooks
   ```
2. Create a branch from `develop`: `feat/<short-name>` or `fix/<short-name>`.
3. Follow the project architecture (see README: Ebean entities, Avaje Inject controllers, FXML naming `<Name>Controller.fxml`).
4. Write tests for new behavior (JUnit 5 + Mockito; headless JavaFX via Monocle where needed).
5. Run locally before pushing:
   ```bash
   mvn clean verify
   ```
6. Commit using Conventional Commits (English), push, and open a PR against `develop`.

## Code Style Reminders

- Multi-currency amounts use `MoneyValue`; never raw `BigDecimal` for stored monetary fields.
- Every tenant-owned entity extends `TenantAwareEntity`; queries go through `EconovaFxContext`/`DefaultTenantContextProvider`.
- Services are registered via Avaje Inject custom scopes (`ServiceCustomScope`); avoid manual singletons.
- Native image builds require reflection resources to be listed in `reflect-config.json` (Gson serializers included automatically by default config).
