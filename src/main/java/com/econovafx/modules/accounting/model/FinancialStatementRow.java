package com.econovafx.model;

import jakarta.persistence.*;

/**
 * Financial Statement Row entity for configuring report structure
 */
@Entity
@Table(name = "financial_statement_row")
public class FinancialStatementRow extends BaseEntity {

    public enum RowType {
        HEADER,
        SUBTOTAL,
        TOTAL,
        DATA
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "model_id", nullable = false)
    private FinancialStatementModel model;

    @Column(name = "row_number", nullable = false)
    private Integer rowNumber;

    @Column(nullable = false, length = 200)
    private String label;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_row_id")
    private FinancialStatementRow parentRow;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private RowType rowType;

    @Column(name = "account_codes_pattern", length = 500)
    private String accountCodesPattern;

    @Column(name = "sign_multiplier")
    private Integer signMultiplier = 1;

    @Column(name = "is_bold")
    private Boolean isBold = false;

    @Column(name = "is_italic")
    private Boolean isItalic = false;

    @Column(name = "indent_level")
    private Integer indentLevel = 0;

    // Getters and Setters
    public FinancialStatementModel getModel() { return model; }
    public void setModel(FinancialStatementModel model) { this.model = model; }

    public Integer getRowNumber() { return rowNumber; }
    public void setRowNumber(Integer rowNumber) { this.rowNumber = rowNumber; }

    public String getLabel() { return label; }
    public void setLabel(String label) { this.label = label; }

    public FinancialStatementRow getParentRow() { return parentRow; }
    public void setParentRow(FinancialStatementRow parentRow) { this.parentRow = parentRow; }

    public RowType getRowType() { return rowType; }
    public void setRowType(RowType rowType) { this.rowType = rowType; }

    public String getAccountCodesPattern() { return accountCodesPattern; }
    public void setAccountCodesPattern(String accountCodesPattern) { this.accountCodesPattern = accountCodesPattern; }

    public Integer getSignMultiplier() { return signMultiplier; }
    public void setSignMultiplier(Integer signMultiplier) { this.signMultiplier = signMultiplier; }

    public Boolean getIsBold() { return isBold; }
    public void setIsBold(Boolean bold) { isBold = bold; }

    public Boolean getIsItalic() { return isItalic; }
    public void setIsItalic(Boolean italic) { isItalic = italic; }

    public Integer getIndentLevel() { return indentLevel; }
    public void setIndentLevel(Integer indentLevel) { this.indentLevel = indentLevel; }
}
