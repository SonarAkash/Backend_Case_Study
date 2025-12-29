package com.bynry.inventory.controller;

import com.bynry.inventory.dto.LowStockAlertDTO;
import com.bynry.inventory.service.InventoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@RestController
@RequestMapping("/api/companies")
public class InventoryController {

    @Autowired
    private InventoryService inventoryService;

    @GetMapping("/{companyId}/alerts/low-stock")
    public ResponseEntity<Map<String, Object>> getLowStockAlerts(
            @PathVariable Long companyId) {
        
        List<LowStockAlertDTO> alerts = inventoryService.getLowStockAlerts(companyId);
        
        Map<String, Object> response = new HashMap<>();
        response.put("alerts", alerts);
        response.put("total_alerts", alerts.size());
        
        return ResponseEntity.ok(response);
    }
}
