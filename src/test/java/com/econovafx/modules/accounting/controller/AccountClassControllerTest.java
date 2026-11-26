package com.econovafx.modules.accounting.controller;

import com.econovafx.modules.accounting.model.Account;
import com.econovafx.modules.accounting.model.AccountType;
import com.econovafx.modules.accounting.service.AccountService;
import javafx.scene.control.TreeItem;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

/**
 * The classification tree must reflect the real chart of accounts. It used to be
 * a hardcoded placeholder ("Assets", "Current Assets", ...) that ignored the
 * database entirely.
 */
class AccountClassControllerTest {

    private final AccountClassController controller =
            new AccountClassController(mock(AccountService.class));

    @Test
    void groupsRootAccountsUnderTheirType() {
        Account cash = new Account("1000", "Cash", AccountType.ASSET);
        Account sales = new Account("4000", "Sales", AccountType.REVENUE);

        TreeItem<Object> root = controller.buildTree(List.of(cash, sales));

        // Only types that actually have accounts are shown.
        assertEquals(2, root.getChildren().size());

        TreeItem<Object> assets = root.getChildren().get(0);
        assertEquals(AccountType.ASSET, assets.getValue());
        assertEquals(1, assets.getChildren().size());
        assertSame(cash, assets.getChildren().get(0).getValue());

        TreeItem<Object> revenue = root.getChildren().get(1);
        assertEquals(AccountType.REVENUE, revenue.getValue());
        assertSame(sales, revenue.getChildren().get(0).getValue());
    }

    @Test
    void nestsChildAccountsUnderTheirParent() {
        Account parent = new Account("1000", "Cash", AccountType.ASSET);
        Account child = new Account("1001", "Petty Cash", AccountType.ASSET);
        child.setParentAccount(parent);
        parent.getChildAccounts().add(child);

        TreeItem<Object> root = controller.buildTree(List.of(parent));

        TreeItem<Object> parentNode = root.getChildren().get(0).getChildren().get(0);
        assertSame(parent, parentNode.getValue());
        assertEquals(1, parentNode.getChildren().size());
        assertSame(child, parentNode.getChildren().get(0).getValue());
    }

    @Test
    void emptyChartProducesEmptyTree() {
        TreeItem<Object> root = controller.buildTree(List.of());
        assertTrue(root.getChildren().isEmpty());
    }
}
