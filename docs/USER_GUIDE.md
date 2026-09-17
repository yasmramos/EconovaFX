# 👤 EconoNova FX - User Guide

## 📖 Table of Contents

1. [Getting Started](#getting-started)
2. [Installation](#installation)
3. [First Login](#first-login)
4. [Main Dashboard](#main-dashboard)
5. [Core Modules](#core-modules)
6. [Common Tasks](#common-tasks)
7. [Keyboard Shortcuts](#keyboard-shortcuts)
8. [Troubleshooting](#troubleshooting)

---

## 🚀 Getting Started

### Prerequisites

Before installing EconoNova FX, ensure you have:

- **Java JDK 17** or higher ([Download](https://adoptium.net/))
- **Maven 3.9+** (for building from source)
- At least **2GB RAM** available
- **1GB free disk space**

### System Requirements

| Component | Minimum | Recommended |
|-----------|---------|-------------|
| OS | Windows 10 / macOS 10.14 / Linux | Windows 11 / macOS 12+ / Ubuntu 22.04 |
| RAM | 4GB | 8GB |
| Disk Space | 500MB | 2GB |
| Java | JDK 17 | JDK 17 LTS |
| Screen Resolution | 1280x720 | 1920x1080 |

---

## 📥 Installation

### Option 1: Pre-built JAR (Recommended for Users)

1. Download the latest release from [Releases Page](https://github.com/yasmramos/econovafx/releases)
2. Run the application:
   ```bash
   java -jar econovafx-0.1.0.jar
   ```

### Option 2: Build from Source (Developers)

1. Clone the repository:
   ```bash
   git clone https://github.com/yasmramos/econovafx.git
   cd econovafx
   ```

2. Build with Maven:
   ```bash
   mvn clean package
   ```

3. Run the application:
   ```bash
   java -jar target/econovafx-0.1.0.jar
   ```

### Option 3: IDE Development

**IntelliJ IDEA:**
1. Open project from `pom.xml`
2. Configure Java SDK 17
3. Run `Main.java`

**Eclipse:**
1. Import as Maven Project
2. Set Java 17 compliance
3. Run `Main.java`

---

## 🔐 First Login

### Default Credentials

On first installation, use these default credentials:

| Field | Value |
|-------|-------|
| **Username** | `admin` |
| **Password** | *(leave empty)* |

⚠️ **IMPORTANT**: Change the default password immediately after first login!

### Login Screen

1. Launch the application
2. Enter your username and password
3. Click "Login" or press `Enter`
4. If successful, you'll be redirected to the Main Dashboard

### Password Reset (Admin Only)

If you forget your password:

1. Contact your system administrator
2. Admin can reset passwords via: **Settings → Users → Select User → Reset Password**

---

## 📊 Main Dashboard

### Overview

The dashboard provides a real-time overview of your accounting data:

```
┌─────────────────────────────────────────────────────┐
│  📊 ECONONOVA FX - Dashboard                        │
├─────────────────────────────────────────────────────┤
│                                                     │
│  ┌──────────┐  ┌──────────┐  ┌──────────┐          │
│  │ Accounts │  │ Entries  │  │ Period   │          │
│  │   156    │  │   1,234  │  │  Aug 2024│          │
│  │          │  │  (Month) │  │  [OPEN]  │          │
│  └──────────┘  └──────────┘  └──────────┘          │
│                                                     │
│  ┌─────────────────────────────────────────────┐   │
│  │ Recent Journal Entries                      │   │
│  │ ------------------------------------------- │   │
│  │ JE-2024-001  | Aug 12 | $5,000 | Validated │   │
│  │ JE-2024-002  | Aug 12 | $2,500 | Posted    │   │
│  │ JE-2024-003  | Aug 11 | $1,200 | Draft     │   │
│  └─────────────────────────────────────────────┘   │
│                                                     │
│  Quick Actions:                                     │
│  [New Entry] [View Reports] [Manage Accounts]      │
│                                                     │
└─────────────────────────────────────────────────────┘
```

### Dashboard Widgets

- **Total Accounts**: Number of accounts in chart of accounts
- **Monthly Entries**: Journal entries for current period
- **Current Period**: Active accounting period status
- **Recent Activity**: Last 10 journal entries
- **Quick Actions**: Fast access to common tasks

---

## 📚 Core Modules

### 1. Chart of Accounts

**Purpose**: Manage your company's account structure

**Access**: Main Menu → Accounting → Chart of Accounts

#### Creating an Account

1. Click **"New Account"** button
2. Fill in account details:
   - **Code**: Unique account code (e.g., "1.1.1")
   - **Name**: Account name (e.g., "Cash")
   - **Type**: Asset, Liability, Equity, Revenue, or Expense
   - **Parent Account**: Select parent for hierarchy
   - **Level**: Automatic based on hierarchy
3. Click **"Save"**

#### Account Types

| Type | Code Prefix | Normal Balance | Description |
|------|-------------|----------------|-------------|
| **Asset** | 1 | Debit | Resources owned |
| **Liability** | 2 | Credit | Obligations owed |
| **Equity** | 3 | Credit | Owner's interest |
| **Revenue** | 4 | Credit | Income earned |
| **Expense** | 5 | Debit | Costs incurred |

#### Editing/Deleting Accounts

- **Edit**: Select account → Click "Edit" → Modify → Save
- **Delete**: Select account → Click "Delete" → Confirm
  - ⚠️ Cannot delete accounts with transactions

---

### 2. Journal Entries

**Purpose**: Record accounting transactions using double-entry bookkeeping

**Access**: Main Menu → Accounting → Journal Entries

#### Creating a Journal Entry

1. Click **"New Entry"** button
2. Fill in header information:
   - **Date**: Transaction date
   - **Description**: Brief explanation
   - **Period**: Auto-selected (current period)
3. Add entry lines:
   - Click **"Add Line"**
   - Select **Account**
   - Enter **Debit** or **Credit** amount
   - Add **Description** (optional)
4. Ensure **Debits = Credits** (balanced)
5. Click **"Save"** (Draft status)
6. Click **"Validate"** to validate entry
7. Click **"Post"** to post to ledger

#### Entry Status Workflow

```
Draft → Validated → Posted → (Cannot be modified)
  ↓
Cancelled (if needed before posting)
```

| Status | Can Edit? | Can Delete? | Affects Ledger? |
|--------|-----------|-------------|-----------------|
| Draft | ✅ Yes | ✅ Yes | ❌ No |
| Validated | ✅ Yes | ✅ Yes | ❌ No |
| Posted | ❌ No | ❌ No | ✅ Yes |
| Cancelled | ❌ No | ❌ No | ❌ No |

#### Example: Recording a Sale

```
Date: Aug 12, 2024
Description: Sale of merchandise to Customer ABC

Line 1:
  Account: 1.1.1 - Cash
  Debit: $1,000
  Credit: $0

Line 2:
  Account: 4.1.1 - Sales Revenue
  Debit: $0
  Credit: $1,000

Total Debits: $1,000
Total Credits: $1,000
✓ Balanced
```

---

### 3. Period Control

**Purpose**: Manage accounting periods (monthly/yearly)

**Access**: Main Menu → Accounting → Periods

#### Opening a Period

1. Select the period (e.g., "August 2024")
2. Click **"Open Period"**
3. Confirm opening
4. Period status changes to **OPEN**
5. Journal entries can now be posted to this period

#### Closing a Period

1. Ensure all entries are posted
2. Select the period
3. Click **"Close Period"**
4. Confirm closure
5. Period status changes to **CLOSED**
6. No new entries can be posted to closed periods

#### Period Status

| Status | Color | Can Post? | Can Close? |
|--------|-------|-----------|------------|
| **Not Opened** | Gray | ❌ No | ✅ Yes |
| **Open** | Green | ✅ Yes | ✅ Yes |
| **Closed** | Red | ❌ No | ❌ No |

---

### 4. Third Parties

**Purpose**: Manage customers, suppliers, and employees

**Access**: Main Menu → CRM → Third Parties

#### Adding a Third Party

1. Click **"New Third Party"**
2. Select **Type**: Customer, Supplier, or Employee
3. Fill in details:
   - **Name**: Legal name
   - **Tax ID**: Tax identification number
   - **Email**: Contact email
   - **Phone**: Contact phone
   - **Address**: Physical address
4. Click **"Save"**

#### Third Party Types

| Type | Use Case | Example |
|------|----------|---------|
| **Customer** | Sell goods/services | Retail clients |
| **Supplier** | Purchase goods/services | Vendors |
| **Employee** | Staff records | Company employees |

---

### 5. Users & Security

**Purpose**: Manage system users and permissions

**Access**: Main Menu → Settings → Users (Admin only)

#### Creating a User

1. Click **"New User"**
2. Fill in user details:
   - **Username**: Unique login name
   - **Email**: User email
   - **Role**: Administrator, Accountant, Auditor, Viewer
   - **Active**: Checkbox to enable/disable
3. Set initial password
4. Click **"Save"**

#### User Roles

| Role | Permissions | Best For |
|------|-------------|----------|
| **Administrator** | Full access to all modules | IT Manager |
| **Accountant** | Full accounting access, no user management | Accounting Staff |
| **Auditor** | Read-only access to all data | External Auditor |
| **Viewer** | Read-only access to reports | Management |

---

### 6. Reports

**Purpose**: Generate financial reports

**Access**: Main Menu → Reports

#### Available Reports

1. **Trial Balance**
   - List of all accounts with debit/credit balances
   - Filter by period/date range
   - Export to PDF, Excel, CSV

2. **General Ledger**
   - Detailed transaction history by account
   - Chronological view of all entries

3. **Journal Entries Report**
   - List of all journal entries
   - Filter by status, date, period

4. **Financial Statements** (Future)
   - Balance Sheet
   - Income Statement
   - Cash Flow Statement

#### Generating a Report

1. Select report type
2. Choose filters (date range, period, account, etc.)
3. Click **"Generate"**
4. Preview report
5. Click **"Export"** to download (PDF/Excel/CSV)

---

## ⌨️ Keyboard Shortcuts

| Action | Windows/Linux | macOS |
|--------|---------------|-------|
| **Save** | `Ctrl + S` | `Cmd + S` |
| **New Entry** | `Ctrl + N` | `Cmd + N` |
| **Search** | `Ctrl + F` | `Cmd + F` |
| **Print/Export** | `Ctrl + P` | `Cmd + P` |
| **Refresh** | `F5` | `Cmd + R` |
| **Logout** | `Ctrl + Q` | `Cmd + Q` |
| **Help** | `F1` | `Cmd + ?` |
| **Confirm Action** | `Enter` | `Return` |
| **Cancel** | `Esc` | `Esc` |

---

## 🔧 Troubleshooting

### Common Issues

#### 1. Application Won't Start

**Symptoms**: Black screen or immediate crash

**Solutions**:
- Verify Java 17 is installed: `java --version`
- Check JavaFX is available
- Review log file: `logs/application.log`
- Ensure sufficient RAM (minimum 2GB free)

#### 2. Database Connection Error

**Symptoms**: "Cannot connect to database" error

**Solutions**:
- Close other instances of the application
- Check if H2 database file is corrupted
- Restore from backup if needed
- Contact administrator for production databases

#### 3. Cannot Post Journal Entry

**Symptoms**: "Posting failed" or entry remains in Draft

**Solutions**:
- Verify debits = credits (must balance)
- Check that period is OPEN
- Ensure all required fields are filled
- Verify user has posting permissions

#### 4. Forgot Password

**Solutions**:
- Contact system administrator
- Admin can reset via: Settings → Users → Reset Password
- Default admin password is empty (change immediately!)

#### 5. Slow Performance

**Solutions**:
- Close unused applications to free RAM
- Reduce date range in reports
- Archive old periods (future feature)
- Consider upgrading hardware

### Getting Help

- **Documentation**: Check `/docs` folder in repository
- **Logs**: Review `logs/application.log` for errors
- **GitHub Issues**: Report bugs at [github.com/yasmramos/econovafx/issues](https://github.com/yasmramos/econovafx/issues)
- **Email**: Contact yasmramos95@gmail.com for support

---

## 📞 Support

For technical support or questions:

- **Email**: yasmramos95@gmail.com
- **GitHub**: [Create an Issue](https://github.com/yasmramos/econovafx/issues)
- **Documentation**: See `/docs` folder

---

**Last Updated**: August 2024  
**Version**: 0.1.0  
**Maintainer**: yasmramos
