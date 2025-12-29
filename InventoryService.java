package com.bynry.inventory.service;

import com.bynry.inventory.dto.LowStockAlertDTO;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import java.util.ArrayList;
import java.util.List;


@Service
public class InventoryService {

    // Assuming we have a Repository interface defined elsewhere
    @Autowired
    private InventoryRepository inventoryRepo;

    public List<LowStockAlertDTO> getLowStockAlerts(Long companyId) {
        List<LowStockAlertDTO> alerts = new ArrayList<>();

        // 1. Fetch all inventory items for this company where quantity <= threshold
        // (Assuming a custom JPQL query in Repository that joins Product, Warehouse, and Supplier)
        List<InventoryProjection> lowStockItems = inventoryRepo.findLowStockItems(companyId);

        for (InventoryProjection item : lowStockItems) {
            
            // Business Rule: Only alert for products with recent sales
            if (!hasRecentSales(item.getProductId(), 30)) {
                continue; 
            }

            // Logic: Calculate Days Until Stockout
            // days = current_stock / avg_daily_sales
            double avgDailySales = getAverageDailySales(item.getProductId(), item.getWarehouseId());
            int daysUntilStockout = 999; // Default if no sales velocity
            
            if (avgDailySales > 0) {
                daysUntilStockout = (int) (item.getQuantity() / avgDailySales);
            }

            // Map DB result to DTO
            LowStockAlertDTO alert = LowStockAlertDTO.builder()
                .productId(item.getProductId())
                .productName(item.getProductName())
                .sku(item.getSku())
                .warehouseId(item.getWarehouseId())
                .warehouseName(item.getWarehouseName())
                .currentStock(item.getQuantity())
                .threshold(item.getReorderLevel())
                .daysUntilStockout(daysUntilStockout)
                .supplier(LowStockAlertDTO.SupplierDTO.builder()
                    .id(item.getSupplierId())
                    .name(item.getSupplierName())
                    .contactEmail(item.getSupplierEmail())
                    .build())
                .build();

            alerts.add(alert);
        }
        return alerts;
    }

   //  Checks if items were sold recently
    private boolean hasRecentSales(Long productId, int days) {
        // LOGIC: Join tables to find history for this product
        // SQL: 
        // SELECT COUNT(*) FROM inventory_history h
        // JOIN inventory i ON h.inventory_id = i.id
        // WHERE i.product_id = :productId 
        // AND h.reason = 'SALE'
        // AND h.changed_at > (NOW() - :days)
        
        // If count > 0, it means we have recent activity.
        return true; 
    }

    // Calculates velocity (How many units we sell per day)
    private double getAverageDailySales(Long productId, Long warehouseId) {
        // LOGIC: 
        // 1. Identify the specific Inventory Record
        //    SELECT id FROM inventory 
        //    WHERE product_id = :productId AND warehouse_id = :warehouseId
        
        // 2. Sum sales from history for THAT inventory record
        //    SELECT SUM(ABS(change_amount)) FROM inventory_history 
        //    WHERE inventory_id = :inventoryId 
        //    AND reason = 'SALE' 
        //    AND changed_at > (NOW() - 30 days)
        
    
        //   return total_sold / 30.0
        
        return 5.0; // Mock value since we don't have a live DB connection
    }
}
