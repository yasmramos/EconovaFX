package com.econovafx.external.bc;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Modelo para una tasa de cambio individual del Banco Central de Cuba.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class TasaCambio {
    
    @JsonProperty("_id")
    private String id;
    
    @JsonProperty("codigoMoneda")
    private String codigoMoneda;
    
    @JsonProperty("nombreMoneda")
    private String nombreMoneda;
    
    @JsonProperty("tasaOficial")
    private Double tasaOficial;
    
    @JsonProperty("tasaPublica")
    private Double tasaPublica;
    
    @JsonProperty("tasaEspecial")
    private Double tasaEspecial;
    
    @JsonProperty("fechaDesde")
    private String fechaDesde;
    
    @JsonProperty("fechaHasta")
    private String fechaHasta;
    
    @JsonProperty("fechaDia")
    private String fechaDia;
    
    @JsonProperty("estado")
    private String estado;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCodigoMoneda() {
        return codigoMoneda;
    }

    public void setCodigoMoneda(String codigoMoneda) {
        this.codigoMoneda = codigoMoneda;
    }

    public String getNombreMoneda() {
        return nombreMoneda;
    }

    public void setNombreMoneda(String nombreMoneda) {
        this.nombreMoneda = nombreMoneda;
    }

    public Double getTasaOficial() {
        return tasaOficial;
    }

    public void setTasaOficial(Double tasaOficial) {
        this.tasaOficial = tasaOficial;
    }

    public Double getTasaPublica() {
        return tasaPublica;
    }

    public void setTasaPublica(Double tasaPublica) {
        this.tasaPublica = tasaPublica;
    }

    public Double getTasaEspecial() {
        return tasaEspecial;
    }

    public void setTasaEspecial(Double tasaEspecial) {
        this.tasaEspecial = tasaEspecial;
    }

    public String getFechaDesde() {
        return fechaDesde;
    }

    public void setFechaDesde(String fechaDesde) {
        this.fechaDesde = fechaDesde;
    }

    public String getFechaHasta() {
        return fechaHasta;
    }

    public void setFechaHasta(String fechaHasta) {
        this.fechaHasta = fechaHasta;
    }

    public String getFechaDia() {
        return fechaDia;
    }

    public void setFechaDia(String fechaDia) {
        this.fechaDia = fechaDia;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }
}
