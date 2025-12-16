package com.example.GoogleQuery.core;

import java.util.*;
import com.example.GoogleQuery.model.*;

/**
 * Ranker - 負責計算網站分數並進行排名
 * 整合 Tree 結構的深度權重與關鍵字權重
 */
public class Ranker {
    
    private ArrayList<WebPage> pages;
    private Map<WebPage, Double> scoreMap;
    private double maxScore;
    private double minScore;
    
    /**
     * 建構子
     * @param pages 要排名的網站列表
     */
    public Ranker(ArrayList<WebPage> pages) {
        this.pages = pages != null ? pages : new ArrayList<>();
        this.scoreMap = new HashMap<>();
        this.maxScore = 0.0;
        this.minScore = Double.MAX_VALUE;
    }
    
    /**
     * 計算所有網站的最終分數
     * 結合關鍵字權重與 Tree 深度權重
     * @param keywords 關鍵字列表
     */
    public void computeFinalScores(ArrayList<Keyword> keywords) {
        maxScore = 0.0;
        minScore = Double.MAX_VALUE;
        
        for (WebPage page : pages) {
            // 1. 計算關鍵字分數
            page.setScore(keywords);
            double baseScore = page.getScore();
            
            // 2. 如果網站有對應的 WebTree，套用深度權重
            // 這部分會在 WebTree.setPostOrderScore 中計算
            // 這裡只需取得最終分數
            double finalScore = baseScore;
            
            // 3. 儲存分數
            scoreMap.put(page, finalScore);
            
            // 4. 更新最大最小值
            if (finalScore > maxScore) maxScore = finalScore;
            if (finalScore < minScore) minScore = finalScore;
        }
    }
    
    /**
     * 套用深度權重（配合 WebTree 使用）
     * 深度越深，權重越低
     * @param node WebTree 中的節點
     * @return 深度權重倍數
     */
    public double applyDepthWeight(WebNode node) {
        if (node == null) return 1.0;
        
        int depth = node.getDepth();
        
        // 深度權重公式：1 / (1 + depth * 0.1)
        // depth=0: weight=1.0
        // depth=1: weight=0.909
        // depth=2: weight=0.833
        // depth=3: weight=0.769
        double depthWeight = 1.0 / (1.0 + depth * 0.1);
        
        return depthWeight;
    }
    
    /**
     * 將分數標準化到 0-100 的範圍
     * 避免分數過大或過小，方便前端顯示
     */
    public void normalizeScores() {
        if (maxScore == minScore) {
            // 所有分數相同，全部設為 50
            for (WebPage page : pages) {
                scoreMap.put(page, 50.0);
                page.setScore(50.0);
            }
            return;
        }
        
        for (WebPage page : pages) {
            double originalScore = scoreMap.get(page);
            // 標準化公式：(score - min) / (max - min) * 100
            double normalizedScore = ((originalScore - minScore) / (maxScore - minScore)) * 100.0;
            scoreMap.put(page, normalizedScore);
            page.setScore(normalizedScore);
        }
    }
    
    /**
     * 按分數排序網站（由高到低）
     * @return 排序後的網站列表
     */
    public ArrayList<WebPage> sortByScore() {
        ArrayList<WebPage> sortedPages = new ArrayList<>(pages);
        
        sortedPages.sort((p1, p2) -> {
            double score1 = scoreMap.getOrDefault(p1, 0.0);
            double score2 = scoreMap.getOrDefault(p2, 0.0);
            return Double.compare(score2, score1); // 降序排列
        });
        
        return sortedPages;
    }
    
    /**
     * 取得排序後的搜尋結果物件列表
     * @return SearchResult 列表（包含網站和分數）
     */
    public ArrayList<SearchResult> getRankedResults() {
        ArrayList<WebPage> sortedPages = sortByScore();
        ArrayList<SearchResult> results = new ArrayList<>();
        
        for (WebPage page : sortedPages) {
            double score = scoreMap.getOrDefault(page, 0.0);
            SearchResult result = new SearchResult(page, score);
            
            // 如果 page 是 Cafe 實例，設定額外的咖啡廳資訊
            if (page instanceof Cafe) {
                Cafe cafe = (Cafe) page;
                result.setCafeId(cafe.getId());
                result.setPhoneNumber(cafe.getPhone());
                result.setRating(cafe.getRating());
                result.setFeatures(cafe.getFeatures());
                result.setTags(cafe.getTags());
                result.setDistrict(cafe.getDistrict());
                result.setAddress(cafe.getAddress());
            }
            
            results.add(result);
        }
        
        return results;
    }
    
