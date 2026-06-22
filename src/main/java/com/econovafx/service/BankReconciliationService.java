package com.econovafx.service;

import com.econovafx.model.BankReconciliation;
import com.econovafx.model.ReconciliationItem;
import com.econovafx.repository.BankReconciliationRepository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * Service for Bank Reconciliation according to Resolution 340/2004.
 * Supports any reconciliation method (adjusted balance or direct).
 */
public class BankReconciliationService {
    
    private final BankReconciliationRepository repository = new BankReconciliationRepository();

    public BankReconciliation createReconciliation(BankReconciliation reconciliation) {
        if (reconciliation.getBankAccountId() == null) {
            throw new IllegalArgumentException("Bank account is required");
        }
        if (reconciliation.getStatementDate() == null) {
            throw new IllegalArgumentException("Statement date is required");
        }
        return repository.save(reconciliation);
    }

    public BankReconciliation addBankItem(Long reconciliationId, ReconciliationItem item) {
        Optional<BankReconciliation> optional = repository.findById(reconciliationId);
        if (optional.isEmpty()) {
            throw new IllegalArgumentException("Reconciliation not found");
        }
        BankReconciliation reconciliation = optional.get();
        if (reconciliation.getStatus() != BankReconciliation.Status.IN_PROGRESS) {
            throw new IllegalStateException("Cannot modify a completed or cancelled reconciliation");
        }
        // No need to set reconciliationId, the relationship is handled via @ManyToOne
        reconciliation.addBankItem(item);
        return repository.save(reconciliation);
    }

    public BankReconciliation addSystemItem(Long reconciliationId, ReconciliationItem item) {
        Optional<BankReconciliation> optional = repository.findById(reconciliationId);
        if (optional.isEmpty()) {
            throw new IllegalArgumentException("Reconciliation not found");
        }
        BankReconciliation reconciliation = optional.get();
        if (reconciliation.getStatus() != BankReconciliation.Status.IN_PROGRESS) {
            throw new IllegalStateException("Cannot modify a completed or cancelled reconciliation");
        }
        // No need to set reconciliationId, the relationship is handled via @ManyToOne
        reconciliation.addSystemItem(item);
        return repository.save(reconciliation);
    }

    public boolean validateReconciliation(Long reconciliationId) {
        Optional<BankReconciliation> optional = repository.findById(reconciliationId);
        if (optional.isEmpty()) {
            return false;
        }
        BankReconciliation rec = optional.get();
        
        BigDecimal adjustedBankBalance = rec.getBankBalance();
        for (ReconciliationItem item : rec.getBankItems()) {
            adjustedBankBalance = adjustedBankBalance.add(item.getAmount());
        }
        
        BigDecimal adjustedSystemBalance = rec.getSystemBalance();
        for (ReconciliationItem item : rec.getSystemItems()) {
            adjustedSystemBalance = adjustedSystemBalance.subtract(item.getAmount());
        }
        
        return adjustedBankBalance.compareTo(adjustedSystemBalance) == 0;
    }

    public BankReconciliation completeReconciliation(Long reconciliationId, String completedBy) {
        Optional<BankReconciliation> optional = repository.findById(reconciliationId);
        if (optional.isEmpty()) {
            throw new IllegalArgumentException("Reconciliation not found");
        }
        BankReconciliation rec = optional.get();
        
        if (!validateReconciliation(reconciliationId)) {
            throw new IllegalStateException("Reconciliation does not balance. Cannot complete.");
        }
        
        rec.setStatus(BankReconciliation.Status.COMPLETED);
        rec.setCompletedBy(completedBy);
        rec.setCompletedAt(java.time.LocalDateTime.now());
        rec.setReconciledBalance(rec.getSystemBalance());
        
        return repository.save(rec);
    }

    public Optional<BankReconciliation> getReconciliation(Long id) {
        return repository.findById(id);
    }

    public List<BankReconciliation> getAllReconciliations() {
        return repository.findAll();
    }

    public List<BankReconciliation> getByBankAccount(Long bankAccountId) {
        return repository.findByBankAccountId(bankAccountId);
    }
}
