package com.econovafx.modules.core.ui.view;

import com.econovafx.modules.accounting.controller.AccountFormController;
import com.econovafx.modules.accounting.controller.AccountingClosuresController;
import com.econovafx.modules.accounting.controller.AccountingPeriodsController;
import com.econovafx.modules.accounting.controller.AccountsController;
import com.econovafx.modules.accounting.controller.ComprobantesController;
import com.econovafx.modules.accounting.controller.TransactionEntryController;
import com.econovafx.modules.accounting.controller.TransactionsController;
import com.econovafx.modules.accounting.service.AccountService;
import com.econovafx.modules.accounting.service.AccountingPeriodService;
import com.econovafx.modules.accounting.service.TransactionService;
import com.econovafx.modules.billing.controller.ThirdPartiesController;
import com.econovafx.modules.billing.controller.ThirdPartyFormController;
import com.econovafx.modules.billing.service.ThirdPartyService;
import com.econovafx.modules.core.service.ExportService;
import com.econovafx.modules.core.ui.controller.DashboardController;
import com.econovafx.modules.core.ui.controller.ExchangeRatesController;
import com.econovafx.modules.core.ui.controller.SystemSettingsController;
import com.econovafx.modules.core.ui.controller.WebViewController;
import com.econovafx.modules.inventory.controller.InventoryController;
import com.econovafx.modules.inventory.service.InventoryService;
import com.econovafx.modules.reporting.controller.FinancialReportingViewController;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

/**
 * Guards the dashboard's "view all transactions" navigation.
 *
 * <p>{@code ViewFactory.showTransactions()} used to be a dead end: it wrapped a
 * log statement in a {@code Consumer<Runnable>} that {@code AppContext} always
 * passed as {@code null}, so the dashboard button never changed the page. The
 * factory now holds a plain navigator {@code Runnable} that {@code App} wires to
 * the shell controller.
 */
public class ViewFactoryNavigationTest {

    private ViewFactory newFactory() {
        return new ViewFactory(
                mock(DashboardController.class),
                mock(AccountsController.class),
                mock(TransactionsController.class),
                mock(ThirdPartiesController.class),
                mock(AccountingPeriodsController.class),
                mock(AccountingClosuresController.class),
                mock(ExchangeRatesController.class),
                mock(AccountFormController.class),
                mock(ThirdPartyFormController.class),
                mock(TransactionEntryController.class),
                mock(ComprobantesController.class),
                mock(SystemSettingsController.class),
                mock(InventoryController.class),
                mock(WebViewController.class),
                mock(FinancialReportingViewController.class),
                mock(AccountService.class),
                mock(ThirdPartyService.class),
                mock(TransactionService.class),
                mock(ExportService.class),
                mock(AccountingPeriodService.class),
                mock(InventoryService.class));
    }

    @Test
    void showTransactionsRunsTheRegisteredNavigator() {
        ViewFactory factory = newFactory();
        AtomicInteger calls = new AtomicInteger();

        factory.setTransactionsNavigator(calls::incrementAndGet);
        factory.showTransactions();
        factory.showTransactions();

        assertEquals(2, calls.get(),
                "each call to showTransactions must run the registered navigator");
    }

    @Test
    void showTransactionsIsANoOpWithoutANavigator() {
        ViewFactory factory = newFactory();

        // No navigator wired yet: must not throw, just log a warning.
        factory.showTransactions();
    }
}
