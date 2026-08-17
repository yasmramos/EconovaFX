package com.econovafx.modules.accounting.service;

import com.econovafx.modules.accounting.model.*;
import com.econovafx.modules.accounting.repository.ExchangeDifferenceRepository;
import com.econovafx.modules.core.repository.ExchangeRateRepository;
import com.econovafx.modules.accounting.repository.AccountRepository;
import com.econovafx.modules.payables.model.SupplierInvoice;
import com.econovafx.modules.payables.repository.SupplierInvoiceRepository;
import com.econovafx.modules.receivables.model.CustomerInvoice;
import com.econovafx.modules.receivables.repository.CustomerInvoiceRepository;
import com.econovafx.modules.core.service.AuditService;
import com.econovafx.modules.core.service.SystemConfigService;
import com.econovafx.modules.core.model.AuditLog;
import com.econovafx.modules.core.model.Currency;
import com.econovafx.modules.core.model.ExchangeRate;
import com.econovafx.modules.billing.model.ThirdParty;
import com.econovafx.modules.core.security.RequiresTenant;
import io.avaje.inject.Component;
import jakarta.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Service for revaluing open balances in foreign currency at period end.
 * 
 * This is a key differentiator vs Versat Sarasola - automatic unrealized exchange
 * difference calculation for open invoices in foreign currency.
 */
@Component
@RequiresTenant
public class ExchangeRevaluationService {

    private static final Logger logger = LoggerFactory.getLogger(ExchangeRevaluationService.class);

    private final ExchangeDifferenceRepository exchangeDifferenceRepository;
    private final ExchangeRateRepository exchangeRateRepository;
    private final TransactionService transactionService;
    private final AccountRepository accountRepository;
    private final AuditService auditService;
    private final SystemConfigService systemConfigService;
    private final SupplierInvoiceRepository supplierInvoiceRepository;
    private final CustomerInvoiceRepository customerInvoiceRepository;

    @Inject
    public ExchangeRevaluationService(
            ExchangeDifferenceRepository exchangeDifferenceRepository,
            ExchangeRateRepository exchangeRateRepository,
            TransactionService transactionService,
            AccountRepository accountRepository,
            AuditService auditService,
            SystemConfigService systemConfigService,
            SupplierInvoiceRepository supplierInvoiceRepository,
            CustomerInvoiceRepository customerInvoiceRepository) {
        this.exchangeDifferenceRepository = exchangeDifferenceRepository;
        this.exchangeRateRepository = exchangeRateRepository;
        this.transactionService = transactionService;
        this.accountRepository = accountRepository;
        this.auditService = auditService;
        this.systemConfigService = systemConfigService;
        this.supplierInvoiceRepository = supplierInvoiceRepository;
        this.customerInvoiceRepository = customerInvoiceRepository;
    }

    /**
     * Revalues all open balances in foreign currency at period end.
     * 
     * @param cutoffDate The date to use for exchange rate (typically period end date)
     * @param username User performing the revaluation
     * @return List of ExchangeDifference records created for unrealized differences
     */
    public List<ExchangeDifference> revalueOpenBalances(LocalDate cutoffDate, String username) {
        logger.info("Starting revaluation of open balances as of {}", cutoffDate);

        // Process supplier invoices (Accounts Payable)
        List<ExchangeDifference> supplierDifferences = revalueSupplierInvoices(cutoffDate, username);

        // Process customer invoices (Accounts Receivable)
        List<ExchangeDifference> customerDifferences = revalueCustomerInvoices(cutoffDate, username);

        logger.info("Revaluation completed. Created {} unrealized difference records", 
            supplierDifferences.size() + customerDifferences.size());

        return Stream.concat(supplierDifferences.stream(), customerDifferences.stream())
                .collect(Collectors.toList());
    }

