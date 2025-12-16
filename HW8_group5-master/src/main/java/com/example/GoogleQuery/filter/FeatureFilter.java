package com.example.GoogleQuery.filter;

import com.example.GoogleQuery.model.SearchResult;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * FeatureFilter - 功能性篩選器
 * 根據咖啡廳功能特性進行篩選（如：不限時、有插座、有wifi等）
 */
public class FeatureFilter implements Filter {

    private Set<String> requiredFeatures;
    private boolean matchAll; // true: 需符合所有功能, false: 符合任一功能即可

    /**
     * 建構子（預設需符合所有功能）
     * @param features 必要功能列表
     */
    public FeatureFilter(List<String> features) {
        this(features, true);
    }

    /**
     * 建構子
     * @param features 必要功能列表
     * @param matchAll true: 需符合所有功能, false: 符合任一功能即可
     */
    public FeatureFilter(List<String> features, boolean matchAll) {
        this.requiredFeatures = features != null ? new HashSet<>(features) : new HashSet<>();
        this.matchAll = matchAll;
    }

    /**
     * 建構子（單一功能）
     * @param feature 必要功能
     */
    public FeatureFilter(String feature) {
        this.requiredFeatures = new HashSet<>();
        if (feature != null && !feature.isEmpty()) {
            this.requiredFeatures.add(feature);
        }
        this.matchAll = true;
    }

    /**
     * 執行篩選
     * @param results 搜尋結果列表
     * @return 篩選後的結果列表
     */
    @Override
    public ArrayList<SearchResult> filter(ArrayList<SearchResult> results) {
        // 如果沒有指定功能，返回所有結果
        if (requiredFeatures == null || requiredFeatures.isEmpty()) {
            return results;
        }

        // 篩選符合功能條件的結果
        return results.stream()
                .filter(this::matchesFeatures)
                .collect(Collectors.toCollection(ArrayList::new));
    }

    /**
     * 檢查搜尋結果是否符合功能條件
     * @param result 搜尋結果
     * @return 是否符合
     */
    private boolean matchesFeatures(SearchResult result) {
        List<String> cafeFeatures = result.getFeatures();
        
        // 如果咖啡廳沒有功能資訊，則不符合
        if (cafeFeatures == null || cafeFeatures.isEmpty()) {
            return false;
        }

        Set<String> cafeFeatureSet = new HashSet<>(cafeFeatures);

        if (matchAll) {
            // 需符合所有功能
            return cafeFeatureSet.containsAll(requiredFeatures);
        } else {
            // 符合任一功能即可
            return requiredFeatures.stream()
                    .anyMatch(cafeFeatureSet::contains);
        }
    }

    /**
     * 新增必要功能
     * @param feature 功能名稱
     */
    public void addFeature(String feature) {
        if (feature != null && !feature.isEmpty()) {
            requiredFeatures.add(feature);
        }
    }

    /**
     * 移除必要功能
     * @param feature 功能名稱
     */
    public void removeFeature(String feature) {
        requiredFeatures.remove(feature);
    }

    /**
     * 設定必要功能列表
     * @param features 功能列表
     */
    public void setFeatures(List<String> features) {
        this.requiredFeatures = features != null ? new HashSet<>(features) : new HashSet<>();
    }

    /**
     * 獲取必要功能列表
     * @return 功能列表
     */
    public List<String> getFeatures() {
        return new ArrayList<>(requiredFeatures);
    }

    /**
     * 設定匹配模式
     * @param matchAll true: 需符合所有功能, false: 符合任一功能即可
     */
    public void setMatchAll(boolean matchAll) {
        this.matchAll = matchAll;
    }

    /**
     * 獲取匹配模式
     * @return 是否需符合所有功能
     */
    public boolean isMatchAll() {
        return matchAll;
    }

    /**
     * 清空功能列表
     */
    public void clearFeatures() {
        requiredFeatures.clear();
    }

    /**
     * 檢查是否包含特定功能
     * @param feature 功能名稱
     * @return 是否包含
     */
    public boolean containsFeature(String feature) {
        return requiredFeatures.contains(feature);
    }

    /**
     * 獲取功能數量
     * @return 功能數量
     */
    public int getFeatureCount() {
        return requiredFeatures.size();
    }

    /**
     * 檢查是否為空（沒有設定任何功能）
     * @return 是否為空
     */
    public boolean isEmpty() {
        return requiredFeatures.isEmpty();
    }

