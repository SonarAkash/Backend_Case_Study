package com.bynry.inventory.dto;

import lombok.Builder;
import lombok.Data;

@Data // Lombok for getters/setters
@Builder
public class LowStockAlertDTO {
    private Long productId;
    private String productName;
    private String sku;
    private Long warehouseId;
    private String warehouseName;
    private int currentStock;
    private int threshold;
    private int daysUntilStockout;
    private SupplierDTO supplier;

    @Data
    @Builder
    public static class SupplierDTO {
        private Long id;
        private String name;
        private String contactEmail;
    }
}
