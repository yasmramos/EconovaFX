package com.econovafx.modules.payables.repository;

import com.econovafx.modules.payables.model.SupplierInvoice;
import com.econovafx.modules.billing.model.ThirdParty;
import com.econovafx.modules.payables.model.SupplierInvoice.InvoiceStatus;
import io.ebean.annotation.Transactional;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Repository for SupplierInvoice entities.
 * Provides database operations for supplier invoices management.
 */
@Singleton
public class SupplierInvoiceRepository {

    private final io.ebean.Database database;

    @Inject
    public SupplierInvoiceRepository(io.ebean.Database database) {
        this.database = database;
    }

    public Optional<SupplierInvoice> findById(Long id) {
        return Optional.ofNullable(database.find(SupplierInvoice.class, id));
    }

    public Optional<SupplierInvoice> findByInvoiceNumber(String invoiceNumber) {
        return Optional.ofNullable(
            database.find(SupplierInvoice.class)
                .where().eq("invoiceNumber", invoiceNumber)
                .findOne()
        );
    }

    public List<SupplierInvoice> findAll() {
        return database.find(SupplierInvoice.class).findList();
    }

    public List<SupplierInvoice> findBySupplier(ThirdParty supplier) {
        return database.find(SupplierInvoice.class)
            .where().eq("supplier", supplier)
            .findList();
    }

    public List<SupplierInvoice> findByStatus(InvoiceStatus status) {
        return database.find(SupplierInvoice.class)
            .where().eq("status", status)
            .findList();
    }

    public List<SupplierInvoice> findOverdueInvoices() {
        LocalDate today = LocalDate.now();
        return database.find(SupplierInvoice.class)
            .where()
            .lt("dueDate", today)
            .gt("pendingAmount", java.math.BigDecimal.ZERO)
            .ne("status", InvoiceStatus.CANCELLED)
            .findList();
    }

    public List<SupplierInvoice> findByDueDateBetween(LocalDate startDate, LocalDate endDate) {
        return database.find(SupplierInvoice.class)
            .where()
            .between("dueDate", startDate, endDate)
            .findList();
    }

    public List<SupplierInvoice> findByInvoiceDateBetween(LocalDate startDate, LocalDate endDate) {
        return database.find(SupplierInvoice.class)
            .where()
            .between("invoiceDate", startDate, endDate)
            .findList();
    }

    @Transactional
    public SupplierInvoice save(SupplierInvoice invoice) {
        database.save(invoice);
        return invoice;
    }

    @Transactional
    public void update(SupplierInvoice invoice) {
        database.update(invoice);
    }

    @Transactional
    public void delete(SupplierInvoice invoice) {
        database.delete(invoice);
    }

    public long count() {
        return database.find(SupplierInvoice.class).findCount();
    }

    public long countBySupplier(ThirdParty supplier) {
        return database.find(SupplierInvoice.class)
            .where().eq("supplier", supplier)
            .findCount();
    }

    public boolean existsByInvoiceNumber(String invoiceNumber) {
        return database.find(SupplierInvoice.class)
            .where().eq("invoiceNumber", invoiceNumber)
            .exists();
    }

    public boolean existsById(Long id) {
        return findById(id).isPresent();
    }
}
