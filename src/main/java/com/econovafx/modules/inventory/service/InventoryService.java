package com.econovafx.modules.inventory.service;

import com.econovafx.modules.inventory.model.*;
import com.econovafx.modules.inventory.repository.*;
import com.econovafx.modules.accounting.repository.AccountingPeriodRepository;
import com.econovafx.modules.accounting.service.TransactionService;
import com.econovafx.modules.core.service.AuditService;
import com.econovafx.modules.core.model.User;
import com.econovafx.modules.billing.model.ThirdParty;
import com.econovafx.modules.accounting.model.AccountingPeriod;
import io.avaje.inject.Component;
import com.econovafx.modules.core.security.RequiresTenant;
import jakarta.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Servicio para gestión de inventarios.
 * Implementa reglas de negocio y validaciones según Resolución 340/2004.
 */
@Component
@RequiresTenant
public class InventoryService {

    private static final Logger log = LoggerFactory.getLogger(InventoryService.class);

    @Inject
    public InventoryItemRepository itemRepository;

    @Inject
    public InventoryCategoryRepository categoryRepository;

    @Inject
    public InventoryMovementRepository movementRepository;

    @Inject
    public WarehouseRepository warehouseRepository;

    @Inject
    public TransactionService transactionService;

    @Inject
    public AuditService auditService;

    /**
     * Calcula el costo de salida según el método de valoración del almacén.
     * Soporta FIFO (PEPS) y Promedio Ponderado.
     */
    public BigDecimal calculateOutputCost(InventoryItem item, Warehouse warehouse, BigDecimal quantity) {
        ValuationMethod method = warehouse.getValuationMethod();
        
        switch (method) {
            case FIFO:
                return calculateFifoCost(item, warehouse, quantity);
            case WEIGHTED_AVERAGE:
                return item.getUnitCost().multiply(quantity);
            default:
                throw new IllegalArgumentException("Método de valoración no soportado: " + method);
        }
    }

    /**
     * Calcula el costo de salida usando el método FIFO (PEPS).
     * Las primeras unidades en entrar son las primeras en salir.
     */
    private BigDecimal calculateFifoCost(InventoryItem item, Warehouse warehouse, BigDecimal quantity) {
        // Obtener movimientos de entrada ordenados por fecha (más antiguos primero)
        List<InventoryMovement> entries = movementRepository.findEntriesByItemAndWarehouse(item.getId(), warehouse.getId());
        
        BigDecimal remainingQuantity = quantity;
        BigDecimal totalCost = BigDecimal.ZERO;
        
        for (InventoryMovement entry : entries) {
            if (remainingQuantity.compareTo(BigDecimal.ZERO) <= 0) {
                break;
            }
            
            BigDecimal availableQuantity = entry.getQuantity();
            BigDecimal quantityToUse = remainingQuantity.min(availableQuantity);
            
            totalCost = totalCost.add(entry.getUnitCost().multiply(quantityToUse));
            remainingQuantity = remainingQuantity.subtract(quantityToUse);
        }
        
        if (remainingQuantity.compareTo(BigDecimal.ZERO) > 0) {
            throw new IllegalStateException("Stock insuficiente para aplicar FIFO. Cantidad faltante: " + remainingQuantity);
        }
        
        return totalCost;
    }


    // ==================== CATEGORÍAS ====================

    /**
     * Guarda una nueva categoría de inventario.
     */
    public InventoryCategory saveCategory(InventoryCategory category) {
        validateCategory(category);
        
        if (categoryRepository.existsByCodeExclude(category.getCode(), null)) {
            throw new IllegalArgumentException("Ya existe una categoría con el código: " + category.getCode());
        }
        
        categoryRepository.save(category);
        log.info("Categoría de inventario creada: {} - {}", category.getCode(), category.getName());
        
        return category;
    }

    /**
     * Actualiza una categoría existente.
     */
    public InventoryCategory updateCategory(InventoryCategory category) {
        validateCategory(category);
        
        if (category.getId() == null) {
            throw new IllegalArgumentException("La categoría debe tener un ID válido");
        }
        
        if (categoryRepository.existsByCodeExclude(category.getCode(), category.getId())) {
            throw new IllegalArgumentException("Ya existe otra categoría con el código: " + category.getCode());
        }
        
        categoryRepository.update(category);
        log.info("Categoría de inventario actualizada: {}", category.getId());
        
        return category;
    }

    /**
     * Elimina una categoría (solo si no tiene productos asociados).
     */
    public void deleteCategory(Long categoryId) {
        var category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new IllegalArgumentException("Categoría no encontrada"));
        
        // Verificar si tiene productos asociados
        var items = itemRepository.findByCategory(category);
        if (!items.isEmpty()) {
            throw new IllegalStateException("No se puede eliminar la categoría porque tiene productos asociados");
        }
        
