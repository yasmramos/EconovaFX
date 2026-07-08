package com.econovafx.service;

import com.econovafx.model.Currency;
import com.econovafx.model.ExchangeRate;
import com.econovafx.model.User;
import com.econovafx.repository.CurrencyRepository;
import com.econovafx.repository.ExchangeRateRepository;
import com.econovafx.security.SecurityUtil;
import io.avaje.inject.Component;
import com.econovafx.security.RequiresTenant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Servicio para gestión de tipos de cambio con integración al Banco Central de Cuba.
 * Soporta la obtención automática de tasas desde www.bc.gob.cu
 */
@Component
@RequiresTenant
public class ExchangeRateService {

    private static final Logger log = LoggerFactory.getLogger(ExchangeRateService.class);

    private final ExchangeRateRepository exchangeRateRepository;
    private final CurrencyRepository currencyRepository;
    private final BCCExchangeRateFetcher bccFetcher;

    public ExchangeRateService(ExchangeRateRepository exchangeRateRepository,
                               CurrencyRepository currencyRepository,
                               BCCExchangeRateFetcher bccFetcher) {
        this.exchangeRateRepository = exchangeRateRepository;
        this.currencyRepository = currencyRepository;
        this.bccFetcher = bccFetcher;
    }

    /**
     * Obtiene la tasa de cambio vigente entre dos monedas en una fecha determinada
     */
    public Optional<ExchangeRate> getExchangeRate(String fromCurrencyCode, 
                                                   String toCurrencyCode, 
                                                   LocalDateTime date) {
        Currency fromCurrency = currencyRepository.findByCode(fromCurrencyCode)
                .orElseThrow(() -> new IllegalArgumentException("Moneda no encontrada: " + fromCurrencyCode));
        Currency toCurrency = currencyRepository.findByCode(toCurrencyCode)
                .orElseThrow(() -> new IllegalArgumentException("Moneda no encontrada: " + toCurrencyCode));

        return exchangeRateRepository.findCurrentRate(fromCurrency, toCurrency, date);
    }

    /**
     * Convierte un monto de una moneda a otra usando la tasa vigente
     */
    public BigDecimal convertAmount(BigDecimal amount, 
                                    String fromCurrencyCode, 
                                    String toCurrencyCode,
                                    LocalDateTime date) {
        if (date == null) {
            date = LocalDateTime.now();
        }

        Currency fromCurrency = currencyRepository.findByCode(fromCurrencyCode)
                .orElseThrow(() -> new IllegalArgumentException("Moneda no encontrada: " + fromCurrencyCode));
        Currency toCurrency = currencyRepository.findByCode(toCurrencyCode)
                .orElseThrow(() -> new IllegalArgumentException("Moneda no encontrada: " + toCurrencyCode));

        return exchangeRateRepository.convertAmount(amount, fromCurrency, toCurrency, date);
    }

    /**
     * Registra manualmente una nueva tasa de cambio
     */
    public ExchangeRate registerExchangeRate(String fromCurrencyCode,
                                              String toCurrencyCode,
                                              BigDecimal rate,
                                              LocalDateTime effectiveDate,
                                              ExchangeRate.RateType rateType,
                                              String observations) {
        Currency fromCurrency = currencyRepository.findByCode(fromCurrencyCode)
                .orElseThrow(() -> new IllegalArgumentException("Moneda no encontrada: " + fromCurrencyCode));
        Currency toCurrency = currencyRepository.findByCode(toCurrencyCode)
                .orElseThrow(() -> new IllegalArgumentException("Moneda no encontrada: " + toCurrencyCode));

        ExchangeRate exchangeRate = new ExchangeRate(fromCurrency, toCurrency, rate, effectiveDate);
        exchangeRate.setRateType(rateType);
        exchangeRate.setObservations(observations);
        exchangeRate.setStatus("ACTIVE");

        // Usuario actual como creador
        User currentUser = SecurityUtil.getCurrentUser();
        if (currentUser != null) {
            exchangeRate.setCreatedByUser(currentUser);
        }

        return exchangeRateRepository.save(exchangeRate);
    }

