package com.example.GoogleQuery.controller;

import com.example.GoogleQuery.model.SearchResult;
import com.example.GoogleQuery.service.RecommendationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * RecommendationController - 熱門推薦 API 控制器
 * 處理咖啡廳推薦相關的 HTTP 請求
 * 使用預先計算的 baseline score 進行推薦
 */
@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/recommendations")
public class RecommendationController {

    @Autowired
    private RecommendationService recommendationService;

    /**
     * 獲取熱門推薦咖啡廳（依照 baseline score）
     * GET /api/recommendations/top?limit=10
     * 
     * @param limit 返回數量（預設 10）
     * @return 熱門推薦咖啡廳列表
     */
    @GetMapping("/top")
    public ResponseEntity<Map<String, Object>> getTopRecommendations(
            @RequestParam(defaultValue = "10") int limit) {
        
        try {
            if (limit <= 0 || limit > 30) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("error", "限制數量必須在 1-30 之間");
                
                return ResponseEntity.badRequest().body(errorResponse);
            }
            
            ArrayList<SearchResult> recommendations = 
                recommendationService.getTopRecommendations(limit);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("total", recommendations.size());
            response.put("recommendations", recommendations);
            response.put("criteriaUsed", "baseline_score");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "獲取推薦失敗: " + e.getMessage());
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                 .body(errorResponse);
        }
    }

    /**
     * 根據地區獲取推薦
     * GET /api/recommendations/by-district?district=大安區&limit=5
     * 
     * @param district 行政區名稱
     * @param limit 返回數量（預設 5）
     * @return 該地區的推薦咖啡廳列表
     */
    @GetMapping("/by-district")
    public ResponseEntity<Map<String, Object>> getRecommendationsByDistrict(
            @RequestParam String district,
            @RequestParam(defaultValue = "5") int limit) {
        
        try {
            ArrayList<SearchResult> recommendations = 
                recommendationService.getRecommendationsByDistrict(district, limit);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("district", district);
            response.put("total", recommendations.size());
            response.put("recommendations", recommendations);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "獲取地區推薦失敗: " + e.getMessage());
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                 .body(errorResponse);
        }
    }

    /**
     * 根據功能特性獲取推薦
     * GET /api/recommendations/by-feature?feature=不限時&limit=5
     * 
     * @param feature 功能特性（如：不限時、有插座、CP值高、有wifi）
     * @param limit 返回數量（預設 5）
     * @return 符合該特性的推薦咖啡廳列表
     */
    @GetMapping("/by-feature")
    public ResponseEntity<Map<String, Object>> getRecommendationsByFeature(
            @RequestParam String feature,
            @RequestParam(defaultValue = "5") int limit) {
        
        try {
            ArrayList<SearchResult> recommendations = 
                recommendationService.getRecommendationsByFeature(feature, limit);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("feature", feature);
            response.put("total", recommendations.size());
            response.put("recommendations", recommendations);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "獲取功能推薦失敗: " + e.getMessage());
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                 .body(errorResponse);
        }
    }

    /**
     * 綜合推薦（結合多個條件）
     * POST /api/recommendations/combined
     * Body: { "districts": ["大安區"], "features": ["不限時", "有插座"], "limit": 5 }
     * 
     * @param requestBody 推薦條件
     * @return 綜合推薦結果
     */
    @PostMapping("/combined")
    public ResponseEntity<Map<String, Object>> getCombinedRecommendations(
            @RequestBody Map<String, Object> requestBody) {
        
        try {
            @SuppressWarnings("unchecked")
            List<String> districts = (List<String>) requestBody.get("districts");
            
            @SuppressWarnings("unchecked")
            List<String> features = (List<String>) requestBody.get("features");
            
            Integer limit = requestBody.get("limit") != null 
                ? (Integer) requestBody.get("limit") 
                : 10;
            
            ArrayList<SearchResult> recommendations = 
                recommendationService.getCombinedRecommendations(
                    districts, features, limit
                );
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("filters", new HashMap<String, Object>() {{
                put("districts", districts);
                put("features", features);
            }});
            response.put("total", recommendations.size());
            response.put("recommendations", recommendations);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "綜合推薦失敗: " + e.getMessage());
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                 .body(errorResponse);
        }
    }

    /**
     * 獲取隨機推薦（探索新咖啡廳）
     * GET /api/recommendations/random?count=3
     * 
     * @param count 返回數量（預設 3）
     * @return 隨機推薦的咖啡廳列表
     */
    @GetMapping("/random")
    public ResponseEntity<Map<String, Object>> getRandomRecommendations(
            @RequestParam(defaultValue = "3") int count) {
        
        try {
            if (count <= 0 || count > 10) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("error", "數量必須在 1-10 之間");
                
                return ResponseEntity.badRequest().body(errorResponse);
            }
            
            ArrayList<SearchResult> recommendations = 
                recommendationService.getRandomRecommendations(count);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("total", recommendations.size());
            response.put("recommendations", recommendations);
            response.put("type", "random");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "隨機推薦失敗: " + e.getMessage());
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                 .body(errorResponse);
        }
    }

    /**
     * 獲取推薦類別列表
     * GET /api/recommendations/categories
     * 
     * @return 所有可用的推薦類別
     */
    @GetMapping("/categories")
    public ResponseEntity<Map<String, Object>> getRecommendationCategories() {
        try {
            Map<String, List<String>> categories = 
                recommendationService.getAvailableCategories();
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("categories", categories);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "獲取類別失敗: " + e.getMessage());
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                 .body(errorResponse);
        }
    }

    /**
     * 獲取推薦理由
     * GET /api/recommendations/reason/cafe_001
     * 
     * @param cafeId 咖啡廳 ID
     * @return 推薦理由說明
     */
    @GetMapping("/reason/{cafeId}")
    public ResponseEntity<Map<String, Object>> getRecommendationReason(
            @PathVariable String cafeId) {
        
        try {
            Map<String, Object> reasonData = 
                recommendationService.getRecommendationReason(cafeId);
            
            if (reasonData == null) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("error", "找不到該咖啡廳");
                
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                                     .body(errorResponse);
            }
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("cafeId", cafeId);
            response.put("reason", reasonData);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "獲取推薦理由失敗: " + e.getMessage());
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                 .body(errorResponse);
        }
    }

    /**
     * 健康檢查端點
     * GET /api/recommendations/health
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> healthCheck() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "ok");
        response.put("service", "RecommendationController");
        response.put("timestamp", System.currentTimeMillis());
        response.put("scoreType", "baseline_score");
        
        return ResponseEntity.ok(response);
    }
}


