package com.econovafx.core.service;

import io.ebean.DB;
import io.ebean.Database;
import jakarta.inject.Singleton;

import java.time.LocalDate;
import java.util.*;

/**
 * Servicio para generar reportes contables obligatorios según Resolución 340/2004
 */
@Singleton
public class AccountingReportService {

    private final Database database;

    public AccountingReportService() {
        this.database = DB.getDefault();
    }

    /**
     * Genera Balance de Comprobación
     * @param startDate Fecha inicial del período
     * @param endDate Fecha final del período
     * @param includeSubAccounts Si incluye subcuentas en el reporte
     * @return Lista de mapas con información del balance
     */
    public List<Map<String, Object>> generateTrialBalance(LocalDate startDate, LocalDate endDate, boolean includeSubAccounts) {
        List<Map<String, Object>> result = new ArrayList<>();
        
        String sql = """
            SELECT 
                a.code as account_code,
                a.name as account_name,
                a.type as account_type,
                COALESCE(SUM(CASE WHEN te.debit > 0 THEN te.debit ELSE 0 END), 0) as total_debit,
                COALESCE(SUM(CASE WHEN te.credit > 0 THEN te.credit ELSE 0 END), 0) as total_credit,
                COALESCE(SUM(CASE WHEN te.debit > 0 THEN te.debit ELSE -te.credit END), 0) as net_balance
            FROM account a
            LEFT JOIN sub_account sa ON a.id = sa.account_id
            LEFT JOIN transaction_entry te ON (te.account_id = a.id OR te.sub_account_id = sa.id)
            LEFT JOIN transaction t ON te.transaction_id = t.id
            WHERE t.posted = true
              AND t.date >= ?
              AND t.date <= ?
            GROUP BY a.id, a.code, a.name, a.type
            ORDER BY a.code
        """;
        
        List<Object[]> rows = database.findNative(Object[].class, sql)
            .setParameter(1, startDate)
            .setParameter(2, endDate)
            .findList();
        
        for (Object[] row : rows) {
            Map<String, Object> item = new HashMap<>();
            item.put("accountCode", row[0]);
            item.put("accountName", row[1]);
            item.put("accountType", row[2]);
            item.put("totalDebit", ((Number) row[3]).doubleValue());
            item.put("totalCredit", ((Number) row[4]).doubleValue());
            item.put("netBalance", ((Number) row[5]).doubleValue());
            result.add(item);
        }
        
        return result;
    }

    /**
     * Genera Libro Mayor para una cuenta específica
     * @param accountId ID de la cuenta
     * @param startDate Fecha inicial
     * @param endDate Fecha final
     * @return Lista de movimientos detallados
     */
    public List<Map<String, Object>> generateGeneralLedger(Long accountId, LocalDate startDate, LocalDate endDate) {
        List<Map<String, Object>> result = new ArrayList<>();
        
        String sql = """
            SELECT 
                t.id as transaction_id,
                t.voucher_number as voucher_number,
                t.date as transaction_date,
                t.description as description,
                te.debit as debit,
                te.credit as credit,
                te.debit - te.credit as balance,
                a.code as account_code,
                a.name as account_name,
                sa.code as sub_account_code,
                sa.name as sub_account_name
            FROM transaction t
            JOIN transaction_entry te ON t.id = te.transaction_id
            JOIN account a ON te.account_id = a.id
            LEFT JOIN sub_account sa ON te.sub_account_id = sa.id
            WHERE t.posted = true
              AND (te.account_id = ? OR te.sub_account_id IN (SELECT id FROM sub_account WHERE account_id = ?))
              AND t.date >= ?
              AND t.date <= ?
            ORDER BY t.date, t.id, te.id
        """;
        
        List<Object[]> rows = database.findNative(Object[].class, sql)
            .setParameter(1, accountId)
            .setParameter(2, accountId)
            .setParameter(3, startDate)
            .setParameter(4, endDate)
            .findList();
        
        double runningBalance = 0.0;
        for (Object[] row : rows) {
            Map<String, Object> item = new HashMap<>();
            item.put("transactionId", row[0]);
            item.put("voucherNumber", row[1]);
            item.put("date", row[2]);
            item.put("description", row[3]);
            item.put("debit", ((Number) row[4]).doubleValue());
            item.put("credit", ((Number) row[5]).doubleValue());
            runningBalance += ((Number) row[4]).doubleValue() - ((Number) row[5]).doubleValue();
            item.put("runningBalance", runningBalance);
            item.put("accountCode", row[7]);
            item.put("accountName", row[8]);
            item.put("subAccountCode", row[9]);
            item.put("subAccountName", row[10]);
            result.add(item);
        }
        
        return result;
    }

