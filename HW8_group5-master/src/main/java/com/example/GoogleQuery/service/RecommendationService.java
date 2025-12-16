package com.example.GoogleQuery.service;

import com.example.GoogleQuery.model.Cafe;
import com.example.GoogleQuery.model.SearchResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * RecommendationService - 熱門推薦服務
 * 使用預先計算的 baseline score 進行推薦
 */
@Service
public class RecommendationService {

    @Autowired
    private SearchService searchService;

    @Autowired
    private RankingService rankingService;

    /**
     * 獲取熱門推薦咖啡廳（依 baseline score）
     * @param limit 返回數量
     * @return 熱門推薦列表
     */
    public ArrayList<SearchResult> getTopRecommendations(int limit) {
        // 獲取所有咖啡廳
        List<Cafe> allCafes = searchService.getAllCafes();
        
        // 轉換為 SearchResult 並設定 baseline score
        ArrayList<SearchResult> results = new ArrayList<>();
        for (Cafe cafe : allCafes) {
            SearchResult result = searchService.getCafeById(cafe.getId());
            if (result != null) {
                double baselineScore = rankingService.getBaselineScore(cafe.getId());
                result.setScore(baselineScore);
                results.add(result);
            }
        }
        
        // 依 baseline score 排序
        results.sort((a, b) -> Double.compare(b.getScore(), a.getScore()));
        
        // 返回前 N 個
        return results.stream()
                .limit(limit)
                .collect(Collectors.toCollection(ArrayList::new));
    }

    /**
     * 依地區獲取推薦
     * @param district 地區名稱
     * @param limit 返回數量
     * @return 該地區的推薦列表
     */
    public ArrayList<SearchResult> getRecommendationsByDistrict(String district, int limit) {
        // 先搜尋該地區的咖啡廳
        ArrayList<SearchResult> districtCafes = searchService.searchByDistrict(district);
        
        // 依 baseline score 設定分數
        for (SearchResult cafe : districtCafes) {
            double baselineScore = rankingService.getBaselineScore(cafe.getCafeId());
            cafe.setScore(baselineScore);
        }
        
        // 排序並返回
        districtCafes.sort((a, b) -> Double.compare(b.getScore(), a.getScore()));
        
        return districtCafes.stream()
                .limit(limit)
                .collect(Collectors.toCollection(ArrayList::new));
    }

    /**
     * 依功能特性獲取推薦
     * @param feature 功能特性
     * @param limit 返回數量
     * @return 符合該特性的推薦列表
     */
    public ArrayList<SearchResult> getRecommendationsByFeature(String feature, int limit) {
        // 搜尋有該功能的咖啡廳
        ArrayList<SearchResult> featureCafes = searchService.searchByFeature(feature);
        
        // 設定 baseline score
        for (SearchResult cafe : featureCafes) {
            double baselineScore = rankingService.getBaselineScore(cafe.getCafeId());
            cafe.setScore(baselineScore);
        }
        
        // 排序並返回
        featureCafes.sort((a, b) -> Double.compare(b.getScore(), a.getScore()));
        
        return featureCafes.stream()
                .limit(limit)
                .collect(Collectors.toCollection(ArrayList::new));
    }

    /**
     * 綜合推薦（結合多個條件）
     * @param districts 地區列表
     * @param features 功能列表
     * @param limit 返回數量
     * @return 綜合推薦列表
     */
    public ArrayList<SearchResult> getCombinedRecommendations(
            List<String> districts,
            List<String> features,
            int limit) {
        
        // 使用進階搜尋（空關鍵字，只用篩選條件）
        ArrayList<SearchResult> results = searchService.advancedSearch("", districts, features);
        
        // 設定 baseline score
        for (SearchResult cafe : results) {
            double baselineScore = rankingService.getBaselineScore(cafe.getCafeId());
            cafe.setScore(baselineScore);
        }
        
        // 排序並返回
        results.sort((a, b) -> Double.compare(b.getScore(), a.getScore()));
        
        return results.stream()
                .limit(limit)
                .collect(Collectors.toCollection(ArrayList::new));
    }

