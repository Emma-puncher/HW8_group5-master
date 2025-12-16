package com.example.GoogleQuery.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.ArrayList;

/**
 * SearchResult - 搜尋結果封裝類別
 * 將 WebPage 和其對應的分數包裝在一起
 */
public class SearchResult implements Comparable<SearchResult> {
    
    private WebPage page;
    private double score;

    // 額外的咖啡廳資訊（用於 API 回傳）
    private String cafeId;
    private String phoneNumber;
    private String openingHours;
    private double rating;
    private List<String> features;
    private List<String> tags;
    private Integer userRatingsTotal;
    private Double latitude;
    private Double longitude;
    
    // Stage 3: Google 搜尋整合欄位
    private String source;  // "local" 或 "google"
    private boolean isGoogleResult;  // 是否為 Google 搜尋結果
    private String district;  // 地區（本地結果用）
    private String address;   // 地址（本地結果用）
    
    /**
     * 建構子
     * @param page 網站
     * @param score 分數
     */
    public SearchResult(WebPage page, double score) {
        this.page = page;
        this.score = score;
    }

    /**
     * 無參數建構子（向後相容）
     */
    public SearchResult() {
        this.page = new WebPage("", "");
        this.score = 0.0;
        this.features = new ArrayList<>();
        this.tags = new ArrayList<>();
        this.userRatingsTotal = null;
        this.latitude = null;
        this.longitude = null;
        this.source = "local";
        this.isGoogleResult = false;
    }

    /**
     * 從 Cafe 物件建立 SearchResult
     * @param cafe 咖啡廳物件
     */
    public SearchResult(Cafe cafe) {
        this.page = cafe; // Cafe extends WebPage
        this.score = 0.0;
        this.cafeId = cafe.getId();
        this.phoneNumber = cafe.getPhone();
        this.rating = cafe.getRating();
        this.features = cafe.getFeatures();
        this.tags = cafe.getTags();
        this.userRatingsTotal = cafe.getReviewCount();
    }

    /**
     * 建構子（含 URL 和名稱）
     * @param url URL
     * @param name 名稱
     * @param score 分數
     */
    public SearchResult(String url, String name, double score) {
        this.page = new WebPage(url, name);
        this.score = score;
    }
    
    // ========== 基本 Getters and Setters ==========

    /**
     * 取得網站
     * @return WebPage 物件
     */
    public WebPage getPage() {
        return page;
    }
    
    /**
     * 取得分數
     * @return 分數
     */
    public double getScore() {
        return score;
    }
    
    /**
     * 設定分數
     * @param score 分數
     */
    public void setScore(double score) {
        this.score = score;
    }
    
    // ========== WebPage 的便利方法 ==========

    /**
     * 取得網站名稱（便利方法）
     * @return 名稱
     */
    public void setName(String name) {
        page.setName(name);
    }

    /**
     * 取得網站名稱（便利方法）
     * @return 名稱
     */
    public String getName() {
        return page.getName();
    }

    /**
     * 取得標題（別名）
     * @return 標題
     */
    public String getTitle() {
        return page.getName();
    }
    
    /**
     * 取得網站 URL（便利方法）
     * @return URL
     */
    public String getUrl() {
        return page.getUrl();
    }
    
    /**
     * 取得 Hashtags（便利方法）
     * @return Hashtags 字串
     */
    @JsonIgnore
    public String getHashtags() {
        return page.getHashtags();
    }
    
    /**
     * 取得 Hashtags 列表（用於 JSON 序列化）
     * @return Hashtags 列表
     */
    @JsonProperty("hashtags")
    public List<String> getHashtagList() {
        // 如果有 tags 欄位，優先使用
        if (tags != null && !tags.isEmpty()) {
            return tags;
        }
        
        // 否則解析 hashtags 字串
        String hashtagsStr = getHashtags();
        if (hashtagsStr == null || hashtagsStr.trim().isEmpty()) {
            return new ArrayList<>();
        }
        
        // 移除前後的方括號並分割
        hashtagsStr = hashtagsStr.trim();
        if (hashtagsStr.startsWith("[") && hashtagsStr.endsWith("]")) {
            hashtagsStr = hashtagsStr.substring(1, hashtagsStr.length() - 1);
        }
        
        List<String> result = new ArrayList<>();
        String[] parts = hashtagsStr.split(",");
        for (String part : parts) {
            String cleaned = part.trim().replace("\"", "");
            if (!cleaned.isEmpty()) {
                result.add(cleaned);
            }
        }
        return result;
    }
    
