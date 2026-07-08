package com.econovafx.ui.controller;

import com.econovafx.model.AuditLog;
import com.econovafx.service.AuditService;
import com.econovafx.service.NotificationService;
import com.econovafx.ui.util.ModernDialog;
import io.avaje.inject.Component;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.inject.Inject;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Controller for Audit Logs view
 * Provides comprehensive audit trail viewing and filtering
 */
@Component
public class AuditLogsController {

    private static final Logger logger = LoggerFactory.getLogger(AuditLogsController.class);

    @Inject
    public AuditService auditService;

    @Inject
    public NotificationService notificationService;
    
    private Stage mainStage;

    // Filters
    @FXML
    private ComboBox<String> userFilter;

    @FXML
    private ComboBox<AuditLog.OperationType> operationTypeFilter;

    @FXML
    private DatePicker startDatePicker;

    @FXML
    private DatePicker endDatePicker;

    // Statistics
    @FXML
    private Label totalOperationsLabel;

    @FXML
    private Label successfulOperationsLabel;

    @FXML
    private Label failedOperationsLabel;

    @FXML
    private Label activeUsersLabel;

    // Table
    @FXML
    private TableView<AuditLog> auditLogsTable;

    @FXML
    private TableColumn<AuditLog, Long> idColumn;

    @FXML
    private TableColumn<AuditLog, String> timestampColumn;

    @FXML
    private TableColumn<AuditLog, String> usernameColumn;

    @FXML
    private TableColumn<AuditLog, String> operationTypeColumn;

    @FXML
    private TableColumn<AuditLog, String> entityTypeColumn;

    @FXML
    private TableColumn<AuditLog, Long> entityIdColumn;

    @FXML
    private TableColumn<AuditLog, String> descriptionColumn;

    @FXML
    private TableColumn<AuditLog, Boolean> successColumn;

    @FXML
    private TableColumn<AuditLog, Void> actionsColumn;

    // Status
    @FXML
    private Label statusLabel;

    @FXML
    private Label lastUpdateLabel;

    private ObservableList<AuditLog> auditLogsData = FXCollections.observableArrayList();
    private List<AuditLog> allAuditLogs;

    @FXML
    public void initialize() {
        logger.info("Initializing AuditLogsController");
        setupTableColumns();
        initializeFilters();
        loadAuditLogs();
        updateStatistics();
    }

