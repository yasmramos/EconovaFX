package com.econovafx.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

/**
 * Entity representing a Company in the multi-tenant system.
 * Each company has its own independent database.
 */
@Entity
@Table(name = "companies")
public class Company extends BaseEntity {

    private String name;

    private String code;

    private String nif;

    private String address;

    private String phone;

    private String email;

    /**
     * Path or name of the database associated with this company.
     * Example: "jdbc:h2:./db/econova_company1" or "jdbc:postgresql://localhost/company1_db"
     */
    private String databaseUrl;

    /**
     * Database user (if applicable)
     */
    private String databaseUser;

    /**
     * Company status: ACTIVE, INACTIVE, SUSPENDED
     */
    private String status;

    // Constructors
    public Company() {
    }

    public Company(String name, String code, String nif) {
        this.name = name;
        this.code = code;
        this.nif = nif;
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

    public String getNif() {
        return nif;
    }

    public void setNif(String nif) {
        this.nif = nif;
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

    public String getDatabaseUrl() {
        return databaseUrl;
    }

    public void setDatabaseUrl(String databaseUrl) {
        this.databaseUrl = databaseUrl;
    }

    public String getDatabaseUser() {
        return databaseUser;
    }

    public void setDatabaseUser(String databaseUser) {
        this.databaseUser = databaseUser;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }

    @Override
    public String toString() {
        return "Company{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", code='" + code + '\'' +
                ", nif='" + nif + '\'' +
                ", status='" + status + '\'' +
                '}';
    }
}
