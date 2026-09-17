package com.econovafx.modules.reporting.model;

import com.econovafx.modules.core.model.BaseEntity;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.List;
import java.util.ArrayList;

@Entity
@Table(name = "financial_reports")
public class FinancialReport extends BaseEntity {

    public enum ReportType {
        BALANCE_SHEET, INCOME_STATEMENT, CASH_FLOW, TRIAL_BALANCE
    }

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private ReportType reportType;

    @Column(nullable = false, columnDefinition = "DATE")
    private LocalDate startDate;

    @Column(nullable = false, columnDefinition = "DATE")
    private LocalDate endDate;

    @Column(nullable = false, columnDefinition = "INTEGER")
    private Integer fiscalYear;

    @ElementCollection
    @CollectionTable(name = "report_lines", joinColumns = @JoinColumn(name = "report_id"))
    private List<ReportLine> lines = new ArrayList<>();

    @Column(columnDefinition = "TEXT")
    private String generatedData;

    @Column(nullable = false)
    private boolean isFinal;

    // Getters y Setters
    public ReportType getReportType() { return reportType; }
    public void setReportType(ReportType reportType) { this.reportType = reportType; }
    
    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }
    
    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }
    
    public Integer getFiscalYear() { return fiscalYear; }
    public void setFiscalYear(Integer fiscalYear) { this.fiscalYear = fiscalYear; }
    
    public List<ReportLine> getLines() { return lines; }
    public void setLines(List<ReportLine> lines) { this.lines = lines; }
    
    public String getGeneratedData() { return generatedData; }
    public void setGeneratedData(String generatedData) { this.generatedData = generatedData; }
    
    public boolean isFinal() { return isFinal; }
    public void setFinal(boolean aFinal) { isFinal = aFinal; }
}
