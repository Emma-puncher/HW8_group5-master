package com.example.GoogleQuery.service;

import com.example.GoogleQuery.model.SearchResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * FavoriteService - 收藏服務
 * 提供收藏功能的後端支援
 * 注意：主要使用前端 LocalStorage，此服務提供輔助功能
 */
@Service
public class FavoriteService {

    @Autowired
    private SearchService searchService;

    // 記憶體中的收藏記錄（模擬，實際應使用資料庫）
    private Map<String, Set<String>> userFavorites = new HashMap<>();

    /**
     * 新增收藏
     * @param userId 使用者 ID（如果有登入系統）
     * @param cafeId 咖啡廳 ID
     * @return 是否新增成功
     */
    public boolean addFavorite(String userId, String cafeId) {
        if (userId == null || cafeId == null) {
            return false;
        }
        
        userFavorites.putIfAbsent(userId, new HashSet<>());
        return userFavorites.get(userId).add(cafeId);
    }

    /**
     * 移除收藏
     * @param userId 使用者 ID
     * @param cafeId 咖啡廳 ID
     * @return 是否移除成功
     */
    public boolean removeFavorite(String userId, String cafeId) {
        if (userId == null || cafeId == null) {
            return false;
        }
        
        Set<String> favorites = userFavorites.get(userId);
        if (favorites == null) {
            return false;
        }
        
        return favorites.remove(cafeId);
    }

    /**
     * 檢查是否已收藏
     * @param userId 使用者 ID
     * @param cafeId 咖啡廳 ID
     * @return 是否已收藏
     */
    public boolean isFavorite(String userId, String cafeId) {
        if (userId == null || cafeId == null) {
            return false;
        }
        
        Set<String> favorites = userFavorites.get(userId);
        return favorites != null && favorites.contains(cafeId);
    }

