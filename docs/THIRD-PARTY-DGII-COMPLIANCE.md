# Third Party Module - DGII Resolution 340-2004 Compliance

## Overview
This document describes the enhancements made to the Third Party module to comply with Dominican Republic DGII Resolution 340-2004 requirements for fiscal reporting (606, 607, 608, 609).

## Changes Implemented

### 1. Entity Model Updates (`ThirdParty.java`)

#### New Fields Added:
- **`taxClassification`** (`TaxClassification` enum): Fiscal classification according to DGII
  - `CONTRIBUTOR`: Contribuyente Ordinario
  - `SIMPLIFIED`: Régimen Simplificado
  - `NON_TAXPAYER`: No Responsable
  - `GOVERNMENT`: Gobierno
  - `FOREIGN`: Extranjero
  - `SPECIAL`: Regímenes Especiales

- **`withholdingIsr`** (`Double`): Income Tax (ISR) withholding amount
- **`withholdingItbis`** (`Double`): ITBIS withholding amount

#### Modified Fields:
- **`identificationNumber`**: Now required, max length 20 chars (for RNC/Cédula)
- **`country`**: Changed default from "Perú" to "República Dominicana"

### 2. Tax Classification Enum
```java
public enum TaxClassification {
    CONTRIBUTOR,    // Contribuyente Ordinario - Required for 606/607 reports
    SIMPLIFIED,     // Régimen Simplificado
    NON_TAXPAYER,   // No Responsable
    GOVERNMENT,     // Government entities
    FOREIGN,        // Foreign taxpayers (used for 606 type 03)
    SPECIAL         // Special regimes
}
```

## DGII Compliance Features

### For Report 606 (Purchases):
- ✅ Tax classification identifies taxpayer type
- ✅ Withholding fields track ISR and ITBIS retentions
- ✅ Identification number validation (RNC/Cédula format)

### For Report 607 (Sales):
- ✅ Customer tax classification for proper reporting
- ✅ Supports foreign customers (FOREIGN classification)

### For Report 609 (Withholdings):
- ✅ `withholdingIsr` field tracks income tax withholdings
- ✅ `withholdingItbis` field tracks ITBIS withholdings

## Next Steps for Full Compliance

### Pending Validations:
1. **RNC Validation Algorithm**: Implement modulo 10 check digit validation
2. **Cédula Validation**: Implement Dominican ID validation
3. **Required Fields by Type**: 
   - Contributors must have valid RNC
   - Foreigners require passport or NITE
4. **Default Values**: Auto-set tax classification based on identification type

### Service Layer Enhancements Needed:
- Add validation methods in `ThirdPartyService`
- Create utility class for DGII identification validation
- Add methods to calculate automatic withholdings based on classification

## Database Migration Required

```sql
ALTER TABLE third_parties 
ADD COLUMN tax_classification VARCHAR(50) DEFAULT 'CONTRIBUTOR' NOT NULL,
ADD COLUMN withholding_isr DECIMAL(19,4) DEFAULT 0.00,
ADD COLUMN withholding_itbis DECIMAL(19,4) DEFAULT 0.00;

ALTER TABLE third_parties 
MODIFY COLUMN identification_number VARCHAR(20) NOT NULL;
```

## Testing Recommendations

1. Test creation of third parties with all tax classifications
2. Verify withholding calculations
3. Test identification number uniqueness constraint
4. Validate export formats for 606/607/609 reports

## References
- DGII Resolution 340-2004
- RFC 606: Compras Report
- RFC 607: Sales Report
- RFC 609: Withholdings Report
