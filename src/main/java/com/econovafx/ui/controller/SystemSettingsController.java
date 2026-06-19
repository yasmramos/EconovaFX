package com.econovafx.ui.controller;

import com.econovafx.service.NotificationService;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import java.io.File;
import java.util.ResourceBundle;

/**
 * Controlador para la Configuración General del Sistema.
 * Diseño inspirado en preferencesFx con navegación lateral.
 */
public class SystemSettingsController {

    @FXML
    private VBox sidebarContainer;

    @FXML
    private StackPane contentArea;

    @FXML
    private TextField companyNameField;

    @FXML
    private TextField taxIdField;

    @FXML
    private TextArea addressField;

    @FXML
    private TextField phoneField;

    @FXML
    private TextField emailField;

    @FXML
    private ImageView logoPreview;

    @FXML
    private ComboBox<String> fiscalYearStartCombo;

    @FXML
    private ComboBox<String> currencyCombo;

    @FXML
    private ComboBox<String> inventoryMethodCombo;

    @FXML
    private CheckBox autoBackupCheck;

    @FXML
    private TextField backupPathField;

    @FXML
    private Spinner<Integer> stockAlertSpinner;

    @FXML
    private ComboBox<String> themeCombo;

    @FXML
    private ComboBox<String> languageCombo;

    private ResourceBundle resources;

    @FXML
    public void initialize() {
        setupSidebar();
        loadCurrentSettings();
        setupListeners();
    }

    private void setupSidebar() {
        sidebarContainer.getChildren().clear();
        
        String[] categories = {
            "Empresa",
            "Contabilidad",
            "Inventario",
            "Seguridad",
            "Copias de Seguridad",
            "Interfaz"
        };

        for (String category : categories) {
            Button btn = new Button(category);
            btn.getStyleClass().add("sidebar-button");
            btn.setMaxWidth(Double.MAX_VALUE);
            btn.setOnAction(e -> switchContent(category));
            sidebarContainer.getChildren().add(btn);
        }
        
        // Seleccionar primero por defecto
        if (!sidebarContainer.getChildren().isEmpty()) {
            switchContent("Empresa");
        }
    }

    private void switchContent(String category) {
        contentArea.getChildren().clear();
        VBox panel = new VBox(15);
        panel.getStyleClass().add("settings-panel");
        panel.setPadding(new javafx.geometry.Insets(20));

        Label title = new Label("Configuración de " + category);
        title.getStyleClass().add("settings-title");

        switch (category) {
            case "Empresa":
                panel.getChildren().addAll(
                    createFormLabel("Nombre de la Empresa"), companyNameField,
                    createFormLabel("NIF / CIF"), taxIdField,
                    createFormLabel("Dirección"), addressField,
                    createFormLabel("Teléfono"), phoneField,
                    createFormLabel("Email"), emailField,
                    createButton("Cambiar Logo", this::changeLogo),
                    logoPreview
                );
                break;
            case "Contabilidad":
                panel.getChildren().addAll(
                    createFormLabel("Inicio del Ejercicio Fiscal"), fiscalYearStartCombo,
                    createFormLabel("Moneda Base"), currencyCombo
                );
                break;
            case "Inventario":
                panel.getChildren().addAll(
                    createFormLabel("Método de Valoración por Defecto"), inventoryMethodCombo,
                    createFormLabel("Alerta de Stock Mínimo"), stockAlertSpinner
                );
                break;
            case "Seguridad":
                panel.getChildren().addAll(
                    createFormLabel("Política de Contraseñas"), new Label("Complejidad media requerida"),
                    createFormLabel("Tiempo de Sesión"), new Label("30 minutos")
                );
                break;
            case "Copias de Seguridad":
                panel.getChildren().addAll(
                    autoBackupCheck,
                    createFormLabel("Ruta de Copias"), backupPathField,
                    createButton("Seleccionar Carpeta", this::selectBackupPath)
                );
                break;
            case "Interfaz":
                panel.getChildren().addAll(
                    createFormLabel("Tema"), themeCombo,
                    createFormLabel("Idioma"), languageCombo
                );
                break;
        }

        Button saveBtn = new Button("Guardar Cambios");
        saveBtn.getStyleClass().add("primary-button");
        saveBtn.setOnAction(e -> saveSettings(category));
        
        panel.getChildren().add(saveBtn);
        contentArea.getChildren().add(panel);
    }

    private Label createFormLabel(String text) {
        Label label = new Label(text);
        label.getStyleClass().add("form-label");
        return label;
    }

    private Button createButton(String text, javafx.event.EventHandler<javafx.scene.input.MouseEvent> action) {
        Button btn = new Button(text);
        btn.getStyleClass().add("secondary-button");
        btn.setOnMouseClicked(action);
        return btn;
    }

    private void loadCurrentSettings() {
        // Cargar valores simulados (en producción vendrían de un servicio de configuración)
        companyNameField.setText("EconoNova Demo S.A.");
        taxIdField.setText("B05123456");
        addressField.setText("Calle Principal #123, La Habana");
        phoneField.setText("+53 7 1234567");
        emailField.setText("info@econovafx.cu");
        
        fiscalYearStartCombo.getItems().addAll("1 de Enero", "1 de Abril", "1 de Julio");
        fiscalYearStartCombo.setValue("1 de Enero");
        
        currencyCombo.getItems().addAll("CUP", "USD", "EUR");
        currencyCombo.setValue("CUP");
        
        inventoryMethodCombo.getItems().addAll("PEPS (FIFO)", "Promedio Ponderado", "Identificación Específica");
        inventoryMethodCombo.setValue("Promedio Ponderado");
        
        stockAlertSpinner.getValueFactory().setValue(10);
        
        autoBackupCheck.setSelected(true);
        backupPathField.setText("./backups");
        
        themeCombo.getItems().addAll("Claro", "Oscuro", "Sistema");
        themeCombo.setValue("Sistema");
        
        languageCombo.getItems().addAll("Español", "English");
        languageCombo.setValue("Español");
    }

    private void setupListeners() {
        // Listeners adicionales si son necesarios
    }

    private void changeLogo(javafx.scene.input.MouseEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Imágenes", "*.png", "*.jpg", "*.jpeg"));
        Stage stage = (Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();
        File file = fileChooser.showOpenDialog(stage);
        if (file != null) {
            // Aquí se cargaría la imagen real
            notificationService.showSuccess("Logo actualizado correctamente");
        }
    }

    private void selectBackupPath(javafx.scene.input.MouseEvent event) {
        DirectoryChooser dirChooser = new DirectoryChooser();
        Stage stage = (Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();
        File dir = dirChooser.showDialog(stage);
        if (dir != null) {
            backupPathField.setText(dir.getAbsolutePath());
        }
    }

    private void saveSettings(String category) {
        // Validaciones y guardado
        // En una implementación real, esto llamaría a un SettingsService
        notificationService.showSuccess("Configuración de " + category + " guardada exitosamente");
        
        // Simular recarga de contexto si es necesario
        if (category.equals("Interfaz")) {
            notificationService.showInfo("Reinicie la aplicación para aplicar los cambios de tema/idioma");
        }
    }
}
