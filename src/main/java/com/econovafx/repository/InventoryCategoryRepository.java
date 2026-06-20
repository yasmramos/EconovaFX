package com.econovafx.repository;

import com.econovafx.domain.InventoryCategory;
import io.ebean.Database;
import io.avaje.inject.Component;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio para gestión de categorías de inventario.
 */
@Component
public class InventoryCategoryRepository {

    private final Database database;

    public InventoryCategoryRepository(Database database) {
        this.database = database;
    }

    /**
     * Guarda una categoría en la base de datos.
     */
    public void save(InventoryCategory category) {
        database.save(category);
    }

    /**
     * Actualiza una categoría existente.
     */
    public void update(InventoryCategory category) {
        database.update(category);
    }

    /**
     * Elimina una categoría por su ID.
     */
    public void deleteById(Long id) {
        database.delete(InventoryCategory.class, id);
    }

    /**
     * Busca una categoría por su ID.
     */
    public Optional<InventoryCategory> findById(Long id) {
        return Optional.ofNullable(database.find(InventoryCategory.class, id));
    }

    /**
     * Busca una categoría por su código.
     */
    public Optional<InventoryCategory> findByCode(String code) {
        return database.find(InventoryCategory.class)
                .where()
                .eq("code", code)
                .findOneOrEmpty();
    }

    /**
     * Obtiene todas las categorías activas.
     */
    public List<InventoryCategory> findAllActive() {
        return database.find(InventoryCategory.class)
                .where()
                .eq("active", true)
                .orderBy("name")
                .findList();
    }

    /**
     * Obtiene todas las categorías (activas e inactivas).
     */
    public List<InventoryCategory> findAll() {
        return database.find(InventoryCategory.class)
                .orderBy("name")
                .findList();
    }

    /**
     * Obtiene categorías hijas de una categoría padre.
     */
    public List<InventoryCategory> findByParent(InventoryCategory parent) {
        return database.find(InventoryCategory.class)
                .where()
                .eq("parent", parent)
                .eq("active", true)
                .orderBy("name")
                .findList();
    }

    /**
     * Obtiene categorías sin padre (raíz).
     */
    public List<InventoryCategory> findRootCategories() {
        return database.find(InventoryCategory.class)
                .where()
                .isNull("parent")
                .eq("active", true)
                .orderBy("name")
                .findList();
    }

    /**
     * Busca categorías por nombre (búsqueda parcial).
     */
    public List<InventoryCategory> findByNameContaining(String name) {
        return database.find(InventoryCategory.class)
                .where()
                .ilike("name", "%" + name + "%")
                .eq("active", true)
                .orderBy("name")
                .findList();
    }

    /**
     * Cuenta el total de categorías activas.
     */
    public int countActive() {
        return database.find(InventoryCategory.class)
                .where()
                .eq("active", true)
                .findCount();
    }

    /**
     * Verifica si existe una categoría con el código dado (excluyendo la actual).
     */
    public boolean existsByCodeExclude(String code, Long excludeId) {
        var query = database.find(InventoryCategory.class)
                .where()
                .eq("code", code);
        
        if (excludeId != null) {
            query.ne("id", excludeId);
        }
        
        return query.exists();
    }
}
