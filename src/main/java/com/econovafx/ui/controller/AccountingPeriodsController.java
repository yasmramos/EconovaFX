package com.econovafx.ui.controller;

import com.econovafx.model.AccountingPeriod;
import com.econovafx.service.AccountingPeriodService;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

/**
 * Controller for managing accounting periods and closing operations.
 */
public class AccountingPeriodsController {

    private static final Logger log = LoggerFactory.getLogger(AccountingPeriodsController.class);
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @FXML private Label totalPeriodsLabel;
    @FXML private Label openPeriodsLabel;
    @FXML private Label closedPeriodsLabel;
    @FXML private Label lockedPeriodsLabel;
    @FXML private Label currentPeriodInfo;
    
    @FXML private TableView<AccountingPeriod> periodsTable;
    @FXML private TableColumn<AccountingPeriod, String> colName;
    @FXML private TableColumn<AccountingPeriod, String> colStartDate;
    @FXML private TableColumn<AccountingPeriod, String> colEndDate;
    @FXML private TableColumn<AccountingPeriod, String> colStatus;
    @FXML private TableColumn<AccountingPeriod, String> colClosedBy;
    @FXML private TableColumn<AccountingPeriod, String> colClosedDate;
    @FXML private TableColumn<AccountingPeriod, Void> colActions;
    
    @FXML private Button newPeriodButton;
    @FXML private Button closePeriodButton;
    @FXML private Button reopenPeriodButton;
    @FXML private Button lockPeriodButton;

    private final AccountingPeriodService periodService;

    public AccountingPeriodsController(AccountingPeriodService periodService) {
        this.periodService = periodService;
    }

    @FXML
    public void initialize() {
        setupTableColumns();
        loadPeriods();
        updateStatistics();
    }

