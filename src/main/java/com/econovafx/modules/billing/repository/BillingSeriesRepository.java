package com.econovafx.repository;

import com.econovafx.model.BillingSeries;
import com.econovafx.model.BillingSeries.DocumentType;
import io.avaje.inject.Component;
import io.ebean.Database;
import jakarta.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;

/**
 * Repository for BillingSeries entities.
 * Maneja operaciones de acceso a datos para series de facturación.
 */
@Component
public class BillingSeriesRepository {

    private static final Logger logger = LoggerFactory.getLogger(BillingSeriesRepository.class);

    private final Database database;

    @Inject
    public BillingSeriesRepository(Database database) {
        this.database = database;
    }

    public Optional<BillingSeries> findById(Long id) {
        return Optional.ofNullable(database.find(BillingSeries.class, id));
    }

    public List<BillingSeries> findAll() {
        return database.find(BillingSeries.class)
                .orderBy().asc("seriesCode").findList();
    }

    public List<BillingSeries> findByDocumentType(DocumentType documentType) {
        return database.find(BillingSeries.class)
                .where().eq("documentType", documentType)
                .orderBy().asc("seriesCode").findList();
    }

    public List<BillingSeries> findActiveSeries() {
        return database.find(BillingSeries.class)
                .where().eq("active", true)
                .orderBy().asc("seriesCode").findList();
    }

    public Optional<BillingSeries> findBySeriesCodeAndDocumentType(String seriesCode, DocumentType documentType) {
        return Optional.ofNullable(database.find(BillingSeries.class)
                .where()
                .eq("seriesCode", seriesCode)
                .eq("documentType", documentType)
                .findOne());
    }

    public BillingSeries save(BillingSeries series) {
        database.save(series);
        logger.debug("BillingSeries saved: {} - {}", series.getSeriesCode(), series.getDocumentType());
        return series;
    }

    public void update(BillingSeries series) {
        database.update(series);
        logger.debug("BillingSeries updated: {}", series.getId());
    }

    public void delete(BillingSeries series) {
        database.delete(series);
        logger.debug("BillingSeries deleted: {}", series.getSeriesCode());
    }

    public void deleteById(Long id) {
        database.delete(BillingSeries.class, id);
        logger.debug("BillingSeries deleted by ID: {}", id);
    }

    public boolean existsBySeriesCodeAndDocumentType(String seriesCode, DocumentType documentType) {
        return database.find(BillingSeries.class)
                .where()
                .eq("seriesCode", seriesCode)
                .eq("documentType", documentType)
                .exists();
    }

    public boolean existsBySeriesCodeAndDocumentTypeExclude(String seriesCode, DocumentType documentType, Long excludeId) {
        var query = database.find(BillingSeries.class)
                .where()
                .eq("seriesCode", seriesCode)
                .eq("documentType", documentType);
        if (excludeId != null) {
            query.ne("id", excludeId);
        }
        return query.exists();
    }

    public boolean existsById(Long id) {
        return database.find(BillingSeries.class, id) != null;
    }

    public long count() {
        return database.find(BillingSeries.class).findCount();
    }

    public long countByDocumentType(DocumentType documentType) {
        return database.find(BillingSeries.class)
                .where().eq("documentType", documentType).findCount();
    }
}
