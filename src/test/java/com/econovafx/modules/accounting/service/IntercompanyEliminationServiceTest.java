package com.econovafx.modules.accounting.service;

import com.econovafx.modules.accounting.model.FinancialStatementRow;
import com.econovafx.modules.accounting.model.IntercompanyElimination;
import com.econovafx.modules.reporting.service.consolidation.ConsolidatedRow;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for IntercompanyEliminationService.
 *
 * The service is built with null repositories because only the pure
 * calculation methods are exercised here; the repository-backed methods are
 * covered elsewhere.
 */
class IntercompanyEliminationServiceTest {

    private final IntercompanyEliminationService service =
            new IntercompanyEliminationService(null, null, null, null);

    // --- validateEliminationsBalanced ---

    @Test
    void validateBalancedTreatsEmptyBatchAsBalanced() {
        assertTrue(service.validateEliminationsBalanced(null));
        assertTrue(service.validateEliminationsBalanced(List.of()));
    }

    @Test
    void validateBalancedRejectsNonPositiveAmount() {
        assertFalse(service.validateEliminationsBalanced(
                List.of(elimination(IntercompanyElimination.EliminationType.RECEIVABLE_PAYABLE,
                        "1.1.3.01", "2.1.3.01", BigDecimal.ZERO))));

        assertFalse(service.validateEliminationsBalanced(
                List.of(elimination(IntercompanyElimination.EliminationType.RECEIVABLE_PAYABLE,
                        "1.1.3.01", "2.1.3.01", BigDecimal.valueOf(-100)))));
    }

    @Test
    void validateBalancedRejectsBlankAccountCodes() {
        assertFalse(service.validateEliminationsBalanced(
                List.of(elimination(IntercompanyElimination.EliminationType.RECEIVABLE_PAYABLE,
                        "  ", "2.1.3.01", BigDecimal.TEN))));

        assertFalse(service.validateEliminationsBalanced(
                List.of(elimination(IntercompanyElimination.EliminationType.RECEIVABLE_PAYABLE,
                        "1.1.3.01", null, BigDecimal.TEN))));
    }

    @Test
    void validateBalancedAcceptsPostableBatch() {
        assertTrue(service.validateEliminationsBalanced(List.of(
                elimination(IntercompanyElimination.EliminationType.RECEIVABLE_PAYABLE,
                        "1.1.3.01", "2.1.3.01", BigDecimal.valueOf(500)),
                elimination(IntercompanyElimination.EliminationType.REVENUE_EXPENSE,
                        "4.1.01", "5.1.01", BigDecimal.valueOf(1200)))));
    }

    // --- applyEliminations ---

    @Test
    void applyEliminationsReturnsSameListWhenEmpty() {
        List<ConsolidatedRow> rows = List.of(row(1, "Cash", BigDecimal.valueOf(1000)));
        assertSame(rows, service.applyEliminations(rows, Map.of(), List.of()));
    }

    @Test
    void applyEliminationsReducesReceivableAndPayableRows() {
        List<ConsolidatedRow> rows = List.of(
                row(1, "Intercompany Receivable", BigDecimal.valueOf(1000)),
                row(2, "Intercompany Payable", BigDecimal.valueOf(800)),
                row(3, "Cash", BigDecimal.valueOf(5000)));

        List<IntercompanyElimination> eliminations = List.of(
                elimination(IntercompanyElimination.EliminationType.RECEIVABLE_PAYABLE,
                        "1.1.3.01", "2.1.3.01", BigDecimal.valueOf(500)));

        List<ConsolidatedRow> adjusted = service.applyEliminations(rows, Map.of(), eliminations);

        assertEquals(0, adjusted.get(0).getConsolidatedValue().compareTo(BigDecimal.valueOf(500)));
        assertEquals(0, adjusted.get(1).getConsolidatedValue().compareTo(BigDecimal.valueOf(300)));
        assertEquals(0, adjusted.get(2).getConsolidatedValue().compareTo(BigDecimal.valueOf(5000)));
    }

    @Test
    void applyEliminationsReducesRevenueAndExpenseRows() {
        List<ConsolidatedRow> rows = List.of(
                row(1, "Intercompany Revenue", BigDecimal.valueOf(2000)),
                row(2, "Intercompany Expense", BigDecimal.valueOf(1500)));

        List<IntercompanyElimination> eliminations = List.of(
                elimination(IntercompanyElimination.EliminationType.REVENUE_EXPENSE,
                        "4.1.01", "5.1.01", BigDecimal.valueOf(1200)));

        List<ConsolidatedRow> adjusted = service.applyEliminations(rows, Map.of(), eliminations);

        assertEquals(0, adjusted.get(0).getConsolidatedValue().compareTo(BigDecimal.valueOf(800)));
        assertEquals(0, adjusted.get(1).getConsolidatedValue().compareTo(BigDecimal.valueOf(300)));
    }

    @Test
    void applyEliminationsMovesUnrealizedProfitFromInventoryToCogs() {
        List<ConsolidatedRow> rows = List.of(
                row(1, "Inventory", BigDecimal.valueOf(3000)),
                row(2, "Cost of Goods Sold", BigDecimal.valueOf(1000)));

        List<IntercompanyElimination> eliminations = List.of(
                elimination(IntercompanyElimination.EliminationType.UNREALIZED_PROFIT,
                        "1.3.01", "5.2.01", BigDecimal.valueOf(200)));

        List<ConsolidatedRow> adjusted = service.applyEliminations(rows, Map.of(), eliminations);

        assertEquals(0, adjusted.get(0).getConsolidatedValue().compareTo(BigDecimal.valueOf(2800)));
        assertEquals(0, adjusted.get(1).getConsolidatedValue().compareTo(BigDecimal.valueOf(1200)));
    }

    // --- helpers ---

    private static IntercompanyElimination elimination(IntercompanyElimination.EliminationType type,
                                                       String accountCode,
                                                       String counterAccountCode,
                                                       BigDecimal amount) {
        return new IntercompanyElimination(type, "test", 1L, 2L,
                accountCode, counterAccountCode, amount, LocalDate.of(2024, 1, 31));
    }

    private static ConsolidatedRow row(int number, String label, BigDecimal value) {
        return new ConsolidatedRow(number, label, FinancialStatementRow.RowType.DATA, value,
                0, false, false, Map.of());
    }
}
