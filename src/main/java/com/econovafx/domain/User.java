package com.econovafx.domain;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * User entity for system access with multi-tenant and multi-currency support
 */
@Entity
@Table(name = "users")
public class User extends BaseEntity {

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String email;

    @Column(nullable = false)
    private String fullName;

    @Enumerated(EnumType.STRING)
    private UserRole role = UserRole.USER;

    @Column(name = "last_login", columnDefinition = "TIMESTAMP")
    private LocalDateTime lastLogin;

    /**
     * Empresa/tenant al que pertenece este usuario.
     * Un usuario puede estar asociado a una empresa específica.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id")
    private Company company;

    /**
     * Moneda preferida del usuario para visualización de importes.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "preferred_currency_id")
    private Currency preferredCurrency;

    /**
     * Empresas a las que tiene acceso el usuario (para usuarios que trabajan con múltiples empresas).
     */
    @ManyToMany
    @JoinTable(
        name = "user_companies",
        joinColumns = @JoinColumn(name = "user_id"),
        inverseJoinColumns = @JoinColumn(name = "company_id")
    )
    private Set<Company> accessibleCompanies = new HashSet<>();

    /**
     * Estado del usuario: ACTIVE, INACTIVE, SUSPENDED
     */
    @Column(length = 20)
    private String status = "ACTIVE";

    // Constructores
    public User() {
    }

    public User(String username, String email, String fullName) {
        this.username = username;
        this.email = email;
        this.fullName = fullName;
        this.status = "ACTIVE";
    }

    public enum UserRole {
        ADMIN,
        ACCOUNTANT,
        USER,
        VIEWER
    }

    // Getters and Setters
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public UserRole getRole() {
        return role;
    }

    public void setRole(UserRole role) {
        this.role = role;
    }

    public LocalDateTime getLastLogin() {
        return lastLogin;
    }

    public void setLastLogin(LocalDateTime lastLogin) {
        this.lastLogin = lastLogin;
    }

    public Company getCompany() {
        return company;
    }

    public void setCompany(Company company) {
        this.company = company;
    }

    public Currency getPreferredCurrency() {
        return preferredCurrency;
    }

    public void setPreferredCurrency(Currency preferredCurrency) {
        this.preferredCurrency = preferredCurrency;
    }

    public Set<Company> getAccessibleCompanies() {
        return accessibleCompanies;
    }

    public void setAccessibleCompanies(Set<Company> accessibleCompanies) {
        this.accessibleCompanies = accessibleCompanies;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