    /**
     * 計算搜尋結果符合的功能數量
     * @param result 搜尋結果
     * @return 符合的功能數量
     */
    public int countMatchedFeatures(SearchResult result) {
        if (result.getFeatures() == null) {
            return 0;
        }

        Set<String> cafeFeatureSet = new HashSet<>(result.getFeatures());
        
        return (int) requiredFeatures.stream()
                .filter(cafeFeatureSet::contains)
                .count();
    }

    /**
     * 獲取搜尋結果缺少的功能
     * @param result 搜尋結果
     * @return 缺少的功能列表
     */
    public List<String> getMissingFeatures(SearchResult result) {
        if (result.getFeatures() == null) {
            return new ArrayList<>(requiredFeatures);
        }

        Set<String> cafeFeatureSet = new HashSet<>(result.getFeatures());
        
        return requiredFeatures.stream()
                .filter(feature -> !cafeFeatureSet.contains(feature))
                .collect(Collectors.toList());
    }

    /**
     * 統計篩選結果
     * @param originalResults 原始結果
     * @param filteredResults 篩選後結果
     * @return 統計資訊
     */
    public String getFilterStatistics(ArrayList<SearchResult> originalResults, 
                                     ArrayList<SearchResult> filteredResults) {
        int originalCount = originalResults.size();
        int filteredCount = filteredResults.size();
        int removedCount = originalCount - filteredCount;
        
        StringBuilder stats = new StringBuilder();
        stats.append("功能篩選統計:\n");
        stats.append("  原始結果: ").append(originalCount).append(" 筆\n");
        stats.append("  篩選後: ").append(filteredCount).append(" 筆\n");
        stats.append("  移除: ").append(removedCount).append(" 筆\n");
        stats.append("  保留率: ").append(String.format("%.1f%%", 
            originalCount > 0 ? (double) filteredCount / originalCount * 100 : 0)).append("\n");
        stats.append("  必要功能: ").append(String.join(", ", requiredFeatures)).append("\n");
        stats.append("  匹配模式: ").append(matchAll ? "需符合所有功能" : "符合任一功能即可");
        
        return stats.toString();
    }

    /**
     * 獲取篩選器描述
     * @return 描述文字
     */
    @Override
    public String getDescription() {
        if (requiredFeatures.isEmpty()) {
            return "功能篩選器（無限制）";
        }
        
        String mode = matchAll ? "需全部符合" : "符合任一即可";
        return "功能篩選器（" + String.join(", ", requiredFeatures) + "）[" + mode + "]";
    }

    /**
     * 驗證功能名稱是否合法
     * @param feature 功能名稱
     * @return 是否合法
     */
    public static boolean isValidFeature(String feature) {
        List<String> validFeatures = getAllValidFeatures();
        return validFeatures.contains(feature);
    }

    /**
     * 取得所有有效的功能特性
     * @return 功能列表
     */
    public static List<String> getAllValidFeatures() {
        return List.of(
            "不限時", "有插座", "有wifi", "CP值高",
            "安靜", "明亮", "舒適", "寬敞",
            "有包廂", "可預約", "提供餐點", "寵物友善"
        );
    }

    /**
     * 依類別取得功能特性
     * @param category 類別（environment, facility, service）
     * @return 該類別的功能列表
     */
    public static List<String> getFeaturesByCategory(String category) {
        switch (category.toLowerCase()) {
            case "environment":
                return List.of("安靜", "明亮", "舒適", "寬敞");
            case "facility":
                return List.of("有插座", "有wifi", "有包廂");
            case "service":
                return List.of("不限時", "CP值高", "可預約", "提供餐點", "寵物友善");
            default:
                return new ArrayList<>();
        }
    }

    /**
     * 建立「需全部符合」模式的篩選器
     * @param features 功能列表
     * @return 篩選器實例
     */
    public static FeatureFilter createMatchAllFilter(List<String> features) {
        return new FeatureFilter(features, true);
    }

    /**
     * 建立「符合任一即可」模式的篩選器
     * @param features 功能列表
     * @return 篩選器實例
     */
    public static FeatureFilter createMatchAnyFilter(List<String> features) {
        return new FeatureFilter(features, false);
    }

    /**
     * 複製篩選器
     * @return 新的篩選器實例
     */
    public FeatureFilter copy() {
        return new FeatureFilter(new ArrayList<>(this.requiredFeatures), this.matchAll);
    }

    @Override
    public String toString() {
        return getDescription();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        
        FeatureFilter that = (FeatureFilter) obj;
        return matchAll == that.matchAll && requiredFeatures.equals(that.requiredFeatures);
    }

    @Override
    public int hashCode() {
        int result = requiredFeatures.hashCode();
        result = 31 * result + (matchAll ? 1 : 0);
        return result;
    }
}

