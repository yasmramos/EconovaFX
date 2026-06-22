package com.econovafx.repository;

import com.econovafx.model.DepreciationRecord;
import io.avaje.inject.Component;
import io.ebean.Database;
import jakarta.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Repository for DepreciationRecord entities.
 * Maneja operaciones de acceso a datos para registros de depreciación.
 */
@Component
public class DepreciationRecordRepository {

    private static final Logger logger = LoggerFactory.getLogger(DepreciationRecordRepository.class);

    private final Database database;

    @Inject
    public DepreciationRecordRepository(Database database) {
        this.database = database;
    }

    public Optional<DepreciationRecord> findById(Long id) {
        return Optional.ofNullable(database.find(DepreciationRecord.class, id));
    }

    public List<DepreciationRecord> findAll() {
        return database.find(DepreciationRecord.class)
                .orderBy().desc("processingDate").findList();
    }

    public List<DepreciationRecord> findByFixedAssetId(Long fixedAssetId) {
        return database.find(DepreciationRecord.class)
                .where().eq("fixedAsset.id", fixedAssetId)
                .orderBy().desc("year").desc("month").findList();
    }

    public List<DepreciationRecord> findByYearAndMonth(Integer year, Integer month) {
        return database.find(DepreciationRecord.class)
                .where()
                .eq("year", year)
                .eq("month", month)
                .orderBy().asc("fixedAsset.id")
                .findList();
    }

    public List<DepreciationRecord> findByYear(Integer year) {
        return database.find(DepreciationRecord.class)
                .where().eq("year", year)
                .orderBy().asc("month").asc("fixedAsset.id")
                .findList();
    }

    public List<DepreciationRecord> findUnpostedRecords() {
        return database.find(DepreciationRecord.class)
                .where()
                .eq("posted", false)
                .orderBy().asc("processingDate")
                .findList();
    }

    public Optional<DepreciationRecord> findByAssetAndPeriod(Long fixedAssetId, Integer year, Integer month) {
        return Optional.ofNullable(database.find(DepreciationRecord.class)
                .where()
                .eq("fixedAsset.id", fixedAssetId)
                .eq("year", year)
                .eq("month", month)
                .findOne());
    }

    public DepreciationRecord save(DepreciationRecord record) {
        database.save(record);
        logger.debug("DepreciationRecord saved: Asset={}, Year={}, Month={}", 
                record.getFixedAsset().getCode(), record.getYear(), record.getMonth());
        return record;
    }

    public void update(DepreciationRecord record) {
        database.update(record);
        logger.debug("DepreciationRecord updated: Asset={}, Year={}, Month={}", 
                record.getFixedAsset().getCode(), record.getYear(), record.getMonth());
    }

    public void delete(DepreciationRecord record) {
        database.delete(record);
        logger.debug("DepreciationRecord deleted: {}", record.getId());
    }

    public void deleteById(Long id) {
        database.delete(DepreciationRecord.class, id);
        logger.debug("DepreciationRecord deleted by ID: {}", id);
    }

    public boolean existsById(Long id) {
        return database.find(DepreciationRecord.class, id) != null;
    }

    public long count() {
        return database.find(DepreciationRecord.class).findCount();
    }

    public long countByYear(Integer year) {
        return database.find(DepreciationRecord.class)
                .where().eq("year", year).findCount();
    }

    public List<DepreciationRecord> findByDateRange(LocalDate startDate, LocalDate endDate) {
        return database.find(DepreciationRecord.class)
                .where()
                .ge("processingDate", startDate)
                .le("processingDate", endDate)
                .orderBy().desc("processingDate")
                .findList();
    }
}
