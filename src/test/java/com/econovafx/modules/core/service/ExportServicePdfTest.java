package com.econovafx.modules.core.service;

import com.econovafx.modules.accounting.model.Account;
import com.econovafx.modules.accounting.model.Transaction;
import com.econovafx.modules.accounting.model.TransactionEntry;
import com.econovafx.modules.bank.model.BankReconciliation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for ExportService PDF generation.
 * Generated PDFs are exported to the export_pdf/ directory for visual verification.
 */
public class ExportServicePdfTest {

    private ExportService exportService;

    private static final String EXPORT_DIR = "export_pdf";

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        exportService = new ExportService();
        
        // Create export directory if it doesn't exist
        File dir = new File(EXPORT_DIR);
        if (!dir.exists()) {
            dir.mkdirs();
        }
    }

    @Test
    public void testExportTransactionToPdf_GeneratesValidPdf() throws IOException {
        // Arrange: Create a sample transaction
        Transaction transaction = createSampleTransaction();
        
        // Act: Generate PDF
        byte[] pdfContent = exportService.exportTransactionToPdf(transaction);
        
        // Assert: PDF is not null and has content
        assertNotNull(pdfContent);
        assertTrue(pdfContent.length > 0, "PDF should have content");
        
        // Export PDF to file for visual verification
        String fileName = EXPORT_DIR + "/transaction_voucher_" + transaction.getNumber() + ".pdf";
        savePdfToFile(pdfContent, fileName);
        
        // Verify file was created
        File pdfFile = new File(fileName);
        assertTrue(pdfFile.exists(), "PDF file should be created in " + EXPORT_DIR);
        assertTrue(pdfFile.length() > 0, "PDF file should not be empty");
        
        System.out.println("Transaction voucher PDF generated: " + pdfFile.getAbsolutePath());
    }

    @Test
    public void testExportBankReconciliationToPdf_GeneratesValidPdf() throws IOException {
        // Arrange: Create a sample bank reconciliation
        BankReconciliation reconciliation = createSampleBankReconciliation();
        
        // Act: Generate PDF
        byte[] pdfContent = exportService.exportBankReconciliationToPdf(reconciliation);
        
        // Assert: PDF is not null and has content
        assertNotNull(pdfContent);
        assertTrue(pdfContent.length > 0, "PDF should have content");
        
        // Export PDF to file for visual verification
        String fileName = EXPORT_DIR + "/bank_reconciliation_" + reconciliation.getId() + ".pdf";
        savePdfToFile(pdfContent, fileName);
        
        // Verify file was created
        File pdfFile = new File(fileName);
        assertTrue(pdfFile.exists(), "PDF file should be created in " + EXPORT_DIR);
        assertTrue(pdfFile.length() > 0, "PDF file should not be empty");
        
        System.out.println("Bank reconciliation PDF generated: " + pdfFile.getAbsolutePath());
    }

    @Test
    public void testExportBalanceSheetToPdf_GeneratesValidPdf() throws IOException {
        // Arrange: Create sample accounts
        List<Account> accounts = createSampleAccountsForFinancialStatements();
        LocalDate startDate = LocalDate.now().withDayOfMonth(1);
        LocalDate endDate = LocalDate.now();
        
        // Act: Generate PDF
        byte[] pdfContent = exportService.exportBalanceSheetToPdf(accounts, startDate, endDate);
        
        // Assert: PDF is not null and has content
        assertNotNull(pdfContent);
        assertTrue(pdfContent.length > 0, "PDF should have content");
        
        // Export PDF to file for visual verification
        String fileName = EXPORT_DIR + "/balance_sheet.pdf";
        savePdfToFile(pdfContent, fileName);
        
        // Verify file was created
        File pdfFile = new File(fileName);
        assertTrue(pdfFile.exists(), "PDF file should be created in " + EXPORT_DIR);
        assertTrue(pdfFile.length() > 0, "PDF file should not be empty");
        
        System.out.println("Balance Sheet PDF generated: " + pdfFile.getAbsolutePath());
    }

    @Test
    public void testExportIncomeStatementToPdf_GeneratesValidPdf() throws IOException {
        // Arrange: Create sample accounts
        List<Account> accounts = createSampleAccountsForFinancialStatements();
        LocalDate startDate = LocalDate.now().withDayOfMonth(1);
        LocalDate endDate = LocalDate.now();
        
        // Act: Generate PDF
        byte[] pdfContent = exportService.exportIncomeStatementToPdf(accounts, startDate, endDate);
        
        // Assert: PDF is not null and has content
        assertNotNull(pdfContent);
        assertTrue(pdfContent.length > 0, "PDF should have content");
        
        // Export PDF to file for visual verification
        String fileName = EXPORT_DIR + "/income_statement.pdf";
        savePdfToFile(pdfContent, fileName);
        
        // Verify file was created
        File pdfFile = new File(fileName);
        assertTrue(pdfFile.exists(), "PDF file should be created in " + EXPORT_DIR);
        assertTrue(pdfFile.length() > 0, "PDF file should not be empty");
        
        System.out.println("Income Statement PDF generated: " + pdfFile.getAbsolutePath());
    }

    /**
     * Helper method to save PDF content to a file
     */
    private void savePdfToFile(byte[] pdfContent, String fileName) throws IOException {
        try (FileOutputStream fos = new FileOutputStream(fileName)) {
            fos.write(pdfContent);
            fos.flush();
        }
    }

    /**
     * Creates a sample transaction for testing
     */
    private Transaction createSampleTransaction() {
        return createSampleTransactionWithNumber("VC-000001");
    }

    /**
     * Creates a sample transaction with a specific number
     */
    private Transaction createSampleTransactionWithNumber(String number) {
        Transaction transaction = new Transaction();
        transaction.setId(1L);
        transaction.setNumber(number);
        transaction.setDate(LocalDate.now());
        transaction.setType("VC");
        transaction.setDescription("Test transaction for PDF export");
        transaction.setIsPosted(true);
        
        // Create accounts for entries
        Account account1 = new Account();
        account1.setId(1L);
        account1.setCode("101.001");
        account1.setName("Caja - Moneda Nacional");
        
        Account account2 = new Account();
        account2.setId(2L);
        account2.setCode("401.001");
        account2.setName("Ventas");
        
        // Add transaction entries
        List<TransactionEntry> entries = new ArrayList<>();
        
        TransactionEntry entry1 = new TransactionEntry();
        entry1.setId(1L);
        entry1.setAccount(account1);
        entry1.setDebitAmount(new BigDecimal("1000.00"));
        entry1.setCreditAmount(BigDecimal.ZERO);
        entry1.setDescription("Debit entry");
        entries.add(entry1);
        
        TransactionEntry entry2 = new TransactionEntry();
        entry2.setId(2L);
        entry2.setAccount(account2);
        entry2.setDebitAmount(BigDecimal.ZERO);
        entry2.setCreditAmount(new BigDecimal("1000.00"));
        entry2.setDescription("Credit entry");
        entries.add(entry2);
        
        transaction.setEntries(entries);
        
        return transaction;
    }

    /**
     * Creates a sample bank reconciliation for testing
     */
    private BankReconciliation createSampleBankReconciliation() {
        BankReconciliation reconciliation = new BankReconciliation();
        reconciliation.setId(1L);
        reconciliation.setBankAccountId(1L);
        reconciliation.setReconciliationNumber("RC-000001");
        reconciliation.setStatementDate(LocalDate.now());
        reconciliation.setBankBalance(new BigDecimal("50000.00"));
        reconciliation.setSystemBalance(new BigDecimal("49500.00"));
        reconciliation.setReconciledBalance(new BigDecimal("50500.00"));
        reconciliation.setStatus(BankReconciliation.Status.COMPLETED);
        
        // Add bank items
        List<com.econovafx.modules.bank.model.ReconciliationItem> bankItems = new ArrayList<>();
        
        com.econovafx.modules.bank.model.ReconciliationItem item1 = 
            new com.econovafx.modules.bank.model.ReconciliationItem();
        item1.setId(1L);
        item1.setDate(LocalDate.now().minusDays(5));
        item1.setDescription("Check #1234");
        item1.setAmount(new BigDecimal("500.00"));
        item1.setOriginType(com.econovafx.modules.bank.model.ReconciliationItem.OriginType.BANK);
        item1.setReconciled(false);
        bankItems.add(item1);
        
        com.econovafx.modules.bank.model.ReconciliationItem item2 = 
            new com.econovafx.modules.bank.model.ReconciliationItem();
        item2.setId(2L);
        item2.setDate(LocalDate.now().minusDays(3));
        item2.setDescription("Deposit #5678");
        item2.setAmount(new BigDecimal("2000.00"));
        item2.setOriginType(com.econovafx.modules.bank.model.ReconciliationItem.OriginType.BANK);
        item2.setReconciled(false);
        bankItems.add(item2);
        
        reconciliation.setBankItems(bankItems);
        
        return reconciliation;
    }

    /**
     * Creates sample accounts for financial statement tests
     */
    private List<Account> createSampleAccountsForFinancialStatements() {
        List<Account> accounts = new ArrayList<>();
        
        // Asset account
        Account assetAccount = new Account();
        assetAccount.setId(1L);
        assetAccount.setCode("101.001");
        assetAccount.setName("Caja - Moneda Nacional");
        assetAccount.setType(com.econovafx.modules.accounting.model.AccountType.ASSET);
        accounts.add(assetAccount);
        
        // Liability account
        Account liabilityAccount = new Account();
        liabilityAccount.setId(2L);
        liabilityAccount.setCode("201.001");
        liabilityAccount.setName("Cuentas por Pagar");
        liabilityAccount.setType(com.econovafx.modules.accounting.model.AccountType.LIABILITY);
        accounts.add(liabilityAccount);
        
        // Equity account
        Account equityAccount = new Account();
        equityAccount.setId(3L);
        equityAccount.setCode("301.001");
        equityAccount.setName("Capital Social");
        equityAccount.setType(com.econovafx.modules.accounting.model.AccountType.EQUITY);
        accounts.add(equityAccount);
        
        // Revenue account
        Account revenueAccount = new Account();
        revenueAccount.setId(4L);
        revenueAccount.setCode("401.001");
        revenueAccount.setName("Ventas de Mercancías");
        revenueAccount.setType(com.econovafx.modules.accounting.model.AccountType.REVENUE);
        accounts.add(revenueAccount);
        
        // Expense account
        Account expenseAccount = new Account();
        expenseAccount.setId(5L);
        expenseAccount.setCode("501.001");
        expenseAccount.setName("Gastos de Personal");
        expenseAccount.setType(com.econovafx.modules.accounting.model.AccountType.EXPENSE);
        accounts.add(expenseAccount);
        
        return accounts;
    }
}
