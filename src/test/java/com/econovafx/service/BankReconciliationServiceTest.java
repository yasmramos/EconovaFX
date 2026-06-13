package com.econovafx.service;

import com.econovafx.model.BankReconciliation;
import com.econovafx.model.ReconciliationItem;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for BankReconciliationService.
 */
public class BankReconciliationServiceTest {
    
    private BankReconciliationService service;
    private BankReconciliation reconciliation;

    @BeforeEach
    public void setUp() {
        service = new BankReconciliationService();
        reconciliation = new BankReconciliation();
        reconciliation.setBankAccountId(1L);
        reconciliation.setStatementDate(LocalDate.now().minusMonths(1));
        reconciliation.setBankBalance(new BigDecimal("10000.00"));
        reconciliation.setSystemBalance(new BigDecimal("9500.00"));
    }

    @Test
    public void testCreateReconciliation() {
        BankReconciliation created = service.createReconciliation(reconciliation);
        
        assertNotNull(created.getId());
        assertEquals(BankReconciliation.Status.IN_PROGRESS, created.getStatus());
        assertEquals(1L, created.getBankAccountId());
    }

    @Test
    public void testCreateReconciliationWithoutBankAccount() {
        BankReconciliation invalid = new BankReconciliation();
        invalid.setStatementDate(LocalDate.now());
        
        assertThrows(IllegalArgumentException.class, () -> {
            service.createReconciliation(invalid);
        });
    }

    @Test
    public void testAddBankItem() {
        BankReconciliation created = service.createReconciliation(reconciliation);
        
        ReconciliationItem item = new ReconciliationItem();
        item.setDescription("Deposit in transit");
        item.setAmount(new BigDecimal("500.00"));
        item.setDate(LocalDate.now());
        
        BankReconciliation updated = service.addBankItem(created.getId(), item);
        
        assertEquals(1, updated.getBankItems().size());
        assertEquals(ReconciliationItem.OriginType.BANK, updated.getBankItems().get(0).getOriginType());
    }

    @Test
    public void testAddSystemItem() {
        BankReconciliation created = service.createReconciliation(reconciliation);
        
        ReconciliationItem item = new ReconciliationItem();
        item.setDescription("Outstanding check");
        item.setAmount(new BigDecimal("1000.00"));
        item.setDate(LocalDate.now());
        
        BankReconciliation updated = service.addSystemItem(created.getId(), item);
        
        assertEquals(1, updated.getSystemItems().size());
        assertEquals(ReconciliationItem.OriginType.SYSTEM, updated.getSystemItems().get(0).getOriginType());
    }

    @Test
    public void testValidateReconciliationBalanced() {
        BankReconciliation created = service.createReconciliation(reconciliation);
        
        // Bank balance: 10000 + 500 (deposit in transit) = 10500
        ReconciliationItem bankItem = new ReconciliationItem();
        bankItem.setDescription("Deposit in transit");
        bankItem.setAmount(new BigDecimal("500.00"));
        bankItem.setDate(LocalDate.now());
        service.addBankItem(created.getId(), bankItem);
        
        // System balance: 9500 + 1000 (outstanding check not yet cleared) = 10500 adjusted
        ReconciliationItem systemItem = new ReconciliationItem();
        systemItem.setDescription("Outstanding check");
        systemItem.setAmount(new BigDecimal("1000.00"));
        systemItem.setDate(LocalDate.now());
        service.addSystemItem(created.getId(), systemItem);
        
        // Adjust balances to match
        created.setSystemBalance(new BigDecimal("10500.00"));
        service.createReconciliation(created);
        
        assertTrue(service.validateReconciliation(created.getId()));
    }

    @Test
    public void testCompleteReconciliation() {
        BankReconciliation created = service.createReconciliation(reconciliation);
        
        // Make balances match
        created.setSystemBalance(created.getBankBalance());
        service.createReconciliation(created);
        
        BankReconciliation completed = service.completeReconciliation(created.getId(), "testUser");
        
        assertEquals(BankReconciliation.Status.COMPLETED, completed.getStatus());
        assertEquals("testUser", completed.getCompletedBy());
        assertNotNull(completed.getCompletedAt());
    }

    @Test
    public void testCompleteUnbalancedReconciliation() {
        BankReconciliation created = service.createReconciliation(reconciliation);
        
        assertThrows(IllegalStateException.class, () -> {
            service.completeReconciliation(created.getId(), "testUser");
        });
    }
}