    /**
     * 獲取隨機推薦（探索新咖啡廳）
     * @param count 返回數量
     * @return 隨機推薦列表
     */
    public ArrayList<SearchResult> getRandomRecommendations(int count) {
        List<Cafe> allCafes = searchService.getAllCafes();
        
        // 隨機打亂
        List<Cafe> shuffled = new ArrayList<>(allCafes);
        Collections.shuffle(shuffled);
        
        // 取前 N 個並轉換為 SearchResult
        return shuffled.stream()
                .limit(count)
                .map(cafe -> searchService.getCafeById(cafe.getId()))
                .filter(Objects::nonNull)
                .collect(Collectors.toCollection(ArrayList::new));
    }

    /**
     * 獲取推薦類別
     * @return 所有可用的推薦類別
     */
    public Map<String, List<String>> getAvailableCategories() {
        Map<String, List<String>> categories = new HashMap<>();
        
        categories.put("districts", searchService.getAllDistricts());
        categories.put("features", searchService.getAllFeatures());
        
        return categories;
    }

    /**
     * 獲取推薦理由
     * @param cafeId 咖啡廳 ID
     * @return 推薦理由
     */
    public Map<String, Object> getRecommendationReason(String cafeId) {
        SearchResult cafe = searchService.getCafeById(cafeId);
        
        if (cafe == null) {
            return null;
        }
        
        Map<String, Object> reason = new HashMap<>();
        
        // 基本資訊
        reason.put("cafeName", cafe.getTitle());
        reason.put("district", cafe.getDistrict());
        
        // Baseline score
        double baselineScore = rankingService.getBaselineScore(cafeId);
        reason.put("baselineScore", baselineScore);
        
        // 評分
        reason.put("rating", cafe.getRating());
        
        // 推薦理由列表
        List<String> reasons = new ArrayList<>();
        
        if (baselineScore >= 8.0) {
            reasons.add("熱門咖啡廳（高分: " + String.format("%.1f", baselineScore) + "）");
        }
        
        if (cafe.getRating() >= 4.5) {
            reasons.add("高評價（" + cafe.getRating() + "★）");
        }
        
        if (cafe.getFeatures() != null) {
            if (cafe.getFeatures().contains("不限時")) {
                reasons.add("不限時間，適合長時間工作或讀書");
            }
            if (cafe.getFeatures().contains("有插座")) {
                reasons.add("提供插座，適合使用筆電");
            }
            if (cafe.getFeatures().contains("有wifi")) {
                reasons.add("提供 WiFi 上網");
            }
            if (cafe.getFeatures().contains("CP值高")) {
                reasons.add("價格實惠");
            }
            if (cafe.getFeatures().contains("安靜")) {
                reasons.add("環境安靜，適合專注工作");
            }
        }
        
        reason.put("reasons", reasons);
        
        // 適合的使用情境
        List<String> suitableFor = new ArrayList<>();
        if (cafe.getFeatures() != null) {
            if (cafe.getFeatures().contains("不限時") && cafe.getFeatures().contains("有插座")) {
                suitableFor.add("長時間工作");
                suitableFor.add("寫程式");
                suitableFor.add("準備考試");
            }
            if (cafe.getFeatures().contains("安靜")) {
                suitableFor.add("閱讀");
                suitableFor.add("自習");
            }
            if (cafe.getFeatures().contains("CP值高")) {
                suitableFor.add("學生族群");
                suitableFor.add("預算有限");
            }
        }
        reason.put("suitableFor", suitableFor);
        
        return reason;
    }

    /**
     * 新客推薦（針對新使用者）
     * @param limit 返回數量
     * @return 新客推薦列表
     */
    public ArrayList<SearchResult> getNewUserRecommendations(int limit) {
        // 推薦高分且功能齊全的咖啡廳
        List<Cafe> allCafes = searchService.getAllCafes();
        
        return allCafes.stream()
                .map(cafe -> searchService.getCafeById(cafe.getId()))
                .filter(Objects::nonNull)
                .filter(cafe -> cafe.getRating() >= 4.0) // 評分 4.0 以上
                .filter(cafe -> cafe.getFeatures() != null && cafe.getFeatures().size() >= 3) // 至少 3 個功能
                .map(cafe -> {
                    double baselineScore = rankingService.getBaselineScore(cafe.getCafeId());
                    cafe.setScore(baselineScore);
                    return cafe;
                })
                .sorted((a, b) -> Double.compare(b.getScore(), a.getScore()))
                .limit(limit)
                .collect(Collectors.toCollection(ArrayList::new));
    }

