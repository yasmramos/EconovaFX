# Commit Guidelines - Conventional Commits

This repository enforces **strict Conventional Commits** format for all commit messages.

## Format

```
<type>(<scope>): <description>
```

## Allowed Types

| Type | Description |
|------|-------------|
| `feat` | A new feature |
| `fix` | A bug fix |
| `docs` | Documentation only changes |
| `style` | Changes that do not affect the meaning (white-space, formatting, missing semi-colons, etc) |
| `refactor` | A code change that neither fixes a bug nor adds a feature |
| `perf` | A code change that improves performance |
| `test` | Adding missing tests or correcting existing tests |
| `chore` | Changes to the build process or auxiliary tools and libraries |
| `revert` | Reverts a previous commit |

## Rules

1. **Language**: All commit messages must be written in **English**
2. **Imperative mood**: Use "Add" not "Added", "Fix" not "Fixed"
3. **No period**: Do not end the subject line with a period
4. **Length limit**: Subject line must be 72 characters or less
5. **Lowercase type**: The type must be lowercase (e.g., `feat` not `FEAT`)
6. **Scope**: Optional, use lowercase alphanumeric with hyphens/underscores
7. **Spacing**: Required space after the colon

## Valid Examples

```bash
feat(auth): add user login functionality
fix(api): resolve null pointer exception in user service
docs(readme): update installation instructions
style(ui): format button component
refactor(core): simplify validation logic
perf(database): optimize query performance for user lookup
test(unit): add tests for payment service
chore(deps): update dependency versions
revert: revert "feat(auth): add social login"
```

## Invalid Examples

```bash
# ❌ Uppercase type
FEAT(auth): add user login

# ❌ Wrong tense (not imperative)
fixed(api): resolve null pointer

# ❌ Period at the end
docs(readme): update installation instructions.

# ❌ Missing space after colon
fix(api):resolve null pointer

# ❌ Invalid type
update(deps): update dependencies

# ❌ Too long subject line (>72 chars)
feat(very-long-scope-name-that-makes-the-subject-line-exceed-the-maximum-length): description
```

## Automated Validation

A Git hook (`commit-msg`) automatically validates all commits. If your commit message doesn't follow the format, the commit will be rejected with helpful error messages.

## Setup Verification

Run the following to verify your setup:

```bash
git config user.name
git config user.email
git config credential.helper
git config commit.template
```

## Security Notes

- Your GitHub token is stored securely in `~/.git-credentials`
- File permissions are set to 600 (owner read/write only)
- Never commit `.git-credentials` or share your token
- If compromised, revoke immediately on GitHub Settings → Developer settings → Personal access tokens
