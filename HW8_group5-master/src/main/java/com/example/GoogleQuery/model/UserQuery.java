package com.example.GoogleQuery.model;

import java.util.ArrayList;
import java.util.List;

/**
 * UserQuery - 使用者查詢模型
 * 封裝使用者的搜尋查詢和篩選條件
 */
public class UserQuery {
    
    private String keyword;                // 搜尋關鍵字
    private List<String> districts;        // 地區篩選（可複選）
    private List<String> features;         // 功能篩選（可複選）
    private String sortBy;                 // 排序方式（score/rating/distance）
    private int limit;                     // 返回結果數量限制
    private long timestamp;                // 查詢時間戳
    
    /**
     * 建構子（基本查詢）
     * @param keyword 搜尋關鍵字
     */
    public UserQuery(String keyword) {
        this.keyword = keyword;
        this.districts = new ArrayList<>();
        this.features = new ArrayList<>();
        this.sortBy = "score"; // 預設按分數排序
        this.limit = 20;       // 預設返回 20 個結果
        this.timestamp = System.currentTimeMillis();
    }
    
    /**
     * 建構子（完整查詢）
     * @param keyword 搜尋關鍵字
     * @param districts 地區列表
     * @param features 功能列表
     */
    public UserQuery(String keyword, List<String> districts, List<String> features) {
        this(keyword);
        this.districts = districts != null ? new ArrayList<>(districts) : new ArrayList<>();
        this.features = features != null ? new ArrayList<>(features) : new ArrayList<>();
    }
    
    // ========== Getters and Setters ==========
    
    public String getKeyword() {
        return keyword;
    }
    
    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }
    
    public List<String> getDistricts() {
        return new ArrayList<>(districts);
    }
    
    public void setDistricts(List<String> districts) {
        this.districts = districts != null ? new ArrayList<>(districts) : new ArrayList<>();
    }
    
    public void addDistrict(String district) {
        if (district != null && !district.isEmpty() && !districts.contains(district)) {
            districts.add(district);
        }
    }
    
    public void removeDistrict(String district) {
        districts.remove(district);
    }
    
    public List<String> getFeatures() {
        return new ArrayList<>(features);
    }
    
    public void setFeatures(List<String> features) {
        this.features = features != null ? new ArrayList<>(features) : new ArrayList<>();
    }
    
    public void addFeature(String feature) {
        if (feature != null && !feature.isEmpty() && !features.contains(feature)) {
            features.add(feature);
        }
    }
    
    public void removeFeature(String feature) {
        features.remove(feature);
    }
    
    public String getSortBy() {
        return sortBy;
    }
    
    public void setSortBy(String sortBy) {
        // 驗證排序方式
        if (sortBy != null && 
            (sortBy.equals("score") || sortBy.equals("rating") || sortBy.equals("distance"))) {
            this.sortBy = sortBy;
        }
    }
    
    public int getLimit() {
        return limit;
    }
    
    public void setLimit(int limit) {
        this.limit = Math.max(1, Math.min(100, limit)); // 限制在 1-100 之間
    }
    
    public long getTimestamp() {
        return timestamp;
    }
    
    // ========== 便利方法 ==========
    
    /**
     * 檢查是否有地區篩選
     * @return true 如果有地區篩選
     */
    public boolean hasDistrictFilter() {
        return !districts.isEmpty();
    }
    
    /**
     * 檢查是否有功能篩選
     * @return true 如果有功能篩選
     */
    public boolean hasFeatureFilter() {
        return !features.isEmpty();
    }
    
    /**
     * 檢查是否有任何篩選條件
     * @return true 如果有篩選條件
     */
    public boolean hasFilter() {
        return hasDistrictFilter() || hasFeatureFilter();
    }
    
    /**
     * 檢查關鍵字是否為空
     * @return true 如果關鍵字為空
     */
    public boolean isEmptyKeyword() {
        return keyword == null || keyword.trim().isEmpty();
    }
    
    /**
     * 清除所有篩選條件
     */
    public void clearFilters() {
        districts.clear();
        features.clear();
    }
    
    /**
     * 取得查詢描述（用於顯示）
     * @return 查詢描述字串
     */
    public String getQueryDescription() {
        StringBuilder desc = new StringBuilder();
        
        // 關鍵字
        if (!isEmptyKeyword()) {
            desc.append("搜尋：").append(keyword);
        } else {
            desc.append("全部咖啡廳");
        }
        
        // 地區篩選
        if (hasDistrictFilter()) {
            desc.append(" | 地區：").append(String.join(", ", districts));
        }
        
        // 功能篩選
        if (hasFeatureFilter()) {
            desc.append(" | 功能：").append(String.join(", ", features));
        }
        
        return desc.toString();
    }
    
    /**
     * 驗證查詢是否有效
     * @return true 如果查詢有效
     */
    public boolean isValid() {
        // 至少要有關鍵字或篩選條件
        return !isEmptyKeyword() || hasFilter();
    }
    
    /**
     * 轉換為 JSON 格式
     * @return JSON 字串
     */
    public String toJson() {
        return String.format(
            "{" +
            "\"keyword\": \"%s\", " +
            "\"districts\": [%s], " +
            "\"features\": [%s], " +
            "\"sortBy\": \"%s\", " +
            "\"limit\": %d, " +
            "\"timestamp\": %d" +
            "}",
            keyword != null ? keyword : "",
            formatListForJson(districts),
            formatListForJson(features),
            sortBy,
            limit,
            timestamp
        );
    }
    
    /**
     * 格式化列表為 JSON 陣列字串
     * @param list 列表
     * @return JSON 陣列字串
     */
    private String formatListForJson(List<String> list) {
        if (list == null || list.isEmpty()) {
            return "";
        }
        
        List<String> quoted = new ArrayList<>();
        for (String item : list) {
            quoted.add("\"" + item + "\"");
        }
        
        return String.join(", ", quoted);
    }
    
    /**
     * 複製查詢
     * @return 新的 UserQuery 物件
     */
    public UserQuery clone() {
        UserQuery cloned = new UserQuery(this.keyword);
        cloned.setDistricts(this.districts);
        cloned.setFeatures(this.features);
        cloned.setSortBy(this.sortBy);
        cloned.setLimit(this.limit);
        return cloned;
    }
    
    /**
     * toString 方法
     */
    @Override
    public String toString() {
        return getQueryDescription();
    }
    
    /**
     * equals 方法
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        UserQuery other = (UserQuery) obj;
        return keyword.equals(other.keyword) &&
               districts.equals(other.districts) &&
               features.equals(other.features);
    }
    
    /**
     * hashCode 方法
     */
    @Override
    public int hashCode() {
        return keyword.hashCode() + districts.hashCode() + features.hashCode();
    }
}

