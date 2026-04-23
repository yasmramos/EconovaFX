package com.econovafx.ui.controller;

import com.econovafx.domain.Transaction;
import com.econovafx.service.AccountService;
import com.econovafx.service.TransactionService;
import com.econovafx.ui.view.ViewFactory;
import io.avaje.inject.Component;
import jakarta.inject.Inject;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;

/**
 * Controller for Comprobantes de Operaciones view
 */
public class ComprobantesController implements Initializable {

    private static final Logger logger = LoggerFactory.getLogger(ComprobantesController.class);

    private final TransactionService transactionService;
    private final AccountService accountService;
    private ViewFactory viewFactory;

    // Filters
    @FXML
    private DatePicker filterStartDate;
    @FXML
    private DatePicker filterEndDate;
    @FXML
    private ComboBox<String> filterStatusCombo;
    @FXML
    private TextField searchField;

    // Summary labels
    @FXML
    private Label totalCountLabel;
    @FXML
    private Label postedCountLabel;
    @FXML
    private Label draftCountLabel;
    @FXML
    private Label cancelledCountLabel;

    // Table
    @FXML
    private TableView<ComprobanteRow> comprobantesTable;
    @FXML
    private TableColumn<ComprobanteRow, String> colNumber;
    @FXML
    private TableColumn<ComprobanteRow, String> colDate;
    @FXML
    private TableColumn<ComprobanteRow, String> colType;
    @FXML
    private TableColumn<ComprobanteRow, String> colDescription;
    @FXML
    private TableColumn<ComprobanteRow, String> colAmount;
    @FXML
    private TableColumn<ComprobanteRow, String> colAccounts;
    @FXML
    private TableColumn<ComprobanteRow, String> colStatus;
    @FXML
    private TableColumn<ComprobanteRow, Void> colActions;

    // Detail panel
    @FXML
    private VBox detailPanel;
    @FXML
    private Label detailNumber;
    @FXML
    private Label detailDate;
    @FXML
    private Label detailType;
    @FXML
    private Label detailStatus;
    @FXML
    private Label detailDescription;
    @FXML
    private Label detailAmount;
    @FXML
    private Button btnEdit;
    @FXML
    private Button btnPublish;
    @FXML
    private Button btnCancel;
    @FXML
    private Button btnCloseDetail;

    // Loading
    @FXML
    private StackPane loadingOverlay;
    @FXML
    private ProgressIndicator loadingIndicator;

    // Buttons
    @FXML
    private Button btnNewComprobante;
    @FXML
    private Button btnRefresh;

    private final ObservableList<ComprobanteRow> comprobantesData = FXCollections.observableArrayList();

    // Constructor for dependency injection
    public ComprobantesController(TransactionService transactionService, AccountService accountService, ViewFactory viewFactory) {
        this.transactionService = transactionService;
        this.accountService = accountService;
        this.viewFactory = viewFactory;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        logger.info("ComprobantesController initialized");
        setupTableColumns();
        setupFilters();
        setupSelectionListener();
        loadSampleData();
    }