    /**
     * 獲取使用者的所有收藏
     * @param userId 使用者 ID
     * @return 收藏的咖啡廳列表
     */
    public ArrayList<SearchResult> getUserFavorites(String userId) {
        Set<String> favoriteIds = userFavorites.get(userId);
        
        if (favoriteIds == null || favoriteIds.isEmpty()) {
            return new ArrayList<>();
        }
        
        ArrayList<SearchResult> favorites = new ArrayList<>();
        for (String cafeId : favoriteIds) {
            SearchResult cafe = searchService.getCafeById(cafeId);
            if (cafe != null) {
                favorites.add(cafe);
            }
        }
        
        return favorites;
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

    // Compatibility: controller-facing methods
    public ArrayList<SearchResult> getFavoriteDetails(List<String> cafeIds) {
        return getCafesByIds(cafeIds);
    }

    public boolean cafeExists(String cafeId) {
        return searchService.getCafeById(cafeId) != null;
    }

    public Map<String, List<String>> validateBatchCafeIds(List<String> cafeIds) {
        Map<String, List<String>> res = new HashMap<>();
        List<String> valid = new ArrayList<>();
        List<String> invalid = new ArrayList<>();
        for (String id : cafeIds) {
            if (searchService.getCafeById(id) != null) valid.add(id);
            else invalid.add(id);
        }
        res.put("valid", valid);
        res.put("invalid", invalid);
        return res;
    }

    public List<SearchResult> getRecommendationsBasedOnFavorites(List<String> favoriteIds, int limit) {
        ArrayList<SearchResult> favorites = getCafesByIds(favoriteIds);
        if (favorites.isEmpty()) return new ArrayList<>();

        // re-use recommendSimilarCafes logic but using provided favorites
        Map<String, Integer> featureScores = new HashMap<>();
        Set<String> favoriteDistricts = new HashSet<>();
        for (SearchResult f : favorites) {
            favoriteDistricts.add(f.getDistrict());
            if (f.getFeatures() != null) {
                for (String feature : f.getFeatures()) {
                    featureScores.put(feature, featureScores.getOrDefault(feature, 0) + 1);
                }
            }
        }

        List<SearchResult> all = searchService.getAllCafes().stream()
                .map(c -> searchService.getCafeById(c.getId()))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        Set<String> favoriteIdSet = favorites.stream()
                .map(SearchResult::getCafeId)
                .collect(Collectors.toSet());

        return all.stream()
                .filter(c -> !favoriteIdSet.contains(c.getCafeId()))
                .map(c -> {
                    int score = 0;
                    if (favoriteDistricts.contains(c.getDistrict())) score += 3;
                    if (c.getFeatures() != null) {
                        for (String f : c.getFeatures()) score += featureScores.getOrDefault(f, 0);
                    }
                    c.setRecommendationScore((double) score);
                    return c;
                })
                .sorted((a, b) -> Double.compare(b.getRecommendationScore(), a.getRecommendationScore()))
                .limit(limit)
                .collect(Collectors.toList());
    }

    /**
     * 清除使用者的所有收藏
     * @param userId 使用者 ID
     * @return 是否清除成功
     */
    public boolean clearFavorites(String userId) {
        if (userId == null) {
            return false;
        }
        
        Set<String> favorites = userFavorites.remove(userId);
        return favorites != null;
    }

    /**
     * 獲取收藏數量
     * @param userId 使用者 ID
     * @return 收藏數量
     */
    public int getFavoriteCount(String userId) {
        Set<String> favorites = userFavorites.get(userId);
        return favorites != null ? favorites.size() : 0;
    }

    /**
     * 切換收藏狀態（已收藏則移除，未收藏則新增）
     * @param userId 使用者 ID
     * @param cafeId 咖啡廳 ID
     * @return 切換後的狀態（true: 已收藏, false: 未收藏）
     */
    public boolean toggleFavorite(String userId, String cafeId) {
        if (isFavorite(userId, cafeId)) {
            removeFavorite(userId, cafeId);
            return false;
        } else {
            addFavorite(userId, cafeId);
            return true;
        }
    }

    /**
     * 批次新增收藏
     * @param userId 使用者 ID
     * @param cafeIds 咖啡廳 ID 列表
     * @return 成功新增的數量
     */
    public int addFavoritesBatch(String userId, List<String> cafeIds) {
        if (userId == null || cafeIds == null) {
            return 0;
        }
        
        int count = 0;
        for (String cafeId : cafeIds) {
            if (addFavorite(userId, cafeId)) {
                count++;
            }
        }
        
        return count;
    }

    /**
     * 批次移除收藏
     * @param userId 使用者 ID
     * @param cafeIds 咖啡廳 ID 列表
     * @return 成功移除的數量
     */
    public int removeFavoritesBatch(String userId, List<String> cafeIds) {
        if (userId == null || cafeIds == null) {
            return 0;
        }
        
        int count = 0;
        for (String cafeId : cafeIds) {
            if (removeFavorite(userId, cafeId)) {
                count++;
            }
        }
        
        return count;
    }

    /**
     * 獲取收藏統計資訊
     * @param userId 使用者 ID
     * @return 統計資訊
     */
    public Map<String, Object> getFavoriteStatistics(String userId) {
        Map<String, Object> stats = new HashMap<>();
        
        ArrayList<SearchResult> favorites = getUserFavorites(userId);
        
        stats.put("totalFavorites", favorites.size());
        
        if (favorites.isEmpty()) {
            return stats;
        }
        
        // 統計地區分布
        Map<String, Long> districtCounts = favorites.stream()
                .collect(Collectors.groupingBy(
                    SearchResult::getDistrict,
                    Collectors.counting()
                ));
        stats.put("districtDistribution", districtCounts);
        
        // 平均評分
        double avgRating = favorites.stream()
                .filter(f -> f.getRating() > 0)
                .mapToDouble(SearchResult::getRating)
                .average()
                .orElse(0.0);
        stats.put("averageRating", avgRating);
        
        // 統計最常收藏的功能
        Map<String, Long> featureCounts = new HashMap<>();
        for (SearchResult favorite : favorites) {
            if (favorite.getFeatures() != null) {
                for (String feature : favorite.getFeatures()) {
                    featureCounts.put(feature, featureCounts.getOrDefault(feature, 0L) + 1);
                }
            }
        }
        stats.put("topFeatures", featureCounts.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(5)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList())
        );
        
        return stats;
    }

