package com.econovafx.repository;

import com.econovafx.domain.ConciliacionBancaria;
import io.ebean.DB;
import io.ebean.Database;
import io.ebean.ExpressionList;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Repository for Bank Reconciliation operations
 */
@Component
public class ConciliacionBancariaRepository {

    private final Database database;

    public ConciliacionBancariaRepository() {
        this.database = DB.getDefault();
    }

    public List<ConciliacionBancaria> findAll() {
        return database.find(ConciliacionBancaria.class)
                .orderBy("fechaFin.desc")
                .findList();
    }

    public Optional<ConciliacionBancaria> findById(Long id) {
        return Optional.ofNullable(database.find(ConciliacionBancaria.class, id));
    }

    public Optional<ConciliacionBancaria> findByNumeroConciliacion(String numeroConciliacion) {
        return Optional.ofNullable(database.find(ConciliacionBancaria.class)
                .where().eq("numeroConciliacion", numeroConciliacion)
                .findOne());
    }

    public ConciliacionBancaria save(ConciliacionBancaria conciliacion) {
        database.save(conciliacion);
        return conciliacion;
    }

    public void delete(Long id) {
        database.delete(ConciliacionBancaria.class, id);
    }

    public ExpressionList<ConciliacionBancaria> find() {
        return database.find(ConciliacionBancaria.class);
    }

    public List<ConciliacionBancaria> findByCuentaBancaria(Long cuentaBancariaId) {
        return database.find(ConciliacionBancaria.class)
                .where().eq("cuentaBancaria.id", cuentaBancariaId)
                .orderBy("fechaFin.desc")
                .findList();
    }

    public List<ConciliacionBancaria> findNoPosteadas() {
        return database.find(ConciliacionBancaria.class)
                .where().eq("esPosteada", false)
                .orderBy("fechaFin.asc")
                .findList();
    }

    public List<ConciliacionBancaria> findByCuentaBancariaAndFechaBetween(
            Long cuentaBancariaId, LocalDate fechaInicio, LocalDate fechaFin) {
        return database.find(ConciliacionBancaria.class)
                .where()
                .eq("cuentaBancaria.id", cuentaBancariaId)
                .ge("fechaFin", fechaInicio)
                .le("fechaInicio", fechaFin)
                .orderBy("fechaFin.desc")
                .findList();
    }

    public Optional<ConciliacionBancaria> findUltimaPorCuentaBancaria(Long cuentaBancariaId) {
        List<ConciliacionBancaria> lista = database.find(ConciliacionBancaria.class)
                .where().eq("cuentaBancaria.id", cuentaBancariaId)
                .orderBy("fechaFin.desc")
                .setMaxRows(1)
                .findList();
        
        if (lista.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(lista.get(0));
    }
}
