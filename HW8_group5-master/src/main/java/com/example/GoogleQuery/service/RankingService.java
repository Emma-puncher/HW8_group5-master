package com.example.GoogleQuery.service;

import com.example.GoogleQuery.model.SearchResult;
import com.example.GoogleQuery.model.WebPage;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;

/**
 * RankingService - 排名服務
 * 負責計算網頁相關性分數並進行排序
 */
@Service
public class RankingService {

    @Autowired
    private KeywordService keywordService;

    // Baseline scores（預先計算的熱門分數）
    private Map<String, Double> baselineScores;

    /**
     * 初始化：載入 baseline scores
     */
    @PostConstruct
    public void init() {
        try {
            loadBaselineScores();
            System.out.println("RankingService 初始化完成：已載入 " + baselineScores.size() + " 個 baseline scores");
            
        } catch (Exception e) {
            System.err.println("RankingService 初始化失敗: " + e.getMessage());
            baselineScores = new HashMap<>();
        }
    }

    /**
     * 從 JSON 載入 baseline scores
     */
    private void loadBaselineScores() throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        InputStream inputStream = new ClassPathResource("data/baseline-scores.json").getInputStream();
        
        baselineScores = mapper.readValue(inputStream, new TypeReference<Map<String, Double>>() {});
        
