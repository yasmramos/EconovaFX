package com.econovafx.modules.billing.controller;

import com.econovafx.modules.billing.model.ThirdParty;
import com.econovafx.modules.billing.service.ThirdPartyService;
import com.econovafx.modules.core.exception.GlobalExceptionHandler;
import com.econovafx.modules.core.ui.util.ModernDialog;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Third Party form controller for creating and editing customers/suppliers
 */
public class ThirdPartyFormController implements Initializable {
    
    private static final Logger logger = LoggerFactory.getLogger(ThirdPartyFormController.class);
    private static final ExecutorService backgroundExecutor = Executors.newCachedThreadPool();
    
    private final ThirdPartyService thirdPartyService;
    
    // Reference to dialog handle for proper closure
    private ModernDialog.DialogHandle dialogHandle;
    
    /**
     * Sets the dialog handle for this controller.
     * Call this after loading the FXML to enable proper dialog closure.
     * @param handle The dialog handle
     */
    public void setDialogHandle(ModernDialog.DialogHandle handle) {
        this.dialogHandle = handle;
    }
    
    @FXML
    private Label titleLabel;
    
    @FXML
    private TextField nameField;
    
    @FXML
    private TextField identificationField;
    
    @FXML
    private ComboBox<ThirdParty.ThirdPartyType> typeComboBox;
    
    @FXML
    private TextField emailField;
    
    @FXML
    private TextField phoneField;
    
    @FXML
    private TextField addressField;
    
    @FXML
    private TextField cityField;
    
    @FXML
    private TextField countryField;
    
    @FXML
    private TextField taxIdField;
    
    @FXML
    private TextField creditLimitField;
    
    @FXML
    private TextField paymentDaysField;
    
    @FXML
    private CheckBox activeCheckBox;
    
    @FXML
    private TextArea notesArea;
    
    private ThirdParty editingThirdParty;
    private boolean saved = false;
    
