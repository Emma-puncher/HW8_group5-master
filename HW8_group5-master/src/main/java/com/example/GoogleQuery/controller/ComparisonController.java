package com.example.GoogleQuery.controller;

import com.example.GoogleQuery.model.ComparisonResult;
import com.example.GoogleQuery.service.ComparisonService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * ComparisonController - 比較功能 API 控制器
 * 處理咖啡廳並排比較的相關操作
 * 允許使用者同時比較 2-3 間咖啡廳
 */
@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/comparison")
public class ComparisonController {

    @Autowired
    private ComparisonService comparisonService;

    /**
     * 比較多家咖啡廳
     * POST /api/comparison/compare
     * Body: {"cafeIds": ["cafe_001", "cafe_002", "cafe_003"]}
     * 
     * @param requestData 包含要比較的咖啡廳 ID 列表（2-3 個）
     * @return 比較結果（並排顯示各項特徵）
     */
    @PostMapping("/compare")
    public ResponseEntity<Map<String, Object>> compareCafes(
            @RequestBody Map<String, Object> requestData) {
        
        try {
            @SuppressWarnings("unchecked")
            List<String> cafeIds = (List<String>) requestData.get("cafeIds");
            
            // 驗證輸入
            if (cafeIds == null || cafeIds.size() < 2 || cafeIds.size() > 3) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("error", "請選擇 2-3 家咖啡廳進行比較");
                
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                     .body(errorResponse);
            }
            
            // 執行比較
            ComparisonResult comparisonResult = comparisonService.compareCafesByIds(cafeIds);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("comparison", comparisonResult);
            
            return ResponseEntity.ok(response);
            
        } catch (IllegalArgumentException e) {
            // 咖啡廳不存在的錯誤
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", e.getMessage());
            
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                                 .body(errorResponse);
            
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "比較失敗: " + e.getMessage());
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                 .body(errorResponse);
        }
    }

    /**
     * 取得詳細比較表
     * POST /api/comparison/detailed
     * Body: {"cafeIds": ["cafe_001", "cafe_002"], "features": ["不限時", "有插座", "wifi"]}
     * 
     * @param requestData 包含咖啡廳 ID 和要比較的功能列表
     * @return 詳細比較表（只顯示指定的功能）
     */
    @PostMapping("/detailed")
    public ResponseEntity<Map<String, Object>> detailedComparison(
            @RequestBody Map<String, Object> requestData) {
        
        try {
            @SuppressWarnings("unchecked")
            List<String> cafeIds = (List<String>) requestData.get("cafeIds");
            
            @SuppressWarnings("unchecked")
            List<String> features = (List<String>) requestData.get("features");
            
            // 驗證輸入
            if (cafeIds == null || cafeIds.size() < 2 || cafeIds.size() > 3) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("error", "請選擇 2-3 家咖啡廳進行比較");
                
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                     .body(errorResponse);
            }
            
            // 執行詳細比較
            ComparisonResult comparisonResult = 
                comparisonService.detailedComparison(cafeIds, features);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("comparison", comparisonResult);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "詳細比較失敗: " + e.getMessage());
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                 .body(errorResponse);
        }
    }

    /**
     * 取得比較摘要（優缺點分析）
     * POST /api/comparison/summary
     * Body: {"cafeIds": ["cafe_001", "cafe_002"]}
     * 
     * 分析並返回每家咖啡廳的優勢和劣勢
     * 
     * @param requestData 包含咖啡廳 ID 列表
     * @return 優缺點分析摘要
     */
    @PostMapping("/summary")
    public ResponseEntity<Map<String, Object>> getComparisonSummary(
            @RequestBody Map<String, Object> requestData) {
        
        try {
            @SuppressWarnings("unchecked")
            List<String> cafeIds = (List<String>) requestData.get("cafeIds");
            
            if (cafeIds == null || cafeIds.size() < 2) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("error", "至少需要 2 家咖啡廳才能比較");
                
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                     .body(errorResponse);
            }
            
            Map<String, Object> summary = comparisonService.generateComparisonSummaryForIds(cafeIds);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("summary", summary);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "生成摘要失敗: " + e.getMessage());
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                 .body(errorResponse);
        }
    }

    /**
     * 取得最佳選擇建議
     * POST /api/comparison/recommend
     * Body: {"cafeIds": ["cafe_001", "cafe_002", "cafe_003"], "priorities": ["不限時", "安靜"]}
     * 
     * 根據使用者的優先條件，推薦最適合的咖啡廳
     * 
     * @param requestData 包含咖啡廳 ID 和優先條件
     * @return 最佳選擇建議
     */
    @PostMapping("/recommend")
    public ResponseEntity<Map<String, Object>> getRecommendation(
            @RequestBody Map<String, Object> requestData) {
        
        try {
            @SuppressWarnings("unchecked")
            List<String> cafeIds = (List<String>) requestData.get("cafeIds");
            
            @SuppressWarnings("unchecked")
            List<String> priorities = (List<String>) requestData.get("priorities");
            
            if (cafeIds == null || cafeIds.isEmpty()) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("error", "請提供要比較的咖啡廳");
                
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                     .body(errorResponse);
            }
            
            Map<String, Object> recommendation = 
                comparisonService.getRecommendationBasedOnPriorities(cafeIds, priorities);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("recommendation", recommendation);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "取得建議失敗: " + e.getMessage());
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                 .body(errorResponse);
        }
    }

    /**
     * 比較特定功能
     * POST /api/comparison/feature
     * Body: {"cafeIds": ["cafe_001", "cafe_002"], "feature": "不限時"}
     * 
     * 比較多家咖啡廳在特定功能上的表現
     * 
     * @param requestData 包含咖啡廳 ID 和要比較的功能
     * @return 特定功能的比較結果
     */
    @PostMapping("/feature")
    public ResponseEntity<Map<String, Object>> compareByFeature(
            @RequestBody Map<String, Object> requestData) {
        
        try {
            @SuppressWarnings("unchecked")
            List<String> cafeIds = (List<String>) requestData.get("cafeIds");
            String feature = (String) requestData.get("feature");
            
            if (cafeIds == null || feature == null) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("error", "請提供咖啡廳 ID 和功能名稱");
                
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                     .body(errorResponse);
            }
            
            Map<String, Object> featureComparison = 
                comparisonService.compareByFeature(cafeIds, feature);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("feature", feature);
            response.put("comparison", featureComparison);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "功能比較失敗: " + e.getMessage());
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                 .body(errorResponse);
        }
    }

    /**
     * 匯出比較結果（PDF 或 JSON）
     * POST /api/comparison/export
     * Body: {"cafeIds": ["cafe_001", "cafe_002"], "format": "json"}
     * 
     * @param requestData 包含咖啡廳 ID 和匯出格式
     * @return 可匯出的比較資料
     */
    @PostMapping("/export")
    public ResponseEntity<Map<String, Object>> exportComparison(
            @RequestBody Map<String, Object> requestData) {
        
        try {
            @SuppressWarnings("unchecked")
            List<String> cafeIds = (List<String>) requestData.get("cafeIds");
            String format = (String) requestData.getOrDefault("format", "json");
            
            Map<String, Object> exportData = 
                comparisonService.exportComparison(cafeIds, format);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("format", format);
            response.put("exportData", exportData);
            response.put("exportDate", System.currentTimeMillis());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "匯出比較結果失敗: " + e.getMessage());
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                 .body(errorResponse);
        }
    }

    /**
     * 健康檢查端點
     * GET /api/comparison/health
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> healthCheck() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "ok");
        response.put("service", "ComparisonController");
        response.put("maxComparisons", 3);
        response.put("timestamp", System.currentTimeMillis());
        
        return ResponseEntity.ok(response);
    }
}

