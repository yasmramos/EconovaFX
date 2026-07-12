package com.econovafx.modules.core.ui.controller;

import com.econovafx.modules.accounting.model.Account;
import com.econovafx.modules.accounting.model.AccountType;
import com.econovafx.modules.accounting.model.Transaction;
import com.econovafx.modules.accounting.service.AccountService;
import com.econovafx.modules.accounting.service.TransactionService;
import com.econovafx.modules.core.ui.view.ViewFactory;
import io.avaje.inject.Component;
import jakarta.inject.Inject;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.StackPane;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.net.URL;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Dashboard controller with TailwindFX styling
 */
public class DashboardController implements Initializable {

    private static final Logger logger = LoggerFactory.getLogger(DashboardController.class);

    private final AccountService accountService;
    private final TransactionService transactionService;
    private final ViewFactory viewFactory;

    // Summary Labels
    @FXML
    private Label totalAssetsLabel;
    @FXML
    private Label totalLiabilitiesLabel;
    @FXML
    private Label totalEquityLabel;
    @FXML
    private Label balanceLabel;

    // Trend Labels
    @FXML
    private Label assetsTrendLabel;
    @FXML
    private Label liabilitiesTrendLabel;
    @FXML
    private Label equityTrendLabel;
    @FXML
    private Label balanceTrendLabel;

    // Charts
    @FXML
    private PieChart financialPieChart;
    @FXML
    private BarChart<String, Number> monthlyBarChart;
    @FXML
    private LineChart<String, Number> cashFlowLineChart;
    
    // KPI Cards - Additional Metrics
    @FXML
    private Label revenueLabel;
    @FXML
    private Label expensesLabel;
    @FXML
    private Label profitLabel;
    @FXML
    private Label averageTransactionLabel;
    
    // Trend Labels for KPIs
    @FXML
    private Label revenueTrendLabel;
    @FXML
    private Label expensesTrendLabel;
    @FXML
    private Label profitTrendLabel;

    // Transaction Table
    @FXML
    private TableView<Transaction> recentTransactionsTable;
    @FXML
    private TableColumn<Transaction, LocalDate> colDate;
    @FXML
    private TableColumn<Transaction, String> colNumber;
    @FXML
    private TableColumn<Transaction, String> colType;
    @FXML
    private TableColumn<Transaction, String> colDescription;
    @FXML
    private TableColumn<Transaction, BigDecimal> colDebit;
    @FXML
    private TableColumn<Transaction, BigDecimal> colCredit;
    @FXML
    private TableColumn<Transaction, Boolean> colStatus;

    // Filters
    @FXML
    private DatePicker filterStartDate;
    @FXML
    private DatePicker filterEndDate;

    // Quick Transaction
    @FXML
    private ComboBox<String> quickTransactionType;
    @FXML
    private TextField quickDescription;
    @FXML
    private TextField quickAmount;
    @FXML
    private ComboBox<String> quickAccountSelect;

    // Statistics
    @FXML
    private Label totalTransactionsLabel;
    @FXML
    private Label postedTransactionsLabel;
    @FXML
    private Label unpostedTransactionsLabel;
    @FXML
    private Label totalAccountsLabel;

    // Refresh Button
    @FXML
    private Button refreshButton;

    // Loading Overlay
    @FXML
    private StackPane loadingOverlay;
    @FXML
    private ProgressIndicator loadingIndicator;

    // Observable Lists
    private ObservableList<Transaction> transactionObservableList;

    public DashboardController(AccountService accountService,
                               TransactionService transactionService,
                               ViewFactory viewFactory) {
        this.accountService = accountService;
        this.transactionService = transactionService;
        this.viewFactory = viewFactory;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        logger.info("DashboardController initialized");
        
        initializeTableColumns();
        
        // Wrap initialization in try-catch for visual tests where DB might not be ready
        try {
            initializeComboBoxes();
            initializeDatePickerDefaults();
            loadDashboardData();
        } catch (Exception e) {
            logger.warn("Could not initialize dashboard data (DB might not be ready): {}", e.getMessage());
        }
        
        // Auto-refresh every 30 seconds
        startAutoRefresh();
    }

