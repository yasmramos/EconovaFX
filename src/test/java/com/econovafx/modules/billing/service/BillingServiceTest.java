package com.econovafx.modules.billing.service;

import com.econovafx.modules.billing.model.*;
import com.econovafx.modules.billing.repository.SalesInvoiceRepository;
import com.econovafx.modules.billing.repository.BillingSeriesRepository;
import com.econovafx.modules.billing.repository.TaxRateRepository;
import com.econovafx.modules.accounting.repository.AccountRepository;
import com.econovafx.modules.accounting.service.TransactionService;
import com.econovafx.modules.inventory.service.InventoryService;
import com.econovafx.modules.core.service.AuditService;
import com.econovafx.modules.core.model.AuditLog;
import com.econovafx.modules.core.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Tests para BillingService.
 */
@ExtendWith(MockitoExtension.class)
class BillingServiceTest {

    @Mock
    private SalesInvoiceRepository salesInvoiceRepository;

    @Mock
    private BillingSeriesRepository billingSeriesRepository;

    @Mock
    private SequentialNumberService sequentialNumberService;

    @Mock
    private TransactionService transactionService;

    @Mock
    private InventoryService inventoryService;

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private TaxRateRepository taxRateRepository;

    @Mock
    private AuditService auditService;

    private BillingService billingService;

    @BeforeEach
    void setUp() {
        billingService = new BillingService(
            salesInvoiceRepository,
            billingSeriesRepository,
            sequentialNumberService,
            transactionService,
            inventoryService,
            accountRepository,
            taxRateRepository,
            auditService
        );
    }

    @Test
    void testIssueInvoice_Success() {
        // Arrange
        ThirdParty customer = new ThirdParty();
        customer.setId(1L);
        customer.setName("Cliente Test");
        customer.setTaxId("123456789");

        BillingSeries series = new BillingSeries();
        series.setId(1L);
        series.setCode("FAC001");

        SalesInvoiceLine line = new SalesInvoiceLine();
        line.setProductCode("PROD001");
        line.setQuantity(BigDecimal.valueOf(2));
        line.setUnitPrice(BigDecimal.valueOf(100));
        line.setDiscount(BigDecimal.ZERO);
        line.setTaxRate(null);

        SalesInvoice invoice = new SalesInvoice();
        invoice.setId(1L);
        invoice.setThirdParty(customer);
        invoice.setBillingSeries(series);
        invoice.setLines(List.of(line));
        invoice.setWarehouseId(1L);

        when(sequentialNumberService.generateDocumentNumber(1L)).thenReturn("FAC001-00000001");
        when(salesInvoiceRepository.save(any(SalesInvoice.class))).thenAnswer(i -> i.getArguments()[0]);
        when(transactionService.createTransaction(any(), anyList(), anyString())).thenReturn(new com.econovafx.modules.accounting.model.Transaction());
        doNothing().when(auditService).logWithValues(any(), any(), any(), any(), any(), any(), any());

        // Act
        SalesInvoice result = billingService.issueInvoice(invoice, "testuser");

        // Assert
        assertNotNull(result);
        assertEquals("FAC001-00000001", result.getInvoiceNumber());
        assertEquals(SalesInvoice.InvoiceStatus.ISSUED, result.getStatus());
        assertNotNull(result.getIssueDate());
        verify(salesInvoiceRepository, times(2)).save(any(SalesInvoice.class));
        verify(auditService).logWithValues(any(), any(), any(), any(), any(), any(), any());
    }

