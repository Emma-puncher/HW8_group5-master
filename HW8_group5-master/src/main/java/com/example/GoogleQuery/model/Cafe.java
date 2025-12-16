package com.example.GoogleQuery.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Cafe - 咖啡廳資料模型
 * 擴充版的 WebPage，包含咖啡廳特有的屬性
 */
public class Cafe extends WebPage {
    
    private String id;                    // 咖啡廳唯一 ID（例：cafe_001）
    private String googleMapUrl;          // Google Maps 連結
    private String phone;                 // 電話
    private String openingHours;          // 營業時間
    private double rating;                // 評分（1-5 星）
    private int reviewCount;              // 評論數量
    
    // 功能標籤（boolean 型態，方便篩選）
    private boolean noTimeLimit;          // 是否不限時
    private boolean hasSocket;            // 是否有插座
    private boolean hasWifi;              // 是否有 wifi
    private boolean highCP;               // 是否 CP 值高
    private boolean petFriendly;          // 是否寵物友善
    private boolean hasOutdoorSeating;    // 是否有戶外座位
    private boolean quiet;                // 是否安靜
    private boolean goodLighting;         // 是否燈光充足
    
    // 額外資訊
    private String description;           // 咖啡廳描述
    private List<String> images;          // 圖片 URL 列表
    private double baselineScore;         // 基準分數（用於熱門推薦）
    private String keywords;

    /**
     * 建構子
     * @param id 咖啡廳 ID
     * @param name 咖啡廳名稱
     * @param url 網站 URL
     * @param district 地區
     * @param address 地址
     */

    public Cafe() {
        super("", "", "", "", "");
        this.images = new ArrayList<>();
    }

    public Cafe(String id, String name, String url, String district, String address) {
        super(url, name, district, "", address);
        this.id = id;
        this.googleMapUrl = generateGoogleMapUrl(address);
        this.images = new ArrayList<>();
        this.rating = 0.0;
        this.reviewCount = 0;
        this.baselineScore = 0.0;
        
        // 預設所有功能為 false
        this.noTimeLimit = false;
        this.hasSocket = false;
        this.hasWifi = false;
        this.highCP = false;
        this.petFriendly = false;
        this.hasOutdoorSeating = false;
        this.quiet = false;
        this.goodLighting = false;
    }
    
    /**
     * 完整建構子
     */
    public Cafe(String id, String name, String url, String district, String address,
                boolean noTimeLimit, boolean hasSocket, boolean hasWifi, boolean highCP) {
        this(id, name, url, district, address);
        this.noTimeLimit = noTimeLimit;
        this.hasSocket = hasSocket;
        this.hasWifi = hasWifi;
        this.highCP = highCP;
    }
    
    /**
     * 根據地址生成 Google Maps URL
     * @param address 地址
     * @return Google Maps URL
     */
    private String generateGoogleMapUrl(String address) {
        if (address == null || address.isEmpty()) {
            return "";
        }
        // URL encode 地址
        String encodedAddress = address.replace(" ", "+");
        return "https://www.google.com/maps/search/?api=1&query=" + encodedAddress;
    }
    
    // ========== Getters and Setters ==========
    
    public String getKeywords() {
        return keywords;
    }

public void setKeywords(String keywords) {
    this.keywords = keywords;
}

    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public String getGoogleMapUrl() {
        return googleMapUrl;
    }
    
    public void setGoogleMapUrl(String googleMapUrl) {
        this.googleMapUrl = googleMapUrl;
    }
    
    public String getPhone() {
        return phone;
    }
    
    public void setPhone(String phone) {
        this.phone = phone;
    }
    
    public String getOpeningHours() {
        return openingHours;
    }
    
    public void setOpeningHours(String openingHours) {
        this.openingHours = openingHours;
    }
    
    public double getRating() {
        return rating;
    }
    
    public void setRating(double rating) {
        this.rating = Math.max(0.0, Math.min(5.0, rating)); // 限制在 0-5 之間
    }
    
