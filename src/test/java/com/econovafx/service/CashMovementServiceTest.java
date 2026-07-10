package com.econovafx.service;

import com.econovafx.modules.cash.model.CashMovement;
import com.econovafx.modules.cash.service.CashMovementService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for CashMovementService.
 */
public class CashMovementServiceTest {
    
    private CashMovementService service;
    private CashMovement movement;

    @BeforeEach
    public void setUp() {
        service = new CashMovementService();
        movement = new CashMovement();
        movement.setMovementType(CashMovement.MovementType.INCOME);
        movement.setAmount(new BigDecimal("1000.00"));
        movement.setDate(LocalDate.now());
        movement.setDescription("Test income");
        movement.setDestinationAccountId(1L);
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