    private void initializeTableColumns() {
        colDate.setCellValueFactory(new PropertyValueFactory<>("date"));
        colNumber.setCellValueFactory(new PropertyValueFactory<>("number"));
        colType.setCellValueFactory(new PropertyValueFactory<>("type"));
        colDescription.setCellValueFactory(new PropertyValueFactory<>("description"));
        colDebit.setCellValueFactory(new PropertyValueFactory<>("totalDebit"));
        colCredit.setCellValueFactory(new PropertyValueFactory<>("totalCredit"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("isPosted"));

        // Custom cell factory for status column
        colStatus.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(Boolean item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item ? "Publicado" : "Borrador");
                    setStyle(item ? "-fx-text-fill: #10b981; -fx-font-weight: bold;" : 
                                   "-fx-text-fill: #f59e0b; -fx-font-weight: bold;");
                }
            }
        });

        // Format BigDecimal columns
        colDebit.setCellFactory(column -> new TableCell<>() {
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

        colCredit.setCellFactory(column -> new TableCell<>() {
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
    }

    private void initializeComboBoxes() {
        quickTransactionType.setItems(FXCollections.observableArrayList(
                "INGRESO", "GASTO", "TRANSFERENCIA", "ASIENTO"));
        
        // Load accounts for quick selection
        updateAccountComboBox();
    }

    private void updateAccountComboBox() {
        List<Account> accounts = accountService.getAllAccounts();
        ObservableList<String> accountNames = FXCollections.observableArrayList();
        accountNames.add("Seleccione una cuenta...");
        for (Account account : accounts) {
            accountNames.add(account.getCode() + " - " + account.getName());
        }
        quickAccountSelect.setItems(accountNames);
        quickAccountSelect.setValue("Seleccione una cuenta...");
    }

    private void initializeDatePickerDefaults() {
        // Set default date range to last 30 days
        filterEndDate.setValue(LocalDate.now());
        filterStartDate.setValue(LocalDate.now().minusDays(30));
    }

    private void loadDashboardData() {
        showLoading(true);

        // Use background thread for data loading
        new Thread(() -> {
            try {
                loadFinancialSummary();
                loadKPIs();
                loadCharts();
                loadTransactions();
                loadStatistics();
                // Update account combo box on FX thread
                Platform.runLater(() -> updateAccountComboBox());
            } catch (Exception e) {
                logger.error("Error loading dashboard data", e);
                Platform.runLater(() -> showError("Error cargando datos: " + e.getMessage()));
            } finally {
                Platform.runLater(() -> showLoading(false));
            }
        }).start();
    }

    private void loadFinancialSummary() {
        List<Account> allAccounts = accountService.getAllAccounts();

        BigDecimal totalAssets = BigDecimal.ZERO;
        BigDecimal totalLiabilities = BigDecimal.ZERO;
        BigDecimal totalEquity = BigDecimal.ZERO;

        for (Account account : allAccounts) {
            switch (account.getType()) {
                case ASSET -> totalAssets = totalAssets.add(account.getBalance());
                case LIABILITY -> totalLiabilities = totalLiabilities.add(account.getBalance());
                case EQUITY -> totalEquity = totalEquity.add(account.getBalance());
                case REVENUE, EXPENSE -> {
                    if (account.getType() == AccountType.REVENUE) {
                        totalEquity = totalEquity.add(account.getBalance());
                    } else {
                        totalEquity = totalEquity.subtract(account.getBalance());
                    }
                }
            }
        }

        final BigDecimal finalAssets = totalAssets;
        final BigDecimal finalLiabilities = totalLiabilities;
        final BigDecimal finalEquity = totalEquity;
        
        Platform.runLater(() -> {
            totalAssetsLabel.setText(formatCurrency(finalAssets));
            totalLiabilitiesLabel.setText(formatCurrency(finalLiabilities));
            totalEquityLabel.setText(formatCurrency(finalEquity));
            
            BigDecimal balance = finalAssets.subtract(finalLiabilities);
            balanceLabel.setText(formatCurrency(balance));
            
            updateTrendLabels();
        });
    }

    private void updateTrendLabels() {
        assetsTrendLabel.setText("▼ 0%");
        assetsTrendLabel.setStyle("-fx-text-fill: #ef4444; -fx-font-size: 12px;");
        
        liabilitiesTrendLabel.setText("▼ 0%");
        liabilitiesTrendLabel.setStyle("-fx-text-fill: #10b981; -fx-font-size: 12px;");
        
        equityTrendLabel.setText("▬ 0%");
        equityTrendLabel.setStyle("-fx-text-fill: #6b7280; -fx-font-size: 12px;");
        
        balanceTrendLabel.setText("Estable");
        balanceTrendLabel.setStyle("-fx-text-fill: #10b981; -fx-font-size: 12px;");
    }
    
    private void loadKPIs() {
        List<Transaction> allTransactions = transactionService.getAllTransactions();
        
        // Calculate revenue and expenses from posted transactions
        BigDecimal totalRevenue = BigDecimal.ZERO;
        BigDecimal totalExpenses = BigDecimal.ZERO;
        
        for (Transaction t : allTransactions) {
            if (t.getIsPosted() && t.getDate() != null) {
                // Consider only transactions from current month
                YearMonth currentMonth = YearMonth.now();
                YearMonth transactionMonth = YearMonth.from(t.getDate());
                
                if (currentMonth.equals(transactionMonth)) {
                    if (t.getType().equalsIgnoreCase("INGRESO")) {
                        totalRevenue = totalRevenue.add(t.getTotalDebit());
                    } else if (t.getType().equalsIgnoreCase("GASTO")) {
                        totalExpenses = totalExpenses.add(t.getTotalDebit());
                    }
                }
            }
        }
        
        BigDecimal profit = totalRevenue.subtract(totalExpenses);
        
        // Calculate average transaction amount
        BigDecimal avgTransaction = allTransactions.stream()
                .filter(Transaction::getIsPosted)
                .map(Transaction::getTotalDebit)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(BigDecimal.valueOf(allTransactions.stream().filter(Transaction::getIsPosted).count()), 
                        java.math.RoundingMode.HALF_UP);
        
        final BigDecimal finalRevenue = totalRevenue;
        final BigDecimal finalExpenses = totalExpenses;
        final BigDecimal finalProfit = profit;
        final BigDecimal finalAvgTransaction = avgTransaction;
        
        Platform.runLater(() -> {
            revenueLabel.setText(formatCurrency(finalRevenue));
            expensesLabel.setText(formatCurrency(finalExpenses));
            profitLabel.setText(formatCurrency(finalProfit));
            averageTransactionLabel.setText(formatCurrency(finalAvgTransaction));
            
            // Set trend indicators (placeholder - would need historical data)
            if (finalProfit.compareTo(BigDecimal.ZERO) > 0) {
                profitTrendLabel.setText("▲ Positivo");
                profitTrendLabel.setStyle("-fx-text-fill: #10b981; -fx-font-size: 12px; -fx-font-weight: bold;");
            } else if (finalProfit.compareTo(BigDecimal.ZERO) < 0) {
                profitTrendLabel.setText("▼ Negativo");
                profitTrendLabel.setStyle("-fx-text-fill: #ef4444; -fx-font-size: 12px; -fx-font-weight: bold;");
            } else {
                profitTrendLabel.setText("▬ Equilibrio");
                profitTrendLabel.setStyle("-fx-text-fill: #6b7280; -fx-font-size: 12px; -fx-font-weight: bold;");
            }
            
            revenueTrendLabel.setText("Este mes");
            revenueTrendLabel.setStyle("-fx-text-fill: #6b7280; -fx-font-size: 11px;");
            
            expensesTrendLabel.setText("Este mes");
            expensesTrendLabel.setStyle("-fx-text-fill: #6b7280; -fx-font-size: 11px;");
        });
    }

    private void loadCharts() {
        Platform.runLater(() -> {
            loadPieChart();
            loadBarChart();
            loadLineChart();
        });
    }

    private void loadPieChart() {
        List<Account> allAccounts = accountService.getAllAccounts();

        BigDecimal assets = BigDecimal.ZERO;
        BigDecimal liabilities = BigDecimal.ZERO;
        BigDecimal equity = BigDecimal.ZERO;

        for (Account account : allAccounts) {
            switch (account.getType()) {
                case ASSET -> assets = assets.add(account.getBalance());
                case LIABILITY -> liabilities = liabilities.add(account.getBalance());
                case EQUITY, REVENUE, EXPENSE -> equity = equity.add(account.getBalance());
            }
        }

        final BigDecimal finalAssetsPie = assets;
        final BigDecimal finalLiabilitiesPie = liabilities;
        final BigDecimal finalEquityPie = equity;
        
        ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList(
                new PieChart.Data("Activos (" + formatCurrency(finalAssetsPie) + ")", finalAssetsPie.doubleValue()),
                new PieChart.Data("Pasivos (" + formatCurrency(finalLiabilitiesPie) + ")", finalLiabilitiesPie.doubleValue()),
                new PieChart.Data("Patrimonio (" + formatCurrency(finalEquityPie) + ")", finalEquityPie.doubleValue())
        );

        financialPieChart.setData(pieChartData);
        financialPieChart.setLegendVisible(true);
        financialPieChart.setLegendSide(javafx.geometry.Side.BOTTOM);
        
        // Add percentage labels
        pieChartData.forEach(data -> {
            double total = finalAssetsPie.doubleValue() + finalLiabilitiesPie.doubleValue() + finalEquityPie.doubleValue();
            if (total > 0) {
                double percentage = (data.getPieValue() / total) * 100;
                data.setName(String.format("%s - %.1f%%", data.getName(), percentage));
            }
        });
    }

    private void loadBarChart() {
        List<Transaction> allTransactions = transactionService.getAllTransactions();
        
        Map<String, BigDecimal> incomeByMonth = new LinkedHashMap<>();
        Map<String, BigDecimal> expenseByMonth = new LinkedHashMap<>();
        
        for (int i = 5; i >= 0; i--) {
            YearMonth ym = YearMonth.now().minusMonths(i);
            String monthLabel = ym.format(DateTimeFormatter.ofPattern("MMM yy", Locale.forLanguageTag("es")));
            incomeByMonth.put(monthLabel, BigDecimal.ZERO);
            expenseByMonth.put(monthLabel, BigDecimal.ZERO);
        }
        
        for (Transaction t : allTransactions) {
            if (t.getIsPosted() && t.getDate() != null) {
                String monthKey = YearMonth.from(t.getDate()).format(
                        DateTimeFormatter.ofPattern("MMM yy", Locale.forLanguageTag("es")));
                
                if (incomeByMonth.containsKey(monthKey)) {
                    if (t.getType().equalsIgnoreCase("INGRESO")) {
                        incomeByMonth.put(monthKey, incomeByMonth.get(monthKey).add(t.getTotalDebit()));
                    } else if (t.getType().equalsIgnoreCase("GASTO")) {
                        expenseByMonth.put(monthKey, expenseByMonth.get(monthKey).add(t.getTotalDebit()));
                    }
                }
            }
        }

        XYChart.Series<String, Number> incomeSeries = new XYChart.Series<>();
        incomeSeries.setName("Ingresos");
        XYChart.Series<String, Number> expenseSeries = new XYChart.Series<>();
        expenseSeries.setName("Gastos");

        for (String month : incomeByMonth.keySet()) {
            incomeSeries.getData().add(new XYChart.Data<>(month, incomeByMonth.get(month).doubleValue()));
            expenseSeries.getData().add(new XYChart.Data<>(month, expenseByMonth.get(month).doubleValue()));
        }

        monthlyBarChart.getData().clear();
        monthlyBarChart.getData().addAll(List.of(incomeSeries, expenseSeries));
        monthlyBarChart.setLegendVisible(true);
        monthlyBarChart.setLegendSide(javafx.geometry.Side.BOTTOM);
    }
    
    private void loadLineChart() {
        List<Transaction> allTransactions = transactionService.getAllTransactions();
        
        // Calculate daily cash flow for the last 30 days
        Map<String, BigDecimal> dailyCashFlow = new LinkedHashMap<>();
        
        for (int i = 29; i >= 0; i--) {
            LocalDate date = LocalDate.now().minusDays(i);
            String dateLabel = date.format(DateTimeFormatter.ofPattern("dd/MM", Locale.forLanguageTag("es")));
            dailyCashFlow.put(dateLabel, BigDecimal.ZERO);
        }
        
        for (Transaction t : allTransactions) {
            if (t.getIsPosted() && t.getDate() != null) {
                String dateKey = t.getDate().format(DateTimeFormatter.ofPattern("dd/MM", Locale.forLanguageTag("es")));
                
                if (dailyCashFlow.containsKey(dateKey)) {
                    if (t.getType().equalsIgnoreCase("INGRESO")) {
                        dailyCashFlow.put(dateKey, dailyCashFlow.get(dateKey).add(t.getTotalDebit()));
                    } else if (t.getType().equalsIgnoreCase("GASTO")) {
                        dailyCashFlow.put(dateKey, dailyCashFlow.get(dateKey).subtract(t.getTotalDebit()));
                    }
                }
            }
        }
        
        XYChart.Series<String, Number> cashFlowSeries = new XYChart.Series<>();
        cashFlowSeries.setName("Flujo de Caja");
        
        for (String date : dailyCashFlow.keySet()) {
            cashFlowSeries.getData().add(new XYChart.Data<>(date, dailyCashFlow.get(date).doubleValue()));
        }
        
        if (cashFlowLineChart != null) {
            cashFlowLineChart.getData().clear();
            cashFlowLineChart.getData().addAll(List.of(cashFlowSeries));
            cashFlowLineChart.setLegendVisible(true);
            cashFlowLineChart.setLegendSide(javafx.geometry.Side.BOTTOM);
            
            // Style the line chart
            NumberAxis yAxis = (NumberAxis) cashFlowLineChart.getYAxis();
            yAxis.setLabel("Monto");
            CategoryAxis xAxis = (CategoryAxis) cashFlowLineChart.getXAxis();
            xAxis.setLabel("Día");
        }
    }

    private void loadTransactions() {
        List<Transaction> transactions = transactionService.getAllTransactions();

        LocalDate startDate = filterStartDate.getValue();
        LocalDate endDate = filterEndDate.getValue();

        if (startDate != null && endDate != null) {
            transactions = transactions.stream()
                    .filter(t -> t.getDate() != null &&
                           !t.getDate().isBefore(startDate) &&
                           !t.getDate().isAfter(endDate))
                    .limit(20)
                    .toList();
        } else {
            transactions = transactions.stream().limit(20).toList();
        }

        final List<Transaction> finalTransactions = transactions;
        Platform.runLater(() -> {
            transactionObservableList = FXCollections.observableArrayList(finalTransactions);
            recentTransactionsTable.setItems(transactionObservableList);
        });
    }

    private void loadStatistics() {
        List<Transaction> allTransactions = transactionService.getAllTransactions();
        List<Transaction> postedTransactions = transactionService.getPostedTransactions();
        List<Account> allAccounts = accountService.getAllAccounts();

        long unpostedCount = allTransactions.stream()
                .filter(t -> !t.getIsPosted())
                .count();

        Platform.runLater(() -> {
            totalTransactionsLabel.setText(String.valueOf(allTransactions.size()));
            postedTransactionsLabel.setText(String.valueOf(postedTransactions.size()));
            unpostedTransactionsLabel.setText(String.valueOf(unpostedCount));
            totalAccountsLabel.setText(String.valueOf(allAccounts.size()));
        });
    }

    @FXML
    private void refreshDashboard() {
        logger.info("Refreshing dashboard data");
        
        loadDashboardData();
        showNotification("Dashboard actualizado", "Los datos han sido refrescados");
    }

    @FXML
    private void applyFilters() {
        logger.info("Applying date filters");
        loadTransactions();
    }

    @FXML
    private void clearFilters() {
        filterStartDate.setValue(null);
        filterEndDate.setValue(null);
        initializeDatePickerDefaults();
        loadTransactions();
    }

    @FXML
    private void viewAllTransactions() {
        logger.debug("View all transactions clicked");
    }

    @FXML
    private void createQuickTransaction() {
        String type = quickTransactionType.getValue();
        String description = quickDescription.getText();
        String amountText = quickAmount.getText();
        String accountSelection = quickAccountSelect.getValue();

        if (type == null || description.isEmpty() || amountText.isEmpty() || 
            accountSelection == null || accountSelection.equals("Seleccione una cuenta...")) {
            showAlert(Alert.AlertType.WARNING, "Campos requeridos",
                    "Por favor complete todos los campos incluyendo la cuenta");
            return;
        }

        try {
            BigDecimal amount = new BigDecimal(amountText);
            String accountCode = accountSelection.split(" - ")[0];
            
            showAlert(Alert.AlertType.INFORMATION, "Transacción Creada",
                    "Transacción rápida creada: " + type + " - " + amount + 
                    " en cuenta " + accountCode);

            quickDescription.clear();
            quickAmount.clear();
            quickAccountSelect.setValue("Seleccione una cuenta...");
            
            loadDashboardData();

        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Error",
                    "Monto inválido. Use formato numérico decimal (ej: 100.50)");
        }
    }

    private void startAutoRefresh() {
        Timer timer = new Timer(true);
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                Platform.runLater(() -> {
                    logger.debug("Auto-refreshing dashboard");
                    loadFinancialSummary();
                    loadKPIs();
                    loadStatistics();
                });
            }
        }, 30000, 30000);
    }

    private void showLoading(boolean show) {
        Platform.runLater(() -> {
            loadingOverlay.setVisible(show);
            loadingOverlay.setManaged(show);
        });
    }

    private void showNotification(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.initOwner(refreshButton.getScene().getWindow());
        alert.showAndWait();
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.initOwner(refreshButton.getScene().getWindow());
        alert.showAndWait();
    }

    private String formatCurrency(BigDecimal amount) {
        return "$ " + String.format("%,.2f", amount.doubleValue());
    }

    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.initOwner(refreshButton.getScene().getWindow());
        alert.showAndWait();
    }
}
