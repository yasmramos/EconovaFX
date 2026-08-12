package com.econovafx.modules.payables.service;

import com.econovafx.modules.payables.model.SupplierInvoice;
import com.econovafx.modules.payables.repository.SupplierInvoiceRepository;
import com.econovafx.modules.billing.model.ThirdParty;
import com.econovafx.modules.billing.service.ThirdPartyService;
import com.econovafx.modules.core.config.UserContext;
import com.econovafx.modules.core.exception.EntityNotFoundException;
import com.econovafx.modules.core.exception.ValidationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PayablesServiceTest {

    @Mock
    private SupplierInvoiceRepository invoiceRepository;

    @Mock
    private ThirdPartyService thirdPartyService;

    @Mock
    private UserContext userContext;

    private PayablesService payablesService;

    @BeforeEach
    void setUp() {
        payablesService = new PayablesService(invoiceRepository, null, thirdPartyService, null, userContext);
    }

    @Test
    void testGetInvoiceById_WhenExists_ReturnsOptional() {
        Long id = 1L;
        SupplierInvoice invoice = createInvoice(id, "SUP-INV-000001", 1000.00);

        when(invoiceRepository.findById(id)).thenReturn(Optional.of(invoice));

        Optional<SupplierInvoice> result = payablesService.getInvoiceById(id);

        assertTrue(result.isPresent());
        assertEquals("SUP-INV-000001", result.get().getInvoiceNumber());
        verify(invoiceRepository).findById(id);
    }

    @Test
    void testGetInvoiceById_WhenNotExists_ReturnsEmpty() {
        Long id = 999L;
        when(invoiceRepository.findById(id)).thenReturn(Optional.empty());

        Optional<SupplierInvoice> result = payablesService.getInvoiceById(id);

        assertFalse(result.isPresent());
        verify(invoiceRepository).findById(id);
    }

    @Test
    void testGetInvoiceByNumber_WhenExists_ReturnsOptional() {
        String invoiceNumber = "SUP-INV-000001";
        SupplierInvoice invoice = createInvoice(1L, invoiceNumber, 1000.00);

        when(invoiceRepository.findByInvoiceNumber(invoiceNumber)).thenReturn(Optional.of(invoice));

        Optional<SupplierInvoice> result = payablesService.getInvoiceByNumber(invoiceNumber);

        assertTrue(result.isPresent());
        assertEquals(invoiceNumber, result.get().getInvoiceNumber());
        verify(invoiceRepository).findByInvoiceNumber(invoiceNumber);
    }

    @Test
    void testGetAllInvoices_ReturnsList() {
        List<SupplierInvoice> invoices = Arrays.asList(
            createInvoice(1L, "SUP-INV-000001", 1000.00),
            createInvoice(2L, "SUP-INV-000002", 2000.00)
        );

        when(invoiceRepository.findAll()).thenReturn(invoices);

        List<SupplierInvoice> result = payablesService.getAllInvoices();

        assertEquals(2, result.size());
        verify(invoiceRepository).findAll();
    }

    @Test
    void testGetInvoicesBySupplier_WhenExists_ReturnsList() {
        Long supplierId = 1L;
        ThirdParty supplier = createThirdParty(supplierId, "Test Supplier", ThirdParty.ThirdPartyType.SUPPLIER);
        List<SupplierInvoice> invoices = Arrays.asList(
            createInvoiceForSupplier(1L, supplier, 1000.00),
            createInvoiceForSupplier(2L, supplier, 2000.00)
        );

        when(thirdPartyService.getThirdPartyById(supplierId)).thenReturn(Optional.of(supplier));
        when(invoiceRepository.findBySupplier(supplier)).thenReturn(invoices);

        List<SupplierInvoice> result = payablesService.getInvoicesBySupplier(supplierId);

        assertEquals(2, result.size());
        verify(thirdPartyService).getThirdPartyById(supplierId);
        verify(invoiceRepository).findBySupplier(supplier);
    }

    @Test
    void testGetInvoicesBySupplier_WhenNotExists_ThrowsException() {
        Long supplierId = 999L;
        when(thirdPartyService.getThirdPartyById(supplierId)).thenReturn(Optional.empty());

        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> {
            payablesService.getInvoicesBySupplier(supplierId);
        });

        verify(invoiceRepository, never()).findBySupplier(any());
    }

    @Test
    void testGetInvoicesByStatus_ReturnsFilteredList() {
        SupplierInvoice.InvoiceStatus status = SupplierInvoice.InvoiceStatus.PENDING;
        List<SupplierInvoice> invoices = Arrays.asList(
            createInvoiceWithStatus(1L, "SUP-INV-000001", 1000.00, status),
            createInvoiceWithStatus(2L, "SUP-INV-000002", 2000.00, status)
        );

        when(invoiceRepository.findByStatus(status)).thenReturn(invoices);

        List<SupplierInvoice> result = payablesService.getInvoicesByStatus(status);

        assertEquals(2, result.size());
        verify(invoiceRepository).findByStatus(status);
    }

    @Test
    void testGetOverdueInvoices_ReturnsList() {
        LocalDate beforeDate = LocalDate.now().minusDays(30);
        List<SupplierInvoice> overdueInvoices = Arrays.asList(
            createInvoiceWithDueDate(1L, "SUP-INV-000001", 1000.00, LocalDate.now().minusDays(10)),
            createInvoiceWithDueDate(2L, "SUP-INV-000002", 2000.00, LocalDate.now().minusDays(5))
        );

        when(invoiceRepository.findOverdueInvoices(beforeDate)).thenReturn(overdueInvoices);

        List<SupplierInvoice> result = payablesService.getOverdueInvoices(beforeDate);

        assertEquals(2, result.size());
        verify(invoiceRepository).findOverdueInvoices(beforeDate);
    }

    @Test
    void testCreateInvoice_WithValidData_Success() {
        SupplierInvoice invoice = createInvoice(null, null, 1000.00);
        SupplierInvoice savedInvoice = createInvoice(1L, "SUP-INV-000001", 1000.00);
        ThirdParty supplier = createThirdParty(1L, "Test Supplier", ThirdParty.ThirdPartyType.SUPPLIER);

        when(thirdPartyService.getThirdPartyById(1L)).thenReturn(Optional.of(supplier));
        when(invoiceRepository.save(any(SupplierInvoice.class))).thenReturn(savedInvoice);

        SupplierInvoice result = payablesService.createInvoice(invoice);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        verify(invoiceRepository).save(any(SupplierInvoice.class));
    }

    @Test
    void testCreateInvoice_WithNullSupplier_ThrowsException() {
        SupplierInvoice invoice = createInvoice(null, null, 1000.00);
        invoice.setSupplier(null);

        ValidationException exception = assertThrows(ValidationException.class, () -> {
            payablesService.createInvoice(invoice);
        });

        verify(invoiceRepository, never()).save(any());
    }

    @Test
    void testCreateInvoice_WithInvalidSupplierType_ThrowsException() {
        SupplierInvoice invoice = createInvoice(null, null, 1000.00);
        ThirdParty customer = createThirdParty(1L, "Test Customer", ThirdParty.ThirdPartyType.CUSTOMER);

        when(thirdPartyService.getThirdPartyById(1L)).thenReturn(Optional.of(customer));

        ValidationException exception = assertThrows(ValidationException.class, () -> {
            payablesService.createInvoice(invoice);
        });

        assertTrue(exception.getMessage().contains("CUSTOMER"));
        verify(invoiceRepository, never()).save(any());
    }

    @Test
    void testCreateInvoice_WithNegativeAmount_ThrowsException() {
        SupplierInvoice invoice = createInvoice(null, null, -100.00);

        ValidationException exception = assertThrows(ValidationException.class, () -> {
            payablesService.createInvoice(invoice);
        });

        verify(invoiceRepository, never()).save(any());
    }

    @Test
    void testCreateInvoice_WithDueDateBeforeInvoiceDate_ThrowsException() {
        SupplierInvoice invoice = createInvoice(null, null, 1000.00);
        invoice.setInvoiceDate(LocalDate.now());
        invoice.setDueDate(LocalDate.now().minusDays(1));

        ValidationException exception = assertThrows(ValidationException.class, () -> {
            payablesService.createInvoice(invoice);
        });

        assertTrue(exception.getMessage().contains("fecha de vencimiento"));
        verify(invoiceRepository, never()).save(any());
    }

    @Test
    void testUpdateInvoice_WithValidData_Success() {
        SupplierInvoice invoice = createInvoice(1L, "SUP-INV-000001", 1000.00);
        invoice.setNotes("Updated notes");

        when(invoiceRepository.existsById(1L)).thenReturn(true);
        doNothing().when(invoiceRepository).update(invoice);

        SupplierInvoice result = payablesService.updateInvoice(invoice);

        assertEquals("Updated notes", result.getNotes());
        verify(invoiceRepository).update(invoice);
    }

    @Test
    void testUpdateInvoice_WithNonExistentId_ThrowsException() {
        SupplierInvoice invoice = createInvoice(999L, "SUP-INV-999999", 1000.00);

        when(invoiceRepository.existsById(999L)).thenReturn(false);

        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> {
            payablesService.updateInvoice(invoice);
        });

        verify(invoiceRepository, never()).update(any());
    }

    @Test
    void testCancelInvoice_WithValidReason_Success() {
        Long id = 1L;
        String reason = "Supplier requested cancellation";
        SupplierInvoice invoice = createInvoice(id, "SUP-INV-000001", 1000.00);
        invoice.setStatus(SupplierInvoice.InvoiceStatus.PENDING);

        when(invoiceRepository.findById(id)).thenReturn(Optional.of(invoice));
        doNothing().when(invoiceRepository).update(invoice);

        payablesService.cancelInvoice(id, reason);

        assertEquals(SupplierInvoice.InvoiceStatus.CANCELLED, invoice.getStatus());
        assertEquals(reason, invoice.getCancellationReason());
        verify(invoiceRepository).update(invoice);
    }

    @Test
    void testCancelInvoice_WhenAlreadyCancelled_ThrowsException() {
        Long id = 1L;
        SupplierInvoice invoice = createInvoice(id, "SUP-INV-000001", 1000.00);
        invoice.setStatus(SupplierInvoice.InvoiceStatus.CANCELLED);

        when(invoiceRepository.findById(id)).thenReturn(Optional.of(invoice));

        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            payablesService.cancelInvoice(id, "Another reason");
        });

        assertTrue(exception.getMessage().contains("ya está cancelada"));
        verify(invoiceRepository, never()).update(any());
    }

    @Test
    void testCancelInvoice_WhenPaid_ThrowsException() {
        Long id = 1L;
        SupplierInvoice invoice = createInvoice(id, "SUP-INV-000001", 1000.00);
        invoice.setStatus(SupplierInvoice.InvoiceStatus.PAID);

        when(invoiceRepository.findById(id)).thenReturn(Optional.of(invoice));

        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            payablesService.cancelInvoice(id, "Reason");
        });

        assertTrue(exception.getMessage().contains("no se puede cancelar una factura pagada"));
        verify(invoiceRepository, never()).update(any());
    }

    @Test
    void testDeleteInvoice_WhenExists_Success() {
        Long id = 1L;
        SupplierInvoice invoice = createInvoice(id, "SUP-INV-000001", 1000.00);

        when(invoiceRepository.findById(id)).thenReturn(Optional.of(invoice));
        doNothing().when(invoiceRepository).delete(invoice);

        payablesService.deleteInvoice(id);

        verify(invoiceRepository).delete(invoice);
    }

    @Test
    void testDeleteInvoice_WhenNotExists_ThrowsException() {
        Long id = 999L;
        when(invoiceRepository.findById(id)).thenReturn(Optional.empty());

        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> {
            payablesService.deleteInvoice(id);
        });

        verify(invoiceRepository, never()).delete(any());
    }

    @Test
    void testGetInvoicesCount_ReturnsCount() {
        long count = 10L;
        when(invoiceRepository.count()).thenReturn(count);

        long result = payablesService.getInvoicesCount();

        assertEquals(10L, result);
        verify(invoiceRepository).count();
    }

    @Test
    void testGetPendingInvoicesCount_ReturnsCount() {
        long pendingCount = 5L;
        when(invoiceRepository.countByStatus(SupplierInvoice.InvoiceStatus.PENDING)).thenReturn(pendingCount);

        long result = payablesService.getPendingInvoicesCount();

        assertEquals(5L, result);
        verify(invoiceRepository).countByStatus(SupplierInvoice.InvoiceStatus.PENDING);
    }

    @Test
    void testGetOverdueInvoicesCount_ReturnsCount() {
        long overdueCount = 3L;
        LocalDate beforeDate = LocalDate.now().minusDays(30);
        when(invoiceRepository.countOverdueInvoices(beforeDate)).thenReturn(overdueCount);

        long result = payablesService.getOverdueInvoicesCount(beforeDate);

        assertEquals(3L, result);
        verify(invoiceRepository).countOverdueInvoices(beforeDate);
    }

    private SupplierInvoice createInvoice(Long id, String invoiceNumber, double amount) {
        SupplierInvoice invoice = new SupplierInvoice();
        invoice.setId(id);
        invoice.setInvoiceNumber(invoiceNumber);
        invoice.setTotalAmount(BigDecimal.valueOf(amount));
        invoice.setBalance(BigDecimal.valueOf(amount));
        invoice.setInvoiceDate(LocalDate.now());
        invoice.setDueDate(LocalDate.now().plusDays(30));
        invoice.setStatus(SupplierInvoice.InvoiceStatus.PENDING);
        invoice.setIsActive(true);
        return invoice;
    }

    private SupplierInvoice createInvoiceForSupplier(Long id, ThirdParty supplier, double amount) {
        SupplierInvoice invoice = createInvoice(id, "SUP-INV-" + String.format("%06d", id), amount);
        invoice.setSupplier(supplier);
        return invoice;
    }

    private SupplierInvoice createInvoiceWithStatus(Long id, String invoiceNumber, double amount, SupplierInvoice.InvoiceStatus status) {
        SupplierInvoice invoice = createInvoice(id, invoiceNumber, amount);
        invoice.setStatus(status);
        return invoice;
    }

    private SupplierInvoice createInvoiceWithDueDate(Long id, String invoiceNumber, double amount, LocalDate dueDate) {
        SupplierInvoice invoice = createInvoice(id, invoiceNumber, amount);
        invoice.setDueDate(dueDate);
        return invoice;
    }

    private ThirdParty createThirdParty(Long id, String name, ThirdParty.ThirdPartyType type) {
        ThirdParty thirdParty = new ThirdParty();
        thirdParty.setId(id);
        thirdParty.setName(name);
        thirdParty.setIdentificationNumber("12345678901");
        thirdParty.setType(type);
        thirdParty.setEmail("test@example.com");
        thirdParty.setIsActive(true);
        return thirdParty;
    }
}