    public int getReviewCount() {
        return reviewCount;
    }

    /**
     * 向後相容：回傳使用者評分總數
     * @return 使用者評分總數
     */
    public int getUserRatingsTotal() {
        return reviewCount;
    }
    
    public void setReviewCount(int reviewCount) {
        this.reviewCount = Math.max(0, reviewCount);
    }
    
    public boolean isNoTimeLimit() {
        return noTimeLimit;
    }
    
    public void setNoTimeLimit(boolean noTimeLimit) {
        this.noTimeLimit = noTimeLimit;
    }
    
    public boolean isHasSocket() {
        return hasSocket;
    }
    
    public void setHasSocket(boolean hasSocket) {
        this.hasSocket = hasSocket;
    }
    
    public boolean isHasWifi() {
        return hasWifi;
    }
    
    public void setHasWifi(boolean hasWifi) {
        this.hasWifi = hasWifi;
    }
    
    public boolean isHighCP() {
        return highCP;
    }
    
    public void setHighCP(boolean highCP) {
        this.highCP = highCP;
    }
    
    public boolean isPetFriendly() {
        return petFriendly;
    }
    
    public void setPetFriendly(boolean petFriendly) {
        this.petFriendly = petFriendly;
    }
    
    public boolean isHasOutdoorSeating() {
        return hasOutdoorSeating;
    }
    
    public void setHasOutdoorSeating(boolean hasOutdoorSeating) {
        this.hasOutdoorSeating = hasOutdoorSeating;
    }
    
    public boolean isQuiet() {
        return quiet;
    }
    
    public void setQuiet(boolean quiet) {
        this.quiet = quiet;
    }
    
    public boolean isGoodLighting() {
        return goodLighting;
    }
    