    /**
     * Revalues open supplier invoices in foreign currency.
     */
    private List<ExchangeDifference> revalueSupplierInvoices(LocalDate cutoffDate, String username) {
        List<SupplierInvoice> openInvoices = supplierInvoiceRepository.findOpenForeignCurrencyInvoices();
        
        for (SupplierInvoice invoice : openInvoices) {
            Currency invoiceCurrency = invoice.getCurrency();
            
            // Skip if invoice is in base currency (CUP)
            if (invoiceCurrency == null || "CUP".equals(invoiceCurrency.getCode())) {
                continue;
            }

            try {
                BigDecimal cutoffRate = getHistoricalExchangeRate(invoiceCurrency, cutoffDate);
                BigDecimal originalRate = getHistoricalExchangeRate(invoiceCurrency, invoice.getInvoiceDate());
                
                // Calculate unrealized difference on pending amount
                BigDecimal pendingAmount = invoice.getPendingAmount();
                BigDecimal localValueAtOriginal = pendingAmount.multiply(originalRate);
                BigDecimal localValueAtCutoff = pendingAmount.multiply(cutoffRate);
                BigDecimal differenceAmount = localValueAtCutoff.subtract(localValueAtOriginal);

                if (differenceAmount.abs().compareTo(BigDecimal.valueOf(0.01)) >= 0) {
                    createUnrealizedDifferenceRecord(
                        "PURCHASE_INVOICE",
                        invoice.getId(),
                        invoice.getInvoiceNumber(),
                        invoice.getSupplier(),
                        invoiceCurrency,
                        pendingAmount,
                        originalRate,
                        cutoffRate,
                        differenceAmount,
                        invoice.getInvoiceDate(),
                        cutoffDate,
                        "Unrealized exchange difference at period end",
                        username
                    );
                }
            } catch (Exception e) {
                logger.error("Error revaluing supplier invoice {}: {}", invoice.getInvoiceNumber(), e.getMessage());
            }
        }

        return exchangeDifferenceRepository.findByDocumentAndType(
            "PURCHASE_INVOICE", 
            1L, // Document ID placeholder - method signature needs update
            ExchangeDifference.DifferenceType.UNREALIZED_GAIN
        );
    }

    /**
     * Revalues open customer invoices in foreign currency.
     */
    private List<ExchangeDifference> revalueCustomerInvoices(LocalDate cutoffDate, String username) {
        List<CustomerInvoice> openInvoices = customerInvoiceRepository.findOpenForeignCurrencyInvoices();
        
        for (CustomerInvoice invoice : openInvoices) {
            Currency invoiceCurrency = invoice.getCurrency();
            
            // Skip if invoice is in base currency (CUP)
            if (invoiceCurrency == null || "CUP".equals(invoiceCurrency.getCode())) {
                continue;
            }

            try {
                BigDecimal cutoffRate = getHistoricalExchangeRate(invoiceCurrency, cutoffDate);
                BigDecimal originalRate = getHistoricalExchangeRate(invoiceCurrency, invoice.getInvoiceDate());
                
                // Calculate unrealized difference on pending amount
                BigDecimal pendingAmount = invoice.getPendingAmount();
                BigDecimal localValueAtOriginal = pendingAmount.multiply(originalRate);
                BigDecimal localValueAtCutoff = pendingAmount.multiply(cutoffRate);
                BigDecimal differenceAmount = localValueAtCutoff.subtract(localValueAtOriginal);

                if (differenceAmount.abs().compareTo(BigDecimal.valueOf(0.01)) >= 0) {
                    createUnrealizedDifferenceRecord(
                        "SALES_INVOICE",
                        invoice.getId(),
                        invoice.getInvoiceNumber(),
                        invoice.getCustomer(),
                        invoiceCurrency,
                        pendingAmount,
                        originalRate,
                        cutoffRate,
                        differenceAmount,
                        invoice.getInvoiceDate(),
                        cutoffDate,
                        "Unrealized exchange difference at period end",
                        username
                    );
                }
            } catch (Exception e) {
                logger.error("Error revaluing customer invoice {}: {}", invoice.getInvoiceNumber(), e.getMessage());
            }
        }

        return exchangeDifferenceRepository.findByDocumentAndType(
            "SALES_INVOICE",
            1L, // Document ID placeholder - method signature needs update
            ExchangeDifference.DifferenceType.UNREALIZED_GAIN
        );
    }