    public ThirdPartyFormController(ThirdPartyService thirdPartyService) {
        this.thirdPartyService = thirdPartyService;
    }
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        logger.info("ThirdPartyFormController initialized");
        initializeComboBoxes();
    }
    
    private void initializeComboBoxes() {
        typeComboBox.setItems(FXCollections.observableArrayList(ThirdParty.ThirdPartyType.values()));
        typeComboBox.setConverter(new StringConverter<ThirdParty.ThirdPartyType>() {
            @Override
            public String toString(ThirdParty.ThirdPartyType type) {
                return type != null ? getTypeDisplayName(type) : "";
            }
            
            @Override
            public ThirdParty.ThirdPartyType fromString(String string) {
                for (ThirdParty.ThirdPartyType type : ThirdParty.ThirdPartyType.values()) {
                    if (getTypeDisplayName(type).equals(string)) {
                        return type;
                    }
                }
                return null;
            }
        });
        
        // Load parent third parties for reference (if needed in future)
        // For now, we just set the default value
        typeComboBox.setValue(ThirdParty.ThirdPartyType.CUSTOMER);
    }
    
    private String getTypeDisplayName(ThirdParty.ThirdPartyType type) {
        return switch (type) {
            case CUSTOMER -> "Customer";
            case SUPPLIER -> "Supplier";
            case BOTH -> "Both";
        };
    }
    
    public void setEditingThirdParty(ThirdParty thirdParty) {
        this.editingThirdParty = thirdParty;
        
        if (thirdParty != null) {
            titleLabel.setText("Edit Third Party");
            populateForm(thirdParty);
        } else {
            titleLabel.setText("New Third Party");
            clearForm();
        }
    }
    
    private void populateForm(ThirdParty thirdParty) {
        nameField.setText(thirdParty.getName());
        identificationField.setText(thirdParty.getIdentificationNumber());
        typeComboBox.setValue(thirdParty.getType());
        emailField.setText(thirdParty.getEmail());
        phoneField.setText(thirdParty.getPhone());
        addressField.setText(thirdParty.getAddress());
        cityField.setText(thirdParty.getCity());
        countryField.setText(thirdParty.getCountry());
        taxIdField.setText(thirdParty.getTaxId());
        creditLimitField.setText(thirdParty.getCreditLimit() != null ? thirdParty.getCreditLimit().toString() : "0");
        paymentDaysField.setText(thirdParty.getPaymentDays() != null ? thirdParty.getPaymentDays().toString() : "30");
        activeCheckBox.setSelected(thirdParty.getIsActive() != null && thirdParty.getIsActive());
        notesArea.setText(thirdParty.getNotes());
    }
    
    private void clearForm() {
        nameField.clear();
        identificationField.clear();
        typeComboBox.setValue(ThirdParty.ThirdPartyType.CUSTOMER);
        emailField.clear();
        phoneField.clear();
        addressField.clear();
        cityField.clear();
        countryField.setText("Peru");
        taxIdField.clear();
        creditLimitField.setText("0");
        paymentDaysField.setText("30");
        activeCheckBox.setSelected(true);
        notesArea.clear();
    }
    
    @FXML
    private void save() {
        if (!validateForm()) {
            return;
        }
        
        // Disable UI during save operation
        setUIEnabled(false);
        
        CompletableFuture.runAsync(() -> {
            try {
                if (editingThirdParty == null) {
                    editingThirdParty = new ThirdParty();
                }
                
                // Update entity with form data
                editingThirdParty.setName(nameField.getText().trim());
                editingThirdParty.setIdentificationNumber(identificationField.getText().trim());
                editingThirdParty.setType(typeComboBox.getValue());
                editingThirdParty.setEmail(emailField.getText().trim());
                editingThirdParty.setPhone(phoneField.getText().trim());
                editingThirdParty.setAddress(addressField.getText().trim());
                editingThirdParty.setCity(cityField.getText().trim());
                editingThirdParty.setCountry(countryField.getText().trim());
                editingThirdParty.setTaxId(taxIdField.getText().trim());
                
                try {
                    editingThirdParty.setCreditLimit(new BigDecimal(creditLimitField.getText().trim()));
                } catch (NumberFormatException e) {
                    editingThirdParty.setCreditLimit(BigDecimal.ZERO);
                }
                
                try {
                    editingThirdParty.setPaymentDays(Integer.parseInt(paymentDaysField.getText().trim()));
                } catch (NumberFormatException e) {
                    editingThirdParty.setPaymentDays(30);
                }
                
                editingThirdParty.setIsActive(activeCheckBox.isSelected());
                editingThirdParty.setNotes(notesArea.getText().trim());
                
                // Save to database in background thread
                thirdPartyService.createThirdParty(editingThirdParty);
                
                // Update UI on JavaFX thread
                javafx.application.Platform.runLater(() -> {
                    saved = true;
                    closeDialog();
                    logger.info("Third party saved successfully: {}", editingThirdParty.getName());
                });
                
            } catch (Exception ex) {
                javafx.application.Platform.runLater(() -> {
                    showError("Save Error", GlobalExceptionHandler.handleException(ex));
                    logger.error("Error saving third party", ex);
                    setUIEnabled(true);
                });
            }
        }, backgroundExecutor);
    }
    
    private void setUIEnabled(boolean enabled) {
        nameField.setDisable(!enabled);
        identificationField.setDisable(!enabled);
        typeComboBox.setDisable(!enabled);
        emailField.setDisable(!enabled);
        phoneField.setDisable(!enabled);
        addressField.setDisable(!enabled);
        cityField.setDisable(!enabled);
        countryField.setDisable(!enabled);
        taxIdField.setDisable(!enabled);
        creditLimitField.setDisable(!enabled);
        paymentDaysField.setDisable(!enabled);
        activeCheckBox.setDisable(!enabled);
        notesArea.setDisable(!enabled);
    }
    
    @FXML
    private void cancel() {
        saved = false;
        editingThirdParty = null;
        closeDialog();
        logger.info("Third party form cancelled");
    }
    
    private boolean validateForm() {
        if (nameField.getText().trim().isEmpty()) {
            showError("Validation Error", "Name is required");
            return false;
        }
        
        if (identificationField.getText().trim().isEmpty()) {
            showError("Validation Error", "Identification number is required");
            return false;
        }
        
        if (emailField.getText().trim().isEmpty()) {
            showError("Validation Error", "Email is required");
            return false;
        }
        
        // Validate email format
        if (!emailField.getText().trim().matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            showError("Validation Error", "Invalid email format");
            return false;
        }
        
        return true;
    }
    
    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    private void closeDialog() {
        if (dialogHandle != null) {
            dialogHandle.close();
        } else {
            // Fallback for backward compatibility
            Stage stage = (Stage) nameField.getScene().getWindow();
            stage.close();
        }
    }
    
    public ThirdParty getResult() {
        return saved ? editingThirdParty : null;
    }
}
