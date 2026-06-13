package com.econovafx.domain;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Bank Reconciliation entity (Conciliación Bancaria)
 * Represents the reconciliation between bank statements and system records
 */
@Entity
@Table(name = "conciliaciones_bancarias")
public class ConciliacionBancaria extends BaseEntity {

    @Column(nullable = false, length = 50)
    private String numeroConciliacion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cuenta_bancaria_id", nullable = false)
    private CuentaBancaria cuentaBancaria;

    @Column(nullable = false)
    private LocalDate fechaInicio;

    @Column(nullable = false)
    private LocalDate fechaFin;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal saldoSegunSistemaInicial;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal saldoSegunBancoInicial;

    @Column(precision = 19, scale = 4)
    private BigDecimal saldoSegunSistemaFinal;

    @Column(precision = 19, scale = 4)
    private BigDecimal saldoSegunBancoFinal;

    @Column(precision = 19, scale = 4)
    private BigDecimal diferencia;

    @Column(length = 20)
    private String moneda;

    @Column(name = "esta_cuadrada")
    private Boolean estaCuadrada = false;

    @Column(name = "es_posteada")
    private Boolean esPosteada = false;

    @Column(name = "fecha_posteo")
    private LocalDateTime fechaPosteo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_posteo_id")
    private User usuarioPosteo;

    @OneToMany(mappedBy = "conciliacion", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PartidaConciliacion> partidas = new ArrayList<>();

    @Column(length = 500)
    private String observaciones;

    public ConciliacionBancaria() {
    }

    // Getters and Setters
    public String getNumeroConciliacion() {
        return numeroConciliacion;
    }

    public void setNumeroConciliacion(String numeroConciliacion) {
        this.numeroConciliacion = numeroConciliacion;
    }

    public CuentaBancaria getCuentaBancaria() {
        return cuentaBancaria;
    }

    public void setCuentaBancaria(CuentaBancaria cuentaBancaria) {
        this.cuentaBancaria = cuentaBancaria;
    }

    public LocalDate getFechaInicio() {
        return fechaInicio;
    }

    public void setFechaInicio(LocalDate fechaInicio) {
        this.fechaInicio = fechaInicio;
    }

    public LocalDate getFechaFin() {
        return fechaFin;
    }

    public void setFechaFin(LocalDate fechaFin) {
        this.fechaFin = fechaFin;
    }

    public BigDecimal getSaldoSegunSistemaInicial() {
        return saldoSegunSistemaInicial;
    }

    public void setSaldoSegunSistemaInicial(BigDecimal saldoSegunSistemaInicial) {
        this.saldoSegunSistemaInicial = saldoSegunSistemaInicial;
    }

    public BigDecimal getSaldoSegunBancoInicial() {
        return saldoSegunBancoInicial;
    }

    public void setSaldoSegunBancoInicial(BigDecimal saldoSegunBancoInicial) {
        this.saldoSegunBancoInicial = saldoSegunBancoInicial;
    }

    public BigDecimal getSaldoSegunSistemaFinal() {
        return saldoSegunSistemaFinal;
    }

    public void setSaldoSegunSistemaFinal(BigDecimal saldoSegunSistemaFinal) {
        this.saldoSegunSistemaFinal = saldoSegunSistemaFinal;
    }

    public BigDecimal getSaldoSegunBancoFinal() {
        return saldoSegunBancoFinal;
    }

    public void setSaldoSegunBancoFinal(BigDecimal saldoSegunBancoFinal) {
        this.saldoSegunBancoFinal = saldoSegunBancoFinal;
    }

    public BigDecimal getDiferencia() {
        return diferencia;
    }

    public void setDiferencia(BigDecimal diferencia) {
        this.diferencia = diferencia;
    }

    public String getMoneda() {
        return moneda;
    }

    public void setMoneda(String moneda) {
        this.moneda = moneda;
    }

    public Boolean getEstaCuadrada() {
        return estaCuadrada;
    }

    public void setEstaCuadrada(Boolean cuadrada) {
        estaCuadrada = cuadrada;
    }

    public Boolean getEsPosteada() {
        return esPosteada;
    }

    public void setEsPosteada(Boolean posteada) {
        esPosteada = posteada;
    }

    public LocalDateTime getFechaPosteo() {
        return fechaPosteo;
    }

    public void setFechaPosteo(LocalDateTime fechaPosteo) {
        this.fechaPosteo = fechaPosteo;
    }

    public User getUsuarioPosteo() {
        return usuarioPosteo;
    }

    public void setUsuarioPosteo(User usuarioPosteo) {
        this.usuarioPosteo = usuarioPosteo;
    }

    public List<PartidaConciliacion> getPartidas() {
        return partidas;
    }

    public void setPartidas(List<PartidaConciliacion> partidas) {
        this.partidas = partidas;
    }

    public void addPartida(PartidaConciliacion partida) {
        partidas.add(partida);
        partida.setConciliacion(this);
    }

    public String getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }

    public boolean isBalanced() {
        if (diferencia == null) {
            return false;
        }
        return diferencia.compareTo(BigDecimal.ZERO) == 0;
    }
}
