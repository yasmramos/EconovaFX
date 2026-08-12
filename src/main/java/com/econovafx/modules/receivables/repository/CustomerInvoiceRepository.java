package com.econovafx.modules.receivables.repository;

import com.econovafx.modules.receivables.model.CustomerInvoice;
import com.econovafx.modules.billing.model.ThirdParty;
import com.econovafx.modules.receivables.model.CustomerInvoice.InvoiceStatus;
import io.ebean.annotation.Transactional;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Repository for CustomerInvoice entities.
 * Provides database operations for customer invoices management.
 */
@Singleton
public class CustomerInvoiceRepository {

    private final io.ebean.Database database;

    @Inject
    public CustomerInvoiceRepository(io.ebean.Database database) {
        this.database = database;
    }

    public Optional<CustomerInvoice> findById(Long id) {
        return Optional.ofNullable(database.find(CustomerInvoice.class, id));
    }

    public Optional<CustomerInvoice> findByInvoiceNumber(String invoiceNumber) {
        return Optional.ofNullable(
            database.find(CustomerInvoice.class)
                .where().eq("invoiceNumber", invoiceNumber)
                .findOne()
        );
    }

    public List<CustomerInvoice> findAll() {
        return database.find(CustomerInvoice.class).findList();
    }

    public List<CustomerInvoice> findByCustomer(ThirdParty customer) {
        return database.find(CustomerInvoice.class)
            .where().eq("customer", customer)
            .findList();
    }

    public List<CustomerInvoice> findByStatus(InvoiceStatus status) {
        return database.find(CustomerInvoice.class)
            .where().eq("status", status)
            .findList();
    }

    public List<CustomerInvoice> findOverdueInvoices() {
        LocalDate today = LocalDate.now();
        return database.find(CustomerInvoice.class)
            .where()
            .lt("dueDate", today)
            .gt("pendingAmount", java.math.BigDecimal.ZERO)
            .ne("status", InvoiceStatus.CANCELLED)
            .findList();
    }

    public List<CustomerInvoice> findByDueDateBetween(LocalDate startDate, LocalDate endDate) {
        return database.find(CustomerInvoice.class)
            .where()
            .between("dueDate", startDate, endDate)
            .findList();
    }

    public List<CustomerInvoice> findByInvoiceDateBetween(LocalDate startDate, LocalDate endDate) {
        return database.find(CustomerInvoice.class)
            .where()
            .between("invoiceDate", startDate, endDate)
            .findList();
    }

    @Transactional
    public CustomerInvoice save(CustomerInvoice invoice) {
        database.save(invoice);
        return invoice;
    }

    @Transactional
    public void update(CustomerInvoice invoice) {
        database.update(invoice);
    }

    @Transactional
    public void delete(CustomerInvoice invoice) {
        database.delete(invoice);
    }

    public long count() {
        return database.find(CustomerInvoice.class).findCount();
    }

    public long countByCustomer(ThirdParty customer) {
        return database.find(CustomerInvoice.class)
            .where().eq("customer", customer)
            .findCount();
    }

    public boolean existsByInvoiceNumber(String invoiceNumber) {
        return database.find(CustomerInvoice.class)
            .where().eq("invoiceNumber", invoiceNumber)
            .exists();
    }
}
