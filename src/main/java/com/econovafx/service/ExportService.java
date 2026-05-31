package com.econovafx.service;

import com.econovafx.domain.Transaction;
import com.econovafx.domain.TransactionEntry;
import com.econovafx.domain.ThirdParty;
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
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Service for exporting transactions to PDF and Excel formats
 */
@Singleton
public class ExportService {

    private static final Logger logger = LoggerFactory.getLogger(ExportService.class);
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * Export a single transaction to PDF
     * @param transaction the transaction to export
     * @return byte array containing the PDF content
     * @throws IOException if an error occurs during PDF generation
     */
    public byte[] exportTransactionToPdf(Transaction transaction) throws IOException {
        try (PDDocument document = new PDDocument()) {
            PDPage page = new PDPage();
            document.addPage(page);

            PDPageContentStream contentStream = new PDPageContentStream(document, page);
            
            // Title
            contentStream.beginText();
            contentStream.setFont(PDType1Font.HELVETICA_BOLD, 16);
            contentStream.newLineAtOffset(50, 750);
            contentStream.showText("Accounting Voucher - " + transaction.getNumber());
            contentStream.endText();

            // Transaction details
            contentStream.beginText();
            contentStream.setFont(PDType1Font.HELVETICA, 12);
            contentStream.newLineAtOffset(50, 700);
            contentStream.showText("Date: " + transaction.getDate().format(DATE_FORMATTER));
            contentStream.newLineAtOffset(0, -20);
            contentStream.showText("Type: " + transaction.getType());
            contentStream.newLineAtOffset(0, -20);
            contentStream.showText("Status: " + (transaction.getIsPosted() ? "Posted" : "Draft"));
            contentStream.newLineAtOffset(0, -20);
            contentStream.showText("Description: " + transaction.getDescription());
            
            // Third party info if exists
            if (transaction.getThirdParty() != null) {
                ThirdParty tp = transaction.getThirdParty();
                contentStream.newLineAtOffset(0, -30);
                contentStream.showText("Third Party: " + tp.getName() + " (" + tp.getIdentificationNumber() + ")");
                contentStream.newLineAtOffset(0, -20);
                contentStream.showText("Third Party Type: " + tp.getType());
            }
            
            contentStream.endText();

            // Table header
            float yPosition = 550;
            contentStream.beginText();
            contentStream.setFont(PDType1Font.HELVETICA_BOLD, 11);
            contentStream.newLineAtOffset(50, yPosition);
            contentStream.showText("Account Code");
            contentStream.newLineAtOffset(150, 0);
            contentStream.showText("Account Name");
            contentStream.newLineAtOffset(200, 0);
            contentStream.showText("Debit");
            contentStream.newLineAtOffset(100, 0);
            contentStream.showText("Credit");
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
            contentStream.showText("Total Debit: " + transaction.getTotalDebit().toPlainString());
            contentStream.newLineAtOffset(200, 0);
            contentStream.showText("Total Credit: " + transaction.getTotalCredit().toPlainString());
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
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Transactions");

            // Create header row
            Row headerRow = sheet.createRow(0);
            String[] headers = {"Number", "Date", "Type", "Status", "Description", 
                               "Third Party", "Third Party ID", "Account Code", 
                               "Account Name", "Debit", "Credit"};
            
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
                    row.createCell(3).setCellValue(transaction.getIsPosted() ? "Posted" : "Draft");
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
}
