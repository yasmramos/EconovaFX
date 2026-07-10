package com.econovafx.modules.core.service;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.avaje.inject.Component;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Cliente para obtener tasas de cambio desde el sitio web del Banco Central de Cuba.
 * URL: https://www.bc.gob.cu/tasas-de-cambio
 */
@Component
public class BCCExchangeRateFetcher {

    private static final Logger log = LoggerFactory.getLogger(BCCExchangeRateFetcher.class);
    
    private static final String BCC_URL = "https://www.bc.gob.cu/tasas-de-cambio";
    
    // Timeout en milisegundos
    private static final int TIMEOUT = 10000;

    /**
     * Registro de tasa de cambio obtenida del BC
     */
    public record BCCRate(
            String currencyCode,
            String currencyName,
            BigDecimal rate,
            String symbol,
            LocalDate date,
            String source
    ) {}

    /**
     * Obtiene las tasas de cambio actuales desde el Banco Central de Cuba
     */
    public List<BCCRate> fetchCurrentRates() {
        log.debug("Conectando a {} para obtener tasas de cambio", BCC_URL);
        
        try {
            Document doc = Jsoup.connect(BCC_URL)
                    .timeout(TIMEOUT)
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                    .get();

            return parseRatesFromDocument(doc);

        } catch (IOException e) {
            log.error("Error conectando al Banco Central de Cuba: {}", e.getMessage());
            throw new RuntimeException("No se pudo conectar con el Banco Central de Cuba", e);
        }
    }

    /**
     * Analiza el documento HTML y extrae las tasas de cambio
     */
    private List<BCCRate> parseRatesFromDocument(Document doc) {
        List<BCCRate> rates = new ArrayList<>();
        
        try {
            // Buscar tablas que contengan tasas de cambio
            Elements tables = doc.select("table");
            
            for (Element table : tables) {
                // Buscar filas de la tabla
                Elements rows = table.select("tr");
                
                for (Element row : rows) {
                    Elements cells = row.select("td");
                    
                    if (cells.size() >= 2) {
                        try {
                            // Intentar extraer información de moneda y tasa
                            String currencyName = extractCurrencyName(cells);
                            String currencyCode = extractCurrencyCode(currencyName);
                            BigDecimal rate = extractRate(cells);
                            
                            if (currencyCode != null && rate != null) {
                                BCCRate bccRate = new BCCRate(
                                        currencyCode,
                                        currencyName,
                                        rate,
                                        extractSymbol(currencyCode),
                                        LocalDate.now(),
                                        "Banco Central de Cuba"
                                );
                                rates.add(bccRate);
                                log.debug("Tasa encontrada: {} = {} CUP", currencyCode, rate);
                            }
                        } catch (Exception e) {
                            log.debug("Error procesando fila: {}", e.getMessage());
                        }
                    }
                }
            }
            
            // Si no se encontró nada en tablas, intentar con otros selectores comunes
            if (rates.isEmpty()) {
                log.info("No se encontraron tasas en tablas, intentando con otros métodos...");
                rates = parseAlternativeSelectors(doc);
            }

        } catch (Exception e) {
            log.error("Error analizando documento del BC: {}", e.getMessage());
        }
        
        return rates;
    }

    /**
     * Métodos alternativos para extraer tasas si el formato estándar falla
     */
    private List<BCCRate> parseAlternativeSelectors(Document doc) {
        List<BCCRate> rates = new ArrayList<>();
        
        // Intentar buscar elementos con clases comunes para tasas de cambio
        Elements rateElements = doc.select("[class*=rate], [class*=exchange], [class*=currency]");
        
        for (Element element : rateElements) {
            try {
                String text = element.text().trim();
                if (text.contains("USD") || text.contains("EUR") || text.contains("GBP")) {
                    // Parsear manualmente
                    BCCRate rate = parseManualText(text);
                    if (rate != null) {
                        rates.add(rate);
                    }
                }
            } catch (Exception e) {
                log.debug("Error en parseo alternativo: {}", e.getMessage());
            }
        }
        
        return rates;
    }

