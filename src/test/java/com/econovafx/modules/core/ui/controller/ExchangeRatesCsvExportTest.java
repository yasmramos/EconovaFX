package com.econovafx.modules.core.ui.controller;

import com.econovafx.modules.core.model.Currency;
import com.econovafx.modules.core.model.ExchangeRate;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Covers the CSV body the exchange-rate page writes on export. The builder is
 * package-private and free of JavaFX state, so the column order and the field
 * escaping can be pinned without opening a window.
 */
class ExchangeRatesCsvExportTest {

    private static ExchangeRate rate(String code, String name, String value, LocalDateTime effective) {
        Currency currency = new Currency();
        currency.setCode(code);
        currency.setName(name);
        ExchangeRate rate = new ExchangeRate();
        rate.setToCurrency(currency);
        rate.setRate(new BigDecimal(value));
        rate.setEffectiveDate(effective);
        return rate;
    }

    @Test
    void writesHeaderAndOneLinePerRate() {
        String csv = ExchangeRatesController.buildRatesCsv(List.of(
                rate("USD", "Dólar", "24.5000", LocalDateTime.of(2026, 1, 2, 9, 30)),
                rate("EUR", "Euro", "26.1000", LocalDateTime.of(2026, 1, 3, 10, 0))));

        String[] lines = csv.split("\n");
        assertEquals("Moneda,Nombre,Tasa,Fecha,Tipo", lines[0]);
        assertEquals(3, lines.length);
        assertTrue(lines[1].startsWith("USD,Dólar,24.5000,2026-01-02 09:30,"), lines[1]);
        assertTrue(lines[2].startsWith("EUR,Euro,26.1000,2026-01-03 10:00,"), lines[2]);
    }

    @Test
    void escapesFieldsWithCommasAndQuotes() {
        String csv = ExchangeRatesController.buildRatesCsv(List.of(
                rate("USD", "US, Dollar \"official\"", "24.5", LocalDateTime.of(2026, 1, 2, 9, 30))));

        assertTrue(csv.contains("\"US, Dollar \"\"official\"\"\""), csv);
    }

    @Test
    void toleratesNullFields() {
        ExchangeRate sparse = new ExchangeRate();
        String csv = ExchangeRatesController.buildRatesCsv(List.of(sparse));

        assertEquals("Moneda,Nombre,Tasa,Fecha,Tipo\n,,,,OFICIAL\n", csv);
    }
}
