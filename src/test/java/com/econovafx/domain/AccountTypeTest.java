package com.econovafx.domain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for AccountType enum
 */
class AccountTypeTest {

    @Test
    void testAssetDisplayName() {
        assertEquals("Activo", AccountType.ASSET.getDisplayName());
    }

    @Test
    void testLiabilityDisplayName() {
        assertEquals("Pasivo", AccountType.LIABILITY.getDisplayName());
    }

    @Test
    void testEquityDisplayName() {
        assertEquals("Patrimonio", AccountType.EQUITY.getDisplayName());
    }

    @Test
    void testRevenueDisplayName() {
        assertEquals("Ingreso", AccountType.REVENUE.getDisplayName());
    }

    @Test
    void testExpenseDisplayName() {
        assertEquals("Gasto", AccountType.EXPENSE.getDisplayName());
    }

    @Test
    void testAllAccountTypesExist() {
        AccountType[] types = AccountType.values();
        assertEquals(5, types.length);
        assertArrayEquals(
            new AccountType[]{AccountType.ASSET, AccountType.LIABILITY, AccountType.EQUITY, 
                            AccountType.REVENUE, AccountType.EXPENSE},
            types
        );
    }
}
