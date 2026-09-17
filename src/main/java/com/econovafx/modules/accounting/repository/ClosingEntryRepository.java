package com.econovafx.modules.accounting.repository;

import com.econovafx.modules.accounting.model.ClosingEntry;
import com.econovafx.modules.core.model.BaseEntity;
import io.ebean.Database;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

import java.time.LocalDate;
import java.util.List;

/**
 * Repository for managing ClosingEntry entities.
 * Resolution 340/2004 Compliance: Tracks closing entries for opening, income, expense, and result closures.
 */
@Singleton
public class ClosingEntryRepository {

    @Inject
    Database database;

    /**
     * Find all closing entries for a fiscal year.
     * 
     * @param fiscalYear The fiscal year to search
     * @return List of closing entries for the year
     */
    public List<ClosingEntry> findByFiscalYear(Integer fiscalYear) {
        return database.find(ClosingEntry.class)
                .where()
                .eq("fiscalYear", fiscalYear)
                .orderBy().desc("closingDate")
                .findList();
    }

    /**
     * Find closing entries by type and fiscal year.
     * 
     * @param closingType The type of closing entry
     * @param fiscalYear The fiscal year to search
     * @return List of closing entries matching the criteria
     */
    public List<ClosingEntry> findByClosingTypeAndFiscalYear(ClosingEntry.ClosingType closingType, Integer fiscalYear) {
        return database.find(ClosingEntry.class)
                .where()
                .eq("closingType", closingType)
                .eq("fiscalYear", fiscalYear)
                .orderBy().desc("closingDate")
                .findList();
    }

    /**
     * Check if opening has been closed for a fiscal year.
     * 
     * @param fiscalYear The fiscal year to check
     * @return true if opening is closed, false otherwise
     */
    public boolean isOpeningClosed(Integer fiscalYear) {
        long count = database.find(ClosingEntry.class)
                .where()
                .eq("closingType", ClosingEntry.ClosingType.OPENING)
                .eq("fiscalYear", fiscalYear)
                .eq("isPosted", true)
                .findCount();
        return count > 0;
    }

    /**
     * Check if nominal accounts have been closed for a fiscal year.
     * Validates that both INCOME and EXPENSE closing entries exist.
     * 
     * @param fiscalYear The fiscal year to check
     * @return true if nominal accounts are closed, false otherwise
     */
    public boolean areNominalAccountsClosed(Integer fiscalYear) {
        long incomeCount = database.find(ClosingEntry.class)
                .where()
                .eq("closingType", ClosingEntry.ClosingType.INCOME)
                .eq("fiscalYear", fiscalYear)
                .eq("isPosted", true)
                .findCount();

        long expenseCount = database.find(ClosingEntry.class)
                .where()
                .eq("closingType", ClosingEntry.ClosingType.EXPENSE)
                .eq("fiscalYear", fiscalYear)
                .eq("isPosted", true)
                .findCount();

        return incomeCount > 0 && expenseCount > 0;
    }

    /**
     * Save a closing entry.
     * 
     * @param closingEntry The closing entry to save
     * @return The saved closing entry
     */
    public void save(ClosingEntry closingEntry) {
        database.save(closingEntry);
    }

    /**
     * Find a closing entry by ID.
     * 
     * @param id The ID of the closing entry
     * @return The closing entry or null if not found
     */
    public ClosingEntry findById(Long id) {
        return database.find(ClosingEntry.class).setId(id).findOne();
    }

    /**
     * Find all closing entries.
     * 
     * @return List of all closing entries
     */
    public List<ClosingEntry> findAll() {
        return database.find(ClosingEntry.class).findList();
    }
}
