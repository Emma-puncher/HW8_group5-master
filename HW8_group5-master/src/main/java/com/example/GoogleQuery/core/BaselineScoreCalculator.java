package com.example.GoogleQuery.core;

import com.example.GoogleQuery.model.Keyword;
import com.example.GoogleQuery.model.WebPage;
import java.util.*;

/**
 * BaselineScoreCalculator - 基準分數計算器
 * 計算每家咖啡廳的基準分數（baseline score）
 * 用於「熱門推薦」功能，分數越高代表越符合「適合讀書/工作的咖啡廳」
 * 
 * 計算方式：
 * 1. 使用「關鍵核心詞」（weight 2.0-3.0）計算分數
 * 2. 考慮關鍵字出現次數和權重
 * 3. 所有咖啡廳預先計算好分數，存入 baseline-scores.json
 */
public class BaselineScoreCalculator {
    
    // 用於計算基準分數的核心關鍵字權重閾值
    private static final double CORE_KEYWORD_THRESHOLD = 2.0;
    
    /**
     * 計算所有咖啡廳的基準分數
     * @param pages 所有咖啡廳列表
     * @param keywords 所有關鍵字（包含核心詞、次要詞、參考詞）
     */
    public void calculateAllBaselineScores(ArrayList<WebPage> pages, ArrayList<Keyword> keywords) {
        // 篩選出核心關鍵字（weight >= 2.0）
        ArrayList<Keyword> coreKeywords = filterCoreKeywords(keywords);
        
        System.out.println("使用 " + coreKeywords.size() + " 個核心關鍵字計算基準分數");
        
        // 計算每個咖啡廳的基準分數
        for (WebPage page : pages) {
            double baselineScore = calculateBaselineScore(page, coreKeywords);
            page.setScore(baselineScore);
        }
        
        // 標準化分數到 0-100 範圍
        normalizeScores(pages);
    }
    
    /**
     * 計算單一咖啡廳的基準分數
     * @param page 咖啡廳網站
     * @param coreKeywords 核心關鍵字列表
     * @return 基準分數
     */
    public double calculateBaselineScore(WebPage page, ArrayList<Keyword> coreKeywords) {
        double totalScore = 0.0;
        String content = page.getContent();
        
        if (content == null || content.isEmpty()) {
            return 0.0;
        }
        
        String lowerContent = content.toLowerCase();
        
        // 計算每個核心關鍵字的加權分數
        for (Keyword keyword : coreKeywords) {
            int count = countKeywordOccurrences(lowerContent, keyword.name);
            
            // 分數 = 出現次數 × 權重
            double keywordScore = count * keyword.weight;
            totalScore += keywordScore;
        }
        
        return totalScore;
    }
    
    /**
     * 篩選出核心關鍵字（weight >= 2.0）
     * @param keywords 所有關鍵字
     * @return 核心關鍵字列表
     */
    private ArrayList<Keyword> filterCoreKeywords(ArrayList<Keyword> keywords) {
        ArrayList<Keyword> coreKeywords = new ArrayList<>();
        
        for (Keyword keyword : keywords) {
            if (keyword.weight >= CORE_KEYWORD_THRESHOLD) {
                coreKeywords.add(keyword);
            }
        }
        
        return coreKeywords;
    }
    
    /**
     * 計算關鍵字在內容中出現的次數
     * @param content 內容（已轉為小寫）
     * @param keyword 關鍵字
     * @return 出現次數
     */
    private int countKeywordOccurrences(String content, String keyword) {
        String lowerKeyword = keyword.toLowerCase();
        int count = 0;
        int index = 0;
        
        while ((index = content.indexOf(lowerKeyword, index)) != -1) {
            count++;
            index += lowerKeyword.length();
        }
        
        return count;
    }
    
    /**
     * 標準化所有咖啡廳的分數到 0-100 範圍
     * @param pages 咖啡廳列表
     */
    private void normalizeScores(ArrayList<WebPage> pages) {
        if (pages.isEmpty()) {
            return;
        }
        
        // 找出最高分和最低分
        double maxScore = Double.MIN_VALUE;
        double minScore = Double.MAX_VALUE;
        
        for (WebPage page : pages) {
            double score = page.getScore();
            if (score > maxScore) maxScore = score;
            if (score < minScore) minScore = score;
        }
        
        // 避免除以零
        if (maxScore == minScore) {
            for (WebPage page : pages) {
                page.setScore(50.0); // 所有分數相同，設為 50
            }
            return;
        }
        
        // 標準化公式：(score - min) / (max - min) * 100
        for (WebPage page : pages) {
            double originalScore = page.getScore();
            double normalizedScore = ((originalScore - minScore) / (maxScore - minScore)) * 100.0;
            page.setScore(normalizedScore);
        }
    }
    
    /**
     * 取得排名前 N 的咖啡廳（根據基準分數）
     * @param pages 所有咖啡廳列表
     * @param topN 取前 N 名
     * @return 排序後的前 N 名咖啡廳
     */
    public ArrayList<WebPage> getTopNCafes(ArrayList<WebPage> pages, int topN) {
        // 複製列表並按分數降序排序
        ArrayList<WebPage> sortedPages = new ArrayList<>(pages);
        sortedPages.sort((p1, p2) -> Double.compare(p2.getScore(), p1.getScore()));
        
        // 取前 topN 個
        int limit = Math.min(topN, sortedPages.size());
        return new ArrayList<>(sortedPages.subList(0, limit));
    }
    
