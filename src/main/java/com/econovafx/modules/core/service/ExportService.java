package com.econovafx.modules.core.service;

import com.econovafx.core.i18n.I18nManager;
import com.econovafx.modules.accounting.model.Account;
import com.econovafx.modules.accounting.model.AccountType;
import com.econovafx.modules.accounting.model.Transaction;
import com.econovafx.modules.accounting.model.TransactionEntry;
import com.econovafx.modules.bank.model.BankReconciliation;
import com.econovafx.modules.bank.model.ReconciliationItem;
import com.econovafx.modules.billing.model.ThirdParty;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.inject.Singleton;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

/**
 * Service for exporting transactions to PDF and Excel formats
 */
@Singleton
public class ExportService {

    private static final Logger logger = LoggerFactory.getLogger(ExportService.class);
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    // Resource bundle for internationalization
    private static ResourceBundle bundle;
    
    /**
     * Gets the resource bundle, initializing it if necessary.
     */
    private static ResourceBundle getBundle() {
        if (bundle == null) {
            bundle = I18nManager.getBundle();
        }
        return bundle;
    }

    /**
     * Export a single transaction to PDF
     * @param transaction the transaction to export
     * @return byte array containing the PDF content
     * @throws IOException if an error occurs during PDF generation
     */
    public byte[] exportTransactionToPdf(Transaction transaction) throws IOException {
        ResourceBundle bundle = getBundle();
        
        try (PDDocument document = new PDDocument()) {
            PDPage page = new PDPage();
            document.addPage(page);

            PDPageContentStream contentStream = new PDPageContentStream(document, page);
            
            // Title - Comprobante Contable
            contentStream.beginText();
            contentStream.setFont(PDType1Font.HELVETICA_BOLD, 16);
            contentStream.newLineAtOffset(50, 750);
            String voucherTitle = String.format(bundle.getString("report.voucher.title"), transaction.getNumber());
            contentStream.showText(voucherTitle);
            contentStream.endText();

            // Transaction details
            contentStream.beginText();
            contentStream.setFont(PDType1Font.HELVETICA, 12);
            contentStream.newLineAtOffset(50, 700);
            contentStream.showText(bundle.getString("report.voucher.date") + ": " + transaction.getDate().format(DATE_FORMATTER));
            contentStream.newLineAtOffset(0, -20);
            contentStream.showText(bundle.getString("report.voucher.type") + ": " + transaction.getType());
            contentStream.newLineAtOffset(0, -20);
            String status = transaction.getIsPosted() ? bundle.getString("report.voucher.status.posted") : bundle.getString("report.voucher.status.draft");
            contentStream.showText(bundle.getString("report.voucher.status") + ": " + status);
            contentStream.newLineAtOffset(0, -20);
            contentStream.showText(bundle.getString("report.voucher.description") + ": " + transaction.getDescription());
            
            // Third party info if exists
            if (transaction.getThirdParty() != null) {
                ThirdParty tp = transaction.getThirdParty();
                contentStream.newLineAtOffset(0, -30);
                contentStream.showText(bundle.getString("report.voucher.third.party") + ": " + tp.getName() + " (" + tp.getIdentificationNumber() + ")");
                contentStream.newLineAtOffset(0, -20);
                contentStream.showText(bundle.getString("report.voucher.third.party.type") + ": " + tp.getType());
            }
            
            contentStream.endText();

            // Table header
            float yPosition = 550;
            contentStream.beginText();
            contentStream.setFont(PDType1Font.HELVETICA_BOLD, 11);
            contentStream.newLineAtOffset(50, yPosition);
            contentStream.showText(bundle.getString("report.voucher.account.code"));
            contentStream.newLineAtOffset(150, 0);
            contentStream.showText(bundle.getString("report.voucher.account.name"));
            contentStream.newLineAtOffset(200, 0);
            contentStream.showText(bundle.getString("report.voucher.debit"));
            contentStream.newLineAtOffset(100, 0);
            contentStream.showText(bundle.getString("report.voucher.credit"));
            contentStream.endText();

            // Table lines
            contentStream.moveTo(50, yPosition - 5);
            contentStream.lineTo(550, yPosition - 5);
            contentStream.stroke();

            yPosition -= 25;
            contentStream.setFont(PDType1Font.HELVETICA, 10);

            for (TransactionEntry entry : transaction.getEntries()) {
                contentStream.beginText();
                contentStream.newLineAtOffset(50, yPosition);
                contentStream.showText(entry.getAccount().getCode());
                contentStream.newLineAtOffset(150, 0);
                
                String accountName = entry.getAccount().getName();
                if (accountName.length() > 25) {
                    accountName = accountName.substring(0, 22) + "...";
                }
                contentStream.showText(accountName);
                
                contentStream.newLineAtOffset(200, 0);
                contentStream.showText(entry.getDebitAmount().compareTo(BigDecimal.ZERO) > 0 
                    ? entry.getDebitAmount().toPlainString() : "-");
                
                contentStream.newLineAtOffset(100, 0);
                contentStream.showText(entry.getCreditAmount().compareTo(BigDecimal.ZERO) > 0 
                    ? entry.getCreditAmount().toPlainString() : "-");
                
                contentStream.endText();
                yPosition -= 20;
            }

            // Total
            yPosition -= 30;
            contentStream.beginText();
            contentStream.setFont(PDType1Font.HELVETICA_BOLD, 12);
            contentStream.newLineAtOffset(50, yPosition);
            contentStream.showText(bundle.getString("report.voucher.total.debit") + ": " + transaction.getTotalDebit().toPlainString());
            contentStream.newLineAtOffset(200, 0);
            contentStream.showText(bundle.getString("report.voucher.total.credit") + ": " + transaction.getTotalCredit().toPlainString());
            contentStream.endText();

            contentStream.close();
            
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            document.save(outputStream);
            return outputStream.toByteArray();
        }
    }