    public void setGoodLighting(boolean goodLighting) {
        this.goodLighting = goodLighting;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public List<String> getImages() {
        return new ArrayList<>(images);
    }
    
    public void addImage(String imageUrl) {
        if (imageUrl != null && !imageUrl.isEmpty()) {
            this.images.add(imageUrl);
        }
    }
    
    public void setImages(List<String> images) {
        this.images = images != null ? new ArrayList<>(images) : new ArrayList<>();
    }
    
    public double getBaselineScore() {
        return baselineScore;
    }
    
    public void setBaselineScore(double baselineScore) {
        this.baselineScore = baselineScore;
    }
    
    // ========== 便利方法 ==========
    
    /**
     * 取得所有功能標籤（用於顯示）
     * @return 功能標籤列表
     */
    public List<String> getFeatureTags() {
        List<String> tags = new ArrayList<>();
        
        if (noTimeLimit) tags.add("不限時");
        if (hasSocket) tags.add("有插座");
        if (hasWifi) tags.add("有wifi");
        if (highCP) tags.add("CP值高");
        if (petFriendly) tags.add("寵物友善");
        if (hasOutdoorSeating) tags.add("戶外座位");
        if (quiet) tags.add("安靜");
        if (goodLighting) tags.add("燈光充足");
        
        return tags;
    }

    /**
     * 取得所有功能標籤（別名方法，用於相容性）
     * @return 功能標籤列表
     */
    public List<String> getFeatures() {
        return getFeatureTags();
    }
    
    /**
     * 檢查是否符合指定功能
     * @param feature 功能名稱
     * @return true 如果符合 
     */
    public boolean hasFeature(String feature) {
        if (feature == null) return false;
        
        switch (feature) {
            case "不限時": return noTimeLimit;
            case "有插座": return hasSocket;
            case "有wifi": return hasWifi;
            case "CP值高": return highCP;
            case "寵物友善": return petFriendly;
            case "戶外座位": return hasOutdoorSeating;
            case "安靜": return quiet;
            case "燈光充足": return goodLighting;
            default: return false;
        }
    }
    
    /**
     * 設定功能標籤
     * @param feature 功能名稱
     * @param value true/false
     */
    public void setFeature(String feature, boolean value) {
        if (feature == null) return;
        
        switch (feature) {
            case "不限時": noTimeLimit = value; break;
            case "有插座": hasSocket = value; break;
            case "有wifi": hasWifi = value; break;
            case "CP值高": highCP = value; break;
            case "寵物友善": petFriendly = value; break;
            case "戶外座位": hasOutdoorSeating = value; break;
            case "安靜": quiet = value; break;
            case "燈光充足": goodLighting = value; break;
        }
    }
    
    /**
     * 計算功能匹配度（用於比較）
     * @param requiredFeatures 需要的功能列表
     * @return 匹配度（0-1）
     */
    public double calculateFeatureMatch(List<String> requiredFeatures) {
        if (requiredFeatures == null || requiredFeatures.isEmpty()) {
            return 1.0;
        }
        
        int matchCount = 0;
        for (String feature : requiredFeatures) {
            if (hasFeature(feature)) {
                matchCount++;
            }
        }
        
        return (double) matchCount / requiredFeatures.size();
    }
    
    /**
     * 轉換為 JSON 格式（用於 API 回傳）
     */
    @Override
    public String toJson() {
        return String.format(
            "{" +
            "\"id\": \"%s\", " +
            "\"name\": \"%s\", " +
            "\"url\": \"%s\", " +
            "\"district\": \"%s\", " +
            "\"address\": \"%s\", " +
            "\"googleMapUrl\": \"%s\", " +
            "\"phone\": \"%s\", " +
            "\"openingHours\": \"%s\", " +
            "\"rating\": %.1f, " +
            "\"reviewCount\": %d, " +
            "\"score\": %.2f, " +
            "\"baselineScore\": %.2f, " +
            "\"hashtags\": \"%s\", " +
            "\"preview\": \"%s\", " +
            "\"features\": {" +
                "\"noTimeLimit\": %b, " +
                "\"hasSocket\": %b, " +
                "\"hasWifi\": %b, " +
                "\"highCP\": %b, " +
                "\"petFriendly\": %b, " +
                "\"hasOutdoorSeating\": %b, " +
                "\"quiet\": %b, " +
                "\"goodLighting\": %b" +
            "}, " +
            "\"description\": \"%s\"" +
            "}",
            id, getName(), getUrl(), getDistrict(), getAddress(),
            googleMapUrl, phone, openingHours, rating, reviewCount,
            getScore(), baselineScore, getHashtags(), getPreview(),
            noTimeLimit, hasSocket, hasWifi, highCP, 
            petFriendly, hasOutdoorSeating, quiet, goodLighting,
            description != null ? description : ""
        );
    }
    
    /**
     * toString 方法
     */
    @Override
    public String toString() {
        return String.format(
            "Cafe{id='%s', name='%s', district='%s', rating=%.1f, features=%s}",
            id, getName(), getDistrict(), rating, getFeatureTags()
        );
    }
    
    /**
     * 構建咖啡廳的可搜尋內容
     * 將所有相關資訊組合成一個字串，供關鍵字搜尋使用
     */
    public void buildSearchableContent() {
        StringBuilder content = new StringBuilder();
        
        // 加入名稱
        content.append(getName()).append(" ");
        
        // 加入描述
        if (description != null && !description.isEmpty()) {
            content.append(description).append(" ");
        }
        
        // 加入地區和地址
        content.append(getDistrict()).append(" ");
        content.append(getAddress()).append(" ");
        
        // 加入功能特性（轉換為文字）
        List<String> features = getFeatureTags();
        for (String feature : features) {
            content.append(feature).append(" ");
        }
        
        // 加入標籤
        if (getTags() != null) {
            for (String tag : getTags()) {
                content.append(tag).append(" ");
            }
        }
        
        // 更新 WordCounter 的內容
        this.counter = new WordCounter(getUrl(), content.toString());
    }
}

