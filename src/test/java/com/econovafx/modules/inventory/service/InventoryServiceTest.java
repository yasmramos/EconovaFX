package com.econovafx.modules.inventory.service;

import com.econovafx.modules.inventory.model.*;
import com.econovafx.modules.inventory.repository.*;
import com.econovafx.modules.accounting.repository.AccountingPeriodRepository;
import com.econovafx.modules.accounting.service.TransactionService;
import com.econovafx.modules.core.service.AuditService;
import com.econovafx.modules.core.model.User;
import com.econovafx.modules.core.model.AuditLog;
import com.econovafx.modules.billing.model.ThirdParty;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Tests para InventoryService.
 */
@ExtendWith(MockitoExtension.class)
class InventoryServiceTest {

    @Mock
    private InventoryItemRepository itemRepository;

    @Mock
    private InventoryCategoryRepository categoryRepository;

    @Mock
    private InventoryMovementRepository movementRepository;

    @Mock
    private WarehouseRepository warehouseRepository;

    @Mock
    private TransactionService transactionService;

    @Mock
    private AuditService auditService;

    @InjectMocks
    private InventoryService inventoryService;

    private User testUser;
    private InventoryCategory testCategory;
    private InventoryItem testItem;
    private Warehouse testWarehouse;
    private ThirdParty testSupplier;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setUsername("testuser");

        testCategory = new InventoryCategory();
        testCategory.setId(1L);
        testCategory.setCode("CAT001");
        testCategory.setName("Categoría Test");
        testCategory.setActive(true);

        testItem = new InventoryItem();
        testItem.setId(1L);
        testItem.setCode("ITEM001");
        testItem.setName("Producto Test");
        testItem.setCategory(testCategory);
        testItem.setCurrentStock(new BigDecimal("100"));
        testItem.setUnitCost(new BigDecimal("10.00"));
        testItem.setMinimumStock(new BigDecimal("10"));
        testItem.setActive(true);

        testWarehouse = new Warehouse();
        testWarehouse.setId(1L);
        testWarehouse.setCode("WH001");
        testWarehouse.setName("Almacén Test");
        testWarehouse.setValuationMethod(ValuationMethod.WEIGHTED_AVERAGE);
        testWarehouse.setActive(true);

