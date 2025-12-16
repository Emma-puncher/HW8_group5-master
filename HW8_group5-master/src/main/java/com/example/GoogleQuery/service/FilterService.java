package com.example.GoogleQuery.service;

import com.example.GoogleQuery.model.Cafe;
import com.example.GoogleQuery.repository.CafeRepository;
import com.example.GoogleQuery.model.SearchResult;
import com.example.GoogleQuery.filter.DistrictFilter;
import com.example.GoogleQuery.filter.FeatureFilter;
import com.example.GoogleQuery.filter.FilterChain;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;

import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;

/**
 * FilterService - 篩選服務
 * 提供地區、功能等多種篩選功能
 */
@Service
public class FilterService {

    private List<String> availableDistricts;
    private List<String> availableFeatures;
    private Map<String, String> districtInfo;
    private Map<String, String> featureDescriptions;

    /**
     * 初始化：載入篩選器配置
     */
    @PostConstruct
    public void init() {
        try {
            loadDistrictsConfig();
            loadFeaturesConfig();
            
            System.out.println("FilterService 初始化完成：");
            System.out.println("  - 可用地區: " + availableDistricts.size());
            System.out.println("  - 可用功能: " + availableFeatures.size());
            
        } catch (Exception e) {
            System.err.println("FilterService 初始化失敗: " + e.getMessage());
            initializeDefaultConfig();
        }
    }

    @Autowired
    private CafeRepository cafeRepository;

    /**
     * 兼容性方法：回傳所有地區
     */
    public List<String> getAllDistricts() {
        if (availableDistricts != null && !availableDistricts.isEmpty()) {
            return getAvailableDistricts();
        }
        return cafeRepository != null ? cafeRepository.findAllDistricts() : new ArrayList<>();
    }

    /**
     * 兼容性方法：回傳所有功能
     */
    public List<String> getAllFeatures() {
        if (availableFeatures != null && !availableFeatures.isEmpty()) {
            return getAvailableFeatures();
        }
        return cafeRepository != null ? cafeRepository.findAllFeatures() : new ArrayList<>();
    }

    public int getCafeCountByDistrict(String district) {
        if (cafeRepository == null) return 0;
        return cafeRepository.findByDistrict(district).size();
    }

    public int getCafeCountByFeature(String feature) {
        if (cafeRepository == null) return 0;
        return cafeRepository.findByFeature(feature).size();
    }

    public boolean validateFilters(Map<String, Object> filterData) {
        if (filterData == null) return true;
        if (filterData.containsKey("districts")) {
            @SuppressWarnings("unchecked")
            List<String> districts = (List<String>) filterData.get("districts");
            List<String> valid = validateDistricts(districts);
            if (valid.size() != (districts == null ? 0 : districts.size())) return false;
        }
        if (filterData.containsKey("features")) {
            @SuppressWarnings("unchecked")
            List<String> features = (List<String>) filterData.get("features");
            List<String> valid = validateFeatures(features);
            if (valid.size() != (features == null ? 0 : features.size())) return false;
        }
        return true;
    }

    public Map<String, Integer> getDistrictStatistics() {
        Map<String, Integer> stats = new HashMap<>();
        if (cafeRepository == null) return stats;
        Map<String, Long> raw = cafeRepository.countByDistrict();
        for (Map.Entry<String, Long> e : raw.entrySet()) {
            stats.put(e.getKey(), e.getValue().intValue());
        }
        return stats;
    }

    public Map<String, Integer> getFeatureStatistics() {
        Map<String, Integer> stats = new HashMap<>();
        if (cafeRepository == null) return stats;
        Map<String, Long> raw = cafeRepository.countByFeature();
        for (Map.Entry<String, Long> e : raw.entrySet()) {
            stats.put(e.getKey(), e.getValue().intValue());
        }
        return stats;
    }

    /**
     * 載入地區配置
     */
    private void loadDistrictsConfig() throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        InputStream inputStream = new ClassPathResource("data/districts.json").getInputStream();
        
        Map<String, Object> data = mapper.readValue(inputStream, new TypeReference<Map<String, Object>>() {});
        
        availableDistricts = (List<String>) data.get("districts");
        districtInfo = (Map<String, String>) data.get("info");
        
