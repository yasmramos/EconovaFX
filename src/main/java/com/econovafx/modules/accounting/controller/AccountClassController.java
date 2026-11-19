package com.econovafx.modules.accounting.controller;

import com.econovafx.modules.accounting.model.Account;
import com.econovafx.modules.accounting.model.AccountType;
import com.econovafx.modules.accounting.service.AccountService;
import com.econovafx.modules.core.ui.view.ViewFactory;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.ResourceBundle;

/**
 * Account Classification Controller - manages the chart of accounts hierarchy.
 *
 * <p>The tree is built from the real chart of accounts (grouped by
 * {@link AccountType}), and New/Edit/Delete act on the selected account through
 * {@link AccountService}. It used to render a hardcoded placeholder tree and
 * answer every action with a "Coming Soon" dialog.
 */
public class AccountClassController implements Initializable {

    private static final Logger logger = LoggerFactory.getLogger(AccountClassController.class);

    private final AccountService accountService;
    private ViewFactory viewFactory;

    @FXML
    private VBox contentArea;

    @FXML
    private TreeView<Object> classTree;

    @FXML
    private Button btnNuevo;

    @FXML
    private Button btnEditar;

    @FXML
    private Button btnEliminar;

    @FXML
    private TextField txtBuscar;

    public AccountClassController(AccountService accountService) {
        this.accountService = accountService;
    }

    /**
     * Initialize ViewFactory reference (two-phase initialization pattern)
     */
    public void initializeViewFactory(ViewFactory viewFactory) {
        this.viewFactory = viewFactory;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        logger.info("AccountClassController initialized");
        classTree.setShowRoot(true);
        classTree.setCellFactory(tree -> new TreeCell<>() {
            @Override
            protected void updateItem(Object item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else if (item instanceof AccountType type) {
                    setText(type.getDisplayName());
                } else if (item instanceof Account account) {
                    setText(account.getCode() + " - " + account.getName());
                } else {
                    setText(item.toString());
                }
            }
        });

        if (txtBuscar != null) {
            txtBuscar.textProperty().addListener((obs, oldVal, newVal) -> reload(newVal));
        }

        reload(null);
    }

    private void reload(String searchTerm) {
        List<Account> accounts = (searchTerm == null || searchTerm.isBlank())
                ? accountService.getAllAccounts()
                : accountService.searchAccounts(searchTerm.trim());
        classTree.setRoot(buildTree(accounts));
    }

    /**
     * Builds the classification tree from the chart of accounts, grouping roots
     * by {@link AccountType} and nesting children under their parent. Package
     * private so it can be exercised without a JavaFX toolkit.
     */
    TreeItem<Object> buildTree(List<Account> accounts) {
        TreeItem<Object> root = new TreeItem<>("Account Classification");
        root.setExpanded(true);

        Map<AccountType, List<Account>> byType = new LinkedHashMap<>();
        for (AccountType type : AccountType.values()) {
            byType.put(type, new ArrayList<>());
        }
        for (Account account : accounts) {
            if (account.getType() != null) {
                byType.get(account.getType()).add(account);
            }
        }

        for (Map.Entry<AccountType, List<Account>> entry : byType.entrySet()) {
            TreeItem<Object> typeNode = new TreeItem<>(entry.getKey());
            typeNode.setExpanded(true);
            for (Account account : entry.getValue()) {
                typeNode.getChildren().add(accountNode(account));
            }
            if (!typeNode.getChildren().isEmpty()) {
                root.getChildren().add(typeNode);
            }
        }
        return root;
    }

    private TreeItem<Object> accountNode(Account account) {
        TreeItem<Object> node = new TreeItem<>(account);
        for (Account child : account.getChildAccounts()) {
            node.getChildren().add(accountNode(child));
        }
        return node;
    }

    @FXML
    private void handleNuevo() {
        logger.debug("New classification clicked");
        if (viewFactory == null) {
            showAlert(Alert.AlertType.ERROR, "Error", "El formulario de cuentas no está disponible");
            return;
        }
        Optional<Account> result = viewFactory.showAccountFormDialog(null);
        result.ifPresent(account -> {
            try {
                accountService.createAccount(account);
                reload(currentSearch());
                showAlert(Alert.AlertType.INFORMATION, "Éxito", "Cuenta creada correctamente");
            } catch (IllegalArgumentException e) {
                showAlert(Alert.AlertType.ERROR, "Error", e.getMessage());
            }
        });
    }

    @FXML
    private void handleEditar() {
        Account selected = selectedAccount();
        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "Selección requerida",
                    "Por favor seleccione una cuenta para editar");
            return;
        }
        if (viewFactory == null) {
            showAlert(Alert.AlertType.ERROR, "Error", "El formulario de cuentas no está disponible");
            return;
        }
        Optional<Account> result = viewFactory.showAccountFormDialog(selected);
        result.ifPresent(account -> {
            try {
                accountService.updateAccount(account);
                reload(currentSearch());
                showAlert(Alert.AlertType.INFORMATION, "Éxito", "Cuenta actualizada correctamente");
            } catch (IllegalArgumentException e) {
                showAlert(Alert.AlertType.ERROR, "Error", e.getMessage());
            }
        });
    }

    @FXML
    private void handleEliminar() {
        Account selected = selectedAccount();
        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "Selección requerida",
                    "Por favor seleccione una cuenta para eliminar");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmar Eliminación");
        confirm.setHeaderText("¿Está seguro de eliminar esta cuenta?");
        confirm.setContentText("Cuenta: " + selected.getCode() + " - " + selected.getName());
        Optional<ButtonType> answer = confirm.showAndWait();
        if (answer.isPresent() && answer.get() == ButtonType.OK) {
            try {
                accountService.deleteAccount(selected.getId());
                reload(currentSearch());
                showAlert(Alert.AlertType.INFORMATION, "Éxito", "Cuenta eliminada correctamente");
            } catch (IllegalArgumentException e) {
                showAlert(Alert.AlertType.ERROR, "Error", e.getMessage());
            }
        }
    }

    private Account selectedAccount() {
        TreeItem<Object> selected = classTree.getSelectionModel().getSelectedItem();
        if (selected != null && selected.getValue() instanceof Account account) {
            return account;
        }
        return null;
    }

    private String currentSearch() {
        return txtBuscar != null ? txtBuscar.getText() : null;
    }

    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
