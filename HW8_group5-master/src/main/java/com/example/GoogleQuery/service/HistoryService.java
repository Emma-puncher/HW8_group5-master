package com.example.GoogleQuery.service;

import com.example.GoogleQuery.model.SearchResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * HistoryService - 瀏覽紀錄服務
 * 提供瀏覽紀錄功能的後端支援
 * 注意：主要使用前端 LocalStorage，此服務提供輔助功能
 */
@Service
public class HistoryService {

    @Autowired
    private SearchService searchService;

    // 記憶體中的瀏覽記錄（模擬，實際應使用資料庫）
    // Map<userId, List<HistoryEntry>>
    private Map<String, List<HistoryEntry>> userHistories = new HashMap<>();

    /**
     * 瀏覽記錄項目
     */
    private static class HistoryEntry {
        String cafeId;
        String cafeName;
        long timestamp;
        
        HistoryEntry(String cafeId, String cafeName, long timestamp) {
            this.cafeId = cafeId;
            this.cafeName = cafeName;
            this.timestamp = timestamp;
        }
    }

    /**
     * 記錄瀏覽行為
     * @param historyData 瀏覽資料（包含 cafeId, cafeName, timestamp 等）
     * @return 是否記錄成功
     */
    public boolean recordVisit(Map<String, Object> historyData) {
        try {
            String userId = (String) historyData.getOrDefault("userId", "guest");
            String cafeId = (String) historyData.get("cafeId");
            String cafeName = (String) historyData.get("cafeName");
            
            long timestamp = historyData.containsKey("timestamp")
                ? ((Number) historyData.get("timestamp")).longValue()
                : System.currentTimeMillis();
            
            if (cafeId == null) {
                return false;
            }
            
            userHistories.putIfAbsent(userId, new ArrayList<>());
            List<HistoryEntry> history = userHistories.get(userId);
            
            // 移除相同咖啡廳的舊記錄（避免重複）
            history.removeIf(entry -> entry.cafeId.equals(cafeId));
            
            // 新增到最前面
            history.add(0, new HistoryEntry(cafeId, cafeName, timestamp));
            
            // 限制記錄數量（最多保留 100 條）
            if (history.size() > 100) {
                history.remove(history.size() - 1);
            }
            
            return true;
            
        } catch (Exception e) {
            System.err.println("記錄瀏覽失敗: " + e.getMessage());
            return false;
        }
    }

    /**
     * 批次獲取咖啡廳（根據 ID 列表）
     * 用於從前端 LocalStorage 傳來的 ID 列表獲取完整資訊
     * @param cafeIds 咖啡廳 ID 列表
     * @return 咖啡廳詳細資訊列表
     */
    public ArrayList<SearchResult> getCafesByIds(List<String> cafeIds) {
        if (cafeIds == null || cafeIds.isEmpty()) {
            return new ArrayList<>();
        }
        
        ArrayList<SearchResult> cafes = new ArrayList<>();
        for (String cafeId : cafeIds) {
            SearchResult cafe = searchService.getCafeById(cafeId);
            if (cafe != null) {
                cafes.add(cafe);
            }
        }
        
        return cafes;
    }

    /**
     * 獲取最近瀏覽記錄
     * @param limit 返回數量
     * @return 最近瀏覽的咖啡廳列表
     */
    public ArrayList<SearchResult> getRecentVisits(int limit) {
        return getRecentVisitsByUser("guest", limit);
    }

    /**
     * 獲取指定使用者的最近瀏覽記錄
     * @param userId 使用者 ID
     * @param limit 返回數量
     * @return 最近瀏覽的咖啡廳列表
     */
    public ArrayList<SearchResult> getRecentVisitsByUser(String userId, int limit) {
        List<HistoryEntry> history = userHistories.get(userId);
        
        if (history == null || history.isEmpty()) {
            return new ArrayList<>();
        }
        
        return history.stream()
                .limit(limit)
                .map(entry -> searchService.getCafeById(entry.cafeId))
                .filter(Objects::nonNull)
                .collect(Collectors.toCollection(ArrayList::new));
    }

    /**
     * 清除所有瀏覽記錄
     * @return 是否清除成功
     */
    public boolean clearAllHistory() {
        return clearHistoryByUser("guest");
    }

    /**
     * 清除指定使用者的瀏覽記錄
     * @param userId 使用者 ID
     * @return 是否清除成功
     */
    public boolean clearHistoryByUser(String userId) {
        List<HistoryEntry> history = userHistories.remove(userId);
        return history != null;
    }