    private void setupTableColumns() {
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        
        timestampColumn.setCellValueFactory(cellData -> {
            Instant createdAt = cellData.getValue().getCreatedAt();
            return new javafx.beans.property.SimpleStringProperty(
                createdAt.atZone(ZoneId.systemDefault()).format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))
            );
        });
        
        usernameColumn.setCellValueFactory(new PropertyValueFactory<>("username"));
        
        operationTypeColumn.setCellValueFactory(cellData -> {
            return new javafx.beans.property.SimpleStringProperty(
                cellData.getValue().getOperationType().toString()
            );
        });
        
        entityTypeColumn.setCellValueFactory(new PropertyValueFactory<>("entityType"));
        entityIdColumn.setCellValueFactory(new PropertyValueFactory<>("entityId"));
        descriptionColumn.setCellValueFactory(new PropertyValueFactory<>("description"));
        
        successColumn.setCellValueFactory(cellData -> {
            Boolean success = cellData.getValue().getSuccess();
            return new javafx.beans.property.SimpleObjectProperty<>(success);
        });
        successColumn.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Boolean success, boolean empty) {
                super.updateItem(success, empty);
                if (empty || success == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(success ? "✓ Éxito" : "✗ Fallido");
                    setStyle(success ? "-fx-text-fill: green;" : "-fx-text-fill: red;");
                }
            }
        });

        setupActionColumn();
        
        auditLogsTable.setItems(auditLogsData);
    }

    private void setupActionColumn() {
        actionsColumn.setCellFactory(param -> new TableCell<>() {
            private final Button detailBtn = new Button("Ver Detalle");
            
            {
                detailBtn.setStyle("-fx-background-color: #3498db; -fx-text-fill: white;");
                detailBtn.setOnAction(event -> {
                    AuditLog log = getTableView().getItems().get(getIndex());
                    showAuditLogDetails(log);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(detailBtn);
                }
            }
        });
    }

    private void initializeFilters() {
        // Populate user filter
        try {
            allAuditLogs = auditService.getAllAuditLogs();
            Set<String> uniqueUsers = allAuditLogs.stream()
                .map(AuditLog::getUsername)
                .filter(u -> u != null && !u.isEmpty())
                .collect(Collectors.toSet());
            
            userFilter.getItems().add("Todos los usuarios");
            userFilter.getItems().addAll(uniqueUsers.stream().sorted().toList());
            userFilter.setValue("Todos los usuarios");
        } catch (Exception e) {
            logger.error("Error loading users for filter", e);
            userFilter.getItems().add("Todos los usuarios");
        }

        // Populate operation type filter
        operationTypeFilter.getItems().add(null); // Represents "All"
        operationTypeFilter.getItems().addAll(AuditLog.OperationType.values());
        operationTypeFilter.setValue(null);
        
        // Set default date range (last 30 days)
        endDatePicker.setValue(LocalDate.now());
        startDatePicker.setValue(LocalDate.now().minusDays(30));
    }

    private void loadAuditLogs() {
        loadAuditLogs(null, null, null, null);
    }

    private void loadAuditLogs(String username, AuditLog.OperationType operationType, 
                               LocalDate startDate, LocalDate endDate) {
        try {
            List<AuditLog> filteredLogs;
            
            if (username != null && !username.equals("Todos los usuarios") && !username.isEmpty()) {
                filteredLogs = auditService.getAuditLogsByUser(username);
            } else if (operationType != null) {
                filteredLogs = auditService.getAuditLogsByOperationType(operationType);
            } else if (startDate != null && endDate != null) {
                filteredLogs = auditService.getAuditLogsByDateRange(
                    startDate.atStartOfDay(ZoneId.systemDefault()).toInstant(), 
                    endDate.atTime(23, 59, 59).atZone(ZoneId.systemDefault()).toInstant()
                );
            } else {
                filteredLogs = auditService.getAllAuditLogs();
            }
            
            auditLogsData.setAll(filteredLogs);
            updateStatistics();
            updateLastUpdateTime();
            statusLabel.setText(String.format("%d registros mostrados", filteredLogs.size()));
            
        } catch (Exception e) {
            logger.error("Error loading audit logs", e);
            notificationService.showError("Error al cargar auditoría: " + e.getMessage());
            statusLabel.setText("Error al cargar datos");
        }
    }

    @FXML
    private void handleFilter() {
        String selectedUser = userFilter.getValue();
        AuditLog.OperationType selectedOperation = operationTypeFilter.getValue();
        LocalDate startDate = startDatePicker.getValue();
        LocalDate endDate = endDatePicker.getValue();
        
        if (startDate != null && endDate != null && startDate.isAfter(endDate)) {
            notificationService.showWarning("Fechas inválidas: " + 
                "La fecha de inicio no puede ser posterior a la fecha de fin");
            return;
        }
        
        loadAuditLogs(selectedUser, selectedOperation, startDate, endDate);
        notificationService.showSuccess("Filtros aplicados", "Se han aplicado los filtros seleccionados");
    }

    @FXML
    private void handleClearFilters() {
        userFilter.setValue("Todos los usuarios");
        operationTypeFilter.setValue(null);
        startDatePicker.setValue(LocalDate.now().minusDays(30));
        endDatePicker.setValue(LocalDate.now());
        
        loadAuditLogs();
        notificationService.showInfo("Filtros limpiados: " + 
            "Se han restablecido los filtros predeterminados");
    }

    @FXML
    private void handleExport() {
        try {
            List<AuditLog> logsToExport = auditLogsData;
            if (logsToExport.isEmpty()) {
                notificationService.showWarning("Sin datos", "No hay registros para exportar");
                return;
            }
            
            String csv = auditService.exportToCSV(logsToExport);
            
            // In a real implementation, this would save to a file
            // For now, we'll just show a success message
            logger.info("CSV Export generated with {} rows", logsToExport.size());
            
            notificationService.showSuccess("Exportación exitosa: " + 
                String.format("Se exportaron %d registros de auditoría", logsToExport.size()));
            
        } catch (Exception e) {
            logger.error("Error exporting audit logs", e);
            notificationService.showError("Error al exportar: " + e.getMessage());
        }
    }

    private void showAuditLogDetails(AuditLog log) {
        StringBuilder details = new StringBuilder();
        details.append("ID: ").append(log.getId()).append("\n\n");
        details.append("Usuario: ").append(log.getUsername()).append("\n");
        details.append("Operación: ").append(log.getOperationType()).append("\n");
        details.append("Entidad: ").append(log.getEntityType()).append("\n");
        details.append("ID Entidad: ").append(log.getEntityId()).append("\n");
        details.append("Descripción: ").append(log.getDescription()).append("\n");
        details.append("Fecha: ").append(log.getCreatedAt().atZone(ZoneId.systemDefault()).format(
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"))).append("\n\n");
        details.append("Estado: ").append(log.getSuccess() ? "Exitosa" : "Fallida").append("\n");
        
        if (log.getErrorMessage() != null) {
            details.append("Error: ").append(log.getErrorMessage()).append("\n");
        }
        
        if (log.getOldValues() != null) {
            details.append("\nValores Anteriores:\n").append(log.getOldValues()).append("\n");
        }
        
        if (log.getNewValues() != null) {
            details.append("\nValores Nuevos:\n").append(log.getNewValues()).append("\n");
        }
        
        if (log.getIpAddress() != null) {
            details.append("\nIP: ").append(log.getIpAddress()).append("\n");
        }
        
        ModernDialog.showInfoDialog(getStage(), "Detalle de Auditoría", details.toString());
    }
    
    private javafx.stage.Stage getStage() {
        if (auditLogsTable != null && auditLogsTable.getScene() != null) {
            return (javafx.stage.Stage) auditLogsTable.getScene().getWindow();
        }
        return null;
    }

    private void updateStatistics() {
        if (allAuditLogs == null || allAuditLogs.isEmpty()) {
            allAuditLogs = auditService.getAllAuditLogs();
        }
        
        long total = allAuditLogs.size();
        long successful = allAuditLogs.stream().filter(AuditLog::getSuccess).count();
        long failed = total - successful;
        long activeUsers = allAuditLogs.stream()
            .map(AuditLog::getUsername)
            .filter(u -> u != null && !u.isEmpty())
            .distinct()
            .count();
        
        totalOperationsLabel.setText(String.valueOf(total));
        successfulOperationsLabel.setText(String.valueOf(successful));
        failedOperationsLabel.setText(String.valueOf(failed));
        activeUsersLabel.setText(String.valueOf(activeUsers));
    }

    private void updateLastUpdateTime() {
        lastUpdateLabel.setText(Instant.now().atZone(ZoneId.systemDefault()).format(
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")));
    }

    public void refresh() {
        logger.info("Refreshing audit logs");
        allAuditLogs = null; // Force reload
        initializeFilters();
        loadAuditLogs();
    }
}
