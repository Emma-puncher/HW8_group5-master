package com.example.GoogleQuery.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * LLMComparisonService - Stage 6 LLM 比較服務
 * 
 * 功能：
 * - 提供搜尋結果與 LLM 生成結果的比較介面
 * - LLM 查詢 Prompt 生成
 * - 搜尋引擎結果 vs LLM 結果的評估指標
 * - 集成可選的外部 LLM API（OpenAI, Gemini 等）
 */
@Service
public class LLMComparisonService {
    
    @Autowired
    private HybridSearchService hybridSearchService;
    
    @Autowired
    private SemanticAnalysisService semanticAnalysisService;
    
    /**
     * LLM API 配置（可選）
     */
    private static class LLMConfig {
        static final String OPENAI_API_KEY = System.getenv("OPENAI_API_KEY");
        static final String GEMINI_API_KEY = System.getenv("GEMINI_API_KEY");
        static final String OPENAI_MODEL = "gpt-3.5-turbo";
        static final String GEMINI_MODEL = "gemini-pro";
    }
    
    /**
     * 執行完整的 LLM 比較分析
     * 
     * @param query 搜尋詞
     * @param includeGoogleResults 是否包含 Google 結果
     * @return 比較分析結果
     */
    public Map<String, Object> performLLMComparison(String query, boolean includeGoogleResults) {
        Map<String, Object> comparisonResult = new HashMap<>();
        
        try {
            // Step 1: 執行搜尋引擎搜尋
            ArrayList<Map<String, Object>> searchEngineResults = 
                    semanticAnalysisService.semanticSearch(query, includeGoogleResults);
            
            // Step 2: 生成 LLM 查詢 Prompt
            String llmPrompt = generateLLMPrompt(query);
            
            // Step 3: 調用 LLM（如果可用）
            Map<String, Object> llmResponse = callLLM(llmPrompt);
            
            // Step 4: 執行比較分析
            Map<String, Object> comparison = analyzeComparison(
                    searchEngineResults, 
                    llmResponse
            );
            
            // Step 5: 生成評估報告
            comparisonResult.put("query", query);
            comparisonResult.put("timestamp", new Date());
            comparisonResult.put("searchEngineResults", searchEngineResults);
            comparisonResult.put("llmPrompt", llmPrompt);
            comparisonResult.put("llmResponse", llmResponse);
            comparisonResult.put("comparison", comparison);
            comparisonResult.put("recommendation", generateRecommendation(comparison));
            
            System.out.println("[LLMComparison] 完整分析完成");
            
        } catch (Exception e) {
            System.err.println("[LLMComparison] 比較分析錯誤: " + e.getMessage());
            e.printStackTrace();
        }
        
        return comparisonResult;
    }
    
    /**
     * 生成 LLM 查詢 Prompt
     * 構建適合的提示詞用於 LLM 生成推薦
     * 
     * @param query 原始搜尋詞
     * @return 優化的 Prompt
     */
    public String generateLLMPrompt(String query) {
        StringBuilder prompt = new StringBuilder();
        
        // 識別搜尋意圖
        String intent = semanticAnalysisService.identifyIntent(query);
        
        prompt.append("基於以下搜尋條件，請推薦台北市最適合的咖啡廳：\n\n");
        
        prompt.append("【搜尋詞】: ").append(query).append("\n");
        
        // 根據意圖調整 Prompt
        switch (intent) {
            case "workspace":
                prompt.append("【搜尋意圖】: 尋找適合工作/讀書的咖啡廳\n");
                prompt.append("【重點要求】:\n");
                prompt.append("  - 環境安靜，不會有噪音干擾\n");
                prompt.append("  - 有充足的插座供電腦使用\n");
                prompt.append("  - 無時間限制或限制較寬鬆\n");
                prompt.append("  - 網速穩定\n");
                break;
            case "social":
                prompt.append("【搜尋意圖】: 尋找適合社交/聚會的咖啡廳\n");
                prompt.append("【重點要求】:\n");
                prompt.append("  - 舒適的座位排列，方便多人交談\n");
                prompt.append("  - 氛圍輕鬆，可久坐不會被催促\n");
                prompt.append("  - 飲品和食物選擇豐富\n");
                prompt.append("  - 適合拍照打卡\n");
                break;
            case "exploration":
                prompt.append("【搜尋意圖】: 尋找新穎/話題咖啡廳\n");
                prompt.append("【重點要求】:\n");
                prompt.append("  - 獨特的裝潢或主題\n");
                prompt.append("  - 最近新開幕或改裝\n");
                prompt.append("  - 社群媒體上受歡迎\n");
                prompt.append("  - 值得拍照分享\n");
                break;
            case "budget":
                prompt.append("【搜尋意圖】: 尋找CP值高的經濟實惠咖啡廳\n");
                prompt.append("【重點要求】:\n");
                prompt.append("  - 飲品價格親民\n");
                prompt.append("  - 飲品品質不妥協\n");
                prompt.append("  - 環境乾淨舒適\n");
                prompt.append("  - 無低消或低消不高\n");
                break;
            default:
                prompt.append("【搜尋意圖】: 一般咖啡廳推薦搜尋\n");
                prompt.append("【重點要求】: 根據用戶的搜尋詞推薦相關的咖啡廳\n");
        }
        
        prompt.append("\n請提供:\n");
        prompt.append("1. 最適合的3-5家咖啡廳（依優先級排序）\n");
        prompt.append("2. 推薦理由\n");
        prompt.append("3. 建議的訪問時間和注意事項\n\n");
        
        prompt.append("台北市咖啡廳資訊庫已提供，請綜合考慮用戶需求和咖啡廳特色進行推薦。");
        
        return prompt.toString();
    }
    