    private void setupTableColumns() {
        colName.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getName()));
        colStartDate.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleStringProperty(cellData.getValue().getStartDate().format(DATE_FORMATTER)));
        colEndDate.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleStringProperty(cellData.getValue().getEndDate().format(DATE_FORMATTER)));
        colStatus.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleStringProperty(cellData.getValue().getStatus().toString()));
        colClosedBy.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleStringProperty(cellData.getValue().getClosedBy() != null ? cellData.getValue().getClosedBy() : ""));
        colClosedDate.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleStringProperty(cellData.getValue().getClosedDate() != null ? cellData.getValue().getClosedDate().format(DATE_FORMATTER) : ""));

        // Setup action buttons column
        setupActionColumn();
    }

    private void setupActionColumn() {
        colActions.setCellFactory(param -> new TableCell<>() {
            private final HBox hbox = new HBox(5);
            private final Button btnClose = createActionButton("Close", "bg-orange-500");
            private final Button btnReopen = createActionButton("Reopen", "bg-blue-500");
            private final Button btnLock = createActionButton("Lock", "bg-red-500");

            {
                hbox.getChildren().addAll(btnClose, btnReopen, btnLock);
                
                btnClose.setOnAction(event -> {
                    AccountingPeriod period = getTableView().getItems().get(getIndex());
                    handlePeriodAction(period, "close");
                });
                
                btnReopen.setOnAction(event -> {
                    AccountingPeriod period = getTableView().getItems().get(getIndex());
                    handlePeriodAction(period, "reopen");
                });
                
                btnLock.setOnAction(event -> {
                    AccountingPeriod period = getTableView().getItems().get(getIndex());
                    handlePeriodAction(period, "lock");
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    AccountingPeriod period = getTableView().getItems().get(getIndex());
                    updateButtonsState(period);
                    setGraphic(hbox);
                }
            }

            private void updateButtonsState(AccountingPeriod period) {
                btnClose.setDisable(!period.isOpen());
                btnReopen.setDisable(!period.isClosed() || period.getStatus() == AccountingPeriod.PeriodStatus.LOCKED);
                btnLock.setDisable(!period.isClosed());
            }
        });
    }

    private Button createActionButton(String text, String colorClass) {
        Button button = new Button(text);
        button.getStyleClass().addAll(colorClass, "text-white", "px-2", "py-1", "rounded-md", "cursor-hand");
        return button;
    }

    private void handlePeriodAction(AccountingPeriod period, String action) {
        try {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Confirm Action");
            
            switch (action) {
                case "close":
                    alert.setHeaderText("Close Accounting Period");
                    alert.setContentText("Are you sure you want to close the period '" + period.getName() + 
                                       "'? No new transactions will be allowed.");
                    alert.showAndWait().ifPresent(response -> {
                        if (response == ButtonType.OK) {
                            periodService.closePeriod(period.getId(), "Current User");
                            showAlert("Success", "Period closed successfully.", Alert.AlertType.INFORMATION);
                            loadPeriods();
                            updateStatistics();
                        }
                    });
                    break;
                    
                case "reopen":
                    alert.setHeaderText("Reopen Accounting Period");
                    alert.setContentText("Are you sure you want to reopen the period '" + period.getName() + 
                                       "'? This will allow new transactions.");
                    alert.showAndWait().ifPresent(response -> {
                        if (response == ButtonType.OK) {
                            periodService.reopenPeriod(period.getId());
                            showAlert("Success", "Period reopened successfully.", Alert.AlertType.INFORMATION);
                            loadPeriods();
                            updateStatistics();
                        }
                    });
                    break;
                    
                case "lock":
                    alert.setHeaderText("Lock Accounting Period");
                    alert.setContentText("WARNING: Locking the period '" + period.getName() + 
                                       "' is permanent and cannot be undone!");
                    alert.showAndWait().ifPresent(response -> {
                        if (response == ButtonType.OK) {
                            periodService.lockPeriod(period.getId());
                            showAlert("Success", "Period locked successfully.", Alert.AlertType.INFORMATION);
                            loadPeriods();
                            updateStatistics();
                        }
                    });
                    break;
            }
        } catch (Exception e) {
            log.error("Error performing action on period: {}", period.getName(), e);
            showAlert("Error", "Failed to perform action: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void newPeriod() {
        try {
            Dialog<AccountingPeriod> dialog = new Dialog<>();
            dialog.setTitle("New Accounting Period");
            dialog.setHeaderText("Create a new accounting period");

            ButtonType createButtonType = new ButtonType("Create", ButtonBar.ButtonData.OK_DONE);
            dialog.getDialogPane().getButtonTypes().addAll(createButtonType, ButtonType.CANCEL);

            GridPane grid = new GridPane();
            grid.setHgap(10);
            grid.setVgap(10);
            grid.setPadding(new Insets(20, 150, 10, 10));

            TextField nameField = new TextField();
            nameField.setPromptText("e.g., Fiscal Year 2024");
            DatePicker startDatePicker = new DatePicker(LocalDate.now().withDayOfMonth(1));
            DatePicker endDatePicker = new DatePicker(LocalDate.now().withDayOfMonth(1).plusMonths(1).minusDays(1));

            grid.add(new Label("Period Name:"), 0, 0);
            grid.add(nameField, 1, 0);
            grid.add(new Label("Start Date:"), 0, 1);
            grid.add(startDatePicker, 1, 1);
            grid.add(new Label("End Date:"), 0, 2);
            grid.add(endDatePicker, 1, 2);

            dialog.getDialogPane().setContent(grid);

            dialog.setResultConverter(dialogButton -> {
                if (dialogButton == createButtonType) {
                    return new AccountingPeriod(
                        nameField.getText(),
                        startDatePicker.getValue(),
                        endDatePicker.getValue()
                    );
                }
                return null;
            });

            Optional<AccountingPeriod> result = dialog.showAndWait();
            result.ifPresent(period -> {
                try {
                    periodService.createPeriod(period.getName(), period.getStartDate(), period.getEndDate());
                    showAlert("Success", "Accounting period created successfully.", Alert.AlertType.INFORMATION);
                    loadPeriods();
                    updateStatistics();
                } catch (Exception e) {
                    showAlert("Error", "Failed to create period: " + e.getMessage(), Alert.AlertType.ERROR);
                }
            });

        } catch (Exception e) {
            log.error("Error creating new period", e);
            showAlert("Error", "Failed to create period: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void closePeriod() {
        AccountingPeriod selected = periodsTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Warning", "Please select a period to close.", Alert.AlertType.WARNING);
            return;
        }
        handlePeriodAction(selected, "close");
    }

    @FXML
    private void reopenPeriod() {
        AccountingPeriod selected = periodsTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Warning", "Please select a period to reopen.", Alert.AlertType.WARNING);
            return;
        }
        handlePeriodAction(selected, "reopen");
    }

    @FXML
    private void lockPeriod() {
        AccountingPeriod selected = periodsTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Warning", "Please select a period to lock.", Alert.AlertType.WARNING);
            return;
        }
        handlePeriodAction(selected, "lock");
    }

    private void loadPeriods() {
        try {
            List<AccountingPeriod> periods = periodService.getAllPeriods();
            periodsTable.getItems().setAll(periods);
            updateCurrentPeriodInfo();
        } catch (Exception e) {
            log.error("Error loading periods", e);
            showAlert("Error", "Failed to load periods: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void updateStatistics() {
        try {
            List<AccountingPeriod> periods = periodService.getAllPeriods();
            
            long total = periods.size();
            long open = periods.stream().filter(AccountingPeriod::isOpen).count();
            long closed = periods.stream().filter(p -> p.getStatus() == AccountingPeriod.PeriodStatus.CLOSED).count();
            long locked = periods.stream().filter(p -> p.getStatus() == AccountingPeriod.PeriodStatus.LOCKED).count();

            totalPeriodsLabel.setText(String.valueOf(total));
            openPeriodsLabel.setText(String.valueOf(open));
            closedPeriodsLabel.setText(String.valueOf(closed));
            lockedPeriodsLabel.setText(String.valueOf(locked));
        } catch (Exception e) {
            log.error("Error updating statistics", e);
        }
    }

    private void updateCurrentPeriodInfo() {
        Optional<AccountingPeriod> currentPeriod = periodService.getCurrentOpenPeriod();
        if (currentPeriod.isPresent()) {
            AccountingPeriod period = currentPeriod.get();
            currentPeriodInfo.setText("Current open period: " + period.getName() + 
                                    " (" + period.getStartDate() + " to " + period.getEndDate() + ")");
            currentPeriodInfo.getStyleClass().removeAll("text-yellow-800");
            currentPeriodInfo.getStyleClass().add("text-green-800");
        } else {
            currentPeriodInfo.setText("No open period available. Please create or reopen a period.");
            currentPeriodInfo.getStyleClass().removeAll("text-green-800");
            currentPeriodInfo.getStyleClass().add("text-yellow-800");
        }
    }

    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
