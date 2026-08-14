package com.econovafx.modules.core.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.JoinColumn;

/**
 * Entity representing a Business Unit within a Company.
 * Used for multi-tenant systems where companies have multiple branches/units.
 */
@Entity
@Table(name = "business_units")
public class BusinessUnit extends BaseEntity {

    private String name;

    private String code;

    private String address;

    private String phone;

    private String email;

    /**
     * Parent company that owns this unit.
     */
    @ManyToOne
    @JoinColumn(name = "company_id")
    private Company company;

    /**
     * Unit status: ACTIVE, INACTIVE
     */
    private String status;

    // Constructors
    public BusinessUnit() {
    }

    public BusinessUnit(String name, String code, Company company) {
        this.name = name;
        this.code = code;
        this.company = company;
        this.status = "ACTIVE";
    }

    // Getters and Setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Company getCompany() {
        return company;
    }

    public void setCompany(Company company) {
        this.company = company;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "BusinessUnit{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", code='" + code + '\'' +
                ", company='" + (company != null ? company.getName() : "null") + '\'' +
                ", status='" + status + '\'' +
                '}';
    }
}
