package com.econovafx.domain;

import jakarta.persistence.*;
import java.sql.Timestamp;

/**
 * Report Definition entity for saved report configurations
 */
@Entity
@Table(name = "report_definition")
public class ReportDefinition {

    public enum ReportType {
        TRIAL_BALANCE,
        GENERAL_LEDGER,
        VOUCHER_HISTORY,
        FINANCIAL_STATEMENT
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 20)
    private String code;

    @Column(nullable = false, length = 100)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private ReportType reportType;

    @Column(name = "parameters_json", columnDefinition = "TEXT")
    private String parametersJson;

    @Column(name = "default_filters_json", columnDefinition = "TEXT")
    private String defaultFiltersJson;

    @Column(name = "is_public")
    private Boolean isPublic = true;

    @Column(name = "created_at")
    private Timestamp createdAt;

    @Column(name = "created_by", length = 50)
    private String createdBy;

    @Column(name = "updated_at")
    private Timestamp updatedAt;

    @Column(name = "updated_by", length = 50)
    private String updatedBy;

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public ReportType getReportType() { return reportType; }
    public void setReportType(ReportType reportType) { this.reportType = reportType; }

    public String getParametersJson() { return parametersJson; }
    public void setParametersJson(String parametersJson) { this.parametersJson = parametersJson; }

    public String getDefaultFiltersJson() { return defaultFiltersJson; }
    public void setDefaultFiltersJson(String defaultFiltersJson) { this.defaultFiltersJson = defaultFiltersJson; }

    public Boolean getIsPublic() { return isPublic; }
    public void setIsPublic(Boolean aPublic) { isPublic = aPublic; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }

    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }

    public Timestamp getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Timestamp updatedAt) { this.updatedAt = updatedAt; }

    public String getUpdatedBy() { return updatedBy; }
    public void setUpdatedBy(String updatedBy) { this.updatedBy = updatedBy; }
}