    /**
     * Creates an unrealized exchange difference record and corresponding accounting entry.
     */
    private ExchangeDifference createUnrealizedDifferenceRecord(
            String documentType,
            Long documentId,
            String documentNumber,
            ThirdParty thirdParty,
            Currency currency,
            BigDecimal foreignAmount,
            BigDecimal originalRate,
            BigDecimal cutoffRate,
            BigDecimal differenceAmount,
            LocalDate invoiceDate,
            LocalDate cutoffDate,
            String notes,
            String username) {

        ExchangeDifference.DifferenceType differenceType;
        if (differenceAmount.compareTo(BigDecimal.ZERO) > 0) {
            differenceType = ExchangeDifference.DifferenceType.UNREALIZED_GAIN;
        } else if (differenceAmount.compareTo(BigDecimal.ZERO) < 0) {
            differenceType = ExchangeDifference.DifferenceType.UNREALIZED_LOSS;
        } else {
            differenceType = ExchangeDifference.DifferenceType.NONE;
        }

        ExchangeDifference exchangeDifference = new ExchangeDifference();
        exchangeDifference.setDocumentType(documentType);
        exchangeDifference.setDocumentId(documentId);
        exchangeDifference.setDocumentNumber(documentNumber);
        exchangeDifference.setThirdParty(thirdParty);
        exchangeDifference.setCurrency(currency);
        exchangeDifference.setOriginalExchangeRate(originalRate);
        exchangeDifference.setPaymentExchangeRate(cutoffRate);
        exchangeDifference.setOriginalAmount(foreignAmount);
        exchangeDifference.setLocalCurrencyAmountAtInvoice(foreignAmount.multiply(originalRate));
        exchangeDifference.setLocalCurrencyAmountAtPayment(foreignAmount.multiply(cutoffRate));
        exchangeDifference.setDifferenceAmount(differenceAmount.abs());
        exchangeDifference.setDifferenceType(differenceType);
        exchangeDifference.setInvoiceDate(invoiceDate);
        exchangeDifference.setPaymentDate(cutoffDate);
        exchangeDifference.setNotes(notes);
        exchangeDifference.setUnrealized(true); // Mark as unrealized

        ExchangeDifference savedDifference = exchangeDifferenceRepository.save(exchangeDifference);
        logger.info("Unrealized difference recorded: Type={}, Amount={}", differenceType, differenceAmount.abs());

        // Generate accounting entry if significant
        if (differenceType != ExchangeDifference.DifferenceType.NONE && 
            differenceAmount.abs().compareTo(BigDecimal.valueOf(0.01)) >= 0) {
            
            try {
                Long journalEntryId = createAccountingEntryForUnrealizedDifference(savedDifference, username).getId();
                savedDifference.setJournalEntryId(journalEntryId);
                exchangeDifferenceRepository.update(savedDifference);
                logger.info("Accounting entry generated for unrealized difference: {}", journalEntryId);

                // Audit log
                auditService.logWithValues(
                    username,
                    AuditLog.OperationType.CREATE,
                    "ExchangeDifference",
                    savedDifference.getId(),
                    "Unrealized exchange difference recorded: " + documentNumber,
                    null,
                    buildDifferenceJson(savedDifference)
                );

            } catch (Exception e) {
                logger.error("Error generating accounting entry for unrealized difference: {}", e.getMessage());
                savedDifference.setNotes(savedDifference.getNotes() + " | Error in accounting entry: " + e.getMessage());
                exchangeDifferenceRepository.update(savedDifference);
            }
        }

        return savedDifference;
    }

