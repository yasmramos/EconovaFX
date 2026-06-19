package com.econovafx.repository;

import com.econovafx.domain.Warehouse;
import io.ebean.DB;
import io.ebean.Database;
import io.avaje.inject.Component;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio para gestión de almacenes.
 */
@Component
public class WarehouseRepository {

    private final Database database;

    public WarehouseRepository() {
        this.database = DB.getDefault();
    }

    /**
     * Guarda un almacén en la base de datos.
     */
    public void save(Warehouse warehouse) {
        database.save(warehouse);
    }

    /**
     * Actualiza un almacén existente.
     */
    public void update(Warehouse warehouse) {
        database.update(warehouse);
    }

    /**
     * Elimina un almacén por su ID.
     */
    public void deleteById(Long id) {
        database.delete(Warehouse.class, id);
    }

    /**
     * Busca un almacén por su ID.
     */
    public Optional<Warehouse> findById(Long id) {
        return Optional.ofNullable(database.find(Warehouse.class, id));
    }

    /**
     * Busca un almacén por su código.
     */
    public Optional<Warehouse> findByCode(String code) {
        return database.find(Warehouse.class)
                .where()
                .eq("code", code)
                .findOneOrEmpty();
    }

    /**
     * Obtiene todos los almacenes activos.
     */
    public List<Warehouse> findAllActive() {
        return database.find(Warehouse.class)
                .where()
                .eq("active", true)
                .orderBy("name")
                .findList();
    }

    /**
     * Obtiene todos los almacenes (activos e inactivos).
     */
    public List<Warehouse> findAll() {
        return database.find(Warehouse.class)
                .orderBy("name")
                .findList();
    }

    /**
     * Obtiene todos los almacenes (activos e inactivos) - alias para compatibilidad.
     */
    public List<Warehouse> findAllActiveAndInactive() {
        return findAll();
    }

    /**
     * Busca almacenes por nombre (búsqueda parcial).
     */
    public List<Warehouse> findByNameContaining(String name) {
        return database.find(Warehouse.class)
                .where()
                .ilike("name", "%" + name + "%")
                .eq("active", true)
                .orderBy("name")
                .findList();
    }

    /**
     * Cuenta el total de almacenes activos.
     */
    public int countActive() {
        return database.find(Warehouse.class)
                .where()
                .eq("active", true)
                .findCount();
    }

    /**
     * Verifica si existe un almacén con el código dado (excluyendo el actual).
     */
    public boolean existsByCodeExclude(String code, Long excludeId) {
        var query = database.find(Warehouse.class)
                .where()
                .eq("code", code);
        
        if (excludeId != null) {
            query.ne("id", excludeId);
        }
        
        return query.exists();
    }

    /**
     * Obtiene almacenes gestionados por un usuario específico.
     */
    public List<Warehouse> findByManager(Long userId) {
        return database.find(Warehouse.class)
                .fetch("manager")
                .where()
                .eq("manager.id", userId)
                .eq("active", true)
                .orderBy("name")
                .findList();
    }
}
