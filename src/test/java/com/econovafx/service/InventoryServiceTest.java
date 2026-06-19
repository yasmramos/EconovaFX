package com.econovafx.service;

import com.econovafx.domain.*;
import com.econovafx.repository.*;
import com.econovafx.service.AuditService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Tests unitarios para el servicio de inventarios.
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

    private InventoryService inventoryService;

    @BeforeEach
    void setUp() {
        inventoryService = new InventoryService();
        // Inyectar mocks manualmente (ya que usamos @Inject en lugar de constructor)
        try {
            var field = InventoryService.class.getDeclaredField("itemRepository");
            field.setAccessible(true);
            field.set(inventoryService, itemRepository);

            field = InventoryService.class.getDeclaredField("categoryRepository");
            field.setAccessible(true);
            field.set(inventoryService, categoryRepository);

            field = InventoryService.class.getDeclaredField("movementRepository");
            field.setAccessible(true);
            field.set(inventoryService, movementRepository);

            field = InventoryService.class.getDeclaredField("warehouseRepository");
            field.setAccessible(true);
            field.set(inventoryService, warehouseRepository);

            field = InventoryService.class.getDeclaredField("transactionService");
            field.setAccessible(true);
            field.set(inventoryService, transactionService);

            field = InventoryService.class.getDeclaredField("auditService");
            field.setAccessible(true);
            field.set(inventoryService, auditService);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void testSaveCategory_Success() {
        InventoryCategory category = new InventoryCategory();
        category.setCode("CAT001");
        category.setName("Productos Terminados");

        when(categoryRepository.existsByCodeExclude(anyString(), isNull())).thenReturn(false);
        doAnswer(invocation -> {
            ((InventoryCategory) invocation.getArgument(0)).setId(1L);
            return null;
        }).when(categoryRepository).save(any(InventoryCategory.class));

        InventoryCategory saved = inventoryService.saveCategory(category);

        assertNotNull(saved);
        verify(categoryRepository).save(category);
        verify(categoryRepository).existsByCodeExclude("CAT001", null);
    }

    @Test
    void testSaveCategory_DuplicateCode() {
        InventoryCategory category = new InventoryCategory();
        category.setCode("CAT001");
        category.setName("Productos Terminados");

        when(categoryRepository.existsByCodeExclude(anyString(), isNull())).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () -> inventoryService.saveCategory(category));
    }

    @Test
    void testDeleteCategory_WithProducts() {
        InventoryCategory category = new InventoryCategory();
        category.setId(1L);
        category.setCode("CAT001");
        category.setName("Productos Terminados");

        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(itemRepository.findByCategory(category)).thenReturn(List.of(new InventoryItem()));

        assertThrows(IllegalStateException.class, () -> inventoryService.deleteCategory(1L));
        verify(categoryRepository, never()).deleteById(1L);
    }

    @Test
    void testSaveItem_Success() {
        InventoryItem item = new InventoryItem();
        item.setCode("PROD001");
        item.setName("Producto de Prueba");
        item.setUnitCost(new BigDecimal("10.00"));
        item.setSalePrice(new BigDecimal("15.00"));
        item.setCurrentStock(BigDecimal.ZERO);
        item.setMinimumStock(new BigDecimal("10"));
        item.setMaximumStock(new BigDecimal("100"));

        InventoryCategory category = new InventoryCategory();
        category.setId(1L);
        item.setCategory(category);

        User user = new User();
        user.setUsername("testuser");

        when(itemRepository.existsByCodeExclude(anyString(), isNull())).thenReturn(false);
        doAnswer(invocation -> {
            ((InventoryItem) invocation.getArgument(0)).setId(1L);
            return null;
        }).when(itemRepository).save(any(InventoryItem.class));

        InventoryItem saved = inventoryService.saveItem(item, user);

        assertNotNull(saved);
        verify(itemRepository).save(item);
        verify(auditService).logWithValues(anyString(), any(AuditLog.OperationType.class), anyString(), anyLong(), anyString(), isNull(), anyString());
    }

    @Test
    void testRegisterEntry_Success() {
        InventoryItem item = new InventoryItem();
        item.setId(1L);
        item.setCode("PROD001");
        item.setName("Producto de Prueba");
        item.setUnitCost(new BigDecimal("10.00"));
        item.setCurrentStock(new BigDecimal("50"));

        Warehouse warehouse = new Warehouse();
        warehouse.setId(1L);

        User user = new User();
        user.setUsername("testuser");

        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));
        doAnswer(invocation -> {
            InventoryMovement m = (InventoryMovement) invocation.getArgument(0);
            m.setId(1L);
            return null;
        }).when(movementRepository).save(any(InventoryMovement.class));

        InventoryMovement movement = inventoryService.registerEntry(
            item, warehouse, new BigDecimal("10"), new BigDecimal("12.00"),
            "DOC001", "Entrada de prueba", null, user
        );

        assertNotNull(movement);
        assertEquals(InventoryMovement.MovementType.ENTRY, movement.getType());
        verify(movementRepository).save(any(InventoryMovement.class));
        verify(itemRepository).updateStock(eq(1L), any(BigDecimal.class));
        verify(auditService).logWithValues(anyString(), any(AuditLog.OperationType.class), anyString(), anyLong(), anyString(), isNull(), anyString());
    }

    @Test
    void testRegisterOutput_InsufficientStock() {
        InventoryItem item = new InventoryItem();
        item.setId(1L);
        item.setCurrentStock(new BigDecimal("5"));

        Warehouse warehouse = new Warehouse();
        warehouse.setId(1L);

        User user = new User();
        user.setUsername("testuser");

        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));

        assertThrows(IllegalStateException.class, () -> 
            inventoryService.registerOutput(item, warehouse, new BigDecimal("10"), "DOC001", "Salida", user)
        );
    }

    @Test
    void testRegisterOutput_Success() {
        InventoryItem item = new InventoryItem();
        item.setId(1L);
        item.setCode("PROD001");
        item.setName("Producto de Prueba");
        item.setUnitCost(new BigDecimal("10.00"));
        item.setCurrentStock(new BigDecimal("50"));

        Warehouse warehouse = new Warehouse();
        warehouse.setId(1L);
        warehouse.setValuationMethod(ValuationMethod.WEIGHTED_AVERAGE); // Usar Promedio Ponderado para evitar necesidad de entradas previas

        User user = new User();
        user.setUsername("testuser");

        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));
        doAnswer(invocation -> {
            InventoryMovement m = (InventoryMovement) invocation.getArgument(0);
            m.setId(1L);
            return null;
        }).when(movementRepository).save(any(InventoryMovement.class));

        InventoryMovement movement = inventoryService.registerOutput(
            item, warehouse, new BigDecimal("10"), "DOC001", "Salida de prueba", user
        );

        assertNotNull(movement);
        assertEquals(InventoryMovement.MovementType.OUTPUT, movement.getType());
        verify(movementRepository).save(any(InventoryMovement.class));
        verify(itemRepository).updateStock(eq(1L), any(BigDecimal.class));
    }

    @Test
    void testGetLowStockItems() {
        when(itemRepository.findBelowMinimumStock()).thenReturn(List.of(new InventoryItem()));

        List<InventoryItem> lowStock = inventoryService.getLowStockItems();

        assertNotNull(lowStock);
        assertEquals(1, lowStock.size());
        verify(itemRepository).findBelowMinimumStock();
    }

    @Test
    void testGetTotalInventoryValue() {
        when(itemRepository.getTotalInventoryValue()).thenReturn(new BigDecimal("1000.00"));

        BigDecimal value = inventoryService.getTotalInventoryValue();

        assertNotNull(value);
        assertEquals(new BigDecimal("1000.00"), value);
    }

    @Test
    void testSaveWarehouse_Success() {
        Warehouse warehouse = new Warehouse();
        warehouse.setCode("WH001");
        warehouse.setName("Almacén Principal");

        when(warehouseRepository.existsByCodeExclude(anyString(), isNull())).thenReturn(false);
        doAnswer(invocation -> {
            ((Warehouse) invocation.getArgument(0)).setId(1L);
            return null;
        }).when(warehouseRepository).save(any(Warehouse.class));

        Warehouse saved = inventoryService.saveWarehouse(warehouse);

        assertNotNull(saved);
        verify(warehouseRepository).save(warehouse);
    }

    @Test
    void testGetAllWarehouses() {
        when(warehouseRepository.findAllActive()).thenReturn(List.of(new Warehouse()));

        List<Warehouse> warehouses = inventoryService.getAllWarehouses();

        assertNotNull(warehouses);
        assertEquals(1, warehouses.size());
    }

    @Test
    void testIsModuleClosedForPeriod() {
        AccountingPeriod period = new AccountingPeriod();
        period.setName("Enero 2025");

        boolean closed = inventoryService.isModuleClosedForPeriod(period);

        assertFalse(closed); // El módulo está abierto por defecto
    }

    @Test
    void testGetMovementsForPeriod() {
        AccountingPeriod period = new AccountingPeriod();
        period.setStartDate(java.time.LocalDate.now().minusDays(30));
        period.setEndDate(java.time.LocalDate.now());

        when(movementRepository.findByDateRange(any(), any())).thenReturn(List.of(new InventoryMovement()));

        List<InventoryMovement> movements = inventoryService.getMovementsForPeriod(period);

        assertNotNull(movements);
        assertEquals(1, movements.size());
    }

    @Test
    void testSearchItems() {
        when(itemRepository.search("producto")).thenReturn(List.of(new InventoryItem()));

        List<InventoryItem> results = inventoryService.searchItems("producto");

        assertNotNull(results);
        assertEquals(1, results.size());
        verify(itemRepository).search("producto");
    }

    @Test
    void testGetMovementsByItem() {
        InventoryItem item = new InventoryItem();
        item.setId(1L);

        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));
        when(movementRepository.findByItem(item)).thenReturn(List.of(new InventoryMovement()));

        List<InventoryMovement> movements = inventoryService.getMovementsByItem(1L);

        assertNotNull(movements);
        assertEquals(1, movements.size());
    }

    @Test
    void testUpdateItem_Success() {
        InventoryItem existingItem = new InventoryItem();
        existingItem.setId(1L);
        existingItem.setCode("PROD001");
        existingItem.setName("Producto Original");

        InventoryItem updatedItem = new InventoryItem();
        updatedItem.setId(1L);
        updatedItem.setCode("PROD001");
        updatedItem.setName("Producto Actualizado");
        updatedItem.setUnitCost(new BigDecimal("12.00"));
        updatedItem.setSalePrice(new BigDecimal("18.00"));

        InventoryCategory category = new InventoryCategory();
        category.setId(1L);
        updatedItem.setCategory(category);

        User user = new User();
        user.setUsername("testuser");

        when(itemRepository.findById(1L)).thenReturn(Optional.of(existingItem));
        when(itemRepository.existsByCodeExclude("PROD001", 1L)).thenReturn(false);

        InventoryItem result = inventoryService.updateItem(updatedItem, user);

        assertNotNull(result);
        verify(itemRepository).update(updatedItem);
        verify(auditService).logWithValues(anyString(), any(AuditLog.OperationType.class), anyString(), anyLong(), anyString(), anyString(), anyString());
    }

    @Test
    void testCalculateOutputCost_WeightedAverage() {
        Warehouse warehouse = new Warehouse();
        warehouse.setId(1L);
        warehouse.setCode("WH001");
        warehouse.setName("Almacén Principal");
        warehouse.setValuationMethod(ValuationMethod.WEIGHTED_AVERAGE);

        InventoryItem item = new InventoryItem();
        item.setId(1L);
        item.setCode("PROD001");
        item.setName("Producto Prueba");
        item.setUnitCost(new BigDecimal("10.50"));
        item.setCurrentStock(new BigDecimal("100"));

        BigDecimal quantity = new BigDecimal("5");
        BigDecimal expectedCost = new BigDecimal("52.50"); // 10.50 * 5

        BigDecimal result = inventoryService.calculateOutputCost(item, warehouse, quantity);

        assertEquals(0, expectedCost.compareTo(result), "El costo debe ser igual al costo unitario por cantidad en promedio ponderado");
    }

    @Test
    void testCalculateOutputCost_Fifo() {
        Warehouse warehouse = new Warehouse();
        warehouse.setId(1L);
        warehouse.setCode("WH001");
        warehouse.setName("Almacén FIFO");
        warehouse.setValuationMethod(ValuationMethod.FIFO);

        InventoryItem item = new InventoryItem();
        item.setId(1L);
        item.setCode("PROD001");
        item.setName("Producto FIFO");

        // Crear movimientos de entrada con diferentes costos (simulando compras en diferentes fechas)
        InventoryMovement entry1 = new InventoryMovement();
        entry1.setType(InventoryMovement.MovementType.ENTRY);
        entry1.setItem(item);
        entry1.setWarehouse(warehouse);
        entry1.setQuantity(new BigDecimal("10"));
        entry1.setUnitCost(new BigDecimal("10.00"));
        entry1.setMovementDate(LocalDateTime.now().minusDays(10));

        InventoryMovement entry2 = new InventoryMovement();
        entry2.setType(InventoryMovement.MovementType.ENTRY);
        entry2.setItem(item);
        entry2.setWarehouse(warehouse);
        entry2.setQuantity(new BigDecimal("20"));
        entry2.setUnitCost(new BigDecimal("12.00"));
        entry2.setMovementDate(LocalDateTime.now().minusDays(5));

        when(movementRepository.findEntriesByItemAndWarehouse(1L, 1L))
                .thenReturn(List.of(entry1, entry2));

        // Solicitar 15 unidades: 10 a $10 + 5 a $12 = $100 + $60 = $160
        BigDecimal quantity = new BigDecimal("15");
        BigDecimal expectedCost = new BigDecimal("160.00");

        BigDecimal result = inventoryService.calculateOutputCost(item, warehouse, quantity);

        assertEquals(0, expectedCost.compareTo(result), "El costo FIFO debe calcularse correctamente");
    }

    @Test
    void testCalculateOutputCost_FifoInsufficientStock() {
        Warehouse warehouse = new Warehouse();
        warehouse.setId(1L);
        warehouse.setCode("WH001");
        warehouse.setName("Almacén FIFO");
        warehouse.setValuationMethod(ValuationMethod.FIFO);

        InventoryItem item = new InventoryItem();
        item.setId(1L);
        item.setCode("PROD001");
        item.setName("Producto FIFO");

        // Solo hay 10 unidades en total
        InventoryMovement entry1 = new InventoryMovement();
        entry1.setType(InventoryMovement.MovementType.ENTRY);
        entry1.setItem(item);
        entry1.setWarehouse(warehouse);
        entry1.setQuantity(new BigDecimal("10"));
        entry1.setUnitCost(new BigDecimal("10.00"));
        entry1.setMovementDate(LocalDateTime.now().minusDays(10));

        when(movementRepository.findEntriesByItemAndWarehouse(1L, 1L))
                .thenReturn(List.of(entry1));

        // Solicitar 15 unidades (más de lo disponible)
        BigDecimal quantity = new BigDecimal("15");

        assertThrows(IllegalStateException.class, () -> {
            inventoryService.calculateOutputCost(item, warehouse, quantity);
        }, "Debe lanzar excepción cuando no hay stock suficiente para FIFO");
    }
}
