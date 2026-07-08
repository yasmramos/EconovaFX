package com.econovafx.model;

import jakarta.persistence.*;

/**
 * Enumeración que define los métodos de valoración de inventario soportados.
 * 
 * Métodos disponibles:
 * - FIFO (PEPS): Primeras Entradas, Primeras Salidas. Ideal para productos perecederos.
 * - Weighted Average Cost (Promedio Ponderado): Costo promedio de todas las unidades.
 *   Ideal para productos homogéneos con rotación constante.
 */
public enum ValuationMethod {
    /**
     * PEPS (FIFO): First In, First Out.
     * Las primeras unidades en entrar son las primeras en salir.
     * El inventario final queda valorado a los costos más recientes.
     */
    FIFO("PEPS - Primeras Entradas, Primeras Salidas"),
    
    /**
     * Costo Promedio Ponderado.
     * Calcula el costo promedio de todas las unidades disponibles.
     * Suaviza las fluctuaciones de precios.
     */
    WEIGHTED_AVERAGE("Promedio Ponderado");
    
    private final String description;
    
    ValuationMethod(String description) {
        this.description = description;
    }
    
    public String getDescription() {
        return description;
    }
}