        if (baselineScores == null) {
            baselineScores = new HashMap<>();
        }
    }

    /**
     * 計算單一網頁的相關性分數
     * @param webPage 網頁
     * @param keywords 搜尋關鍵字列表
     * @return 相關性分數
     */
    public double calculateScore(WebPage webPage, List<String> keywords) {
        double totalScore = 0.0;
        
        String content = webPage.getContent().toLowerCase();
        
        // 計算每個關鍵字的貢獻
        for (String keyword : keywords) {
            String lowerKeyword = keyword.toLowerCase();
            
            // 計算關鍵字出現次數
            int count = countOccurrences(content, lowerKeyword);
            
            // 獲取關鍵字權重
            double weight = keywordService.getKeywordWeight(keyword);
            
            // 累加分數：次數 × 權重
            totalScore += count * weight;
        }
        
        // 加上 baseline score（如果有的話）
        String cafeId = extractCafeIdFromUrl(webPage.getUrl());
        if (cafeId != null && baselineScores.containsKey(cafeId)) {
            totalScore += baselineScores.get(cafeId);
        }
        
        return totalScore;
    }

    /**
     * 計算關鍵字在文字中的出現次數
     * @param text 文字內容
     * @param keyword 關鍵字
     * @return 出現次數
     */
    private int countOccurrences(String text, String keyword) {
        if (text == null || text.isEmpty() || keyword == null || keyword.isEmpty()) {
            return 0;
        }
        
        int count = 0;
        int index = 0;
        
        while ((index = text.indexOf(keyword, index)) != -1) {
            count++;
            index += keyword.length();
        }
        
        return count;
    }

    /**
     * 從 URL 提取咖啡廳 ID
     * 假設 URL 格式為：https://example.com/cafe_001
     */
    private String extractCafeIdFromUrl(String url) {
        try {
            String[] parts = url.split("/");
            String lastPart = parts[parts.length - 1];
            
            // 如果最後一部分看起來像 cafe_XXX 格式
            if (lastPart.startsWith("cafe_")) {
                return lastPart;
            }
            
            return null;
            
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 批次計算並排名多個網頁
     * @param webPages 網頁列表
     * @param keywords 搜尋關鍵字列表
     * @return 排序後的 SearchResult 列表（分數由高到低）
     */
    public ArrayList<SearchResult> rankWebPages(ArrayList<WebPage> webPages, List<String> keywords) {
        ArrayList<SearchResult> results = new ArrayList<>();
        
        // 計算每個網頁的分數
        for (WebPage page : webPages) {
            double score = calculateScore(page, keywords);
            
            SearchResult result = new SearchResult(
                page.getUrl(),
                page.getName(),
                score
            );
            
            results.add(result);
        }
        
        // 依分數排序（由高到低）
        results.sort((a, b) -> Double.compare(b.getScore(), a.getScore()));
        
        return results;
    }

    /**
     * 獲取 baseline score
     * @param cafeId 咖啡廳 ID
     * @return baseline score（如果不存在則返回 0.0）
     */
    public double getBaselineScore(String cafeId) {
        return baselineScores.getOrDefault(cafeId, 0.0);
    }

    /**
     * 計算詳細的關鍵字貢獻度
     * @param webPage 網頁
     * @param keywords 關鍵字列表
     * @return Map（關鍵字 -> 貢獻分數）
     */
    public Map<String, Double> calculateKeywordContributions(WebPage webPage, List<String> keywords) {
        Map<String, Double> contributions = new HashMap<>();
        
        String content = webPage.getContent().toLowerCase();
        
        for (String keyword : keywords) {
            String lowerKeyword = keyword.toLowerCase();
            int count = countOccurrences(content, lowerKeyword);
            double weight = keywordService.getKeywordWeight(keyword);
            double contribution = count * weight;
            
            contributions.put(keyword, contribution);
        }
        
        return contributions;
    }

    /**
     * 獲取關鍵字出現次數統計
     * @param webPage 網頁
     * @param keywords 關鍵字列表
     * @return Map（關鍵字 -> 出現次數）
     */
    public Map<String, Integer> getKeywordCounts(WebPage webPage, List<String> keywords) {
        Map<String, Integer> counts = new HashMap<>();
        
        String content = webPage.getContent().toLowerCase();
        
        for (String keyword : keywords) {
            String lowerKeyword = keyword.toLowerCase();
            int count = countOccurrences(content, lowerKeyword);
            counts.put(keyword, count);
        }
        
        return counts;
    }

    /**
     * 找出網頁中出現最多的領域關鍵字（用於生成 hashtag）
     * @param webPage 網頁
     * @param topN 返回前 N 個
     * @return 出現最多的關鍵字列表
     */
    public List<String> getTopDomainKeywords(WebPage webPage, int topN) {
        List<String> allDomainKeywords = keywordService.getAllKeywordsName();
        Map<String, Integer> counts = getKeywordCounts(webPage, allDomainKeywords);
        
        return counts.entrySet().stream()
                .sorted((a, b) -> Integer.compare(b.getValue(), a.getValue()))
                .limit(topN)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }

    /**
     * 計算加權排名分數（考慮權重和出現次數）
     * @param webPage 網頁
     * @param keywords 關鍵字列表
     * @return 加權排名分數
     */
    public double calculateWeightedRankingScore(WebPage webPage, List<String> keywords) {
        Map<String, Integer> counts = getKeywordCounts(webPage, keywords);
        return keywordService.calculateWeightedScore(keywords, counts);
    }

    /**
     * 重新排名（使用新的關鍵字）
     * @param results 現有的搜尋結果
     * @param newKeywords 新關鍵字列表
     * @param webPagesMap WebPage 映射表
     * @return 重新排序後的結果
     */
    public ArrayList<SearchResult> rerank(
            ArrayList<SearchResult> results,
            List<String> newKeywords,
            Map<String, WebPage> webPagesMap) {
        
        // 重新計算分數
        for (SearchResult result : results) {
            WebPage page = webPagesMap.get(result.getUrl());
            if (page != null) {
                double newScore = calculateScore(page, newKeywords);
                result.setScore(newScore);
            }
        }
        
        // 重新排序
        results.sort((a, b) -> Double.compare(b.getScore(), a.getScore()));
        
        return results;
    }

    /**
     * 依 baseline score 排名（用於熱門推薦）
     * @param cafeIds 咖啡廳 ID 列表
     * @return 排序後的 ID 列表（分數由高到低）
     */
    public List<String> rankByBaselineScore(List<String> cafeIds) {
        return cafeIds.stream()
                .sorted((a, b) -> Double.compare(getBaselineScore(b), getBaselineScore(a)))
                .collect(Collectors.toList());
    }

    /**
     * 計算分數百分位數
     * @param results 搜尋結果列表
     * @param score 目標分數
     * @return 百分位數（0-100）
     */
    public double calculatePercentile(ArrayList<SearchResult> results, double score) {
        if (results.isEmpty()) {
            return 0.0;
        }
        
        long countBelow = results.stream()
                .filter(r -> r.getScore() < score)
                .count();
        
        return (double) countBelow / results.size() * 100;
    }

    /**
     * 獲取分數分布統計
     * @param results 搜尋結果列表
     * @return 統計資訊
     */
    public Map<String, Object> getScoreDistribution(ArrayList<SearchResult> results) {
        Map<String, Object> distribution = new HashMap<>();
        
        if (results.isEmpty()) {
            distribution.put("count", 0);
            distribution.put("min", 0.0);
            distribution.put("max", 0.0);
            distribution.put("average", 0.0);
            distribution.put("median", 0.0);
            return distribution;
        }
        
        List<Double> scores = results.stream()
                .map(SearchResult::getScore)
                .sorted()
                .collect(Collectors.toList());
        
        distribution.put("count", scores.size());
        distribution.put("min", scores.get(0));
        distribution.put("max", scores.get(scores.size() - 1));
        
        double average = scores.stream()
                .mapToDouble(Double::doubleValue)
                .average()
                .orElse(0.0);
        distribution.put("average", average);
        
        double median = scores.size() % 2 == 0
                ? (scores.get(scores.size() / 2 - 1) + scores.get(scores.size() / 2)) / 2.0
                : scores.get(scores.size() / 2);
        distribution.put("median", median);
        
        return distribution;
    }

    /**
     * 正規化分數（將分數縮放到 0-100 範圍）
     * @param results 搜尋結果列表
     * @return 正規化後的結果列表
     */
    public ArrayList<SearchResult> normalizeScores(ArrayList<SearchResult> results) {
        if (results.isEmpty()) {
            return results;
        }
        
        double maxScore = results.stream()
                .mapToDouble(SearchResult::getScore)
                .max()
                .orElse(1.0);
        
        double minScore = results.stream()
                .mapToDouble(SearchResult::getScore)
                .min()
                .orElse(0.0);
        
        double range = maxScore - minScore;
        
        if (range == 0) {
            // 所有分數相同
            for (SearchResult result : results) {
                result.setNormalizedScore(100.0);
            }
        } else {
            for (SearchResult result : results) {
                double normalized = ((result.getScore() - minScore) / range) * 100;
                result.setNormalizedScore(normalized);
            }
        }
        
        return results;
    }

    /**
     * 過濾低分結果
     * @param results 搜尋結果列表
     * @param minScore 最低分數門檻
     * @return 過濾後的結果列表
     */
    public ArrayList<SearchResult> filterByMinScore(ArrayList<SearchResult> results, double minScore) {
        return results.stream()
                .filter(result -> result.getScore() >= minScore)
                .collect(Collectors.toCollection(ArrayList::new));
    }

    /**
     * 取得前 N 名結果
     * @param results 搜尋結果列表
     * @param topN 數量
     * @return 前 N 名結果
     */
    public ArrayList<SearchResult> getTopNResults(ArrayList<SearchResult> results, int topN) {
        return results.stream()
                .limit(topN)
                .collect(Collectors.toCollection(ArrayList::new));
    }

    /**
     * 計算排名變化（比較兩次搜尋結果）
     * @param oldResults 舊結果
     * @param newResults 新結果
     * @return Map（URL -> 排名變化）
     */
    public Map<String, Integer> calculateRankingChanges(
            ArrayList<SearchResult> oldResults,
            ArrayList<SearchResult> newResults) {
        
        Map<String, Integer> changes = new HashMap<>();
        
        // 建立舊排名映射
        Map<String, Integer> oldRanks = new HashMap<>();
        for (int i = 0; i < oldResults.size(); i++) {
            oldRanks.put(oldResults.get(i).getUrl(), i + 1);
        }
        
        // 計算排名變化
        for (int i = 0; i < newResults.size(); i++) {
            String url = newResults.get(i).getUrl();
            int newRank = i + 1;
            int oldRank = oldRanks.getOrDefault(url, -1);
            
            if (oldRank != -1) {
                int change = oldRank - newRank; // 正數表示上升，負數表示下降
                changes.put(url, change);
            }
        }
        
        return changes;
    }

    /**
     * 設定或更新 baseline score
     * @param cafeId 咖啡廳 ID
     * @param score 分數
     */
    public void setBaselineScore(String cafeId, double score) {
        baselineScores.put(cafeId, score);
    }

    /**
     * 批次設定 baseline scores
     * @param scores Map（咖啡廳 ID -> 分數）
     */
    public void setBaselineScores(Map<String, Double> scores) {
        baselineScores.putAll(scores);
    }

    /**
     * 獲取所有 baseline scores
     * @return baseline scores Map
     */
    public Map<String, Double> getAllBaselineScores() {
        return new HashMap<>(baselineScores);
    }

    /**
     * 計算平均 baseline score
     * @return 平均分數
     */
    public double getAverageBaselineScore() {
        return baselineScores.values().stream()
                .mapToDouble(Double::doubleValue)
                .average()
                .orElse(0.0);
    }

    /**
     * 獲取排名服務統計資訊
     * @return 統計資訊
     */
    public Map<String, Object> getRankingStatistics() {
        Map<String, Object> stats = new HashMap<>();
        
        stats.put("baselineScoresCount", baselineScores.size());
        stats.put("averageBaselineScore", getAverageBaselineScore());
        
        if (!baselineScores.isEmpty()) {
            stats.put("maxBaselineScore", 
                baselineScores.values().stream()
                    .mapToDouble(Double::doubleValue)
                    .max()
                    .orElse(0.0)
            );
            
            stats.put("minBaselineScore", 
                baselineScores.values().stream()
                    .mapToDouble(Double::doubleValue)
                    .min()
                    .orElse(0.0)
            );
        }
        
        return stats;
    }

    /**
     * 檢查服務狀態
     */
    public Map<String, Object> getServiceStatus() {
        Map<String, Object> status = new HashMap<>();
        status.put("service", "RankingService");
        status.put("status", "running");
        status.put("baselineScoresLoaded", baselineScores.size() > 0);
        status.put("timestamp", System.currentTimeMillis());
        
        return status;
    }
}


