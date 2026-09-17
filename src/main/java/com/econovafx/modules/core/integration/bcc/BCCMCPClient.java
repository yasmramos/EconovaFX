package com.econovafx.modules.core.integration.bcc;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Cliente para el Model Context Protocol (MCP) del Banco Central de Cuba.
 * Permite conectar asistentes y agentes de IA con datos oficiales de tasas de cambio.
 * 
 * Uso básico:
 * 1. Inicializar sesión con initialize()
 * 2. Usar el sessionId retornado para llamar a tools/call
 */
public class BCCMCPClient {

    private static final Logger log = LoggerFactory.getLogger(BCCMCPClient.class);
    private static final String MCP_ENDPOINT = "https://api.bc.gob.cu/v1/mcp";
    
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    private String sessionId;
    private boolean initialized = false;

    public BCCMCPClient() {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(30))
                .build();
        this.objectMapper = new ObjectMapper();
    }

    /**
     * Inicializa la sesión MCP con el servidor del Banco Central.
     * Debe llamarse antes de usar cualquier herramienta.
     * 
     * @return El ID de sesión para usar en llamadas posteriores
     * @throws IOException si falla la conexión
     * @throws InterruptedException si se interrumpe la llamada HTTP
     */
    public String initialize() throws IOException, InterruptedException {
        log.info("Inicializando sesión MCP con el Banco Central de Cuba...");
        
        Map<String, Object> request = new HashMap<>();
        request.put("jsonrpc", "2.0");
        request.put("id", 1);
        request.put("method", "initialize");
        
        Map<String, String> params = new HashMap<>();
        params.put("protocolVersion", "2025-11-25");
        request.put("params", params);

        HttpRequest httpRequest = HttpRequest.newBuilder()
                .uri(URI.create(MCP_ENDPOINT))
                .header("Content-Type", "application/json")
                .header("Accept", "application/json, text/event-stream")
                .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(request)))
                .timeout(Duration.ofSeconds(30))
                .build();

        HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
        
        if (response.statusCode() != 200) {
            throw new IOException("Error al inicializar MCP: HTTP " + response.statusCode() + " - " + response.body());
        }

        // Extraer sessionId del header
        this.sessionId = response.headers().firstValue("MCP-Session-Id").orElse(null);
        if (this.sessionId == null || this.sessionId.isEmpty()) {
            throw new IOException("No se recibió MCP-Session-Id en la respuesta");
        }

        this.initialized = true;
        log.info("Sesión MCP inicializada exitosamente. SessionId: {}", this.sessionId);
        
        return this.sessionId;
    }

    /**
     * Llama a una herramienta MCP específica.
     * 
     * @param toolName Nombre de la herramienta (ej. "get_active_rates")
     * @param arguments Argumentos para la herramienta
     * @return Respuesta JSON de la herramienta
     * @throws IOException si falla la conexión
     * @throws InterruptedException si se interrumpe la llamada HTTP
     * @throws IllegalStateException si no se ha inicializado la sesión
     */
    public JsonNode callTool(String toolName, Map<String, Object> arguments) 
            throws IOException, InterruptedException {
        
        if (!initialized) {
            throw new IllegalStateException("La sesión MCP no está inicializada. Llame a initialize() primero.");
        }

        log.info("Llamando a herramienta MCP: {} con argumentos: {}", toolName, arguments);

        Map<String, Object> request = new HashMap<>();
        request.put("jsonrpc", "2.0");
        request.put("id", System.currentTimeMillis()); // ID único por llamada
        request.put("method", "tools/call");
        
        Map<String, Object> params = new HashMap<>();
        params.put("name", toolName);
        params.put("arguments", arguments != null ? arguments : new HashMap<>());
        request.put("params", params);

        HttpRequest httpRequest = HttpRequest.newBuilder()
                .uri(URI.create(MCP_ENDPOINT))
                .header("Content-Type", "application/json")
                .header("Accept", "application/json, text/event-stream")
                .header("MCP-Session-Id", this.sessionId)
                .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(request)))
                .timeout(Duration.ofSeconds(30))
                .build();

        HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
        
        if (response.statusCode() != 200) {
            throw new IOException("Error al llamar herramienta MCP: HTTP " + response.statusCode() + " - " + response.body());
        }

        JsonNode jsonResponse = objectMapper.readTree(response.body());
        
        // Verificar errores en la respuesta JSON-RPC
        if (jsonResponse.has("error")) {
            JsonNode error = jsonResponse.get("error");
            throw new IOException("Error MCP: " + error.get("message").asText());
        }

        log.info("Herramienta MCP ejecutada exitosamente: {}", toolName);
        return jsonResponse.get("result");
    }

    /**
     * Obtiene las tasas de cambio activas usando la herramienta MCP.
     * 
     * @param codigoMoneda Código de moneda opcional (ej. "USD", "EUR"). Si es null, devuelve todas.
     * @return Nodo JSON con las tasas activas
     * @throws IOException si falla la conexión
     * @throws InterruptedException si se interrumpe la llamada HTTP
     */
    public JsonNode getActiveRates(String codigoMoneda) throws IOException, InterruptedException {
        Map<String, Object> args = new HashMap<>();
        if (codigoMoneda != null && !codigoMoneda.isEmpty()) {
            args.put("codigoMoneda", codigoMoneda);
        }
        return callTool("get_active_rates", args);
    }

    /**
     * Obtiene tasas de cambio para una fecha específica.
     * 
     * @param fecha Fecha en formato YYYY-MM-DD
     * @param codigoMoneda Código de moneda opcional
     * @return Nodo JSON con las tasas de la fecha
     * @throws IOException si falla la conexión
     * @throws InterruptedException si se interrumpe la llamada HTTP
     */
    public JsonNode getRatesByDate(String fecha, String codigoMoneda) throws IOException, InterruptedException {
        Map<String, Object> args = new HashMap<>();
        args.put("fecha", fecha);
        if (codigoMoneda != null && !codigoMoneda.isEmpty()) {
            args.put("codigoMoneda", codigoMoneda);
        }
        return callTool("get_rates_by_date", args);
    }

    /**
     * Obtiene histórico de tasas de cambio.
     * 
     * @param fechaInicio Fecha inicio YYYY-MM-DD
     * @param fechaFin Fecha fin YYYY-MM-DD
     * @param codigoMoneda Código de moneda opcional
     * @return Nodo JSON con el histórico
     * @throws IOException si falla la conexión
     * @throws InterruptedException si se interrumpe la llamada HTTP
     */
    public JsonNode getHistoricalRates(String fechaInicio, String fechaFin, String codigoMoneda) 
            throws IOException, InterruptedException {
        
        Map<String, Object> args = new HashMap<>();
        args.put("fechaInicio", fechaInicio);
        args.put("fechaFin", fechaFin);
        if (codigoMoneda != null && !codigoMoneda.isEmpty()) {
            args.put("codigoMoneda", codigoMoneda);
        }
        return callTool("get_historical_rates", args);
    }

    /**
     * Verifica si la sesión está inicializada.
     */
    public boolean isInitialized() {
        return initialized;
    }

    /**
     * Obtiene el ID de sesión actual.
     */
    public Optional<String> getSessionId() {
        return Optional.ofNullable(sessionId);
    }
}