    /**
     * 依時段推薦
     * @param timeSlot 時段（morning, afternoon, evening, night）
     * @param limit 返回數量
     * @return 適合該時段的推薦列表
     */
    public ArrayList<SearchResult> getRecommendationsByTimeSlot(String timeSlot, int limit) {
        ArrayList<SearchResult> recommendations = getTopRecommendations(30);
        
        // 根據時段篩選（這裡簡化處理，實際應根據營業時間）
        List<String> preferredFeatures = new ArrayList<>();
        
        switch (timeSlot.toLowerCase()) {
            case "morning":
                preferredFeatures.add("明亮");
                break;
            case "afternoon":
                preferredFeatures.add("舒適");
                break;
            case "evening":
                preferredFeatures.add("不限時");
                break;
            case "night":
                preferredFeatures.add("安靜");
                break;
        }
        
        return recommendations.stream()
                .filter(cafe -> {
                    if (cafe.getFeatures() == null) return true;
                    return preferredFeatures.stream()
                            .anyMatch(feature -> cafe.getFeatures().contains(feature));
                })
                .limit(limit)
                .collect(Collectors.toCollection(ArrayList::new));
    }

    /**
     * 主題推薦
     * @param theme 主題（study, work, dating, meeting）
     * @param limit 返回數量
     * @return 適合該主題的推薦列表
     */
    public ArrayList<SearchResult> getRecommendationsByTheme(String theme, int limit) {
        List<String> requiredFeatures = new ArrayList<>();
        
        switch (theme.toLowerCase()) {
            case "study":
                requiredFeatures.add("安靜");
                requiredFeatures.add("有插座");
                requiredFeatures.add("不限時");
                break;
            case "work":
                requiredFeatures.add("有wifi");
                requiredFeatures.add("有插座");
                break;
            case "dating":
                requiredFeatures.add("舒適");
                break;
            case "meeting":
                requiredFeatures.add("寬敞");
                break;
        }
        
        return getCombinedRecommendations(null, requiredFeatures, limit);
    }

    /**
     * 獲取推薦統計
     * @return 推薦統計資訊
     */
    public Map<String, Object> getRecommendationStatistics() {
        Map<String, Object> stats = new HashMap<>();
        
        ArrayList<SearchResult> topCafes = getTopRecommendations(10);
        
        stats.put("totalCafes", searchService.getAllCafes().size());
        stats.put("topCafesCount", topCafes.size());
        
        if (!topCafes.isEmpty()) {
            // 平均分數
            double avgScore = topCafes.stream()
                    .mapToDouble(SearchResult::getScore)
                    .average()
                    .orElse(0.0);
            stats.put("averageTopScore", avgScore);
            
            // 最高分
            double maxScore = topCafes.stream()
                    .mapToDouble(SearchResult::getScore)
                    .max()
                    .orElse(0.0);
            stats.put("maxScore", maxScore);
            
            // 地區分布
            Map<String, Long> districtCounts = topCafes.stream()
                    .collect(Collectors.groupingBy(
                        SearchResult::getDistrict,
                        Collectors.counting()
                    ));
            stats.put("topDistrictDistribution", districtCounts);
        }
        
        return stats;
    }

    /**
     * 檢查服務狀態
     */
    public Map<String, Object> getServiceStatus() {
        Map<String, Object> status = new HashMap<>();
        status.put("service", "RecommendationService");
        status.put("status", "running");
        status.put("totalCafes", searchService.getAllCafes().size());
        status.put("usingBaselineScores", true);
        status.put("timestamp", System.currentTimeMillis());
        
        return status;
    }
}