        if (availableDistricts == null) {
            availableDistricts = new ArrayList<>();
        }
        if (districtInfo == null) {
            districtInfo = new HashMap<>();
        }
    }

    /**
     * 載入功能特性配置
     */
    private void loadFeaturesConfig() throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        InputStream inputStream = new ClassPathResource("data/features.json").getInputStream();
        
        Map<String, Object> data = mapper.readValue(inputStream, new TypeReference<Map<String, Object>>() {});
        
        availableFeatures = (List<String>) data.get("features");
        featureDescriptions = (Map<String, String>) data.get("descriptions");
        
        if (availableFeatures == null) {
            availableFeatures = new ArrayList<>();
        }
        if (featureDescriptions == null) {
            featureDescriptions = new HashMap<>();
        }
    }

    /**
     * 初始化預設配置
     */
    private void initializeDefaultConfig() {
        availableDistricts = Arrays.asList(
            "中正區", "大同區", "中山區", "松山區", "大安區",
            "萬華區", "信義區", "士林區", "北投區", "內湖區",
            "南港區", "文山區"
        );
        
        availableFeatures = Arrays.asList(
            "不限時", "有插座", "有wifi", "CP值高",
            "安靜", "明亮", "舒適", "寬敞"
        );
        
        districtInfo = new HashMap<>();
        featureDescriptions = new HashMap<>();
    }

    /**
     * 依地區篩選
     * @param results 搜尋結果
     * @param districts 地區列表
     * @return 篩選後的結果
     */
    public ArrayList<SearchResult> filterByDistrict(
            ArrayList<SearchResult> results, 
            List<String> districts) {
        
        if (districts == null || districts.isEmpty()) {
            return results;
        }
        
        DistrictFilter filter = new DistrictFilter(districts);
        return filter.filter(results);
    }

    /**
     * 依功能特性篩選
     * @param results 搜尋結果
     * @param features 功能列表
     * @return 篩選後的結果
     */
    public ArrayList<SearchResult> filterByFeature(
            ArrayList<SearchResult> results,
            List<String> features) {
        
        if (features == null || features.isEmpty()) {
            return results;
        }
        
        FeatureFilter filter = new FeatureFilter(features);
        return filter.filter(results);
    }

    /**
     * 組合篩選（地區 + 功能）
     * @param results 搜尋結果
     * @param districts 地區列表
     * @param features 功能列表
     * @return 篩選後的結果
     */
    public ArrayList<SearchResult> filterByCombination(
            ArrayList<SearchResult> results,
            List<String> districts,
            List<String> features) {
        
        // 使用 FilterChain 串接篩選器
        FilterChain filterChain = new FilterChain();
        
        if (districts != null && !districts.isEmpty()) {
            filterChain.addFilter(new DistrictFilter(districts));
        }
        
        if (features != null && !features.isEmpty()) {
            filterChain.addFilter(new FeatureFilter(features));
        }
        
        return filterChain.apply(results);
    }

    /**
     * 依評分篩選
     * @param results 搜尋結果
     * @param minRating 最低評分
     * @return 篩選後的結果
     */
    public ArrayList<SearchResult> filterByRating(
            ArrayList<SearchResult> results,
            double minRating) {
        
        return results.stream()
                .filter(result -> result.getRating() >= minRating)
                .collect(Collectors.toCollection(ArrayList::new));
    }

    /**
     * 依評分範圍篩選
     * @param results 搜尋結果
     * @param minRating 最低評分
     * @param maxRating 最高評分
     * @return 篩選後的結果
     */
    public ArrayList<SearchResult> filterByRatingRange(
            ArrayList<SearchResult> results,
            double minRating,
            double maxRating) {
        
        return results.stream()
                .filter(result -> result.getRating() >= minRating && result.getRating() <= maxRating)
                .collect(Collectors.toCollection(ArrayList::new));
    }

    /**
     * 依標籤篩選
     * @param results 搜尋結果
     * @param tags 標籤列表
     * @param matchAll 是否需要匹配所有標籤（true）或任一標籤（false）
     * @return 篩選後的結果
     */
    public ArrayList<SearchResult> filterByTags(
            ArrayList<SearchResult> results,
            List<String> tags,
            boolean matchAll) {
        
        if (tags == null || tags.isEmpty()) {
            return results;
        }
        
        return results.stream()
                .filter(result -> {
                    if (result.getTags() == null) {
                        return false;
                    }
                    
                    if (matchAll) {
                        // 需要包含所有標籤
                        return result.getTags().containsAll(tags);
                    } else {
                        // 只需包含任一標籤
                        return tags.stream().anyMatch(tag -> result.getTags().contains(tag));
                    }
                })
                .collect(Collectors.toCollection(ArrayList::new));
    }

    /**
     * 依距離篩選（如果有座標資訊）
     * @param results 搜尋結果
     * @param centerLat 中心緯度
     * @param centerLng 中心經度
     * @param radiusKm 半徑（公里）
     * @return 篩選後的結果
     */
    public ArrayList<SearchResult> filterByDistance(
            ArrayList<SearchResult> results,
            double centerLat,
            double centerLng,
            double radiusKm) {
        
        return results.stream()
                .filter(result -> {
                    // 假設 SearchResult 有座標資訊
                    if (result.getLatitude() == null || result.getLongitude() == null) {
                        return false;
                    }
                    
                    double distance = calculateDistance(
                        centerLat, centerLng,
                        result.getLatitude(), result.getLongitude()
                    );
                    
                    return distance <= radiusKm;
                })
                .collect(Collectors.toCollection(ArrayList::new));
    }

    /**
     * 計算兩點之間的距離（使用 Haversine 公式）
     * @return 距離（公里）
     */
    private double calculateDistance(double lat1, double lng1, double lat2, double lng2) {
        final double R = 6371; // 地球半徑（公里）
        
        double dLat = Math.toRadians(lat2 - lat1);
        double dLng = Math.toRadians(lng2 - lng1);
        
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                   Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                   Math.sin(dLng / 2) * Math.sin(dLng / 2);
        
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        
        return R * c;
    }

    /**
     * 進階組合篩選（支援多種條件）
     * @param results 搜尋結果
     * @param filterCriteria 篩選條件
     * @return 篩選後的結果
     */
    public ArrayList<SearchResult> filterByAdvancedCriteria(
            ArrayList<SearchResult> results,
            Map<String, Object> filterCriteria) {
        
        ArrayList<SearchResult> filtered = results;
        
        // 地區篩選
        if (filterCriteria.containsKey("districts")) {
            List<String> districts = (List<String>) filterCriteria.get("districts");
            filtered = filterByDistrict(filtered, districts);
        }
        
        // 功能篩選
        if (filterCriteria.containsKey("features")) {
            List<String> features = (List<String>) filterCriteria.get("features");
            filtered = filterByFeature(filtered, features);
        }
        
        // 評分篩選
        if (filterCriteria.containsKey("minRating")) {
            double minRating = ((Number) filterCriteria.get("minRating")).doubleValue();
            filtered = filterByRating(filtered, minRating);
        }
        
        // 標籤篩選
        if (filterCriteria.containsKey("tags")) {
            List<String> tags = (List<String>) filterCriteria.get("tags");
            boolean matchAll = filterCriteria.containsKey("matchAllTags")
                ? (Boolean) filterCriteria.get("matchAllTags")
                : false;
            filtered = filterByTags(filtered, tags, matchAll);
        }
        
        return filtered;
    }

    /**
     * 獲取可用的地區列表
     * @return 地區列表
     */
    public List<String> getAvailableDistricts() {
        return new ArrayList<>(availableDistricts);
    }

    /**
     * 獲取可用的功能特性列表
     * @return 功能列表
     */
    public List<String> getAvailableFeatures() {
        return new ArrayList<>(availableFeatures);
    }

    /**
     * 獲取地區資訊
     * @param district 地區名稱
     * @return 地區資訊
     */
    public String getDistrictInfo(String district) {
        return districtInfo.getOrDefault(district, "無相關資訊");
    }

    /**
     * 獲取功能描述
     * @param feature 功能名稱
     * @return 功能描述
     */
    public String getFeatureDescription(String feature) {
        return featureDescriptions.getOrDefault(feature, "無相關描述");
    }

    /**
     * 驗證地區是否有效
     * @param district 地區名稱
     * @return 是否有效
     */
    public boolean isValidDistrict(String district) {
        return availableDistricts.contains(district);
    }

    /**
     * 驗證功能是否有效
     * @param feature 功能名稱
     * @return 是否有效
     */
    public boolean isValidFeature(String feature) {
        return availableFeatures.contains(feature);
    }

    /**
     * 驗證地區列表
     * @param districts 地區列表
     * @return 有效的地區列表
     */
    public List<String> validateDistricts(List<String> districts) {
        if (districts == null) {
            return new ArrayList<>();
        }
        
        return districts.stream()
                .filter(this::isValidDistrict)
                .collect(Collectors.toList());
    }

    /**
     * 驗證功能列表
     * @param features 功能列表
     * @return 有效的功能列表
     */
    public List<String> validateFeatures(List<String> features) {
        if (features == null) {
            return new ArrayList<>();
        }
        
        return features.stream()
                .filter(this::isValidFeature)
                .collect(Collectors.toList());
    }

    /**
     * 獲取篩選統計
     * @param originalResults 原始結果
     * @param filteredResults 篩選後結果
     * @return 統計資訊
     */
    public Map<String, Object> getFilterStatistics(
            ArrayList<SearchResult> originalResults,
            ArrayList<SearchResult> filteredResults) {
        
        Map<String, Object> stats = new HashMap<>();
        
        stats.put("originalCount", originalResults.size());
        stats.put("filteredCount", filteredResults.size());
        stats.put("removedCount", originalResults.size() - filteredResults.size());
        
        double retentionRate = originalResults.isEmpty() 
            ? 0.0 
            : (double) filteredResults.size() / originalResults.size() * 100;
        stats.put("retentionRate", String.format("%.1f%%", retentionRate));
        
        // 統計篩選後的地區分布
        Map<String, Long> districtDistribution = filteredResults.stream()
                .collect(Collectors.groupingBy(
                    SearchResult::getDistrict,
                    Collectors.counting()
                ));
        stats.put("districtDistribution", districtDistribution);
        
        // 統計平均評分
        double avgRating = filteredResults.stream()
                .filter(r -> r.getRating() > 0)
                .mapToDouble(SearchResult::getRating)
                .average()
                .orElse(0.0);
        stats.put("averageRating", avgRating);
        
        return stats;
    }

    /**
     * 取得篩選器配置摘要
     * @return 配置摘要
     */
    public Map<String, Object> getFilterConfiguration() {
        Map<String, Object> config = new HashMap<>();
        
        config.put("availableDistricts", availableDistricts);
        config.put("availableFeatures", availableFeatures);
        config.put("districtCount", availableDistricts.size());
        config.put("featureCount", availableFeatures.size());
        
        return config;
    }

    /**
     * 建議篩選條件（基於搜尋結果）
     * @param results 搜尋結果
     * @return 建議的篩選條件
     */
    public Map<String, Object> suggestFilters(ArrayList<SearchResult> results) {
        Map<String, Object> suggestions = new HashMap<>();
        
        // 統計地區分布
        Map<String, Long> districtCounts = results.stream()
                .collect(Collectors.groupingBy(
                    SearchResult::getDistrict,
                    Collectors.counting()
                ));
        
        // 找出結果最多的地區
        String topDistrict = districtCounts.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(null);
        
        suggestions.put("topDistrict", topDistrict);
        suggestions.put("districtDistribution", districtCounts);
        
        // 統計常見功能
        Map<String, Long> featureCounts = new HashMap<>();
        for (SearchResult result : results) {
            if (result.getFeatures() != null) {
                for (String feature : result.getFeatures()) {
                    featureCounts.put(feature, featureCounts.getOrDefault(feature, 0L) + 1);
                }
            }
        }
        
        List<String> topFeatures = featureCounts.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(5)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
        
        suggestions.put("topFeatures", topFeatures);
        suggestions.put("featureDistribution", featureCounts);
        
        return suggestions;
    }

    /**
     * 檢查服務狀態
     */
    public Map<String, Object> getServiceStatus() {
        Map<String, Object> status = new HashMap<>();
        status.put("service", "FilterService");
        status.put("status", "running");
        status.put("availableDistricts", availableDistricts.size());
        status.put("availableFeatures", availableFeatures.size());
        status.put("timestamp", System.currentTimeMillis());
        
        return status;
    }
}