    private void setupTableColumns() {
        colNumber.setCellValueFactory(cellData -> cellData.getValue().numberProperty());
        colDate.setCellValueFactory(cellData -> cellData.getValue().dateProperty());
        colType.setCellValueFactory(cellData -> cellData.getValue().typeProperty());
        colDescription.setCellValueFactory(cellData -> cellData.getValue().descriptionProperty());
        colAmount.setCellValueFactory(cellData -> cellData.getValue().amountProperty());
        colAccounts.setCellValueFactory(cellData -> cellData.getValue().accountsProperty());
        colStatus.setCellValueFactory(cellData -> cellData.getValue().statusProperty());

        // Status badge
        colStatus.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                    getStyleClass().clear();
                } else {
                    setText(item);
                    getStyleClass().setAll("status-badge");
                    if ("Publicado".equals(item)) {
                        getStyleClass().add("published");
                    } else if ("Borrador".equals(item)) {
                        getStyleClass().add("draft");
                    } else if ("Anulado".equals(item)) {
                        getStyleClass().add("reversed");
                    }
                }
            }
        });

        // Action buttons
        colActions.setCellFactory(column -> new TableCell<>() {
            private final Button btnView = new Button("👁️");
            private final Button btnEdit = new Button("✏️");

            {
                btnView.setStyle("-fx-background-color: #3b82f6; -fx-text-fill: white; -fx-background-radius: 6; -fx-padding: 4px 8px; -fx-cursor: hand;");
                btnEdit.setStyle("-fx-background-color: #10b981; -fx-text-fill: white; -fx-background-radius: 6; -fx-padding: 4px 8px; -fx-cursor: hand;");
                btnView.setOnAction(e -> {
                    ComprobanteRow row = getTableView().getItems().get(getIndex());
                    showDetail(row);
                });
                btnEdit.setOnAction(e -> {
                    ComprobanteRow row = getTableView().getItems().get(getIndex());
                    editComprobante();
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    HBox box = new HBox(4, btnView, btnEdit);
                    box.setStyle("-fx-alignment: center;");
                    setGraphic(box);
                }
            }
        });

        comprobantesTable.setItems(comprobantesData);
    }

    private void setupFilters() {
        filterStatusCombo.setItems(FXCollections.observableArrayList(
                "Todos", "Borrador", "Publicado", "Anulado"
        ));
        filterStatusCombo.setValue("Todos");

        filterStartDate.setValue(LocalDate.now().withDayOfMonth(1));
        filterEndDate.setValue(LocalDate.now());
    }

    private void setupSelectionListener() {
        comprobantesTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                showDetail(newSelection);
            } else {
                closeDetail();
            }
        });
    }

    private void loadSampleData() {
        showLoading(true);

        javafx.application.Platform.runLater(() -> {
            try {
                comprobantesData.clear();
                var transactions = transactionService.getAllTransactions();
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

                for (Transaction txn : transactions) {
                    String status = txn.getIsPosted() ? "Publicado" : "Borrador";
                    String accounts = txn.getEntries().stream()
                            .map(e -> e.getAccount().getCode())
                            .limit(2)
                            .reduce((a, b) -> a + " / " + b)
                            .orElse("");

                    comprobantesData.add(new ComprobanteRow(
                            txn.getNumber(),
                            txn.getDate().format(formatter),
                            txn.getType(),
                            txn.getDescription(),
                            txn.getTotalDebit().toPlainString(),
                            accounts,
                            status
                    ));
                }
                updateSummary();
            } catch (Exception e) {
                logger.error("Error loading transactions", e);
                showAlert(Alert.AlertType.ERROR, "Error", "Error al cargar los comprobantes: " + e.getMessage());
            } finally {
                showLoading(false);
            }
        });
    }

    private void updateSummary() {
        int total = comprobantesData.size();
        long posted = comprobantesData.stream().filter(r -> "Publicado".equals(r.getStatus())).count();
        long draft = comprobantesData.stream().filter(r -> "Borrador".equals(r.getStatus())).count();
        long cancelled = comprobantesData.stream().filter(r -> "Anulado".equals(r.getStatus())).count();

        totalCountLabel.setText(String.valueOf(total));
        postedCountLabel.setText(String.valueOf(posted));
        draftCountLabel.setText(String.valueOf(draft));
        cancelledCountLabel.setText(String.valueOf(cancelled));
    }

    private void showDetail(ComprobanteRow row) {
        detailNumber.setText(row.getNumber());
        detailDate.setText(row.getDate());
        detailType.setText(row.getType());
        detailStatus.setText(row.getStatus());
        detailDescription.setText(row.getDescription());
        detailAmount.setText("$ " + row.getAmount());

        // Color status
        detailStatus.getStyleClass().removeAll("value-positive", "value-warning", "value-negative", "value-info");
        if ("Publicado".equals(row.getStatus())) {
            detailStatus.getStyleClass().add("value-positive");
        } else if ("Borrador".equals(row.getStatus())) {
            detailStatus.getStyleClass().add("value-warning");
        } else {
            detailStatus.getStyleClass().add("value-negative");
        }

        detailPanel.setVisible(true);
        detailPanel.setManaged(true);
    }

    private void showLoading(boolean show) {
        loadingOverlay.setVisible(show);
        loadingOverlay.setManaged(show);
    }

    @FXML
    private void applyFilters() {
        logger.debug("Applying filters");
        loadSampleData();
    }

    @FXML
    private void clearFilters() {
        logger.debug("Clearing filters");
        filterStartDate.setValue(LocalDate.now().withDayOfMonth(1));
        filterEndDate.setValue(LocalDate.now());
        filterStatusCombo.setValue("Todos");
        searchField.clear();
        loadSampleData();
    }

    @FXML
    private void newComprobante() {
        logger.info("Opening new comprobante dialog");
        try {
            var result = viewFactory.showComprobanteFormDialog(null);
            if (result.isPresent()) {
                logger.info("Comprobante created successfully");
                refreshData();
                showAlert(Alert.AlertType.INFORMATION, "Éxito", "Comprobante creado exitosamente");
            }
        } catch (Exception e) {
            logger.error("Error opening comprobante dialog", e);
            showAlert(Alert.AlertType.ERROR, "Error", "Error al abrir el formulario: " + e.getMessage());
        }
    }

    @FXML
    private void refreshData() {
        logger.info("Refreshing data");
        loadSampleData();
    }

    @FXML
    private void editComprobante() {
        ComprobanteRow selected = comprobantesTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "Editar", "Seleccione un comprobante de la tabla");
            return;
        }

        if (!"Borrador".equals(selected.getStatus())) {
            showAlert(Alert.AlertType.WARNING, "Editar", "Solo se pueden editar comprobantes en borrador");
            return;
        }

        logger.info("Edit comprobante: {}", selected.getNumber());
        try {
            var transaction = transactionService.getTransactionByNumber(selected.getNumber());
            if (transaction.isPresent()) {
                var result = viewFactory.showComprobanteFormDialog(transaction.get());
                if (result.isPresent()) {
                    logger.info("Comprobante updated successfully");
                    refreshData();
                    showAlert(Alert.AlertType.INFORMATION, "Éxito", "Comprobante actualizado exitosamente");
                }
            }
        } catch (Exception e) {
            logger.error("Error editing comprobante", e);
            showAlert(Alert.AlertType.ERROR, "Error", "Error al editar el comprobante: " + e.getMessage());
        }
    }

    @FXML
    private void publishComprobante() {
        ComprobanteRow selected = comprobantesTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "Publicar", "Seleccione un comprobante de la tabla");
            return;
        }
        if (!"Borrador".equals(selected.getStatus())) {
            showAlert(Alert.AlertType.WARNING, "Publicar", "Solo se pueden publicar comprobantes en borrador");
            return;
        }

        // Confirm action
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Publicar Comprobante");
        confirm.setHeaderText(null);
        confirm.setContentText("¿Está seguro de publicar el comprobante " + selected.getNumber() + "?");
        var result = confirm.showAndWait();

        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                var transaction = transactionService.getTransactionByNumber(selected.getNumber());
                if (transaction.isPresent()) {
                    transactionService.postTransaction(transaction.get().getId());
                    logger.info("Comprobante publicado: {}", selected.getNumber());
                    refreshData();
                    showAlert(Alert.AlertType.INFORMATION, "Éxito", "Comprobante publicado exitosamente");
                }
            } catch (Exception e) {
                logger.error("Error publishing comprobante", e);
                showAlert(Alert.AlertType.ERROR, "Error", "Error al publicar: " + e.getMessage());
            }
        }
    }

    @FXML
    private void cancelComprobante() {
        ComprobanteRow selected = comprobantesTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "Anular", "Seleccione un comprobante de la tabla");
            return;
        }
        if (!"Publicado".equals(selected.getStatus())) {
            showAlert(Alert.AlertType.WARNING, "Anular", "Solo se pueden anular comprobantes publicados");
            return;
        }

        // Get reason from user
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Anular Comprobante");
        dialog.setHeaderText(null);
        dialog.setContentText("Motivo de la anulación:");

        dialog.showAndWait().ifPresent(reason -> {
            try {
                var transaction = transactionService.getTransactionByNumber(selected.getNumber());
                if (transaction.isPresent()) {
                    transactionService.reverseTransaction(transaction.get().getId(), reason);
                    logger.info("Comprobante anulado: {}", selected.getNumber());
                    refreshData();
                    showAlert(Alert.AlertType.INFORMATION, "Éxito", "Comprobante anulado exitosamente");
                }
            } catch (Exception e) {
                logger.error("Error cancelling comprobante", e);
                showAlert(Alert.AlertType.ERROR, "Error", "Error al anular: " + e.getMessage());
            }
        });
    }

    @FXML
    private void closeDetail() {
        detailPanel.setVisible(false);
        detailPanel.setManaged(false);
        comprobantesTable.getSelectionModel().clearSelection();
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Simple data model for table rows
     */
    public static class ComprobanteRow {

        private final javafx.beans.property.SimpleStringProperty number;
        private final javafx.beans.property.SimpleStringProperty date;
        private final javafx.beans.property.SimpleStringProperty type;
        private final javafx.beans.property.SimpleStringProperty description;
        private final javafx.beans.property.SimpleStringProperty amount;
        private final javafx.beans.property.SimpleStringProperty accounts;
        private final javafx.beans.property.SimpleStringProperty status;

        public ComprobanteRow(String number, String date, String type, String description,
                String amount, String accounts, String status) {
            this.number = new javafx.beans.property.SimpleStringProperty(number);
            this.date = new javafx.beans.property.SimpleStringProperty(date);
            this.type = new javafx.beans.property.SimpleStringProperty(type);
            this.description = new javafx.beans.property.SimpleStringProperty(description);
            this.amount = new javafx.beans.property.SimpleStringProperty(amount);
            this.accounts = new javafx.beans.property.SimpleStringProperty(accounts);
            this.status = new javafx.beans.property.SimpleStringProperty(status);
        }

        public javafx.beans.property.StringProperty numberProperty() {
            return number;
        }

        public javafx.beans.property.StringProperty dateProperty() {
            return date;
        }

        public javafx.beans.property.StringProperty typeProperty() {
            return type;
        }

        public javafx.beans.property.StringProperty descriptionProperty() {
            return description;
        }

        public javafx.beans.property.StringProperty amountProperty() {
            return amount;
        }

        public javafx.beans.property.StringProperty accountsProperty() {
            return accounts;
        }

        public javafx.beans.property.StringProperty statusProperty() {
            return status;
        }

        public String getNumber() {
            return number.get();
        }

        public String getDate() {
            return date.get();
        }

        public String getType() {
            return type.get();
        }

        public String getDescription() {
            return description.get();
        }

        public String getAmount() {
            return amount.get();
        }

        public String getAccounts() {
            return accounts.get();
        }

        public String getStatus() {
            return status.get();
        }

        public void setStatus(String status) {
            this.status.set(status);
        }
    }
}
