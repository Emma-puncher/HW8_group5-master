package com.example.GoogleQuery.controller;

import com.example.GoogleQuery.service.*;
import com.example.GoogleQuery.model.SearchResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * AdvancedSearchController - 高級搜尋控制器
 * 
 * 整合所有 Stage 的功能：
 * - Stage 1-2: 本地咖啡廳搜尋與排名
 * - Stage 3: Google 網路搜尋整合
 * - Stage 4: 語意分析與關鍵字衍生
 * - Stage 5: Web 應用發布（REST API）
 * - Stage 6: LLM 比較與分析
 * 
 * 提供統一的 REST API 端點用於前端調用
 */
@RestController
@RequestMapping("/api/v2")
public class AdvancedSearchController {
    
    @Autowired
    private SearchService searchService;
    
    @Autowired
    private HybridSearchService hybridSearchService;
    
    @Autowired
    private SemanticAnalysisService semanticAnalysisService;
    
    @Autowired
    private LLMComparisonService llmComparisonService;
    
    /**
     * =====================================================
     * Stage 3: 混合搜尋（本地 + Google）
     * =====================================================
     */
    
    /**
     * 執行混合搜尋
     * GET /api/v2/hybrid-search?q=keyword&google=true
     * 
     * @param q 搜尋關鍵字
     * @param google 是否包含 Google 結果（默認 true）
     * @return 混合搜尋結果
     */
    @GetMapping("/hybrid-search")
    public Map<String, Object> hybridSearch(
            @RequestParam String q,
            @RequestParam(defaultValue = "true") boolean google) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            ArrayList<SearchResult> results = 
                    hybridSearchService.hybridSearch(q, google);
            
