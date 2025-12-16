package com.example.GoogleQuery.repository;

import com.example.GoogleQuery.model.Cafe;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Repository;

import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;

/**
 * CafeRepository - 咖啡廳資料存取
 * 負責從 JSON 檔案載入和管理咖啡廳資料
 */
@Repository
public class CafeRepository {

    private Map<String, Cafe> cafeMap; // 以 ID 為 key 的快速查詢 Map
    private List<Cafe> allCafes; // 所有咖啡廳列表
    
    private static final String DATA_FILE_PATH = "data/cafes.json";

    /**
     * 初始化：載入咖啡廳資料
     */
    @PostConstruct
    public void init() {
        try {
            loadCafesFromFile();
            System.out.println("CafeRepository 初始化完成：已載入 " + allCafes.size() + " 家咖啡廳");
        } catch (IOException e) {
            System.err.println("載入咖啡廳資料失敗: " + e.getMessage());
            // 使用空資料初始化
            allCafes = new ArrayList<>();
            cafeMap = new HashMap<>();
        }
    }

    /**
     * 從 JSON 檔案載入咖啡廳資料
     */
    private void loadCafesFromFile() throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        InputStream inputStream = new ClassPathResource(DATA_FILE_PATH).getInputStream();
        
        allCafes = mapper.readValue(inputStream, new TypeReference<List<Cafe>>() {});
        
        if (allCafes == null) {
            allCafes = new ArrayList<>();
        }
        
