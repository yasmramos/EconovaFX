package com.econovafx.repository;

import com.econovafx.model.InventoryItem;
import com.econovafx.model.InventoryCategory;
import com.econovafx.model.Warehouse;
import io.ebean.Database;
import io.ebean.Query;
import io.avaje.inject.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * Repositorio para gestión de productos del inventario.
 */
@Component
public class InventoryItemRepository {

    private final Database database;

    public InventoryItemRepository(Database database) {
        this.database = database;
    }

    /**
     * Guarda un producto en la base de datos.
     */
    public void save(InventoryItem item) {
        database.save(item);
    }

    /**
     * Actualiza un producto existente.
     */
    public void update(InventoryItem item) {
        database.update(item);
    }

    /**
     * Elimina un producto por su ID.
     */
    public void deleteById(Long id) {
        database.delete(InventoryItem.class, id);
    }

    /**
     * Busca un producto por su ID.
     */
    public Optional<InventoryItem> findById(Long id) {
        return Optional.ofNullable(database.find(InventoryItem.class, id));
    }

    /**
     * Busca un producto por su código.
     */
    public Optional<InventoryItem> findByCode(String code) {
        return database.find(InventoryItem.class)
                .where()
                .eq("code", code)
                .findOneOrEmpty();
    }

    /**
     * Obtiene todos los productos activos.
     */
    public List<InventoryItem> findAllActive() {
        return database.find(InventoryItem.class)
                .where()
                .eq("active", true)
                .orderBy("name")
                .findList();
    }

    /**
     * Obtiene todos los productos (activos e inactivos).
     */
    public List<InventoryItem> findAll() {
        return database.find(InventoryItem.class)
                .orderBy("name")
                .findList();
    }

    /**
     * Busca productos por categoría.
     */
    public List<InventoryItem> findByCategory(InventoryCategory category) {
        return database.find(InventoryItem.class)
                .where()
                .eq("category", category)
                .eq("active", true)
                .orderBy("name")
                .findList();
    }

    /**
     * Busca productos con stock por debajo del mínimo.
     */
    public List<InventoryItem> findBelowMinimumStock() {
        return database.find(InventoryItem.class)
                .where()
                .lt("currentStock", "minimumStock")
                .eq("active", true)
                .orderBy("name")
                .findList();
    }

    /**
     * Busca productos por nombre (búsqueda parcial).
     */
    public List<InventoryItem> findByNameContaining(String name) {
        return database.find(InventoryItem.class)
                .where()
                .ilike("name", "%" + name + "%")
                .eq("active", true)
                .orderBy("name")
                .findList();
    }

    /**
     * Busca productos por código o nombre.
     */
    public List<InventoryItem> search(String query) {
        String searchPattern = "%" + query.toLowerCase() + "%";
        return database.find(InventoryItem.class)
                .where()
                .disjunction()
                    .ilike("code", searchPattern)
                    .ilike("name", searchPattern)
                .endJunction()
                .eq("active", true)
                .orderBy("name")
                .findList();
    }

    /**
     * Obtiene el valor total del inventario.
     */
    public BigDecimal getTotalInventoryValue() {
        String sql = "SELECT COALESCE(SUM(unit_cost * current_stock), 0) as value FROM inventory_items WHERE active = TRUE";
        io.ebean.SqlRow row = database.sqlQuery(sql).findOne();
        return row != null ? row.getBigDecimal("value") : BigDecimal.ZERO;
    }

    /**
     * Cuenta el total de productos activos.
     */
    public int countActive() {
        return database.find(InventoryItem.class)
                .where()
                .eq("active", true)
                .findCount();
    }

    /**
     * Verifica si existe un producto con el código dado (excluyendo el actual).
     */
    public boolean existsByCodeExclude(String code, Long excludeId) {
        var query = database.find(InventoryItem.class)
                .where()
                .eq("code", code);
        
        if (excludeId != null) {
            query.ne("id", excludeId);
        }
        
        return query.exists();
    }

    /**
     * Actualiza el stock de un producto.
     */
    public void updateStock(Long itemId, BigDecimal quantityChange) {
        String sql = "UPDATE inventory_items SET current_stock = current_stock + ?, updated_at = NOW() WHERE id = ?";
        database.sqlUpdate(sql)
                .setParameter(quantityChange)
                .setParameter(itemId)
                .execute();
    }

    /**
     * Obtiene productos de un proveedor específico.
     */
    public List<InventoryItem> findBySupplier(Long supplierId) {
        return database.find(InventoryItem.class)
                .fetch("supplier")
                .where()
                .eq("supplier.id", supplierId)
                .eq("active", true)
                .orderBy("name")
                .findList();
    }
}
