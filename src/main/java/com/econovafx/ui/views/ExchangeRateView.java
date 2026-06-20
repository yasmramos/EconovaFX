package com.econovafx.ui.views;

import com.econovafx.domain.ExchangeRate;
import com.econovafx.domain.Currency;
import com.econovafx.service.ExchangeRateService;
import com.econovafx.service.BCCExchangeRateClient;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.util.Callback;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Vista principal para la gestión de Tasas de Cambio.
 * Integra consulta de tasas activas, histórico y actualización desde el Banco Central.
 */
public class ExchangeRateView extends VBox {

    private final ExchangeRateService exchangeRateService;
    private final BCCExchangeRateClient bccClient;

    // Componentes UI - Tasas Activas
    private TableView<ExchangeRate> activeRatesTable;
    private Label lastUpdateLabel;
    private Button refreshButton;
    private Label statusLabel;

    // Componentes UI - Histórico
    private DatePicker startDatePicker;
    private DatePicker endDatePicker;
    private ComboBox<String> currencyFilterCombo;
    private Button searchButton;
    private TableView<ExchangeRate> historyTable;
    private Button exportButton;

    public ExchangeRateView(ExchangeRateService exchangeRateService, BCCExchangeRateClient bccClient) {
        this.exchangeRateService = exchangeRateService;
        this.bccClient = bccClient;
        this.getStyleClass().add("main-view");
        initUI();
        loadActiveRates();
    }

    private void initUI() {
        setSpacing(15);
        setPadding(new Insets(20));

        // Título
        Label title = new Label("Gestión de Tasas de Cambio");
        title.setFont(new Font("Arial", 24));
        title.setStyle("-fx-font-weight: bold;");

        // Pestañas
        TabPane tabPane = new TabPane();
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

        Tab activeTab = createActiveRatesTab();
        Tab historyTab = createHistoryTab();

        tabPane.getTabs().addAll(activeTab, historyTab);
        VBox.setVgrow(tabPane, Priority.ALWAYS);

        getChildren().addAll(title, tabPane);
    }

    private Tab createActiveRatesTab() {
        Tab tab = new Tab("Tasas Activas");
        VBox content = new VBox(10);
        content.setPadding(new Insets(10));

        // Panel superior: Estado y Botones
        HBox topPanel = new HBox(15);
        topPanel.setAlignment(Pos.CENTER_LEFT);

        statusLabel = new Label("Estado: Desconocido");
        statusLabel.setStyle("-fx-text-fill: gray;");
        
        lastUpdateLabel = new Label("Última act.: --");
        lastUpdateLabel.setStyle("-fx-font-style: italic;");

        refreshButton = new Button("🔄 Actualizar desde BC");
        refreshButton.setOnAction(e -> handleRefreshFromBC());
        refreshButton.setStyle("-fx-background-color: #0078D7; -fx-text-fill: white; -fx-padding: 5 15;");

        topPanel.getChildren().addAll(statusLabel, new Separator(), lastUpdateLabel, new Region(), refreshButton);

        // Tabla
        activeRatesTable = new TableView<>();
        activeRatesTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        
        TableColumn<ExchangeRate, String> fromCol = new TableColumn<>("De");
        fromCol.setCellValueFactory(data -> {
            Currency from = data.getValue().getFromCurrency();
            return new javafx.beans.property.SimpleStringProperty(from != null ? from.getCode() : "");
        });
        
        TableColumn<ExchangeRate, String> toCol = new TableColumn<>("A");
        toCol.setCellValueFactory(data -> {
            Currency to = data.getValue().getToCurrency();
            return new javafx.beans.property.SimpleStringProperty(to != null ? to.getCode() : "");
        });
        
        TableColumn<ExchangeRate, String> rateCol = new TableColumn<>("Tasa");
        rateCol.setCellValueFactory(data -> {
            java.math.BigDecimal rate = data.getValue().getRate();
            return new javafx.beans.property.SimpleStringProperty(rate != null ? rate.toPlainString() : "");
        });
        
        TableColumn<ExchangeRate, String> dateCol = new TableColumn<>("Fecha");
        dateCol.setCellValueFactory(data -> {
            LocalDateTime date = data.getValue().getEffectiveDate();
            return new javafx.beans.property.SimpleStringProperty(date != null ? date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) : "");
        });

        activeRatesTable.getColumns().addAll(fromCol, toCol, rateCol, dateCol);

