package com.econovafx.modules.receivables.service;

import com.econovafx.modules.receivables.model.CustomerInvoice;
import com.econovafx.modules.receivables.repository.CustomerInvoiceRepository;
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
class ReceivablesServiceTest {

    @Mock
    private CustomerInvoiceRepository invoiceRepository;

    @Mock
    private ThirdPartyService thirdPartyService;

    @Mock
    private UserContext userContext;

    private ReceivablesService receivablesService;

    @BeforeEach
    void setUp() {
        receivablesService = new ReceivablesService(invoiceRepository, null, thirdPartyService, null, userContext);
    }

    @Test
    void testGetInvoiceById_WhenExists_ReturnsOptional() {
        Long id = 1L;
        CustomerInvoice invoice = createInvoice(id, "INV-000001", 1000.00);

        when(invoiceRepository.findById(id)).thenReturn(Optional.of(invoice));

        Optional<CustomerInvoice> result = receivablesService.getInvoiceById(id);

        assertTrue(result.isPresent());
        assertEquals("INV-000001", result.get().getInvoiceNumber());
        verify(invoiceRepository).findById(id);
    }

    @Test
    void testGetInvoiceById_WhenNotExists_ReturnsEmpty() {
        Long id = 999L;
        when(invoiceRepository.findById(id)).thenReturn(Optional.empty());

        Optional<CustomerInvoice> result = receivablesService.getInvoiceById(id);

        assertFalse(result.isPresent());
        verify(invoiceRepository).findById(id);
    }

    @Test
    void testGetInvoiceByNumber_WhenExists_ReturnsOptional() {
        String invoiceNumber = "INV-000001";
        CustomerInvoice invoice = createInvoice(1L, invoiceNumber, 1000.00);

        when(invoiceRepository.findByInvoiceNumber(invoiceNumber)).thenReturn(Optional.of(invoice));

        Optional<CustomerInvoice> result = receivablesService.getInvoiceByNumber(invoiceNumber);

        assertTrue(result.isPresent());
        assertEquals(invoiceNumber, result.get().getInvoiceNumber());
        verify(invoiceRepository).findByInvoiceNumber(invoiceNumber);
    }

    @Test
    void testGetAllInvoices_ReturnsList() {
        List<CustomerInvoice> invoices = Arrays.asList(
            createInvoice(1L, "INV-000001", 1000.00),
            createInvoice(2L, "INV-000002", 2000.00)
        );

        when(invoiceRepository.findAll()).thenReturn(invoices);

        List<CustomerInvoice> result = receivablesService.getAllInvoices();

        assertEquals(2, result.size());
        verify(invoiceRepository).findAll();
    }

    @Test
    void testGetInvoicesByCustomer_WhenExists_ReturnsList() {
        Long customerId = 1L;
        ThirdParty customer = createThirdParty(customerId, "Test Customer", ThirdParty.ThirdPartyType.CUSTOMER);
        List<CustomerInvoice> invoices = Arrays.asList(
            createInvoiceForCustomer(1L, customer, 1000.00),
            createInvoiceForCustomer(2L, customer, 2000.00)
        );

        when(thirdPartyService.getThirdPartyById(customerId)).thenReturn(Optional.of(customer));
        when(invoiceRepository.findByCustomer(customer)).thenReturn(invoices);

        List<CustomerInvoice> result = receivablesService.getInvoicesByCustomer(customerId);

        assertEquals(2, result.size());
        verify(thirdPartyService).getThirdPartyById(customerId);
        verify(invoiceRepository).findByCustomer(customer);
    }

    @Test
    void testGetInvoicesByCustomer_WhenNotExists_ThrowsException() {
        Long customerId = 999L;
        when(thirdPartyService.getThirdPartyById(customerId)).thenReturn(Optional.empty());

        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> {
            receivablesService.getInvoicesByCustomer(customerId);
        });

