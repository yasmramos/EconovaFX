package com.econovafx.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.avaje.inject.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Cliente HTTP para la API oficial del Banco Central de Cuba.
 * Endpoints: https://api.bc.gob.cu/v1/tasas-de-cambio
 */
@Component
public class BCCExchangeRateClient {

    private static final Logger log = LoggerFactory.getLogger(BCCExchangeRateClient.class);
    
    private final String baseUrl;
    private final int timeoutSeconds;
    private final int maxRetryAttempts;
    private final long retryDelayMs;
    private final ObjectMapper objectMapper;
    private final HttpClient httpClient;

    public BCCExchangeRateClient() {
        // Configuración desde propiedades (valores por defecto si no existen)
        this.baseUrl = System.getProperty("bcc.api.base.url", "https://api.bc.gob.cu/v1/tasas-de-cambio");
        this.timeoutSeconds = Integer.getInteger("bcc.api.timeout.seconds", 30);
        this.maxRetryAttempts = Integer.getInteger("bcc.api.retry.max.attempts", 3);
        this.retryDelayMs = Long.getLong("bcc.api.retry.delay.ms", 2000);
        
        this.objectMapper = new ObjectMapper();
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(timeoutSeconds))
                .build();
    }

    /**
     * Registro de tasa de cambio obtenida de la API del BC
     */
    public record BCCRateDTO(
            String codigoMoneda,
            String nombreMoneda,
            BigDecimal tasa,
            LocalDate fecha,
            String fuente
    ) {
        // Getters explícitos para compatibilidad con JavaFX y Expression Language
        public String getCodigoMoneda() { return codigoMoneda; }
        public String getNombreMoneda() { return nombreMoneda; }
        public BigDecimal getTasa() { return tasa; }
        public LocalDate getFecha() { return fecha; }
        public String getFuente() { return fuente; }
    }

    /**
     * Obtiene las tasas de cambio activas desde la API del Banco Central de Cuba
     * GET /activas
     */
    public List<BCCRateDTO> fetchActiveRates() {
        return fetchWithRetry("/activas", null);
    }

    /**
     * Obtiene tasas de cambio para una fecha específica
     * GET /activas-por-fecha?fecha=YYYY-MM-DD
     */
    public List<BCCRateDTO> fetchRatesByDate(LocalDate date) {
        if (date == null) {
            date = LocalDate.now();
        }
        String params = "?fecha=" + date.toString();
        return fetchWithRetry("/activas-por-fecha" + params, null);
    }

    /**
     * Obtiene histórico de tasas de cambio
     * GET /historico?fechaInicio=YYYY-MM-DD&fechaFin=YYYY-MM-DD&codigoMoneda=XXX
     */
    public List<BCCRateDTO> fetchHistoricalRates(LocalDate startDate, 
                                                  LocalDate endDate, 
                                                  String currencyCode) {
        StringBuilder params = new StringBuilder("?");
        if (startDate != null) {
            params.append("fechaInicio=").append(startDate.toString()).append("&");
        }
        if (endDate != null) {
            params.append("fechaFin=").append(endDate.toString()).append("&");
        }
        if (currencyCode != null && !currencyCode.isEmpty()) {
            params.append("codigoMoneda=").append(currencyCode).append("&");
        }
        
        String queryString = params.toString();
        if (queryString.endsWith("&")) {
            queryString = queryString.substring(0, queryString.length() - 1);
        }
        
        return fetchWithRetry("/historico" + queryString, null);
    }

    /**
     * Método genérico con reintentos automáticos
     */
    private List<BCCRateDTO> fetchWithRetry(String endpoint, String fullUrl) {
        String url = fullUrl != null ? fullUrl : baseUrl + endpoint;
        int attempt = 0;
        
        while (attempt < maxRetryAttempts) {
            try {
                log.debug("Intentando obtener datos de: {} (intento {}/{})", 
                         url, attempt + 1, maxRetryAttempts);
                
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(url))
                        .GET()
                        .header("Accept", "application/json")
                        .header("User-Agent", "EconoNovaFX/1.0")
                        .timeout(Duration.ofSeconds(timeoutSeconds))
                        .build();

                HttpResponse<String> response = httpClient.send(request, 
                        HttpResponse.BodyHandlers.ofString());

                if (response.statusCode() == 200) {
                    return parseResponse(response.body());
                } else if (response.statusCode() == 429) {
                    // Rate limiting - esperar más tiempo
                    log.warn("Rate limiting detectado. Esperando {} ms antes del siguiente intento.", 
                            retryDelayMs * 2);
                    Thread.sleep(retryDelayMs * 2);
                } else {
                    log.error("Error en la respuesta del BC: HTTP {}", response.statusCode());
                    throw new RuntimeException("Error en API del BC: HTTP " + response.statusCode());
                }

            } catch (IOException e) {
                log.error("Error de conexión con el BC (intento {}/{}): {}", 
                         attempt + 1, maxRetryAttempts, e.getMessage());
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.error("Hilo interrumpido durante reintento");
                throw new RuntimeException("Operación interrumpida", e);
            } catch (Exception e) {
                log.error("Error inesperado: {}", e.getMessage(), e);
            }
            
            attempt++;
            if (attempt < maxRetryAttempts) {
                try {
                    log.debug("Esperando {} ms antes del reintento...", retryDelayMs);
                    Thread.sleep(retryDelayMs);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException("Reintento interrumpido", e);
                }
            }
        }
        
        log.error("Se agotaron los {} intentos para obtener datos del BC", maxRetryAttempts);
        throw new RuntimeException("No se pudo conectar con el Banco Central de Cuba después de " 
                + maxRetryAttempts + " intentos");
    }

    /**
     * Parsea la respuesta JSON de la API
     */
    private List<BCCRateDTO> parseResponse(String jsonResponse) {
        try {
            List<BCCRateDTO> rates = new ArrayList<>();
            JsonNode rootNode = objectMapper.readTree(jsonResponse);
            
            // La API puede devolver un array directamente o un objeto con data
            JsonNode dataArray = rootNode.isArray() ? rootNode : rootNode.path("data");
            
            for (JsonNode item : dataArray) {
                try {
                    String codigoMoneda = item.path("codigoMoneda").asText();
                    String nombreMoneda = item.path("nombreMoneda").asText();
                    BigDecimal tasa = item.path("tasa").decimalValue();
                    String fechaStr = item.path("fecha").asText();
                    LocalDate fecha = fechaStr.isEmpty() ? LocalDate.now() : LocalDate.parse(fechaStr);
                    String fuente = item.path("fuente").asText("Banco Central de Cuba");
                    
                    if (codigoMoneda != null && !codigoMoneda.isEmpty() && tasa != null) {
                        rates.add(new BCCRateDTO(codigoMoneda, nombreMoneda, tasa, fecha, fuente));
                        log.debug("Tasa parseada: {} = {} CUP ({})", codigoMoneda, tasa, fecha);
                    }
                } catch (Exception e) {
                    log.warn("Error procesando un item de la respuesta: {}", e.getMessage());
                }
            }
            
            if (rates.isEmpty()) {
                log.warn("La API no devolvió tasas válidas");
            } else {
                log.info("Se obtuvieron {} tasas de cambio de la API del BC", rates.size());
            }
            
            return rates;
            
        } catch (Exception e) {
            log.error("Error parseando respuesta JSON del BC: {}", e.getMessage(), e);
            throw new RuntimeException("Error procesando datos del Banco Central de Cuba", e);
        }
    }
}
