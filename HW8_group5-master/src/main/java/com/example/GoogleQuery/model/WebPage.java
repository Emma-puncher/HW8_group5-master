package com.example.GoogleQuery.model;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.example.GoogleQuery.core.KeywordParser;

/**
 * WebPage - 代表一個網站
 * 包含 URL、名稱、內容、分數等資訊
 * 整合 WordCounter 功能
 */
public class WebPage {
    
    private String url;
    private String name;
    protected WordCounter counter;  // 改為 protected，讓子類可以訪問
    private double score;
    
    // 新增欄位：用於支援新功能
    private String hashtags;              // Top 2-3 關鍵字的 hashtags
    private String preview;               // 預覽文字（前 150-200 字）
    private String district;              // 地區（例：大安區、中山區）
    private String feature;              // 分類（例：不限時、有插座、寵物友善）
    private List<String> featuresList;     // 分類列表
    private String address;               // 地址
    private List<String> tags;            // 標籤列表
    
    /**
     * 建構子
     * @param url 網站 URL
     * @param name 網站名稱
     */
    public WebPage(String url, String name) {
        this.url = url;
        this.name = name;
        this.counter = new WordCounter(url);
        this.score = 0.0;
        this.hashtags = "";
        this.preview = "";
        this.district = "";
        this.feature = "";
        this.address = "";
        this.tags = new ArrayList<>();
    }

    /**
     * 建構子（含網頁內容）
     * @param url 網站 URL
     * @param name 網站名稱
     * @param content 網頁文字內容
     */
    public WebPage(String url, String name, String content) {
        this.url = url;
        this.name = name;
        this.counter = new WordCounter(url);
        this.score = 0.0;
        this.hashtags = "";
        this.preview = content != null && content.length() > 200 
            ? content.substring(0, 200) + "..." 
            : content;
        this.district = "";
        this.feature = "";
        this.address = "";
        this.tags = new ArrayList<>();
    }
    
    /**
     * 建構子（含地區、分類、地址）
     * @param url 網站 URL
     * @param name 網站名稱
     * @param district 地區
     * @param category 分類
     * @param address 地址
     */
    public WebPage(String url, String name, String district, String feature, String address) {
        this(url, name);
        this.district = district;
        this.feature = feature;
        this.address = address;
    }
    
    /**
     * 計算並設定網站分數（基於關鍵字）
     * @param keywords 關鍵字列表
     */
    public void setScore(ArrayList<Keyword> keywords) {
        // 使用 KeywordParser 計算分數
        String content = counter.getContent();
        KeywordParser parser = new KeywordParser(content);
        this.score = parser.calculateWeightedScore(keywords);
    }
    
    /**
     * 直接設定分數（用於外部計算後設定）
     * @param score 分數
     */
    public void setScore(double score) {
        this.score = score;
    }
    
    /**
     * 取得分數
     * @return 分數
     */
    public double getScore() {
        return score;
    }
    
    /**
     * 取得 URL
     * @return URL
     */
    public String getUrl() {
        return url;
    }
    
    /**
     * 取得名稱
     * @return 名稱
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * 取得名稱
     * @return 名稱
     */
    public String getName() {
        return name;
    }
    
    /**
     * 取得 WordCounter
     * @return WordCounter 物件
     */
    public WordCounter getCounter() {
        return counter;
    }
    
    /**
     * 取得網站內容
     * @return 網站文字內容
     */
    public String getContent() {
        return counter.getContent();
    }

    /**
     * 設定網頁內容（測試/手動注入使用）
     * @param content 網頁文字內容
     */
    public void setContent(String content) {
        if (this.counter == null) {
            this.counter = new WordCounter(this.url, content);
        } else {
            this.counter.setContent(content);
        }
        this.preview = content != null && content.length() > 200
            ? content.substring(0, 200) + "..." : content;
    }
    
    /**
     * 從網頁 HTML 中提取最多 20 個對外連結
     * @return 連結列表
     */
    public ArrayList<String> getLinks() {
        ArrayList<String> links = new ArrayList<>();
        
        try {
            Document doc = Jsoup.connect(url)
                .userAgent("Mozilla/5.0")
                .timeout(5000)
                .get();
            
            Elements linkElements = doc.select("a[href]");
            
            for (Element link : linkElements) {
                String href = link.absUrl("href");
                
                // 過濾掉空連結、錨點、JavaScript
                if (!href.isEmpty() && 
                    !href.startsWith("#") && 
                    !href.startsWith("javascript:") &&
                    !href.equals(url)) {  // 排除自己
                    
                    links.add(href);
                    
                    if (links.size() >= 20) {
                        break;
                    }
                }
            }
            
        } catch (IOException e) {
            System.err.println("無法擷取連結: " + url);
        }
        
        return links;
    }
    
