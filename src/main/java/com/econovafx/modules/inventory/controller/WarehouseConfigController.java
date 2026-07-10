package com.econovafx.modules.inventory.controller;

import com.econovafx.modules.core.model.AuditLog;
import com.econovafx.modules.core.model.User;
import com.econovafx.modules.inventory.model.ValuationMethod;
import com.econovafx.modules.inventory.model.Warehouse;
import com.econovafx.modules.core.repository.UserRepository;
import com.econovafx.modules.core.repository.WarehouseRepository;
import com.econovafx.modules.core.service.AuditService;
import com.econovafx.modules.core.ui.util.ModernDialog;
import com.econovafx.modules.core.ui.util.NotificationService;
import jakarta.inject.Inject;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;

/**
 * Controlador para la gestión y configuración de almacenes.
 * Permite crear, editar, activar/desactivar y configurar métodos de valoración.
 */
public class WarehouseConfigController {

    private static final Logger log = LoggerFactory.getLogger(WarehouseConfigController.class);

    @Inject
    private WarehouseRepository warehouseRepository;

    @Inject
    private UserRepository userRepository;

    @Inject
    private AuditService auditService;

    @Inject
    private NotificationService notificationService;

    @FXML
    private TableView<Warehouse> warehouseTable;

    @FXML
    private TableColumn<Warehouse, String> codeColumn;

    @FXML
    private TableColumn<Warehouse, String> nameColumn;

    @FXML
    private TableColumn<Warehouse, String> locationColumn;

    @FXML
    private TableColumn<Warehouse, String> valuationMethodColumn;

    @FXML
    private TableColumn<Warehouse, String> managerColumn;

    @FXML
    private TableColumn<Warehouse, Boolean> activeColumn;

    @FXML
    private TextField searchField;

    @FXML
    private Button addBtn;

    @FXML
    private Button editBtn;

    @FXML
    private Button toggleActiveBtn;

    @FXML
    private Button deleteBtn;

