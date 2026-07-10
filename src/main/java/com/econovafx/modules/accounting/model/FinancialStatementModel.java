package com.econovafx.modules.accounting.model;

import io.ebean.annotation.DbEnumType;
import io.ebean.annotation.DbEnumValue;
import jakarta.persistence.*;

/**
 * Financial Statement Model entity for configurable report templates
 */
@Entity
@Table(name = "financial_statement_model")
public class FinancialStatementModel extends BaseEntity {

    public enum ModelType {
        BALANCE_SHEET,
        INCOME_STATEMENT,
        CASH_FLOW;

        @DbEnumValue(storage = DbEnumType.VARCHAR)
        public String toValue() {
            return name();
        }
    }

    @Column(nullable = false, unique = true, length = 20)
    private String code;

    @Column(nullable = false, length = 100)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ModelType modelType;

    @Column(length = 2000)
    private String description;

    @Column(name = "created_by", length = 50)
    private String createdBy;

    @Column(name = "updated_by", length = 50)
    private String updatedBy;

    // Getters and Setters
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public ModelType getModelType() { return modelType; }
    public void setModelType(ModelType modelType) { this.modelType = modelType; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }

    public String getUpdatedBy() { return updatedBy; }
    public void setUpdatedBy(String updatedBy) { this.updatedBy = updatedBy; }

    @Override
    public String toString() {
        return "FinancialStatementModel{" +
                "id=" + getId() +
                ", code='" + code + '\'' +
                ", name='" + name + '\'' +
                '}';
    }
}
