package com.econovafx.modules.core.ui.controller;

import com.econovafx.modules.accounting.model.Account;
import com.econovafx.modules.accounting.model.AccountType;
import com.econovafx.modules.accounting.model.Transaction;
import com.econovafx.modules.accounting.model.TransactionEntry;
import com.econovafx.modules.accounting.service.AccountService;
import com.econovafx.modules.accounting.service.TransactionService;
import com.econovafx.modules.accounting.service.TransactionService.TransactionEntryData;
import com.econovafx.modules.core.exception.ValidationException;
import com.econovafx.modules.core.model.SystemConfiguration;
import com.econovafx.modules.core.service.SystemConfigService;
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
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import org.kordamp.ikonli.javafx.FontIcon;
import org.kordamp.ikonli.materialdesign2.MaterialDesignA;
import org.kordamp.ikonli.materialdesign2.MaterialDesignM;
import javafx.scene.paint.Color;
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
@Component
public class DashboardController implements Initializable {

    private static final Logger logger = LoggerFactory.getLogger(DashboardController.class);

    private final AccountService accountService;
    private final TransactionService transactionService;
    private final SystemConfigService systemConfigService;
    private ViewFactory viewFactory;

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
    private HBox assetsTrendIconContainer;
    @FXML
    private Label liabilitiesTrendLabel;
    @FXML
    private HBox liabilitiesTrendIconContainer;
    @FXML
    private Label equityTrendLabel;
    @FXML
    private HBox equityTrendIconContainer;
    @FXML
    private Label balanceTrendLabel;
    @FXML
    private HBox balanceTrendIconContainer;

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
                               SystemConfigService systemConfigService) {
        this.accountService = accountService;
        this.transactionService = transactionService;
        this.systemConfigService = systemConfigService;
    }

    /**
     * Initialize ViewFactory reference (two-phase initialization pattern)
     */
    public void initializeViewFactory(ViewFactory viewFactory) {
        this.viewFactory = viewFactory;
    }

    /**
     * Complete initialization after ViewFactory is fully constructed
     */
    public void completeInitialization(ViewFactory viewFactory) {
        // Additional initialization if needed
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        logger.info("DashboardController initialized");
        
        initializeTableColumns();
        
        // Only load data if tenant is already selected (e.g., returning to dashboard)
        // Otherwise, wait for onCompanySelected() to be called after company selection
        if (com.econovafx.modules.core.config.TenantContext.hasTenant()) {
            try {
                initializeComboBoxes();
                initializeDatePickerDefaults();
                loadDashboardData();
            } catch (Exception e) {
                logger.warn("Could not initialize dashboard data (DB might not be ready): {}", e.getMessage());
            }
        } else {
            logger.info("No tenant selected yet, waiting for company selection to load dashboard data");
        }
        
        // Auto-refresh every 30 seconds (only if tenant is selected)
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

    public void loadDashboardData() {
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
        LocalDate endDate = filterEndDate.getValue() != null ? filterEndDate.getValue() : LocalDate.now();
        LocalDate startDate = filterStartDate.getValue() != null ? filterStartDate.getValue() : endDate.minusDays(30);
        
        // Calculate current period balances
        Map<AccountType, BigDecimal> currentBalances = calculateBalancesByAccountType(startDate, endDate);
        
        // Calculate previous period balances (same duration before current period)
        LocalDate prevEndDate = startDate.minusDays(1);
        long periodDays = endDate.toEpochDay() - startDate.toEpochDay();
        LocalDate prevStartDate = prevEndDate.minusDays(periodDays);
        if (prevStartDate.isBefore(LocalDate.of(1900, 1, 1))) {
            prevStartDate = LocalDate.of(1900, 1, 1);
        }
        Map<AccountType, BigDecimal> previousBalances = calculateBalancesByAccountType(prevStartDate, prevEndDate);

        final BigDecimal totalAssets = currentBalances.getOrDefault(AccountType.ASSET, BigDecimal.ZERO);
        final BigDecimal totalLiabilities = currentBalances.getOrDefault(AccountType.LIABILITY, BigDecimal.ZERO);
        final BigDecimal totalEquity = currentBalances.getOrDefault(AccountType.EQUITY, BigDecimal.ZERO);
        
        Platform.runLater(() -> {
            totalAssetsLabel.setText(formatCurrency(totalAssets));
            totalLiabilitiesLabel.setText(formatCurrency(totalLiabilities));
            totalEquityLabel.setText(formatCurrency(totalEquity));
            
            BigDecimal balance = totalAssets.subtract(totalLiabilities);
            balanceLabel.setText(formatCurrency(balance));
            
            updateTrendLabels(currentBalances, previousBalances);
        });
    }

    /**
     * Calculate total balances by account type for a given date range
     */
    private Map<AccountType, BigDecimal> calculateBalancesByAccountType(LocalDate startDate, LocalDate endDate) {
        Map<AccountType, BigDecimal> balances = new EnumMap<>(AccountType.class);
        balances.put(AccountType.ASSET, BigDecimal.ZERO);
        balances.put(AccountType.LIABILITY, BigDecimal.ZERO);
        balances.put(AccountType.EQUITY, BigDecimal.ZERO);
        balances.put(AccountType.REVENUE, BigDecimal.ZERO);
        balances.put(AccountType.EXPENSE, BigDecimal.ZERO);

        List<Transaction> transactions = transactionService.getTransactionsByDateRange(startDate, endDate);
        
        for (Transaction t : transactions) {
            if (!t.getIsPosted() || t.getDate() == null) {
                continue;
            }
            
            for (TransactionEntry entry : t.getEntries()) {
                Account account = entry.getAccount();
                AccountType type = account.getType();
                
                if (type == null) continue;
                
                BigDecimal debit = entry.getDebitAmount() != null ? entry.getDebitAmount() : BigDecimal.ZERO;
                BigDecimal credit = entry.getCreditAmount() != null ? entry.getCreditAmount() : BigDecimal.ZERO;
                
                // For assets and expenses: debit increases, credit decreases
                if (type == AccountType.ASSET || type == AccountType.EXPENSE) {
                    balances.put(type, balances.get(type).add(debit).subtract(credit));
                }
                // For liabilities, equity, and revenue: credit increases, debit decreases
                else if (type == AccountType.LIABILITY || type == AccountType.EQUITY || type == AccountType.REVENUE) {
                    balances.put(type, balances.get(type).add(credit).subtract(debit));
                }
            }
        }
        
        return balances;
    }

    /**
     * Update trend labels with real percentage variations between periods
     */
    private void updateTrendLabels(Map<AccountType, BigDecimal> currentBalances, 
                                    Map<AccountType, BigDecimal> previousBalances) {
        BigDecimal currentAssets = currentBalances.getOrDefault(AccountType.ASSET, BigDecimal.ZERO);
        BigDecimal previousAssets = previousBalances.getOrDefault(AccountType.ASSET, BigDecimal.ZERO);
        updateTrendLabel(assetsTrendLabel, assetsTrendIconContainer, currentAssets, previousAssets, true);
        
        BigDecimal currentLiabilities = currentBalances.getOrDefault(AccountType.LIABILITY, BigDecimal.ZERO);
        BigDecimal previousLiabilities = previousBalances.getOrDefault(AccountType.LIABILITY, BigDecimal.ZERO);
        updateTrendLabel(liabilitiesTrendLabel, liabilitiesTrendIconContainer, currentLiabilities, previousLiabilities, false);
        
        BigDecimal currentEquity = currentBalances.getOrDefault(AccountType.EQUITY, BigDecimal.ZERO);
        BigDecimal previousEquity = previousBalances.getOrDefault(AccountType.EQUITY, BigDecimal.ZERO);
        updateTrendLabel(equityTrendLabel, equityTrendIconContainer, currentEquity, previousEquity, true);
        
        BigDecimal currentBalance = currentAssets.subtract(currentLiabilities);
        BigDecimal previousBalance = previousAssets.subtract(previousLiabilities);
        updateTrendLabel(balanceTrendLabel, balanceTrendIconContainer, currentBalance, previousBalance, true);
    }

    /**
     * Update a single trend label with icon and text based on percentage variation
     * @param label The label to display the percentage text
     * @param iconContainer The container to hold the trend icon
     * @param current Current period value
     * @param previous Previous period value
     * @param favorableIncrease true if increase is favorable (assets, equity, balance), 
     *                          false if increase is unfavorable (liabilities)
     */
    private void updateTrendLabel(Label label, HBox iconContainer, BigDecimal current, BigDecimal previous, boolean favorableIncrease) {
        TrendData trendData = calculateTrendData(current, previous, favorableIncrease);
        
        // Update label text with percentage
        label.setText(trendData.percentageText);
        label.setStyle("-fx-text-fill: " + trendData.colorHex + ";");
        
        // Update icon container with FontIcon
        if (iconContainer != null) {
            iconContainer.getChildren().clear();
            
            if (!"mdi2l-minus".equals(trendData.iconCode)) {
                FontIcon trendIcon = new FontIcon(getMaterialDesignIcon(trendData.iconCode));
                trendIcon.setIconSize(16);
                trendIcon.setIconColor(Color.web(trendData.colorHex));
                iconContainer.getChildren().add(trendIcon);
            }
        }
    }

    /**
     * Calculate trend data including icon code, percentage text and color
     * @param current Current period value
     * @param previous Previous period value
     * @param favorableIncrease true if increase is favorable
     * @return TrendData object with icon code, percentage text and color
     */
    private TrendData calculateTrendData(BigDecimal current, BigDecimal previous, boolean favorableIncrease) {
        String iconCode;
        String percentageText;
        String colorHex;
        
        if (previous.compareTo(BigDecimal.ZERO) == 0) {
            if (current.compareTo(BigDecimal.ZERO) == 0) {
                return new TrendData("mdi2l-minus", "0.0%", "#6b7280");
            } else {
                return new TrendData("mdi2l-minus", "N/D", "#6b7280");
            }
        }
        
        BigDecimal variation = current.subtract(previous);
        BigDecimal percentage = variation.multiply(new BigDecimal("100"))
                .divide(previous.abs(), 1, java.math.RoundingMode.HALF_UP);
        
        if (variation.compareTo(BigDecimal.ZERO) > 0) {
            iconCode = favorableIncrease ? "mdi2a-arrow-up-bold" : "mdi2a-arrow-down-bold";
            boolean isFavorable = favorableIncrease;
            colorHex = isFavorable ? "#10b981" : "#ef4444";
        } else if (variation.compareTo(BigDecimal.ZERO) < 0) {
            iconCode = favorableIncrease ? "mdi2a-arrow-down-bold" : "mdi2a-arrow-up-bold";
            boolean isFavorable = !favorableIncrease;
            colorHex = isFavorable ? "#10b981" : "#ef4444";
        } else {
            iconCode = "mdi2l-minus";
            colorHex = "#6b7280";
        }
        
        percentageText = percentage.abs().setScale(1, java.math.RoundingMode.HALF_UP) + "%";
        return new TrendData(iconCode, percentageText, colorHex);
    }
    
    /**
     * Helper class to hold trend data
     */
    private static class TrendData {
        String iconCode;
        String percentageText;
        String colorHex;
        
        TrendData(String iconCode, String percentageText, String colorHex) {
            this.iconCode = iconCode;
            this.percentageText = percentageText;
            this.colorHex = colorHex;
        }
    }
    
    /**
     * Get the appropriate MaterialDesign icon based on the icon code string
     * @param iconCode The icon code (e.g., "mdi2a-arrow-up-bold")
     * @return The corresponding Ikonli icon
     */
    private org.kordamp.ikonli.Ikon getMaterialDesignIcon(String iconCode) {
        if (iconCode == null) {
            return MaterialDesignM.MINUS;
        }
        
        switch (iconCode) {
            case "mdi2a-arrow-up-bold":
                return MaterialDesignA.ARROW_UP_BOLD;
            case "mdi2a-arrow-down-bold":
                return MaterialDesignA.ARROW_DOWN_BOLD;
            case "mdi2l-minus":
            default:
                return MaterialDesignM.MINUS;
        }
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
        long postedCount = allTransactions.stream().filter(Transaction::getIsPosted).count();
        BigDecimal avgTransaction;
        if (postedCount > 0) {
            BigDecimal totalPostedAmount = allTransactions.stream()
                    .filter(Transaction::getIsPosted)
                    .map(Transaction::getTotalDebit)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            if (totalPostedAmount != null && postedCount > 0) {
                avgTransaction = totalPostedAmount.divide(BigDecimal.valueOf(postedCount), 
                        java.math.RoundingMode.HALF_UP);
            } else {
                avgTransaction = BigDecimal.ZERO;
            }
        } else {
            avgTransaction = BigDecimal.ZERO;
        }
        
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
        logger.info("View all transactions clicked - navigating to transactions view");
        if (viewFactory != null) {
            // Navigate to the transactions view using the ViewFactory
            // This will replace the current content area with the transactions view
            try {
                // Get the main view controller's content area through the view factory
                // The viewFactory has access to switch views in the main application
                viewFactory.showTransactions();
                logger.debug("Successfully navigated to transactions view");
            } catch (Exception e) {
                logger.error("Error navigating to transactions view", e);
                // Fallback: try to get the stage and show transactions in a new window
                try {
                    javafx.stage.Stage stage = new javafx.stage.Stage();
                    stage.setTitle("All Transactions");
                    javafx.scene.Parent root = (javafx.scene.Parent) viewFactory.createTransactionsView();
                    stage.setScene(new javafx.scene.Scene(root, 1024, 600));
                    stage.show();
                    logger.debug("Opened transactions view in new window");
                } catch (Exception ex) {
                    logger.error("Failed to open transactions view", ex);
                }
            }
        } else {
            logger.warn("ViewFactory is not initialized, cannot navigate to transactions view");
        }
    }

    @FXML
    private void createQuickTransaction() {
        String type = quickTransactionType.getValue();
        String description = quickDescription.getText();
        String amountText = quickAmount.getText();
        String accountSelection = quickAccountSelect.getValue();

        if (type == null || description.isEmpty() || amountText.isEmpty() || 
            accountSelection == null || accountSelection.equals("Seleccione una cuenta...")) {
            showAlert(Alert.AlertType.WARNING, "Required Fields",
                    "Please complete all fields including the account");
            return;
        }

        try {
            BigDecimal amount = new BigDecimal(amountText);
            String accountCode = accountSelection.split(" - ")[0];
            
            // Find the selected account
            Account selectedAccount = accountService.getAccountByCode(accountCode)
                    .orElseThrow(() -> new NoSuchElementException("Account not found: " + accountCode));
            
            // Get system configuration for counterparty account
            SystemConfiguration config = systemConfigService.getCurrentConfig();
            String counterpartyAccountCode = getCounterpartyAccountCode(type, config);
            
            Account counterpartyAccount = accountService.getAccountByCode(counterpartyAccountCode)
                    .orElseThrow(() -> new NoSuchElementException("Counterparty account not found: " + counterpartyAccountCode));
            
            // Create transaction with double-entry bookkeeping
            Transaction transaction = new Transaction();
            transaction.setDate(LocalDate.now());
            transaction.setType(type);
            transaction.setDescription("Quick Transaction: " + description);
            
            // Determine debit/credit based on transaction type
            List<TransactionEntryData> entries = new ArrayList<>();
            
            if ("INGRESO".equals(type) || "GASTO".equals(type)) {
                // For INGRESO (Income): Debit Cash/Bank, Credit Revenue
                // For GASTO (Expense): Debit Expense, Credit Cash/Bank
                if ("INGRESO".equals(type)) {
                    // Debit: Selected account (asset/revenue), Credit: Counterparty (revenue)
                    entries.add(new TransactionEntryData(selectedAccount.getId(), amount, BigDecimal.ZERO, description));
                    entries.add(new TransactionEntryData(counterpartyAccount.getId(), BigDecimal.ZERO, amount, description));
                } else {
                    // GASTO: Debit Expense, Credit Cash/Bank
                    entries.add(new TransactionEntryData(selectedAccount.getId(), amount, BigDecimal.ZERO, description));
                    entries.add(new TransactionEntryData(counterpartyAccount.getId(), BigDecimal.ZERO, amount, description));
                }
            } else if ("TRANSFERENCIA".equals(type)) {
                // Transfer between accounts: Debit destination, Credit source
                entries.add(new TransactionEntryData(selectedAccount.getId(), amount, BigDecimal.ZERO, "Transfer to " + selectedAccount.getCode()));
                entries.add(new TransactionEntryData(counterpartyAccount.getId(), BigDecimal.ZERO, amount, "Transfer from " + counterpartyAccount.getCode()));
            } else {
                // ASIENTO (Journal Entry): Use selected account as debit, counterparty as credit
                entries.add(new TransactionEntryData(selectedAccount.getId(), amount, BigDecimal.ZERO, description));
                entries.add(new TransactionEntryData(counterpartyAccount.getId(), BigDecimal.ZERO, amount, description));
            }
            
            // Create and persist the transaction
            Transaction createdTransaction = transactionService.createTransaction(transaction, entries);
            
            // Post the transaction immediately for quick transactions
            transactionService.postTransaction(createdTransaction.getId());
            
            showAlert(Alert.AlertType.INFORMATION, "Transaction Created",
                    "Quick transaction created successfully: " + type + " - " + amount + 
                    " in account " + accountCode);

            quickDescription.clear();
            quickAmount.clear();
            quickAccountSelect.setValue("Seleccione una cuenta...");
            
            loadDashboardData();

        } catch (NoSuchElementException e) {
            logger.error("Account not found", e);
            showAlert(Alert.AlertType.ERROR, "Error",
                    "Account not found: " + e.getMessage());
        } catch (ValidationException e) {
            logger.error("Validation error creating transaction", e);
            showAlert(Alert.AlertType.ERROR, "Validation Error",
                    e.getMessage());
        } catch (Exception e) {
            logger.error("Error creating quick transaction", e);
            showAlert(Alert.AlertType.ERROR, "Error",
                    "Error creating transaction: " + e.getMessage());
        }
    }
    
    /**
     * Determines the counterparty account code based on transaction type and system configuration.
     * @param type The transaction type (INGRESO, GASTO, TRANSFERENCIA, ASIENTO)
     * @param config The system configuration
     * @return The account code for the counterparty entry
     */
    private String getCounterpartyAccountCode(String type, SystemConfiguration config) {
        if ("INGRESO".equals(type)) {
            // For income, credit revenue account
            return config.getRevenueAccountCode() != null ? config.getRevenueAccountCode() : "401-001";
        } else if ("GASTO".equals(type)) {
            // For expense, credit cash/bank account
            return config.getCashAccountCode() != null ? config.getCashAccountCode() : "101-001";
        } else if ("TRANSFERENCIA".equals(type)) {
            // For transfers, use cash account as default counterparty
            return config.getCashAccountCode() != null ? config.getCashAccountCode() : "101-001";
        } else {
            // For journal entries (ASIENTO), use cash account as default
            return config.getCashAccountCode() != null ? config.getCashAccountCode() : "101-001";
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
        if (loadingOverlay != null) {
            Platform.runLater(() -> {
                loadingOverlay.setVisible(show);
                loadingOverlay.setManaged(show);
            });
        }
    }

    private void showNotification(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        
        // Only set owner if the button is properly initialized and added to the scene
        if (refreshButton != null && refreshButton.getScene() != null) {
            alert.initOwner(refreshButton.getScene().getWindow());
        }
        
        alert.show();
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        
        // Only set owner if the button is properly initialized and added to the scene
        if (refreshButton != null && refreshButton.getScene() != null) {
            alert.initOwner(refreshButton.getScene().getWindow());
        }
        
        alert.show();
    }

    private String formatCurrency(BigDecimal amount) {
        return "$ " + String.format("%,.2f", amount.doubleValue());
    }

    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        
        // Only set owner if the button is properly initialized and added to the scene
        if (refreshButton != null && refreshButton.getScene() != null) {
            alert.initOwner(refreshButton.getScene().getWindow());
        }
        
        alert.showAndWait();
    }
}