    /**
     * 移除特定的瀏覽記錄
     * @param userId 使用者 ID
     * @param cafeId 咖啡廳 ID
     * @return 是否移除成功
     */
    public boolean removeHistoryEntry(String userId, String cafeId) {
        List<HistoryEntry> history = userHistories.get(userId);
        
        if (history == null) {
            return false;
        }
        
        return history.removeIf(entry -> entry.cafeId.equals(cafeId));
    }

    /**
     * 獲取瀏覽統計資訊
     * @return 統計資訊
     */
    public Map<String, Object> getVisitStatistics() {
        return getVisitStatisticsByUser("guest");
    }

    /**
     * 獲取指定使用者的瀏覽統計資訊
     * @param userId 使用者 ID
     * @return 統計資訊
     */
    public Map<String, Object> getVisitStatisticsByUser(String userId) {
        Map<String, Object> stats = new HashMap<>();
        
        List<HistoryEntry> history = userHistories.get(userId);
        
        if (history == null || history.isEmpty()) {
            stats.put("totalVisits", 0);
            return stats;
        }
        
        stats.put("totalVisits", history.size());
        
        // 獲取完整資訊
        ArrayList<SearchResult> cafes = history.stream()
                .map(entry -> searchService.getCafeById(entry.cafeId))
                .filter(Objects::nonNull)
                .collect(Collectors.toCollection(ArrayList::new));
        
        if (cafes.isEmpty()) {
            return stats;
        }
        
        // 統計地區分布
        Map<String, Long> districtCounts = cafes.stream()
                .collect(Collectors.groupingBy(
                    SearchResult::getDistrict,
                    Collectors.counting()
                ));
        stats.put("districtDistribution", districtCounts);
        
        // 最常訪問的咖啡廳
        Map<String, Long> visitCounts = history.stream()
                .collect(Collectors.groupingBy(
                    entry -> entry.cafeId,
                    Collectors.counting()
                ));
        
        String mostVisitedId = visitCounts.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(null);
        
        if (mostVisitedId != null) {
            SearchResult mostVisited = searchService.getCafeById(mostVisitedId);
            if (mostVisited != null) {
                stats.put("mostVisited", mostVisited.getTitle());
                stats.put("mostVisitedCount", visitCounts.get(mostVisitedId));
            }
        }
        
        // 統計常訪問的功能特性
        Map<String, Long> featureCounts = new HashMap<>();
        for (SearchResult cafe : cafes) {
            if (cafe.getFeatures() != null) {
                for (String feature : cafe.getFeatures()) {
                    featureCounts.put(feature, featureCounts.getOrDefault(feature, 0L) + 1);
                }
            }
        }
        
        List<String> topFeatures = featureCounts.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(5)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
        stats.put("topFeatures", topFeatures);
        
        // 平均評分
        double avgRating = cafes.stream()
                .filter(c -> c.getRating() > 0)
                .mapToDouble(SearchResult::getRating)
                .average()
                .orElse(0.0);
        stats.put("averageRating", avgRating);
        
        // 最近訪問時間
        if (!history.isEmpty()) {
            stats.put("lastVisitTime", history.get(0).timestamp);
            stats.put("lastVisitCafe", history.get(0).cafeName);
        }
        
        return stats;
    }

    /**
     * 依時間範圍獲取瀏覽記錄
     * @param userId 使用者 ID
     * @param startTime 開始時間（毫秒）
     * @param endTime 結束時間（毫秒）
     * @return 該時間範圍的瀏覽記錄
     */
    public ArrayList<SearchResult> getVisitsByTimeRange(
            String userId, 
            long startTime, 
            long endTime) {
        
        List<HistoryEntry> history = userHistories.get(userId);
        
        if (history == null || history.isEmpty()) {
            return new ArrayList<>();
        }
        
        return history.stream()
                .filter(entry -> entry.timestamp >= startTime && entry.timestamp <= endTime)
                .map(entry -> searchService.getCafeById(entry.cafeId))
                .filter(Objects::nonNull)
                .collect(Collectors.toCollection(ArrayList::new));
    }

    /**
     * 獲取今日瀏覽記錄
     * @param userId 使用者 ID
     * @return 今日瀏覽的咖啡廳列表
     */
    public ArrayList<SearchResult> getTodayVisits(String userId) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        
        long startOfDay = calendar.getTimeInMillis();
        long endOfDay = System.currentTimeMillis();
        
