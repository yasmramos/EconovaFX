package com.econovafx.service;

import com.econovafx.model.*;
import com.econovafx.repository.*;
import io.avaje.inject.Component;
import jakarta.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Servicio para gestión de depreciación de activos fijos.
 * Calcula y registra la depreciación mensual según el método lineal.
 * Cumple con los requisitos de la Resolución 340/2004.
 */
@Component
public class DepreciationService {

    private static final Logger logger = LoggerFactory.getLogger(DepreciationService.class);

    private final FixedAssetRepository fixedAssetRepository;
    private final DepreciationRecordRepository depreciationRecordRepository;
    private final TransactionService transactionService;
    private final AccountRepository accountRepository;
    private final AuditService auditService;

    @Inject
    public DepreciationService(
            FixedAssetRepository fixedAssetRepository,
            DepreciationRecordRepository depreciationRecordRepository,
            TransactionService transactionService,
            AccountRepository accountRepository,
            AuditService auditService) {
        this.fixedAssetRepository = fixedAssetRepository;
        this.depreciationRecordRepository = depreciationRecordRepository;
        this.transactionService = transactionService;
        this.accountRepository = accountRepository;
        this.auditService = auditService;
    }

    /**
     * Calcula la depreciación mensual de un activo usando el método lineal.
     * Fórmula: (Costo Adquisición - Valor Residual) / Vida Útil (meses)
     * 
     * @param asset Activo fijo
     * @return Monto de depreciación mensual
     */
    public BigDecimal calculateMonthlyDepreciation(FixedAsset asset) {
        BigDecimal acquisitionCost = asset.getAcquisitionCost();
        
        // Asumimos valor residual = 0 para simplificar (puede configurarse en el futuro)
        BigDecimal residualValue = BigDecimal.ZERO;
        
        // Obtener vida útil de la categoría del activo
        Integer usefulLifeYears = asset.getCategory().getUsefulLifeYears();
        if (usefulLifeYears == null || usefulLifeYears <= 0) {
            throw new IllegalArgumentException(
                "La categoría del activo debe tener una vida útil definida: " + asset.getCategory().getName());
        }
        
        int usefulLifeMonths = usefulLifeYears * 12;
        
        // Depreciación lineal mensual
        BigDecimal depreciableAmount = acquisitionCost.subtract(residualValue);
        return depreciableAmount.divide(BigDecimal.valueOf(usefulLifeMonths), 2, java.math.RoundingMode.HALF_UP);
    }

    /**
     * Procesa la depreciación mensual para todos los activos elegibles.
     * Crea registros de depreciación y genera asientos contables automáticos.
     * 
     * @param year Año de procesamiento
     * @param month Mes de procesamiento (1-12)
     * @param username Usuario que ejecuta el proceso
     * @return Lista de registros de depreciación creados
     */
    public List<DepreciationRecord> processMonthlyDepreciation(Integer year, Integer month, String username) {
        logger.info("Iniciando proceso de depreciación mensual: {}/{}", month, year);
        
        LocalDate processingDate = LocalDate.of(year, month, 1);
        
        // Verificar si ya se procesó este período
        var existingRecords = depreciationRecordRepository.findByYearAndMonth(year, month);
        if (!existingRecords.isEmpty()) {
            logger.warn("Ya existen registros de depreciación para {}/{}. Cantidad: {}", 
                month, year, existingRecords.size());
            throw new IllegalStateException(
                "La depreciación del período " + month + "/" + year + " ya fue procesada.");
        }
        
        // Obtener todos los activos elegibles para depreciación
        List<FixedAsset> assets = fixedAssetRepository.findAssetsForDepreciation();
        logger.info("Activos elegibles para depreciación: {}", assets.size());
        
        for (FixedAsset asset : assets) {
            try {
                processAssetDepreciation(asset, year, month, processingDate, username);
            } catch (Exception e) {
                logger.error("Error procesando depreciación del activo {}: {}", 
                    asset.getCode(), e.getMessage());
                // Continuar con el siguiente activo
            }
        }
        
        logger.info("Proceso de depreciación completado para {}/{}", month, year);
        
        return depreciationRecordRepository.findByYearAndMonth(year, month);
    }

    /**
     * Procesa la depreciación de un activo individual.
     */
    private void processAssetDepreciation(
            FixedAsset asset,
            Integer year,
            Integer month,
            LocalDate processingDate,
            String username) {
        
        // Verificar si ya existe registro para este activo y período
        var existingRecord = depreciationRecordRepository.findByAssetAndPeriod(asset.getId(), year, month);
        if (existingRecord.isPresent()) {
            logger.debug("Activo {} ya tiene depreciación registrada para {}/{}", 
                asset.getCode(), month, year);
            return;
        }
        
        // Calcular depreciación mensual
        BigDecimal monthlyDepreciation = calculateMonthlyDepreciation(asset);
        
        // Calcular depreciación acumulada actualizada
        BigDecimal currentAccumulatedDepreciation = asset.getAccumulatedDepreciation() != null 
            ? asset.getAccumulatedDepreciation() : BigDecimal.ZERO;
        BigDecimal newAccumulatedDepreciation = currentAccumulatedDepreciation.add(monthlyDepreciation);
        
        // Calcular valor en libros actualizado
        BigDecimal netBookValue = asset.getAcquisitionCost().subtract(newAccumulatedDepreciation);
        
        // Crear registro de depreciación
        DepreciationRecord record = new DepreciationRecord();
        record.setFixedAsset(asset);
        record.setYear(year);
        record.setMonth(month);
        record.setDepreciationAmount(monthlyDepreciation);
        record.setAccumulatedDepreciation(newAccumulatedDepreciation);
        record.setNetBookValue(netBookValue);
        record.setProcessingDate(processingDate);
        record.setPosted(false); // Pendiente de generar asiento contable
        record.setNotes("Depreciación automática mes " + month + "/" + year);
        
        depreciationRecordRepository.save(record);
        
        // Actualizar activo con nueva depreciación acumulada
        asset.setAccumulatedDepreciation(newAccumulatedDepreciation);
        asset.setNetBookValue(netBookValue);
        fixedAssetRepository.update(asset);
        
        logger.info("Depreciación registrada: Activo={}, Monto={}, Acumulada={}, Valor Libros={}",
            asset.getCode(), monthlyDepreciation, newAccumulatedDepreciation, netBookValue);
    }

