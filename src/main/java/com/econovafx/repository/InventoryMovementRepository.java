package com.econovafx.repository;

import com.econovafx.domain.InventoryMovement;
import com.econovafx.domain.InventoryMovement.MovementType;
import com.econovafx.domain.InventoryItem;
import com.econovafx.domain.Warehouse;
import io.ebean.DB;
import io.ebean.Database;
import io.avaje.inject.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repositorio para gestión de movimientos de inventario.
 */
@Component
public class InventoryMovementRepository {

    private final Database database;

    public InventoryMovementRepository() {
        this.database = DB.getDefault();
    }

    /**
     * Guarda un movimiento en la base de datos.
     */
    public void save(InventoryMovement movement) {
        database.save(movement);
    }

    /**
     * Busca un movimiento por su ID.
     */
    public Optional<InventoryMovement> findById(Long id) {
        return Optional.ofNullable(database.find(InventoryMovement.class, id));
    }

    /**
     * Obtiene todos los movimientos ordenados por fecha descendente.
     */
    public List<InventoryMovement> findAll() {
        return database.find(InventoryMovement.class)
                .orderBy("movementDate desc")
                .findList();
    }

    /**
     * Obtiene movimientos de un producto específico.
     */
    public List<InventoryMovement> findByItem(InventoryItem item) {
        return database.find(InventoryMovement.class)
                .fetch("item")
                .fetch("warehouse")
                .fetch("createdBy")
                .where()
                .eq("item", item)
                .orderBy("movementDate desc")
                .findList();
    }

    /**
     * Obtiene movimientos de un almacén específico.
     */
    public List<InventoryMovement> findByWarehouse(Warehouse warehouse) {
        return database.find(InventoryMovement.class)
                .fetch("item")
                .fetch("warehouse")
                .fetch("createdBy")
                .where()
                .eq("warehouse", warehouse)
                .orderBy("movementDate desc")
                .findList();
    }

    /**
     * Obtiene movimientos por tipo.
     */
    public List<InventoryMovement> findByType(MovementType type) {
        return database.find(InventoryMovement.class)
                .fetch("item")
                .fetch("warehouse")
                .fetch("createdBy")
                .where()
                .eq("type", type)
                .orderBy("movementDate desc")
                .findList();
    }

    /**
     * Obtiene movimientos en un rango de fechas.
     */
    public List<InventoryMovement> findByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        return database.find(InventoryMovement.class)
                .fetch("item")
                .fetch("warehouse")
                .fetch("createdBy")
                .where()
                .between("movementDate", startDate, endDate)
                .orderBy("movementDate desc")
                .findList();
    }

    /**
     * Obtiene movimientos por número de documento.
     */
    public List<InventoryMovement> findByDocumentNumber(String documentNumber) {
        return database.find(InventoryMovement.class)
                .fetch("item")
                .fetch("warehouse")
                .fetch("createdBy")
                .where()
                .eq("documentNumber", documentNumber)
                .orderBy("movementDate desc")
                .findList();
    }

    /**
     * Busca movimientos por número de documento (búsqueda parcial).
     */
    public List<InventoryMovement> findByDocumentNumberContaining(String documentNumber) {
        return database.find(InventoryMovement.class)
                .fetch("item")
                .fetch("warehouse")
                .fetch("createdBy")
                .where()
                .ilike("documentNumber", "%" + documentNumber + "%")
                .orderBy("movementDate desc")
                .findList();
    }

    /**
     * Obtiene movimientos relacionados con una transacción contable.
     */
    public List<InventoryMovement> findByTransaction(Long transactionId) {
        return database.find(InventoryMovement.class)
                .fetch("item")
                .fetch("warehouse")
                .fetch("createdBy")
                .fetch("relatedTransaction")
                .where()
                .eq("relatedTransaction.id", transactionId)
                .orderBy("movementDate desc")
                .findList();
    }

    /**
     * Cuenta el total de movimientos.
     */
    public int count() {
        return database.find(InventoryMovement.class).findCount();
    }

    /**
     * Obtiene movimientos recientes (últimos N registros).
     */
    public List<InventoryMovement> findRecent(int limit) {
        return database.find(InventoryMovement.class)
                .fetch("item")
                .fetch("warehouse")
                .fetch("createdBy")
                .setMaxRows(limit)
                .orderBy("movementDate desc")
                .findList();
    }

    /**
     * Obtiene el total movimentado por tipo en un período.
     */
    public java.math.BigDecimal getTotalByTypeAndPeriod(MovementType type, LocalDateTime startDate, LocalDateTime endDate) {
        String sql = "SELECT COALESCE(SUM(quantity), 0) as total FROM inventory_movement WHERE type = ? AND movement_date BETWEEN ? AND ?";
        io.ebean.SqlRow row = database.sqlQuery(sql)
                .setParameter(type.name())
                .setParameter(startDate)
                .setParameter(endDate)
                .findOne();
        return row != null ? row.getBigDecimal("total") : BigDecimal.ZERO;
    }

    /**
     * Obtiene todos los movimientos de entrada de un producto en un almacén, ordenados por fecha (más antiguos primero).
     * Necesario para el cálculo FIFO (PEPS).
     */
    public List<InventoryMovement> findEntriesByItemAndWarehouse(Long itemId, Long warehouseId) {
        return database.find(InventoryMovement.class)
                .fetch("item")
                .fetch("warehouse")
                .where()
                .eq("type", MovementType.ENTRY)
                .eq("item.id", itemId)
                .eq("warehouse.id", warehouseId)
                .orderBy("movementDate asc")
                .findList();
    }
}
