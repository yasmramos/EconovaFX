package com.econovafx.repository;

import com.econovafx.domain.MovimientoEfectivo;
import io.ebean.DB;
import io.ebean.Database;
import io.ebean.ExpressionList;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Repository for Cash Movement operations
 */
@Component
public class MovimientoEfectivoRepository {

    private final Database database;

    public MovimientoEfectivoRepository() {
        this.database = DB.getDefault();
    }

    public List<MovimientoEfectivo> findAll() {
        return database.find(MovimientoEfectivo.class)
                .orderBy("fecha.desc")
                .findList();
    }

    public Optional<MovimientoEfectivo> findById(Long id) {
        return Optional.ofNullable(database.find(MovimientoEfectivo.class, id));
    }

    public Optional<MovimientoEfectivo> findByNumeroComprobante(String numeroComprobante) {
        return Optional.ofNullable(database.find(MovimientoEfectivo.class)
                .where().eq("numeroComprobante", numeroComprobante)
                .findOne());
    }

    public MovimientoEfectivo save(MovimientoEfectivo movimiento) {
        database.save(movimiento);
        return movimiento;
    }

    public void delete(Long id) {
        database.delete(MovimientoEfectivo.class, id);
    }

    public ExpressionList<MovimientoEfectivo> find() {
        return database.find(MovimientoEfectivo.class);
    }

    public List<MovimientoEfectivo> findByCuentaBancaria(Long cuentaBancariaId) {
        return database.find(MovimientoEfectivo.class)
                .where().eq("cuentaBancaria.id", cuentaBancariaId)
                .eq("estaAnulado", false)
                .orderBy("fecha.desc")
                .findList();
    }

    public List<MovimientoEfectivo> findByCaja(Long cajaId) {
        return database.find(MovimientoEfectivo.class)
                .where().eq("caja.id", cajaId)
                .eq("estaAnulado", false)
                .orderBy("fecha.desc")
                .findList();
    }

    public List<MovimientoEfectivo> findByFechaBetween(LocalDate fechaInicio, LocalDate fechaFin) {
        return database.find(MovimientoEfectivo.class)
                .where()
                .between("fecha", fechaInicio, fechaFin)
                .eq("estaAnulado", false)
                .orderBy("fecha.desc")
                .findList();
    }

    public List<MovimientoEfectivo> findNoPosteados() {
        return database.find(MovimientoEfectivo.class)
                .where().eq("esPosteado", false)
                .eq("estaAnulado", false)
                .orderBy("fecha.asc")
                .findList();
    }

    public List<MovimientoEfectivo> findByCuentaBancariaAndFechaBetween(
            Long cuentaBancariaId, LocalDate fechaInicio, LocalDate fechaFin) {
        return database.find(MovimientoEfectivo.class)
                .where()
                .eq("cuentaBancaria.id", cuentaBancariaId)
                .between("fecha", fechaInicio, fechaFin)
                .eq("estaAnulado", false)
                .orderBy("fecha.desc")
                .findList();
    }
}
