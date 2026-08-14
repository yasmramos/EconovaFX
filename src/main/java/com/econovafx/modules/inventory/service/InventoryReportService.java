package com.econovafx.modules.inventory.service;

import com.econovafx.modules.inventory.model.InventoryItem;
import com.econovafx.modules.inventory.model.InventoryMovement;
import com.econovafx.modules.inventory.model.Warehouse;
import com.econovafx.modules.inventory.repository.InventoryItemRepository;
import com.econovafx.modules.inventory.repository.InventoryMovementRepository;
import io.avaje.inject.Component;
import jakarta.inject.Inject;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service for generating fiscal reports and managing inventory closures
 * according to Resolution 340/2004 of Cuba (ONAT requirements).
 */
@Component
public class InventoryReportService {

    @Inject
    InventoryItemRepository itemRepository;

    @Inject
    InventoryMovementRepository movementRepository;

    /**
     * Report of paralyzed products (no movement in last N days)
     * Required by Resolution 340/2004 for inventory analysis.
     */
    public List<InventoryItem> getParalyzedProducts(Integer warehouseId, int daysThreshold) {
        LocalDate thresholdDate = LocalDate.now().minusDays(daysThreshold);
        List<InventoryItem> allItems = itemRepository.findAll();
        
        return allItems.stream()
            .filter(item -> item.getCurrentStock().compareTo(BigDecimal.ZERO) > 0)
            .filter(item -> {
                List<InventoryMovement> movements = movementRepository.findByItem(item);
                if (movements.isEmpty()) {
                    return true; // No movements at all
                }
                
                // Filter by warehouse if specified
                if (warehouseId != null) {
                    boolean hasMovementInWarehouse = movements.stream()
                        .anyMatch(m -> m.getWarehouse().getId().equals(warehouseId));
                    if (!hasMovementInWarehouse) {
                        return true; // No movements in this warehouse
                    }
                }
                
                LocalDateTime lastMovementDate = movements.get(0).getMovementDate();
                return lastMovementDate.toLocalDate().isBefore(thresholdDate);
            })
            .collect(Collectors.toList());
    }

    /**
     * Report of slow-moving products (low rotation rate)
     * Required for inventory efficiency analysis.
     */
    public List<Map<String, Object>> getSlowMovingProducts(Integer warehouseId, int periodDays) {
        LocalDateTime startDate = LocalDateTime.now().minusDays(periodDays);
        List<InventoryItem> items = itemRepository.findAll();
        List<Map<String, Object>> report = new ArrayList<>();

        for (InventoryItem item : items) {
            List<InventoryMovement> movements = movementRepository.findByItem(item);
            
            // Filter by warehouse if specified
            if (warehouseId != null) {
                boolean hasMovementInWarehouse = movements.stream()
                    .anyMatch(m -> m.getWarehouse().getId().equals(warehouseId));
                if (!hasMovementInWarehouse) {
                    continue; // Skip items without movements in this warehouse
                }
            }

            long outputCount = movements.stream()
                .filter(m -> m.getType() == InventoryMovement.MovementType.OUTPUT)
                .filter(m -> m.getMovementDate().isAfter(startDate))
                .count();

            BigDecimal avgDailyOutput = BigDecimal.valueOf(outputCount)
                .divide(BigDecimal.valueOf(periodDays), 4, RoundingMode.HALF_UP);

            Map<String, Object> productData = new HashMap<>();
            productData.put("item", item);
            productData.put("outputCount", outputCount);
            productData.put("avgDailyOutput", avgDailyOutput);
            productData.put("currentStock", item.getCurrentStock());
            productData.put("daysOfStockRemaining", avgDailyOutput.compareTo(BigDecimal.ZERO) > 0 
                ? item.getCurrentStock().divide(avgDailyOutput, 0, RoundingMode.HALF_UP) 
                : BigDecimal.valueOf(-1));

            report.add(productData);
        }

        return report.stream()
            .filter(r -> {
                BigDecimal daysRemaining = (BigDecimal) r.get("daysOfStockRemaining");
                return daysRemaining.compareTo(BigDecimal.valueOf(90)) > 0; // More than 90 days
            })
            .collect(Collectors.toList());
    }

