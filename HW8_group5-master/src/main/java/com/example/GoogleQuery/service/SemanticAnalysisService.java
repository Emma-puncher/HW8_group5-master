package com.example.GoogleQuery.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * SemanticAnalysisService - Stage 4 語意分析與關鍵字衍生服務
 * 
 * 功能：
 * - 關鍵字語義理解與衍生（同義詞、相關詞）
 * - 查詢意圖識別（用戶在尋找什麼類型的咖啡廳）
 * - 自動關鍵字擴展搜尋
 * - 搜尋結果的語義相關性評分
 */
@Service
public class SemanticAnalysisService {
    
    @Autowired
    private KeywordService keywordService;
    
    @Autowired
    private HybridSearchService hybridSearchService;
    
    // 關鍵字同義詞映射表（中文咖啡廳搜尋）
    private static final Map<String, List<String>> SYNONYM_MAP = new HashMap<>();
    
    static {
        // 環境類同義詞
        SYNONYM_MAP.put("安靜", Arrays.asList("寧靜", "清靜", "不吵", "隔音"));
        SYNONYM_MAP.put("寧靜", Arrays.asList("安靜", "清靜", "不吵"));
        SYNONYM_MAP.put("吵", Arrays.asList("熱鬧", "人多", "高音量"));
        
        // 設施類同義詞
        SYNONYM_MAP.put("插座", Arrays.asList("充電", "電源", "usb", "插孔"));
        SYNONYM_MAP.put("充電", Arrays.asList("插座", "電源", "usb"));
        SYNONYM_MAP.put("wifi", Arrays.asList("網路", "無線", "上網", "網速"));
        SYNONYM_MAP.put("網路", Arrays.asList("wifi", "無線", "上網"));
        
        // 時間類同義詞
        SYNONYM_MAP.put("不限時", Arrays.asList("久坐", "下午茶", "可待", "長坐"));
        SYNONYM_MAP.put("久坐", Arrays.asList("不限時", "可待", "長坐"));
        SYNONYM_MAP.put("宵夜", Arrays.asList("夜間", "深夜", "24小時"));
        
        // 風格類同義詞
        SYNONYM_MAP.put("隱藏版", Arrays.asList("秘密", "不知名", "小眾", "巷口"));
        SYNONYM_MAP.put("網紅", Arrays.asList("打卡", "IG", "火紅", "熱門"));
        SYNONYM_MAP.put("文青", Arrays.asList("藝文", "創意", "設計感", "小資"));
        
        // 飲品類同義詞
        SYNONYM_MAP.put("咖啡", Arrays.asList("拿鐵", "卡布奇諾", "濃縮", "美式"));
        SYNONYM_MAP.put("拿鐵", Arrays.asList("咖啡", "奶咖", "caf"));
        SYNONYM_MAP.put("甜點", Arrays.asList("蛋糕", "司康", "貝果", "甜食"));
        SYNONYM_MAP.put("肉食", Arrays.asList("漢堡", "三明治", "烤肉"));
    }
    
    // 查詢意圖識別關鍵字
    private static final Map<String, String> INTENT_KEYWORDS = new HashMap<>();
    
    static {
        // 工作/學習意圖
        INTENT_KEYWORDS.put("工作空間", "workspace");
        INTENT_KEYWORDS.put("安靜讀書", "workspace");
        INTENT_KEYWORDS.put("開會", "workspace");
        INTENT_KEYWORDS.put("辦公", "workspace");
        
        // 約會/休閒意圖
        INTENT_KEYWORDS.put("約會", "social");
        INTENT_KEYWORDS.put("約朋友", "social");
        INTENT_KEYWORDS.put("聚餐", "social");
        INTENT_KEYWORDS.put("聚會", "social");
        
        // 打卡/探店意圖
        INTENT_KEYWORDS.put("網紅", "exploration");
        INTENT_KEYWORDS.put("打卡", "exploration");
        INTENT_KEYWORDS.put("IG", "exploration");
        INTENT_KEYWORDS.put("新開", "exploration");
        
        // 價格導向意圖
        INTENT_KEYWORDS.put("便宜", "budget");
        INTENT_KEYWORDS.put("cp值", "budget");
        INTENT_KEYWORDS.put("經濟", "budget");
        INTENT_KEYWORDS.put("平價", "budget");
    }
    
