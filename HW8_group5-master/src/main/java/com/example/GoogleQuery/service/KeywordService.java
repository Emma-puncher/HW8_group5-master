package com.example.GoogleQuery.service;

import com.example.GoogleQuery.model.Keyword;
import com.example.GoogleQuery.model.KeywordTier;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;

/**
 * KeywordService - 關鍵字處理服務
 * 管理咖啡廳領域的分級關鍵字及其權重
 */
@Service
public class KeywordService {

    private static final String DATA_FILE_PATH = "data/keywords.json";

    // 關鍵字映射表 (keyword name -> Keyword object)
    private Map<String, Keyword> keywordMap;
    
    // 層級映射表 (tier number -> List of Keywords)
    private Map<Integer, List<Keyword>> tierMap;
    
    // 所有關鍵字的統一映射（保留向後兼容）
    private Map<String, Keyword> allKeywordsMap;
    
    // 多語言關鍵字對照
    private Map<String, String> keywordTranslations;
    
    // 停用詞列表
    private Set<String> stopWords;
    
    // 注入從 DataLoaderConfig 載入的關鍵字列表
    private final ArrayList<Keyword> keywordList;
    
    public KeywordService(@Qualifier("keywordList") ArrayList<Keyword> keywordList) {
        this.keywordList = keywordList;
    }

    /**
     * 初始化：載入關鍵字資料
     */
    @PostConstruct
    public void init() {
        try {
            // 使用已經載入的關鍵字列表
            loadKeywordsFromList();
            loadTranslations();
            loadStopWords();
            
            // allKeywordsMap 指向 keywordMap（保持向後兼容）
            allKeywordsMap = keywordMap;
            
            System.out.println("KeywordService 初始化完成：");
            System.out.println("  - Tier 1 關鍵字: " + tierMap.getOrDefault(1, new ArrayList<>()).size());
            System.out.println("  - Tier 2 關鍵字: " + tierMap.getOrDefault(2, new ArrayList<>()).size());
            System.out.println("  - Tier 3 關鍵字: " + tierMap.getOrDefault(3, new ArrayList<>()).size());
            System.out.println("  - 停用詞: " + stopWords.size());
            
        } catch (Exception e) {
            System.err.println("KeywordService 初始化失敗: " + e.getMessage());
            e.printStackTrace();
            initializeDefaultKeywords();
            allKeywordsMap = keywordMap;
        }
    }
    
    /**
     * 從注入的關鍵字列表載入資料
     */
    private void loadKeywordsFromList() {
        keywordMap = new HashMap<>();
        tierMap = new HashMap<>();
        
        for (Keyword keyword : keywordList) {
            keywordMap.put(keyword.getName(), keyword);
            
            // 根據 KeywordTier 轉換為數字層級
            int tierNumber = getTierNumber(keyword.getTier());
            tierMap.computeIfAbsent(tierNumber, k -> new ArrayList<>()).add(keyword);
        }
    }
    
    /**
     * 將 KeywordTier 轉換為數字層級
     */
    private int getTierNumber(KeywordTier tier) {
        if (tier == null) {
            return 3; // 預設為 Tier 3
        }
        switch (tier) {
            case CORE:
                return 1;
            case SECONDARY:
                return 2;
            case REFERENCE:
                return 3;
            default:
                return 3;
        }
    }