    /**
     * 匯出基準分數到 Map（用於儲存到 JSON）
     * @param pages 咖啡廳列表
     * @return Map<咖啡廳ID, 基準分數>
     */
    public Map<String, Double> exportBaselineScores(ArrayList<WebPage> pages) {
        Map<String, Double> scores = new HashMap<>();
        
        for (WebPage page : pages) {
            // 使用 URL 作為 ID
            String cafeId = extractCafeId(page.getUrl());
            scores.put(cafeId, page.getScore());
        }
        
        return scores;
    }
    
    /**
     * 從 URL 提取咖啡廳 ID
     * 例如：https://example.com/cafe_001 → cafe_001
     * @param url 網站 URL
     * @return 咖啡廳 ID
     */
    private String extractCafeId(String url) {
        if (url == null || url.isEmpty()) {
            return "unknown";
        }
        
        // 簡單實作：取最後一個 / 後面的部分
        String[] parts = url.split("/");
        return parts[parts.length - 1];
    }
    
    /**
     * 從 Map 載入基準分數（從 JSON 讀取）
     * @param pages 咖啡廳列表
     * @param baselineScores Map<咖啡廳ID, 基準分數>
     */
    public void loadBaselineScores(ArrayList<WebPage> pages, Map<String, Double> baselineScores) {
        for (WebPage page : pages) {
            String cafeId = extractCafeId(page.getUrl());
            
            if (baselineScores.containsKey(cafeId)) {
                page.setScore(baselineScores.get(cafeId));
            }
        }
    }
    
    /**
     * 取得基準分數的統計資訊
     * @param pages 咖啡廳列表
     * @return 統計資訊（最高分、最低分、平均分等）
     */
    public Map<String, Double> getStatistics(ArrayList<WebPage> pages) {
        Map<String, Double> stats = new HashMap<>();
        
        if (pages.isEmpty()) {
            stats.put("max", 0.0);
            stats.put("min", 0.0);
            stats.put("average", 0.0);
            stats.put("median", 0.0);
            return stats;
        }
        
        // 收集所有分數
        List<Double> scores = new ArrayList<>();
        for (WebPage page : pages) {
            scores.add(page.getScore());
        }
        
        // 排序
        Collections.sort(scores);
        
        // 計算統計值
        double max = scores.get(scores.size() - 1);
        double min = scores.get(0);
        double sum = scores.stream().mapToDouble(Double::doubleValue).sum();
        double average = sum / scores.size();
        double median = scores.size() % 2 == 0 
            ? (scores.get(scores.size() / 2 - 1) + scores.get(scores.size() / 2)) / 2.0
            : scores.get(scores.size() / 2);
        
        stats.put("max", max);
        stats.put("min", min);
        stats.put("average", average);
        stats.put("median", median);
        stats.put("count", (double) scores.size());
        
        return stats;
    }
    
    /**
     * 列印基準分數排名
     * @param pages 咖啡廳列表
     * @param topN 顯示前 N 名（預設 10）
     */
    public void printTopRankings(ArrayList<WebPage> pages, int topN) {
        ArrayList<WebPage> topCafes = getTopNCafes(pages, topN);
        
        System.out.println("\n╔════════════════════════════════════════════════════════╗");
        System.out.println("║          熱門推薦咖啡廳 (Top " + topN + ")                      ║");
        System.out.println("╚════════════════════════════════════════════════════════╝\n");
        
        for (int i = 0; i < topCafes.size(); i++) {
            WebPage page = topCafes.get(i);
            System.out.printf("【%d】%s\n", i + 1, page.getName());
            System.out.printf("    基準分數: %.2f\n", page.getScore());
            System.out.printf("    地區: %s\n", page.getDistrict());
            System.out.printf("    特色: %s\n", page.getFeatures());
            System.out.println();
        }
        
        // 顯示統計資訊
        Map<String, Double> stats = getStatistics(pages);
        System.out.println("統計資訊：");
        System.out.printf("  最高分: %.2f\n", stats.get("max"));
        System.out.printf("  最低分: %.2f\n", stats.get("min"));
        System.out.printf("  平均分: %.2f\n", stats.get("average"));
        System.out.printf("  中位數: %.2f\n", stats.get("median"));
    }
    
    /**
     * 依地區分組計算平均基準分數
     * @param pages 咖啡廳列表
     * @return Map<地區, 平均分數>
     */
    public Map<String, Double> getAverageScoresByDistrict(ArrayList<WebPage> pages) {
        Map<String, List<Double>> districtScores = new HashMap<>();
        
        // 按地區分組
        for (WebPage page : pages) {
            String district = page.getDistrict();
            districtScores.putIfAbsent(district, new ArrayList<>());
            districtScores.get(district).add(page.getScore());
        }
        
        // 計算每個地區的平均分數
        Map<String, Double> averages = new HashMap<>();
        for (Map.Entry<String, List<Double>> entry : districtScores.entrySet()) {
            String district = entry.getKey();
            List<Double> scores = entry.getValue();
            double average = scores.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
            averages.put(district, average);
        }
        
        return averages;
    }
}

