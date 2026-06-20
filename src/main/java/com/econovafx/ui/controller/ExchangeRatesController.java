package com.econovafx.ui.controller;

import com.econovafx.domain.ExchangeRate;
import com.econovafx.service.ExchangeRateService;
import com.econovafx.ui.util.ModernDialog;
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

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

/**
 * Controller for Exchange Rates management UI
 */
public class ExchangeRatesController {

    private static final Logger logger = LoggerFactory.getLogger(ExchangeRatesController.class);

    @Inject
    private ExchangeRateService exchangeRateService;

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
        // TODO: Implement CSV export
        ModernDialog.showInfoDialog((Stage) rootView.getScene().getWindow(), 
            "Exportar", "Funcionalidad de exportación a CSV en desarrollo");
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