    /**
     * 列印前 N 名的搜尋結果
     * @param n 要顯示的數量
     */
    public void printTopResults(int n) {
        ArrayList<WebPage> sortedPages = sortByScore();
        int limit = Math.min(n, sortedPages.size());
        
        System.out.println("\n=== Top " + limit + " 搜尋結果 ===");
        for (int i = 0; i < limit; i++) {
            WebPage page = sortedPages.get(i);
            double score = scoreMap.get(page);
            System.out.printf("%d. %s\n   URL: %s\n   分數: %.2f\n\n", 
                i + 1, page.getName(), page.getUrl(), score);
        }
    }
    
    /**
     * 列印所有網站的詳細分數資訊
     */
    public void printDetailedScores() {
        ArrayList<WebPage> sortedPages = sortByScore();
        
        System.out.println("\n=== 詳細排名結果 ===");
        System.out.println(String.format("%-5s %-30s %-15s", "排名", "網站名稱", "分數"));
        System.out.println("=".repeat(55));
        
        for (int i = 0; i < sortedPages.size(); i++) {
            WebPage page = sortedPages.get(i);
            double score = scoreMap.get(page);
            System.out.printf("%-5d %-30s %.2f\n", 
                i + 1, page.getName(), score);
        }
        
        System.out.println("\n統計資訊：");
        System.out.printf("總網站數: %d\n", pages.size());
        System.out.printf("最高分: %.2f\n", maxScore);
        System.out.printf("最低分: %.2f\n", minScore);
        System.out.printf("平均分: %.2f\n", getAverageScore());
    }
    
    /**
     * 計算平均分數
     * @return 平均分數
     */
    public double getAverageScore() {
        if (pages.isEmpty()) return 0.0;
        
        double sum = 0.0;
        for (double score : scoreMap.values()) {
            sum += score;
        }
        return sum / pages.size();
    }
    
    /**
     * 取得特定網站的分數
     * @param page 網站
     * @return 分數
     */
    public double getScore(WebPage page) {
        return scoreMap.getOrDefault(page, 0.0);
    }
    
    /**
     * 取得特定網站的排名
     * @param page 網站
     * @return 排名（從 1 開始），如果找不到則返回 -1
     */
    public int getRank(WebPage page) {
        ArrayList<WebPage> sortedPages = sortByScore();
        for (int i = 0; i < sortedPages.size(); i++) {
            if (sortedPages.get(i).equals(page)) {
                return i + 1;
            }
        }
        return -1;
    }
    
    /**
     * 根據分數範圍篩選網站
     * @param minScore 最低分數
     * @param maxScore 最高分數
     * @return 符合條件的網站列表
     */
    public ArrayList<WebPage> filterByScoreRange(double minScore, double maxScore) {
        ArrayList<WebPage> filtered = new ArrayList<>();
        
        for (WebPage page : pages) {
            double score = scoreMap.getOrDefault(page, 0.0);
            if (score >= minScore && score <= maxScore) {
                filtered.add(page);
            }
        }
        
        return filtered;
    }
    
    /**
     * 取得前 N 名網站（用於前端顯示）
     * @param n 數量
     * @return 前 N 名網站列表
     */
    public ArrayList<WebPage> getTopN(int n) {
        ArrayList<WebPage> sortedPages = sortByScore();
        int limit = Math.min(n, sortedPages.size());
        return new ArrayList<>(sortedPages.subList(0, limit));
    }
    
    /**
     * 重設 Ranker（清除所有計算結果）
     */
    public void reset() {
        scoreMap.clear();
        maxScore = 0.0;
        minScore = Double.MAX_VALUE;
    }
    
    /**
     * 新增網站到排名系統
     * @param page 要新增的網站
     */
    public void addPage(WebPage page) {
        if (!pages.contains(page)) {
            pages.add(page);
        }
    }
    
    /**
     * 移除網站
     * @param page 要移除的網站
     */
    public void removePage(WebPage page) {
        pages.remove(page);
        scoreMap.remove(page);
    }
}

