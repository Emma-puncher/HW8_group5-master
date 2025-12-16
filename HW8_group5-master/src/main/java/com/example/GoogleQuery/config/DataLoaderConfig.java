package com.example.GoogleQuery.config;

import com.example.GoogleQuery.model.*;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * DataLoaderConfig - 資料載入配置
 * 在應用程式啟動時載入 JSON 資料（咖啡廳、關鍵字等）
 */
@Configuration
public class DataLoaderConfig {
    
    private final Gson gson = new Gson();
    
    /**
     * 載入咖啡廳資料
     * @return 咖啡廳列表
     */
    @Bean(name = "cafeList")
    public ArrayList<Cafe> loadCafes() {
        try {
            System.out.println("正在載入咖啡廳資料...");
            
            ClassPathResource resource = new ClassPathResource("data/cafes.json");
            Reader reader = new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8);
            
            // 直接解析為 Cafe 陣列（前端提供的是陣列格式）
            Type cafeListType = new TypeToken<ArrayList<Cafe>>(){}.getType();
            ArrayList<Cafe> cafes = gson.fromJson(reader, cafeListType);
            
            reader.close();
            
            if (cafes != null && !cafes.isEmpty()) {
                // 解析 keywords 並設定對應的功能布林值
                for (Cafe cafe : cafes) {
                    parseKeywordsToFeatures(cafe);
                }
                System.out.println("成功載入 " + cafes.size() + " 家咖啡廳");
                return cafes;
            } else {
                System.out.println("警告：咖啡廳資料為空，使用預設資料");
                return createDefaultCafes();
            }
            
        } catch (Exception e) {
            System.err.println("無法載入咖啡廳資料: " + e.getMessage());
            e.printStackTrace();
            System.out.println("使用預設咖啡廳資料");
            return createDefaultCafes();
        }
    }
    
    /**
     * 載入關鍵字資料
     * @return 關鍵字列表
     */
    @Bean(name = "keywordList")
    public ArrayList<Keyword> loadKeywords() {
        try {
            System.out.println("正在載入關鍵字資料...");
            
            ClassPathResource resource = new ClassPathResource("data/keywords.json");
            Reader reader = new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8);
            
            // 解析新的 JSON 格式（包含 meta_info 和 keywords 陣列）
            Type keywordListType = new TypeToken<KeywordDataWrapper>(){}.getType();
            KeywordDataWrapper wrapper = gson.fromJson(reader, keywordListType);
            
            reader.close();
            
            ArrayList<Keyword> allKeywords = new ArrayList<>();
            
            if (wrapper != null && wrapper.keywords != null) {
                // 根據 category 分類處理關鍵字
                for (KeywordItem item : wrapper.keywords) {
                    KeywordTier tier;
                    switch (item.category.toLowerCase()) {
                        case "core":
                            tier = KeywordTier.CORE;
                            break;
                        case "secondary":
                            tier = KeywordTier.SECONDARY;
                            break;
                        case "reference":
                            tier = KeywordTier.REFERENCE;
                            break;
                        default:
                            tier = KeywordTier.REFERENCE;
                    }
                    
                    Keyword keyword = new Keyword(item.term, item.weight);
                    keyword.setTier(tier);
                    allKeywords.add(keyword);
                }
                
                System.out.println("成功載入 " + allKeywords.size() + " 個關鍵字");
                return allKeywords;
            } else {
                System.out.println("警告：關鍵字資料為空，使用預設資料");
                return createDefaultKeywords();
            }
            
        } catch (Exception e) {
            System.err.println("無法載入關鍵字資料: " + e.getMessage());
            e.printStackTrace();
            System.out.println("使用預設關鍵字資料");
            return createDefaultKeywords();
        }
    }
    
    /**
     * 載入基準分數資料
     * @return 基準分數 Map
     */
    @Bean(name = "baselineScores")
    public Map<String, Double> loadBaselineScores() {
        try {
            System.out.println("正在載入基準分數...");
            
            ClassPathResource resource = new ClassPathResource("data/baseline-scores.json");
            Reader reader = new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8);
            
            Type mapType = new TypeToken<Map<String, Double>>(){}.getType();
            Map<String, Double> scores = gson.fromJson(reader, mapType);
            
            reader.close();
            
            if (scores != null) {
                System.out.println("成功載入 " + scores.size() + " 個基準分數");
                return scores;
            } else {
                System.out.println("警告：基準分數資料為空");
                return new java.util.HashMap<>();
            }
            
        } catch (IOException e) {
            System.err.println("無法載入基準分數: " + e.getMessage());
            return new java.util.HashMap<>();
        }
    }
    
    /**
     * 建立預設咖啡廳資料（當 JSON 檔案不存在時使用）
     */
    private ArrayList<Cafe> createDefaultCafes() {
        ArrayList<Cafe> cafes = new ArrayList<>();
        
        // 範例咖啡廳 1
        Cafe cafe1 = new Cafe(
            "cafe_001",
            "讀字書店咖啡廳",
            "https://example.com/cafe1",
            "大安區",
            "台北市大安區羅斯福路三段269巷16號"
        );
        cafe1.setNoTimeLimit(true);
        cafe1.setHasSocket(true);
        cafe1.setHasWifi(true);
        cafe1.setQuiet(true);
        cafe1.setDescription("結合書店與咖啡廳，提供舒適的閱讀空間");
        cafe1.setRating(4.5);
        cafe1.setReviewCount(120);
        cafes.add(cafe1);
        
        // 範例咖啡廳 2
        Cafe cafe2 = new Cafe(
            "cafe_002",
            "工業風咖啡",
            "https://example.com/cafe2",
            "中山區",
            "台北市中山區南京東路二段123號"
        );
        cafe2.setHasSocket(true);
        cafe2.setHasWifi(true);
        cafe2.setDescription("工業風格裝潢，適合工作");
        cafe2.setRating(4.2);
        cafe2.setReviewCount(85);
        cafes.add(cafe2);
        
        return cafes;
    }
    
    /**
     * 建立預設關鍵字資料
     */
    private ArrayList<Keyword> createDefaultKeywords() {
        ArrayList<Keyword> keywords = new ArrayList<>();
        
        // 核心詞
        keywords.add(new Keyword("不限時", 3.0));
        keywords.add(new Keyword("安靜", 2.8));
        keywords.add(new Keyword("插座", 2.7));
        keywords.add(new Keyword("wifi", 2.6));
        keywords.add(new Keyword("適合讀書", 2.9));
        
        // 次要詞
        keywords.add(new Keyword("咖啡", 1.8));
        keywords.add(new Keyword("舒適", 1.6));
        keywords.add(new Keyword("文青", 1.5));
        
        // 參考詞
        keywords.add(new Keyword("甜點", 0.8));
        keywords.add(new Keyword("早午餐", 0.7));
        
        return keywords;
    }
    
    // ========== 內部類別（用於 JSON 解析） ==========
    
    /**
     * 咖啡廳資料包裝類別
     */
    private static class CafeDataWrapper {
        List<Cafe> cafes;
    }
    
    /**
     * 關鍵字資料包裝類別（適配新的 JSON 格式）
     */
    private static class KeywordDataWrapper {
        List<KeywordItem> keywords;
    }
    
    /**
     * 關鍵字項目類別
     */
    private static class KeywordItem {
        String term;
        String category;
        double weight;
        String note;
    }
    
    /**
     * 解析 keywords 字符串並設定對應的功能布林值
     * @param cafe 咖啡廳物件
     */
    private void parseKeywordsToFeatures(Cafe cafe) {
        if (cafe == null) return;
        
        String keywords = cafe.getKeywords();
        if (keywords == null || keywords.isEmpty()) return;
        
        try {
            // keywords 可能是 JSON 陣列字符串: "[\"不限時\",\"有插座\"]"
            // 或逗號分隔的字符串: "不限時,有插座"
            List<String> keywordList = new ArrayList<>();
            
            if (keywords.startsWith("[")) {
                // JSON 陣列格式
                Type listType = new TypeToken<List<String>>(){}.getType();
                keywordList = gson.fromJson(keywords, listType);
            } else {
                // 逗號分隔格式
                String[] parts = keywords.split(",");
                for (String part : parts) {
                    keywordList.add(part.trim());
                }
            }
            
            // 根據關鍵字設定對應的布林值
            for (String keyword : keywordList) {
                switch (keyword.trim()) {
                    case "不限時":
                        cafe.setNoTimeLimit(true);
                        break;
                    case "有插座":
                        cafe.setHasSocket(true);
                        break;
                    case "有wifi":
                    case "wifi":
                        cafe.setHasWifi(true);
                        break;
                    case "安靜":
                        cafe.setQuiet(true);
                        break;
                    case "CP值高":
                        cafe.setHighCP(true);
                        break;
                    case "寵物友善":
                        cafe.setPetFriendly(true);
                        break;
                    case "戶外座位":
                        cafe.setHasOutdoorSeating(true);
                        break;
                    case "燈光充足":
                        cafe.setGoodLighting(true);
                        break;
                }
            }
        } catch (Exception e) {
            System.err.println("解析咖啡廳 " + cafe.getName() + " 的 keywords 失敗: " + e.getMessage());
        }
    }
}

