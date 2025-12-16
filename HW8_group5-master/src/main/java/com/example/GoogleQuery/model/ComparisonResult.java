package com.example.GoogleQuery.model;

import java.util.*;

/**
 * ComparisonResult - 比較結果模型
 * 封裝 2-3 家咖啡廳的比較結果
 */
public class ComparisonResult {
    
    private List<Cafe> cafes;                           // 要比較的咖啡廳列表
    private Map<String, Map<String, Object>> comparison; // 比較表（功能 -> {咖啡廳ID -> 值}）
    private Map<String, List<String>> advantages;        // 各咖啡廳的優勢
    private Map<String, List<String>> disadvantages;     // 各咖啡廳的劣勢
    private String recommendation;                       // 推薦建議
    private long timestamp;                              // 比較時間戳
    
    /**
     * 建構子
     * @param cafes 要比較的咖啡廳列表（2-3 家）
     */
    public ComparisonResult(List<Cafe> cafes) {
        if (cafes == null || cafes.size() < 2 || cafes.size() > 3) {
            throw new IllegalArgumentException("比較功能需要 2-3 家咖啡廳");
        }
        
        this.cafes = new ArrayList<>(cafes);
        this.comparison = new HashMap<>();
        this.advantages = new HashMap<>();
        this.disadvantages = new HashMap<>();
        this.recommendation = "";
        this.timestamp = System.currentTimeMillis();
        
        // 初始化優劣勢列表
        for (Cafe cafe : cafes) {
            advantages.put(cafe.getId(), new ArrayList<>());
            disadvantages.put(cafe.getId(), new ArrayList<>());
        }
    }

    /**
     * 無參數建構子（向後相容）
     */
    public ComparisonResult() {
        this.cafes = new ArrayList<>();
        this.comparison = new HashMap<>();
        this.advantages = new HashMap<>();
        this.disadvantages = new HashMap<>();
        this.recommendation = "";
        this.timestamp = System.currentTimeMillis();
    }

    // Compatibility setters used by ComparisonService (accept SearchResult lists)
    public void setCafes(List<SearchResult> searchResults) {
        if (searchResults == null) {
            this.cafes = new ArrayList<>();
            return;
        }

        List<Cafe> converted = new ArrayList<>();
        for (SearchResult sr : searchResults) {
            String id = sr.getCafeId() != null && !sr.getCafeId().isEmpty() ? sr.getCafeId() : java.util.UUID.randomUUID().toString();
            String name = sr.getName() != null ? sr.getName() : "";
            String url = sr.getUrl() != null ? sr.getUrl() : "";
            String district = sr.getDistrict() != null ? sr.getDistrict() : "";
            String address = sr.getAddress() != null ? sr.getAddress() : "";

            Cafe c = new Cafe(id, name, url, district, address);
            c.setRating(sr.getRating());
            c.setBaselineScore(sr.getScore());
            if (sr.getFeatures() != null) {
                c.setImages(new ArrayList<>());
            }
            converted.add(c);
        }

        this.cafes = converted;
    }

    public void setBasicComparison(Map<String, List<String>> basic) {
        // convert basic comparisons into internal comparison map under key "basic"
        Map<String, Object> map = new HashMap<>();
        for (Map.Entry<String, List<String>> e : basic.entrySet()) {
            map.put(e.getKey(), e.getValue());
        }
        this.comparison.put("basic", map);
    }

    public void setFeatureComparison(Map<String, List<Boolean>> featureComparison) {
        // store under key "feature"
        Map<String, Object> map = new HashMap<>();
        for (Map.Entry<String, List<Boolean>> e : featureComparison.entrySet()) {
            map.put(e.getKey(), e.getValue());
        }
        this.comparison.put("feature", map);
    }

    public void setProsAndCons(List<Map<String, List<String>>> prosAndCons) {
        // flatten into advantages/disadvantages if possible
        if (prosAndCons == null) return;
        for (int i = 0; i < prosAndCons.size(); i++) {
            Map<String, List<String>> m = prosAndCons.get(i);
            String cafeId = (i < cafes.size()) ? cafes.get(i).getId() : "cafe_" + i;
            advantages.put(cafeId, m.getOrDefault("pros", new ArrayList<>()));
            disadvantages.put(cafeId, m.getOrDefault("cons", new ArrayList<>()));
        }
    }

