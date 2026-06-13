package com.econovafx.domain;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Cash Movement entity (Movimiento de Efectivo)
 * Represents cash transactions for bank accounts and cash boxes
 */
@Entity
@Table(name = "movimientos_efectivo")
public class MovimientoEfectivo extends BaseEntity {

    @Column(nullable = false, length = 50)
    private String numeroComprobante;

    @Column(nullable = false)
    private LocalDate fecha;

    @Column(nullable = false, length = 20)
    private String tipoMovimiento; // INCOME, EXPENSE, TRANSFER

    @Column(length = 255)
    private String descripcion;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal importe;

    @Column(length = 20)
    private String moneda;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cuenta_bancaria_id")
    private CuentaBancaria cuentaBancaria;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "caja_id")
    private Caja caja;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tercero_id")
    private ThirdParty tercero;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cuenta_contrapartida_id")
    private Account cuentaContrapartida;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "centro_costo_id")
    private Account centroCosto;

    @Column(name = "es_posteado")
    private Boolean esPosteado = false;

    @Column(name = "fecha_posteo")
    private LocalDateTime fechaPosteo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_posteo_id")
    private User usuarioPosteo;

    @Column(name = "esta_anulado")
    private Boolean estaAnulado = false;

    @Column(name = "fecha_anulacion")
    private LocalDateTime fechaAnulacion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_anulacion_id")
    private User usuarioAnulacion;

    @Column(length = 255)
    private String motivoAnulacion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "comprobante_contable_id")
    private Transaction comprobanteContable;

    public MovimientoEfectivo() {
    }

    // Getters and Setters
    public String getNumeroComprobante() {
        return numeroComprobante;
    }

    public void setNumeroComprobante(String numeroComprobante) {
        this.numeroComprobante = numeroComprobante;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public void setFecha(LocalDate fecha) {
        this.fecha = fecha;
    }

    public String getTipoMovimiento() {
        return tipoMovimiento;
    }

    public void setTipoMovimiento(String tipoMovimiento) {
        this.tipoMovimiento = tipoMovimiento;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public BigDecimal getImporte() {
        return importe;
    }

    public void setImporte(BigDecimal importe) {
        this.importe = importe;
    }

    public String getMoneda() {
        return moneda;
    }

    public void setMoneda(String moneda) {
        this.moneda = moneda;
    }

    public CuentaBancaria getCuentaBancaria() {
        return cuentaBancaria;
    }

    public void setCuentaBancaria(CuentaBancaria cuentaBancaria) {
        this.cuentaBancaria = cuentaBancaria;
    }

    public Caja getCaja() {
        return caja;
    }

    public void setCaja(Caja caja) {
        this.caja = caja;
    }

    public ThirdParty getTercero() {
        return tercero;
    }

    public void setTercero(ThirdParty tercero) {
        this.tercero = tercero;
    }

    public Account getCuentaContrapartida() {
        return cuentaContrapartida;
    }

    public void setCuentaContrapartida(Account cuentaContrapartida) {
        this.cuentaContrapartida = cuentaContrapartida;
    }

    public Account getCentroCosto() {
        return centroCosto;
    }

    public void setCentroCosto(Account centroCosto) {
        this.centroCosto = centroCosto;
    }

    public Boolean getEsPosteado() {
        return esPosteado;
    }

    public void setEsPosteado(Boolean posteado) {
        esPosteado = posteado;
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

    public Boolean getEstaAnulado() {
        return estaAnulado;
    }

    public void setEstaAnulado(Boolean anulado) {
        estaAnulado = anulado;
    }

    public LocalDateTime getFechaAnulacion() {
        return fechaAnulacion;
    }

    public void setFechaAnulacion(LocalDateTime fechaAnulacion) {
        this.fechaAnulacion = fechaAnulacion;
    }

    public User getUsuarioAnulacion() {
        return usuarioAnulacion;
    }

    public void setUsuarioAnulacion(User usuarioAnulacion) {
        this.usuarioAnulacion = usuarioAnulacion;
    }

    public String getMotivoAnulacion() {
        return motivoAnulacion;
    }

    public void setMotivoAnulacion(String motivoAnulacion) {
        this.motivoAnulacion = motivoAnulacion;
    }

    public Transaction getComprobanteContable() {
        return comprobanteContable;
    }

    public void setComprobanteContable(Transaction comprobanteContable) {
        this.comprobanteContable = comprobanteContable;
    }
}