    /**
     * 初始化預設關鍵字（當無法載入檔案時使用）
     */
    private void initializeDefaultKeywords() {
        keywordMap = new HashMap<>();
        tierMap = new HashMap<>();
        
        // Tier 1: 核心詞 - 使用 int 建構子
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
     * 載入多語言關鍵字對照
     */
    private void loadTranslations() {
        try {
            ObjectMapper mapper = new ObjectMapper();
            InputStream inputStream = new ClassPathResource("data/keyword-translations.json").getInputStream();
            
            keywordTranslations = mapper.readValue(inputStream, new TypeReference<Map<String, String>>() {});
            
        } catch (IOException e) {
            System.out.println("未找到翻譯檔案，使用預設設定");
            keywordTranslations = new HashMap<>();
        }
    }

    /**
     * 載入停用詞
     */
    private void loadStopWords() {
        stopWords = new HashSet<>();
        
        try {
            InputStream inputStream = new ClassPathResource("config/stopwords.txt").getInputStream();
            Scanner scanner = new Scanner(inputStream, "UTF-8");
            
            while (scanner.hasNextLine()) {
                String word = scanner.nextLine().trim();
                if (!word.isEmpty() && !word.startsWith("#")) {
                    stopWords.add(word);
                }
            }
            
            scanner.close();
            
        } catch (IOException e) {
            System.out.println("未找到停用詞檔案，使用預設停用詞");
            initializeDefaultStopWords();
        }
    }

    /**
     * 初始化預設停用詞
     */
    private void initializeDefaultStopWords() {
        stopWords = new HashSet<>(Arrays.asList(
            "的", "了", "是", "在", "我", "有", "和", "就", "不", "人",
            "都", "一", "一個", "上", "也", "很", "到", "說", "要", "去"
        ));
    }

    /**
     * 獲取關鍵字權重
     * @param keyword 關鍵字
     * @return 權重值（如果不存在則返回 0.0）
     */
    public double getKeywordWeight(String keyword) {
        Keyword kw = keywordMap.get(keyword);
        return kw != null ? kw.getWeight() : 0.0;
    }

    /**
     * 檢查是否為領域關鍵字
     * @param keyword 關鍵字
     * @return 是否為領域關鍵字
     */
    public boolean isDomainKeyword(String keyword) {
        return keywordMap.containsKey(keyword);
    }

    /**
     * 獲取關鍵字的層級
     * @param keyword 關鍵字
     * @return 層級 (1, 2, 3) 或 0（不存在）
     */
    public int getKeywordTier(String keyword) {
        Keyword kw = keywordMap.get(keyword);
        return kw != null ? kw.getTierNumber() : 0;
    }

    /**
     * 獲取指定層級的關鍵字
     * @param tier 層級（1, 2, 3）
     * @return 該層級的關鍵字列表
     */
    public List<Keyword> getKeywordsByTier(int tier) {
        return tierMap.getOrDefault(tier, new ArrayList<>());
    }

    /**
     * 檢查是否為停用詞
     * @param word 詞彙
     * @return 是否為停用詞
     */
    public boolean isStopWord(String word) {
        return stopWords.contains(word);
    }

    /**
     * 過濾停用詞
     * @param words 詞彙列表
     * @return 過濾後的詞彙列表
     */
    public List<String> filterStopWords(List<String> words) {
        return words.stream()
                .filter(word -> !isStopWord(word))
                .collect(Collectors.toList());
    }

    /**
     * 翻譯關鍵字（支援多語言）
     * @param keyword 關鍵字
     * @return 翻譯後的關鍵字（如果沒有翻譯則返回原詞）
     */
    public String translateKeyword(String keyword) {
        return keywordTranslations.getOrDefault(keyword, keyword);
    }

    /**
     * 提取使用者輸入中的領域關鍵字
     * @param userInput 使用者輸入
     * @return 領域關鍵字列表
     */
    public List<String> extractDomainKeywords(String userInput) {
        List<String> domainKeywords = new ArrayList<>();
        
        if (userInput == null || userInput.isEmpty()) {
            return domainKeywords;
        }
        
        String input = userInput.toLowerCase();
        
        // 檢查所有領域關鍵字是否出現在輸入中
        for (String keyword : keywordMap.keySet()) {
            if (input.contains(keyword)) {
                domainKeywords.add(keyword);
            }
        }
        
        return domainKeywords;
    }

    /**
     * 計算關鍵字的加權分數
     * @param keywords 關鍵字列表
     * @param counts 每個關鍵字的出現次數
     * @return 總加權分數
     */
    public double calculateWeightedScore(List<String> keywords, Map<String, Integer> counts) {
        double totalScore = 0.0;
        
        for (String keyword : keywords) {
            double weight = getKeywordWeight(keyword);
            int count = counts.getOrDefault(keyword, 0);
            totalScore += weight * count;
        }
        
        return totalScore;
    }

    /**
     * 獲取所有關鍵字
     * @return 所有關鍵字列表
     */
    public List<Keyword> getAllKeywords() {
        return new ArrayList<>(keywordMap.values());
    }

    /**
     * 獲取所有關鍵字的字串型態
     * @return 所有關鍵字列表
     */
    public List<String> getAllKeywordsName() {
        return new ArrayList<>(keywordMap.keySet());
    }

    /**
     * 獲取 Tier 1 關鍵字（核心詞）
     * @return Tier 1 關鍵字列表
     */
    public List<String> getTier1Keywords() {
        return tierMap.getOrDefault(1, new ArrayList<>()).stream()
                .map(Keyword::getName)
                .collect(Collectors.toList());
    }

    /**
     * 獲取 Tier 2 關鍵字（次要詞）
     * @return Tier 2 關鍵字列表
     */
    public List<String> getTier2Keywords() {
        return tierMap.getOrDefault(2, new ArrayList<>()).stream()
                .map(Keyword::getName)
                .collect(Collectors.toList());
    }

    /**
     * 獲取 Tier 3 關鍵字（參考詞）
     * @return Tier 3 關鍵字列表
     */
    public List<String> getTier3Keywords() {
        return tierMap.getOrDefault(3, new ArrayList<>()).stream()
                .map(Keyword::getName)
                .collect(Collectors.toList());
    }

    /**
     * 獲取關鍵字詳細資訊
     * @param keyword 關鍵字
     * @return Keyword 物件
     */
    public Keyword getKeywordInfo(String keyword) {
        return keywordMap.get(keyword);
    }

    /**
     * 依權重排序關鍵字
     * @param keywords 關鍵字列表
     * @return 排序後的關鍵字列表（權重由高到低）
     */
    public List<String> sortByWeight(List<String> keywords) {
        return keywords.stream()
                .sorted((a, b) -> Double.compare(getKeywordWeight(b), getKeywordWeight(a)))
                .collect(Collectors.toList());
    }

    /**
     * 依出現次數和權重排序關鍵字
     * @param keywordCounts 關鍵字及其出現次數
     * @return 排序後的關鍵字列表
     */
    public List<String> sortByWeightedCount(Map<String, Integer> keywordCounts) {
        return keywordCounts.entrySet().stream()
                .sorted((a, b) -> {
                    double scoreA = getKeywordWeight(a.getKey()) * a.getValue();
                    double scoreB = getKeywordWeight(b.getKey()) * b.getValue();
                    return Double.compare(scoreB, scoreA);
                })
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }

    /**
     * 驗證關鍵字
     * @param keyword 關鍵字
     * @return 是否有效（不為空、不是停用詞、長度合理）
     */
    public boolean isValidKeyword(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return false;
        }
        
        String trimmed = keyword.trim();
        
        // 檢查長度（1-20 字元）
        if (trimmed.length() < 1 || trimmed.length() > 20) {
            return false;
        }
        
        // 不能是停用詞
        if (isStopWord(trimmed)) {
            return false;
        }
        
        return true;
    }

