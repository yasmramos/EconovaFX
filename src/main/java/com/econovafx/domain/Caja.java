package com.econovafx.domain;

import javax.persistence.*;
import java.math.BigDecimal;

/**
 * Cash Box entity (Caja)
 * Represents a physical cash box for cash management module
 */
@Entity
@Table(name = "cajas")
public class Caja extends BaseEntity {

    @Column(nullable = false, length = 50)
    private String codigo;

    @Column(nullable = false, length = 100)
    private String descripcion;

    @Column(length = 20)
    private String moneda;

    @Column(precision = 19, scale = 4)
    private BigDecimal saldoActual = BigDecimal.ZERO;

    @Column(name = "es_activa")
    private Boolean esActiva = true;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cuenta_contable_id")
    private Account cuentaContable;

    @Column(length = 255)
    private String ubicacion;

    @Column(length = 255)
    private String responsable;

    public Caja() {
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

    public String getUbicacion() {
        return ubicacion;
    }

    public void setUbicacion(String ubicacion) {
        this.ubicacion = ubicacion;
    }

    public String getResponsable() {
        return responsable;
    }

    public void setResponsable(String responsable) {
        this.responsable = responsable;
    }
}
