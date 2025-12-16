package com.example.GoogleQuery.controller;

import com.example.GoogleQuery.model.SearchResult;
import com.example.GoogleQuery.service.HistoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * HistoryController - 瀏覽紀錄 API 控制器
 * 處理使用者瀏覽紀錄相關的 HTTP 請求
 * 注意：實際儲存使用 LocalStorage（前端），此 API 提供輔助功能
 */
@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/history")
public class HistoryController {

    @Autowired
    private HistoryService historyService;

    /**
     * 記錄瀏覽行為（可選，主要用 LocalStorage）
     * POST /api/history/record
     * Body: { "cafeId": "cafe_001", "cafeName": "某咖啡廳", "timestamp": 1234567890 }
     * 
     * @param historyData 瀏覽紀錄資料
     * @return 記錄結果
     */
    @PostMapping("/record")
    public ResponseEntity<Map<String, Object>> recordHistory(
            @RequestBody Map<String, Object> historyData) {
        
        try {
            String cafeId = (String) historyData.get("cafeId");
            
            if (cafeId == null || cafeId.isEmpty()) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("error", "咖啡廳 ID 不能為空");
                
                return ResponseEntity.badRequest().body(errorResponse);
            }
            
            // 記錄瀏覽行為（如果需要後端追蹤）
            boolean recorded = historyService.recordVisit(historyData);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", recorded);
            response.put("message", "瀏覽紀錄已記錄");
            response.put("cafeId", cafeId);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "記錄失敗: " + e.getMessage());
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                 .body(errorResponse);
        }
    }

    /**
     * 批次獲取咖啡廳資訊（根據 ID 列表）
     * POST /api/history/batch
     * Body: { "cafeIds": ["cafe_001", "cafe_002", "cafe_003"] }
     * 
     * @param requestBody 包含咖啡廳 ID 列表
     * @return 咖啡廳詳細資訊列表
     */
    @PostMapping("/batch")
    public ResponseEntity<Map<String, Object>> getBatchCafes(
            @RequestBody Map<String, Object> requestBody) {
        
        try {
            @SuppressWarnings("unchecked")
            List<String> cafeIds = (List<String>) requestBody.get("cafeIds");
            
            if (cafeIds == null || cafeIds.isEmpty()) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("error", "咖啡廳 ID 列表不能為空");
                
                return ResponseEntity.badRequest().body(errorResponse);
            }
            
            // 獲取咖啡廳資訊
            ArrayList<SearchResult> cafes = historyService.getCafesByIds(cafeIds);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("total", cafes.size());
            response.put("cafes", cafes);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "批次獲取失敗: " + e.getMessage());
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                 .body(errorResponse);
        }
    }

    /**
     * 獲取最近瀏覽紀錄（從後端，如果有實作追蹤）
     * GET /api/history/recent?limit=10
     * 
     * @param limit 返回數量（預設 10）
     * @return 最近瀏覽的咖啡廳列表
     */
    @GetMapping("/recent")
    public ResponseEntity<Map<String, Object>> getRecentHistory(
            @RequestParam(defaultValue = "10") int limit) {
        
        try {
            ArrayList<SearchResult> recentCafes = historyService.getRecentVisits(limit);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("total", recentCafes.size());
            response.put("history", recentCafes);
            response.put("note", "主要使用 LocalStorage，此為輔助功能");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "獲取瀏覽紀錄失敗: " + e.getMessage());
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                 .body(errorResponse);
        }
    }

    /**
     * 清除瀏覽紀錄（如果有後端追蹤）
     * DELETE /api/history/clear
     * 
     * @return 清除結果
     */
    @DeleteMapping("/clear")
    public ResponseEntity<Map<String, Object>> clearHistory() {
        try {
            boolean cleared = historyService.clearAllHistory();
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", cleared);
            response.put("message", "瀏覽紀錄已清除");
            response.put("note", "LocalStorage 需由前端自行清除");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "清除失敗: " + e.getMessage());
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                 .body(errorResponse);
        }
    }

    /**
     * 獲取瀏覽統計資訊
     * GET /api/history/stats
     * 
     * @return 統計資訊（瀏覽次數、常訪咖啡廳等）
     */
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getHistoryStats() {
        try {
            Map<String, Object> stats = historyService.getVisitStatistics();
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("stats", stats);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "獲取統計失敗: " + e.getMessage());
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                 .body(errorResponse);
        }
    }

    /**
     * 健康檢查端點
     * GET /api/history/health
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> healthCheck() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "ok");
        response.put("service", "HistoryController");
        response.put("timestamp", System.currentTimeMillis());
        response.put("note", "主要功能使用前端 LocalStorage");
        
        return ResponseEntity.ok(response);
    }
}