    public void setSummary(String summary) {
        // put into recommendation as well for compatibility
        this.recommendation = summary != null ? summary : this.recommendation;
    }
    
    
    // ========== Getters and Setters ==========
    
    public List<Cafe> getCafes() {
        return new ArrayList<>(cafes);
    }
    
    public Map<String, Map<String, Object>> getComparison() {
        return new HashMap<>(comparison);
    }
    
    public void setComparison(Map<String, Map<String, Object>> comparison) {
        this.comparison = comparison;
    }
    
    public Map<String, List<String>> getAdvantages() {
        return new HashMap<>(advantages);
    }
    
    public Map<String, List<String>> getDisadvantages() {
        return new HashMap<>(disadvantages);
    }
    
    public String getRecommendation() {
        return recommendation;
    }
    
    public void setRecommendation(String recommendation) {
        this.recommendation = recommendation;
    }
    
    public long getTimestamp() {
        return timestamp;
    }
    
    // ========== 比較功能方法 ==========
    
    /**
     * 新增比較項目
     * @param feature 功能名稱（例：不限時、有插座）
     * @param values 各咖啡廳的值（Map<咖啡廳ID, 值>）
     */
    public void addComparisonItem(String feature, Map<String, Object> values) {
        comparison.put(feature, values);
    }
    
    /**
     * 比較特定功能
     * @param feature 功能名稱
     * @return 各咖啡廳在此功能上的值
     */
    public Map<String, Object> getFeatureComparison(String feature) {
        return comparison.getOrDefault(feature, new HashMap<>());
    }
    
    /**
     * 新增咖啡廳的優勢
     * @param cafeId 咖啡廳 ID
     * @param advantage 優勢描述
     */
    public void addAdvantage(String cafeId, String advantage) {
        if (advantages.containsKey(cafeId)) {
            advantages.get(cafeId).add(advantage);
        }
    }
    
    /**
     * 新增咖啡廳的劣勢
     * @param cafeId 咖啡廳 ID
     * @param disadvantage 劣勢描述
     */
    public void addDisadvantage(String cafeId, String disadvantage) {
        if (disadvantages.containsKey(cafeId)) {
            disadvantages.get(cafeId).add(disadvantage);
        }
    }
    
    /**
     * 取得特定咖啡廳的優勢
     * @param cafeId 咖啡廳 ID
     * @return 優勢列表
     */
    public List<String> getCafeAdvantages(String cafeId) {
        return new ArrayList<>(advantages.getOrDefault(cafeId, new ArrayList<>()));
    }
    
    /**
     * 取得特定咖啡廳的劣勢
     * @param cafeId 咖啡廳 ID
     * @return 劣勢列表
     */
    public List<String> getCafeDisadvantages(String cafeId) {
        return new ArrayList<>(disadvantages.getOrDefault(cafeId, new ArrayList<>()));
    }
    
    /**
     * 分析優劣勢（自動生成）
     * 根據功能比較結果自動判斷各咖啡廳的優劣勢
     */
    public void analyzeAdvantagesAndDisadvantages() {
        // 清空現有的優劣勢
        for (String cafeId : advantages.keySet()) {
            advantages.get(cafeId).clear();
            disadvantages.get(cafeId).clear();
        }
        
        // 功能性比較
        String[] features = {"不限時", "有插座", "有wifi", "CP值高", "安靜", "燈光充足"};
        
        for (String feature : features) {
            Map<String, Boolean> featureValues = new HashMap<>();
            
            // 收集各咖啡廳在此功能上的值
            for (Cafe cafe : cafes) {
                boolean hasFeature = cafe.hasFeature(feature);
                featureValues.put(cafe.getId(), hasFeature);
            }
            
            // 判斷優劣勢
            for (Cafe cafe : cafes) {
                boolean hasFeature = featureValues.get(cafe.getId());
                
                // 統計有多少咖啡廳有此功能
                long countWithFeature = featureValues.values().stream()
                                                     .filter(v -> v)
                                                     .count();
                
                // 如果此咖啡廳有而其他都沒有，算優勢
                if (hasFeature && countWithFeature == 1) {
                    addAdvantage(cafe.getId(), feature);
                }
                
                // 如果此咖啡廳沒有而其他都有，算劣勢
                if (!hasFeature && countWithFeature == cafes.size() - 1) {
                    addDisadvantage(cafe.getId(), "沒有" + feature);
                }
            }
        }
        
        // 分數比較
        Cafe highestScoreCafe = cafes.stream()
                                     .max(Comparator.comparingDouble(Cafe::getScore))
                                     .orElse(null);
        
        if (highestScoreCafe != null) {
            addAdvantage(highestScoreCafe.getId(), "綜合評分最高");
        }
        
        // 評分比較
        Cafe highestRatingCafe = cafes.stream()
                                      .max(Comparator.comparingDouble(Cafe::getRating))
                                      .orElse(null);
        
        if (highestRatingCafe != null && !highestRatingCafe.equals(highestScoreCafe)) {
            addAdvantage(highestRatingCafe.getId(), "使用者評分最高");
        }
    }
    