    private ObservableList<Warehouse> warehouseList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        log.info("Inicializando WarehouseConfigController");
        setupTableColumns();
        loadWarehouses();
        setupListeners();
    }

    private void setupTableColumns() {
        codeColumn.setCellValueFactory(new PropertyValueFactory<>("code"));
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        locationColumn.setCellValueFactory(new PropertyValueFactory<>("location"));
        
        valuationMethodColumn.setCellValueFactory(cellData -> {
            Warehouse warehouse = cellData.getValue();
            return new javafx.beans.property.SimpleStringProperty(
                warehouse.getValuationMethod().getDescription()
            );
        });
        
        managerColumn.setCellValueFactory(cellData -> {
            Warehouse warehouse = cellData.getValue();
            User manager = warehouse.getManager();
            return new javafx.beans.property.SimpleStringProperty(
                manager != null ? manager.getUsername() : "Sin asignar"
            );
        });
        
        activeColumn.setCellValueFactory(new PropertyValueFactory<>("active"));
        activeColumn.setCellFactory(tc -> new TableCell<>() {
            @Override
            protected void updateItem(Boolean active, boolean empty) {
                super.updateItem(active, empty);
                if (empty || active == null) {
                    setText(null);
                } else {
                    setText(active ? "Activo" : "Inactivo");
                    setStyle(active ? "-fx-text-fill: green;" : "-fx-text-fill: red;");
                }
            }
        });

        warehouseTable.setItems(warehouseList);
        warehouseTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, newSel) -> updateButtonStates());
    }

    private void loadWarehouses() {
        try {
            List<Warehouse> warehouses = warehouseRepository.findAllActiveAndInactive();
            warehouseList.setAll(warehouses);
            log.info("Cargados {} almacenes", warehouses.size());
        } catch (Exception e) {
            log.error("Error cargando almacenes", e);
            notificationService.showError(getCurrentStage(), "Error al cargar almacenes: " + e.getMessage());
        }
    }

    private void setupListeners() {
        searchField.textProperty().addListener((obs, oldText, newText) -> filterWarehouses(newText));
    }

    private void filterWarehouses(String searchText) {
        if (searchText == null || searchText.trim().isEmpty()) {
            loadWarehouses();
            return;
        }

        String filter = searchText.toLowerCase();
        List<Warehouse> filtered = warehouseList.stream()
            .filter(w -> w.getCode().toLowerCase().contains(filter) ||
                        w.getName().toLowerCase().contains(filter) ||
                        (w.getLocation() != null && w.getLocation().toLowerCase().contains(filter)))
            .toList();
        
        warehouseList.setAll(filtered);
    }

    private void updateButtonStates() {
        Warehouse selected = warehouseTable.getSelectionModel().getSelectedItem();
        boolean hasSelection = selected != null;
        
        editBtn.setDisable(!hasSelection);
        toggleActiveBtn.setDisable(!hasSelection);
        deleteBtn.setDisable(!hasSelection);
        
        if (hasSelection) {
            toggleActiveBtn.setText(selected.isActive() ? "Desactivar" : "Activar");
        }
    }

    @FXML
    private void onAddWarehouse() {
        log.info("Abriendo diálogo para nuevo almacén");
        Optional<Warehouse> result = showWarehouseDialog(null);
        result.ifPresent(warehouse -> {
            try {
                warehouseRepository.save(warehouse);
                auditService.logWithValues(
                    "SYSTEM",
                    AuditLog.OperationType.CREATE,
                    "Warehouse",
                    warehouse.getId(),
                    "Creó almacén: " + warehouse.getCode(),
                    null,
                    warehouse.toString()
                );
                notificationService.showSuccess(getCurrentStage(), "Almacén '" + warehouse.getName() + "' creado exitosamente");
                loadWarehouses();
            } catch (Exception e) {
                log.error("Error creando almacén", e);
                notificationService.showError(getCurrentStage(), "Error al crear almacén: " + e.getMessage());
            }
        });
    }

    @FXML
    private void onEditWarehouse() {
        Warehouse selected = warehouseTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            notificationService.showWarning(getCurrentStage(), "Seleccione un almacén para editar");
            return;
        }

        log.info("Editando almacén: {}", selected.getCode());
        String originalData = selected.toString();
        
        Optional<Warehouse> result = showWarehouseDialog(selected);
        result.ifPresent(updatedWarehouse -> {
            try {
                warehouseRepository.save(updatedWarehouse);
                auditService.logWithValues(
                    "SYSTEM",
                    AuditLog.OperationType.UPDATE,
                    "Warehouse",
                    updatedWarehouse.getId(),
                    "Actualizó almacén: " + updatedWarehouse.getCode(),
                    originalData,
                    updatedWarehouse.toString()
                );
                notificationService.showSuccess(getCurrentStage(), "Almacén actualizado exitosamente");
                loadWarehouses();
            } catch (Exception e) {
                log.error("Error actualizando almacén", e);
                notificationService.showError(getCurrentStage(), "Error al actualizar almacén: " + e.getMessage());
            }
        });
    }

    @FXML
    private void onToggleActive() {
        Warehouse selected = warehouseTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            notificationService.showWarning(getCurrentStage(), "Seleccione un almacén");
            return;
        }

        boolean newStatus = !selected.isActive();
        String action = newStatus ? "activar" : "desactivar";
        
        Alert dialog = new Alert(Alert.AlertType.CONFIRMATION);
        dialog.setTitle("Confirmar " + action.substring(0, 1).toUpperCase() + action.substring(1));
        dialog.setHeaderText(null);
        dialog.setContentText("¿Está seguro que desea " + action + " el almacén '" + selected.getName() + "'?");
        dialog.initOwner(getCurrentStage());
        
        Optional<ButtonType> result = dialog.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                selected.setActive(newStatus);
                warehouseRepository.save(selected);
                
                auditService.logWithValues(
                    "SYSTEM",
                    newStatus ? AuditLog.OperationType.UPDATE : AuditLog.OperationType.DELETE,
                    "Warehouse",
                    selected.getId(),
                    "Usuario " + action + " almacén: " + selected.getCode(),
                    "Active: " + !newStatus,
                    "Active: " + newStatus
                );
                
                notificationService.showSuccess(getCurrentStage(), "Almacén " + (newStatus ? "activado" : "desactivado") + " exitosamente");
                loadWarehouses();
            } catch (Exception e) {
                log.error("Error cambiando estado del almacén", e);
                notificationService.showError(getCurrentStage(), "Error al cambiar estado: " + e.getMessage());
            }
        }
    }

    @FXML
    private void onDeleteWarehouse() {
        Warehouse selected = warehouseTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            notificationService.showWarning(getCurrentStage(), "Seleccione un almacén para eliminar");
            return;
        }

        Alert dialog = new Alert(Alert.AlertType.CONFIRMATION);
        dialog.setTitle("Confirmar Eliminación");
        dialog.setHeaderText(null);
        dialog.setContentText("¿Está seguro que desea eliminar permanentemente el almacén '" + selected.getName() + "'?\n\n" +
            "ADVERTENCIA: Esta acción no se puede deshacer y puede afectar los movimientos de inventario asociados.");
        dialog.initOwner(getCurrentStage());
        
        Optional<ButtonType> result = dialog.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                // Verificar si tiene movimientos
                // Esto debería validarse en el servicio
                warehouseRepository.deleteById(selected.getId());
                
                auditService.logWithValues(
                    "SYSTEM",
                    AuditLog.OperationType.DELETE,
                    "WAREHOUSE",
                    selected.getId(),
                    "Usuario eliminó almacén: " + selected.getCode(),
                    selected.toString(),
                    null
                );
                
                notificationService.showSuccess(getCurrentStage(), "Almacén eliminado exitosamente");
                loadWarehouses();
            } catch (Exception e) {
                log.error("Error eliminando almacén", e);
                notificationService.showError(getCurrentStage(), "Error al eliminar almacén: " + e.getMessage());
            }
        }
    }

    private Optional<Warehouse> showWarehouseDialog(Warehouse existing) {
        Dialog<Warehouse> dialog = new Dialog<>();
        dialog.setTitle(existing == null ? "Nuevo Almacén" : "Editar Almacén");
        dialog.setHeaderText(existing == null ? 
            "Ingrese los datos del nuevo almacén" : 
            "Modifique los datos del almacén");

        ButtonType saveButtonType = new ButtonType("Guardar", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        // Crear formulario
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);

        TextField codeField = new TextField(existing != null ? existing.getCode() : "");
        codeField.setPromptText("Código único");
        TextField nameField = new TextField(existing != null ? existing.getName() : "");
        nameField.setPromptText("Nombre del almacén");
        TextArea descField = new TextArea(existing != null ? existing.getDescription() : "");
        descField.setPromptText("Descripción");
        descField.setPrefRowCount(3);
        TextField locationField = new TextField(existing != null ? existing.getLocation() : "");
        locationField.setPromptText("Ubicación física");
        
        ComboBox<ValuationMethod> valuationCombo = new ComboBox<>();
        valuationCombo.getItems().addAll(ValuationMethod.values());
        valuationCombo.setValue(existing != null ? existing.getValuationMethod() : ValuationMethod.FIFO);
        
        ComboBox<User> managerCombo = new ComboBox<>();
        managerCombo.getItems().addAll(userRepository.findAllActive());
        managerCombo.setPromptText("Responsable del almacén");
        if (existing != null && existing.getManager() != null) {
            managerCombo.setValue(existing.getManager());
        }

        grid.add(new Label("Código:"), 0, 0);
        grid.add(codeField, 1, 0);
        grid.add(new Label("Nombre:"), 0, 1);
        grid.add(nameField, 1, 1);
        grid.add(new Label("Descripción:"), 0, 2);
        grid.add(descField, 1, 2);
        grid.add(new Label("Ubicación:"), 0, 3);
        grid.add(locationField, 1, 3);
        grid.add(new Label("Método de Valoración:"), 0, 4);
        grid.add(valuationCombo, 1, 4);
        grid.add(new Label("Responsable:"), 0, 5);
        grid.add(managerCombo, 1, 5);

        dialog.getDialogPane().setContent(grid);

        // Validación
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                if (codeField.getText().trim().isEmpty()) {
                    notificationService.showError(getCurrentStage(), "El código es obligatorio");
                    return null;
                }
                if (nameField.getText().trim().isEmpty()) {
                    notificationService.showError(getCurrentStage(), "El nombre es obligatorio");
                    return null;
                }
                
                Warehouse warehouse = existing != null ? existing : new Warehouse();
                warehouse.setCode(codeField.getText().trim().toUpperCase());
                warehouse.setName(nameField.getText().trim());
                warehouse.setDescription(descField.getText().trim());
                warehouse.setLocation(locationField.getText().trim());
                warehouse.setValuationMethod(valuationCombo.getValue());
                warehouse.setManager(managerCombo.getValue());
                warehouse.setActive(existing != null ? existing.isActive() : true);
                
                return warehouse;
            }
            return null;
        });

        return dialog.showAndWait();
    }

    private Warehouse cloneWarehouse(Warehouse original) {
        Warehouse clone = new Warehouse();
        clone.setId(original.getId());
        clone.setCode(original.getCode());
        clone.setName(original.getName());
        clone.setDescription(original.getDescription());
        clone.setLocation(original.getLocation());
        clone.setManager(original.getManager());
        clone.setValuationMethod(original.getValuationMethod());
        clone.setActive(original.isActive());
        clone.setCreatedAt(original.getCreatedAt());
        clone.setUpdatedAt(original.getUpdatedAt());
        return clone;
    }

    private Stage getCurrentStage() {
        return (Stage) warehouseTable.getScene().getWindow();
    }
}
