package com.example.GoogleQuery.controller;

import com.example.GoogleQuery.model.SearchResult;
import com.example.GoogleQuery.service.FavoriteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * FavoriteController - 收藏功能 API 控制器
 * 處理使用者收藏咖啡廳的相關操作
 * 
 * 注意：實際的收藏資料儲存在前端 LocalStorage
 * 此 API 主要用於提供收藏咖啡廳的詳細資訊
 */
@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/favorites")
public class FavoriteController {

    @Autowired
    private FavoriteService favoriteService;

    /**
     * 取得收藏咖啡廳的詳細資訊
     * POST /api/favorites/details
     * Body: {"cafeIds": ["cafe_001", "cafe_002", "cafe_003"]}
     * 
     * 前端從 LocalStorage 取得收藏的咖啡廳 ID 列表
     * 後端根據 ID 返回完整的咖啡廳資訊
     * 
     * @param requestData 包含咖啡廳 ID 列表的請求資料
     * @return 收藏咖啡廳的詳細資訊列表
     */
    @PostMapping("/details")
    public ResponseEntity<Map<String, Object>> getFavoriteDetails(
            @RequestBody Map<String, Object> requestData) {
        
        try {
            // 從請求中取得咖啡廳 ID 列表
            @SuppressWarnings("unchecked")
            List<String> cafeIds = (List<String>) requestData.get("cafeIds");
            
            if (cafeIds == null || cafeIds.isEmpty()) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", true);
                response.put("total", 0);
                response.put("favorites", List.of());
                return ResponseEntity.ok(response);
            }
            
            // 取得咖啡廳詳細資訊
            List<SearchResult> favorites = favoriteService.getFavoriteDetails(cafeIds);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("total", favorites.size());
            response.put("favorites", favorites);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "取得收藏資訊失敗: " + e.getMessage());
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                 .body(errorResponse);
        }
    }

    /**
     * 驗證咖啡廳 ID 是否存在
     * GET /api/favorites/validate/cafe_001
     * 
     * 用於前端驗證收藏的咖啡廳是否仍然存在於系統中
     * 
     * @param cafeId 咖啡廳 ID
     * @return 驗證結果
     */
    @GetMapping("/validate/{cafeId}")
    public ResponseEntity<Map<String, Object>> validateCafeId(
            @PathVariable String cafeId) {
        
        try {
            boolean exists = favoriteService.cafeExists(cafeId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("cafeId", cafeId);
            response.put("exists", exists);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "驗證咖啡廳失敗: " + e.getMessage());
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                 .body(errorResponse);
        }
    }

    /**
     * 批次驗證多個咖啡廳 ID
     * POST /api/favorites/validate/batch
     * Body: {"cafeIds": ["cafe_001", "cafe_002", "cafe_003"]}
     * 
     * @param requestData 包含咖啡廳 ID 列表
     * @return 驗證結果（哪些存在、哪些不存在）
     */
    @PostMapping("/validate/batch")
    public ResponseEntity<Map<String, Object>> validateBatchCafeIds(
            @RequestBody Map<String, Object> requestData) {
        
        try {
            @SuppressWarnings("unchecked")
            List<String> cafeIds = (List<String>) requestData.get("cafeIds");
            
            if (cafeIds == null || cafeIds.isEmpty()) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", true);
                response.put("valid", List.of());
                response.put("invalid", List.of());
                return ResponseEntity.ok(response);
            }
            
            Map<String, List<String>> validationResult = 
                favoriteService.validateBatchCafeIds(cafeIds);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("valid", validationResult.get("valid"));
            response.put("invalid", validationResult.get("invalid"));
            response.put("total", cafeIds.size());
            response.put("validCount", validationResult.get("valid").size());
            response.put("invalidCount", validationResult.get("invalid").size());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "批次驗證失敗: " + e.getMessage());
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                 .body(errorResponse);
        }
    }

    /**
     * 取得推薦的收藏咖啡廳
     * GET /api/favorites/recommendations?limit=5
     * 
     * 根據使用者的收藏歷史，推薦相似的咖啡廳
     * 
     * @param cafeIds 已收藏的咖啡廳 ID（逗號分隔）
     * @param limit 推薦數量（預設 5）
     * @return 推薦的咖啡廳列表
     */
    @GetMapping("/recommendations")
    public ResponseEntity<Map<String, Object>> getRecommendations(
            @RequestParam(required = false) String cafeIds,
            @RequestParam(defaultValue = "5") int limit) {
        
        try {
            List<String> favoriteIds = List.of();
            if (cafeIds != null && !cafeIds.isEmpty()) {
                favoriteIds = List.of(cafeIds.split(","));
            }
            
            List<SearchResult> recommendations = 
                favoriteService.getRecommendationsBasedOnFavorites(favoriteIds, limit);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("total", recommendations.size());
            response.put("recommendations", recommendations);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "取得推薦失敗: " + e.getMessage());
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                 .body(errorResponse);
        }
    }

    /**
     * 匯出收藏清單（JSON 格式）
     * POST /api/favorites/export
     * Body: {"cafeIds": ["cafe_001", "cafe_002"]}
     * 
     * @param requestData 包含咖啡廳 ID 列表
     * @return 可匯出的完整收藏資料
     */
    @PostMapping("/export")
    public ResponseEntity<Map<String, Object>> exportFavorites(
            @RequestBody Map<String, Object> requestData) {
        
        try {
            @SuppressWarnings("unchecked")
            List<String> cafeIds = (List<String>) requestData.get("cafeIds");
            
            Map<String, Object> exportData = favoriteService.exportFavorites(cafeIds);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("exportData", exportData);
            response.put("exportDate", System.currentTimeMillis());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "匯出收藏失敗: " + e.getMessage());
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                 .body(errorResponse);
        }
    }

    /**
     * 取得收藏統計資訊
     * POST /api/favorites/statistics
     * Body: {"cafeIds": ["cafe_001", "cafe_002"]}
     * 
     * @param requestData 包含咖啡廳 ID 列表
     * @return 收藏的統計資訊（地區分布、功能分布等）
     */
    @PostMapping("/statistics")
    public ResponseEntity<Map<String, Object>> getFavoriteStatistics(
            @RequestBody Map<String, Object> requestData) {
        
        try {
            @SuppressWarnings("unchecked")
            List<String> cafeIds = (List<String>) requestData.get("cafeIds");
            
            if (cafeIds == null || cafeIds.isEmpty()) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", true);
                response.put("statistics", Map.of());
                return ResponseEntity.ok(response);
            }
            
            Map<String, Object> statistics = favoriteService.getFavoriteStatistics(cafeIds);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("statistics", statistics);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "取得統計資訊失敗: " + e.getMessage());
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                 .body(errorResponse);
        }
    }

    /**
     * 健康檢查端點
     * GET /api/favorites/health
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> healthCheck() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "ok");
        response.put("service", "FavoriteController");
        response.put("note", "收藏資料儲存在前端 LocalStorage");
        response.put("timestamp", System.currentTimeMillis());
        
        return ResponseEntity.ok(response);
    }
}



