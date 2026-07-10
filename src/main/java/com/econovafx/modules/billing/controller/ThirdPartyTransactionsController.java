package com.econovafx.controller;

import com.econovafx.modules.billing.model.ThirdParty;
import com.econovafx.modules.accounting.model.Transaction;
import com.econovafx.modules.core.service.ExportService;
import com.econovafx.modules.core.service.TransactionService;
import com.econovafx.modules.core.service.ThirdPartyService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Controller for displaying third party transactions history.
 */
public class ThirdPartyTransactionsController {

    private static final Logger logger = LoggerFactory.getLogger(ThirdPartyTransactionsController.class);
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @FXML
    private Label thirdPartyNameLabel;
    @FXML
    private Label thirdPartyIdLabel;
    @FXML
    private Label totalBalanceLabel;
    @FXML
    private Label totalDebitLabel;
    @FXML
    private Label totalCreditLabel;
    @FXML
    private Label transactionCountLabel;
    @FXML
    private TableView<Transaction> transactionsTable;
    @FXML
    private TableColumn<Transaction, String> dateColumn;
    @FXML
    private TableColumn<Transaction, String> voucherTypeColumn;
    @FXML
    private TableColumn<Transaction, String> voucherNumberColumn;
    @FXML
    private TableColumn<Transaction, String> descriptionColumn;
    @FXML
    private TableColumn<Transaction, BigDecimal> debitColumn;
    @FXML
    private TableColumn<Transaction, BigDecimal> creditColumn;
    @FXML
    private TableColumn<Transaction, BigDecimal> balanceColumn;
    @FXML
    private TableColumn<Transaction, String> statusColumn;
    @FXML
    private Button exportExcelButton;
    @FXML
    private Button closeButton;

    private ThirdPartyService thirdPartyService;
    private TransactionService transactionService;
    private ExportService exportService;
    private ThirdParty currentThirdParty;
    private Stage stage;

    /**
     * Sets the dependencies for this controller.
     */
    public void setServices(ThirdPartyService thirdPartyService, TransactionService transactionService, ExportService exportService) {
        this.thirdPartyService = thirdPartyService;
        this.transactionService = transactionService;
        this.exportService = exportService;
    }

    /**
     * Initializes the controller with third party data.
     */
    public void initData(ThirdParty thirdParty) {
        this.currentThirdParty = thirdParty;
        loadThirdPartyInfo();
        loadTransactions();
    }

    /**
     * Sets the stage for this controller.
     */
    public void setStage(Stage stage) {
        this.stage = stage;
    }

    /**
     * Loads third party information into the UI.
     */
    private void loadThirdPartyInfo() {
        if (currentThirdParty != null) {
            thirdPartyNameLabel.setText(currentThirdParty.getName());
            thirdPartyIdLabel.setText("ID: " + currentThirdParty.getIdentificationNumber());
            
            // Calculate and display balance
            BigDecimal balance = calculateThirdPartyBalance();
            totalBalanceLabel.setText(formatCurrency(balance));
            
            // Color code the balance
            if (balance.compareTo(BigDecimal.ZERO) > 0) {
                totalBalanceLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #dc2626;");
            } else if (balance.compareTo(BigDecimal.ZERO) < 0) {
                totalBalanceLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #059669;");
            } else {
                totalBalanceLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #6b7280;");
            }
        }
    }

    /**
     * Loads transactions for the current third party.
     */
    private void loadTransactions() {
        if (currentThirdParty == null || transactionService == null) {
            return;
        }

        try {
            List<Transaction> transactions = transactionService.getTransactionsByThirdPartyId(currentThirdParty.getId());
            ObservableList<Transaction> observableTransactions = FXCollections.observableArrayList(transactions);
            transactionsTable.setItems(observableTransactions);

            // Setup columns
            setupColumns();

            // Calculate statistics
            calculateStatistics(transactions);

            logger.info("Loaded {} transactions for third party: {}", transactions.size(), currentThirdParty.getName());
        } catch (Exception e) {
            logger.error("Error loading transactions for third party", e);
            showAlert("Error", "Failed to load transactions: " + e.getMessage());
        }
    }

    /**
     * Sets up table columns with cell factories.
     */
    private void setupColumns() {
        dateColumn.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleStringProperty(
                cellData.getValue().getDate().format(DATE_FORMATTER)
            )
        );