    @Test
    void testIssueInvoice_ThrowsExceptionWhenNoThirdParty() {
        // Arrange
        SalesInvoice invoice = new SalesInvoice();
        invoice.setThirdParty(null);
        invoice.setBillingSeries(new BillingSeries());
        invoice.setLines(List.of(new SalesInvoiceLine()));

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> 
            billingService.issueInvoice(invoice, "testuser")
        );
    }

    @Test
    void testIssueInvoice_ThrowsExceptionWhenNoBillingSeries() {
        // Arrange
        SalesInvoice invoice = new SalesInvoice();
        invoice.setThirdParty(new ThirdParty());
        invoice.setBillingSeries(null);
        invoice.setLines(List.of(new SalesInvoiceLine()));

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> 
            billingService.issueInvoice(invoice, "testuser")
        );
    }

    @Test
    void testIssueInvoice_ThrowsExceptionWhenNoLines() {
        // Arrange
        SalesInvoice invoice = new SalesInvoice();
        invoice.setThirdParty(new ThirdParty());
        invoice.setBillingSeries(new BillingSeries());
        invoice.setLines(null);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> 
            billingService.issueInvoice(invoice, "testuser")
        );
    }

    @Test
    void testIssueInvoice_ThrowsExceptionWhenEmptyLines() {
        // Arrange
        SalesInvoice invoice = new SalesInvoice();
        invoice.setThirdParty(new ThirdParty());
        invoice.setBillingSeries(new BillingSeries());
        invoice.setLines(List.of());

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> 
            billingService.issueInvoice(invoice, "testuser")
        );
    }

    @Test
    void testIssueInvoice_ThrowsExceptionWhenLineHasNoProductCode() {
        // Arrange
        SalesInvoiceLine line = new SalesInvoiceLine();
        line.setProductCode(null);
        line.setQuantity(BigDecimal.ONE);
        line.setUnitPrice(BigDecimal.TEN);

        SalesInvoice invoice = new SalesInvoice();
        invoice.setThirdParty(new ThirdParty());
        invoice.setBillingSeries(new BillingSeries());
        invoice.setLines(List.of(line));

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> 
            billingService.issueInvoice(invoice, "testuser")
        );
    }

    @Test
    void testIssueInvoice_ThrowsExceptionWhenLineHasInvalidQuantity() {
        // Arrange
        SalesInvoiceLine line = new SalesInvoiceLine();
        line.setProductCode("PROD001");
        line.setQuantity(BigDecimal.ZERO);
        line.setUnitPrice(BigDecimal.TEN);

        SalesInvoice invoice = new SalesInvoice();
        invoice.setThirdParty(new ThirdParty());
        invoice.setBillingSeries(new BillingSeries());
        invoice.setLines(List.of(line));

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> 
            billingService.issueInvoice(invoice, "testuser")
        );
    }

    @Test
    void testIssueInvoice_ThrowsExceptionWhenLineHasNegativeQuantity() {
        // Arrange
        SalesInvoiceLine line = new SalesInvoiceLine();
        line.setProductCode("PROD001");
        line.setQuantity(BigDecimal.valueOf(-1));
        line.setUnitPrice(BigDecimal.TEN);

        SalesInvoice invoice = new SalesInvoice();
        invoice.setThirdParty(new ThirdParty());
        invoice.setBillingSeries(new BillingSeries());
        invoice.setLines(List.of(line));

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> 
            billingService.issueInvoice(invoice, "testuser")
        );
    }

    @Test
    void testIssueInvoice_ThrowsExceptionWhenLineHasInvalidPrice() {
        // Arrange
        SalesInvoiceLine line = new SalesInvoiceLine();
        line.setProductCode("PROD001");
        line.setQuantity(BigDecimal.ONE);
        line.setUnitPrice(BigDecimal.ZERO);

        SalesInvoice invoice = new SalesInvoice();
        invoice.setThirdParty(new ThirdParty());
        invoice.setBillingSeries(new BillingSeries());
        invoice.setLines(List.of(line));

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> 
            billingService.issueInvoice(invoice, "testuser")
        );
    }

    @Test
    void testIssueInvoice_ThrowsExceptionWhenLineHasNegativePrice() {
        // Arrange
        SalesInvoiceLine line = new SalesInvoiceLine();
        line.setProductCode("PROD001");
        line.setQuantity(BigDecimal.ONE);
        line.setUnitPrice(BigDecimal.valueOf(-10));

        SalesInvoice invoice = new SalesInvoice();
        invoice.setThirdParty(new ThirdParty());
        invoice.setBillingSeries(new BillingSeries());
        invoice.setLines(List.of(line));

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> 
            billingService.issueInvoice(invoice, "testuser")
        );
    }

    @Test
    void testIssueInvoice_CalculatesTotalsCorrectly() {
        // Arrange
        ThirdParty customer = new ThirdParty();
        customer.setId(1L);
        customer.setName("Cliente Test");
        customer.setTaxId("123456789");

        BillingSeries series = new BillingSeries();
        series.setId(1L);

        SalesInvoiceLine line = new SalesInvoiceLine();
        line.setProductCode("PROD001");
        line.setQuantity(BigDecimal.valueOf(2));
        line.setUnitPrice(BigDecimal.valueOf(100));
        line.setDiscount(BigDecimal.valueOf(10));
        line.setTaxRate(null);

        SalesInvoice invoice = new SalesInvoice();
        invoice.setId(1L);
        invoice.setThirdParty(customer);
        invoice.setBillingSeries(series);
        invoice.setLines(List.of(line));
        invoice.setWarehouseId(1L);

        when(sequentialNumberService.generateDocumentNumber(1L)).thenReturn("FAC001-00000001");
        when(salesInvoiceRepository.save(any(SalesInvoice.class))).thenAnswer(i -> i.getArguments()[0]);
        when(transactionService.createTransaction(any(), anyList(), anyString())).thenReturn(new com.econovafx.modules.accounting.model.Transaction());
        doNothing().when(auditService).logWithValues(any(), any(), any(), any(), any(), any(), any());

        // Act
        SalesInvoice result = billingService.issueInvoice(invoice, "testuser");

        // Assert
        assertNotNull(result);
        assertEquals(BigDecimal.valueOf(190), result.getSubtotal()); // (100 * 2) - 10
        assertEquals(BigDecimal.ZERO, result.getTaxAmount());
        assertEquals(BigDecimal.valueOf(190), result.getTotal());
    }

    @Test
    void testIssueInvoice_CalculatesTaxCorrectly() {
        // Arrange
        ThirdParty customer = new ThirdParty();
        customer.setId(1L);
        customer.setName("Cliente Test");
        customer.setTaxId("123456789");

        BillingSeries series = new BillingSeries();
        series.setId(1L);

        TaxRate taxRate = new TaxRate();
        taxRate.setPercentage(BigDecimal.valueOf(15));

        SalesInvoiceLine line = new SalesInvoiceLine();
        line.setProductCode("PROD001");
        line.setQuantity(BigDecimal.valueOf(2));
        line.setUnitPrice(BigDecimal.valueOf(100));
        line.setDiscount(BigDecimal.ZERO);
        line.setTaxRate(taxRate);

        SalesInvoice invoice = new SalesInvoice();
        invoice.setId(1L);
        invoice.setThirdParty(customer);
        invoice.setBillingSeries(series);
        invoice.setLines(List.of(line));
        invoice.setWarehouseId(1L);

        when(sequentialNumberService.generateDocumentNumber(1L)).thenReturn("FAC001-00000001");
        when(salesInvoiceRepository.save(any(SalesInvoice.class))).thenAnswer(i -> i.getArguments()[0]);
        when(transactionService.createTransaction(any(), anyList(), anyString())).thenReturn(new com.econovafx.modules.accounting.model.Transaction());
        doNothing().when(auditService).logWithValues(any(), any(), any(), any(), any(), any(), any());

        // Act
        SalesInvoice result = billingService.issueInvoice(invoice, "testuser");

        // Assert
        assertNotNull(result);
        assertEquals(BigDecimal.valueOf(200), result.getSubtotal());
        assertEquals(BigDecimal.valueOf(30), result.getTaxAmount()); // 200 * 15%
        assertEquals(BigDecimal.valueOf(230), result.getTotal());
    }

    @Test
    void testGetInvoiceById_Success() {
        // Arrange
        SalesInvoice invoice = new SalesInvoice();
        invoice.setId(1L);
        invoice.setInvoiceNumber("FAC001-00000001");

        when(salesInvoiceRepository.findById(1L)).thenReturn(Optional.of(invoice));

        // Act
        SalesInvoice result = billingService.getInvoiceById(1L);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("FAC001-00000001", result.getInvoiceNumber());
    }

    @Test
    void testGetInvoiceById_NotFound() {
        // Arrange
        when(salesInvoiceRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> 
            billingService.getInvoiceById(999L)
        );
    }

    @Test
    void testGetInvoiceByNumber_Success() {
        // Arrange
        SalesInvoice invoice = new SalesInvoice();
        invoice.setId(1L);
        invoice.setInvoiceNumber("FAC001-00000001");

        when(salesInvoiceRepository.findByInvoiceNumber("FAC001-00000001")).thenReturn(Optional.of(invoice));

        // Act
        SalesInvoice result = billingService.getInvoiceByNumber("FAC001-00000001");

        // Assert
        assertNotNull(result);
        assertEquals("FAC001-00000001", result.getInvoiceNumber());
    }

    @Test
    void testGetInvoiceByNumber_NotFound() {
        // Arrange
        when(salesInvoiceRepository.findByInvoiceNumber("INVALID")).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> 
            billingService.getInvoiceByNumber("INVALID")
        );
    }

    @Test
    void testGetAllInvoices() {
        // Arrange
        SalesInvoice invoice1 = new SalesInvoice();
        invoice1.setId(1L);
        SalesInvoice invoice2 = new SalesInvoice();
        invoice2.setId(2L);

        when(salesInvoiceRepository.findAll()).thenReturn(List.of(invoice1, invoice2));

        // Act
        List<SalesInvoice> result = billingService.getAllInvoices();

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
    }

    @Test
    void testGetInvoicesByStatus() {
        // Arrange
        SalesInvoice invoice1 = new SalesInvoice();
        invoice1.setId(1L);
        invoice1.setStatus(SalesInvoice.InvoiceStatus.ISSUED);
        SalesInvoice invoice2 = new SalesInvoice();
        invoice2.setId(2L);
        invoice2.setStatus(SalesInvoice.InvoiceStatus.ISSUED);

        when(salesInvoiceRepository.findByStatus(SalesInvoice.InvoiceStatus.ISSUED))
            .thenReturn(List.of(invoice1, invoice2));

        // Act
        List<SalesInvoice> result = billingService.getInvoicesByStatus(SalesInvoice.InvoiceStatus.ISSUED);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.stream().allMatch(i -> i.getStatus() == SalesInvoice.InvoiceStatus.ISSUED));
    }

    @Test
    void testIssueInvoice_HandlesAccountingEntryError() {
        // Arrange
        ThirdParty customer = new ThirdParty();
        customer.setId(1L);
        customer.setName("Cliente Test");
        customer.setTaxId("123456789");

        BillingSeries series = new BillingSeries();
        series.setId(1L);

        SalesInvoiceLine line = new SalesInvoiceLine();
        line.setProductCode("PROD001");
        line.setQuantity(BigDecimal.ONE);
        line.setUnitPrice(BigDecimal.TEN);

        SalesInvoice invoice = new SalesInvoice();
        invoice.setId(1L);
        invoice.setThirdParty(customer);
        invoice.setBillingSeries(series);
        invoice.setLines(List.of(line));
        invoice.setWarehouseId(1L);

        when(sequentialNumberService.generateDocumentNumber(1L)).thenReturn("FAC001-00000001");
        when(salesInvoiceRepository.save(any(SalesInvoice.class))).thenAnswer(i -> i.getArguments()[0]);
        when(transactionService.createTransaction(any(), anyList(), anyString()))
            .thenThrow(new RuntimeException("Error contable"));
        doNothing().when(salesInvoiceRepository).update(any(SalesInvoice.class));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> 
            billingService.issueInvoice(invoice, "testuser")
        );
        assertTrue(exception.getMessage().contains("Error procesando factura"));
        verify(salesInvoiceRepository, times(2)).save(any(SalesInvoice.class));
        verify(salesInvoiceRepository).update(argThat(inv -> 
            inv.getNotes() != null && inv.getNotes().contains("Error en procesamiento posterior")));
    }

    @Test
    void testIssueInvoice_ThrowsExceptionWhenCustomerHasNoTaxId() {
        // Arrange
        ThirdParty customer = new ThirdParty();
        customer.setId(1L);
        customer.setName("Cliente Sin RUC");
        customer.setTaxId(null);

        BillingSeries series = new BillingSeries();
        series.setId(1L);

        SalesInvoiceLine line = new SalesInvoiceLine();
        line.setProductCode("PROD001");
        line.setQuantity(BigDecimal.ONE);
        line.setUnitPrice(BigDecimal.TEN);

        SalesInvoice invoice = new SalesInvoice();
        invoice.setId(1L);
        invoice.setThirdParty(customer);
        invoice.setBillingSeries(series);
        invoice.setLines(List.of(line));
        invoice.setWarehouseId(1L);

        when(sequentialNumberService.generateDocumentNumber(1L)).thenReturn("FAC001-00000001");
        when(salesInvoiceRepository.save(any(SalesInvoice.class))).thenAnswer(i -> i.getArguments()[0]);

        // Act & Assert
        assertThrows(RuntimeException.class, () -> 
            billingService.issueInvoice(invoice, "testuser")
        );
    }

    @Test
    void testIssueInvoice_ThrowsExceptionWhenCustomerTaxIdIsEmpty() {
        // Arrange
        ThirdParty customer = new ThirdParty();
        customer.setId(1L);
        customer.setName("Cliente Sin RUC");
        customer.setTaxId("");

        BillingSeries series = new BillingSeries();
        series.setId(1L);

        SalesInvoiceLine line = new SalesInvoiceLine();
        line.setProductCode("PROD001");
        line.setQuantity(BigDecimal.ONE);
        line.setUnitPrice(BigDecimal.TEN);

        SalesInvoice invoice = new SalesInvoice();
        invoice.setId(1L);
        invoice.setThirdParty(customer);
        invoice.setBillingSeries(series);
        invoice.setLines(List.of(line));
        invoice.setWarehouseId(1L);

        when(sequentialNumberService.generateDocumentNumber(1L)).thenReturn("FAC001-00000001");
        when(salesInvoiceRepository.save(any(SalesInvoice.class))).thenAnswer(i -> i.getArguments()[0]);

        // Act & Assert
        assertThrows(RuntimeException.class, () -> 
            billingService.issueInvoice(invoice, "testuser")
        );
    }
}
