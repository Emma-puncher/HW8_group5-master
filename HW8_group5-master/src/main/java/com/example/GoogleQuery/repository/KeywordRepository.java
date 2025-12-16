package com.example.GoogleQuery.repository;

import com.example.GoogleQuery.model.Keyword;
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
 * KeywordRepository - 關鍵字資料存取
 * 負責從 JSON 檔案載入和管理關鍵字資料
 */
@Repository
public class KeywordRepository {

    private Map<String, Keyword> keywordMap; // 以關鍵字名稱為 key
    private Map<Integer, List<Keyword>> tierMap; // 以層級為 key
    
    private static final String DATA_FILE_PATH = "data/keywords.json";

    /**
     * 初始化：載入關鍵字資料
     */
    @PostConstruct
    public void init() {
        try {
            loadKeywordsFromFile();
            System.out.println("KeywordRepository 初始化完成：已載入 " + keywordMap.size() + " 個關鍵字");
        } catch (IOException e) {
            System.err.println("載入關鍵字資料失敗: " + e.getMessage());
            // 使用預設資料初始化
            initializeDefaultKeywords();
        }
    }

    /**
     * 從 JSON 檔案載入關鍵字資料
     */
    private void loadKeywordsFromFile() throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        InputStream inputStream = new ClassPathResource(DATA_FILE_PATH).getInputStream();
        
        Map<String, Object> data = mapper.readValue(inputStream, 
            new TypeReference<Map<String, Object>>() {});
        
        keywordMap = new HashMap<>();
        tierMap = new HashMap<>();
        