    /**
     * Genera Fichero Histórico de Comprobantes
     * @param startDate Fecha inicial
     * @param endDate Fecha final
     * @param accountCodeFilter Filtro opcional por código de cuenta
     * @return Lista de comprobantes con sus partidas
     */
    public List<Map<String, Object>> generateVoucherHistory(LocalDate startDate, LocalDate endDate, String accountCodeFilter) {
        List<Map<String, Object>> result = new ArrayList<>();
        
        StringBuilder sql = new StringBuilder("""
            SELECT 
                t.id as transaction_id,
                t.voucher_number as voucher_number,
                t.date as transaction_date,
                t.description as description,
                t.source_module as source_module,
                te.debit as debit,
                te.credit as credit,
                a.code as account_code,
                a.name as account_name,
                sa.code as sub_account_code,
                sa.name as sub_account_name
            FROM transaction t
            JOIN transaction_entry te ON t.id = te.transaction_id
            JOIN account a ON te.account_id = a.id
            LEFT JOIN sub_account sa ON te.sub_account_id = sa.id
            WHERE t.posted = true
              AND t.date >= ?
              AND t.date <= ?
        """);
        
        if (accountCodeFilter != null && !accountCodeFilter.isEmpty()) {
            sql.append(" AND (a.code LIKE ? OR sa.code LIKE ?)");
        }
        
        sql.append(" ORDER BY t.date, t.voucher_number, te.id");
        
        var query = database.findNative(Object[].class, sql.toString())
            .setParameter(1, startDate)
            .setParameter(2, endDate);
        
        if (accountCodeFilter != null && !accountCodeFilter.isEmpty()) {
            String likePattern = "%" + accountCodeFilter + "%";
            query.setParameter(3, likePattern)
                 .setParameter(4, likePattern);
        }
        
        List<Object[]> rows = query.findList();
        
        Map<Long, Map<String, Object>> voucherMap = new LinkedHashMap<>();
        
        for (Object[] row : rows) {
            Long transactionId = ((Number) row[0]).longValue();
            
            if (!voucherMap.containsKey(transactionId)) {
                Map<String, Object> voucher = new HashMap<>();
                voucher.put("transactionId", transactionId);
                voucher.put("voucherNumber", row[1]);
                voucher.put("date", row[2]);
                voucher.put("description", row[3]);
                voucher.put("sourceModule", row[4]);
                voucher.put("entries", new ArrayList<Map<String, Object>>());
                voucher.put("totalDebit", 0.0);
                voucher.put("totalCredit", 0.0);
                voucherMap.put(transactionId, voucher);
            }
            
            Map<String, Object> entry = new HashMap<>();
            entry.put("accountCode", row[7]);
            entry.put("accountName", row[8]);
            entry.put("subAccountCode", row[9]);
            entry.put("subAccountName", row[10]);
            entry.put("debit", ((Number) row[5]).doubleValue());
            entry.put("credit", ((Number) row[6]).doubleValue());
            
            Map<String, Object> voucher = voucherMap.get(transactionId);
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> entries = (List<Map<String, Object>>) voucher.get("entries");
            entries.add(entry);
            voucher.put("totalDebit", ((Double) voucher.get("totalDebit")) + ((Number) row[5]).doubleValue());
            voucher.put("totalCredit", ((Double) voucher.get("totalCredit")) + ((Number) row[6]).doubleValue());
        }
        
        result.addAll(voucherMap.values());
        return result;
    }

    /**
     * Verifica que un comprobante cuadre (Débito = Crédito)
     */
    public boolean validateVoucherBalance(Long transactionId) {
        String sql = """
            SELECT 
                COALESCE(SUM(debit), 0) as total_debit,
                COALESCE(SUM(credit), 0) as total_credit
            FROM transaction_entry
            WHERE transaction_id = ?
        """;
        
        List<Object[]> result = database.findNative(Object[].class, sql)
            .setParameter(1, transactionId)
            .findList();
        
        if (result.isEmpty()) {
            return false;
        }
        
        Object[] row = result.get(0);
        double debit = ((Number) row[0]).doubleValue();
        double credit = ((Number) row[1]).doubleValue();
        
        return Math.abs(debit - credit) < 0.01;
    }
}