    /**
     * 調用 LLM（目前為 Mock 實現，可集成真實 LLM API）
     * 
     * @param prompt Prompt 文字
     * @return LLM 回應
     */
    private Map<String, Object> callLLM(String prompt) {
        Map<String, Object> response = new HashMap<>();
        
        // 檢查是否有 API Key
        boolean hasOpenAI = LLMConfig.OPENAI_API_KEY != null && !LLMConfig.OPENAI_API_KEY.isEmpty();
        boolean hasGemini = LLMConfig.GEMINI_API_KEY != null && !LLMConfig.GEMINI_API_KEY.isEmpty();
        
        if (hasOpenAI) {
            return callOpenAI(prompt);
        } else if (hasGemini) {
            return callGemini(prompt);
        } else {
            // Mock LLM 回應（演示用）
            return generateMockLLMResponse(prompt);
        }
    }
    
    /**
     * 調用 OpenAI API（需要 API Key）
     * 
     * @param prompt Prompt 文字
     * @return OpenAI 回應
     */
    private Map<String, Object> callOpenAI(String prompt) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            // TODO: 實現 OpenAI API 調用
            // 需要新增 openai-java 依賴
            // 使用 OpenAI client 調用 gpt-3.5-turbo 或 gpt-4
            
            response.put("provider", "OpenAI");
            response.put("model", LLMConfig.OPENAI_MODEL);
            response.put("status", "not_configured");
            response.put("message", "OpenAI API 尚未配置，請設定 OPENAI_API_KEY 環境變數");
            