    /**
     * 依據指定的咖啡廳 ID 列表計算統計資訊
     */
    public Map<String, Object> getFavoriteStatistics(List<String> cafeIds) {
        Map<String, Object> stats = new HashMap<>();

        ArrayList<SearchResult> favorites = getCafesByIds(cafeIds);

        stats.put("totalFavorites", favorites.size());

        if (favorites.isEmpty()) {
            return stats;
        }

        // 地區分布
        Map<String, Long> districtCounts = favorites.stream()
                .collect(Collectors.groupingBy(
                    SearchResult::getDistrict,
                    Collectors.counting()
                ));
        stats.put("districtDistribution", districtCounts);

        double avgRating = favorites.stream()
                .filter(f -> f.getRating() > 0)
                .mapToDouble(SearchResult::getRating)
                .average()
                .orElse(0.0);
        stats.put("averageRating", avgRating);

        Map<String, Long> featureCounts = new HashMap<>();
        for (SearchResult favorite : favorites) {
            if (favorite.getFeatures() != null) {
                for (String feature : favorite.getFeatures()) {
                    featureCounts.put(feature, featureCounts.getOrDefault(feature, 0L) + 1);
                }
            }
        }
        stats.put("topFeatures", featureCounts.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(5)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList())
        );

        return stats;
    }

    /**
     * 推薦相似的咖啡廳（基於收藏的咖啡廳）
     * @param userId 使用者 ID
     * @param limit 推薦數量
     * @return 推薦的咖啡廳列表
     */
    public ArrayList<SearchResult> recommendSimilarCafes(String userId, int limit) {
        ArrayList<SearchResult> favorites = getUserFavorites(userId);
        
        if (favorites.isEmpty()) {
            return new ArrayList<>();
        }
        
        // 統計收藏的咖啡廳的共同特徵
        Map<String, Integer> featureScores = new HashMap<>();
        Set<String> favoriteDistricts = new HashSet<>();
        
        for (SearchResult favorite : favorites) {
            favoriteDistricts.add(favorite.getDistrict());
            
            if (favorite.getFeatures() != null) {
                for (String feature : favorite.getFeatures()) {
                    featureScores.put(feature, featureScores.getOrDefault(feature, 0) + 1);
                }
            }
        }
        
        // 獲取所有咖啡廳並計算相似度
        List<SearchResult> allCafes = searchService.getAllCafes().stream()
                .map(cafe -> searchService.getCafeById(cafe.getId()))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        
        Set<String> favoriteIds = favorites.stream()
                .map(SearchResult::getCafeId)
                .collect(Collectors.toSet());
        
        // 過濾已收藏的，計算相似度分數
        return allCafes.stream()
                .filter(cafe -> !favoriteIds.contains(cafe.getCafeId()))
                .map(cafe -> {
                    int score = 0;
                    
                    // 相同地區加分
                    if (favoriteDistricts.contains(cafe.getDistrict())) {
                        score += 3;
                    }
                    
                    // 相同功能加分
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
     * 匯出收藏列表（JSON 格式）
     * @param userId 使用者 ID
     * @return JSON 字串
     */
    public String exportFavorites(String userId) {
        Set<String> favoriteIds = userFavorites.get(userId);
        
        if (favoriteIds == null || favoriteIds.isEmpty()) {
            return "[]";
        }
        
        return "[\"" + String.join("\",\"", favoriteIds) + "\"]";
    }

    /**
     * 兼容：依據 ID 列表匯出收藏資料
     */
    public Map<String, Object> exportFavorites(List<String> cafeIds) {
        Map<String, Object> out = new HashMap<>();
        out.put("ids", cafeIds);
        out.put("count", cafeIds == null ? 0 : cafeIds.size());
        out.put("export", cafeIds == null ? "[]" : "[\"" + String.join("\",\"", cafeIds) + "\"]");
        return out;
    }

    /**
     * 匯入收藏列表
     * @param userId 使用者 ID
     * @param cafeIds 咖啡廳 ID 列表
     * @return 匯入成功的數量
     */
    public int importFavorites(String userId, List<String> cafeIds) {
        return addFavoritesBatch(userId, cafeIds);
    }

    /**
     * 檢查服務狀態
     */
    public Map<String, Object> getServiceStatus() {
        Map<String, Object> status = new HashMap<>();
        status.put("service", "FavoriteService");
        status.put("status", "running");
        status.put("totalUsers", userFavorites.size());
        status.put("totalFavorites", userFavorites.values().stream()
                .mapToInt(Set::size)
                .sum()
        );
        status.put("note", "主要使用前端 LocalStorage");
        status.put("timestamp", System.currentTimeMillis());
        
        return status;
    }
}

