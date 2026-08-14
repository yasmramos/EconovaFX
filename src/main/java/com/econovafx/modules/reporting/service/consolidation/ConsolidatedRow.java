package com.econovafx.modules.reporting.service.consolidation;

import com.econovafx.modules.accounting.model.FinancialStatementRow.RowType;

import java.math.BigDecimal;
import java.util.Map;

/**
 * Represents a consolidated row in a financial statement.
 * Contains aggregated values from multiple companies plus breakdown per company.
 * 
 * @author Development Team
 * @since 1.0.0
 */
public class ConsolidatedRow {

    /**
     * Row number for ordering and identification.
     */
    private final Integer rowNumber;

    /**
     * Label/description of the row concept.
     */
    private final String label;

    /**
     * Type of row (header, subtotal, total, etc.).
     */
    private final RowType rowType;

    /**
     * Consolidated value (sum across all companies).
     */
    private final BigDecimal consolidatedValue;

    /**
     * Indentation level for display formatting.
     */
    private final Integer indentLevel;

    /**
     * Whether the row should be displayed in bold.
     */
    private final Boolean isBold;

    /**
     * Whether the row should be displayed in italic.
     */
    private final Boolean isItalic;

    /**
     * Breakdown of values per company for traceability.
     * Key: Company ID, Value: Individual company's value for this row.
     */
    private final Map<Long, BigDecimal> companyValues;

    /**
     * Creates a new ConsolidatedRow.
     * 
     * @param rowNumber Row number for ordering
     * @param label Row label/description
     * @param rowType Type of row
     * @param consolidatedValue Summed value across all companies
     * @param indentLevel Indentation level
     * @param isBold Whether to display in bold
     * @param isItalic Whether to display in italic
     * @param companyValues Breakdown per company
     */
    public ConsolidatedRow(Integer rowNumber,
                          String label,
                          RowType rowType,
                          BigDecimal consolidatedValue,
                          Integer indentLevel,
                          Boolean isBold,
                          Boolean isItalic,
                          Map<Long, BigDecimal> companyValues) {
        this.rowNumber = rowNumber;
        this.label = label;
        this.rowType = rowType;
        this.consolidatedValue = consolidatedValue != null ? consolidatedValue : BigDecimal.ZERO;
        this.indentLevel = indentLevel != null ? indentLevel : 0;
        this.isBold = isBold != null ? isBold : false;
        this.isItalic = isItalic != null ? isItalic : false;
        this.companyValues = companyValues != null ? companyValues : Map.of();
    }

    /**
     * Gets the row number.
     * @return the row number
     */
    public Integer getRowNumber() {
        return rowNumber;
    }

    /**
     * Gets the row label.
     * @return the label
     */
    public String getLabel() {
        return label;
    }

    /**
     * Gets the row type.
     * @return the row type
     */
    public RowType getRowType() {
        return rowType;
    }

    /**
     * Gets the consolidated value (sum across all companies).
     * @return the consolidated value
     */
    public BigDecimal getConsolidatedValue() {
        return consolidatedValue;
    }

    /**
     * Gets the indentation level.
     * @return the indent level
     */
    public Integer getIndentLevel() {
        return indentLevel;
    }

    /**
     * Checks if the row should be bold.
     * @return true if bold
     */
    public Boolean getIsBold() {
        return isBold;
    }

    /**
     * Checks if the row should be italic.
     * @return true if italic
     */
    public Boolean getIsItalic() {
        return isItalic;
    }

    /**
     * Gets the breakdown of values per company.
     * @return map of company ID to individual value
     */
    public Map<Long, BigDecimal> getCompanyValues() {
        return companyValues;
    }

    /**
     * Gets the value for a specific company.
     * @param companyId The company ID
     * @return The value for that company, or BigDecimal.ZERO if not found
     */
    public BigDecimal getValueForCompany(Long companyId) {
        return companyValues.getOrDefault(companyId, BigDecimal.ZERO);
    }

    /**
     * Gets the number of companies that contributed to this row.
     * @return count of companies with values
     */
    public int getCompanyCount() {
        return companyValues.size();
    }

    @Override
    public String toString() {
        return "ConsolidatedRow{" +
                "rowNumber=" + rowNumber +
                ", label='" + label + '\'' +
                ", consolidatedValue=" + consolidatedValue +
                ", companyCount=" + companyValues.size() +
                '}';
    }
}
