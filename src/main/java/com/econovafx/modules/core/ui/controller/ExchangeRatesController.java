package com.econovafx.modules.core.ui.controller;

import com.econovafx.modules.core.model.ExchangeRate;
import com.econovafx.modules.core.service.ExchangeRateService;
import com.econovafx.modules.core.ui.util.ModernDialog;
// Avaje Inject only honours its own @Inject; jakarta.inject.Inject was silently
// ignored, leaving exchangeRateService null at runtime (NPE on initialize()).
import jakarta.inject.Inject;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import java.time.LocalDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

/**
 * Controller for Exchange Rates management UI
 */
@io.avaje.inject.Component
public class ExchangeRatesController {

    private static final Logger logger = LoggerFactory.getLogger(ExchangeRatesController.class);

    private final ExchangeRateService exchangeRateService;

    /**
     * Avaje Inject has no {@code @Inject} annotation: it wires beans through a single
     * constructor. The previous field injection was therefore never applied and
     * initialize() threw an NPE on the first call to load the active rates.
     */
    public ExchangeRatesController(ExchangeRateService exchangeRateService) {
        this.exchangeRateService = exchangeRateService;
    }

    @FXML
    private VBox rootView;

    @FXML
    private TableView<ExchangeRate> activeRatesTable;

    @FXML
    private TableColumn<ExchangeRate, String> currencyCodeCol;

    @FXML
    private TableColumn<ExchangeRate, String> currencyNameCol;

    @FXML
    private TableColumn<ExchangeRate, Double> buyRateCol;

    @FXML
    private TableColumn<ExchangeRate, Double> sellRateCol;

    @FXML
    private TableColumn<ExchangeRate, LocalDate> dateCol;

    @FXML
    private TableColumn<ExchangeRate, String> sourceCol;

    @FXML
    private TableView<ExchangeRate> historicalRatesTable;

    @FXML
    private TableColumn<ExchangeRate, String> histCurrencyCodeCol;

    @FXML
    private TableColumn<ExchangeRate, LocalDate> histDateCol;

    @FXML
    private TableColumn<ExchangeRate, Double> histBuyRateCol;

    @FXML
    private TableColumn<ExchangeRate, Double> histSellRateCol;

    @FXML
    private DatePicker fromDatePicker;

    @FXML
    private DatePicker toDatePicker;

    @FXML
    private ComboBox<String> currencyFilterCombo;

    @FXML
    private Label statusLabel;

    @FXML
    private Label lastUpdateLabel;