    /**
     * 取得預覽文字（便利方法）
     * @return 預覽文字
     */
    public String getPreview() {
        return page.getPreview();
    }
    
    /**
     * 取得地區（便利方法）
     * @return 地區
     */
    public String getDistrict() {
        return page.getDistrict();
    }

    /**
     * 設定地區
     * @param district 地區
     */
    public void setDistrict(String district) {
        if (this.page != null) {
            this.page.setDistrict(district);
        }
    }

    /**
     * 取得地址（便利方法）
     * @return 地址
     */
    public String getAddress() {
        return page.getAddress();
    }
    
    /**
     * 設定地址
     * @param address 地址
     */
    public void setAddress(String address) {
        if (this.page != null) {
            this.page.setAddress(address);
        }
    }

    // ========== 咖啡廳特有資訊的 Getters and Setters ==========

    /**
     * 取得咖啡廳 ID
     * @return 咖啡廳 ID
     */
    public String getCafeId() {
        return cafeId;
    }

    /**
     * 設定咖啡廳 ID
     * @param cafeId 咖啡廳 ID
     */
    public void setCafeId(String cafeId) {
        this.cafeId = cafeId;
    }

    /**
     * 相容方法：取得 ID（舊名 getId）
     * @return id
     */
    public String getId() {
        return getCafeId();
    }

    /**
     * 相容方法：設定 ID（舊名 setId）
     */
    public void setId(String id) {
        setCafeId(id);
    }

    /**
     * 取得電話號碼
     * @return 電話號碼
     */
    public String getPhoneNumber() {
        return phoneNumber;
    }

    /**
     * 設定電話號碼
     * @param phoneNumber 電話號碼
     */
    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public Integer getUserRatingsTotal() {
        return userRatingsTotal;
    }

    public void setUserRatingsTotal(Integer userRatingsTotal) {
        this.userRatingsTotal = userRatingsTotal;
    }