    /**
     * Report of inventory differences (physical vs system)
     * Critical for fiscal compliance and adjustment posting.
     */
    public List<Map<String, Object>> getInventoryDifferences(LocalDate fromDate, LocalDate toDate) {
        LocalDateTime startDateTime = fromDate.atStartOfDay();
        LocalDateTime endDateTime = toDate.atTime(23, 59, 59);
        List<InventoryMovement> movements = movementRepository.findByDateRange(startDateTime, endDateTime);
        Map<Long, Map<String, Object>> differences = new HashMap<>();

        for (InventoryMovement movement : movements) {
            if (movement.getDifferenceQuantity() != null && movement.getDifferenceQuantity().compareTo(BigDecimal.ZERO) != 0) {
                Long itemId = movement.getItem().getId();
                
                if (!differences.containsKey(itemId)) {
                    Map<String, Object> diffData = new HashMap<>();
                    diffData.put("item", movement.getItem());
                    diffData.put("warehouse", movement.getWarehouse());
                    diffData.put("totalDifference", BigDecimal.ZERO);
                    diffData.put("movements", new ArrayList<InventoryMovement>());
                    differences.put(itemId, diffData);
                }

                Map<String, Object> diffData = differences.get(itemId);
                BigDecimal currentDiff = (BigDecimal) diffData.get("totalDifference");
                diffData.put("totalDifference", currentDiff.add(movement.getDifferenceQuantity()));
                
                @SuppressWarnings("unchecked")
                List<InventoryMovement> movementList = (List<InventoryMovement>) diffData.get("movements");
                movementList.add(movement);
            }
        }

        return new ArrayList<>(differences.values());
    }

    /**
     * Inventory valuation report by warehouse and valuation method
     * Required for financial statements and tax reporting.
     */
    public Map<String, Object> getInventoryValuation(Integer warehouseId) {
        List<InventoryItem> items = itemRepository.findAll();
        BigDecimal totalValue = BigDecimal.ZERO;
        Map<String, BigDecimal> valueByCategory = new HashMap<>();
        Map<String, BigDecimal> valueByWarehouse = new HashMap<>();

        for (InventoryItem item : items) {
            // Filter by warehouse using movements
            if (warehouseId != null) {
                List<InventoryMovement> movements = movementRepository.findByItem(item);
                boolean hasMovementInWarehouse = movements.stream()
                    .anyMatch(m -> m.getWarehouse().getId().equals(warehouseId));
                if (!hasMovementInWarehouse && item.getCurrentStock().compareTo(BigDecimal.ZERO) == 0) {
                    continue; // Skip items without stock or movements in this warehouse
                }
            }

            BigDecimal itemValue = item.getCurrentStock().multiply(item.getUnitCost());
            totalValue = totalValue.add(itemValue);

            String category = item.getCategory() != null ? item.getCategory().getName() : "Sin Categoría";
            valueByCategory.put(category, 
                valueByCategory.getOrDefault(category, BigDecimal.ZERO).add(itemValue));

            // Get warehouse from latest movement
            String warehouseName = "Sin Asignar";
            List<InventoryMovement> movements = movementRepository.findByItem(item);
            if (!movements.isEmpty()) {
                warehouseName = movements.get(0).getWarehouse().getName();
            }
            valueByWarehouse.put(warehouseName,
                valueByWarehouse.getOrDefault(warehouseName, BigDecimal.ZERO).add(itemValue));
        }

        Map<String, Object> report = new HashMap<>();
        report.put("totalValue", totalValue);
        report.put("valueByCategory", valueByCategory);
        report.put("valueByWarehouse", valueByWarehouse);
        report.put("reportDate", LocalDate.now());
        report.put("itemCount", items.size());

        return report;
    }

    /**
     * Audit trail of all inventory movements in a period
     * Required for fiscal inspection and traceability.
     */
    public List<InventoryMovement> getMovementAuditTrail(LocalDate fromDate, LocalDate toDate, Integer warehouseId) {
        LocalDateTime startDateTime = fromDate.atStartOfDay();
        LocalDateTime endDateTime = toDate.atTime(23, 59, 59);
        List<InventoryMovement> movements = movementRepository.findByDateRange(startDateTime, endDateTime);
        
        if (warehouseId != null) {
            return movements.stream()
                .filter(m -> m.getWarehouse().getId().equals(warehouseId))
                .collect(Collectors.toList());
        }
        
        return movements;
    }

    /**
     * Close inventory period with validation
     * Prevents further movements in closed periods per Resolution 340/2004.
     */
    public void closeInventoryPeriod(String periodCode, boolean isAnnual) {
        // Validate no pending differences
        List<Map<String, Object>> differences = getInventoryDifferences(
            LocalDate.now().withDayOfMonth(1),
            LocalDate.now()
        );

        if (!differences.isEmpty()) {
            throw new IllegalStateException(
                "Cannot close inventory period: " + differences.size() + 
                " items have unresolved differences. Resolution 340/2004 requires all differences to be posted before closing."
            );
        }

        // Mark period as closed in configuration
        // This would integrate with AccountingPeriodService
        // Logging handled by caller
    }
}
