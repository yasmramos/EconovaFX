package com.econovafx.modules.cash.repository;

import com.econovafx.modules.cash.model.CashBox;
import io.avaje.inject.Component;
import io.ebean.Database;
import jakarta.inject.Inject;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * Repository for Cash Box data access using Ebean ORM.
 */
@Component
public class CashBoxRepository {
    
    private final Database database;

    @Inject
    public CashBoxRepository(Database database) {
        this.database = database;
    }

    public CashBox save(CashBox cashBox) {
        database.save(cashBox);
        return cashBox;
    }

    public Optional<CashBox> findById(Long id) {
        return Optional.ofNullable(database.find(CashBox.class, id));
    }

    public List<CashBox> findAll() {
        return database.find(CashBox.class).findList();
    }

    public List<CashBox> findOpenBoxes() {
        return database.find(CashBox.class)
                .where().eq("open", true)
                .findList();
    }

    public boolean deleteById(Long id) {
        int rowsDeleted = database.delete(CashBox.class, id);
        return rowsDeleted > 0;
    }

    public void updateBalance(Long id, BigDecimal newBalance) {
        CashBox box = database.find(CashBox.class, id);
        if (box != null) {
            box.setBalance(newBalance);
            database.update(box);
        }
    }
}