        // 檢查是否為新格式 (包含 "keywords" 陣列)
        if (data.containsKey("keywords")) {
            loadKeywordsFromNewFormat(data);
        } else {
            // 舊格式：tier1, tier2, tier3
            loadTierKeywords(data, "tier1", 1);
            loadTierKeywords(data, "tier2", 2);
            loadTierKeywords(data, "tier3", 3);
        }
    }
    
    /**
     * 從新格式載入關鍵字
     */
    private void loadKeywordsFromNewFormat(Map<String, Object> data) {
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> keywords = (List<Map<String, Object>>) data.get("keywords");
        
        // 根據 category 分組
        Map<String, Integer> categoryToTier = new HashMap<>();
        categoryToTier.put("core", 1);
        categoryToTier.put("secondary", 2);
        categoryToTier.put("additional", 3);
        
        for (Map<String, Object> keywordData : keywords) {
            String term = (String) keywordData.get("term");
            String category = (String) keywordData.get("category");
            Double weight = ((Number) keywordData.get("weight")).doubleValue();
            
            int tier = categoryToTier.getOrDefault(category, 3);
            
            Keyword keyword = new Keyword(term, weight, tier);
            keywordMap.put(term, keyword);
            
            // 加入到 tierMap
            tierMap.computeIfAbsent(tier, k -> new ArrayList<>()).add(keyword);
        }
    }

    /**
     * 載入特定層級的關鍵字
     */
    private void loadTierKeywords(Map<String, Object> data, String tierKey, int tier) {
        @SuppressWarnings("unchecked")
        Map<String, Object> tierData = (Map<String, Object>) data.get(tierKey);
        
        if (tierData != null) {
            @SuppressWarnings("unchecked")
            List<String> keywords = (List<String>) tierData.get("keywords");
            Double weight = ((Number) tierData.get("weight")).doubleValue();
            
            List<Keyword> tierKeywords = new ArrayList<>();
            
            for (String keywordName : keywords) {
                Keyword keyword = new Keyword(keywordName, weight, tier);
                keywordMap.put(keywordName, keyword);
                tierKeywords.add(keyword);
            }
            
            tierMap.put(tier, tierKeywords);
        }
    }

    /**
     * 初始化預設關鍵字（當無法載入檔案時使用）
     */
    private void initializeDefaultKeywords() {
        keywordMap = new HashMap<>();
        tierMap = new HashMap<>();
        
        // Tier 1: 核心詞
        List<Keyword> tier1 = Arrays.asList(
            new Keyword("讀書", 2.5, 1),
            new Keyword("工作", 2.5, 1),
            new Keyword("安靜", 2.5, 1),
            new Keyword("插座", 2.5, 1),
            new Keyword("不限時", 3.0, 1)
        );
        
        // Tier 2: 次要詞
        List<Keyword> tier2 = Arrays.asList(
            new Keyword("wifi", 1.5, 2),
            new Keyword("舒適", 1.5, 2),
            new Keyword("寬敞", 1.5, 2),
            new Keyword("明亮", 1.5, 2)
        );
        
        // Tier 3: 參考詞
        List<Keyword> tier3 = Arrays.asList(
            new Keyword("咖啡", 0.8, 3),
            new Keyword("座位", 0.8, 3),
            new Keyword("環境", 0.8, 3)
        );
        
        // 建立索引
        for (Keyword kw : tier1) {
            keywordMap.put(kw.getName(), kw);
        }
        for (Keyword kw : tier2) {
            keywordMap.put(kw.getName(), kw);
        }
        for (Keyword kw : tier3) {
            keywordMap.put(kw.getName(), kw);
        }
        
        tierMap.put(1, tier1);
        tierMap.put(2, tier2);
        tierMap.put(3, tier3);
        
        System.out.println("使用預設關鍵字資料");
    }

    /**
     * 根據名稱查詢關鍵字
     * @param name 關鍵字名稱
     * @return 關鍵字物件，不存在則返回 null
     */
    public Keyword findByName(String name) {
        return keywordMap.get(name);
    }

    /**
     * 查詢所有關鍵字
     * @return 關鍵字列表
     */
    public List<Keyword> findAll() {
        return new ArrayList<>(keywordMap.values());
    }

    /**
     * 根據層級查詢關鍵字
     * @param tier 層級（1, 2, 3）
     * @return 該層級的關鍵字列表
     */
    public List<Keyword> findByTier(int tier) {
        return tierMap.getOrDefault(tier, new ArrayList<>());
    }

    /**
     * 根據權重範圍查詢關鍵字
     * @param minWeight 最低權重
     * @param maxWeight 最高權重
     * @return 權重在範圍內的關鍵字列表
     */
    public List<Keyword> findByWeightRange(double minWeight, double maxWeight) {
        return keywordMap.values().stream()
                .filter(kw -> kw.getWeight() >= minWeight && kw.getWeight() <= maxWeight)
                .collect(Collectors.toList());
    }

    /**
     * 根據最低權重查詢關鍵字
     * @param minWeight 最低權重
     * @return 權重不低於指定值的關鍵字列表
     */
    public List<Keyword> findByMinWeight(double minWeight) {
        return keywordMap.values().stream()
                .filter(kw -> kw.getWeight() >= minWeight)
                .collect(Collectors.toList());
    }

    /**
     * 模糊查詢關鍵字（名稱包含指定字串）
     * @param keyword 關鍵字片段
     * @return 符合的關鍵字列表
     */
    public List<Keyword> findByNameContaining(String keyword) {
        if (keyword == null || keyword.isEmpty()) {
            return new ArrayList<>();
        }
        
        String lowerKeyword = keyword.toLowerCase();
        
        return keywordMap.values().stream()
                .filter(kw -> kw.getName().toLowerCase().contains(lowerKeyword))
                .collect(Collectors.toList());
    }

    /**
     * 批次查詢關鍵字
     * @param names 關鍵字名稱列表
     * @return 關鍵字列表
     */
    public List<Keyword> findByNames(List<String> names) {
        if (names == null || names.isEmpty()) {
            return new ArrayList<>();
        }
        
        return names.stream()
                .map(this::findByName)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    /**
     * 檢查關鍵字是否存在
     * @param name 關鍵字名稱
     * @return 是否存在
     */
    public boolean existsByName(String name) {
        return keywordMap.containsKey(name);
    }

    /**
     * 獲取關鍵字總數
     * @return 總數
     */
    public long count() {
        return keywordMap.size();
    }

    /**
     * 獲取指定層級的關鍵字數量
     * @param tier 層級
     * @return 數量
     */
    public long countByTier(int tier) {
        return tierMap.getOrDefault(tier, new ArrayList<>()).size();
    }

    /**
     * 獲取所有關鍵字名稱列表
     * @return 關鍵字名稱列表
     */
    public List<String> findAllKeywordNames() {
        return new ArrayList<>(keywordMap.keySet());
    }

    /**
     * 獲取 Tier 1 關鍵字（核心詞）
     * @return Tier 1 關鍵字列表
     */
    public List<Keyword> findTier1Keywords() {
        return findByTier(1);
    }

    /**
     * 獲取 Tier 2 關鍵字（次要詞）
     * @return Tier 2 關鍵字列表
     */
    public List<Keyword> findTier2Keywords() {
        return findByTier(2);
    }

    /**
     * 獲取 Tier 3 關鍵字（參考詞）
     * @return Tier 3 關鍵字列表
     */
    public List<Keyword> findTier3Keywords() {
        return findByTier(3);
    }

    /**
     * 獲取權重最高的關鍵字
     * @param limit 返回數量
     * @return 權重最高的關鍵字列表
     */
    public List<Keyword> findTopWeightedKeywords(int limit) {
        return keywordMap.values().stream()
                .sorted((a, b) -> Double.compare(b.getWeight(), a.getWeight()))
                .limit(limit)
                .collect(Collectors.toList());
    }

    /**
     * 依權重分組統計
     * @return Map（權重 -> 關鍵字列表）
     */
    public Map<Double, List<Keyword>> groupByWeight() {
        return keywordMap.values().stream()
                .collect(Collectors.groupingBy(Keyword::getWeight));
    }

    /**
     * 統計各層級的關鍵字數量
     * @return Map（層級 -> 數量）
     */
    public Map<Integer, Long> countByTierMap() {
        Map<Integer, Long> counts = new HashMap<>();
        counts.put(1, countByTier(1));
        counts.put(2, countByTier(2));
        counts.put(3, countByTier(3));
        return counts;
    }

    /**
     * 計算平均權重
     * @return 平均權重
     */
    public double calculateAverageWeight() {
        return keywordMap.values().stream()
                .mapToDouble(Keyword::getWeight)
                .average()
                .orElse(0.0);
    }

    /**
     * 計算指定層級的平均權重
     * @param tier 層級
     * @return 平均權重
     */
    public double calculateAverageWeightByTier(int tier) {
        List<Keyword> keywords = tierMap.get(tier);
        if (keywords == null || keywords.isEmpty()) {
            return 0.0;
        }
        
        return keywords.stream()
                .mapToDouble(Keyword::getWeight)
                .average()
                .orElse(0.0);
    }

    /**
     * 獲取最高權重
     * @return 最高權重
     */
    public double getMaxWeight() {
        return keywordMap.values().stream()
                .mapToDouble(Keyword::getWeight)
                .max()
                .orElse(0.0);
    }

    /**
     * 獲取最低權重
     * @return 最低權重
     */
    public double getMinWeight() {
        return keywordMap.values().stream()
                .mapToDouble(Keyword::getWeight)
                .min()
                .orElse(0.0);
    }

    /**
     * 進階搜尋（支援多條件）
     * @param criteria 搜尋條件
     * @return 符合條件的關鍵字列表
     */
    public List<Keyword> advancedSearch(Map<String, Object> criteria) {
        List<Keyword> results = new ArrayList<>(keywordMap.values());
        
        // 層級篩選
        if (criteria.containsKey("tier")) {
            int tier = (Integer) criteria.get("tier");
            results = results.stream()
                .filter(kw -> kw.getTierNumber() == tier)
                .collect(Collectors.toList());
        }
        
        // 最低權重篩選
        if (criteria.containsKey("minWeight")) {
            double minWeight = ((Number) criteria.get("minWeight")).doubleValue();
            results = results.stream()
                    .filter(kw -> kw.getWeight() >= minWeight)
                    .collect(Collectors.toList());
        }
        
        // 最高權重篩選
        if (criteria.containsKey("maxWeight")) {
            double maxWeight = ((Number) criteria.get("maxWeight")).doubleValue();
            results = results.stream()
                    .filter(kw -> kw.getWeight() <= maxWeight)
                    .collect(Collectors.toList());
        }
        
        // 關鍵字名稱模糊搜尋
        if (criteria.containsKey("keyword")) {
            String keyword = ((String) criteria.get("keyword")).toLowerCase();
            results = results.stream()
                    .filter(kw -> kw.getName().toLowerCase().contains(keyword))
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
            loadKeywordsFromFile();
            System.out.println("重新載入關鍵字資料成功：" + keywordMap.size() + " 個");
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
        
        stats.put("totalKeywords", keywordMap.size());
        stats.put("tier1Count", countByTier(1));
        stats.put("tier2Count", countByTier(2));
        stats.put("tier3Count", countByTier(3));
        stats.put("averageWeight", calculateAverageWeight());
        stats.put("maxWeight", getMaxWeight());
        stats.put("minWeight", getMinWeight());
        
        // 各層級平均權重
        Map<Integer, Double> avgWeightByTier = new HashMap<>();
        avgWeightByTier.put(1, calculateAverageWeightByTier(1));
        avgWeightByTier.put(2, calculateAverageWeightByTier(2));
        avgWeightByTier.put(3, calculateAverageWeightByTier(3));
        stats.put("averageWeightByTier", avgWeightByTier);
        
        // 層級分布
        stats.put("tierDistribution", countByTierMap());
        
        return stats;
    }

    /**
     * 檢查 Repository 狀態
     * @return 狀態資訊
     */
    public Map<String, Object> getStatus() {
        Map<String, Object> status = new HashMap<>();
        status.put("repository", "KeywordRepository");
        status.put("status", "running");
        status.put("dataLoaded", !keywordMap.isEmpty());
        status.put("totalKeywords", keywordMap.size());
        status.put("dataFile", DATA_FILE_PATH);
        status.put("timestamp", System.currentTimeMillis());
        
        return status;
    }
}