    /**
     * 執行語意分析搜尋
     * 自動擴展搜尋關鍵字，包含同義詞和相關詞彙
     * 
     * @param query 原始搜尋詞
     * @param includeGoogle 是否包含 Google 結果
     * @return 擴展搜尋結果
     */
    public ArrayList<Map<String, Object>> semanticSearch(String query, boolean includeGoogle) {
        ArrayList<Map<String, Object>> results = new ArrayList<>();
        
        try {
            // Step 1: 識別查詢意圖
            String intent = identifyIntent(query);
            
            // Step 2: 擴展關鍵字（含同義詞）
            Set<String> expandedKeywords = expandKeywords(query);
            
            System.out.println("[SemanticAnalysis] 原始查詢: " + query);
            System.out.println("[SemanticAnalysis] 識別意圖: " + intent);
            System.out.println("[SemanticAnalysis] 擴展關鍵字: " + expandedKeywords);
            
            // Step 3: 執行混合搜尋
            Map<String, Object> primaryResult = new HashMap<>();
            primaryResult.put("keyword", query);
            primaryResult.put("type", "primary");
            primaryResult.put("intent", intent);
            primaryResult.put("results", hybridSearchService.hybridSearch(query, includeGoogle));
            results.add(primaryResult);
            
            // Step 4: 執行擴展關鍵字搜尋（最多 3 個）
            int expandedCount = 0;
            for (String expandedKeyword : expandedKeywords) {
                if (expandedCount >= 3) break;
                
                Map<String, Object> expandedResult = new HashMap<>();
                expandedResult.put("keyword", expandedKeyword);
                expandedResult.put("type", "expanded");
                expandedResult.put("intent", intent);
                expandedResult.put("results", hybridSearchService.hybridSearch(expandedKeyword, false));
                results.add(expandedResult);
                
                expandedCount++;
            }
            
        } catch (Exception e) {
            System.err.println("[SemanticAnalysis] 語意搜尋錯誤: " + e.getMessage());
        }
        
        return results;
    }
    
    /**
     * 識別查詢意圖
     * 判斷用戶搜尋的意圖（工作、社交、探店等）
     * 
     * @param query 搜尋詞
     * @return 意圖類別
     */
    public String identifyIntent(String query) {
        if (query == null || query.isEmpty()) {
            return "general";
        }
        
        String lowerQuery = query.toLowerCase();
        
        // 檢查意圖關鍵字
        for (Map.Entry<String, String> entry : INTENT_KEYWORDS.entrySet()) {
            if (lowerQuery.contains(entry.getKey())) {
                return entry.getValue();
            }
        }
        
        // 檢查特定模式
        if (lowerQuery.contains("晚上") || lowerQuery.contains("下午") || lowerQuery.contains("夜間")) {
            return "time-specific";
        }
        
        if (lowerQuery.contains("周末") || lowerQuery.contains("假日")) {
            return "weekend";
        }
        
        return "general";
    }
    
    /**
     * 擴展搜尋關鍵字
     * 將原始查詞擴展為同義詞和相關詞彙
     * 
     * @param query 原始查詞
     * @return 擴展後的關鍵字集合
     */
    public Set<String> expandKeywords(String query) {
        Set<String> expanded = new HashSet<>();
        
        if (query == null || query.isEmpty()) {
            return expanded;
        }
        
        // 分割查詞為不同詞彙
        String[] terms = query.split("[\\s\\+\\-\\|、]+");
        
        for (String term : terms) {
            String trimmedTerm = term.trim();
            if (trimmedTerm.isEmpty()) continue;
            
            // 添加同義詞
            if (SYNONYM_MAP.containsKey(trimmedTerm)) {
                expanded.addAll(SYNONYM_MAP.get(trimmedTerm));
            }
        }
        
        // 移除原始詞彙本身
        expanded.remove(query);
        
        return expanded;
    }
    
