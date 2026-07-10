package com.econovafx.modules.cash.service;
import io.avaje.inject.Component;
import com.econovafx.modules.core.security.RequiresTenant;
import com.econovafx.modules.accounting.model.AccountingPeriod;

import com.econovafx.modules.cash.model.CashMovement;
import com.econovafx.modules.bank.model.BankAccount;
import com.econovafx.modules.cash.model.CashBox;
import com.econovafx.modules.core.repository.CashMovementRepository;
import com.econovafx.modules.core.repository.BankAccountRepository;
import com.econovafx.modules.core.repository.CashBoxRepository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Service for managing Cash Movements with Accounting integration.
 */
@Component
@RequiresTenant
public class CashMovementService {
    
    private final CashMovementRepository movementRepository = new CashMovementRepository();
    private final BankAccountRepository bankAccountRepository = new BankAccountRepository();
    private final CashBoxRepository cashBoxRepository = new CashBoxRepository();

    public CashMovement registerMovement(CashMovement movement, String currentUser) {
        validateMovement(movement);
        
        // createdBy se maneja automáticamente vía @TenantId y audit
        movement.setStatus(CashMovement.Status.PENDING);
        
        return movementRepository.save(movement);
    }

    private void validateMovement(CashMovement movement) {
        if (movement.getAmount() == null || movement.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Amount must be greater than zero");
        }
        if (movement.getDate() == null) {
            throw new IllegalArgumentException("Date is required");
        }
        if (movement.getMovementType() == null) {
            throw new IllegalArgumentException("Movement type is required");
        }
        
        if (movement.getMovementType() == CashMovement.MovementType.TRANSFER) {
            if (movement.getSourceAccountId() == null || movement.getDestinationAccountId() == null) {
                throw new IllegalArgumentException("Transfer requires source and destination accounts");
            }
        } else {
            if (movement.getSourceAccountId() == null && movement.getDestinationAccountId() == null) {
                throw new IllegalArgumentException("Source or destination account is required");
            }
        }
    }

    public CashMovement postMovement(Long movementId, String currentUser) {
        Optional<CashMovement> optional = movementRepository.findById(movementId);
        if (optional.isEmpty()) {
            throw new IllegalArgumentException("Movement not found");
        }
        
        CashMovement movement = optional.get();
        if (movement.getStatus() != CashMovement.Status.PENDING) {
            throw new IllegalStateException("Only pending movements can be posted");
        }
        
        updateBalances(movement);
        
        movement.setStatus(CashMovement.Status.POSTED);
        movement.setPostedBy(currentUser);
        movement.setPostedAt(LocalDateTime.now());
        
        // Here would be the integration with Accounting module
        // createAccountingEntry(movement);
        
        return movementRepository.save(movement);
    }

    private void updateBalances(CashMovement movement) {
        BigDecimal amount = movement.getAmount();
        
        if (movement.getSourceAccountId() != null) {
            updateAccountBalance(movement.getSourceAccountId(), amount.negate());
        }
        
        if (movement.getDestinationAccountId() != null) {
            updateAccountBalance(movement.getDestinationAccountId(), amount);
        }
    }

    private void updateAccountBalance(Long accountId, BigDecimal change) {
        Optional<BankAccount> bankOpt = bankAccountRepository.findById(accountId);
        if (bankOpt.isPresent()) {
            BankAccount account = bankOpt.get();
            account.setBalance(account.getBalance().add(change));
            bankAccountRepository.save(account);
            return;
        }
        
        Optional<CashBox> boxOpt = cashBoxRepository.findById(accountId);
        if (boxOpt.isPresent()) {
            CashBox box = boxOpt.get();
            box.setBalance(box.getBalance().add(change));
            cashBoxRepository.save(box);
        }
    }

    public CashMovement cancelMovement(Long movementId, String currentUser) {
        Optional<CashMovement> optional = movementRepository.findById(movementId);
        if (optional.isEmpty()) {
            throw new IllegalArgumentException("Movement not found");
        }
        
        CashMovement movement = optional.get();
        if (movement.getStatus() == CashMovement.Status.CANCELLED) {
            throw new IllegalStateException("Movement is already cancelled");
        }
        if (movement.getReconciled()) {
            throw new IllegalStateException("Cannot cancel a reconciled movement");
        }
        
        if (movement.getStatus() == CashMovement.Status.POSTED) {
            reverseBalances(movement);
        }
        
        movement.setStatus(CashMovement.Status.CANCELLED);
        // updatedBy se maneja automáticamente vía audit
        
        return movementRepository.save(movement);
    }

    private void reverseBalances(CashMovement movement) {
        BigDecimal amount = movement.getAmount().negate();
        
        if (movement.getSourceAccountId() != null) {
            updateAccountBalance(movement.getSourceAccountId(), amount);
        }
        
        if (movement.getDestinationAccountId() != null) {
            updateAccountBalance(movement.getDestinationAccountId(), amount);
        }
    }

    public List<CashMovement> getMovementsByAccount(Long accountId) {
        return movementRepository.findByAccountId(accountId);
    }

    public List<CashMovement> getPendingMovements() {
        return movementRepository.findPendingMovements();
    }

    public Optional<CashMovement> getMovement(Long id) {
        return movementRepository.findById(id);
    }

    /**
     * Check if cash/bank module is closed for a specific accounting period.
     * Resolution 340/2004: Required for accounting period closure validation.
     */
    public boolean isModuleClosedForPeriod(AccountingPeriod period) {
        // Check if there are any unposted movements in the period
        List<CashMovement> movements = movementRepository.findByDateRange(
            period.getStartDate(), 
            period.getEndDate()
        );
        
        // Module is considered "closed" if all movements are posted
        return movements.stream().allMatch(m -> m.getPostedAt() != null);
    }
}