    /**
     * Genera el asiento contable para un registro de depreciación.
     * Debe llamarse después de processMonthlyDepreciation.
     * 
     * @param recordId ID del registro de depreciación
     * @param username Usuario que ejecuta la operación
     * @return Transacción creada (asiento contable)
     */
    public Transaction postDepreciationToAccounting(Long recordId, String username) {
        DepreciationRecord record = depreciationRecordRepository.findById(recordId)
                .orElseThrow(() -> new IllegalArgumentException(
                    "Registro de depreciación no encontrado: " + recordId));
        
        if (record.isPosted()) {
            throw new IllegalStateException(
                "El registro de depreciación ya fue contabilizado: " + recordId);
        }
        
        FixedAsset asset = record.getFixedAsset();
        BigDecimal depreciationAmount = record.getDepreciationAmount();
        
        // Obtener cuentas contables desde la categoría del activo
        FixedAssetCategory category = asset.getCategory();
        
        // Cuenta de gasto por depreciación (debe)
        String expenseAccountCode = category.getDepreciationExpenseAccountCode();
        if (expenseAccountCode == null || expenseAccountCode.trim().isEmpty()) {
            throw new IllegalArgumentException(
                "La categoría del activo debe tener configurada la cuenta de gasto por depreciación");
        }
        
        // Cuenta de depreciación acumulada (haber)
        String accumulatedDepreciationAccountCode = category.getAccumulatedDepreciationAccountCode();
        if (accumulatedDepreciationAccountCode == null || accumulatedDepreciationAccountCode.trim().isEmpty()) {
            throw new IllegalArgumentException(
                "La categoría del activo debe tener configurada la cuenta de depreciación acumulada");
        }
        
        Account expenseAccount = accountRepository.findByCode(expenseAccountCode)
                .orElseThrow(() -> new IllegalArgumentException(
                    "Cuenta de gasto no encontrada: " + expenseAccountCode));
        
        Account accumulatedDepreciationAccount = accountRepository.findByCode(accumulatedDepreciationAccountCode)
                .orElseThrow(() -> new IllegalArgumentException(
                    "Cuenta de depreciación acumulada no encontrada: " + accumulatedDepreciationAccountCode));
        
        // Crear transacción (asiento contable)
        Transaction transaction = new Transaction();
        transaction.setDate(record.getProcessingDate());
        transaction.setType("DEPRECIATION");
        transaction.setDescription("Depreciación mensual: " + asset.getName() + 
            " (" + asset.getCode() + ") - Período " + record.getMonth() + "/" + record.getYear());
        transaction.setReference("DEP-" + record.getYear() + "-" + String.format("%03d", record.getMonth()));
        
        // Crear entradas de diario
        TransactionService.TransactionEntryData debitEntry = new TransactionService.TransactionEntryData(
            expenseAccount.getId(),
            depreciationAmount,
            BigDecimal.ZERO,
            "Depreciación: " + asset.getName()
        );
        
        TransactionService.TransactionEntryData creditEntry = new TransactionService.TransactionEntryData(
            accumulatedDepreciationAccount.getId(),
            BigDecimal.ZERO,
            depreciationAmount,
            "Depreciación acumulada: " + asset.getName()
        );
        
        Transaction savedTransaction = transactionService.createTransaction(
            transaction,
            List.of(debitEntry, creditEntry),
            username
        );
        
        // Marcar registro como contabilizado
        record.setJournalEntryId(savedTransaction.getId());
        record.setPosted(true);
        depreciationRecordRepository.update(record);
        
        // Actualizar referencia en el activo
        logger.info("Asiento contable generado para depreciación: Transacción={}, Registro={}", 
            savedTransaction.getId(), recordId);
        
        return savedTransaction;
    }

    /**
     * Genera asientos contables para todos los registros de depreciación no contabilizados
     * de un período específico.
     * 
     * @param year Año
     * @param month Mes
     * @param username Usuario
     * @return Cantidad de asientos generados
     */
    public int postAllDepreciationForPeriod(Integer year, Integer month, String username) {
        List<DepreciationRecord> records = depreciationRecordRepository.findByYearAndMonth(year, month);
        int postedCount = 0;
        
        for (DepreciationRecord record : records) {
            if (!record.isPosted()) {
                try {
                    postDepreciationToAccounting(record.getId(), username);
                    postedCount++;
                } catch (Exception e) {
                    logger.error("Error generando asiento para registro {}: {}", 
                        record.getId(), e.getMessage());
                }
            }
        }
        
        logger.info("Asientos de depreciación generados: {}/{} para {}/{}", 
            postedCount, records.size(), month, year);
        
        return postedCount;
    }

    /**
     * Obtiene el reporte de depreciación para un período.
     */
    public List<DepreciationRecord> getDepreciationReport(Integer year, Integer month) {
        return depreciationRecordRepository.findByYearAndMonth(year, month);
    }

    /**
     * Obtiene el historial de depreciación de un activo.
     */
    public List<DepreciationRecord> getAssetDepreciationHistory(Long assetId) {
        return depreciationRecordRepository.findByFixedAssetId(assetId);
    }
}
