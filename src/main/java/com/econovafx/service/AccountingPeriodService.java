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
        return createPeriod(name, startDate, endDate, AccountingPeriod.PeriodType.MONTHLY);
    }

    /**
     * Create a new accounting period with specific type.
     */
    public AccountingPeriod createPeriod(String name, LocalDate startDate, LocalDate endDate, AccountingPeriod.PeriodType type) {
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

        AccountingPeriod period = new AccountingPeriod(name, startDate, endDate, type);
        return repository.save(period);
    }

    /**
     * Create a monthly period for a specific month and year.
     */
    public AccountingPeriod createMonthlyPeriod(int year, int month) {
        LocalDate startDate = LocalDate.of(year, month, 1);
        LocalDate endDate = startDate.withDayOfMonth(startDate.lengthOfMonth());
        String name = String.format("%s %d", startDate.getMonth().getDisplayName(java.time.format.TextStyle.FULL, java.util.Locale.ENGLISH), year);
        return createPeriod(name, startDate, endDate, AccountingPeriod.PeriodType.MONTHLY);
    }

    /**
     * Create an annual period for a specific year.
     */
    public AccountingPeriod createAnnualPeriod(int year) {
        LocalDate startDate = LocalDate.of(year, 1, 1);
        LocalDate endDate = LocalDate.of(year, 12, 31);
        String name = "Fiscal Year " + year;
        return createPeriod(name, startDate, endDate, AccountingPeriod.PeriodType.ANNUAL);
    }

    /**
     * Close a monthly period with validation.
     */
    public AccountingPeriod closeMonthlyPeriod(Long periodId, String closedBy, String notes) {
        Optional<AccountingPeriod> periodOpt = repository.findById(periodId);
        if (periodOpt.isEmpty()) {
            throw new IllegalArgumentException("Period not found with ID: " + periodId);
        }

        AccountingPeriod period = periodOpt.get();
        if (!period.isOpen()) {
            throw new IllegalStateException("Period is already closed or locked: " + period.getName());
        }

        if (!period.isMonthly()) {
            throw new IllegalStateException("This operation is only for monthly periods: " + period.getName());
        }

        period.setStatus(AccountingPeriod.PeriodStatus.CLOSED);
        period.setClosedBy(closedBy);
        period.setClosedDate(LocalDate.now());
        period.setClosingNotes(notes);
        
        log.info("Monthly period closed: {} by {} on {}", period.getName(), closedBy, LocalDate.now());
        return repository.save(period);
    }

    /**
     * Close an annual period with validation and optional monthly closure check.
     */
    public AccountingPeriod closeAnnualPeriod(Long periodId, String closedBy, String notes, boolean closeRelatedMonths) {
        Optional<AccountingPeriod> periodOpt = repository.findById(periodId);
        if (periodOpt.isEmpty()) {
            throw new IllegalArgumentException("Period not found with ID: " + periodId);
        }

        AccountingPeriod period = periodOpt.get();
        if (!period.isOpen()) {
            throw new IllegalStateException("Period is already closed or locked: " + period.getName());
        }

        if (!period.isAnnual()) {
            throw new IllegalStateException("This operation is only for annual periods: " + period.getName());
        }

        // Optionally close all monthly periods within this annual period
        if (closeRelatedMonths) {
            List<AccountingPeriod> monthlyPeriods = repository.findPeriodsByYearAndType(
                period.getStartDate().getYear(), 
                AccountingPeriod.PeriodType.MONTHLY
            );
            
            for (AccountingPeriod monthly : monthlyPeriods) {
                if (monthly.isOpen()) {
                    monthly.setStatus(AccountingPeriod.PeriodStatus.CLOSED);
                    monthly.setClosedBy(closedBy);
                    monthly.setClosedDate(LocalDate.now());
                    monthly.setClosingNotes("Auto-closed due to annual closure: " + period.getName());
                    repository.save(monthly);
                    log.info("Auto-closed monthly period: {}", monthly.getName());
                }
            }
        }

        period.setStatus(AccountingPeriod.PeriodStatus.CLOSED);
        period.setClosedBy(closedBy);
        period.setClosedDate(LocalDate.now());
        period.setClosingNotes(notes);
        
        log.info("Annual period closed: {} by {} on {}", period.getName(), closedBy, LocalDate.now());
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