    public Double getLatitude() {
        if (latitude != null) return latitude;
        return null;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    // ========== Stage 3: Google 搜尋整合欄位 Getters/Setters ==========

    /**
     * 取得搜尋結果來源
     * @return "local" 或 "google"
     */
    public String getSource() {
        return source;
    }

    /**
     * 設定搜尋結果來源
     * @param source "local" 或 "google"
     */
    public void setSource(String source) {
        this.source = source;
    }

    /**
     * 檢查是否為 Google 搜尋結果
     * @return true 如果是 Google 結果
     */
    public boolean isGoogleResult() {
        return isGoogleResult;
    }

    /**
     * 設定是否為 Google 搜尋結果
     * @param googleResult true 如果是 Google 結果
     */
    public void setGoogleResult(boolean googleResult) {
        isGoogleResult = googleResult;
    }

    public String getOpeningHours() {
        return openingHours;
    }

    public void setOpeningHours(String openingHours) {
        this.openingHours = openingHours;
    }

    public void setPreview(String preview) {
        if (page != null) page.setPreview(preview);
    }

    /**
     * 取得評分
     * @return 評分
     */
    public double getRating() {
        return rating;
    }
    
    /**
     * 設定評分
     * @param rating 評分
     */
    public void setRating(double rating) {
        this.rating = rating;
    }

    /**
     * 取得功能列表
     * @return 功能列表
     */
    public List<String> getFeatures() {
        if (features != null) {
            return features;
        }
        return page.getFeatures();
    }

    /**
     * 設定功能列表
     * @param features 功能列表
     */
    public void setFeatures(List<String> features) {
        this.features = features;
    }

    /**
     * 取得標籤列表
     * @return 標籤列表
     */
    public List<String> getTags() {
        if (tags != null) {
            return tags;
        }
        return page.getTags();
    }
    
    /**
     * 設定標籤列表
     * @param tags 標籤列表
     */
    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    /**
     * 設定正規化分數
     * @param normalizedScore 正規化分數
     */
    public void setNormalizedScore(double normalizedScore) {
        // 可以用於儲存 0-100 的正規化分數
        this.score = normalizedScore;
    }
    
    /**
     * 設定推薦分數
     * @param recommendationScore 推薦分數
     */
    public void setRecommendationScore(double recommendationScore) {
        // 用於推薦系統
        this.score = recommendationScore;
    }

    /**
     * 設定推薦分數
     * @param recommendationScore 推薦分數
     */
    public double getRecommendationScore() {
        // 用於推薦系統
        return this.score;
    }

    // ========== JSON 和顯示方法 ==========
    
    /**
     * 轉換為 JSON 格式字串（用於 API 回傳）
     * @return JSON 字串
     */
    public String toJson() {
        StringBuilder json = new StringBuilder("{");
        
        // 基本資訊
        json.append("\"cafeId\": \"").append(escapeJson(cafeId)).append("\", ");
        json.append("\"name\": \"").append(escapeJson(getName())).append("\", ");
        json.append("\"url\": \"").append(escapeJson(getUrl())).append("\", ");
        json.append("\"score\": ").append(String.format("%.2f", score)).append(", ");
        
        // 地理資訊
        json.append("\"district\": \"").append(escapeJson(getDistrict())).append("\", ");
        json.append("\"address\": \"").append(escapeJson(getAddress())).append("\", ");
        
        // 聯絡資訊
        json.append("\"phoneNumber\": \"").append(escapeJson(phoneNumber)).append("\", ");
        json.append("\"openingHours\": \"").append(escapeJson(openingHours)).append("\", ");
        json.append("\"rating\": ").append(rating).append(", ");
        
        // 其他資訊
        json.append("\"hashtags\": \"").append(escapeJson(getHashtags())).append("\", ");
        json.append("\"preview\": \"").append(escapeJson(getPreview())).append("\", ");
        
        // 功能和標籤
        json.append("\"features\": ").append(listToJson(getFeatures())).append(", ");
        json.append("\"tags\": ").append(listToJson(getTags()));
        
        json.append("}");
        
        return json.toString();
    }
    
    /**
     * 將 List 轉換為 JSON 陣列字串
     */
    private String listToJson(List<String> list) {
        if (list == null || list.isEmpty()) {
            return "[]";
        }
        
        StringBuilder json = new StringBuilder("[");
        for (int i = 0; i < list.size(); i++) {
            json.append("\"").append(escapeJson(list.get(i))).append("\"");
            if (i < list.size() - 1) {
                json.append(", ");
            }
        }
        json.append("]");
        
        return json.toString();
    }
    
    /**
     * 轉換為格式化的顯示字串
     * @return 格式化字串
     */
    @Override
    public String toString() {
        return String.format(
            "【%s】\n" +
            "  分數: %.2f | 評分: %.1f★\n" +
            "  地區: %s | 標籤: %s\n" +
            "  地址: %s\n" +
            "  電話: %s | 營業時間: %s\n" +
            "  網址: %s",
            getName(),
            score,
            rating,
            getDistrict(),
            getHashtags(),
            getAddress(),
            phoneNumber != null ? phoneNumber : "未提供",
            openingHours != null ? openingHours : "未提供",
            getUrl()
        );
    }
    
    /**
     * 實作 Comparable 介面（用於排序）
     * 按分數降序排列
     * @param other 另一個 SearchResult
     * @return 比較結果
     */
    @Override
    public int compareTo(SearchResult other) {
        return Double.compare(other.score, this.score);  // 降序
    }
    
    /**
     * equals 方法
     * 基於網站 URL 判斷
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        SearchResult other = (SearchResult) obj;
        return page.equals(other.page);
    }
    
    /**
     * hashCode 方法
     */
    @Override
    public int hashCode() {
        return page.hashCode();
    }
    
    /**
     * 轉義 JSON 特殊字元
     * @param str 原始字串
     * @return 轉義後的字串
     */
    private String escapeJson(String str) {
        if (str == null) return "";
        return str.replace("\\", "\\\\")
                  .replace("\"", "\\\"")
                  .replace("\n", "\\n")
                  .replace("\r", "\\r")
                  .replace("\t", "\\t");
    }
}