        testSupplier = new ThirdParty();
        testSupplier.setId(1L);
        testSupplier.setName("Proveedor Test");
    }

    // ==================== PRUEBAS PARA CÁLCULO DE COSTOS ====================

    @Test
    void testCalculateOutputCost_WeightedAverage() {
        BigDecimal quantity = new BigDecimal("5");
        BigDecimal expectedCost = new BigDecimal("10.00").multiply(quantity);

        BigDecimal result = inventoryService.calculateOutputCost(testItem, testWarehouse, quantity);

        assertEquals(expectedCost, result);
    }

    @Test
    void testCalculateOutputCost_Fifo() {
        testWarehouse.setValuationMethod(ValuationMethod.FIFO);

        InventoryMovement entry1 = new InventoryMovement();
        entry1.setQuantity(new BigDecimal("10"));
        entry1.setUnitCost(new BigDecimal("8.00"));

        InventoryMovement entry2 = new InventoryMovement();
        entry2.setQuantity(new BigDecimal("20"));
        entry2.setUnitCost(new BigDecimal("12.00"));

        List<InventoryMovement> entries = Arrays.asList(entry1, entry2);
        when(movementRepository.findEntriesByItemAndWarehouse(testItem.getId(), testWarehouse.getId()))
                .thenReturn(entries);

        BigDecimal quantity = new BigDecimal("15");
        BigDecimal result = inventoryService.calculateOutputCost(testItem, testWarehouse, quantity);

        // 10 unidades a 8.00 + 5 unidades a 12.00 = 80 + 60 = 140
        assertEquals(new BigDecimal("140.00"), result);
    }

    @Test
    void testCalculateOutputCost_FifoInsufficientStock() {
        testWarehouse.setValuationMethod(ValuationMethod.FIFO);

        InventoryMovement entry = new InventoryMovement();
        entry.setQuantity(new BigDecimal("10"));
        entry.setUnitCost(new BigDecimal("8.00"));

        when(movementRepository.findEntriesByItemAndWarehouse(testItem.getId(), testWarehouse.getId()))
                .thenReturn(List.of(entry));

        BigDecimal quantity = new BigDecimal("15");

        assertThrows(IllegalStateException.class, () ->
                inventoryService.calculateOutputCost(testItem, testWarehouse, quantity));
    }

    @Test
    void testCalculateOutputCost_UnsupportedValuationMethod() {
        ValuationMethod unsupportedMethod = null; // Simular método no soportado

        assertThrows(IllegalArgumentException.class, () -> {
            Warehouse wh = new Warehouse();
            wh.setValuationMethod(null);
            inventoryService.calculateOutputCost(testItem, wh, new BigDecimal("5"));
        });
    }

    // ==================== PRUEBAS PARA CATEGORÍAS ====================

    @Test
    void testSaveCategory_Success() {
        when(categoryRepository.existsByCodeExclude(anyString(), isNull())).thenReturn(false);
        doAnswer(invocation -> {
            InventoryCategory cat = invocation.getArgument(0);
            cat.setId(1L);
            return null;
        }).when(categoryRepository).save(any(InventoryCategory.class));

        InventoryCategory result = inventoryService.saveCategory(testCategory);

        assertNotNull(result);
        verify(categoryRepository).save(testCategory);
        verify(auditService, never()).logWithValues(anyString(), any(), anyString(), any(), anyString(), anyString(), anyString());
    }

    @Test
    void testSaveCategory_DuplicateCode() {
        when(categoryRepository.existsByCodeExclude(anyString(), isNull())).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () ->
                inventoryService.saveCategory(testCategory));
    }

    @Test
    void testUpdateCategory_Success() {
        when(categoryRepository.existsByCodeExclude(anyString(), anyLong())).thenReturn(false);
        when(categoryRepository.update(any(InventoryCategory.class))).thenReturn(1);

        InventoryCategory result = inventoryService.updateCategory(testCategory);

        assertNotNull(result);
        verify(categoryRepository).update(testCategory);
    }

    @Test
    void testUpdateCategory_NullId() {
        testCategory.setId(null);

        assertThrows(IllegalArgumentException.class, () ->
                inventoryService.updateCategory(testCategory));
    }

    @Test
    void testUpdateCategory_DuplicateCode() {
        when(categoryRepository.existsByCodeExclude(anyString(), anyLong())).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () ->
                inventoryService.updateCategory(testCategory));
    }

    @Test
    void testDeleteCategory_Success() {
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(testCategory));
        when(itemRepository.findByCategory(testCategory)).thenReturn(List.of());
        doNothing().when(categoryRepository).deleteById(1L);

        inventoryService.deleteCategory(1L);

        verify(categoryRepository).deleteById(1L);
    }

    @Test
    void testDeleteCategory_NotFound() {
        when(categoryRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () ->
                inventoryService.deleteCategory(1L));
    }

    @Test
    void testDeleteCategory_WithAssociatedItems() {
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(testCategory));
        when(itemRepository.findByCategory(testCategory)).thenReturn(List.of(testItem));

        assertThrows(IllegalStateException.class, () ->
                inventoryService.deleteCategory(1L));
    }

    @Test
    void testGetAllCategories() {
        List<InventoryCategory> categories = List.of(testCategory);
        when(categoryRepository.findAllActive()).thenReturn(categories);

        List<InventoryCategory> result = inventoryService.getAllCategories();

        assertEquals(1, result.size());
        verify(categoryRepository).findAllActive();
    }

    @Test
    void testGetRootCategories() {
        List<InventoryCategory> rootCategories = List.of(testCategory);
        when(categoryRepository.findRootCategories()).thenReturn(rootCategories);

        List<InventoryCategory> result = inventoryService.getRootCategories();

        assertEquals(1, result.size());
        verify(categoryRepository).findRootCategories();
    }

    // ==================== PRUEBAS PARA PRODUCTOS ====================

    @Test
    void testSaveItem_Success() {
        when(itemRepository.existsByCodeExclude(anyString(), isNull())).thenReturn(false);
        doAnswer(invocation -> {
            InventoryItem item = invocation.getArgument(0);
            item.setId(1L);
            return null;
        }).when(itemRepository).save(any(InventoryItem.class));

        InventoryItem result = inventoryService.saveItem(testItem, testUser);

        assertNotNull(result);
        verify(itemRepository).save(testItem);
        verify(auditService).logWithValues(anyString(), eq(AuditLog.OperationType.CREATE), anyString(), any(), anyString(), isNull(), anyString());
    }

    @Test
    void testSaveItem_DuplicateCode() {
        when(itemRepository.existsByCodeExclude(anyString(), isNull())).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () ->
                inventoryService.saveItem(testItem, testUser));
    }

    @Test
    void testUpdateItem_Success() {
        when(itemRepository.findById(1L)).thenReturn(Optional.of(testItem));
        when(itemRepository.existsByCodeExclude(anyString(), anyLong())).thenReturn(false);
        when(itemRepository.update(any(InventoryItem.class))).thenReturn(1);

        InventoryItem result = inventoryService.updateItem(testItem, testUser);

        assertNotNull(result);
        verify(itemRepository).update(testItem);
        verify(auditService).logWithValues(anyString(), eq(AuditLog.OperationType.UPDATE), anyString(), any(), anyString(), anyString(), anyString());
    }

    @Test
    void testUpdateItem_NullId() {
        testItem.setId(null);

        assertThrows(IllegalArgumentException.class, () ->
                inventoryService.updateItem(testItem, testUser));
    }

    @Test
    void testUpdateItem_NotFound() {
        when(itemRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () ->
                inventoryService.updateItem(testItem, testUser));
    }

    @Test
    void testUpdateItem_DuplicateCode() {
        when(itemRepository.findById(1L)).thenReturn(Optional.of(testItem));
        when(itemRepository.existsByCodeExclude(anyString(), anyLong())).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () ->
                inventoryService.updateItem(testItem, testUser));
    }

    @Test
    void testDeleteItem_Success() {
        when(itemRepository.findById(1L)).thenReturn(Optional.of(testItem));
        when(itemRepository.update(any(InventoryItem.class))).thenReturn(1);

        inventoryService.deleteItem(1L, testUser);

        assertFalse(testItem.getActive());
        verify(itemRepository).update(testItem);
    }

    @Test
    void testDeleteItem_NotFound() {
        when(itemRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () ->
                inventoryService.deleteItem(1L, testUser));
    }

    @Test
    void testGetAllItems() {
        List<InventoryItem> items = List.of(testItem);
        when(itemRepository.findAllActive()).thenReturn(items);

        List<InventoryItem> result = inventoryService.getAllItems();

        assertEquals(1, result.size());
        verify(itemRepository).findAllActive();
    }

    @Test
    void testSearchItems() {
        List<InventoryItem> items = List.of(testItem);
        when(itemRepository.search("test")).thenReturn(items);

        List<InventoryItem> result = inventoryService.searchItems("test");

        assertEquals(1, result.size());
        verify(itemRepository).search("test");
    }

    @Test
    void testGetLowStockItems() {
        List<InventoryItem> lowStockItems = List.of(testItem);
        when(itemRepository.findBelowMinimumStock()).thenReturn(lowStockItems);

        List<InventoryItem> result = inventoryService.getLowStockItems();

        assertEquals(1, result.size());
        verify(itemRepository).findBelowMinimumStock();
    }

    @Test
    void testGetTotalInventoryValue() {
        BigDecimal totalValue = new BigDecimal("1000.00");
        when(itemRepository.getTotalInventoryValue()).thenReturn(totalValue);

        BigDecimal result = inventoryService.getTotalInventoryValue();

        assertEquals(totalValue, result);
        verify(itemRepository).getTotalInventoryValue();
    }

    // ==================== PRUEBAS PARA MOVIMIENTOS ====================

    @Test
    void testRegisterEntry_Success() {
        BigDecimal quantity = new BigDecimal("10");
        BigDecimal unitCost = new BigDecimal("15.00");
        String documentNumber = "DOC001";
        String notes = "Nota de prueba";

        doAnswer(invocation -> {
            InventoryMovement movement = invocation.getArgument(0);
            movement.setId(1L);
            return null;
        }).when(movementRepository).save(any(InventoryMovement.class));
        when(itemRepository.updateStock(anyLong(), any(BigDecimal.class))).thenReturn(1);

        InventoryMovement result = inventoryService.registerEntry(
                testItem, testWarehouse, quantity, unitCost, documentNumber, notes, testSupplier, testUser);

        assertNotNull(result);
        assertEquals(InventoryMovement.MovementType.ENTRY, result.getType());
        verify(movementRepository).save(any(InventoryMovement.class));
        verify(itemRepository).updateStock(testItem.getId(), quantity);
        verify(auditService).logWithValues(anyString(), eq(AuditLog.OperationType.CREATE), anyString(), any(), anyString(), isNull(), anyString());
    }

    @Test
    void testRegisterOutput_Success() {
        BigDecimal quantity = new BigDecimal("5");
        String documentNumber = "DOC002";
        String notes = "Salida de prueba";

        when(itemRepository.findById(testItem.getId())).thenReturn(Optional.of(testItem));
        doAnswer(invocation -> {
            InventoryMovement movement = invocation.getArgument(0);
            movement.setId(2L);
            return null;
        }).when(movementRepository).save(any(InventoryMovement.class));
        when(itemRepository.updateStock(anyLong(), any(BigDecimal.class))).thenReturn(1);

        InventoryMovement result = inventoryService.registerOutput(
                testItem, testWarehouse, quantity, documentNumber, notes, testUser);

        assertNotNull(result);
        assertEquals(InventoryMovement.MovementType.OUTPUT, result.getType());
        verify(movementRepository).save(any(InventoryMovement.class));
        verify(itemRepository).updateStock(testItem.getId(), quantity.negate());
        verify(auditService).logWithValues(anyString(), eq(AuditLog.OperationType.CREATE), anyString(), any(), anyString(), isNull(), anyString());
    }

    @Test
    void testRegisterOutput_InsufficientStock() {
        BigDecimal quantity = new BigDecimal("150"); // Más que el stock disponible (100)
        String documentNumber = "DOC003";
        String notes = "Salida con stock insuficiente";

        when(itemRepository.findById(testItem.getId())).thenReturn(Optional.of(testItem));

        assertThrows(IllegalStateException.class, () ->
                inventoryService.registerOutput(testItem, testWarehouse, quantity, documentNumber, notes, testUser));
    }

    @Test
    void testRegisterAdjustment_Success() {
        BigDecimal quantityChange = new BigDecimal("-5");
        String documentNumber = "DOC004";
        String reason = "Ajuste de inventario";

        doAnswer(invocation -> {
            InventoryMovement movement = invocation.getArgument(0);
            movement.setId(3L);
            return null;
        }).when(movementRepository).save(any(InventoryMovement.class));
        when(itemRepository.updateStock(anyLong(), any(BigDecimal.class))).thenReturn(1);

        InventoryMovement result = inventoryService.registerAdjustment(
                testItem, testWarehouse, quantityChange, documentNumber, reason, testUser);

        assertNotNull(result);
        assertEquals(InventoryMovement.MovementType.ADJUSTMENT, result.getType());
        verify(movementRepository).save(any(InventoryMovement.class));
        verify(itemRepository).updateStock(testItem.getId(), quantityChange);
    }

    @Test
    void testGetAllMovements() {
        InventoryMovement movement = new InventoryMovement();
        movement.setId(1L);
        List<InventoryMovement> movements = List.of(movement);
        when(movementRepository.findAll()).thenReturn(movements);

        List<InventoryMovement> result = inventoryService.getAllMovements();

        assertEquals(1, result.size());
        verify(movementRepository).findAll();
    }

    @Test
    void testGetMovementsByItem() {
        when(itemRepository.findById(1L)).thenReturn(Optional.of(testItem));
        InventoryMovement movement = new InventoryMovement();
        movement.setId(1L);
        List<InventoryMovement> movements = List.of(movement);
        when(movementRepository.findByItem(testItem)).thenReturn(movements);

        List<InventoryMovement> result = inventoryService.getMovementsByItem(1L);

        assertEquals(1, result.size());
        verify(movementRepository).findByItem(testItem);
    }

    @Test
    void testGetMovementsByItem_NotFound() {
        when(itemRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () ->
                inventoryService.getMovementsByItem(1L));
    }

    @Test
    void testGetMovementsByDateRange() {
        LocalDateTime start = LocalDateTime.now().minusDays(7);
        LocalDateTime end = LocalDateTime.now();
        InventoryMovement movement = new InventoryMovement();
        movement.setId(1L);
        List<InventoryMovement> movements = List.of(movement);
        when(movementRepository.findByDateRange(start, end)).thenReturn(movements);

        List<InventoryMovement> result = inventoryService.getMovementsByDateRange(start, end);

        assertEquals(1, result.size());
        verify(movementRepository).findByDateRange(start, end);
    }

    // ==================== PRUEBAS PARA ALMACENES ====================

    @Test
    void testSaveWarehouse_Success() {
        when(warehouseRepository.existsByCodeExclude(anyString(), isNull())).thenReturn(false);
        doAnswer(invocation -> {
            Warehouse wh = invocation.getArgument(0);
            wh.setId(1L);
            return null;
        }).when(warehouseRepository).save(any(Warehouse.class));

        Warehouse result = inventoryService.saveWarehouse(testWarehouse);

        assertNotNull(result);
        verify(warehouseRepository).save(testWarehouse);
    }

    @Test
    void testSaveWarehouse_DuplicateCode() {
        when(warehouseRepository.existsByCodeExclude(anyString(), isNull())).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () ->
                inventoryService.saveWarehouse(testWarehouse));
    }

    @Test
    void testUpdateWarehouse_Success() {
        when(warehouseRepository.findById(1L)).thenReturn(Optional.of(testWarehouse));
        when(warehouseRepository.existsByCodeExclude(anyString(), anyLong())).thenReturn(false);
        when(warehouseRepository.update(any(Warehouse.class))).thenReturn(1);

        Warehouse result = inventoryService.updateWarehouse(testWarehouse);

        assertNotNull(result);
        verify(warehouseRepository).update(testWarehouse);
    }

    @Test
    void testUpdateWarehouse_NullId() {
        testWarehouse.setId(null);

        assertThrows(IllegalArgumentException.class, () ->
                inventoryService.updateWarehouse(testWarehouse));
    }

    @Test
    void testUpdateWarehouse_NotFound() {
        when(warehouseRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () ->
                inventoryService.updateWarehouse(testWarehouse));
    }

    @Test
    void testUpdateWarehouse_DuplicateCode() {
        when(warehouseRepository.findById(1L)).thenReturn(Optional.of(testWarehouse));
        when(warehouseRepository.existsByCodeExclude(anyString(), anyLong())).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () ->
                inventoryService.updateWarehouse(testWarehouse));
    }

    @Test
    void testGetAllWarehouses() {
        List<Warehouse> warehouses = List.of(testWarehouse);
        when(warehouseRepository.findAllActive()).thenReturn(warehouses);

        List<Warehouse> result = inventoryService.getAllWarehouses();

        assertEquals(1, result.size());
        verify(warehouseRepository).findAllActive();
    }

    @Test
    void testGetWarehouseById() {
        when(warehouseRepository.findById(1L)).thenReturn(Optional.of(testWarehouse));

        Optional<Warehouse> result = warehouseRepository.findById(1L);

        assertTrue(result.isPresent());
        assertEquals(testWarehouse, result.get());
        verify(warehouseRepository).findById(1L);
    }

    @Test
    void testGetWarehouseById_NotFound() {
        when(warehouseRepository.findById(1L)).thenReturn(Optional.empty());

        Optional<Warehouse> result = warehouseRepository.findById(1L);

        assertFalse(result.isPresent());
    }

    // ==================== PRUEBAS DE VALIDACIÓN ====================

    @Test
    void testValidateCategory_NullCategory() {
        assertThrows(IllegalArgumentException.class, () ->
                inventoryService.saveCategory(null));
    }

    @Test
    void testValidateCategory_EmptyCode() {
        testCategory.setCode("");
        assertThrows(IllegalArgumentException.class, () ->
                inventoryService.saveCategory(testCategory));
    }

    @Test
    void testValidateCategory_NullName() {
        testCategory.setName(null);
        assertThrows(IllegalArgumentException.class, () ->
                inventoryService.saveCategory(testCategory));
    }

    @Test
    void testValidateItem_NullItem() {
        assertThrows(IllegalArgumentException.class, () ->
                inventoryService.saveItem(null, testUser));
    }

    @Test
    void testValidateItem_EmptyCode() {
        testItem.setCode("");
        assertThrows(IllegalArgumentException.class, () ->
                inventoryService.saveItem(testItem, testUser));
    }

    @Test
    void testValidateItem_NullName() {
        testItem.setName(null);
        assertThrows(IllegalArgumentException.class, () ->
                inventoryService.saveItem(testItem, testUser));
    }

    @Test
    void testValidateItem_NullCategory() {
        testItem.setCategory(null);
        assertThrows(IllegalArgumentException.class, () ->
                inventoryService.saveItem(testItem, testUser));
    }

    @Test
    void testValidateItem_NegativeCost() {
        testItem.setUnitCost(new BigDecimal("-10.00"));
        assertThrows(IllegalArgumentException.class, () ->
                inventoryService.saveItem(testItem, testUser));
    }

    @Test
    void testValidateWarehouse_NullWarehouse() {
        assertThrows(IllegalArgumentException.class, () ->
                inventoryService.saveWarehouse(null));
    }

    @Test
    void testValidateWarehouse_EmptyCode() {
        testWarehouse.setCode("");
        assertThrows(IllegalArgumentException.class, () ->
                inventoryService.saveWarehouse(testWarehouse));
    }

    @Test
    void testValidateWarehouse_NullName() {
        testWarehouse.setName(null);
        assertThrows(IllegalArgumentException.class, () ->
                inventoryService.saveWarehouse(testWarehouse));
    }

    @Test
    void testValidateMovement_NullItem() {
        assertThrows(IllegalArgumentException.class, () ->
                inventoryService.registerEntry(null, testWarehouse, new BigDecimal("10"),
                        new BigDecimal("10.00"), "DOC001", "Test", testSupplier, testUser));
    }

    @Test
    void testValidateMovement_NullWarehouse() {
        assertThrows(IllegalArgumentException.class, () ->
                inventoryService.registerEntry(testItem, null, new BigDecimal("10"),
                        new BigDecimal("10.00"), "DOC001", "Test", testSupplier, testUser));
    }

    @Test
    void testValidateMovement_NullQuantity() {
        assertThrows(IllegalArgumentException.class, () ->
                inventoryService.registerEntry(testItem, testWarehouse, null,
                        new BigDecimal("10.00"), "DOC001", "Test", testSupplier, testUser));
    }

    @Test
    void testValidateMovement_ZeroQuantity() {
        assertThrows(IllegalArgumentException.class, () ->
                inventoryService.registerEntry(testItem, testWarehouse, BigDecimal.ZERO,
                        new BigDecimal("10.00"), "DOC001", "Test", testSupplier, testUser));
    }

    @Test
    void testValidateMovement_NullDocumentNumber() {
        assertThrows(IllegalArgumentException.class, () ->
                inventoryService.registerEntry(testItem, testWarehouse, new BigDecimal("10"),
                        new BigDecimal("10.00"), null, "Test", testSupplier, testUser));
    }
}
