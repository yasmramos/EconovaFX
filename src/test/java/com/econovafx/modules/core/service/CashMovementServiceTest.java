package com.econovafx.modules.core.service;

import com.econovafx.modules.cash.model.CashMovement;
import com.econovafx.modules.cash.service.CashMovementService;
import com.econovafx.modules.cash.repository.CashMovementRepository;
import com.econovafx.modules.bank.repository.BankAccountRepository;
import com.econovafx.modules.cash.repository.CashBoxRepository;
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
 * Unit tests for CashMovementService.
 */
public class CashMovementServiceTest {
    
    private CashMovementService service;
    private CashMovement movement;
    private CashMovementRepository movementRepository;
    private BankAccountRepository bankAccountRepository;
    private CashBoxRepository cashBoxRepository;

    @BeforeEach
    public void setUp() {
        movementRepository = Mockito.mock(CashMovementRepository.class);
        bankAccountRepository = Mockito.mock(BankAccountRepository.class);
        cashBoxRepository = Mockito.mock(CashBoxRepository.class);
        
        service = new CashMovementService(movementRepository, bankAccountRepository, cashBoxRepository);
        
        movement = new CashMovement();
        movement.setMovementType(CashMovement.MovementType.INCOME);
        movement.setAmount(new BigDecimal("1000.00"));
        movement.setDate(LocalDate.now());
        movement.setDescription("Test income");
        movement.setDestinationAccountId(1L);
        
        // Mock save to return the same object with an ID
        when(movementRepository.save(any(CashMovement.class))).thenAnswer(invocation -> {
            CashMovement m = invocation.getArgument(0);
            if (m.getId() == null) {
                // Simulate ID generation
                m.setId(System.nanoTime());
            }
            return m;
        });
        
        when(movementRepository.findById(any(Long.class))).thenReturn(Optional.of(movement));
        when(movementRepository.findByAccountId(any(Long.class))).thenReturn(List.of(movement));
    }

    @Test
    public void testRegisterIncomeMovement() {
        CashMovement created = service.registerMovement(movement, "testUser");
        
        assertNotNull(created.getId());
        assertEquals(CashMovement.Status.PENDING, created.getStatus());
        // createdBy se maneja automáticamente vía @TenantId y audit
        assertEquals(new BigDecimal("1000.00"), created.getAmount());
    }

    @Test
    public void testRegisterMovementWithInvalidAmount() {
        CashMovement invalid = new CashMovement();
        invalid.setMovementType(CashMovement.MovementType.INCOME);
        invalid.setAmount(BigDecimal.ZERO);
        invalid.setDate(LocalDate.now());
        
        assertThrows(IllegalArgumentException.class, () -> {
            service.registerMovement(invalid, "testUser");
        });
    }

    @Test
    public void testRegisterTransferMovement() {
        CashMovement transfer = new CashMovement();
        transfer.setMovementType(CashMovement.MovementType.TRANSFER);
        transfer.setAmount(new BigDecimal("500.00"));
        transfer.setDate(LocalDate.now());
        transfer.setSourceAccountId(1L);
        transfer.setDestinationAccountId(2L);
        
        CashMovement created = service.registerMovement(transfer, "testUser");
        
        assertNotNull(created.getId());
        assertEquals(CashMovement.MovementType.TRANSFER, created.getMovementType());
        assertEquals(1L, created.getSourceAccountId());
        assertEquals(2L, created.getDestinationAccountId());
    }

    @Test
    public void testRegisterTransferWithoutAccounts() {
        CashMovement invalid = new CashMovement();
        invalid.setMovementType(CashMovement.MovementType.TRANSFER);
        invalid.setAmount(new BigDecimal("500.00"));
        invalid.setDate(LocalDate.now());
        
        assertThrows(IllegalArgumentException.class, () -> {
            service.registerMovement(invalid, "testUser");
        });
    }

    @Test
    public void testPostMovement() {
        CashMovement created = service.registerMovement(movement, "testUser");
        
        CashMovement posted = service.postMovement(created.getId(), "adminUser");
        
        assertEquals(CashMovement.Status.POSTED, posted.getStatus());
        assertEquals("adminUser", posted.getPostedBy());
        assertNotNull(posted.getPostedAt());
    }

    @Test
    public void testCancelPendingMovement() {
        CashMovement created = service.registerMovement(movement, "testUser");
        
        CashMovement cancelled = service.cancelMovement(created.getId(), "adminUser");
        
        assertEquals(CashMovement.Status.CANCELLED, cancelled.getStatus());
    }

    @Test
    public void testGetMovementsByAccount() {
        service.registerMovement(movement, "testUser");
        
        var movements = service.getMovementsByAccount(1L);
        
        assertFalse(movements.isEmpty());
        assertEquals(1, movements.size());
    }
}