        voucherTypeColumn.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleStringProperty(
                cellData.getValue().getType()
            )
        );

        voucherNumberColumn.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleStringProperty(
                cellData.getValue().getNumber()
            )
        );

        descriptionColumn.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleStringProperty(
                cellData.getValue().getDescription()
            )
        );

        debitColumn.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleObjectProperty<>(
                cellData.getValue().getTotalDebit()
            )
        );
        debitColumn.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(BigDecimal item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(formatCurrency(item));
                }
            }
        });

        creditColumn.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleObjectProperty<>(
                cellData.getValue().getTotalCredit()
            )
        );
        creditColumn.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(BigDecimal item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(formatCurrency(item));
                }
            }
        });

        balanceColumn.setCellValueFactory(cellData -> {
            int index = transactionsTable.getItems().indexOf(cellData.getValue());
            BigDecimal runningBalance = calculateRunningBalance(index);
            return new javafx.beans.property.SimpleObjectProperty<>(runningBalance);
        });
        balanceColumn.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(BigDecimal item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(formatCurrency(item));
                    // Color code positive/negative balances
                    if (item.compareTo(BigDecimal.ZERO) > 0) {
                        setStyle("-fx-text-fill: #dc2626; -fx-font-weight: bold;");
                    } else if (item.compareTo(BigDecimal.ZERO) < 0) {
                        setStyle("-fx-text-fill: #059669; -fx-font-weight: bold;");
                    } else {
                        setStyle("-fx-text-fill: #6b7280;");
                    }
                }
            }
        });

        statusColumn.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleStringProperty(
                cellData.getValue().getIsPosted() ? "Posted" : "Unposted"
            )
        );
    }

    /**
     * Calculates statistics for the loaded transactions.
     */
    private void calculateStatistics(List<Transaction> transactions) {
        BigDecimal totalDebit = BigDecimal.ZERO;
        BigDecimal totalCredit = BigDecimal.ZERO;

        for (Transaction transaction : transactions) {
            if (transaction.getTotalDebit() != null) {
                totalDebit = totalDebit.add(transaction.getTotalDebit());
            }
            if (transaction.getTotalCredit() != null) {
                totalCredit = totalCredit.add(transaction.getTotalCredit());
            }
        }

        totalDebitLabel.setText(formatCurrency(totalDebit));
        totalCreditLabel.setText(formatCurrency(totalCredit));
        transactionCountLabel.setText(String.valueOf(transactions.size()));
    }

    /**
     * Calculates the running balance up to a specific index.
     */
    private BigDecimal calculateRunningBalance(int index) {
        BigDecimal balance = BigDecimal.ZERO;
        for (int i = 0; i <= index; i++) {
            Transaction transaction = transactionsTable.getItems().get(i);
            if (transaction.getTotalDebit() != null) {
                balance = balance.add(transaction.getTotalDebit());
            }
            if (transaction.getTotalCredit() != null) {
                balance = balance.subtract(transaction.getTotalCredit());
            }
        }
        return balance;
    }

    /**
     * Calculates the total balance for the third party.
     */
    private BigDecimal calculateThirdPartyBalance() {
        List<Transaction> transactions = transactionService.getTransactionsByThirdPartyId(currentThirdParty.getId());
        BigDecimal balance = BigDecimal.ZERO;
        
        for (Transaction transaction : transactions) {
            if (transaction.getTotalDebit() != null) {
                balance = balance.add(transaction.getTotalDebit());
            }
            if (transaction.getTotalCredit() != null) {
                balance = balance.subtract(transaction.getTotalCredit());
            }
        }
        
        return balance;
    }

    /**
     * Formats a BigDecimal as currency string.
     */
    private String formatCurrency(BigDecimal amount) {
        if (amount == null) {
            return "$0.00";
        }
        return String.format("$%,.2f", amount);
    }

    /**
     * Handles the export to Excel action.
     */
    @FXML
    private void handleExportExcel() {
        if (currentThirdParty == null || transactionsTable.getItems().isEmpty()) {
            showAlert("Warning", "No transactions to export.");
            return;
        }

        try {
            exportService.exportThirdPartyTransactionsToExcel(currentThirdParty, transactionsTable.getItems(), stage);
            logger.info("Successfully exported transactions to Excel for third party: {}", currentThirdParty.getName());
        } catch (Exception e) {
            logger.error("Error exporting transactions to Excel", e);
            showAlert("Error", "Failed to export transactions: " + e.getMessage());
        }
    }

    /**
     * Handles the close action.
     */
    @FXML
    private void handleClose() {
        if (stage != null) {
            stage.close();
        }
    }

    /**
     * Shows an alert dialog with the specified message.
     */
    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.initOwner(stage);
        alert.showAndWait();
    }
}