    /**
     * Export multiple transactions to Excel
     * @param transactions list of transactions to export
     * @return byte array containing the Excel workbook content
     * @throws IOException if an error occurs during Excel generation
     */
    public byte[] exportTransactionsToExcel(List<Transaction> transactions) throws IOException {
        ResourceBundle bundle = getBundle();
        
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Transactions");

            // Create header row
            Row headerRow = sheet.createRow(0);
            String[] headers = {
                bundle.getString("report.voucher.type"),
                bundle.getString("report.voucher.date"),
                bundle.getString("report.voucher.type"),
                bundle.getString("report.voucher.status"),
                bundle.getString("report.voucher.description"), 
                bundle.getString("report.voucher.third.party"),
                bundle.getString("report.thirdparty.id.number"),
                bundle.getString("report.voucher.account.code"), 
                bundle.getString("report.voucher.account.name"),
                bundle.getString("report.voucher.debit"),
                bundle.getString("report.voucher.credit")
            };
            
            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            // Data rows
            int rowNum = 1;
            for (Transaction transaction : transactions) {
                for (TransactionEntry entry : transaction.getEntries()) {
                    Row row = sheet.createRow(rowNum++);
                    
                    row.createCell(0).setCellValue(transaction.getNumber());
                    row.createCell(1).setCellValue(transaction.getDate().format(DATE_FORMATTER));
                    row.createCell(2).setCellValue(transaction.getType());
                    String status = transaction.getIsPosted() ? bundle.getString("report.voucher.status.posted") : bundle.getString("report.voucher.status.draft");
                    row.createCell(3).setCellValue(status);
                    row.createCell(4).setCellValue(transaction.getDescription());
                    
                    // Third party info
                    if (transaction.getThirdParty() != null) {
                        row.createCell(5).setCellValue(transaction.getThirdParty().getName());
                        row.createCell(6).setCellValue(transaction.getThirdParty().getIdentificationNumber());
                    } else {
                        row.createCell(5).setCellValue("");
                        row.createCell(6).setCellValue("");
                    }
                    
                    row.createCell(7).setCellValue(entry.getAccount().getCode());
                    row.createCell(8).setCellValue(entry.getAccount().getName());
                    row.createCell(9).setCellValue(entry.getDebitAmount().doubleValue());
                    row.createCell(10).setCellValue(entry.getCreditAmount().doubleValue());
                }
            }

            // Auto-size columns
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);
            return outputStream.toByteArray();
        }
    }

    /**
     * Export a single transaction to Excel
     * @param transaction the transaction to export
     * @return byte array containing the Excel workbook content
     * @throws IOException if an error occurs during Excel generation
     */
    public byte[] exportTransactionToExcel(Transaction transaction) throws IOException {
        return exportTransactionsToExcel(List.of(transaction));
    }
    
    /**
     * Export third parties to Excel file
     * @param thirdParties list of third parties to export
     * @param file the file to write to
     * @throws IOException if an error occurs during Excel generation
     */
    public void exportThirdPartiesToExcel(List<ThirdParty> thirdParties, java.io.File file) throws IOException {
        ResourceBundle bundle = getBundle();
        
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet(bundle.getString("report.thirdparty.title"));

            // Create header row
            Row headerRow = sheet.createRow(0);
            String[] headers = {
                bundle.getString("report.thirdparty.name"),
                bundle.getString("report.thirdparty.id.number"),
                bundle.getString("report.thirdparty.type"),
                bundle.getString("report.thirdparty.email"),
                bundle.getString("report.thirdparty.phone"),
                bundle.getString("report.thirdparty.address"), 
                bundle.getString("report.thirdparty.city"),
                bundle.getString("report.thirdparty.country"),
                bundle.getString("report.thirdparty.tax.id"),
                bundle.getString("report.thirdparty.credit.limit"),
                bundle.getString("report.thirdparty.payment.days"), 
                bundle.getString("report.thirdparty.balance"),
                bundle.getString("report.thirdparty.active")
            };
            
            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            // Data rows
            int rowNum = 1;
            for (ThirdParty tp : thirdParties) {
                Row row = sheet.createRow(rowNum++);
                
                row.createCell(0).setCellValue(tp.getName());
                row.createCell(1).setCellValue(tp.getIdentificationNumber());
                row.createCell(2).setCellValue(tp.getType().toString());
                row.createCell(3).setCellValue(tp.getEmail() != null ? tp.getEmail() : "");
                row.createCell(4).setCellValue(tp.getPhone() != null ? tp.getPhone() : "");
                row.createCell(5).setCellValue(tp.getAddress() != null ? tp.getAddress() : "");
                row.createCell(6).setCellValue(tp.getCity() != null ? tp.getCity() : "");
                row.createCell(7).setCellValue(tp.getCountry() != null ? tp.getCountry() : "");
                row.createCell(8).setCellValue(tp.getTaxId() != null ? tp.getTaxId() : "");
                row.createCell(9).setCellValue(tp.getCreditLimit() != null ? tp.getCreditLimit().doubleValue() : 0.0);
                row.createCell(10).setCellValue(tp.getPaymentDays() != null ? tp.getPaymentDays() : 30);
                row.createCell(11).setCellValue(tp.getCurrentBalance() != null ? tp.getCurrentBalance().doubleValue() : 0.0);
                row.createCell(12).setCellValue(tp.getIsActive() != null && tp.getIsActive() 
                    ? bundle.getString("report.thirdparty.active") 
                    : bundle.getString("report.thirdparty.inactive"));
            }

            // Auto-size columns
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            try (java.io.FileOutputStream fos = new java.io.FileOutputStream(file)) {
                workbook.write(fos);
            }
            
            logger.info("Exported {} third parties to Excel: {}", thirdParties.size(), file.getAbsolutePath());
        }
    }

    /**
     * Exports third party transactions to an Excel file.
     *
     * @param thirdParty the third party whose transactions are being exported
     * @param transactions the list of transactions to export
     * @param stage the parent stage for the file chooser dialog
     * @throws IOException if an error occurs during Excel generation
     */
    public void exportThirdPartyTransactionsToExcel(ThirdParty thirdParty, List<Transaction> transactions, Stage stage) throws IOException {
        ResourceBundle bundle = getBundle();
        
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle(bundle.getString("report.transaction.save.title"));
        fileChooser.setInitialFileName("transactions_" + thirdParty.getIdentificationNumber() + "_" + 
            java.time.LocalDate.now().toString() + ".xlsx");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Excel Files", "*.xlsx"));
        
        java.io.File file = fileChooser.showSaveDialog(stage);
        if (file == null) {
            return; // User cancelled
        }
        
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Transactions");
            
            // Create header row
            Row headerRow = sheet.createRow(0);
            String[] headers = {
                bundle.getString("report.transaction.date"),
                bundle.getString("report.transaction.voucher.type"),
                bundle.getString("report.transaction.voucher.number"),
                bundle.getString("report.voucher.description"),
                bundle.getString("report.voucher.debit"),
                bundle.getString("report.voucher.credit"),
                bundle.getString("report.transaction.balance")
            };
            
            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }
            
            // Add third party info as a comment
            CreationHelper createHelper = workbook.getCreationHelper();
            ClientAnchor anchor = createHelper.createClientAnchor();
            anchor.setCol1(0);
            anchor.setRow1(0);
            anchor.setCol2(3);
            anchor.setRow2(3);
            Drawing<?> drawing = sheet.createDrawingPatriarch();
            RichTextString richText = createHelper.createRichTextString(
                bundle.getString("report.voucher.third.party") + ": " + thirdParty.getName() + 
                "\n" + bundle.getString("report.thirdparty.id.number") + ": " + thirdParty.getIdentificationNumber() + 
                "\n" + bundle.getString("report.voucher.third.party.type") + ": " + thirdParty.getType());
            Comment comment = drawing.createCellComment(anchor);
            comment.setString(richText);
            comment.setAuthor("EconoNova FX");
            
            // Data rows with running balance
            int rowNum = 1;
            BigDecimal runningBalance = BigDecimal.ZERO;
            java.time.format.DateTimeFormatter dateFormatter = java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd");
            
            for (Transaction transaction : transactions) {
                Row row = sheet.createRow(rowNum++);
                
                row.createCell(0).setCellValue(transaction.getDate().format(dateFormatter));
                row.createCell(1).setCellValue(transaction.getType());
                row.createCell(2).setCellValue(transaction.getNumber());
                row.createCell(3).setCellValue(transaction.getDescription() != null ? transaction.getDescription() : "");
                
                BigDecimal debit = transaction.getTotalDebit() != null ? transaction.getTotalDebit() : BigDecimal.ZERO;
                BigDecimal credit = transaction.getTotalCredit() != null ? transaction.getTotalCredit() : BigDecimal.ZERO;
                
                row.createCell(4).setCellValue(debit.doubleValue());
                row.createCell(5).setCellValue(credit.doubleValue());
                
                runningBalance = runningBalance.add(debit).subtract(credit);
                row.createCell(6).setCellValue(runningBalance.doubleValue());
            }
            
            // Auto-size columns
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }
            
            try (java.io.FileOutputStream fos = new java.io.FileOutputStream(file)) {
                workbook.write(fos);
            }
            
            logger.info("Exported {} transactions to Excel for third party {}: {}", 
                transactions.size(), thirdParty.getName(), file.getAbsolutePath());
        }
    }

    /**
     * Export bank reconciliation to PDF format.
     * @param reconciliation the bank reconciliation to export
     * @return byte array containing the PDF content
     * @throws IOException if an error occurs during PDF generation
     */
    public byte[] exportBankReconciliationToPdf(BankReconciliation reconciliation) throws IOException {
        ResourceBundle bundle = getBundle();
        
        try (PDDocument document = new PDDocument()) {
            PDPage page = new PDPage();
            document.addPage(page);

            PDPageContentStream contentStream = new PDPageContentStream(document, page);
            
            // Title - Informe de Conciliación Bancaria
            contentStream.beginText();
            contentStream.setFont(PDType1Font.HELVETICA_BOLD, 16);
            contentStream.newLineAtOffset(50, 750);
            contentStream.showText(bundle.getString("report.reconciliation.title"));
            contentStream.endText();

            // Reconciliation details
            contentStream.beginText();
            contentStream.setFont(PDType1Font.HELVETICA, 12);
            contentStream.newLineAtOffset(50, 710);
            contentStream.showText(bundle.getString("report.reconciliation.number") + ": " + reconciliation.getReconciliationNumber());
            contentStream.newLineAtOffset(0, -20);
            contentStream.showText(bundle.getString("report.reconciliation.bank.account.id") + ": " + reconciliation.getBankAccountId());
            contentStream.newLineAtOffset(0, -20);
            contentStream.showText(bundle.getString("report.reconciliation.statement.date") + ": " + reconciliation.getStatementDate().format(DATE_FORMATTER));
            contentStream.newLineAtOffset(0, -20);
            contentStream.showText(bundle.getString("report.reconciliation.status") + ": " + reconciliation.getStatus());
            contentStream.newLineAtOffset(0, -20);
            if (reconciliation.getCompletedBy() != null) {
                contentStream.showText(bundle.getString("report.reconciliation.completed.by") + ": " + reconciliation.getCompletedBy());
                contentStream.newLineAtOffset(0, -20);
            }
            if (reconciliation.getCompletedAt() != null) {
                contentStream.showText(bundle.getString("report.reconciliation.completed.at") + ": " + reconciliation.getCompletedAt().format(DATETIME_FORMATTER));
                contentStream.newLineAtOffset(0, -30);
            } else {
                contentStream.newLineAtOffset(0, -30);
            }

            // Balances summary
            contentStream.setFont(PDType1Font.HELVETICA_BOLD, 12);
            contentStream.showText(bundle.getString("report.reconciliation.balance.summary"));
            
            contentStream.setFont(PDType1Font.HELVETICA, 10);
            contentStream.newLineAtOffset(0, -20);
            contentStream.showText(bundle.getString("report.reconciliation.system.balance") + ": " + reconciliation.getSystemBalance().toPlainString());
            contentStream.newLineAtOffset(0, -15);
            contentStream.showText(bundle.getString("report.reconciliation.bank.balance") + ": " + reconciliation.getBankBalance().toPlainString());
            contentStream.newLineAtOffset(0, -15);
            if (reconciliation.getReconciledBalance() != null) {
                contentStream.showText(bundle.getString("report.reconciliation.reconciled.balance") + ": " + reconciliation.getReconciledBalance().toPlainString());
            }
            contentStream.endText();

            // System Items Table Header
            float yPosition = 450;
            contentStream.beginText();
            contentStream.setFont(PDType1Font.HELVETICA_BOLD, 11);
            contentStream.newLineAtOffset(50, yPosition);
            contentStream.showText(bundle.getString("report.reconciliation.system.items"));
            contentStream.endText();
            
            yPosition -= 25;
            contentStream.beginText();
            contentStream.setFont(PDType1Font.HELVETICA_BOLD, 9);
            contentStream.newLineAtOffset(50, yPosition);
            contentStream.showText(bundle.getString("report.voucher.date"));
            contentStream.newLineAtOffset(80, 0);
            contentStream.showText(bundle.getString("report.voucher.description"));
            contentStream.newLineAtOffset(200, 0);
            contentStream.showText(bundle.getString("report.transaction.balance"));
            contentStream.newLineAtOffset(80, 0);
            contentStream.showText(bundle.getString("report.reconciliation.reconciled"));
            contentStream.endText();
            
            yPosition -= 20;
            contentStream.setFont(PDType1Font.HELVETICA, 9);
            
            for (ReconciliationItem item : reconciliation.getSystemItems()) {
                if (yPosition < 250) {
                    // Add new page if needed
                    page = new PDPage();
                    document.addPage(page);
                    contentStream.close();
                    contentStream = new PDPageContentStream(document, page);
                    yPosition = 700;
                }
                
                contentStream.beginText();
                contentStream.newLineAtOffset(50, yPosition);
                contentStream.showText(item.getDate().format(DATE_FORMATTER));
                contentStream.newLineAtOffset(80, 0);
                String desc = item.getDescription() != null ? item.getDescription() : "";
                if (desc.length() > 25) {
                    desc = desc.substring(0, 22) + "...";
                }
                contentStream.showText(desc);
                contentStream.newLineAtOffset(200, 0);
                contentStream.showText(item.getAmount().toPlainString());
                contentStream.newLineAtOffset(80, 0);
                contentStream.showText(item.getReconciled() ? bundle.getString("report.reconciliation.yes") : bundle.getString("report.reconciliation.no"));
                contentStream.endText();
                
                yPosition -= 15;
            }

            // Bank Items Table Header
            yPosition -= 30;
            if (yPosition < 200) {
                page = new PDPage();
                document.addPage(page);
                contentStream.close();
                contentStream = new PDPageContentStream(document, page);
                yPosition = 700;
            }
            
            contentStream.beginText();
            contentStream.setFont(PDType1Font.HELVETICA_BOLD, 11);
            contentStream.newLineAtOffset(50, yPosition);
            contentStream.showText(bundle.getString("report.reconciliation.bank.items"));
            contentStream.endText();
            
            yPosition -= 25;
            contentStream.beginText();
            contentStream.setFont(PDType1Font.HELVETICA_BOLD, 9);
            contentStream.newLineAtOffset(50, yPosition);
            contentStream.showText(bundle.getString("report.voucher.date"));
            contentStream.newLineAtOffset(80, 0);
            contentStream.showText(bundle.getString("report.reconciliation.reference"));
            contentStream.newLineAtOffset(100, 0);
            contentStream.showText(bundle.getString("report.voucher.description"));
            contentStream.newLineAtOffset(150, 0);
            contentStream.showText(bundle.getString("report.transaction.balance"));
            contentStream.newLineAtOffset(80, 0);
            contentStream.showText(bundle.getString("report.reconciliation.reconciled"));
            contentStream.endText();
            
            yPosition -= 20;
            contentStream.setFont(PDType1Font.HELVETICA, 9);
            
            for (ReconciliationItem item : reconciliation.getBankItems()) {
                if (yPosition < 250) {
                    // Add new page if needed
                    page = new PDPage();
                    document.addPage(page);
                    contentStream.close();
                    contentStream = new PDPageContentStream(document, page);
                    yPosition = 700;
                }
                
                contentStream.beginText();
                contentStream.newLineAtOffset(50, yPosition);
                contentStream.showText(item.getDate().format(DATE_FORMATTER));
                contentStream.newLineAtOffset(80, 0);
                String ref = item.getBankReference() != null ? item.getBankReference() : "";
                contentStream.showText(ref);
                contentStream.newLineAtOffset(100, 0);
                String desc = item.getDescription() != null ? item.getDescription() : "";
                if (desc.length() > 18) {
                    desc = desc.substring(0, 15) + "...";
                }
                contentStream.showText(desc);
                contentStream.newLineAtOffset(150, 0);
                contentStream.showText(item.getAmount().toPlainString());
                contentStream.newLineAtOffset(80, 0);
                contentStream.showText(item.getReconciled() ? bundle.getString("report.reconciliation.yes") : bundle.getString("report.reconciliation.no"));
                contentStream.endText();
                
                yPosition -= 15;
            }

            contentStream.close();
            
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            document.save(baos);
            
            logger.info("Exported bank reconciliation {} to PDF", reconciliation.getReconciliationNumber());
            return baos.toByteArray();
        }
    }

    /**
     * Export Balance Sheet (Balance General) to PDF
     * @param accounts list of all accounts
     * @param startDate start date of the period
     * @param endDate end date of the period
     * @return byte array containing the PDF content
     * @throws IOException if an error occurs during PDF generation
     */
    public byte[] exportBalanceSheetToPdf(List<Account> accounts, LocalDate startDate, LocalDate endDate) throws IOException {
        ResourceBundle bundle = getBundle();
        
        try (PDDocument document = new PDDocument()) {
            PDPage page = new PDPage();
            document.addPage(page);

            PDPageContentStream contentStream = new PDPageContentStream(document, page);
            
            // Title - Balance General
            contentStream.beginText();
            contentStream.setFont(PDType1Font.HELVETICA_BOLD, 16);
            contentStream.newLineAtOffset(50, 750);
            contentStream.showText(bundle.getString("report.balance_sheet.title"));
            contentStream.endText();
            
            // Date range
            contentStream.beginText();
            contentStream.setFont(PDType1Font.HELVETICA, 12);
            contentStream.newLineAtOffset(50, 720);
            String dateRange = String.format(bundle.getString("report.period"), 
                startDate.format(DATE_FORMATTER), endDate.format(DATE_FORMATTER));
            contentStream.showText(dateRange);
            contentStream.endText();
            
            float yPosition = 680;
            
            // Calculate balances by account type
            BigDecimal totalAssets = BigDecimal.ZERO;
            BigDecimal totalLiabilities = BigDecimal.ZERO;
            BigDecimal totalEquity = BigDecimal.ZERO;
            
            // Assets section
            contentStream.beginText();
            contentStream.setFont(PDType1Font.HELVETICA_BOLD, 14);
            contentStream.newLineAtOffset(50, yPosition);
            contentStream.showText(bundle.getString("account.type.ASSET"));
            contentStream.endText();
            yPosition -= 25;
            
            List<Account> assetAccounts = accounts.stream()
                .filter(a -> a.getType() == AccountType.ASSET)
                .collect(Collectors.toList());
            
            for (Account account : assetAccounts) {
                BigDecimal balance = calculateAccountBalance(account, startDate, endDate);
                if (balance.compareTo(BigDecimal.ZERO) != 0) {
                    contentStream.beginText();
                    contentStream.setFont(PDType1Font.HELVETICA, 11);
                    contentStream.newLineAtOffset(60, yPosition);
                    contentStream.showText(account.getCode() + " - " + account.getName());
                    contentStream.newLineAtOffset(350, 0);
                    contentStream.showText(balance.setScale(2, RoundingMode.HALF_UP).toPlainString());
                    contentStream.endText();
                    yPosition -= 18;
                    totalAssets = totalAssets.add(balance);
                }
            }
            
            contentStream.beginText();
            contentStream.setFont(PDType1Font.HELVETICA_BOLD, 12);
            contentStream.newLineAtOffset(60, yPosition);
            contentStream.showText(bundle.getString("report.balance_sheet.total_assets"));
            contentStream.newLineAtOffset(350, 0);
            contentStream.showText(totalAssets.setScale(2, RoundingMode.HALF_UP).toPlainString());
            contentStream.endText();
            yPosition -= 30;
            
            // Liabilities section
            contentStream.beginText();
            contentStream.setFont(PDType1Font.HELVETICA_BOLD, 14);
            contentStream.newLineAtOffset(50, yPosition);
            contentStream.showText(bundle.getString("account.type.LIABILITY"));
            contentStream.endText();
            yPosition -= 25;
            
            List<Account> liabilityAccounts = accounts.stream()
                .filter(a -> a.getType() == AccountType.LIABILITY)
                .collect(Collectors.toList());
            
            for (Account account : liabilityAccounts) {
                BigDecimal balance = calculateAccountBalance(account, startDate, endDate);
                if (balance.compareTo(BigDecimal.ZERO) != 0) {
                    contentStream.beginText();
                    contentStream.setFont(PDType1Font.HELVETICA, 11);
                    contentStream.newLineAtOffset(60, yPosition);
                    contentStream.showText(account.getCode() + " - " + account.getName());
                    contentStream.newLineAtOffset(350, 0);
                    contentStream.showText(balance.setScale(2, RoundingMode.HALF_UP).toPlainString());
                    contentStream.endText();
                    yPosition -= 18;
                    totalLiabilities = totalLiabilities.add(balance);
                }
            }
            
            contentStream.beginText();
            contentStream.setFont(PDType1Font.HELVETICA_BOLD, 12);
            contentStream.newLineAtOffset(60, yPosition);
            contentStream.showText(bundle.getString("report.balance_sheet.total_liabilities"));
            contentStream.newLineAtOffset(350, 0);
            contentStream.showText(totalLiabilities.setScale(2, RoundingMode.HALF_UP).toPlainString());
            contentStream.endText();
            yPosition -= 30;
            
            // Equity section
            contentStream.beginText();
            contentStream.setFont(PDType1Font.HELVETICA_BOLD, 14);
            contentStream.newLineAtOffset(50, yPosition);
            contentStream.showText(bundle.getString("account.type.EQUITY"));
            contentStream.endText();
            yPosition -= 25;
            
            List<Account> equityAccounts = accounts.stream()
                .filter(a -> a.getType() == AccountType.EQUITY)
                .collect(Collectors.toList());
            
            for (Account account : equityAccounts) {
                BigDecimal balance = calculateAccountBalance(account, startDate, endDate);
                if (balance.compareTo(BigDecimal.ZERO) != 0) {
                    contentStream.beginText();
                    contentStream.setFont(PDType1Font.HELVETICA, 11);
                    contentStream.newLineAtOffset(60, yPosition);
                    contentStream.showText(account.getCode() + " - " + account.getName());
                    contentStream.newLineAtOffset(350, 0);
                    contentStream.showText(balance.setScale(2, RoundingMode.HALF_UP).toPlainString());
                    contentStream.endText();
                    yPosition -= 18;
                    totalEquity = totalEquity.add(balance);
                }
            }
            
            contentStream.beginText();
            contentStream.setFont(PDType1Font.HELVETICA_BOLD, 12);
            contentStream.newLineAtOffset(60, yPosition);
            contentStream.showText(bundle.getString("report.balance_sheet.total_equity"));
            contentStream.newLineAtOffset(350, 0);
            contentStream.showText(totalEquity.setScale(2, RoundingMode.HALF_UP).toPlainString());
            contentStream.endText();
            yPosition -= 30;
            
            // Accounting equation verification
            contentStream.beginText();
            contentStream.setFont(PDType1Font.HELVETICA_BOLD, 12);
            contentStream.newLineAtOffset(50, yPosition);
            contentStream.showText(bundle.getString("report.balance_sheet.equation_check"));
            contentStream.newLineAtOffset(200, 0);
            BigDecimal liabilitiesAndEquity = totalLiabilities.add(totalEquity);
            String equationStatus = totalAssets.compareTo(liabilitiesAndEquity) == 0 ? 
                bundle.getString("report.balance_sheet.balanced") : bundle.getString("report.balance_sheet.not_balanced");
            contentStream.showText(equationStatus);
            contentStream.endText();
            
            contentStream.close();
            
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            document.save(baos);
            
            logger.info("Exported Balance Sheet to PDF for period {} to {}", startDate, endDate);
            return baos.toByteArray();
        }
    }

    /**
     * Export Income Statement (Estado de Resultados) to PDF
     * @param accounts list of all accounts
     * @param startDate start date of the period
     * @param endDate end date of the period
     * @return byte array containing the PDF content
     * @throws IOException if an error occurs during PDF generation
     */
    public byte[] exportIncomeStatementToPdf(List<Account> accounts, LocalDate startDate, LocalDate endDate) throws IOException {
        ResourceBundle bundle = getBundle();
        
        try (PDDocument document = new PDDocument()) {
            PDPage page = new PDPage();
            document.addPage(page);

            PDPageContentStream contentStream = new PDPageContentStream(document, page);
            
            // Title - Estado de Resultados
            contentStream.beginText();
            contentStream.setFont(PDType1Font.HELVETICA_BOLD, 16);
            contentStream.newLineAtOffset(50, 750);
            contentStream.showText(bundle.getString("report.income_statement.title"));
            contentStream.endText();
            
            // Date range
            contentStream.beginText();
            contentStream.setFont(PDType1Font.HELVETICA, 12);
            contentStream.newLineAtOffset(50, 720);
            String dateRange = String.format(bundle.getString("report.period"), 
                startDate.format(DATE_FORMATTER), endDate.format(DATE_FORMATTER));
            contentStream.showText(dateRange);
            contentStream.endText();
            
            float yPosition = 680;
            
            // Calculate balances
            BigDecimal totalRevenue = BigDecimal.ZERO;
            BigDecimal totalExpenses = BigDecimal.ZERO;
            
            // Revenue section
            contentStream.beginText();
            contentStream.setFont(PDType1Font.HELVETICA_BOLD, 14);
            contentStream.newLineAtOffset(50, yPosition);
            contentStream.showText(bundle.getString("account.type.REVENUE"));
            contentStream.endText();
            yPosition -= 25;
            
            List<Account> revenueAccounts = accounts.stream()
                .filter(a -> a.getType() == AccountType.REVENUE)
                .collect(Collectors.toList());
            
            for (Account account : revenueAccounts) {
                BigDecimal balance = calculateAccountBalance(account, startDate, endDate);
                if (balance.compareTo(BigDecimal.ZERO) != 0) {
                    contentStream.beginText();
                    contentStream.setFont(PDType1Font.HELVETICA, 11);
                    contentStream.newLineAtOffset(60, yPosition);
                    contentStream.showText(account.getCode() + " - " + account.getName());
                    contentStream.newLineAtOffset(350, 0);
                    contentStream.showText(balance.setScale(2, RoundingMode.HALF_UP).toPlainString());
                    contentStream.endText();
                    yPosition -= 18;
                    totalRevenue = totalRevenue.add(balance);
                }
            }
            
            contentStream.beginText();
            contentStream.setFont(PDType1Font.HELVETICA_BOLD, 12);
            contentStream.newLineAtOffset(60, yPosition);
            contentStream.showText(bundle.getString("report.income_statement.total_revenue"));
            contentStream.newLineAtOffset(350, 0);
            contentStream.showText(totalRevenue.setScale(2, RoundingMode.HALF_UP).toPlainString());
            contentStream.endText();
            yPosition -= 30;
            
            // Expenses section
            contentStream.beginText();
            contentStream.setFont(PDType1Font.HELVETICA_BOLD, 14);
            contentStream.newLineAtOffset(50, yPosition);
            contentStream.showText(bundle.getString("account.type.EXPENSE"));
            contentStream.endText();
            yPosition -= 25;
            
            List<Account> expenseAccounts = accounts.stream()
                .filter(a -> a.getType() == AccountType.EXPENSE)
                .collect(Collectors.toList());
            
            for (Account account : expenseAccounts) {
                BigDecimal balance = calculateAccountBalance(account, startDate, endDate);
                if (balance.compareTo(BigDecimal.ZERO) != 0) {
                    contentStream.beginText();
                    contentStream.setFont(PDType1Font.HELVETICA, 11);
                    contentStream.newLineAtOffset(60, yPosition);
                    contentStream.showText(account.getCode() + " - " + account.getName());
                    contentStream.newLineAtOffset(350, 0);
                    contentStream.showText(balance.setScale(2, RoundingMode.HALF_UP).toPlainString());
                    contentStream.endText();
                    yPosition -= 18;
                    totalExpenses = totalExpenses.add(balance);
                }
            }
            
            contentStream.beginText();
            contentStream.setFont(PDType1Font.HELVETICA_BOLD, 12);
            contentStream.newLineAtOffset(60, yPosition);
            contentStream.showText(bundle.getString("report.income_statement.total_expenses"));
            contentStream.newLineAtOffset(350, 0);
            contentStream.showText(totalExpenses.setScale(2, RoundingMode.HALF_UP).toPlainString());
            contentStream.endText();
            yPosition -= 30;
            
            // Net Income/Loss
            BigDecimal netIncome = totalRevenue.subtract(totalExpenses);
            contentStream.beginText();
            contentStream.setFont(PDType1Font.HELVETICA_BOLD, 14);
            contentStream.newLineAtOffset(50, yPosition);
            contentStream.showText(bundle.getString("report.income_statement.net_income"));
            contentStream.newLineAtOffset(350, 0);
            String resultText = netIncome.compareTo(BigDecimal.ZERO) >= 0 ? 
                bundle.getString("report.income_statement.profit") : bundle.getString("report.income_statement.loss");
            contentStream.showText(resultText + ": " + netIncome.abs().setScale(2, RoundingMode.HALF_UP).toPlainString());
            contentStream.endText();
            
            contentStream.close();
            
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            document.save(baos);
            
            logger.info("Exported Income Statement to PDF for period {} to {}", startDate, endDate);
            return baos.toByteArray();
        }
    }

    /**
     * Calculate the balance of an account for a given period.
     * This is a helper method that should be replaced with actual service calls.
     * For now, it returns a placeholder value.
     */
    private BigDecimal calculateAccountBalance(Account account, LocalDate startDate, LocalDate endDate) {
        // TODO: Implement actual balance calculation using TransactionService
        // This is a placeholder that returns zero
        return BigDecimal.ZERO;
    }
}
