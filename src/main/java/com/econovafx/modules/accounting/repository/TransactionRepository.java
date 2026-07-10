package com.econovafx.modules.accounting.repository;

import com.econovafx.modules.accounting.model.Transaction;
import com.econovafx.modules.accounting.model.TransactionEntry;
import io.avaje.inject.Component;
import io.ebean.Database;
import jakarta.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Repository for Transaction entities
 */
@Component
public class TransactionRepository {

    private static final Logger logger = LoggerFactory.getLogger(TransactionRepository.class);

    private final Database database;

    @Inject
    public TransactionRepository(Database database) {
        this.database = database;
    }

    public Optional<Transaction> findById(Long id) {
        return Optional.ofNullable(database.find(Transaction.class, id));
    }

    public Optional<Transaction> findByNumber(String number) {
        return Optional.ofNullable(database.find(Transaction.class)
                .where().eq("number", number).findOne());
    }

    public List<Transaction> findAll() {
        return database.find(Transaction.class)
                .fetch("entries")
                .fetch("entries.account")
                .orderBy().desc("date").findList();
    }

    public List<Transaction> findByDateRange(LocalDate startDate, LocalDate endDate) {
        return database.find(Transaction.class)
                .where()
                .ge("date", startDate)
                .le("date", endDate)
                .orderBy().desc("date")
                .findList();
    }

    public List<Transaction> findByType(String type) {
        return database.find(Transaction.class)
                .where().eq("type", type)
                .orderBy().desc("date").findList();
    }

    public List<Transaction> findPostedTransactions() {
        return database.find(Transaction.class)
                .where().eq("isPosted", true)
                .orderBy().desc("date").findList();
    }

    public List<Transaction> findUnpostedTransactions() {
        return database.find(Transaction.class)
                .where().eq("isPosted", false)
                .orderBy().asc("date").findList();
    }

    public List<Transaction> searchByDescription(String searchTerm) {
        return database.find(Transaction.class)
                .where().ilike("description", "%" + searchTerm + "%")
                .orderBy().desc("date").findList();
    }

    public Transaction save(Transaction transaction) {
        database.save(transaction);
        logger.debug("Transaction saved: {}", transaction.getNumber());
        return transaction;
    }

    public void update(Transaction transaction) {
        database.update(transaction);
        logger.debug("Transaction updated: {}", transaction.getNumber());
    }

    public void delete(Transaction transaction) {
        database.delete(transaction);
        logger.debug("Transaction deleted: {}", transaction.getNumber());
    }

    public void deleteById(Long id) {
        database.delete(Transaction.class, id);
        logger.debug("Transaction deleted by ID: {}", id);
    }

    public boolean existsByNumber(String number) {
        return database.find(Transaction.class)
                .where().eq("number", number).exists();
    }

    public long count() {
        return database.find(Transaction.class).findCount();
    }

    public long countPosted() {
        return database.find(Transaction.class)
                .where().eq("isPosted", true).findCount();
    }

    public TransactionEntry saveEntry(TransactionEntry entry) {
        database.save(entry);
        logger.debug("Transaction entry saved for transaction: {}",
                entry.getTransaction().getNumber());
        return entry;
    }

    public void updateEntry(TransactionEntry entry) {
        database.update(entry);
        logger.debug("Transaction entry updated");
    }

    public void deleteEntry(TransactionEntry entry) {
        database.delete(entry);
        logger.debug("Transaction entry deleted");
    }

    public List<TransactionEntry> findEntriesByTransaction(Long transactionId) {
        return database.find(TransactionEntry.class)
                .where().eq("transaction.id", transactionId)
                .findList();
    }

    public List<TransactionEntry> findEntriesByAccount(Long accountId) {
        return database.find(TransactionEntry.class)
                .where().eq("account.id", accountId)
                .orderBy().desc("transaction.date")
                .findList();
    }

    public List<Transaction> findByThirdPartyId(Long thirdPartyId) {
        return database.find(Transaction.class)
                .fetch("entries")
                .fetch("entries.account")
                .where().eq("thirdParty.id", thirdPartyId)
                .orderBy().desc("date")
                .findList();
    }
}
