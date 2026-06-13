package com.econovafx.repository;

import com.econovafx.domain.CuentaBancaria;
import io.avaje.inject.Component;
import io.ebean.DB;
import io.ebean.Database;
import io.ebean.ExpressionList;

import java.util.List;
import java.util.Optional;

/**
 * Repository for Bank Account operations
 */
@Component
public class CuentaBancariaRepository {

    private final Database database;

    public CuentaBancariaRepository() {
        this.database = DB.getDefault();
    }

    public List<CuentaBancaria> findAll() {
        return database.find(CuentaBancaria.class)
                .orderBy("codigo")
                .findList();
    }

    public List<CuentaBancaria> findActivas() {
        return database.find(CuentaBancaria.class)
                .where().eq("esActiva", true)
                .orderBy("codigo")
                .findList();
    }

    public Optional<CuentaBancaria> findById(Long id) {
        return Optional.ofNullable(database.find(CuentaBancaria.class, id));
    }

    public Optional<CuentaBancaria> findByCodigo(String codigo) {
        return Optional.ofNullable(database.find(CuentaBancaria.class)
                .where().eq("codigo", codigo)
                .findOne());
    }

    public CuentaBancaria save(CuentaBancaria cuentaBancaria) {
        database.save(cuentaBancaria);
        return cuentaBancaria;
    }

    public void delete(Long id) {
        database.delete(CuentaBancaria.class, id);
    }

    public ExpressionList<CuentaBancaria> find() {
        return database.find(CuentaBancaria.class);
    }

    public List<CuentaBancaria> findByMoneda(String moneda) {
        return database.find(CuentaBancaria.class)
                .where().eq("moneda", moneda)
                .eq("esActiva", true)
                .findList();
    }
}