        return getVisitsByTimeRange(userId, startOfDay, endOfDay);
    }

    /**
     * 獲取本週瀏覽記錄
     * @param userId 使用者 ID
     * @return 本週瀏覽的咖啡廳列表
     */
    public ArrayList<SearchResult> getWeekVisits(String userId) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.DAY_OF_WEEK, calendar.getFirstDayOfWeek());
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        
        long startOfWeek = calendar.getTimeInMillis();
        long endOfWeek = System.currentTimeMillis();
        
        return getVisitsByTimeRange(userId, startOfWeek, endOfWeek);
    }

    /**
     * 依地區獲取瀏覽記錄
     * @param userId 使用者 ID
     * @param district 地區
     * @return 該地區的瀏覽記錄
     */
    public ArrayList<SearchResult> getVisitsByDistrict(String userId, String district) {
        List<HistoryEntry> history = userHistories.get(userId);
        
        if (history == null || history.isEmpty()) {
            return new ArrayList<>();
        }
        
        return history.stream()
                .map(entry -> searchService.getCafeById(entry.cafeId))
                .filter(Objects::nonNull)
                .filter(cafe -> cafe.getDistrict().equals(district))
                .collect(Collectors.toCollection(ArrayList::new));
    }

    /**
     * 推薦基於瀏覽記錄的咖啡廳
     * @param userId 使用者 ID
     * @param limit 推薦數量
     * @return 推薦的咖啡廳列表
     */
    public ArrayList<SearchResult> recommendBasedOnHistory(String userId, int limit) {
        List<HistoryEntry> history = userHistories.get(userId);
        
        if (history == null || history.isEmpty()) {
            return new ArrayList<>();
        }
        
        // 獲取瀏覽過的咖啡廳
        Set<String> visitedIds = history.stream()
                .map(entry -> entry.cafeId)
                .collect(Collectors.toSet());
        
        ArrayList<SearchResult> visitedCafes = history.stream()
                .map(entry -> searchService.getCafeById(entry.cafeId))
                .filter(Objects::nonNull)
                .collect(Collectors.toCollection(ArrayList::new));
        
        // 統計偏好
        Map<String, Integer> featureScores = new HashMap<>();
        Set<String> preferredDistricts = new HashSet<>();
        
        for (SearchResult cafe : visitedCafes) {
            preferredDistricts.add(cafe.getDistrict());
            
            if (cafe.getFeatures() != null) {
                for (String feature : cafe.getFeatures()) {
                    featureScores.put(feature, featureScores.getOrDefault(feature, 0) + 1);
                }
            }
        }
        
        // 獲取所有咖啡廳並計算相似度
        List<SearchResult> allCafes = searchService.getAllCafes().stream()
                .map(cafe -> searchService.getCafeById(cafe.getId()))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        
        // 過濾已訪問的，計算推薦分數
        return allCafes.stream()
                .filter(cafe -> !visitedIds.contains(cafe.getCafeId()))
                .map(cafe -> {
                    int score = 0;
                    
                    // 偏好地區加分
                    if (preferredDistricts.contains(cafe.getDistrict())) {
                        score += 3;
                    }
                    
                    // 偏好功能加分
                    if (cafe.getFeatures() != null) {
                        for (String feature : cafe.getFeatures()) {
                            score += featureScores.getOrDefault(feature, 0);
                        }
                    }
                    
                    cafe.setRecommendationScore((double) score);
                    return cafe;
                })
                .sorted((a, b) -> Double.compare(
                    b.getRecommendationScore(), 
                    a.getRecommendationScore()
                ))
                .limit(limit)
                .collect(Collectors.toCollection(ArrayList::new));
    }

    /**
     * 匯出瀏覽記錄
     * @param userId 使用者 ID
     * @return JSON 字串
     */
    public String exportHistory(String userId) {
        List<HistoryEntry> history = userHistories.get(userId);
        
        if (history == null || history.isEmpty()) {
            return "[]";
        }
        
        StringBuilder json = new StringBuilder("[");
        for (int i = 0; i < history.size(); i++) {
            HistoryEntry entry = history.get(i);
            json.append("{")
                .append("\"cafeId\":\"").append(entry.cafeId).append("\",")
                .append("\"cafeName\":\"").append(entry.cafeName).append("\",")
                .append("\"timestamp\":").append(entry.timestamp)
                .append("}");
            
            if (i < history.size() - 1) {
                json.append(",");
            }
        }
        json.append("]");
        
        return json.toString();
    }

    /**
     * 檢查服務狀態
     */
    public Map<String, Object> getServiceStatus() {
        Map<String, Object> status = new HashMap<>();
        status.put("service", "HistoryService");
        status.put("status", "running");
        status.put("totalUsers", userHistories.size());
        status.put("totalRecords", userHistories.values().stream()
                .mapToInt(List::size)
                .sum()
        );
        status.put("note", "主要使用前端 LocalStorage");
        status.put("timestamp", System.currentTimeMillis());
        
        return status;
    }
}