    /**
     * Obtiene tasas de cambio automáticamente desde el Banco Central de Cuba
     * y las registra en el sistema
     */
    public List<ExchangeRate> fetchAndSaveRatesFromBCC() {
        log.info("Iniciando obtención de tasas de cambio desde el Banco Central de Cuba...");
        
        try {
            // Obtener tasas desde el BC
            List<BCCExchangeRateFetcher.BCCRate> bccRates = bccFetcher.fetchCurrentRates();
            
            if (bccRates.isEmpty()) {
                log.warn("No se obtuvieron tasas de cambio del Banco Central de Cuba");
                return List.of();
            }

            // Moneda base asumida: CUP (Código ISO para Peso Cubano)
            Currency cupCurrency = currencyRepository.findByCode("CUP")
                    .orElseGet(() -> {
                        Currency newCurrency = new Currency("CUP", "Peso Cubano", "$");
                        newCurrency.setIsBase(true);
                        newCurrency.setStatus("ACTIVE");
                        return currencyRepository.save(newCurrency);
                    });

            List<ExchangeRate> savedRates = new java.util.ArrayList<>();
            LocalDateTime now = LocalDateTime.now();

            for (BCCExchangeRateFetcher.BCCRate bccRate : bccRates) {
                try {
                    // Buscar o crear la moneda
                    Currency foreignCurrency = currencyRepository.findByCode(bccRate.currencyCode())
                            .orElseGet(() -> {
                                Currency newCurrency = new Currency(
                                        bccRate.currencyCode(),
                                        bccRate.currencyName(),
                                        bccRate.symbol()
                                );
                                newCurrency.setIsBase(false);
                                newCurrency.setStatus("ACTIVE");
                                return currencyRepository.save(newCurrency);
                            });

                    // Registrar tasa: 1 unidad de moneda extranjera = X CUP
                    ExchangeRate exchangeRate = new ExchangeRate(
                            foreignCurrency,
                            cupCurrency,
                            bccRate.rate(),
                            now
                    );
                    exchangeRate.setRateType(ExchangeRate.RateType.OFICIAL);
                    exchangeRate.setObservations("Tasa oficial del Banco Central de Cuba - " + bccRate.source());
                    exchangeRate.setStatus("ACTIVE");
                    
                    User currentUser = SecurityUtil.getCurrentUser();
                    if (currentUser != null) {
                        exchangeRate.setCreatedByUser(currentUser);
                    }

                    ExchangeRate saved = exchangeRateRepository.save(exchangeRate);
                    savedRates.add(saved);

                    log.info("Tasa registrada: 1 {} = {} {} ({})", 
                             bccRate.currencyCode(), 
                             bccRate.rate(), 
                             "CUP",
                             bccRate.source());

                } catch (Exception e) {
                    log.error("Error procesando tasa para {}: {}", bccRate.currencyCode(), e.getMessage());
                }
            }

            log.info("Se registraron {} tasas de cambio desde el Banco Central de Cuba", savedRates.size());
            return savedRates;

        } catch (Exception e) {
            log.error("Error obteniendo tasas del Banco Central de Cuba: {}", e.getMessage(), e);
            throw new RuntimeException("Error conectando con el Banco Central de Cuba: " + e.getMessage(), e);
        }
    }

    /**
     * Obtiene el histórico de tasas de cambio entre dos monedas
     */
    public List<ExchangeRate> getExchangeRateHistory(String fromCurrencyCode,
                                                      String toCurrencyCode,
                                                      int days) {
        Currency fromCurrency = currencyRepository.findByCode(fromCurrencyCode)
                .orElseThrow(() -> new IllegalArgumentException("Moneda no encontrada: " + fromCurrencyCode));
        Currency toCurrency = currencyRepository.findByCode(toCurrencyCode)
                .orElseThrow(() -> new IllegalArgumentException("Moneda no encontrada: " + toCurrencyCode));

        return exchangeRateRepository.findHistory(fromCurrency, toCurrency, days);
    }

    /**
     * Obtiene todas las tasas de cambio activas
     */
    public List<ExchangeRate> getAllActiveRates() {
        return exchangeRateRepository.findAllActive();
    }

    /**
     * Calcula la tasa de cambio cruzada entre dos monedas usando una base intermedia
     */
    public BigDecimal calculateCrossRate(String fromCurrencyCode,
                                          String toCurrencyCode,
                                          String baseCurrencyCode,
                                          LocalDateTime date) {
        Currency fromCurrency = currencyRepository.findByCode(fromCurrencyCode)
                .orElseThrow(() -> new IllegalArgumentException("Moneda no encontrada: " + fromCurrencyCode));
        Currency toCurrency = currencyRepository.findByCode(toCurrencyCode)
                .orElseThrow(() -> new IllegalArgumentException("Moneda no encontrada: " + toCurrencyCode));
        Currency baseCurrency = currencyRepository.findByCode(baseCurrencyCode)
                .orElseThrow(() -> new IllegalArgumentException("Moneda base no encontrada: " + baseCurrencyCode));

        return exchangeRateRepository.calculateCrossRate(fromCurrency, toCurrency, baseCurrency, date);
    }

    /**
     * Elimina/desactiva una tasa de cambio por ID
     */
    public void deactivateExchangeRate(Long id) {
        exchangeRateRepository.deactivate(id);
        log.info("Tasa de cambio {} desactivada", id);
    }

    /**
     * Obtiene una tasa de cambio específica por ID
     */
    public Optional<ExchangeRate> getExchangeRateById(Long id) {
        return exchangeRateRepository.findById(id);
    }

    /**
     * Fetches and saves latest rates from BCC (alias for fetchAndSaveRatesFromBCC)
     */
    public List<ExchangeRate> fetchAndSaveLatestRates() {
        return fetchAndSaveRatesFromBCC();
    }

    /**
     * Gets latest rates for all currencies (active rates)
     */
    public List<ExchangeRate> getLatestRatesForAllCurrencies() {
        return getAllActiveRates();
    }

    /**
     * Gets exchange rates by date range and optional currency filter
     */
    public List<ExchangeRate> getExchangeRatesByDateRange(LocalDate from, LocalDate to, String currencyCode) {
        if (currencyCode != null && !currencyCode.isEmpty()) {
            Currency currency = currencyRepository.findByCode(currencyCode)
                .orElseThrow(() -> new IllegalArgumentException("Moneda no encontrada: " + currencyCode));
            return exchangeRateRepository.findRatesByDateRangeAndCurrency(from.atStartOfDay(), to.atTime(23, 59, 59), currency);
        } else {
            return exchangeRateRepository.findRatesByDateRange(from.atStartOfDay(), to.atTime(23, 59, 59));
        }
    }

    /**
     * Gets the last update time from the most recent exchange rate
     */
    public Optional<LocalDateTime> getLastUpdateTime() {
        List<ExchangeRate> allRates = getAllActiveRates();
        if (allRates.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(allRates.stream()
            .map(ExchangeRate::getEffectiveDate)
            .max(LocalDateTime::compareTo)
            .orElse(null));
    }
}
