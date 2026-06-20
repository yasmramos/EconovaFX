package com.econovafx.external.bc;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.cache.annotation.Cacheable;

/**
 * Servicio para consumir la API de tasas de cambio del Banco Central de Cuba.
 */
@Service
public class BancoCentralService {
    
    private static final String BASE_URL = "https://api.bc.gob.cu/v1/tasas-de-cambio";
    private final RestTemplate restTemplate;
    
    public BancoCentralService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }
    
    /**
     * Obtiene las tasas de cambio activas actuales.
     * Los resultados se cachean por 1 hora para reducir la carga en la API externa.
     */
    @Cacheable(value = "tasas-activas", unless = "#result == null")
    public TasasActivasResponse obtenerTasasActivas() {
        return restTemplate.getForObject(BASE_URL + "/activas", TasasActivasResponse.class);
    }
    
    /**
     * Obtiene las tasas de cambio para una fecha específica.
     * @param fecha Fecha en formato YYYY-MM-DD
     */
    @Cacheable(value = "tasas-por-fecha", key = "#fecha", unless = "#result == null")
    public TasasActivasResponse obtenerTasasPorFecha(String fecha) {
        String url = BASE_URL + "/activas-por-fecha?fecha=" + fecha;
        return restTemplate.getForObject(url, TasasActivasResponse.class);
    }
    
    /**
     * Obtiene el histórico de tasas de cambio.
     * @param fechaInicio Fecha de inicio en formato YYYY-MM-DD
     * @param fechaFin Fecha de fin en formato YYYY-MM-DD
     * @param codigoMoneda Código de moneda opcional (ej. USD, EUR)
     */
    @Cacheable(value = "tasas-historico", key = "#fechaInicio + '-' + #fechaFin + '-' + #codigoMoneda", unless = "#result == null")
    public TasasActivasResponse obtenerHistorico(String fechaInicio, String fechaFin, String codigoMoneda) {
        StringBuilder url = new StringBuilder(BASE_URL + "/historico?");
        url.append("fechaInicio=").append(fechaInicio);
        url.append("&fechaFin=").append(fechaFin);
        if (codigoMoneda != null && !codigoMoneda.isEmpty()) {
            url.append("&codigoMoneda=").append(codigoMoneda);
        }
        return restTemplate.getForObject(url.toString(), TasasActivasResponse.class);
    }
}