    /**
     * 新增關鍵字（動態擴充）
     * @param keyword 關鍵字
     * @param weight 權重
     * @param tier 層級
     * @return 是否新增成功
     */
    public boolean addKeyword(String keyword, double weight, int tier) {
        if (!isValidKeyword(keyword)) {
            return false;
        }
        
        if (keywordMap.containsKey(keyword)) {
            return false; // 已存在
        }
        
        Keyword kw = new Keyword(keyword, weight, tier);
        keywordMap.put(keyword, kw);
        
        // 加入對應的層級列表
        tierMap.computeIfAbsent(tier, k -> new ArrayList<>()).add(kw);
        
        return true;
    }

    /**
     * 更新關鍵字權重
     * @param keyword 關鍵字
     * @param newWeight 新權重
     * @return 是否更新成功
     */
    public boolean updateKeywordWeight(String keyword, double newWeight) {
        Keyword kw = keywordMap.get(keyword);
        if (kw == null) {
            return false;
        }
        
        kw.setWeight(newWeight);
        
        return true;
    }

    /**
     * 獲取關鍵字統計資訊
     * @return 統計資訊
     */
    public Map<String, Object> getKeywordStatistics() {
        Map<String, Object> stats = new HashMap<>();
        
        stats.put("totalKeywords", keywordMap.size());
        stats.put("tier1Count", tierMap.getOrDefault(1, new ArrayList<>()).size());
        stats.put("tier2Count", tierMap.getOrDefault(2, new ArrayList<>()).size());
        stats.put("tier3Count", tierMap.getOrDefault(3, new ArrayList<>()).size());
        stats.put("stopWordsCount", stopWords.size());
        stats.put("translationsCount", keywordTranslations.size());
        
        // 平均權重
        double avgWeight = keywordMap.values().stream()
                .mapToDouble(Keyword::getWeight)
                .average()
                .orElse(0.0);
        stats.put("averageWeight", avgWeight);
        
        return stats;
    }

    /**
     * 檢查服務狀態
     */
    public Map<String, Object> getServiceStatus() {
        Map<String, Object> status = new HashMap<>();
        status.put("service", "KeywordService");
        status.put("status", "running");
        status.put("keywordsLoaded", keywordMap.size() > 0);
        status.put("timestamp", System.currentTimeMillis());
        
        return status;
    }
}