    private ObservableList<ExchangeRate> activeRatesData = FXCollections.observableArrayList();
    private ObservableList<ExchangeRate> historicalRatesData = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        setupTables();
        loadActiveRates();
        loadHistoricalRates();
        updateStatusLabels();
    }

    private void setupTables() {
        // Active rates table columns
        currencyCodeCol.setCellValueFactory(data -> 
            javafx.beans.binding.Bindings.createObjectBinding(() -> 
                data.getValue().getToCurrency().getCode()));
        
        currencyNameCol.setCellValueFactory(data -> 
            javafx.beans.binding.Bindings.createObjectBinding(() -> 
                data.getValue().getToCurrency().getName()));
        
        buyRateCol.setCellValueFactory(data -> 
            new javafx.beans.property.SimpleObjectProperty<>(data.getValue().getRate().doubleValue()));
        buyRateCol.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(String.format("%.4f", item));
                }
            }
        });
        
        sellRateCol.setCellValueFactory(data -> 
            new javafx.beans.property.SimpleObjectProperty<>(data.getValue().getRate().doubleValue()));
        sellRateCol.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(String.format("%.4f", item));
                }
            }
        });
        
        dateCol.setCellValueFactory(data -> 
            new javafx.beans.property.SimpleObjectProperty<>(data.getValue().getEffectiveDate().toLocalDate()));
        
        sourceCol.setCellValueFactory(data -> 
            new javafx.beans.property.SimpleStringProperty(
                data.getValue().getObservations() != null ? "BCC API" : "Manual"));

        // Historical rates table columns
        histCurrencyCodeCol.setCellValueFactory(data -> 
            javafx.beans.binding.Bindings.createObjectBinding(() -> 
                data.getValue().getToCurrency().getCode()));
        
        histDateCol.setCellValueFactory(data -> 
            new javafx.beans.property.SimpleObjectProperty<>(data.getValue().getEffectiveDate().toLocalDate()));
        
        histBuyRateCol.setCellValueFactory(data -> 
            new javafx.beans.property.SimpleObjectProperty<>(data.getValue().getRate().doubleValue()));
        histBuyRateCol.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(String.format("%.4f", item));
                }
            }
        });
        
        histSellRateCol.setCellValueFactory(data -> 
            new javafx.beans.property.SimpleObjectProperty<>(data.getValue().getRate().doubleValue()));
        histSellRateCol.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(String.format("%.4f", item));
                }
            }
        });

        // Initialize date pickers
        fromDatePicker.setValue(LocalDate.now().minusMonths(1));
        toDatePicker.setValue(LocalDate.now());
    }

    @FXML
    private void handleRefreshRates() {
        try {
            statusLabel.setText("Actualizando tasas desde el Banco Central...");
            exchangeRateService.fetchAndSaveLatestRates();
            loadActiveRates();
            updateStatusLabels();
            ModernDialog.showInfoDialog((Stage) rootView.getScene().getWindow(), 
                "Éxito", "Tasas actualizadas correctamente desde el Banco Central de Cuba");
        } catch (Exception e) {
            logger.error("Error al actualizar tasas", e);
            ModernDialog.showInfoDialog((Stage) rootView.getScene().getWindow(), 
                "Error", "No se pudo actualizar las tasas: " + e.getMessage());
        } finally {
            statusLabel.setText("");
        }
    }

    @FXML
    private void handleSearchHistory() {
        try {
            LocalDate from = fromDatePicker.getValue();
            LocalDate to = toDatePicker.getValue();
            String currency = currencyFilterCombo.getValue();

            if (from == null || to == null) {
                ModernDialog.showInfoDialog((Stage) rootView.getScene().getWindow(), 
                    "Advertencia", "Debe seleccionar un rango de fechas válido");
                return;
            }

            List<ExchangeRate> rates = exchangeRateService.getExchangeRatesByDateRange(from, to, currency);
            historicalRatesData.setAll(rates);
            
            if (rates.isEmpty()) {
                ModernDialog.showInfoDialog((Stage) rootView.getScene().getWindow(), 
                    "Información", "No se encontraron tasas en el rango seleccionado");
            }
        } catch (Exception e) {
            logger.error("Error al buscar histórico", e);
            ModernDialog.showInfoDialog((Stage) rootView.getScene().getWindow(), 
                "Error", "No se pudo cargar el histórico: " + e.getMessage());
        }
    }

    @FXML
    private void handleExportToCSV() {
        List<ExchangeRate> rates = historicalRatesData.isEmpty()
            ? List.copyOf(activeRatesData)
            : List.copyOf(historicalRatesData);

        if (rates.isEmpty()) {
            ModernDialog.showInfoDialog((Stage) rootView.getScene().getWindow(),
                "Exportar", "No hay tasas para exportar");
            return;
        }

        try {
            File file = new File(System.getProperty("user.home"),
                "Downloads/tasas_cambio_" + LocalDate.now() + ".csv");
            Files.writeString(file.toPath(), buildRatesCsv(rates), StandardCharsets.UTF_8);
            logger.info("Exchange rates exported to CSV: {}", file.getAbsolutePath());
            ModernDialog.showInfoDialog((Stage) rootView.getScene().getWindow(),
                "Exportar", "Tasas exportadas a:\n" + file.getAbsolutePath());
        } catch (Exception e) {
            logger.error("Error al exportar tasas a CSV", e);
            ModernDialog.showInfoDialog((Stage) rootView.getScene().getWindow(),
                "Error", "No se pudo exportar a CSV: " + e.getMessage());
        }
    }

    /**
     * Builds the CSV body for the given rates. Extracted from the handler so the
     * escaping and column order can be tested without a live JavaFX window.
     */
    static String buildRatesCsv(List<ExchangeRate> rates) {
        StringBuilder csv = new StringBuilder("Moneda,Nombre,Tasa,Fecha,Tipo\n");
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        for (ExchangeRate rate : rates) {
            csv.append(csvField(rate.getToCurrency() != null ? rate.getToCurrency().getCode() : ""))
               .append(',')
               .append(csvField(rate.getToCurrency() != null ? rate.getToCurrency().getName() : ""))
               .append(',')
               .append(rate.getRate() != null ? rate.getRate().toPlainString() : "")
               .append(',')
               .append(rate.getEffectiveDate() != null ? rate.getEffectiveDate().format(formatter) : "")
               .append(',')
               .append(rate.getRateType() != null ? rate.getRateType().name() : "")
               .append('\n');
        }
        return csv.toString();
    }

    private static String csvField(String value) {
        if (value == null) {
            return "";
        }
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }

    private void loadActiveRates() {
        try {
            List<ExchangeRate> rates = exchangeRateService.getLatestRatesForAllCurrencies();
            activeRatesData.setAll(rates);
            activeRatesTable.setItems(activeRatesData);
        } catch (Exception e) {
            logger.error("Error al cargar tasas activas", e);
        }
    }

    private void loadHistoricalRates() {
        try {
            LocalDate from = fromDatePicker.getValue();
            LocalDate to = toDatePicker.getValue();
            List<ExchangeRate> rates = exchangeRateService.getExchangeRatesByDateRange(from, to, null);
            historicalRatesData.setAll(rates);
            historicalRatesTable.setItems(historicalRatesData);
        } catch (Exception e) {
            logger.error("Error al cargar histórico", e);
        }
    }

    private void updateStatusLabels() {
        try {
            Optional<LocalDateTime> lastUpdate = exchangeRateService.getLastUpdateTime();
            lastUpdateLabel.setText("Última actualización: " + 
                lastUpdate.map(dt -> dt.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")))
                    .orElse("Nunca"));
        } catch (Exception e) {
            lastUpdateLabel.setText("Última actualización: Desconocida");
        }
    }
}
