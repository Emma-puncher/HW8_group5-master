package com.example.GoogleQuery.controller;

import com.example.GoogleQuery.model.SearchResult;
import com.example.GoogleQuery.service.SearchService;
import com.example.GoogleQuery.service.RecommendationService;
import com.example.GoogleQuery.service.RelevanceValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * SearchController - 搜尋 API 控制器
 * 處理咖啡廳搜尋相關的 HTTP 請求
 */
@CrossOrigin(origins = "*") // 允許前端 AJAX 呼叫
@RestController
@RequestMapping("/api")
public class SearchController {

    @Autowired
    private SearchService searchService;
    
    @Autowired
    private RecommendationService recommendationService;

    @Autowired
    private RelevanceValidator relevanceValidator;

    /**
     * 基本搜尋 API
     * GET /api/search?q=不限時
     * 
     * @param q 搜尋關鍵字
     * @return 搜尋結果列表
     */
    @GetMapping("/search")
    public ResponseEntity<Map<String, Object>> search(@RequestParam(value = "q", required = false) String q,
                                                       @RequestParam(value = "keyword", required = false) String keyword) {
        try {
            // 支援前端的 q 參數和 keyword 參數（向後兼容）
            String searchKeyword = (q != null) ? q : keyword;
            if (searchKeyword == null || searchKeyword.trim().isEmpty()) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("error", "請提供搜尋關鍵字");
                return ResponseEntity.badRequest().body(errorResponse);
            }

            // 檢查搜尋詞的相關性
            RelevanceValidator.RelevanceResult relevanceResult = relevanceValidator.validateRelevance(searchKeyword);
            
            // 建立回應
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("keyword", searchKeyword);
            response.put("relevance", relevanceResult.isRelevant());
            response.put("message", relevanceResult.getMessage());

            // 如果搜尋詞不相關，提供建議但仍然返回結果（讓使用者決定）
            if (!relevanceResult.isRelevant()) {
                response.put("warning", true);
                response.put("suggestions", relevanceResult.getSuggestions());
                response.put("results", new ArrayList<>()); // 返回空結果
                response.put("total", 0);
                return ResponseEntity.ok(response);
            }
            
            // 呼叫 SearchService 進行搜尋
            ArrayList<SearchResult> results = searchService.search(searchKeyword);
            
            response.put("total", results.size());
            response.put("results", results);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            // 錯誤處理
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "搜尋失敗: " + e.getMessage());
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                 .body(errorResponse);
        }
    }

    /**
     * 進階搜尋 API（支援地區和功能篩選）
     * GET /api/search/advanced?keyword=咖啡&districts=大安區,中山區&features=不限時,有插座
     * 
     * @param keyword 搜尋關鍵字（可選，為空時搜尋全部）
     * @param districts 地區列表（逗號分隔，可選）
     * @param features 功能列表（逗號分隔，可選）
     * @return 搜尋結果列表
     */
    @GetMapping("/search/advanced")
    public ResponseEntity<Map<String, Object>> advancedSearch(
            @RequestParam(required = false, defaultValue = "") String keyword,
            @RequestParam(required = false) String districts,
            @RequestParam(required = false) String features) {
        
        try {
            // 解析地區列表
            List<String> districtList = null;
            if (districts != null && !districts.isEmpty()) {
                districtList = List.of(districts.split(","));
            }
            
            // 解析功能列表
            List<String> featureList = null;
            if (features != null && !features.isEmpty()) {
                featureList = List.of(features.split(","));
            }
            
            // 呼叫進階搜尋（支援空關鍵字）
            ArrayList<SearchResult> results = searchService.advancedSearch(
                keyword, districtList, featureList
            );
            
            // 建立回應
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("keyword", keyword);
            Map<String, Object> filters = new HashMap<>();
            filters.put("districts", districtList);
            filters.put("features", featureList);
            response.put("filters", filters);
            response.put("total", results.size());
            response.put("results", results);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "進階搜尋失敗: " + e.getMessage());
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                 .body(errorResponse);
        }
    }

    /**
     * 取得熱門推薦咖啡廳
     * GET /api/recommendations?limit=10
     * GET /api/search/recommendations?limit=10
     * 
     * @param limit 返回數量（預設 10）
     * @return 熱門推薦咖啡廳列表
     */
    @GetMapping(value = {"/recommendations", "/search/recommendations"})
    public ResponseEntity<Map<String, Object>> getRecommendations(
            @RequestParam(defaultValue = "10") int limit) {
        
        try {
            ArrayList<SearchResult> recommendations = 
                recommendationService.getTopRecommendations(limit);
            
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
     * 取得單一咖啡廳詳細資訊
     * GET /api/search/cafe/cafe_001
     * 
     * @param cafeId 咖啡廳 ID
     * @return 咖啡廳詳細資訊
     */
    @GetMapping("/search/cafe/{cafeId}")
    public ResponseEntity<Map<String, Object>> getCafeDetails(
            @PathVariable String cafeId) {
        
        try {
            SearchResult cafe = searchService.getCafeById(cafeId);
            
            if (cafe == null) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("error", "找不到指定的咖啡廳");
                
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                                     .body(errorResponse);
            }
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("cafe", cafe);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "取得咖啡廳資訊失敗: " + e.getMessage());
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                 .body(errorResponse);
        }
    }

    /**
     * 檢查搜尋詞相關性 API
     * GET /api/search/validate?q=星巴克
     * 
     * @param q 搜尋關鍵字
     * @return 相關性檢查結果
     */
    @GetMapping("/search/validate")
    public ResponseEntity<Map<String, Object>> validateSearchKeyword(
            @RequestParam(value = "q", required = false) String q) {
        
        try {
            if (q == null || q.trim().isEmpty()) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("error", "請提供搜尋關鍵字");
                return ResponseEntity.badRequest().body(errorResponse);
            }

            // 檢查相關性
            RelevanceValidator.RelevanceResult result = relevanceValidator.validateRelevance(q);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("keyword", q);
            response.put("isRelevant", result.isRelevant());
            response.put("message", result.getMessage());
            response.put("suggestions", result.getSuggestions());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "驗證失敗: " + e.getMessage());

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                 .body(errorResponse);
        }
    }

    /**
     * 搜尋建議（自動完成）
     * GET /api/search/suggestions?q=不限
     * 
     * @param query 查詢字串
     * @return 建議關鍵字列表
     */
    @GetMapping("/search/suggestions")
    public ResponseEntity<Map<String, Object>> getSuggestions(
            @RequestParam String query) {
        
        try {
            List<String> suggestions = searchService.getSearchSuggestions(query);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("query", query);
            response.put("suggestions", suggestions);
            
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
     * 健康檢查端點
     * GET /api/search/health
     */
    @GetMapping("/search/health")
    public ResponseEntity<Map<String, Object>> healthCheck() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "ok");
        response.put("service", "SearchController");
        response.put("timestamp", System.currentTimeMillis());
        
        return ResponseEntity.ok(response);
    }
}

