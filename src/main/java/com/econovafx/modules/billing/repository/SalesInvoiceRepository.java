package com.econovafx.modules.billing.repository;

import com.econovafx.modules.billing.model.SalesInvoice;
import com.econovafx.modules.billing.model.SalesInvoice.InvoiceStatus;
import io.avaje.inject.Component;
import io.ebean.Database;
import jakarta.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Repository for SalesInvoice entities.
 * Maneja operaciones de acceso a datos para facturas de venta.
 */
@Component
public class SalesInvoiceRepository {

    private static final Logger logger = LoggerFactory.getLogger(SalesInvoiceRepository.class);

    private final Database database;

    @Inject
    public SalesInvoiceRepository(Database database) {
        this.database = database;
    }

    public Optional<SalesInvoice> findById(Long id) {
        return Optional.ofNullable(database.find(SalesInvoice.class, id));
    }

    public Optional<SalesInvoice> findByInvoiceNumber(String invoiceNumber) {
        return Optional.ofNullable(database.find(SalesInvoice.class)
                .where().eq("invoiceNumber", invoiceNumber).findOne());
    }

    public List<SalesInvoice> findAll() {
        return database.find(SalesInvoice.class)
                .orderBy().desc("createdAt").findList();
    }

    public List<SalesInvoice> findByStatus(InvoiceStatus status) {
        return database.find(SalesInvoice.class)
                .where().eq("status", status)
                .orderBy().desc("createdAt").findList();
    }

    public List<SalesInvoice> findByDateRange(LocalDate startDate, LocalDate endDate) {
        return database.find(SalesInvoice.class)
                .where()
                .ge("issueDate", startDate)
                .le("issueDate", endDate)
                .orderBy().desc("issueDate")
                .findList();
    }

    public List<SalesInvoice> findByThirdPartyId(Long thirdPartyId) {
        return database.find(SalesInvoice.class)
                .where().eq("thirdParty.id", thirdPartyId)
                .orderBy().desc("createdAt").findList();
    }

    public List<SalesInvoice> findByBillingSeriesId(Long billingSeriesId) {
        return database.find(SalesInvoice.class)
                .where().eq("billingSeries.id", billingSeriesId)
                .orderBy().desc("createdAt").findList();
    }

    public SalesInvoice save(SalesInvoice invoice) {
        database.save(invoice);
        logger.debug("SalesInvoice saved: {}", invoice.getInvoiceNumber());
        return invoice;
    }

    public void update(SalesInvoice invoice) {
        database.update(invoice);
        logger.debug("SalesInvoice updated: {}", invoice.getInvoiceNumber());
    }

    public void delete(SalesInvoice invoice) {
        database.delete(invoice);
        logger.debug("SalesInvoice deleted: {}", invoice.getInvoiceNumber());
    }

    public void deleteById(Long id) {
        database.delete(SalesInvoice.class, id);
        logger.debug("SalesInvoice deleted by ID: {}", id);
    }

    public boolean existsByInvoiceNumber(String invoiceNumber) {
        return database.find(SalesInvoice.class)
                .where().eq("invoiceNumber", invoiceNumber).exists();
    }

    public boolean existsById(Long id) {
        return database.find(SalesInvoice.class, id) != null;
    }

    public long count() {
        return database.find(SalesInvoice.class).findCount();
    }

    public long countByStatus(InvoiceStatus status) {
        return database.find(SalesInvoice.class)
                .where().eq("status", status).findCount();
    }

    public List<SalesInvoice> findUnpostedInvoices() {
        return database.find(SalesInvoice.class)
                .where().eq("journalEntryId", null)
                .eq("status", InvoiceStatus.ISSUED)
                .orderBy().asc("issueDate")
                .findList();
    }
}
