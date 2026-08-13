package com.econovafx.modules.reporting.repository;

import com.econovafx.modules.reporting.model.FinancialReport;
import io.avaje.inject.Component;
import io.ebean.Database;
import io.ebean.ExpressionList;
import jakarta.inject.Inject;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Repository for FinancialReport entities.
 * Provides methods to query financial reports by various criteria.
 */
@Component
public class FinancialReportRepository {

    private final Database database;

    @Inject
    public FinancialReportRepository(Database database) {
        this.database = database;
    }

    /**
     * Find all reports.
     * @return list of all reports
     */
    public List<FinancialReport> findAll() {
        return database.find(FinancialReport.class).findList();
    }

    /**
     * Find report by ID.
     * @param id the report ID
     * @return optional containing the report or empty
     */
    public java.util.Optional<FinancialReport> findById(Long id) {
        return Optional.ofNullable(database.find(FinancialReport.class, id));
    }

    /**
     * Save a report.
     * @param report the report to save
     * @return saved report
     */
    public FinancialReport save(FinancialReport report) {
        database.save(report);
        return report;
    }

    /**
     * Find reports by type and date range.
     * @param reportType the type of report
     * @param startDate start date of the report period
     * @param endDate end date of the report period
     * @return list of matching reports
     */
    public List<FinancialReport> findByReportTypeAndStartDateBetween(
        FinancialReport.ReportType reportType, 
        LocalDate startDate, 
        LocalDate endDate
    ) {
        return database.find(FinancialReport.class)
            .where()
            .eq("reportType", reportType)
            .between("startDate", startDate, endDate)
            .findList();
    }

    /**
     * Find all reports for a specific fiscal year.
     * @param fiscalYear the fiscal year
     * @return list of reports for that year
     */
    public List<FinancialReport> findByFiscalYear(Integer fiscalYear) {
        return database.find(FinancialReport.class)
            .where()
            .eq("fiscalYear", fiscalYear)
            .findList();
    }

    /**
     * Find the latest report of a specific type.
     * @param reportType the type of report
     * @return the most recent report or null if none exists
     */
    public FinancialReport findFirstByReportTypeOrderByEndDateDesc(FinancialReport.ReportType reportType) {
        return database.find(FinancialReport.class)
            .where()
            .eq("reportType", reportType)
            .orderBy("endDate desc")
            .setMaxRows(1)
            .findOne();
    }

    /**
     * Check if a report exists for the given period and type.
     * @param reportType the report type
     * @param startDate start date
     * @param endDate end date
     * @return true if a report exists
     */
    public boolean existsByReportTypeAndStartDateAndEndDate(
        FinancialReport.ReportType reportType,
        LocalDate startDate,
        LocalDate endDate
    ) {
        return database.find(FinancialReport.class)
            .where()
            .eq("reportType", reportType)
            .eq("startDate", startDate)
            .eq("endDate", endDate)
            .exists();
    }

    /**
     * Find all final reports.
     * @return list of finalized reports
     */
    public List<FinancialReport> findByIsFinalTrue() {
        return database.find(FinancialReport.class)
            .where()
            .eq("isFinal", true)
            .findList();
    }

    /**
     * Find draft reports (not finalized).
     * @return list of draft reports
     */
    public List<FinancialReport> findByIsFinalFalse() {
        return database.find(FinancialReport.class)
            .where()
            .eq("isFinal", false)
            .findList();
    }
}
