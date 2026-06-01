package com.econovafx.service;

import com.econovafx.domain.AccountingPeriod;
import com.econovafx.repository.AccountingPeriodRepository;
import io.avaje.inject.Component;
import jakarta.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Service for managing accounting periods and closing operations.
 */
@Component
public class AccountingPeriodService {

    private static final Logger log = LoggerFactory.getLogger(AccountingPeriodService.class);

    @Inject
    AccountingPeriodRepository repository;

    /**
     * Get all accounting periods.
     */
    public List<AccountingPeriod> getAllPeriods() {
        return repository.findAll();
    }

    /**
     * Get a period by ID.
     */
    public Optional<AccountingPeriod> getPeriodById(Long id) {
        return repository.findById(id);
    }

    /**
     * Get the current open period.
     */
    public Optional<AccountingPeriod> getCurrentOpenPeriod() {
        return repository.findCurrentOpenPeriod();
    }

    /**
     * Find the period that contains a specific date.
     */
    public Optional<AccountingPeriod> getPeriodByDate(LocalDate date) {
        return repository.findPeriodByDate(date);
    }

    /**
     * Create a new accounting period.
     */
    public AccountingPeriod createPeriod(String name, LocalDate startDate, LocalDate endDate) {
        // Validate dates
        if (startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("Start date must be before end date");
        }

        // Check for overlapping periods
        List<AccountingPeriod> existing = repository.findAll();
        for (AccountingPeriod period : existing) {
            if (!(endDate.isBefore(period.getStartDate()) || startDate.isAfter(period.getEndDate()))) {
                throw new IllegalArgumentException("Period overlaps with existing period: " + period.getName());
            }
        }

        AccountingPeriod period = new AccountingPeriod(name, startDate, endDate);
        return repository.save(period);
    }

    /**
     * Close an accounting period.
     */
    public AccountingPeriod closePeriod(Long periodId, String closedBy) {
        Optional<AccountingPeriod> periodOpt = repository.findById(periodId);
        if (periodOpt.isEmpty()) {
            throw new IllegalArgumentException("Period not found with ID: " + periodId);
        }

        AccountingPeriod period = periodOpt.get();
        if (!period.isOpen()) {
            throw new IllegalStateException("Period is already closed or locked: " + period.getName());
        }

        period.setStatus(AccountingPeriod.PeriodStatus.CLOSED);
        period.setClosedBy(closedBy);
        period.setClosedDate(LocalDate.now());
        
        return repository.save(period);
    }

    /**
     * Lock an accounting period (prevent reopening).
     */
    public AccountingPeriod lockPeriod(Long periodId) {
        Optional<AccountingPeriod> periodOpt = repository.findById(periodId);
        if (periodOpt.isEmpty()) {
            throw new IllegalArgumentException("Period not found with ID: " + periodId);
        }

        AccountingPeriod period = periodOpt.get();
        if (period.isOpen()) {
            throw new IllegalStateException("Cannot lock an open period: " + period.getName());
        }

        period.setStatus(AccountingPeriod.PeriodStatus.LOCKED);
        return repository.save(period);
    }

    /**
     * Reopen a closed period (not allowed for locked periods).
     */
    public AccountingPeriod reopenPeriod(Long periodId) {
        Optional<AccountingPeriod> periodOpt = repository.findById(periodId);
        if (periodOpt.isEmpty()) {
            throw new IllegalArgumentException("Period not found with ID: " + periodId);
        }

        AccountingPeriod period = periodOpt.get();
        if (period.getStatus() == AccountingPeriod.PeriodStatus.LOCKED) {
            throw new IllegalStateException("Cannot reopen a locked period: " + period.getName());
        }

        period.setStatus(AccountingPeriod.PeriodStatus.OPEN);
        period.setClosedBy(null);
        period.setClosedDate(null);
        
        return repository.save(period);
    }

    /**
     * Validate if a date falls within an open period.
     */
    public boolean isValidTransactionDate(LocalDate date) {
        Optional<AccountingPeriod> period = getPeriodByDate(date);
        return period.isPresent() && period.get().isOpen();
    }

    /**
     * Check if there is any open period.
     */
    public boolean hasOpenPeriod() {
        return repository.hasOpenPeriod();
    }
}