        // 建立 ID 索引
        cafeMap = allCafes.stream()
                .collect(Collectors.toMap(Cafe::getId, cafe -> cafe));
    }

    /**
     * 根據 ID 查詢咖啡廳
     * @param cafeId 咖啡廳 ID
     * @return 咖啡廳物件，不存在則返回 null
     */
    public Cafe findById(String cafeId) {
        return cafeMap.get(cafeId);
    }

    /**
     * 查詢所有咖啡廳
     * @return 咖啡廳列表
     */
    public List<Cafe> findAll() {
        return new ArrayList<>(allCafes);
    }

    /**
     * 根據地區查詢咖啡廳
     * @param district 地區名稱
     * @return 該地區的咖啡廳列表
     */
    public List<Cafe> findByDistrict(String district) {
        return allCafes.stream()
                .filter(cafe -> cafe.getDistrict().equals(district))
                .collect(Collectors.toList());
    }

    /**
     * 根據功能特性查詢咖啡廳
     * @param feature 功能特性
     * @return 具有該功能的咖啡廳列表
     */
    public List<Cafe> findByFeature(String feature) {
        return allCafes.stream()
                .filter(cafe -> cafe.getFeatures() != null && 
                               cafe.getFeatures().contains(feature))
                .collect(Collectors.toList());
    }

    /**
     * 根據多個功能特性查詢咖啡廳
     * @param features 功能特性列表
     * @param matchAll true: 需符合所有功能, false: 符合任一功能即可
     * @return 符合條件的咖啡廳列表
     */
    public List<Cafe> findByFeatures(List<String> features, boolean matchAll) {
        if (features == null || features.isEmpty()) {
            return new ArrayList<>();
        }

        return allCafes.stream()
                .filter(cafe -> {
                    if (cafe.getFeatures() == null) {
                        return false;
                    }
                    
                    if (matchAll) {
                        return cafe.getFeatures().containsAll(features);
                    } else {
                        return features.stream()
                                .anyMatch(feature -> cafe.getFeatures().contains(feature));
                    }
                })
                .collect(Collectors.toList());
    }

    /**
     * 根據評分範圍查詢咖啡廳
     * @param minRating 最低評分
     * @param maxRating 最高評分
     * @return 評分在範圍內的咖啡廳列表
     */
    public List<Cafe> findByRatingRange(double minRating, double maxRating) {
        return allCafes.stream()
                .filter(cafe -> cafe.getRating() >= minRating && 
                               cafe.getRating() <= maxRating)
                .collect(Collectors.toList());
    }

    /**
     * 根據最低評分查詢咖啡廳
     * @param minRating 最低評分
     * @return 評分不低於指定值的咖啡廳列表
     */
    public List<Cafe> findByMinRating(double minRating) {
        return allCafes.stream()
                .filter(cafe -> cafe.getRating() >= minRating)
                .collect(Collectors.toList());
    }

    /**
     * 根據名稱模糊查詢咖啡廳
     * @param keyword 關鍵字
     * @return 名稱包含關鍵字的咖啡廳列表
     */
    public List<Cafe> findByNameContaining(String keyword) {
        if (keyword == null || keyword.isEmpty()) {
            return new ArrayList<>();
        }
        
        String lowerKeyword = keyword.toLowerCase();
        
        return allCafes.stream()
                .filter(cafe -> cafe.getName().toLowerCase().contains(lowerKeyword))
                .collect(Collectors.toList());
    }

    /**
     * 根據地址模糊查詢咖啡廳
     * @param keyword 關鍵字
     * @return 地址包含關鍵字的咖啡廳列表
     */
    public List<Cafe> findByAddressContaining(String keyword) {
        if (keyword == null || keyword.isEmpty()) {
            return new ArrayList<>();
        }
        
        String lowerKeyword = keyword.toLowerCase();
        
        return allCafes.stream()
                .filter(cafe -> cafe.getAddress() != null && 
                               cafe.getAddress().toLowerCase().contains(lowerKeyword))
                .collect(Collectors.toList());
    }

    /**
     * 根據標籤查詢咖啡廳
     * @param tag 標籤
     * @return 具有該標籤的咖啡廳列表
     */
    public List<Cafe> findByTag(String tag) {
        return allCafes.stream()
                .filter(cafe -> cafe.getTags() != null && 
                               cafe.getTags().contains(tag))
                .collect(Collectors.toList());
    }

    /**
     * 根據多個 ID 批次查詢咖啡廳
     * @param cafeIds 咖啡廳 ID 列表
     * @return 咖啡廳列表
     */
    public List<Cafe> findByIds(List<String> cafeIds) {
        if (cafeIds == null || cafeIds.isEmpty()) {
            return new ArrayList<>();
        }
        
        return cafeIds.stream()
                .map(this::findById)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    /**
     * 檢查咖啡廳是否存在
     * @param cafeId 咖啡廳 ID
     * @return 是否存在
     */
    public boolean existsById(String cafeId) {
        return cafeMap.containsKey(cafeId);
    }

    /**
     * 獲取咖啡廳總數
     * @return 總數
     */
    public long count() {
        return allCafes.size();
    }

    /**
     * 獲取所有地區列表（不重複）
     * @return 地區列表
     */
    public List<String> findAllDistricts() {
        return allCafes.stream()
                .map(Cafe::getDistrict)
                .distinct()
                .sorted()
                .collect(Collectors.toList());
    }

    /**
     * 獲取所有功能特性列表（不重複）
     * @return 功能列表
     */
    public List<String> findAllFeatures() {
        Set<String> features = new HashSet<>();
        
        for (Cafe cafe : allCafes) {
            if (cafe.getFeatures() != null) {
                features.addAll(cafe.getFeatures());
            }
        }
        
        return new ArrayList<>(features);
    }

    /**
     * 獲取所有標籤列表（不重複）
     * @return 標籤列表
     */
    public List<String> findAllTags() {
        Set<String> tags = new HashSet<>();
        
        for (Cafe cafe : allCafes) {
            if (cafe.getTags() != null) {
                tags.addAll(cafe.getTags());
            }
        }
        
        return new ArrayList<>(tags);
    }

    /**
     * 統計各地區的咖啡廳數量
     * @return Map（地區 -> 數量）
     */
    public Map<String, Long> countByDistrict() {
        return allCafes.stream()
                .collect(Collectors.groupingBy(
                    Cafe::getDistrict,
                    Collectors.counting()
                ));
    }

    /**
     * 統計各功能的咖啡廳數量
     * @return Map（功能 -> 數量）
     */
    public Map<String, Long> countByFeature() {
        Map<String, Long> featureCounts = new HashMap<>();
        
        for (Cafe cafe : allCafes) {
            if (cafe.getFeatures() != null) {
                for (String feature : cafe.getFeatures()) {
                    featureCounts.put(feature, 
                        featureCounts.getOrDefault(feature, 0L) + 1);
                }
            }
        }
        
        return featureCounts;
    }

    /**
     * 獲取評分最高的咖啡廳
     * @param limit 返回數量
     * @return 評分最高的咖啡廳列表
     */
    public List<Cafe> findTopRatedCafes(int limit) {
        return allCafes.stream()
                .sorted((a, b) -> Double.compare(b.getRating(), a.getRating()))
                .limit(limit)
                .collect(Collectors.toList());
    }

    /**
     * 搜尋咖啡廳（名稱、地址、描述）
     * @param keyword 關鍵字
     * @return 符合的咖啡廳列表
     */
    public List<Cafe> search(String keyword) {
        if (keyword == null || keyword.isEmpty()) {
            return new ArrayList<>();
        }
        
        String lowerKeyword = keyword.toLowerCase();
        
        return allCafes.stream()
                .filter(cafe -> 
                    cafe.getName().toLowerCase().contains(lowerKeyword) ||
                    (cafe.getAddress() != null && 
                     cafe.getAddress().toLowerCase().contains(lowerKeyword)) ||
                    (cafe.getDescription() != null && 
                     cafe.getDescription().toLowerCase().contains(lowerKeyword))
                )
                .collect(Collectors.toList());
    }

    /**
     * 進階搜尋（支援多條件）
     * @param criteria 搜尋條件
     * @return 符合條件的咖啡廳列表
     */
    public List<Cafe> advancedSearch(Map<String, Object> criteria) {
        List<Cafe> results = new ArrayList<>(allCafes);
        
        // 地區篩選
        if (criteria.containsKey("district")) {
            String district = (String) criteria.get("district");
            results = results.stream()
                    .filter(cafe -> cafe.getDistrict().equals(district))
                    .collect(Collectors.toList());
        }
        
        // 地區列表篩選
        if (criteria.containsKey("districts")) {
            @SuppressWarnings("unchecked")
            List<String> districts = (List<String>) criteria.get("districts");
            results = results.stream()
                    .filter(cafe -> districts.contains(cafe.getDistrict()))
                    .collect(Collectors.toList());
        }
        
        // 功能篩選
        if (criteria.containsKey("features")) {
            @SuppressWarnings("unchecked")
            List<String> features = (List<String>) criteria.get("features");
            boolean matchAll = criteria.containsKey("matchAllFeatures") 
                ? (Boolean) criteria.get("matchAllFeatures") 
                : true;
            
            results = results.stream()
                    .filter(cafe -> {
                        if (cafe.getFeatures() == null) return false;
                        if (matchAll) {
                            return cafe.getFeatures().containsAll(features);
                        } else {
                            return features.stream()
                                    .anyMatch(f -> cafe.getFeatures().contains(f));
                        }
                    })
                    .collect(Collectors.toList());
        }
        
        // 最低評分篩選
        if (criteria.containsKey("minRating")) {
            double minRating = ((Number) criteria.get("minRating")).doubleValue();
            results = results.stream()
                    .filter(cafe -> cafe.getRating() >= minRating)
                    .collect(Collectors.toList());
        }
        
        // 關鍵字搜尋
        if (criteria.containsKey("keyword")) {
            String keyword = ((String) criteria.get("keyword")).toLowerCase();
            results = results.stream()
                    .filter(cafe -> 
                        cafe.getName().toLowerCase().contains(keyword) ||
                        (cafe.getDescription() != null && 
                         cafe.getDescription().toLowerCase().contains(keyword))
                    )
                    .collect(Collectors.toList());
        }
        
        return results;
    }

    /**
     * 重新載入資料
     * @return 是否成功
     */
    public boolean reload() {
        try {
            loadCafesFromFile();
            System.out.println("重新載入咖啡廳資料成功：" + allCafes.size() + " 家");
            return true;
        } catch (IOException e) {
            System.err.println("重新載入失敗: " + e.getMessage());
            return false;
        }
    }

    /**
     * 獲取統計資訊
     * @return 統計資訊 Map
     */
    public Map<String, Object> getStatistics() {
        Map<String, Object> stats = new HashMap<>();
        
        stats.put("totalCafes", allCafes.size());
        stats.put("districts", findAllDistricts().size());
        stats.put("features", findAllFeatures().size());
        stats.put("tags", findAllTags().size());
        
        // 平均評分
        double avgRating = allCafes.stream()
                .mapToDouble(Cafe::getRating)
                .average()
                .orElse(0.0);
        stats.put("averageRating", avgRating);
        
        // 最高評分
        double maxRating = allCafes.stream()
                .mapToDouble(Cafe::getRating)
                .max()
                .orElse(0.0);
        stats.put("maxRating", maxRating);
        
        // 最低評分
        double minRating = allCafes.stream()
                .mapToDouble(Cafe::getRating)
                .min()
                .orElse(0.0);
        stats.put("minRating", minRating);
        
        // 地區分布
        stats.put("districtDistribution", countByDistrict());
        
        // 功能分布
        stats.put("featureDistribution", countByFeature());
        
        return stats;
    }

    /**
     * 檢查 Repository 狀態
     * @return 狀態資訊
     */
    public Map<String, Object> getStatus() {
        Map<String, Object> status = new HashMap<>();
        status.put("repository", "CafeRepository");
        status.put("status", "running");
        status.put("dataLoaded", !allCafes.isEmpty());
        status.put("totalCafes", allCafes.size());
        status.put("dataFile", DATA_FILE_PATH);
        status.put("timestamp", System.currentTimeMillis());
        
        return status;
    }
}