    /**
     * Parsea texto manual buscando patrones de moneda
     */
    private BCCRate parseManualText(String text) {
        // Patrones comunes: "USD 24.00", "Euro 0.92", etc.
        String[] currencies = {"USD", "EUR", "GBP", "CHF", "CAD", "JPY", "CNY", "MXN"};
        
        for (String currency : currencies) {
            if (text.toUpperCase().contains(currency)) {
                try {
                    // Extraer número decimal del texto
                    String numberStr = text.replaceAll("[^0-9.,]", "").replace(",", ".");
                    BigDecimal rate = new BigDecimal(numberStr);
                    
                    return new BCCRate(
                            currency,
                            getCurrencyName(currency),
                            rate,
                            extractSymbol(currency),
                            LocalDate.now(),
                            "Banco Central de Cuba"
                    );
                } catch (Exception e) {
                    log.debug("Error parseando {}: {}", currency, e.getMessage());
                }
            }
        }
        
        return null;
    }

    /**
     * Extrae el nombre de la moneda de las celdas
     */
    private String extractCurrencyName(Elements cells) {
        // La primera celda usualmente contiene el nombre de la moneda
        if (!cells.isEmpty()) {
            return cells.get(0).text().trim();
        }
        return null;
    }

    /**
     * Extrae el código ISO de la moneda basado en el nombre
     */
    private String extractCurrencyCode(String currencyName) {
        if (currencyName == null) return null;
        
        String nameUpper = currencyName.toUpperCase();
        
        if (nameUpper.contains("DOLAR") || nameUpper.contains("USD") || nameUpper.contains("AMERICANO")) {
            return "USD";
        } else if (nameUpper.contains("EURO") || nameUpper.contains("EUR")) {
            return "EUR";
        } else if (nameUpper.contains("LIBRA") || nameUpper.contains("GBP") || nameUpper.contains("STERLING")) {
            return "GBP";
        } else if (nameUpper.contains("FRANCO") || nameUpper.contains("CHF")) {
            return "CHF";
        } else if (nameUpper.contains("YEN") || nameUpper.contains("JPY")) {
            return "JPY";
        } else if (nameUpper.contains("YUAN") || nameUpper.contains("CNY") || nameUpper.contains("RENMINBI")) {
            return "CNY";
        } else if (nameUpper.contains("PESO MEXICANO") || nameUpper.contains("MXN")) {
            return "MXN";
        } else if (nameUpper.contains("DOLAR CANADIENSE") || nameUpper.contains("CAD")) {
            return "CAD";
        }
        
        return null;
    }

    /**
     * Extrae la tasa de cambio de las celdas
     */
    private BigDecimal extractRate(Elements cells) {
        // Usualmente la tasa está en la segunda o tercera celda
        for (int i = 1; i < cells.size() && i <= 3; i++) {
            try {
                String text = cells.get(i).text().trim()
                        .replace("$", "")
                        .replace(",", ".")
                        .trim();
                
                // Intentar parsear como número decimal
                BigDecimal rate = new BigDecimal(text);
                if (rate.compareTo(BigDecimal.ZERO) > 0) {
                    return rate;
                }
            } catch (NumberFormatException e) {
                // Continuar con la siguiente celda
            }
        }
        return null;
    }

    /**
     * Obtiene el símbolo de la moneda
     */
    private String extractSymbol(String currencyCode) {
        return switch (currencyCode) {
            case "USD" -> "$";
            case "EUR" -> "€";
            case "GBP" -> "£";
            case "JPY" -> "¥";
            case "CNY" -> "¥";
            case "CHF" -> "Fr";
            case "CAD" -> "C$";
            case "MXN" -> "MX$";
            default -> currencyCode;
        };
    }

    /**
     * Obtiene el nombre completo de la moneda
     */
    private String getCurrencyName(String currencyCode) {
        return switch (currencyCode) {
            case "USD" -> "Dólar Estadounidense";
            case "EUR" -> "Euro";
            case "GBP" -> "Libra Esterlina";
            case "JPY" -> "Yen Japonés";
            case "CNY" -> "Yuan Chino";
            case "CHF" -> "Franco Suizo";
            case "CAD" -> "Dólar Canadiense";
            case "MXN" -> "Peso Mexicano";
            default -> currencyCode;
        };
    }
}
