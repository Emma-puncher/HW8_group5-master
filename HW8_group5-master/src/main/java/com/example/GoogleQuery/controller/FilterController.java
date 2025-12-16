package com.example.GoogleQuery.controller;

import com.example.GoogleQuery.service.FilterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * FilterController - 篩選 API 控制器
 * 處理地區、功能等篩選選項的查詢
 */
@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/filters")
public class FilterController {

    @Autowired
    private FilterService filterService;

    /**
     * 取得所有可用的篩選選項
     * GET /api/filters/all
     * 
     * @return 所有篩選選項（地區、功能）
     */
    @GetMapping("/all")
    public ResponseEntity<Map<String, Object>> getAllFilters() {
        try {
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("districts", filterService.getAllDistricts());
            response.put("features", filterService.getAllFeatures());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "取得篩選選項失敗: " + e.getMessage());
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                 .body(errorResponse);
        }
    }

    /**
     * 取得所有台北市行政區列表
     * GET /api/filters/districts
     * 
     * @return 行政區列表
     */
    @GetMapping("/districts")
    public ResponseEntity<Map<String, Object>> getDistricts() {
        try {
            List<String> districts = filterService.getAllDistricts();
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("total", districts.size());
            response.put("districts", districts);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "取得行政區列表失敗: " + e.getMessage());
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                 .body(errorResponse);
        }
    }

    /**
     * 取得所有功能性選項
     * GET /api/filters/features
     * 
     * @return 功能列表（不限時、有插座、CP值高、有wifi）
     */
    @GetMapping("/features")
    public ResponseEntity<Map<String, Object>> getFeatures() {
        try {
            List<String> features = filterService.getAllFeatures();
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("total", features.size());
            response.put("features", features);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "取得功能列表失敗: " + e.getMessage());
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                 .body(errorResponse);
        }
    }

    /**
     * 取得特定地區的咖啡廳數量
     * GET /api/filters/districts/大安區/count
     * 
     * @param district 行政區名稱
     * @return 咖啡廳數量
     */
    @GetMapping("/districts/{district}/count")
    public ResponseEntity<Map<String, Object>> getDistrictCafeCount(
            @PathVariable String district) {
        
        try {
            int count = filterService.getCafeCountByDistrict(district);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("district", district);
            response.put("count", count);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "取得咖啡廳數量失敗: " + e.getMessage());
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                 .body(errorResponse);
        }
    }

    /**
     * 取得特定功能的咖啡廳數量
     * GET /api/filters/features/不限時/count
     * 
     * @param feature 功能名稱
     * @return 咖啡廳數量
     */
    @GetMapping("/features/{feature}/count")
    public ResponseEntity<Map<String, Object>> getFeatureCafeCount(
            @PathVariable String feature) {
        
        try {
            int count = filterService.getCafeCountByFeature(feature);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("feature", feature);
            response.put("count", count);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "取得咖啡廳數量失敗: " + e.getMessage());
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                 .body(errorResponse);
        }
    }

    /**
     * 驗證篩選條件是否有效
     * POST /api/filters/validate
     * Body: {"districts": ["大安區"], "features": ["不限時"]}
     * 
     * @param filterData 篩選條件
     * @return 驗證結果
     */
    @PostMapping("/validate")
    public ResponseEntity<Map<String, Object>> validateFilters(
            @RequestBody Map<String, Object> filterData) {
        
        try {
            boolean isValid = filterService.validateFilters(filterData);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("valid", isValid);
            
            if (!isValid) {
                response.put("message", "部分篩選條件無效");
            }
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "驗證篩選條件失敗: " + e.getMessage());
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                 .body(errorResponse);
        }
    }

    /**
     * 取得各區域的咖啡廳分布統計
     * GET /api/filters/statistics/districts
     * 
     * @return 各區域咖啡廳數量統計
     */
    @GetMapping("/statistics/districts")
    public ResponseEntity<Map<String, Object>> getDistrictStatistics() {
        try {
            Map<String, Integer> statistics = filterService.getDistrictStatistics();
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("statistics", statistics);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "取得統計資料失敗: " + e.getMessage());
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                 .body(errorResponse);
        }
    }

    /**
     * 取得各功能的咖啡廳分布統計
     * GET /api/filters/statistics/features
     * 
     * @return 各功能咖啡廳數量統計
     */
    @GetMapping("/statistics/features")
    public ResponseEntity<Map<String, Object>> getFeatureStatistics() {
        try {
            Map<String, Integer> statistics = filterService.getFeatureStatistics();
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("statistics", statistics);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "取得統計資料失敗: " + e.getMessage());
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                 .body(errorResponse);
        }
    }

    /**
     * 健康檢查端點
     * GET /api/filters/health
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> healthCheck() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "ok");
        response.put("service", "FilterController");
        response.put("timestamp", System.currentTimeMillis());
        
        return ResponseEntity.ok(response);
    }
}



