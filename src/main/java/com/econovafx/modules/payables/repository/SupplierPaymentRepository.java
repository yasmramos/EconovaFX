package com.econovafx.modules.payables.repository;

import com.econovafx.modules.payables.model.SupplierPayment;
import com.econovafx.modules.billing.model.ThirdParty;
import com.econovafx.modules.payables.model.SupplierPayment.PaymentStatus;
import io.ebean.annotation.Transactional;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import java.util.List;
import java.util.Optional;

/**
 * Repository for SupplierPayment entities.
 * Provides database operations for supplier payments management.
 */
@Singleton
public class SupplierPaymentRepository {

    private final io.ebean.Database database;

    @Inject
    public SupplierPaymentRepository(io.ebean.Database database) {
        this.database = database;
    }

    public Optional<SupplierPayment> findById(Long id) {
        return Optional.ofNullable(database.find(SupplierPayment.class, id));
    }

    public Optional<SupplierPayment> findByPaymentNumber(String paymentNumber) {
        return Optional.ofNullable(
            database.find(SupplierPayment.class)
                .where().eq("paymentNumber", paymentNumber)
                .findOne()
        );
    }

    public List<SupplierPayment> findAll() {
        return database.find(SupplierPayment.class).findList();
    }

    public List<SupplierPayment> findBySupplier(ThirdParty supplier) {
        return database.find(SupplierPayment.class)
            .where().eq("supplier", supplier)
            .findList();
    }

    public List<SupplierPayment> findByStatus(PaymentStatus status) {
        return database.find(SupplierPayment.class)
            .where().eq("status", status)
            .findList();
    }

    public List<SupplierPayment> findAdvancePayments() {
        return database.find(SupplierPayment.class)
            .where().eq("advancePayment", true)
            .ne("status", PaymentStatus.CANCELLED)
            .findList();
    }

    @Transactional
    public SupplierPayment save(SupplierPayment payment) {
        database.save(payment);
        return payment;
    }

    @Transactional
    public void update(SupplierPayment payment) {
        database.update(payment);
    }

    @Transactional
    public void delete(SupplierPayment payment) {
        database.delete(payment);
    }

    public long count() {
        return database.find(SupplierPayment.class).findCount();
    }

    public long countBySupplier(ThirdParty supplier) {
        return database.find(SupplierPayment.class)
            .where().eq("supplier", supplier)
            .findCount();
    }

    public boolean existsByPaymentNumber(String paymentNumber) {
        return database.find(SupplierPayment.class)
            .where().eq("paymentNumber", paymentNumber)
            .exists();
    }
}