        categoryRepository.deleteById(categoryId);
        log.info("Categoría de inventario eliminada: {}", categoryId);
    }

    /**
     * Obtiene todas las categorías activas.
     */
    public List<InventoryCategory> getAllCategories() {
        return categoryRepository.findAllActive();
    }

    /**
     * Obtiene categorías raíz (sin padre).
     */
    public List<InventoryCategory> getRootCategories() {
        return categoryRepository.findRootCategories();
    }

    // ==================== PRODUCTOS ====================

    /**
     * Guarda un nuevo producto.
     */
    public InventoryItem saveItem(InventoryItem item, User currentUser) {
        validateItem(item);
        
        if (itemRepository.existsByCodeExclude(item.getCode(), null)) {
            throw new IllegalArgumentException("Ya existe un producto con el código: " + item.getCode());
        }
        
        itemRepository.save(item);
        
        // Registrar auditoría
        auditService.logWithValues(
            "Producto creado",
            AuditLog.OperationType.CREATE,
            "InventoryItem",
            item.getId(),
            currentUser != null ? currentUser.getUsername() : "unknown",
            null,
            item.toString()
        );
        
        log.info("Producto creado: {} - {}", item.getCode(), item.getName());
        
        
        return item;
    }

    /**
     * Actualiza un producto existente.
     */
    public InventoryItem updateItem(InventoryItem item, User currentUser) {
        validateItem(item);
        
        if (item.getId() == null) {
            throw new IllegalArgumentException("El producto debe tener un ID válido");
        }
        
        var existingItem = itemRepository.findById(item.getId())
                .orElseThrow(() -> new IllegalArgumentException("Producto no encontrado"));
        
        if (itemRepository.existsByCodeExclude(item.getCode(), item.getId())) {
            throw new IllegalArgumentException("Ya existe otro producto con el código: " + item.getCode());
        }
        
        String oldValue = existingItem.toString();
        itemRepository.update(item);
        
        // Registrar auditoría
        auditService.logWithValues(
            "Producto actualizado",
            AuditLog.OperationType.UPDATE,
            "InventoryItem",
            item.getId(),
            currentUser != null ? currentUser.getUsername() : "unknown",
            oldValue,
            item.toString()
        );
        
        log.info("Producto actualizado: {}", item.getId());
        
        return item;
    }

    /**
     * Elimina un producto (lógicamente, desactivándolo).
     */
    public void deleteItem(Long itemId, User currentUser) {
        var item = itemRepository.findById(itemId)
                .orElseThrow(() -> new IllegalArgumentException("Producto no encontrado"));
        
        item.setActive(false);
        itemRepository.update(item);
        
        log.info("Producto desactivado: {}", itemId);
        
    }

    /**
     * Obtiene todos los productos activos.
     */
    public List<InventoryItem> getAllItems() {
        return itemRepository.findAllActive();
    }

    /**
     * Busca productos por término de búsqueda.
     */
    public List<InventoryItem> searchItems(String query) {
        return itemRepository.search(query);
    }

    /**
     * Obtiene productos con stock bajo.
     */
    public List<InventoryItem> getLowStockItems() {
        return itemRepository.findBelowMinimumStock();
    }

    /**
     * Obtiene el valor total del inventario.
     */
    public BigDecimal getTotalInventoryValue() {
        return itemRepository.getTotalInventoryValue();
    }

    // ==================== MOVIMIENTOS ====================

    /**
     * Registra una entrada de inventario.
     */
    public InventoryMovement registerEntry(
            InventoryItem item,
            Warehouse warehouse,
            BigDecimal quantity,
            BigDecimal unitCost,
            String documentNumber,
            String notes,
            ThirdParty supplier,
            User currentUser) {
        
        validateMovement(item, warehouse, quantity, documentNumber);
        
        InventoryMovement movement = new InventoryMovement();
        movement.setType(InventoryMovement.MovementType.ENTRY);
        movement.setItem(item);
        movement.setWarehouse(warehouse);
        movement.setQuantity(quantity);
        movement.setUnitCost(unitCost);
        movement.setDocumentNumber(documentNumber);
        movement.setNotes(notes);
        movement.setThirdParty(supplier);
        movement.setCreatedBy(currentUser);
        movement.calculateTotalAmount();
        
        movementRepository.save(movement);
        
        // Actualizar stock
        itemRepository.updateStock(item.getId(), quantity);
        
        // Actualizar costo unitario si es necesario (promedio ponderado)
        updateAverageCost(item, unitCost, quantity);
        
        // Registrar auditoría
        auditService.logWithValues(
            "Entrada de inventario registrada",
            AuditLog.OperationType.CREATE,
            "InventoryMovement",
            movement.getId(),
            currentUser != null ? currentUser.getUsername() : "unknown",
            null,
            movement.toString()
        );
        
        log.info("Entrada de inventario registrada: {} - Cantidad: {}", documentNumber, quantity);
        
        return movement;
    }

    /**
     * Registra una salida de inventario.
     */
    public InventoryMovement registerOutput(
            InventoryItem item,
            Warehouse warehouse,
            BigDecimal quantity,
            String documentNumber,
            String notes,
            User currentUser) {
        
        validateMovement(item, warehouse, quantity, documentNumber);
        
        // Verificar stock suficiente
        var currentItem = itemRepository.findById(item.getId())
                .orElseThrow(() -> new IllegalArgumentException("Producto no encontrado"));
        
        if (currentItem.getCurrentStock().compareTo(quantity) < 0) {
            throw new IllegalStateException("Stock insuficiente para realizar la salida");
        }
        
        // Calcular costo de salida según método de valoración
        BigDecimal outputCost = calculateOutputCost(item, warehouse, quantity);
        BigDecimal unitCost = outputCost.divide(quantity, 4, java.math.RoundingMode.HALF_UP);
        
        InventoryMovement movement = new InventoryMovement();
        movement.setType(InventoryMovement.MovementType.OUTPUT);
        movement.setItem(item);
        movement.setWarehouse(warehouse);
        movement.setQuantity(quantity);
        movement.setUnitCost(unitCost);
        movement.setDocumentNumber(documentNumber);
        movement.setNotes(notes);
        movement.setCreatedBy(currentUser);
        movement.calculateTotalAmount();
        
        movementRepository.save(movement);
        
        // Actualizar stock (negativo para salida)
        itemRepository.updateStock(item.getId(), quantity.negate());
        
        // Registrar auditoría
        auditService.logWithValues(
            "Salida de inventario registrada",
            AuditLog.OperationType.CREATE,
            "InventoryMovement",
            movement.getId(),
            currentUser != null ? currentUser.getUsername() : "unknown",
            null,
            movement.toString()
        );
        
        log.info("Salida de inventario registrada: {} - Cantidad: {} (Método: {})", 
                 documentNumber, quantity, warehouse.getValuationMethod());
        
        
        return movement;
    }

    /**
     * Registra un ajuste de inventario.
     */
    public InventoryMovement registerAdjustment(
            InventoryItem item,
            Warehouse warehouse,
            BigDecimal quantityChange,
            String documentNumber,
            String reason,
            User currentUser) {
        
        validateMovement(item, warehouse, quantityChange, documentNumber);
        
        InventoryMovement.MovementType type = InventoryMovement.MovementType.ADJUSTMENT;
        
        InventoryMovement movement = new InventoryMovement();
        movement.setType(type);
        movement.setItem(item);
        movement.setWarehouse(warehouse);
        movement.setQuantity(quantityChange.abs());
        movement.setUnitCost(item.getUnitCost());
        movement.setDocumentNumber(documentNumber);
        movement.setNotes(reason);
        movement.setCreatedBy(currentUser);
        movement.calculateTotalAmount();
        
        movementRepository.save(movement);
        
        // Actualizar stock
        itemRepository.updateStock(item.getId(), quantityChange);
        
        log.info("Ajuste de inventario registrado: {} - Cantidad: {}", documentNumber, quantityChange);
        
        
        return movement;
    }

    /**
     * Obtiene todos los movimientos.
     */
    public List<InventoryMovement> getAllMovements() {
        return movementRepository.findAll();
    }

    /**
     * Obtiene movimientos de un producto.
     */
    public List<InventoryMovement> getMovementsByItem(Long itemId) {
        var item = itemRepository.findById(itemId)
                .orElseThrow(() -> new IllegalArgumentException("Producto no encontrado"));
        return movementRepository.findByItem(item);
    }

    /**
     * Obtiene movimientos en un rango de fechas.
     */
    public List<InventoryMovement> getMovementsByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        return movementRepository.findByDateRange(startDate, endDate);
    }

    // ==================== ALMACENES ====================

    /**
     * Guarda un nuevo almacén.
     */
    public Warehouse saveWarehouse(Warehouse warehouse) {
        validateWarehouse(warehouse);
        
        if (warehouseRepository.existsByCodeExclude(warehouse.getCode(), null)) {
            throw new IllegalArgumentException("Ya existe un almacén con el código: " + warehouse.getCode());
        }
        
        warehouseRepository.save(warehouse);
        log.info("Almacén creado: {} - {}", warehouse.getCode(), warehouse.getName());
        
        return warehouse;
    }

    /**
     * Actualiza un almacén existente.
     */
    public Warehouse updateWarehouse(Warehouse warehouse) {
        validateWarehouse(warehouse);
        
        if (warehouse.getId() == null) {
            throw new IllegalArgumentException("El almacén debe tener un ID válido");
        }
        
        if (warehouseRepository.existsByCodeExclude(warehouse.getCode(), warehouse.getId())) {
            throw new IllegalArgumentException("Ya existe otro almacén con el código: " + warehouse.getCode());
        }
        
        warehouseRepository.update(warehouse);
        log.info("Almacén actualizado: {}", warehouse.getId());
        
        return warehouse;
    }

    /**
     * Obtiene todos los almacenes activos.
     */
    public List<Warehouse> getAllWarehouses() {
        return warehouseRepository.findAllActive();
    }

    // ==================== VALIDACIONES ====================

    private void validateCategory(InventoryCategory category) {
        if (category == null || category.getCode() == null || category.getCode().isBlank()) {
            throw new IllegalArgumentException("El código de categoría es obligatorio");
        }
        if (category.getName() == null || category.getName().isBlank()) {
            throw new IllegalArgumentException("El nombre de la categoría es obligatorio");
        }
    }

    private void validateItem(InventoryItem item) {
        if (item == null || item.getCode() == null || item.getCode().isBlank()) {
            throw new IllegalArgumentException("El código del producto es obligatorio");
        }
        if (item.getName() == null || item.getName().isBlank()) {
            throw new IllegalArgumentException("El nombre del producto es obligatorio");
        }
        if (item.getCategory() == null) {
            throw new IllegalArgumentException("La categoría del producto es obligatoria");
        }
        if (item.getUnitCost() == null || item.getUnitCost().compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("El costo unitario debe ser mayor o igual a cero");
        }
        if (item.getSalePrice() == null || item.getSalePrice().compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("El precio de venta debe ser mayor o igual a cero");
        }
    }

    private void validateMovement(InventoryItem item, Warehouse warehouse, BigDecimal quantity, String documentNumber) {
        if (item == null || item.getId() == null) {
            throw new IllegalArgumentException("El producto es obligatorio");
        }
        if (warehouse == null || warehouse.getId() == null) {
            throw new IllegalArgumentException("El almacén es obligatorio");
        }
        if (quantity == null || quantity.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("La cantidad debe ser mayor a cero");
        }
        if (documentNumber == null || documentNumber.isBlank()) {
            throw new IllegalArgumentException("El número de documento es obligatorio");
        }
    }

    private void validateWarehouse(Warehouse warehouse) {
        if (warehouse == null || warehouse.getCode() == null || warehouse.getCode().isBlank()) {
            throw new IllegalArgumentException("El código del almacén es obligatorio");
        }
        if (warehouse.getName() == null || warehouse.getName().isBlank()) {
            throw new IllegalArgumentException("El nombre del almacén es obligatorio");
        }
    }

    // ==================== UTILIDADES ====================

    /**
     * Actualiza el costo promedio ponderado de un producto.
     */
    private void updateAverageCost(InventoryItem item, BigDecimal newUnitCost, BigDecimal newQuantity) {
        var currentItem = itemRepository.findById(item.getId())
                .orElseThrow(() -> new IllegalArgumentException("Producto no encontrado"));
        
        BigDecimal currentStock = currentItem.getCurrentStock().subtract(newQuantity);
        BigDecimal currentCost = currentItem.getUnitCost();
        
        if (currentStock.compareTo(BigDecimal.ZERO) > 0) {
            // Calcular nuevo costo promedio
            BigDecimal totalValue = currentCost.multiply(currentStock).add(newUnitCost.multiply(newQuantity));
            BigDecimal newStock = currentStock.add(newQuantity);
            BigDecimal averageCost = totalValue.divide(newStock, 4, java.math.RoundingMode.HALF_UP);
            
            currentItem.setUnitCost(averageCost);
            itemRepository.update(currentItem);
        } else {
            // Si no había stock, usar el nuevo costo
            currentItem.setUnitCost(newUnitCost);
            itemRepository.update(currentItem);
        }
    }

    /**
     * Verifica si el módulo de inventarios está cerrado para un período contable.
     * Cumplimiento Resolución 340/2004: Validación de cierre de módulos.
     */
    public boolean isModuleClosedForPeriod(AccountingPeriod period) {
        // Verificar si hay movimientos sin contabilizar
        // Por ahora retorna false (módulo abierto)
        log.info("Verificación de cierre de inventarios para el período: {}", period.getName());
        return false;
    }

    /**
     * Obtiene los movimientos de inventario para un período contable.
     */
    public List<InventoryMovement> getMovementsForPeriod(AccountingPeriod period) {
        if (period.getStartDate() == null || period.getEndDate() == null) {
            return List.of();
        }
        LocalDateTime startDateTime = period.getStartDate().atStartOfDay();
        LocalDateTime endDateTime = period.getEndDate().atTime(23, 59, 59);
        return movementRepository.findByDateRange(startDateTime, endDateTime);
    }
}