    /**
     * 生成推薦建議
     * 根據比較結果生成推薦文字
     * @param priorities 使用者的優先條件（例：["不限時", "安靜"]）
     */
    public void generateRecommendation(List<String> priorities) {
        if (priorities == null || priorities.isEmpty()) {
            // 沒有特定優先條件，推薦綜合評分最高的
            Cafe bestCafe = cafes.stream()
                                 .max(Comparator.comparingDouble(Cafe::getScore))
                                 .orElse(cafes.get(0));
            
            recommendation = String.format(
                "推薦：%s（綜合評分最高，分數 %.2f）",
                bestCafe.getName(),
                bestCafe.getScore()
            );
        } else {
            // 根據優先條件推薦
            Map<Cafe, Integer> matchCounts = new HashMap<>();
            
            for (Cafe cafe : cafes) {
                int matchCount = 0;
                for (String priority : priorities) {
                    if (cafe.hasFeature(priority)) {
                        matchCount++;
                    }
                }
                matchCounts.put(cafe, matchCount);
            }
            
            // 找出符合最多優先條件的咖啡廳
            Cafe bestMatch = matchCounts.entrySet().stream()
                                        .max(Map.Entry.comparingByValue())
                                        .map(Map.Entry::getKey)
                                        .orElse(cafes.get(0));
            
            int matchCount = matchCounts.get(bestMatch);
            
            recommendation = String.format(
                "根據您的需求（%s），推薦：%s（符合 %d/%d 項條件）",
                String.join("、", priorities),
                bestMatch.getName(),
                matchCount,
                priorities.size()
            );
        }
    }
    
    /**
     * 取得比較摘要（純文字）
     * @return 比較摘要字串
     */
    public String getSummary() {
        StringBuilder summary = new StringBuilder();
        
        summary.append("=== 咖啡廳比較結果 ===\n\n");
        
        // 列出比較的咖啡廳
        summary.append("比較咖啡廳：\n");
        for (int i = 0; i < cafes.size(); i++) {
            Cafe cafe = cafes.get(i);
            summary.append(String.format("  %d. %s（%s）\n", 
                i + 1, cafe.getName(), cafe.getDistrict()));
        }
        summary.append("\n");
        
        // 優劣勢分析
        for (Cafe cafe : cafes) {
            summary.append(String.format("【%s】\n", cafe.getName()));
            
            List<String> adv = getCafeAdvantages(cafe.getId());
            if (!adv.isEmpty()) {
                summary.append("  優勢：").append(String.join("、", adv)).append("\n");
            }
            
            List<String> disadv = getCafeDisadvantages(cafe.getId());
            if (!disadv.isEmpty()) {
                summary.append("  劣勢：").append(String.join("、", disadv)).append("\n");
            }
            
            summary.append("\n");
        }
        
        // 推薦建議
        if (!recommendation.isEmpty()) {
            summary.append(recommendation).append("\n");
        }
        
        return summary.toString();
    }
    
    /**
     * 轉換為 JSON 格式
     * @return JSON 字串
     */
    public String toJson() {
        // 簡化版 JSON（實際專案中應使用 Gson 或 Jackson）
        StringBuilder json = new StringBuilder();
        json.append("{");
        json.append("\"cafes\": [");
        
        for (int i = 0; i < cafes.size(); i++) {
            json.append(cafes.get(i).toJson());
            if (i < cafes.size() - 1) {
                json.append(", ");
            }
        }
        
        json.append("], ");
        json.append("\"recommendation\": \"").append(recommendation).append("\", ");
        json.append("\"timestamp\": ").append(timestamp);
        json.append("}");
        
        return json.toString();
    }
    
    /**
     * toString 方法
     */
    @Override
    public String toString() {
        return getSummary();
    }
}

