package com.example.GoogleQuery.service;

import com.example.GoogleQuery.core.SearchEngine;
import com.example.GoogleQuery.model.Cafe;
import com.example.GoogleQuery.model.SearchResult;
import com.example.GoogleQuery.model.WebPage;
import com.example.GoogleQuery.filter.DistrictFilter;
import com.example.GoogleQuery.filter.FeatureFilter;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;

/**
 * SearchService - 搜尋服務
 * 整合 SearchEngine，提供完整的咖啡廳搜尋功能
 */
@Service
public class SearchService {

    @Autowired
    private KeywordService keywordService;

    @Autowired
    private RankingService rankingService;

    @Autowired
    private GoogleService googleService;

    private SearchEngine searchEngine;
    private List<Cafe> allCafes;
    private Map<String, Cafe> cafeMap; // 以 cafeId 為鍵的咖啡廳映射

    /**
     * 初始化：載入咖啡廳資料和建立 WebPage
     */
    @PostConstruct
    public void init() {
        try {
            // 載入咖啡廳資料
            loadCafesData();
            
            // 建立 WebPage 物件
            // createWebPages();
            
            // 初始化搜尋引擎
            searchEngine = new SearchEngine(keywordService, rankingService);
            
            // ✅ 直接將 Cafe 當作 WebPage 使用（因為 Cafe extends WebPage）
            ArrayList<WebPage> webPages = new ArrayList<>(allCafes);
            for (WebPage page : webPages) {
                searchEngine.addPage(page);
            }

            System.out.println("SearchService 初始化完成：已載入 " + allCafes.size() + " 家咖啡廳");
            
        } catch (Exception e) {
            System.err.println("SearchService 初始化失敗: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 從 JSON 載入咖啡廳資料
     */
    private void loadCafesData() throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        InputStream inputStream = new ClassPathResource("data/cafes.json").getInputStream();
        
        allCafes = mapper.readValue(inputStream, new TypeReference<List<Cafe>>() {});
        
        if (allCafes == null) {
            allCafes = new ArrayList<>();
        }
        
        // ✨ 解析 keywords 並設定功能布林值
        for (Cafe cafe : allCafes) {
            parseKeywordsToFeatures(cafe);
            // 構建可搜尋內容（重要！讓分數計算能正確運作）
            cafe.buildSearchableContent();
        }

        cafeMap = allCafes.stream()
                .collect(Collectors.toMap(Cafe::getId, cafe -> cafe));
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
            List<String> keywordList = new ArrayList<>();
            
            if (keywords.startsWith("[")) {
                // JSON 陣列格式
                ObjectMapper mapper = new ObjectMapper();
                keywordList = mapper.readValue(keywords, new com.fasterxml.jackson.core.type.TypeReference<List<String>>() {});
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

    /**
     * 建立 WebPage 物件
    
    private void createWebPages() {
        cafeWebPages = new HashMap<>();
        
        for (Cafe cafe : allCafes) {
            // 從咖啡廳描述建立 WebPage
            String content = buildCafeContent(cafe);
            WebPage webPage = new WebPage(cafe.getUrl(), cafe.getName(), content);
            cafeWebPages.put(cafe.getId(), webPage);
        }
    }

    /**
     * 建立咖啡廳的搜尋內容（組合所有相關資訊）
     
    private String buildCafeContent(Cafe cafe) {
        StringBuilder content = new StringBuilder();
        
        content.append(cafe.getName()).append(" ");
        content.append(cafe.getDescription()).append(" ");
        content.append(cafe.getDistrict()).append(" ");
        content.append(cafe.getAddress()).append(" ");
        
        // 加入功能特性
        if (cafe.getFeatures() != null) {
            for (String feature : cafe.getFeature()) {
                content.append(feature).append(" ");
            }
        }
        
        // 加入標籤
        if (cafe.getTags() != null) {
            for (String tag : cafe.getTags()) {
                content.append(tag).append(" ");
            }
        }
        
        return content.toString();
    }
    */

    /**
     * 基本搜尋
     * @param keyword 搜尋關鍵字
     * @return 搜尋結果列表
     */
    public ArrayList<SearchResult> search(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return new ArrayList<>();
        }

        try {
            // 使用搜尋引擎進行搜尋
            ArrayList<WebPage> webPages = new ArrayList<>(allCafes);
            ArrayList<SearchResult> results = searchEngine.search(keyword, webPages);
            
            // 補充咖啡廳詳細資訊
            // enrichSearchResults(results);
            
            return results;
            
        } catch (Exception e) {
            System.err.println("搜尋錯誤: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * 進階搜尋（支援地區和功能篩選）
     * @param keyword 搜尋關鍵字（空字串時搜尋全部）
     * @param districts 地區列表
     * @param features 功能列表
     * @return 搜尋結果列表
     */
    public ArrayList<SearchResult> advancedSearch(
            String keyword,
            List<String> districts,
            List<String> features) {

        ArrayList<SearchResult> results;
        
        // 如果關鍵字為空且有篩選條件，則先取得所有咖啡廳
        if ((keyword == null || keyword.trim().isEmpty()) && 
            ((districts != null && !districts.isEmpty()) || 
             (features != null && !features.isEmpty()))) {
            // 將所有咖啡廳轉換為 SearchResult
            results = new ArrayList<>();
            for (Cafe cafe : allCafes) {
                SearchResult result = new SearchResult(cafe);
                results.add(result);
            }
        } else {
            // 有關鍵字時使用正常搜尋
            results = search(keyword);
        }

        // 應用地區篩選
        if (districts != null && !districts.isEmpty()) {
            DistrictFilter districtFilter = new DistrictFilter(districts);
            results = districtFilter.filter(results);
        }

        // 應用功能篩選
        if (features != null && !features.isEmpty()) {
            FeatureFilter featureFilter = new FeatureFilter(features);
            results = featureFilter.filter(results);
        }

        return results;
    }

    /**
     * 進階搜尋（支援地區和功能篩選）
     * @param keyword 搜尋關鍵字
     * @param districts 地區列表
     * @param features 功能列表
     * @return 搜尋結果列表
    
    public ArrayList<SearchResult> advancedSearch(
            String keyword,
            List<String> districts,
            List<String> features) {
        
        // 先執行基本搜尋
        ArrayList<SearchResult> results = search(keyword);
        
        // 應用地區篩選
        if (districts != null && !districts.isEmpty()) {
            DistrictFilter districtFilter = new DistrictFilter(districts);
            results = districtFilter.filter(results);
        }
        
        // 應用功能篩選
        if (features != null && !features.isEmpty()) {
            FeatureFilter featureFilter = new FeatureFilter(features);
            results = featureFilter.filter(results);
        }
        
        return results;
    }

    /**
     * 補充搜尋結果的咖啡廳詳細資訊
    
    private void enrichSearchResults(ArrayList<SearchResult> results) {
        for (SearchResult result : results) {
            Cafe cafe = findCafeByUrl(result.getUrl());
            if (cafe != null) {
                result.setCafeId(cafe.getId());
                result.setDistrict(cafe.getDistrict());
                result.setAddress(cafe.getAddress());
                result.setFeatures(cafe.getFeature());
                result.setTags(cafe.getTags());
                result.setRating(cafe.getRating());
                result.setPhoneNumber(cafe.getPhone());
            } else {
                System.err.println("找不到對應的咖啡廳，URL: " + result.getUrl());
            }
        }
    }
    */

    /**
     * 根據 URL 找咖啡廳
     */
    private Cafe findCafeByUrl(String url) {
        return allCafes.stream()
                .filter(cafe -> cafe.getUrl().equals(url))
                .findFirst()
                .orElse(null);
    }

    /**
     * 根據 ID 獲取咖啡廳詳細資訊
     * @param cafeId 咖啡廳 ID
     * @return SearchResult 物件
     */
    public SearchResult getCafeById(String cafeId) {
        Cafe cafe = cafeMap.get(cafeId);
        
        if (cafe == null) {
            return null;
        }
        
        return new SearchResult(cafe, 0.0);
    }

    /**
     * 獲取所有咖啡廳資料
     * @return 所有咖啡廳列表
     */
    public List<Cafe> getAllCafes() {
        return new ArrayList<>(cafeMap.values());
    }

    /**
     * 獲取咖啡廳總數
     * @return 咖啡廳數量
     */
    public int getCafeCount() {
        return cafeMap.size();
    }

    /**
     * 依地區搜尋咖啡廳
     * @param district 地區名稱
     * @return 該地區的咖啡廳列表
     */
    public ArrayList<SearchResult> searchByDistrict(String district) {
        ArrayList<SearchResult> results = new ArrayList<>();
        
        for (Cafe cafe : cafeMap.values()) {
            // 比對地區（忽略大小寫和空白）
            if (cafe.getDistrict() != null && 
                cafe.getDistrict().trim().equalsIgnoreCase(district.trim())) {
                
                SearchResult result = convertToSearchResult(cafe);
                results.add(result);
            }
        }
        
        return results;
    }

    /**
     * 將 Cafe 轉換為 SearchResult
     * @param cafe 咖啡廳物件
     * @return SearchResult 物件
     */
    private SearchResult convertToSearchResult(Cafe cafe) {
        SearchResult result = new SearchResult();
        result.setCafeId(cafe.getId());
        result.setName(cafe.getName());
        result.setDistrict(cafe.getDistrict());
        result.setAddress(cafe.getAddress());
        result.setRating(cafe.getRating());
        result.setUserRatingsTotal(cafe.getUserRatingsTotal());
        // ... 設定其他欄位
        return result;
    }

    /**
     * 獲取所有不重複的行政區列表
     * @return 行政區列表（已排序）
     */
    public List<String> getAllDistricts() {
        Set<String> districts = new HashSet<>();
        
        for (Cafe cafe : cafeMap.values()) {
            if (cafe.getDistrict() != null && !cafe.getDistrict().trim().isEmpty()) {
                districts.add(cafe.getDistrict().trim());
            }
        }
        
        // 轉為列表並排序
        List<String> sortedDistricts = new ArrayList<>(districts);
        Collections.sort(sortedDistricts);
        
        return sortedDistricts;
    }

    public ArrayList<SearchResult> searchByFeature(String feature) {
        ArrayList<SearchResult> results = new ArrayList<>();
        for (Cafe cafe : allCafes) {
            if (cafe.getFeatures() != null && cafe.getFeatures().contains(feature)) {
                results.add(convertToSearchResult(cafe));
            }
        }

        results.sort((a, b) -> Double.compare(b.getScore(), a.getScore()));
        return results;
    }

    public List<String> getAllFeatures() {
        Set<String> featuresSet = new HashSet<>();
        for (Cafe cafe : allCafes) {
            if (cafe.getFeatures() != null) featuresSet.addAll(cafe.getFeatures());
        }
        return new ArrayList<>(featuresSet);
    }

    public List<String> getSearchSuggestions(String query) {
        if (query == null || query.length() < 2) return new ArrayList<>();
        List<String> allKeywords = keywordService.getAllKeywordsName();
        return allKeywords.stream()
                .filter(k -> k.contains(query))
                .limit(10)
                .collect(Collectors.toList());
    }

    /**
     * 將 Cafe 轉換為 SearchResult
    
    private SearchResult convertCafeToSearchResult(Cafe cafe) {
        WebPage webPage = cafeWebPages.get(cafe.getId());
        
        SearchResult result = new SearchResult(
            cafe.getUrl(),
            cafe.getName(),
            0.0 // 預設分數
        );
        
        result.setCafeId(cafe.getId());
        result.setDistrict(cafe.getDistrict());
        result.setAddress(cafe.getAddress());
        result.setFeatures(cafe.getFeature());
        result.setTags(cafe.getTags());
        result.setRating(cafe.getRating());
        result.setPhoneNumber(cafe.getPhone());
        
        if (webPage != null) {
            result.setPreview(googleService.getContentPreview(webPage, 200));
        }
        
        return result;
    }

    /**
     * 獲取搜尋建議（自動完成）
     * @param query 查詢字串
     * @return 建議關鍵字列表
    
    public List<String> getSearchSuggestions(String query) {
        if (query == null || query.length() < 2) {
            return new ArrayList<>();
        }
        
        // 從所有關鍵字中找出匹配的
        List<String> allKeywords = keywordService.getAllKeywordsName();
        
        return allKeywords.stream()
                .filter(keyword -> keyword.contains(query))
                .limit(10)
                .collect(Collectors.toList());
    }

    /**
     * 依地區搜尋
     * @param district 地區名稱
     * @return 該地區的所有咖啡廳
    
    public ArrayList<SearchResult> searchByDistrict(String district) {
        List<Cafe> cafesInDistrict = allCafes.stream()
                .filter(cafe -> cafe.getDistrict().equals(district))
                .collect(Collectors.toList());
        
        ArrayList<SearchResult> results = new ArrayList<>();
        for (Cafe cafe : cafesInDistrict) {
            results.add(convertCafeToSearchResult(cafe));
        }
        
        // 依照 baseline score 排序
        results.sort((a, b) -> Double.compare(b.getScore(), a.getScore()));
        
        return results;
    }

    /**
     * 依功能特性搜尋
     * @param feature 功能特性
     * @return 符合該特性的咖啡廳
    
    public ArrayList<SearchResult> searchByFeature(String feature) {
        List<Cafe> cafesWithFeature = allCafes.stream()
                .filter(cafe -> cafe.getFeatures() != null && 
                               cafe.getFeatures().contains(feature))
                .collect(Collectors.toList());
        
        ArrayList<SearchResult> results = new ArrayList<>();
        for (Cafe cafe : cafesWithFeature) {
            results.add(convertCafeToSearchResult(cafe));
        }
        
        // 依照 baseline score 排序
        results.sort((a, b) -> Double.compare(b.getScore(), a.getScore()));
        
        return results;
    }

    /**
     * 獲取所有咖啡廳
     * @return 所有咖啡廳列表
    
    public List<Cafe> getAllCafes() {
        return new ArrayList<>(allCafes);
    }

    /**
     * 獲取所有地區
     * @return 地區列表
    
    public List<String> getAllDistricts() {
        return allCafes.stream()
                .map(Cafe::getDistrict)
                .distinct()
                .sorted()
                .collect(Collectors.toList());
    }

    /**
     * 獲取所有功能特性
     * @return 功能特性列表
    
    public List<String> getAllFeatures() {
        Set<String> features = new HashSet<>();
        
        for (Cafe cafe : allCafes) {
            if (cafe.getFeatures() != null) {
                features.addAll(cafe.getFeatures());
            }
        }
        
        return new ArrayList<>(features);
    }

    /**
     * 搜尋統計資訊
     * @param keyword 搜尋關鍵字
     * @return 統計資訊
    
    public Map<String, Object> getSearchStatistics(String keyword) {
        Map<String, Object> stats = new HashMap<>();
        
        ArrayList<SearchResult> results = search(keyword);
        
        stats.put("totalResults", results.size());
        stats.put("keyword", keyword);
        stats.put("timestamp", System.currentTimeMillis());
        
        // 統計各地區的結果數量
        Map<String, Long> districtCounts = results.stream()
                .collect(Collectors.groupingBy(
                    SearchResult::getDistrict,
                    Collectors.counting()
                ));
        stats.put("districtDistribution", districtCounts);
        
        // 平均分數
        double avgScore = results.stream()
                .mapToDouble(SearchResult::getScore)
                .average()
                .orElse(0.0);
        stats.put("averageScore", avgScore);
        
        return stats;
    }

    /**
     * 重新載入資料
    
    public void reloadData() {
        try {
            loadCafesData();
            createWebPages();
            System.out.println("資料重新載入完成");
        } catch (IOException e) {
            System.err.println("資料重新載入失敗: " + e.getMessage());
        }
    }

    /**
     * 檢查服務狀態
    
    public Map<String, Object> getServiceStatus() {
        Map<String, Object> status = new HashMap<>();
        status.put("service", "SearchService");
        status.put("status", "running");
        status.put("totalCafes", allCafes.size());
        status.put("totalWebPages", cafeWebPages.size());
        status.put("timestamp", System.currentTimeMillis());
        
        return status;
    }
    */
}