            response.put("success", true);
            response.put("keyword", q);
            response.put("includeGoogle", google);
            response.put("resultCount", results.size());
            response.put("results", results);
            response.put("timestamp", new Date());
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("error", e.getMessage());
        }
        
        return response;
    }
    
    /**
     * 執行混合高級搜尋（含篩選）
     * GET /api/v2/hybrid-search/advanced?q=keyword&districts=...&features=...&google=true
     * 
     * @param q 搜尋關鍵字
     * @param districts 地區列表（逗號分隔）
     * @param features 功能列表（逗號分隔）
     * @param google 是否包含 Google 結果
     * @return 篩選後的搜尋結果
     */
    @GetMapping("/hybrid-search/advanced")
    public Map<String, Object> hybridAdvancedSearch(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String districts,
            @RequestParam(required = false) String features,
            @RequestParam(defaultValue = "true") boolean google) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            List<String> districtList = parseCommaSeparatedList(districts);
            List<String> featureList = parseCommaSeparatedList(features);
            
            ArrayList<SearchResult> results = 
                    hybridSearchService.hybridAdvancedSearch(q, districtList, featureList, google);
            
            response.put("success", true);
            response.put("keyword", q);
            response.put("filters", new HashMap<String, Object>() {{
                put("districts", districtList);
                put("features", featureList);
                put("includeGoogle", google);
            }});
            response.put("resultCount", results.size());
            response.put("results", results);
            response.put("timestamp", new Date());
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("error", e.getMessage());
        }
        
        return response;
    }
    
    /**
     * =====================================================
     * Stage 4: 語意分析搜尋
     * =====================================================
     */
    
    /**
     * 執行語意分析搜尋
     * GET /api/v2/semantic-search?q=keyword&google=true
     * 
     * 特點：
     * - 自動擴展搜尋關鍵字（同義詞、相關詞）
     * - 識別用戶搜尋意圖
     * - 返回多層搜尋結果
     * 
     * @param q 搜尋關鍵字
     * @param google 是否包含 Google 結果
     * @return 語意分析搜尋結果
     */
    @GetMapping("/semantic-search")
    public Map<String, Object> semanticSearch(
            @RequestParam String q,
            @RequestParam(defaultValue = "true") boolean google) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            ArrayList<Map<String, Object>> results = 
                    semanticAnalysisService.semanticSearch(q, google);
            
            response.put("success", true);
            response.put("keyword", q);
            response.put("includeGoogle", google);
            response.put("searchLayers", results.size());
            response.put("results", results);
            response.put("timestamp", new Date());
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("error", e.getMessage());
        }
        
        return response;
    }
    
    /**
     * 取得關鍵字語意資訊
     * GET /api/v2/keyword-semantics?keyword=word
     * 
     * 返回：
     * - 同義詞
     * - 擴展詞彙
     * - 相關度
     * - 搜尋意圖
     * 
     * @param keyword 關鍵字
     * @return 語意資訊
     */
    @GetMapping("/keyword-semantics")
    public Map<String, Object> getKeywordSemantics(
            @RequestParam String keyword) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            Map<String, Object> semantics = 
                    semanticAnalysisService.getKeywordSemantics(keyword);
            
            response.put("success", true);
            response.put("semantics", semantics);
            response.put("timestamp", new Date());
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("error", e.getMessage());
        }
        
        return response;
    }
    
    /**
     * 取得搜尋建議
     * GET /api/v2/search-advice?q=keyword
     * 
     * 返回：
     * - 識別的搜尋意圖
     * - 建議的搜尋詞
     * - 使用提示
     * 
     * @param q 搜尋關鍵字
     * @return 搜尋建議
     */
    @GetMapping("/search-advice")
    public Map<String, Object> getSearchAdvice(
            @RequestParam String q) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            Map<String, Object> advice = 
                    semanticAnalysisService.getSearchAdvice(q);
            
            response.put("success", true);
            response.put("query", q);
            response.put("advice", advice);
            response.put("timestamp", new Date());
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("error", e.getMessage());
        }
        
        return response;
    }
    
    /**
     * =====================================================
     * Stage 6: LLM 比較與分析
     * =====================================================
     */
    
    /**
     * 執行完整的 LLM 比較分析
     * GET /api/v2/llm-comparison?q=keyword&google=true
     * 
     * 返回：
     * - 搜尋引擎結果
     * - LLM 生成結果
     * - 比較分析
     * - 推薦建議
     * 
     * @param q 搜尋關鍵字
     * @param google 是否包含 Google 結果
     * @return LLM 比較分析結果
     */
    @GetMapping("/llm-comparison")
    public Map<String, Object> performLLMComparison(
            @RequestParam String q,
            @RequestParam(defaultValue = "true") boolean google) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            Map<String, Object> comparison = 
                    llmComparisonService.performLLMComparison(q, google);
            
            response.put("success", true);
            response.put("query", q);
            response.put("comparison", comparison);
            response.put("timestamp", new Date());
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("error", e.getMessage());
        }
        
        return response;
    }
    
    /**
     * 取得 LLM Prompt 預覽
     * GET /api/v2/llm-prompt?q=keyword
     * 
     * 用於調試和理解 LLM 將收到的 Prompt
     * 
     * @param q 搜尋關鍵字
     * @return LLM Prompt
     */
    @GetMapping("/llm-prompt")
    public Map<String, Object> getLLMPrompt(
            @RequestParam String q) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            String prompt = llmComparisonService.generateLLMPrompt(q);
            
            response.put("success", true);
            response.put("query", q);
            response.put("prompt", prompt);
            response.put("timestamp", new Date());
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("error", e.getMessage());
        }
        
        return response;
    }
    
    /**
     * =====================================================
     * 統計與分析
     * =====================================================
     */
    
    /**
     * 取得混合搜尋統計資訊
     * GET /api/v2/search-statistics?q=keyword
     * 
     * @param q 搜尋關鍵字
     * @return 統計資訊
     */
    @GetMapping("/search-statistics")
    public Map<String, Object> getSearchStatistics(
            @RequestParam String q) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            Map<String, Object> stats = 
                    hybridSearchService.getSearchStatistics(q);
            
            response.put("success", true);
            response.put("statistics", stats);
            response.put("timestamp", new Date());
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("error", e.getMessage());
        }
        
        return response;
    }
    
    /**
     * 取得系統健康狀態和功能摘要
     * GET /api/v2/system-status
     * 
     * @return 系統狀態
     */
    @GetMapping("/system-status")
    public Map<String, Object> getSystemStatus() {
        Map<String, Object> status = new HashMap<>();
        
        status.put("timestamp", new Date());
        status.put("stages", new HashMap<String, Object>() {{
            put("stage1", "✅ Page Ranking - 已完成");
            put("stage2", "✅ Site Ranking with Tree - 已完成");
            put("stage3", "✅ Google Search Integration - 已完成");
            put("stage4", "✅ Semantics Analysis - 已完成");
            put("stage5", "✅ Web App Published - 已完成");
            put("stage6", "✅ LLM Comparison - 已完成");
        }});
        
        status.put("features", new ArrayList<String>() {{
            add("✅ 本地咖啡廳搜尋與排名");
            add("✅ Google 混合搜尋");
            add("✅ 語意分析與關鍵字擴展");
            add("✅ 查詢意圖識別");
            add("✅ LLM 結果比較");
            add("✅ 多層搜尋結果");
            add("✅ 進階篩選與統計");
        }});
        
        status.put("endpoints", new HashMap<String, String>() {{
            put("hybrid-search", "混合搜尋（本地 + Google）");
            put("hybrid-search/advanced", "高級混合搜尋（含篩選）");
            put("semantic-search", "語意分析搜尋");
            put("keyword-semantics", "關鍵字語意資訊");
            put("search-advice", "搜尋建議");
            put("llm-comparison", "LLM 比較分析");
            put("llm-prompt", "LLM Prompt 預覽");
            put("search-statistics", "搜尋統計資訊");
            put("system-status", "系統狀態");
        }});
        
        return status;
    }
    
    /**
     * =====================================================
     * 工具方法
     * =====================================================
     */
    
    /**
     * 解析逗號分隔的列表
     * 
     * @param input 輸入字串
     * @return 解析後的列表
     */
    private List<String> parseCommaSeparatedList(String input) {
        List<String> list = new ArrayList<>();
        
        if (input == null || input.isEmpty()) {
            return list;
        }
        
        for (String item : input.split(",")) {
            String trimmed = item.trim();
            if (!trimmed.isEmpty()) {
                list.add(trimmed);
            }
        }
        
        return list;
    }
}
