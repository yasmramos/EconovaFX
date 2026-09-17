package com.econovafx.modules.receivables.repository;

import com.econovafx.modules.receivables.model.CustomerPayment;
import com.econovafx.modules.billing.model.ThirdParty;
import com.econovafx.modules.receivables.model.CustomerPayment.PaymentStatus;
import io.ebean.annotation.Transactional;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import java.util.List;
import java.util.Optional;

/**
 * Repository for CustomerPayment entities.
 * Provides database operations for customer payments management.
 */
@Singleton
public class CustomerPaymentRepository {

    private final io.ebean.Database database;

    @Inject
    public CustomerPaymentRepository(io.ebean.Database database) {
        this.database = database;
    }

    public Optional<CustomerPayment> findById(Long id) {
        return Optional.ofNullable(database.find(CustomerPayment.class, id));
    }

    public Optional<CustomerPayment> findByPaymentNumber(String paymentNumber) {
        return Optional.ofNullable(
            database.find(CustomerPayment.class)
                .where().eq("paymentNumber", paymentNumber)
                .findOne()
        );
    }

    public List<CustomerPayment> findAll() {
        return database.find(CustomerPayment.class).findList();
    }

    public List<CustomerPayment> findByCustomer(ThirdParty customer) {
        return database.find(CustomerPayment.class)
            .where().eq("customer", customer)
            .findList();
    }

    public List<CustomerPayment> findByStatus(PaymentStatus status) {
        return database.find(CustomerPayment.class)
            .where().eq("status", status)
            .findList();
    }

    public List<CustomerPayment> findAdvancePayments() {
        return database.find(CustomerPayment.class)
            .where().eq("advancePayment", true)
            .ne("status", PaymentStatus.CANCELLED)
            .findList();
    }

    @Transactional
    public CustomerPayment save(CustomerPayment payment) {
        database.save(payment);
        return payment;
    }

    @Transactional
    public void update(CustomerPayment payment) {
        database.update(payment);
    }

    @Transactional
    public void delete(CustomerPayment payment) {
        database.delete(payment);
    }

    public long count() {
        return database.find(CustomerPayment.class).findCount();
    }

    public long countByCustomer(ThirdParty customer) {
        return database.find(CustomerPayment.class)
            .where().eq("customer", customer)
            .findCount();
    }

    public boolean existsByPaymentNumber(String paymentNumber) {
        return database.find(CustomerPayment.class)
            .where().eq("paymentNumber", paymentNumber)
            .exists();
    }
}
