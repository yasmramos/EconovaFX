package com.econovafx.repository;

import com.econovafx.domain.AccountingPeriod;
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
}
