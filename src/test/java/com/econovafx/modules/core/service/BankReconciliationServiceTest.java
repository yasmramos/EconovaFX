package com.econovafx.modules.core.service;

import com.econovafx.modules.bank.model.BankReconciliation;
import com.econovafx.modules.bank.model.ReconciliationItem;
import com.econovafx.modules.bank.repository.BankReconciliationRepository;
import com.econovafx.modules.bank.service.BankReconciliationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * Unit tests for BankReconciliationService.
 */
public class BankReconciliationServiceTest {
    
    private BankReconciliationService service;
    private BankReconciliation reconciliation;
    private BankReconciliationRepository repository;

    @BeforeEach
    public void setUp() {
        repository = Mockito.mock(BankReconciliationRepository.class);
        service = new BankReconciliationService(repository);
        
        reconciliation = new BankReconciliation();
        reconciliation.setBankAccountId(1L);
        reconciliation.setStatementDate(LocalDate.now().minusMonths(1));
        reconciliation.setBankBalance(new BigDecimal("10000.00"));
        reconciliation.setSystemBalance(new BigDecimal("9500.00"));
        
        // Mock save to return the same object with an ID
        when(repository.save(any(BankReconciliation.class))).thenAnswer(invocation -> {
            BankReconciliation r = invocation.getArgument(0);
            if (r.getId() == null) {
                r.setId(System.nanoTime());
            }
            return r;
        });
        
        when(repository.findById(any(Long.class))).thenReturn(Optional.of(reconciliation));
        when(repository.findAll()).thenReturn(List.of(reconciliation));
        when(repository.findByBankAccountId(any(Long.class))).thenReturn(List.of(reconciliation));
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
        
        // System balance needs to be 11500 so that 11500 - 1000 (outstanding check) = 10500
        // Outstanding checks are subtracted from system balance to match bank balance
        ReconciliationItem systemItem = new ReconciliationItem();
        systemItem.setDescription("Outstanding check");
        systemItem.setAmount(new BigDecimal("1000.00"));
        systemItem.setDate(LocalDate.now());
        service.addSystemItem(created.getId(), systemItem);
        
        // Adjust system balance to match: 11500 - 1000 = 10500 (matches bank: 10000 + 500)
        created.setSystemBalance(new BigDecimal("11500.00"));
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
        assertNotNull(completed.getReconciledBalance());
    }

    @Test
    public void testCompleteUnbalancedReconciliation() {
        BankReconciliation created = service.createReconciliation(reconciliation);
        
        assertThrows(IllegalStateException.class, () -> {
            service.completeReconciliation(created.getId(), "testUser");
        });
    }

    @Test
    public void testGetReconciliationById() {
        BankReconciliation created = service.createReconciliation(reconciliation);
        
        var found = service.getReconciliation(created.getId());
        
        assertTrue(found.isPresent());
        assertEquals(created.getId(), found.get().getId());
    }

    @Test
    public void testGetAllReconciliations() {
        service.createReconciliation(reconciliation);
        
        var all = service.getAllReconciliations();
        
        assertTrue(all.size() >= 1);
    }

    @Test
    public void testGetReconciliationsByBankAccount() {
        BankReconciliation created = service.createReconciliation(reconciliation);
        
        var byBank = service.getByBankAccount(1L);
        
        assertTrue(byBank.stream().anyMatch(r -> r.getId().equals(created.getId())));
    }
}
