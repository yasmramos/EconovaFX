package com.econovafx.domain;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Bank Account entity (Cuenta Bancaria)
 * Represents a bank account for cash management module
 */
@Entity
@Table(name = "cuentas_bancarias")
public class CuentaBancaria extends BaseEntity {

    @Column(nullable = false, length = 50)
    private String codigo;

    @Column(nullable = false, length = 100)
    private String descripcion;

    @Column(length = 50)
    private String numeroCuenta;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "banco_id")
    private ThirdParty banco;

    @Column(length = 20)
    private String moneda;

    @Column(precision = 19, scale = 4)
    private BigDecimal saldoActual = BigDecimal.ZERO;

    @Column(name = "es_activa")
    private Boolean esActiva = true;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cuenta_contable_id")
    private Account cuentaContable;

    public CuentaBancaria() {
    }

    // Getters and Setters
    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getNumeroCuenta() {
        return numeroCuenta;
    }

    public void setNumeroCuenta(String numeroCuenta) {
        this.numeroCuenta = numeroCuenta;
    }

    public ThirdParty getBanco() {
        return banco;
    }

    public void setBanco(ThirdParty banco) {
        this.banco = banco;
    }

    public String getMoneda() {
        return moneda;
    }

    public void setMoneda(String moneda) {
        this.moneda = moneda;
    }

    public BigDecimal getSaldoActual() {
        return saldoActual;
    }

    public void setSaldoActual(BigDecimal saldoActual) {
        this.saldoActual = saldoActual;
    }

    public Boolean getEsActiva() {
        return esActiva;
    }

    public void setEsActiva(Boolean activa) {
        esActiva = activa;
    }

    public Account getCuentaContable() {
        return cuentaContable;
    }

    public void setCuentaContable(Account cuentaContable) {
        this.cuentaContable = cuentaContable;
    }
}