            System.out.println("[LLMComparison] OpenAI API 未配置");
            
        } catch (Exception e) {
            System.err.println("[LLMComparison] OpenAI 調用失敗: " + e.getMessage());
        }
        
        return response;
    }
    
    /**
     * 調用 Google Gemini API（需要 API Key）
     * 
     * @param prompt Prompt 文字
     * @return Gemini 回應
     */
    private Map<String, Object> callGemini(String prompt) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            // TODO: 實現 Gemini API 調用
            // 需要新增 google-generativeai 依賴
            // 使用 Gemini client 調用 gemini-pro
            
            response.put("provider", "Google Gemini");
            response.put("model", LLMConfig.GEMINI_MODEL);
            response.put("status", "not_configured");
            response.put("message", "Gemini API 尚未配置，請設定 GEMINI_API_KEY 環境變數");
            
            System.out.println("[LLMComparison] Gemini API 未配置");
            
        } catch (Exception e) {
            System.err.println("[LLMComparison] Gemini 調用失敗: " + e.getMessage());
        }
        
        return response;
    }
    
    /**
     * 生成 Mock LLM 回應（演示用）
     * 當無法調用真實 LLM API 時使用
     * 
     * @param prompt Prompt 文字
     * @return Mock 回應
     */
    private Map<String, Object> generateMockLLMResponse(String prompt) {
        Map<String, Object> response = new HashMap<>();
        
        response.put("provider", "Mock LLM");
        response.put("model", "mock-v1");
        response.put("status", "success");
        response.put("content", 
            "根據您的搜尋需求，以下是 LLM 生成的推薦：\n\n" +
            "**推薦咖啡廳 (Top 3):**\n\n" +
            "1. 咖啡館 A\n" +
            "   - 特色：安靜、有插座、不限時\n" +
            "   - 適合：工作、讀書\n" +
            "   - 評分：⭐⭐⭐⭐⭐\n\n" +
            "2. 咖啡館 B\n" +
            "   - 特色：時尚裝潢、網紅打卡\n" +
            "   - 適合：聚會、拍照\n" +
            "   - 評分：⭐⭐⭐⭐\n\n" +
            "3. 咖啡館 C\n" +
            "   - 特色：CP值高、平價親民\n" +
            "   - 適合：日常消費\n" +
            "   - 評分：⭐⭐⭐⭐"
        );
        response.put("generated_at", new Date());
        response.put("note", "這是 Mock 回應。若要啟用真實 LLM 功能，請配置 OpenAI 或 Gemini API Key");
        
        return response;
    }
    
    /**
     * 執行搜尋引擎結果與 LLM 結果的比較分析
     * 
     * @param searchEngineResults 搜尋引擎結果
     * @param llmResponse LLM 回應
     * @return 比較分析結果
     */
    private Map<String, Object> analyzeComparison(
            ArrayList<Map<String, Object>> searchEngineResults,
            Map<String, Object> llmResponse) {
        
        Map<String, Object> comparison = new HashMap<>();
        
        // 提取搜尋引擎結果統計
        int totalSearchResults = 0;
        int localCafes = 0;
        int googleResults = 0;
        
        for (Map<String, Object> result : searchEngineResults) {
            @SuppressWarnings("unchecked")
            ArrayList<Object> resultList = (ArrayList<Object>) result.get("results");
            if (resultList != null) {
                totalSearchResults += resultList.size();
            }
        }
        
        // 評估指標
        comparison.put("searchEngineResultsCount", totalSearchResults);
        comparison.put("llmAvailable", llmResponse.get("status") != null);
        
        // 計算相關性得分
        double relevanceScore = calculateRelevanceScore(searchEngineResults);
        comparison.put("relevanceScore", relevanceScore);
        
        // 覆蓋度分析
        double coverage = Math.min(totalSearchResults / 30.0, 1.0);  // 假設 30 家本地咖啡廳
        comparison.put("coverageScore", coverage);
        
        // 多元性分析（搜尋源頭多元度）
        comparison.put("sourcesDiversity", analyzeSourceDiversity(searchEngineResults));
        
        return comparison;
    }
    
    /**
     * 計算搜尋結果的相關性得分
     * 
     * @param searchEngineResults 搜尋結果
     * @return 相關性得分（0-100）
     */
    private double calculateRelevanceScore(ArrayList<Map<String, Object>> searchEngineResults) {
        if (searchEngineResults.isEmpty()) {
            return 0.0;
        }
        
        double totalScore = 0.0;
        
        for (Map<String, Object> result : searchEngineResults) {
            String type = (String) result.get("type");
            
            // 主要搜尋結果權重更高
            if ("primary".equals(type)) {
                totalScore += 50;
            } else if ("expanded".equals(type)) {
                totalScore += 25;
            }
        }
        
        return Math.min(totalScore, 100.0);
    }
    
    /**
     * 分析搜尋結果的多元性
     * 
     * @param searchEngineResults 搜尋結果
     * @return 多元性分析結果
     */
    private Map<String, Object> analyzeSourceDiversity(ArrayList<Map<String, Object>> searchEngineResults) {
        Map<String, Object> diversity = new HashMap<>();
        
        // 統計各搜尋源的結果數
        int primaryCount = 0;
        int expandedCount = 0;
        
        for (Map<String, Object> result : searchEngineResults) {
            String type = (String) result.get("type");
            if ("primary".equals(type)) {
                primaryCount++;
            } else if ("expanded".equals(type)) {
                expandedCount++;
            }
        }
        
        diversity.put("primarySearch", primaryCount);
        diversity.put("expandedSearch", expandedCount);
        diversity.put("diversityScore", expandedCount > 0 ? "high" : "medium");
        
        return diversity;
    }
    
    /**
     * 根據比較分析生成建議
     * 
     * @param comparison 比較分析結果
     * @return 建議文字
     */
    private String generateRecommendation(Map<String, Object> comparison) {
        StringBuilder recommendation = new StringBuilder();
        
        @SuppressWarnings("unchecked")
        Double relevanceScore = (Double) comparison.get("relevanceScore");
        Double coverageScore = (Double) comparison.get("coverageScore");
        
        recommendation.append("【搜尋結果評估】\n\n");
        
        if (relevanceScore != null && relevanceScore > 75) {
            recommendation.append("✅ 搜尋結果的相關性很高，推薦信度強\n\n");
        } else if (relevanceScore != null && relevanceScore > 50) {
            recommendation.append("⚠️ 搜尋結果的相關性中等，可考慮調整搜尋詞\n\n");
        } else {
            recommendation.append("❌ 搜尋結果的相關性較低，建議嘗試其他搜尋詞\n\n");
        }
        
        if (coverageScore != null && coverageScore > 0.7) {
            recommendation.append("✅ 搜尋覆蓋度高，結果較為完整\n");
        } else {
            recommendation.append("⚠️ 搜尋覆蓋度有限，可能遺漏部分符合條件的咖啡廳\n");
        }
        
        return recommendation.toString();
    }
}
