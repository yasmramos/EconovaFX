package com.econovafx.external.bc;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Modelo para la respuesta de tasas de cambio activas del Banco Central de Cuba.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class TasasActivasResponse {
    
    @JsonProperty("tasas")
    private TasaCambio[] tasas;
    
    @JsonProperty("fechaDia")
    private String fechaDia;
    
    @JsonProperty("fechaHoy")
    private String fechaHoy;

    public TasaCambio[] getTasas() {
        return tasas;
    }

    public void setTasas(TasaCambio[] tasas) {
        this.tasas = tasas;
    }

    public String getFechaDia() {
        return fechaDia;
    }

    public void setFechaDia(String fechaDia) {
        this.fechaDia = fechaDia;
    }

    public String getFechaHoy() {
        return fechaHoy;
    }

    public void setFechaHoy(String fechaHoy) {
        this.fechaHoy = fechaHoy;
    }
}
