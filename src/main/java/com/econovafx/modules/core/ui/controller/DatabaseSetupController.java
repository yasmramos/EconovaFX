package com.econovafx.modules.core.ui.controller;

import com.econovafx.modules.core.config.ConfigFileUtil;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Properties;
import java.nio.file.Path;
import javafx.scene.input.MouseEvent;

/**
 * Controller for the database setup assistant shown at first run.
 */
public class DatabaseSetupController {

    private static final Logger logger = LoggerFactory.getLogger(DatabaseSetupController.class);

    @FXML
    private VBox root;

    @FXML
    private ComboBox<String> dbTypeCombo;

    // PostgreSQL fields
    @FXML
    private TextField pgHostField;
    @FXML
    private TextField pgPortField;
    @FXML
    private TextField pgDatabaseField;
    @FXML
    private TextField pgUserField;
    @FXML
    private PasswordField pgPasswordField;
    @FXML
    private ComboBox<String> pgSslModeCombo;

    @FXML
    private Button testButton;
    @FXML
    private Button saveButton;
    @FXML
    private Button cancelButton;

    @FXML
    private Label statusLabel;

    private Stage dialogStage;
    private boolean saved = false;
    private Path savedConfigPath;
    private double dragOffsetX;
    private double dragOffsetY;

    @FXML
    public void initialize() {
        dbTypeCombo.getItems().addAll("h2", "postgresql");
        dbTypeCombo.setValue("h2");

        pgPortField.setText("5432");
        pgDatabaseField.setText("econovafx_master");
        pgUserField.setText("postgres");
        pgSslModeCombo.getItems().addAll("disable", "allow", "prefer", "require", "verify-ca", "verify-full");
        pgSslModeCombo.setValue("prefer");

        // Toggle visibility based on DB type
        dbTypeCombo.setOnAction(e -> updateFieldsVisibility());
        updateFieldsVisibility();

        testButton.setOnAction(e -> testConnection());
        saveButton.setOnAction(e -> saveAndClose());
        cancelButton.setOnAction(e -> { saved = false; dialogStage.close(); });

        root.addEventHandler(MouseEvent.MOUSE_PRESSED, event -> {
            if (dialogStage != null) {
                dragOffsetX = event.getScreenX() - dialogStage.getX();
                dragOffsetY = event.getScreenY() - dialogStage.getY();
            }
        });
        root.addEventHandler(MouseEvent.MOUSE_DRAGGED, event -> {
            if (dialogStage != null) {
                dialogStage.setX(event.getScreenX() - dragOffsetX);
                dialogStage.setY(event.getScreenY() - dragOffsetY);
            }
        });
    }

    private void updateFieldsVisibility() {
        boolean isPg = "postgresql".equalsIgnoreCase(dbTypeCombo.getValue());
        pgHostField.setDisable(!isPg);
        pgPortField.setDisable(!isPg);
        pgDatabaseField.setDisable(!isPg);
        pgUserField.setDisable(!isPg);
        pgPasswordField.setDisable(!isPg);
        pgSslModeCombo.setDisable(!isPg);
    }

    public void setDialogStage(Stage stage) {
        this.dialogStage = stage;
    }

    public boolean isSaved() {
        return saved;
    }

    public Path getSavedConfigPath() {
        return savedConfigPath;
    }

    private void testConnection() {
        statusLabel.setText("Probando conexión...");
        try {
            String type = dbTypeCombo.getValue();
            if ("postgresql".equalsIgnoreCase(type)) {
                String host = pgHostField.getText();
                String port = pgPortField.getText();
                String db = pgDatabaseField.getText();
                String user = pgUserField.getText();
                String pass = pgPasswordField.getText();
                String ssl = pgSslModeCombo.getValue();
                String url = String.format("jdbc:postgresql://%s:%s/%s?sslmode=%s", host, port, db, ssl);
                // Try connect
                try (Connection c = DriverManager.getConnection(url, user, pass)) {
                    statusLabel.setText("Conexión exitosa a PostgreSQL");
                }
            } else {
                // H2 - try default embedded url
                String url = "jdbc:h2:./db/master;DB_CLOSE_DELAY=-1;AUTO_SERVER=TRUE";
                try (Connection c = DriverManager.getConnection(url, "sa", "")) {
                    statusLabel.setText("Conexión exitosa a H2");
                }
            }
        } catch (Exception ex) {
            logger.warn("Connection test failed", ex);
            statusLabel.setText("Error: " + ex.getMessage());
        }
    }

    private void saveAndClose() {
        statusLabel.setText("Validando y guardando configuración...");
        try {
            String type = dbTypeCombo.getValue();
            if (type == null) {
                throw new IllegalArgumentException("Seleccione un tipo de base de datos.");
            }
            Properties props = new Properties();
            if ("postgresql".equalsIgnoreCase(type)) {
                String host = required(pgHostField.getText(), "host");
                String port = required(pgPortField.getText(), "puerto");
                String db = required(pgDatabaseField.getText(), "base de datos");
                String user = required(pgUserField.getText(), "usuario");
                String pass = pgPasswordField.getText();
                String ssl = pgSslModeCombo.getValue();
                try {
                    int portNumber = Integer.parseInt(port);
                    if (portNumber < 1 || portNumber > 65535) {
                        throw new NumberFormatException();
                    }
                } catch (NumberFormatException ex) {
                    throw new IllegalArgumentException("El puerto debe ser un número entre 1 y 65535.");
                }

                String url = String.format("jdbc:postgresql://%s:%s/%s?sslmode=%s", host, port, db, ssl);

                // Test connection first
                try (Connection c = DriverManager.getConnection(url, user, pass)) {
                    // OK
                }

                props.setProperty("database.type", "postgresql");
                props.setProperty("database.url", url);
                props.setProperty("database.username", user);
                props.setProperty("database.password", pass);

                props.setProperty("database.postgres.host", host);
                props.setProperty("database.postgres.port", port);
                props.setProperty("database.postgres.database", db);
                props.setProperty("database.postgres.username", user);
                props.setProperty("database.postgres.password", pass);
                props.setProperty("database.postgres.sslmode", ssl);

                // Ebean master datasource
                props.setProperty("ebean.datasource.master.driver", "org.postgresql.Driver");
                props.setProperty("ebean.datasource.master.url", url);
                props.setProperty("ebean.datasource.master.username", user);
                props.setProperty("ebean.datasource.master.password", pass);

            } else {
                // H2 defaults
                String url = "jdbc:h2:./db/master;DB_CLOSE_DELAY=-1;AUTO_SERVER=TRUE";
                props.setProperty("database.type", "h2");
                props.setProperty("database.url", url);
                props.setProperty("database.username", "sa");
                props.setProperty("database.password", "");

                props.setProperty("ebean.datasource.master.driver", "org.h2.Driver");
                props.setProperty("ebean.datasource.master.url", url);
                props.setProperty("ebean.datasource.master.username", "sa");
                props.setProperty("ebean.datasource.master.password", "");
            }

            Path saved = ConfigFileUtil.saveProperties(props);
            savedConfigPath = saved;
            this.saved = true;
            statusLabel.setText("Configuración guardada exitosamente");
            // Close after brief delay to show success
            dialogStage.close();
        } catch (Exception ex) {
            logger.error("Failed to save configuration", ex);
            statusLabel.setText("Error al guardar: " + ex.getMessage());
        }
    }

    private String required(String value, String field) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("El campo " + field + " es obligatorio.");
        }
        return value.trim();
    }
}
