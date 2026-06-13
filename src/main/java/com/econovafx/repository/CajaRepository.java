package com.econovafx.repository;

import com.econovafx.domain.Caja;
import io.ebean.DB;
import io.ebean.Database;
import io.ebean.ExpressionList;

import java.util.List;
import java.util.Optional;

/**
 * Repository for Cash Box operations
 */
@Component
public class CajaRepository {

    private final Database database;

    public CajaRepository() {
        this.database = DB.getDefault();
    }

    public List<Caja> findAll() {
        return database.find(Caja.class)
                .orderBy("codigo")
                .findList();
    }

    public List<Caja> findActivas() {
        return database.find(Caja.class)
                .where().eq("esActiva", true)
                .orderBy("codigo")
                .findList();
    }

    public Optional<Caja> findById(Long id) {
        return Optional.ofNullable(database.find(Caja.class, id));
    }

    public Optional<Caja> findByCodigo(String codigo) {
        return Optional.ofNullable(database.find(Caja.class)
                .where().eq("codigo", codigo)
                .findOne());
    }

    public Caja save(Caja caja) {
        database.save(caja);
        return caja;
    }

    public void delete(Long id) {
        database.delete(Caja.class, id);
    }

    public ExpressionList<Caja> find() {
        return database.find(Caja.class);
    }
}
