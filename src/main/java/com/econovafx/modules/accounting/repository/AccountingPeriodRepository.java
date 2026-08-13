package com.econovafx.modules.accounting.repository;

import com.econovafx.modules.accounting.model.AccountingPeriod;
import io.avaje.inject.Component;
import io.ebean.Database;
import jakarta.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Repository for managing AccountingPeriod entities.
 */
@Component
public class AccountingPeriodRepository {

    private static final Logger log = LoggerFactory.getLogger(AccountingPeriodRepository.class);

    @Inject
    Database database;

    /**
     * Find all accounting periods ordered by start date descending.
     */
    public List<AccountingPeriod> findAll() {
        return database.find(AccountingPeriod.class)
                .orderBy("startDate desc")
                .findList();
    }

    /**
     * Find an accounting period by ID.
     */
    public Optional<AccountingPeriod> findById(Long id) {
        AccountingPeriod period = database.find(AccountingPeriod.class, id);
        return Optional.ofNullable(period);
    }

    /**
     * Find the current open accounting period.
     */
    public Optional<AccountingPeriod> findCurrentOpenPeriod() {
        List<AccountingPeriod> results = database.find(AccountingPeriod.class)
                .where().eq("status", AccountingPeriod.PeriodStatus.OPEN)
                .orderBy("startDate desc")
                .setMaxRows(1)
                .findList();
        return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
    }

    /**
     * Find a period that contains a specific date.
     */
    public Optional<AccountingPeriod> findPeriodByDate(LocalDate date) {
        List<AccountingPeriod> results = database.find(AccountingPeriod.class)
                .where()
                .le("startDate", date)
                .ge("endDate", date)
                .findList();
        return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
    }

    /**
     * Save or update an accounting period.
     */
    public AccountingPeriod save(AccountingPeriod period) {
        database.save(period);
        return period;
    }

    /**
     * Delete an accounting period.
     */
    public void delete(Long id) {
        Optional<AccountingPeriod> period = findById(id);
        period.ifPresent(database::delete);
    }

    /**
     * Check if there is any open period.
     */
    public boolean hasOpenPeriod() {
        long count = database.find(AccountingPeriod.class)
                .where().eq("status", AccountingPeriod.PeriodStatus.OPEN)
                .findCount();
        return count > 0;
    }

    /**
     * Find all periods for a specific year and type.
     */
    public List<AccountingPeriod> findPeriodsByYearAndType(int year, AccountingPeriod.PeriodType type) {
        LocalDate startOfYear = LocalDate.of(year, 1, 1);
        LocalDate endOfYear = LocalDate.of(year, 12, 31);
        
        return database.find(AccountingPeriod.class)
                .where()
                .eq("type", type)
                .ge("startDate", startOfYear)
                .le("endDate", endOfYear)
                .orderBy("startDate asc")
                .findList();
    }

    /**
     * Find all monthly periods for a specific year.
     */
    public List<AccountingPeriod> findMonthlyPeriodsByYear(int year) {
        return findPeriodsByYearAndType(year, AccountingPeriod.PeriodType.MONTHLY);
    }

    /**
     * Find the annual period for a specific year.
     */
    public Optional<AccountingPeriod> findAnnualPeriodByYear(int year) {
        LocalDate startOfYear = LocalDate.of(year, 1, 1);
        LocalDate endOfYear = LocalDate.of(year, 12, 31);
        
        List<AccountingPeriod> results = database.find(AccountingPeriod.class)
                .where()
                .eq("type", AccountingPeriod.PeriodType.ANNUAL)
                .ge("startDate", startOfYear)
                .le("endDate", endOfYear)
                .findList();
        
        return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
    }
}