    /**
     * 生成 Hashtags（Top 2-3 關鍵字）
     * @param keywords 關鍵字列表
     * @param topN 取前幾名（通常 2-3）
     */
    public void generateHashtags(ArrayList<Keyword> keywords, int topN) {
        String content = counter.getContent();
        KeywordParser parser = new KeywordParser(content);
        this.hashtags = parser.generateHashtags(keywords, topN);
    }
    
    /**
     * 取得 Hashtags
     * @return hashtags 字串（例：#舒芙蕾 #不限時）
     */
    public String getHashtags() {
        return hashtags;
    }
    
    /**
     * 設定 Hashtags
     * @param hashtags hashtags 字串
     */
    public void setHashtags(String hashtags) {
        this.hashtags = hashtags;
    }
    
    /**
     * 生成預覽文字（前 150-200 字）
     * 從網頁內容中提取
     */
    public void generatePreview() {
        String content = counter.getContent();
        if (content.length() <= 200) {
            this.preview = content;
        } else {
            this.preview = content.substring(0, 200) + "...";
        }
    }
    
    /**
     * 設定預覽文字
     * @param preview 預覽文字
     */
    public void setPreview(String preview) {
        this.preview = preview;
    }
    
    /**
     * 取得預覽文字
     * @return 預覽文字
     */
    public String getPreview() {
        return preview;
    }
    
    /**
     * 取得地區
     * @return 地區
     */
    public String getDistrict() {
        return district;
    }
    
    /**
     * 設定地區
     * @param district 地區
     */
    public void setDistrict(String district) {
        this.district = district;
    }
    
    /**
     * 取得分類
     * @return 分類
     */
    public String getFeature() {
        return feature;
    }

    /**
     * 兼容方法：取得分類（舊名 getCategory）
     * @return 分類字串
     */
    public String getCategory() {
        return feature;
    }

    /**
     * 取得分類
     * @return 分類
     */
    public List<String> getFeatures() {
        return featuresList;
    }
    
    /**
     * 設定分類
     * @param features 分類列表
     */
    public void setFeature(String feature) {
        this.feature = feature;
    }
    
    /**
     * 取得地址
     * @return 地址
     */
    public String getAddress() {
        return address;
    }
    
    /**
     * 設定地址
     * @param address 地址
     */
    public void setAddress(String address) {
        this.address = address;
    }
    
    /**
     * 新增標籤
     * @param tag 標籤
     */
    public void addTag(String tag) {
        if (!tags.contains(tag)) {
            tags.add(tag);
        }
    }
    
    /**
     * 取得所有標籤
     * @return 標籤列表
     */
    public List<String> getTags() {
        return new ArrayList<>(tags);
    }
    
    /**
     * 檢查是否符合篩選條件
     * @param filterDistrict 篩選地區（null 表示不篩選）
     * @param filterFeature 篩選功能（null 表示不篩選）
     * @return true 如果符合條件
     */
    public boolean matchesFilter(String filterDistrict, String filterFeature) {
        boolean districtMatch = (filterDistrict == null || filterDistrict.isEmpty() || 
                                 this.district.equals(filterDistrict));
        boolean featureMatch = (filterFeature == null || filterFeature.isEmpty() || 
                                 this.feature.contains(filterFeature));
        return districtMatch && featureMatch;
    }
    
    /**
     * 計算關鍵字出現次數（便利方法）
     * @param keyword 關鍵字
     * @return 出現次數
     */
    public int countKeyword(String keyword) {
        return counter.countKeyword(keyword);
    }
    
    /**
     * 取得完整的網站資訊（用於 Debug 或前端）
     * @return JSON 格式的字串
     */
    public String toJson() {
        return String.format(
            "{\"name\": \"%s\", \"url\": \"%s\", \"score\": %.2f, \"hashtags\": \"%s\", " +
            "\"preview\": \"%s\", \"district\": \"%s\", \"feature\": \"%s\", \"address\": \"%s\"}",
            name, url, score, hashtags, preview, district, feature, address
        );
    }
    
    /**
     * toString 方法
     */
    @Override
    public String toString() {
        return String.format("[%s] %s (分數: %.2f)", name, url, score);
    }
    
    /**
     * equals 方法（基於 URL）
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        WebPage other = (WebPage) obj;
        return url.equals(other.url);
    }
    
    /**
     * hashCode 方法
     */
    @Override
    public int hashCode() {
        return url.hashCode();
    }
}