        content.getChildren().addAll(topPanel, new Label("Tasas del Día:"), activeRatesTable);
        tab.setContent(content);
        return tab;
    }

    private Tab createHistoryTab() {
        Tab tab = new Tab("Histórico");
        VBox content = new VBox(10);
        content.setPadding(new Insets(10));

        // Filtros
        HBox filterPanel = new HBox(10);
        filterPanel.setAlignment(Pos.CENTER_LEFT);

        startDatePicker = new DatePicker(LocalDate.now().minusDays(30));
        startDatePicker.setPromptText("Desde");
        
        endDatePicker = new DatePicker(LocalDate.now());
        endDatePicker.setPromptText("Hasta");

        currencyFilterCombo = new ComboBox<>();
        currencyFilterCombo.getItems().addAll("TODOS", "USD", "EUR", "GBP", "CHF", "CAD", "JPY");
        currencyFilterCombo.setValue("TODOS");

        searchButton = new Button("🔍 Buscar");
        searchButton.setOnAction(e -> handleSearchHistory());

        exportButton = new Button("📥 Exportar CSV");
        exportButton.setOnAction(e -> handleExportCSV());
        exportButton.setDisable(true);

        filterPanel.getChildren().addAll(
            new Label("Desde:"), startDatePicker,
            new Label("Hasta:"), endDatePicker,
            new Label("Moneda:"), currencyFilterCombo,
            searchButton, new Region(), exportButton
        );

        // Tabla Histórico
        historyTable = new TableView<>();
        historyTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<ExchangeRate, String> histDateCol = new TableColumn<>("Fecha");
        histDateCol.setCellValueFactory(data -> {
            LocalDateTime date = data.getValue().getEffectiveDate();
            return new javafx.beans.property.SimpleStringProperty(date != null ? date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) : "");
        });
        
        TableColumn<ExchangeRate, String> histFromCol = new TableColumn<>("De");
        histFromCol.setCellValueFactory(data -> {
            Currency from = data.getValue().getFromCurrency();
            return new javafx.beans.property.SimpleStringProperty(from != null ? from.getCode() : "");
        });
        
        TableColumn<ExchangeRate, String> histToCol = new TableColumn<>("A");
        histToCol.setCellValueFactory(data -> {
            Currency to = data.getValue().getToCurrency();
            return new javafx.beans.property.SimpleStringProperty(to != null ? to.getCode() : "");
        });
        
        TableColumn<ExchangeRate, String> histRateCol = new TableColumn<>("Tasa");
        histRateCol.setCellValueFactory(data -> {
            java.math.BigDecimal rate = data.getValue().getRate();
            return new javafx.beans.property.SimpleStringProperty(rate != null ? rate.toPlainString() : "");
        });

        historyTable.getColumns().addAll(histDateCol, histFromCol, histToCol, histRateCol);

        content.getChildren().addAll(filterPanel, historyTable);
        tab.setContent(content);
        return tab;
    }

    private void loadActiveRates() {
        try {
            // Obtenemos todas las tasas activas
            List<ExchangeRate> rates = exchangeRateService.getAllActiveRates();
            if (rates != null && !rates.isEmpty()) {
                activeRatesTable.setItems(FXCollections.observableArrayList(rates));
                statusLabel.setText("Estado: Conectado (Local)");
                statusLabel.setStyle("-fx-text-fill: green;");
                
                LocalDateTime lastDate = rates.stream()
                    .map(ExchangeRate::getEffectiveDate)
                    .max(java.util.Comparator.naturalOrder())
                    .orElse(LocalDateTime.now());
                    
                lastUpdateLabel.setText("Última act.: " + lastDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
            } else {
                statusLabel.setText("Estado: Sin datos locales");
                statusLabel.setStyle("-fx-text-fill: orange;");
            }
        } catch (Exception e) {
            showError("Error cargando tasas activas: " + e.getMessage());
            statusLabel.setText("Estado: Error");
            statusLabel.setStyle("-fx-text-fill: red;");
        }
    }

    private void handleRefreshFromBC() {
        refreshButton.setDisable(true);
        statusLabel.setText("Estado: Actualizando...");
        
        new Thread(() -> {
            try {
                exchangeRateService.fetchAndSaveRatesFromBCC();
                javafx.application.Platform.runLater(() -> {
                    loadActiveRates();
                    showAlert("Éxito", "Tasas actualizadas correctamente desde el Banco Central.");
                    refreshButton.setDisable(false);
                });
            } catch (Exception e) {
                javafx.application.Platform.runLater(() -> {
                    showError("Error al actualizar: " + e.getMessage());
                    statusLabel.setText("Estado: Fallo actualización");
                    statusLabel.setStyle("-fx-text-fill: red;");
                    refreshButton.setDisable(false);
                });
            }
        }).start();
    }

    private void handleSearchHistory() {
        LocalDate start = startDatePicker.getValue();
        LocalDate end = endDatePicker.getValue();
        String currency = currencyFilterCombo.getValue();

        if (start == null || end == null) {
            showError("Por favor seleccione un rango de fechas válido.");
            return;
        }

        try {
            List<ExchangeRate> history = exchangeRateService.getExchangeRatesByDateRange(start, end, 
                "TODOS".equals(currency) ? null : currency);

            historyTable.setItems(FXCollections.observableArrayList(history));
            exportButton.setDisable(history.isEmpty());
            
            if (history.isEmpty()) {
                showAlert("Información", "No se encontraron registros para el periodo seleccionado.");
            }
        } catch (Exception e) {
            showError("Error buscando histórico: " + e.getMessage());
        }
    }

    private void handleExportCSV() {
        ObservableList<ExchangeRate> items = historyTable.getItems();
        if (items.isEmpty()) return;

        StringBuilder csv = new StringBuilder("Fecha,De,A,Tasa\n");
        for (ExchangeRate rate : items) {
            LocalDateTime date = rate.getEffectiveDate();
            Currency from = rate.getFromCurrency();
            Currency to = rate.getToCurrency();
            java.math.BigDecimal r = rate.getRate();
            
            csv.append(date != null ? date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) : "")
               .append(",")
               .append(from != null ? from.getCode() : "")
               .append(",")
               .append(to != null ? to.getCode() : "")
               .append(",")
               .append(r != null ? r.toPlainString() : "")
               .append("\n");
        }

        showAlert("Exportar", "Datos listos para CSV:\n" + csv.toString());
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
