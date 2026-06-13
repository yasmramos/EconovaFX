package com.econovafx.domain;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Reconciliation Item entity (Partida de Conciliación)
 * Represents individual items in a bank reconciliation
 */
@Entity
@Table(name = "partidas_conciliacion")
public class PartidaConciliacion extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "conciliacion_id", nullable = false)
    private ConciliacionBancaria conciliacion;

    @Column(nullable = false, length = 20)
    private String tipoPartida; // SISTEMA, BANCO

    @Column(nullable = false)
    private LocalDate fecha;

    @Column(length = 50)
    private String numeroDocumento;

    @Column(length = 255)
    private String descripcion;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal importe;

    @Column(nullable = false)
    private Boolean estaConciliada = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "movimiento_efectivo_id")
    private MovimientoEfectivo movimientoEfectivo;

    @Column(length = 255)
    private String observaciones;

    public PartidaConciliacion() {
    }

    // Getters and Setters
    public ConciliacionBancaria getConciliacion() {
        return conciliacion;
    }

    public void setConciliacion(ConciliacionBancaria conciliacion) {
        this.conciliacion = conciliacion;
    }

    public String getTipoPartida() {
        return tipoPartida;
    }

    public void setTipoPartida(String tipoPartida) {
        this.tipoPartida = tipoPartida;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public void setFecha(LocalDate fecha) {
        this.fecha = fecha;
    }

    public String getNumeroDocumento() {
        return numeroDocumento;
    }

    public void setNumeroDocumento(String numeroDocumento) {
        this.numeroDocumento = numeroDocumento;
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

    public Boolean getEstaConciliada() {
        return estaConciliada;
    }

    public void setEstaConciliada(Boolean conciliada) {
        estaConciliada = conciliada;
    }

    public MovimientoEfectivo getMovimientoEfectivo() {
        return movimientoEfectivo;
    }

    public void setMovimientoEfectivo(MovimientoEfectivo movimientoEfectivo) {
        this.movimientoEfectivo = movimientoEfectivo;
    }

    public String getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }
}