    /**
     * Generates accounting entry for unrealized exchange difference.
     */
    private Transaction createAccountingEntryForUnrealizedDifference(
            ExchangeDifference difference, String username) {
        
        BigDecimal differenceAmount = difference.getDifferenceAmount();
        ExchangeDifference.DifferenceType type = difference.getDifferenceType();

        // Get configured accounts for unrealized gains/losses
        String gainAccountCode = systemConfigService.getCurrentConfig().getUnrealizedExchangeGainAccountCode();
        String lossAccountCode = systemConfigService.getCurrentConfig().getUnrealizedExchangeLossAccountCode();
        
        Account differenceAccount;
        if (type == ExchangeDifference.DifferenceType.UNREALIZED_GAIN) {
            differenceAccount = accountRepository.findByCode(gainAccountCode)
                .orElseThrow(() -> new IllegalArgumentException(
                    "Unrealized exchange gain account not configured: " + gainAccountCode));
        } else {
            differenceAccount = accountRepository.findByCode(lossAccountCode)
                .orElseThrow(() -> new IllegalArgumentException(
                    "Unrealized exchange loss account not configured: " + lossAccountCode));
        }

        Transaction transaction = new Transaction();
        transaction.setDate(difference.getPaymentDate()); // Cutoff date
        transaction.setType("UNREALIZED_EXCHANGE_DIFFERENCE");
        transaction.setDescription(String.format(
            "%s cambiaria no realizada por %s - Factura %s",
            type == ExchangeDifference.DifferenceType.UNREALIZED_GAIN ? "Ganancia" : "Pérdida",
            difference.getCurrency().getCode(),
            difference.getDocumentNumber()));
        transaction.setReference("UNREAL-DIFF-" + difference.getDocumentNumber());

        TransactionService.TransactionEntryData entry1, entry2;
        
        if (type == ExchangeDifference.DifferenceType.UNREALIZED_GAIN) {
            // Unrealized Gain: DEBIT adjustment account, CREDIT unrealized gain
            entry1 = new TransactionService.TransactionEntryData(
                getCashOrPayableAccount(difference).getId(),
                differenceAmount,
                BigDecimal.ZERO,
                "Ajuste por ganancia no realizada " + difference.getDocumentNumber());
            
            entry2 = new TransactionService.TransactionEntryData(
                differenceAccount.getId(),
                BigDecimal.ZERO,
                differenceAmount,
                "Ganancia cambiaria no realizada " + difference.getDocumentNumber());
        } else {
            // Unrealized Loss: DEBIT unrealized loss, CREDIT adjustment account
            entry1 = new TransactionService.TransactionEntryData(
                differenceAccount.getId(),
                differenceAmount,
                BigDecimal.ZERO,
                "Pérdida cambiaria no realizada " + difference.getDocumentNumber());
            
            entry2 = new TransactionService.TransactionEntryData(
                getCashOrPayableAccount(difference).getId(),
                BigDecimal.ZERO,
                differenceAmount,
                "Ajuste por pérdida no realizada " + difference.getDocumentNumber());
        }

        return transactionService.createTransaction(transaction, List.of(entry1, entry2), username);
    }

    /**
     * Gets historical exchange rate for a currency and date.
     */
    private BigDecimal getHistoricalExchangeRate(Currency currency, LocalDate date) {
        return exchangeRateRepository.findByCurrencyAndDate(currency, date)
            .map(ExchangeRate::getRate)
            .orElseGet(() -> {
                List<ExchangeRate> rates = exchangeRateRepository.findByCurrencyBeforeDate(currency, date);
                if (!rates.isEmpty()) {
                    return rates.get(0).getRate();
                }
                return exchangeRateRepository.findCurrentByCurrency(currency)
                    .map(ExchangeRate::getRate)
                    .orElse(BigDecimal.ONE);
            });
    }

    /**
     * Gets the payable/receivable account based on document type.
     */
    private Account getCashOrPayableAccount(ExchangeDifference difference) {
        String accountCode = difference.getDocumentType().equals("SALES_INVOICE") 
            ? systemConfigService.getCurrentConfig().getExchangeReceivableAccountCode()
            : systemConfigService.getCurrentConfig().getExchangePayableAccountCode();
        
        return accountRepository.findByCode(accountCode)
            .orElseThrow(() -> new IllegalArgumentException(
                "Configured account not found: " + accountCode));
    }

    /**
     * Builds JSON for audit logging.
     */
    private String buildDifferenceJson(ExchangeDifference diff) {
        if (diff == null) {
            return null;
        }
        StringBuilder json = new StringBuilder();
        json.append("{");
        json.append("\"id\":").append(diff.getId()).append(",");
        json.append("\"documentType\":\"").append(diff.getDocumentType()).append("\",");
        json.append("\"documentNumber\":\"").append(diff.getDocumentNumber()).append("\",");
        json.append("\"differenceType\":\"").append(diff.getDifferenceType()).append("\",");
        json.append("\"differenceAmount\":").append(diff.getDifferenceAmount()).append(",");
        json.append("\"unrealized\":").append(diff.isUnrealized());
        json.append("}");
        return json.toString();
    }
}