        verify(invoiceRepository, never()).findByCustomer(any());
    }

    @Test
    void testGetInvoicesByStatus_ReturnsFilteredList() {
        CustomerInvoice.InvoiceStatus status = CustomerInvoice.InvoiceStatus.PENDING;
        List<CustomerInvoice> invoices = Arrays.asList(
            createInvoiceWithStatus(1L, "INV-000001", 1000.00, status),
            createInvoiceWithStatus(2L, "INV-000002", 2000.00, status)
        );

        when(invoiceRepository.findByStatus(status)).thenReturn(invoices);

        List<CustomerInvoice> result = receivablesService.getInvoicesByStatus(status);

        assertEquals(2, result.size());
        verify(invoiceRepository).findByStatus(status);
    }

    @Test
    void testGetOverdueInvoices_ReturnsList() {
        LocalDate beforeDate = LocalDate.now().minusDays(30);
        List<CustomerInvoice> overdueInvoices = Arrays.asList(
            createInvoiceWithDueDate(1L, "INV-000001", 1000.00, LocalDate.now().minusDays(10)),
            createInvoiceWithDueDate(2L, "INV-000002", 2000.00, LocalDate.now().minusDays(5))
        );

        when(invoiceRepository.findOverdueInvoices(beforeDate)).thenReturn(overdueInvoices);

        List<CustomerInvoice> result = receivablesService.getOverdueInvoices(beforeDate);

        assertEquals(2, result.size());
        verify(invoiceRepository).findOverdueInvoices(beforeDate);
    }

    @Test
    void testCreateInvoice_WithValidData_Success() {
        CustomerInvoice invoice = createInvoice(null, null, 1000.00);
        CustomerInvoice savedInvoice = createInvoice(1L, "INV-000001", 1000.00);
        ThirdParty customer = createThirdParty(1L, "Test Customer", ThirdParty.ThirdPartyType.CUSTOMER);

        when(thirdPartyService.getThirdPartyById(1L)).thenReturn(Optional.of(customer));
        when(invoiceRepository.save(any(CustomerInvoice.class))).thenReturn(savedInvoice);

        CustomerInvoice result = receivablesService.createInvoice(invoice);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        verify(invoiceRepository).save(any(CustomerInvoice.class));
    }

    @Test
    void testCreateInvoice_WithNullCustomer_ThrowsException() {
        CustomerInvoice invoice = createInvoice(null, null, 1000.00);
        invoice.setCustomer(null);

        ValidationException exception = assertThrows(ValidationException.class, () -> {
            receivablesService.createInvoice(invoice);
        });

        verify(invoiceRepository, never()).save(any());
    }

    @Test
    void testCreateInvoice_WithInvalidCustomerType_ThrowsException() {
        CustomerInvoice invoice = createInvoice(null, null, 1000.00);
        ThirdParty supplier = createThirdParty(1L, "Test Supplier", ThirdParty.ThirdPartyType.SUPPLIER);

        when(thirdPartyService.getThirdPartyById(1L)).thenReturn(Optional.of(supplier));

        ValidationException exception = assertThrows(ValidationException.class, () -> {
            receivablesService.createInvoice(invoice);
        });

        assertTrue(exception.getMessage().contains("SUPPLIER"));
        verify(invoiceRepository, never()).save(any());
    }

    @Test
    void testCreateInvoice_WithNegativeAmount_ThrowsException() {
        CustomerInvoice invoice = createInvoice(null, null, -100.00);

        ValidationException exception = assertThrows(ValidationException.class, () -> {
            receivablesService.createInvoice(invoice);
        });

        verify(invoiceRepository, never()).save(any());
    }

    @Test
    void testCreateInvoice_WithDueDateBeforeInvoiceDate_ThrowsException() {
        CustomerInvoice invoice = createInvoice(null, null, 1000.00);
        invoice.setInvoiceDate(LocalDate.now());
        invoice.setDueDate(LocalDate.now().minusDays(1));

        ValidationException exception = assertThrows(ValidationException.class, () -> {
            receivablesService.createInvoice(invoice);
        });

        assertTrue(exception.getMessage().contains("fecha de vencimiento"));
        verify(invoiceRepository, never()).save(any());
    }

    @Test
    void testUpdateInvoice_WithValidData_Success() {
        CustomerInvoice invoice = createInvoice(1L, "INV-000001", 1000.00);
        invoice.setNotes("Updated notes");

        when(invoiceRepository.existsById(1L)).thenReturn(true);
        doNothing().when(invoiceRepository).update(invoice);

        CustomerInvoice result = receivablesService.updateInvoice(invoice);

        assertEquals("Updated notes", result.getNotes());
        verify(invoiceRepository).update(invoice);
    }

    @Test
    void testUpdateInvoice_WithNonExistentId_ThrowsException() {
        CustomerInvoice invoice = createInvoice(999L, "INV-999999", 1000.00);

        when(invoiceRepository.existsById(999L)).thenReturn(false);

        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> {
            receivablesService.updateInvoice(invoice);
        });

        verify(invoiceRepository, never()).update(any());
    }

    @Test
    void testCancelInvoice_WithValidReason_Success() {
        Long id = 1L;
        String reason = "Customer requested cancellation";
        CustomerInvoice invoice = createInvoice(id, "INV-000001", 1000.00);
        invoice.setStatus(CustomerInvoice.InvoiceStatus.PENDING);

        when(invoiceRepository.findById(id)).thenReturn(Optional.of(invoice));
        doNothing().when(invoiceRepository).update(invoice);

        receivablesService.cancelInvoice(id, reason);

        assertEquals(CustomerInvoice.InvoiceStatus.CANCELLED, invoice.getStatus());
        assertEquals(reason, invoice.getCancellationReason());
        verify(invoiceRepository).update(invoice);
    }

    @Test
    void testCancelInvoice_WhenAlreadyCancelled_ThrowsException() {
        Long id = 1L;
        CustomerInvoice invoice = createInvoice(id, "INV-000001", 1000.00);
        invoice.setStatus(CustomerInvoice.InvoiceStatus.CANCELLED);

        when(invoiceRepository.findById(id)).thenReturn(Optional.of(invoice));

        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            receivablesService.cancelInvoice(id, "Another reason");
        });

        assertTrue(exception.getMessage().contains("ya está cancelada"));
        verify(invoiceRepository, never()).update(any());
    }

    @Test
    void testCancelInvoice_WhenPaid_ThrowsException() {
        Long id = 1L;
        CustomerInvoice invoice = createInvoice(id, "INV-000001", 1000.00);
        invoice.setStatus(CustomerInvoice.InvoiceStatus.PAID);

        when(invoiceRepository.findById(id)).thenReturn(Optional.of(invoice));

        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            receivablesService.cancelInvoice(id, "Reason");
        });

        assertTrue(exception.getMessage().contains("no se puede cancelar una factura pagada"));
        verify(invoiceRepository, never()).update(any());
    }

    @Test
    void testDeleteInvoice_WhenExists_Success() {
        Long id = 1L;
        CustomerInvoice invoice = createInvoice(id, "INV-000001", 1000.00);

        when(invoiceRepository.findById(id)).thenReturn(Optional.of(invoice));
        doNothing().when(invoiceRepository).delete(invoice);

        receivablesService.deleteInvoice(id);

        verify(invoiceRepository).delete(invoice);
    }

    @Test
    void testDeleteInvoice_WhenNotExists_ThrowsException() {
        Long id = 999L;
        when(invoiceRepository.findById(id)).thenReturn(Optional.empty());

        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> {
            receivablesService.deleteInvoice(id);
        });

        verify(invoiceRepository, never()).delete(any());
    }

    @Test
    void testGetInvoicesCount_ReturnsCount() {
        long count = 10L;
        when(invoiceRepository.count()).thenReturn(count);

        long result = receivablesService.getInvoicesCount();

        assertEquals(10L, result);
        verify(invoiceRepository).count();
    }

    @Test
    void testGetPendingInvoicesCount_ReturnsCount() {
        long pendingCount = 5L;
        when(invoiceRepository.countByStatus(CustomerInvoice.InvoiceStatus.PENDING)).thenReturn(pendingCount);

        long result = receivablesService.getPendingInvoicesCount();

        assertEquals(5L, result);
        verify(invoiceRepository).countByStatus(CustomerInvoice.InvoiceStatus.PENDING);
    }

    @Test
    void testGetOverdueInvoicesCount_ReturnsCount() {
        long overdueCount = 3L;
        LocalDate beforeDate = LocalDate.now().minusDays(30);
        when(invoiceRepository.countOverdueInvoices(beforeDate)).thenReturn(overdueCount);

        long result = receivablesService.getOverdueInvoicesCount(beforeDate);

        assertEquals(3L, result);
        verify(invoiceRepository).countOverdueInvoices(beforeDate);
    }

    private CustomerInvoice createInvoice(Long id, String invoiceNumber, double amount) {
        CustomerInvoice invoice = new CustomerInvoice();
        invoice.setId(id);
        invoice.setInvoiceNumber(invoiceNumber);
        invoice.setTotalAmount(BigDecimal.valueOf(amount));
        invoice.setBalance(BigDecimal.valueOf(amount));
        invoice.setInvoiceDate(LocalDate.now());
        invoice.setDueDate(LocalDate.now().plusDays(30));
        invoice.setStatus(CustomerInvoice.InvoiceStatus.PENDING);
        invoice.setIsActive(true);
        return invoice;
    }

    private CustomerInvoice createInvoiceForCustomer(Long id, ThirdParty customer, double amount) {
        CustomerInvoice invoice = createInvoice(id, "INV-" + String.format("%06d", id), amount);
        invoice.setCustomer(customer);
        return invoice;
    }

    private CustomerInvoice createInvoiceWithStatus(Long id, String invoiceNumber, double amount, CustomerInvoice.InvoiceStatus status) {
        CustomerInvoice invoice = createInvoice(id, invoiceNumber, amount);
        invoice.setStatus(status);
        return invoice;
    }

    private CustomerInvoice createInvoiceWithDueDate(Long id, String invoiceNumber, double amount, LocalDate dueDate) {
        CustomerInvoice invoice = createInvoice(id, invoiceNumber, amount);
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
