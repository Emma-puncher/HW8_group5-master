package com.example.GoogleQuery.service;

import com.example.GoogleQuery.model.Cafe;
import com.example.GoogleQuery.model.SearchResult;
import com.example.GoogleQuery.model.WebPage;
import com.example.GoogleQuery.service.KeywordService;
import com.example.GoogleQuery.service.RankingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * HybridSearchService - Stage 3 混合搜尋服務
 * 整合本地咖啡廳搜尋和 Google 網路搜尋結果
 * 支援：
 * - 本地咖啡廳搜尋（SearchService）
 * - Google 搜尋結果整合（GoogleService）
 * - 結果去重和排序
 */
@Service
public class HybridSearchService {
    
    @Autowired
    private SearchService searchService;
    
    @Autowired
    private GoogleService googleService;

    @Autowired
    private RankingService rankingService;

    @Autowired
    private KeywordService keywordService;
    
    private static final int GOOGLE_RESULTS_LIMIT = 20;
    private static final double LOCAL_CAFE_SCORE_BOOST = 2.0;  // 本地咖啡廳加權
    
    /**
     * 執行混合搜尋：先搜尋本地咖啡廳，再補充 Google 搜尋結果
     * 
     * @param keyword 搜尋關鍵字
     * @param includeGoogleResults 是否包含 Google 搜尋結果
     * @return 混合搜尋結果列表（已排序）
     */
    public ArrayList<SearchResult> hybridSearch(String keyword, boolean includeGoogleResults) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return new ArrayList<>();
        }
        
        ArrayList<SearchResult> results = new ArrayList<>();
        
        try {
            // Stage 1: 搜尋本地咖啡廳
            ArrayList<SearchResult> localResults = searchService.search(keyword);
            
            // 對本地結果進行加權（提高優先級）
            for (SearchResult result : localResults) {
                result.setScore(result.getScore() * LOCAL_CAFE_SCORE_BOOST);
                result.setSource("local");  // 標記來源
                results.add(result);
            }
            
            System.out.println("[HybridSearch] 本地搜尋完成: 找到 " + localResults.size() + " 家咖啡廳");
            
            // Stage 2: 如果需要，補充 Google 搜尋結果
            if (includeGoogleResults) {
                ArrayList<SearchResult> googleResults = searchGoogleAndCreateResults(keyword);
                
                // 去重：避免本地結果重複出現
                googleResults = deduplicateResults(googleResults, localResults);
                
                // 限制 Google 結果數量
                if (googleResults.size() > GOOGLE_RESULTS_LIMIT) {
                    googleResults = new ArrayList<>(googleResults.subList(0, GOOGLE_RESULTS_LIMIT));
                }
                
                results.addAll(googleResults);
                
                System.out.println("[HybridSearch] Google 搜尋完成: 找到 " + googleResults.size() + " 個網頁結果");
            }
            
            // Stage 3: 按分數排序
            results.sort((a, b) -> Double.compare(b.getScore(), a.getScore()));
            
            System.out.println("[HybridSearch] 最終結果數: " + results.size());
            
        } catch (Exception e) {
            System.err.println("[HybridSearch] 混合搜尋出錯: " + e.getMessage());
            e.printStackTrace();
        }
        
        return results;
    }
    
    /**
     * 執行高級混合搜尋（含篩選條件）
     * 
     * @param keyword 搜尋關鍵字
     * @param districts 地區列表
     * @param features 功能列表
     * @param includeGoogleResults 是否包含 Google 搜尋結果
     * @return 篩選後的混合搜尋結果
     */
    public ArrayList<SearchResult> hybridAdvancedSearch(
            String keyword,
            List<String> districts,
            List<String> features,
            boolean includeGoogleResults) {
        
        // 執行混合搜尋
        ArrayList<SearchResult> results = hybridSearch(keyword, includeGoogleResults);
        
        // 對本地結果應用篩選（Google 結果不篩選）
        ArrayList<SearchResult> filteredResults = new ArrayList<>();
        
        for (SearchResult result : results) {
            // Google 結果直接加入
            if ("google".equals(result.getSource())) {
                filteredResults.add(result);
                continue;
            }
            
            // 本地結果應用篩選
            boolean passDistrict = districts == null || districts.isEmpty() || 
                    (result.getDistrict() != null && districts.contains(result.getDistrict()));
            
            boolean passFeatures = true;
            if (features != null && !features.isEmpty()) {
                List<String> resultFeatures = result.getFeatures();
                if (resultFeatures != null) {
                    passFeatures = features.stream().anyMatch(f -> resultFeatures.stream()
                            .map(String::toLowerCase)
                            .collect(Collectors.toList())
                            .contains(f.toLowerCase()));
                }
            }
            
            if (passDistrict && passFeatures) {
                filteredResults.add(result);
            }
        }
        
        return filteredResults;
    }
    
    /**
     * 從 Google 搜尋結果建立 SearchResult 列表
     * 
     * @param keyword 搜尋關鍵字
     * @return Google 搜尋結果列表
     */
    private ArrayList<SearchResult> searchGoogleAndCreateResults(String keyword) {
        ArrayList<SearchResult> results = new ArrayList<>();
        
        try {
            // 使用 GoogleService 抓取 Google 前 N 頁 (會建立 WebPage 並抓取文字內容)
            // 如果使用者沒有指定和咖啡廳相關的詞，補上關鍵詞以聚焦到咖啡廳相關頁面
            String googleQuery = keyword == null ? "" : keyword.trim();
            String lower = googleQuery.toLowerCase();
            if (!lower.contains("咖啡") && !lower.contains("咖啡廳") && !lower.contains("coffee") && !lower.contains("cafe")) {
                googleQuery = googleQuery + " 咖啡廳";
            }

            ArrayList<WebPage> pages = googleService.searchAndCreatePages(googleQuery);

            if (pages == null) {
                System.out.println("[HybridSearch] Google searchAndCreatePages returned null");
                return results;
            }

            System.out.println("[HybridSearch] Google raw pages count: " + pages.size());
            // log sample urls and content lengths
            for (int i = 0; i < Math.min(5, pages.size()); i++) {
                WebPage p = pages.get(i);
                String url = p.getUrl();
                int len = p.getContent() == null ? 0 : p.getContent().length();
                System.out.println("[HybridSearch] sample page[" + i + "] url=" + url + " contentLen=" + len);
            }

            // 過濾有效頁面
            pages = googleService.filterValidWebPages(pages);

            System.out.println("[HybridSearch] after filterValidWebPages count: " + (pages == null ? 0 : pages.size()));

            if (pages == null || pages.isEmpty()) {
                System.out.println("[HybridSearch] Google 搜尋取得頁面但無有效內容");
                return results;
            }

            // 使用 RankingService 與 KeywordService 依照我們預先設定的關鍵字與權重計算分數
            List<String> allKeywords = keywordService.getAllKeywordsName();
            ArrayList<SearchResult> ranked = rankingService.rankWebPages(pages, allKeywords);

            // 標記來源並設為 Google 結果
            for (SearchResult r : ranked) {
                r.setSource("google");
                r.setGoogleResult(true);
            }

            results.addAll(ranked);
            
        } catch (Exception e) {
            System.err.println("[HybridSearch] Google 搜尋失敗: " + e.getMessage());
        }
        
        return results;
    }

    /**
     * 僅使用 Google 搜尋並回傳已排序的 SearchResult 列表（不包含本地咖啡廳）
     *
     * @param keyword 搜尋關鍵字
     * @return Google 搜尋結果（已排序）
     */
    public ArrayList<SearchResult> googleSearchOnly(String keyword) {
        ArrayList<SearchResult> results = new ArrayList<>();

        try {
            // If query lacks cafe-related terms, append a focus term to improve relevance
            String googleQuery = keyword == null ? "" : keyword.trim();
            String lower = googleQuery.toLowerCase();
            if (!lower.contains("咖啡") && !lower.contains("咖啡廳") && !lower.contains("coffee") && !lower.contains("cafe")) {
                googleQuery = googleQuery + " 咖啡廳";
            }

            ArrayList<WebPage> pages = googleService.searchAndCreatePages(googleQuery);

            if (pages == null) {
                System.out.println("[HybridSearch] Google-only searchAndCreatePages returned null");
                return results;
            }

            System.out.println("[HybridSearch] Google-only raw pages count: " + pages.size());
            for (int i = 0; i < Math.min(8, pages.size()); i++) {
                WebPage p = pages.get(i);
                System.out.println("[HybridSearch] google-only sample[" + i + "] url=" + p.getUrl() + " name=" + p.getName() + " contentLen=" + (p.getContent() == null ? 0 : p.getContent().length()));
            }

            pages = googleService.filterValidWebPages(pages);

            System.out.println("[HybridSearch] Google-only after filterValidWebPages count: " + (pages == null ? 0 : pages.size()));

            if (pages == null || pages.isEmpty()) {
                System.out.println("[HybridSearch] Google-only 取得頁面但無有效內容");
                return results;
            }

            List<String> allKeywords = keywordService.getAllKeywordsName();
            ArrayList<SearchResult> ranked = rankingService.rankWebPages(pages, allKeywords);

            for (SearchResult r : ranked) {
                r.setSource("google");
                r.setGoogleResult(true);
            }

            results.addAll(ranked);

            // 排序
            results.sort((a, b) -> Double.compare(b.getScore(), a.getScore()));

        } catch (Exception e) {
            System.err.println("[HybridSearch] Google-only 搜尋失敗: " + e.getMessage());
        }

        return results;
    }
    
    /**
     * 去重：移除與本地結果重複的 Google 結果
     * 
     * @param googleResults Google 搜尋結果
     * @param localResults 本地搜尋結果
     * @return 去重後的 Google 結果
     */
    private ArrayList<SearchResult> deduplicateResults(
            ArrayList<SearchResult> googleResults,
            ArrayList<SearchResult> localResults) {
        
        // 建立本地 URL 集合
        Set<String> localUrls = localResults.stream()
                .map(r -> normalizeUrl(r.getUrl()))
                .collect(Collectors.toSet());
        
        // 建立本地名稱集合（用於模糊匹配）
        Set<String> localNames = localResults.stream()
                .map(r -> r.getName().toLowerCase().trim())
                .collect(Collectors.toSet());
        
        // 過濾重複的 Google 結果
        return googleResults.stream()
                .filter(googleResult -> {
                    String googleUrl = normalizeUrl(googleResult.getUrl());
                    String googleName = googleResult.getName().toLowerCase().trim();
                    
                    // 不匹配本地 URL 也不匹配本地名稱
                    return !localUrls.contains(googleUrl) && !localNames.contains(googleName);
                })
                .collect(Collectors.toCollection(ArrayList::new));
    }
    
    /**
     * 正規化 URL 以進行比較
     * 
     * @param url 原始 URL
     * @return 正規化後的 URL
     */
    private String normalizeUrl(String url) {
        if (url == null || url.isEmpty()) {
            return "";
        }
        
        return url.toLowerCase()
                .replaceAll("https?://", "")
                .replaceAll("/$", "")
                .replaceAll("/index\\.html?$", "");
    }
    
    /**
     * 獲取搜尋統計資訊
     * 
     * @param keyword 搜尋關鍵字
     * @return 統計資訊 Map
     */
    public Map<String, Object> getSearchStatistics(String keyword) {
        Map<String, Object> stats = new HashMap<>();
        
        ArrayList<SearchResult> results = hybridSearch(keyword, true);
        
        long localCount = results.stream()
                .filter(r -> "local".equals(r.getSource()))
                .count();
        
        long googleCount = results.stream()
                .filter(r -> "google".equals(r.getSource()))
                .count();
        
        stats.put("keyword", keyword);
        stats.put("totalResults", results.size());
        stats.put("localCafes", localCount);
        stats.put("googleResults", googleCount);
        stats.put("timestamp", new Date());
        
        return stats;
    }
}