    /**
     * 計算兩個詞彙的語義相似度
     * 基於同義詞和相關度
     * 
     * @param word1 詞彙 1
     * @param word2 詞彙 2
     * @return 相似度分數（0.0 - 1.0）
     */
    public double calculateSemanticSimilarity(String word1, String word2) {
        if (word1.equals(word2)) {
            return 1.0;
        }
        
        // 檢查是否為直接同義詞
        if (SYNONYM_MAP.containsKey(word1) && SYNONYM_MAP.get(word1).contains(word2)) {
            return 0.9;
        }
        
        if (SYNONYM_MAP.containsKey(word2) && SYNONYM_MAP.get(word2).contains(word1)) {
            return 0.9;
        }
        
        // 檢查字符串相似度（Levenshtein）
        double levenshteinSim = 1.0 - ((double) levenshteinDistance(word1, word2) / 
                                       Math.max(word1.length(), word2.length()));
        
        return Math.max(levenshteinSim, 0.0);
    }
    
    /**
     * 計算 Levenshtein 距離
     * 
     * @param s1 字串 1
     * @param s2 字串 2
     * @return 距離值
     */
    private int levenshteinDistance(String s1, String s2) {
        if (s1 == null) s1 = "";
        if (s2 == null) s2 = "";
        
        int len1 = s1.length();
        int len2 = s2.length();
        
        int[][] dp = new int[len1 + 1][len2 + 1];
        
        for (int i = 0; i <= len1; i++) {
            dp[i][0] = i;
        }
        
        for (int j = 0; j <= len2; j++) {
            dp[0][j] = j;
        }
        
        for (int i = 1; i <= len1; i++) {
            for (int j = 1; j <= len2; j++) {
                if (s1.charAt(i - 1) == s2.charAt(j - 1)) {
                    dp[i][j] = dp[i - 1][j - 1];
                } else {
                    dp[i][j] = 1 + Math.min(Math.min(dp[i - 1][j], dp[i][j - 1]), dp[i - 1][j - 1]);
                }
            }
        }
        
        return dp[len1][len2];
    }
    
    /**
     * 取得關鍵字的詳細語義資訊
     * 
     * @param keyword 關鍵字
     * @return 語義資訊 Map
     */
    public Map<String, Object> getKeywordSemantics(String keyword) {
        Map<String, Object> semantics = new HashMap<>();
        
        semantics.put("keyword", keyword);
        semantics.put("intent", identifyIntent(keyword));
        semantics.put("synonyms", SYNONYM_MAP.getOrDefault(keyword, new ArrayList<>()));
        semantics.put("expandedTerms", expandKeywords(keyword));
        
        // 計算與常見詞彙的相似度
        Map<String, Double> similarities = new HashMap<>();
        Set<String> commonKeywords = SYNONYM_MAP.keySet();
        for (String commonKeyword : commonKeywords) {
            double sim = calculateSemanticSimilarity(keyword, commonKeyword);
            if (sim > 0.5) {
                similarities.put(commonKeyword, sim);
            }
        }
        
        semantics.put("similarities", similarities);
        
        return semantics;
    }
    
    /**
     * 取得搜尋統計與建議
     * 
     * @param query 原始查詞
     * @return 統計與建議 Map
     */
    public Map<String, Object> getSearchAdvice(String query) {
        Map<String, Object> advice = new HashMap<>();
        
        // 意圖識別
        String intent = identifyIntent(query);
        advice.put("identifiedIntent", intent);
        
        // 建議詞彙
        Set<String> suggestions = expandKeywords(query);
        advice.put("suggestedSearches", suggestions);
        
        // 意圖相關提示
        String tip = getIntentTip(intent);
        advice.put("tip", tip);
        
        return advice;
    }
    
    /**
     * 根據意圖提供搜尋建議
     * 
     * @param intent 意圖類別
     * @return 建議文字
     */
    private String getIntentTip(String intent) {
        switch (intent) {
            case "workspace":
                return "💼 工作空間搜尋：推薦篩選「安靜」、「有插座」等功能";
            case "social":
                return "👥 社交聚會搜尋：推薦篩選「不限時」、「適合多人」等功能";
            case "exploration":
                return "🔍 探店打卡搜尋：推薦搜尋最新開幕或話題咖啡廳";
            case "budget":
                return "💰 經濟實惠搜尋：推薦篩選「CP值高」、「平價」等功能";
            case "time-specific":
                return "🕐 時段特定搜尋：請確認營業時間符合您的需求";
            case "weekend":
                return "📅 假日搜尋：部分咖啡廳可能有特殊營業時間";
            default:
                return "🔎 一般搜尋：您可以使用地區和功能篩選進一步精煉結果";
        }
    }
}
