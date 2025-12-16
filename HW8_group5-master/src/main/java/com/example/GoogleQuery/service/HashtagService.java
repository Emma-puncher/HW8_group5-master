package com.example.GoogleQuery.service;

import com.example.GoogleQuery.model.WebPage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * HashtagService - Hashtag 生成服務
 * 為咖啡廳生成相關的 hashtag
 * 規則：
 * 1. 從使用者輸入提取最多 2 個重要詞彙
 * 2. 從網站內容中找出出現次數最多的 3 個 domain keywords
 * 3. 合併成最多 5 個 hashtag
 * 4. 不使用錯字、停用詞、或超長句子
 */
@Service
public class HashtagService {

    @Autowired
    private KeywordService keywordService;

    @Autowired
    private RankingService rankingService;

    private static final int MAX_HASHTAGS = 5;
    private static final int MAX_USER_KEYWORDS = 2;
    private static final int MAX_CONTENT_KEYWORDS = 3;
    private static final int MAX_HASHTAG_LENGTH = 15;
    private static final int MIN_HASHTAG_LENGTH = 2;

    /**
     * 生成 hashtag 列表
     * @param webPage 網頁
     * @param userInput 使用者輸入
     * @return Hashtag 列表（最多 5 個）
     */
    public List<String> generateHashtags(WebPage webPage, String userInput) {
        List<String> hashtags = new ArrayList<>();
        
        // 1. 從使用者輸入提取重要詞彙（最多 2 個）
        List<String> userKeywords = extractUserKeywords(userInput);
        hashtags.addAll(userKeywords);
        
        // 2. 從網站內容中找出出現最多的領域關鍵字（最多 3 個）
        List<String> contentKeywords = extractContentKeywords(webPage, MAX_CONTENT_KEYWORDS);
        hashtags.addAll(contentKeywords);
        
        // 3. 去重、過濾、限制數量
        List<String> finalHashtags = hashtags.stream()
                .distinct()
                .filter(this::isValidHashtag)
                .limit(MAX_HASHTAGS)
                .collect(Collectors.toList());
        
        return finalHashtags;
    }

    /**
     * 從使用者輸入提取重要詞彙
     * @param userInput 使用者輸入
     * @return 重要詞彙列表（最多 2 個）
     */
    private List<String> extractUserKeywords(String userInput) {
        if (userInput == null || userInput.trim().isEmpty()) {
            return new ArrayList<>();
        }
        
        // 提取領域關鍵字
        List<String> domainKeywords = keywordService.extractDomainKeywords(userInput);
        
        if (domainKeywords.isEmpty()) {
            // 如果沒有領域關鍵字，嘗試分詞並過濾
            return extractGeneralKeywords(userInput);
        }
        
        // 依權重排序並取前 2 個
        List<String> sorted = keywordService.sortByWeight(domainKeywords);
        return sorted.stream()
                .limit(MAX_USER_KEYWORDS)
                .collect(Collectors.toList());
    }

    /**
     * 提取一般關鍵字（當沒有領域關鍵字時使用）
     * @param userInput 使用者輸入
     * @return 關鍵字列表
     */
    private List<String> extractGeneralKeywords(String userInput) {
        // 簡單分詞（以空白分割）
        String[] words = userInput.trim().split("\\s+");
        
        List<String> keywords = new ArrayList<>();
        for (String word : words) {
            word = word.trim();
            
            // 過濾停用詞和無效詞
            if (!keywordService.isStopWord(word) && isValidHashtag(word)) {
                keywords.add(word);
            }
        }
        
        return keywords.stream()
                .limit(MAX_USER_KEYWORDS)
                .collect(Collectors.toList());
    }

    /**
     * 從網站內容中提取關鍵字
     * @param webPage 網頁
     * @param topN 返回前 N 個
     * @return 關鍵字列表
     */
    private List<String> extractContentKeywords(WebPage webPage, int topN) {
        // 使用 RankingService 找出出現最多的領域關鍵字
        return rankingService.getTopDomainKeywords(webPage, topN);
    }

    /**
     * 驗證 hashtag 是否有效
     * @param hashtag Hashtag
     * @return 是否有效
     */
    private boolean isValidHashtag(String hashtag) {
        if (hashtag == null || hashtag.trim().isEmpty()) {
            return false;
        }
        
        String trimmed = hashtag.trim();
        
        // 長度檢查
        if (trimmed.length() < MIN_HASHTAG_LENGTH || trimmed.length() > MAX_HASHTAG_LENGTH) {
            return false;
        }
        
        // 不能是停用詞
        if (keywordService.isStopWord(trimmed)) {
            return false;
        }
        
        // 不能包含特殊字元（只允許中文、英文、數字）
        if (!trimmed.matches("[\\u4e00-\\u9fa5a-zA-Z0-9]+")) {
            return false;
        }
        
        return true;
    }

    /**
     * 生成帶 # 符號的 hashtag
     * @param webPage 網頁
     * @param userInput 使用者輸入
     * @return 帶 # 的 Hashtag 列表
     */
    public List<String> generateHashtagsWithSymbol(WebPage webPage, String userInput) {
        List<String> hashtags = generateHashtags(webPage, userInput);
        
        return hashtags.stream()
                .map(tag -> "#" + tag)
                .collect(Collectors.toList());
    }

    /**
     * 生成 hashtag 字串（用空格分隔）
     * @param webPage 網頁
     * @param userInput 使用者輸入
     * @return Hashtag 字串
     */
    public String generateHashtagString(WebPage webPage, String userInput) {
        List<String> hashtags = generateHashtagsWithSymbol(webPage, userInput);
        return String.join(" ", hashtags);
    }

    /**
     * 批次生成 hashtag
     * @param webPages 網頁列表
     * @param userInput 使用者輸入
     * @return Map（URL -> Hashtag 列表）
     */
    public Map<String, List<String>> generateHashtagsBatch(
            List<WebPage> webPages, 
            String userInput) {
        
        Map<String, List<String>> result = new HashMap<>();
        
        for (WebPage page : webPages) {
            List<String> hashtags = generateHashtags(page, userInput);
            result.put(page.getUrl(), hashtags);
        }
        
        return result;
    }

    /**
     * 計算 hashtag 相關性分數
     * @param hashtags Hashtag 列表
     * @return 總分（基於關鍵字權重）
     */
    public double calculateHashtagScore(List<String> hashtags) {
        double totalScore = 0.0;
        
        for (String hashtag : hashtags) {
            double weight = keywordService.getKeywordWeight(hashtag);
            totalScore += weight;
        }
        
        return totalScore;
    }

    /**
     * 依相關性排序 hashtag
     * @param hashtags Hashtag 列表
     * @return 排序後的列表（權重由高到低）
     */
    public List<String> sortHashtagsByRelevance(List<String> hashtags) {
        return hashtags.stream()
                .sorted((a, b) -> {
                    double weightA = keywordService.getKeywordWeight(a);
                    double weightB = keywordService.getKeywordWeight(b);
                    return Double.compare(weightB, weightA);
                })
                .collect(Collectors.toList());
    }

    /**
     * 生成熱門 hashtag（基於所有咖啡廳）
     * @param webPages 所有網頁
     * @param topN 返回前 N 個
     * @return 熱門 Hashtag 列表
     */
    public List<String> generateTrendingHashtags(List<WebPage> webPages, int topN) {
        Map<String, Integer> hashtagCounts = new HashMap<>();
        
        // 統計所有 hashtag 出現次數
        for (WebPage page : webPages) {
            List<String> hashtags = generateHashtags(page, "");
            
            for (String hashtag : hashtags) {
                hashtagCounts.put(hashtag, hashtagCounts.getOrDefault(hashtag, 0) + 1);
            }
        }
        
        // 依出現次數排序
        return hashtagCounts.entrySet().stream()
                .sorted((a, b) -> Integer.compare(b.getValue(), a.getValue()))
                .limit(topN)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }

    /**
     * 依地區生成代表性 hashtag
     * @param district 地區
     * @param webPages 該地區的網頁列表
     * @param topN 返回前 N 個
     * @return 代表性 Hashtag 列表
     */
    public List<String> generateDistrictHashtags(
            String district, 
            List<WebPage> webPages, 
            int topN) {
        
        Map<String, Integer> hashtagCounts = new HashMap<>();
        
        for (WebPage page : webPages) {
            List<String> hashtags = generateHashtags(page, "");
            
            for (String hashtag : hashtags) {
                hashtagCounts.put(hashtag, hashtagCounts.getOrDefault(hashtag, 0) + 1);
            }
        }
        
        return hashtagCounts.entrySet().stream()
                .sorted((a, b) -> Integer.compare(b.getValue(), a.getValue()))
                .limit(topN)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }

    /**
     * 合併並去重多個 hashtag 列表
     * @param hashtagLists Hashtag 列表的列表
     * @return 合併後的唯一 Hashtag 列表
     */
    public List<String> mergeHashtags(List<List<String>> hashtagLists) {
        return hashtagLists.stream()
                .flatMap(List::stream)
                .distinct()
                .filter(this::isValidHashtag)
                .collect(Collectors.toList());
    }

    /**
     * 找出共同的 hashtag
     * @param hashtagLists Hashtag 列表的列表
     * @return 共同的 Hashtag 列表
     */
    public List<String> findCommonHashtags(List<List<String>> hashtagLists) {
        if (hashtagLists.isEmpty()) {
            return new ArrayList<>();
        }
        
        // 從第一個列表開始
        Set<String> common = new HashSet<>(hashtagLists.get(0));
        
        // 與其他列表求交集
        for (int i = 1; i < hashtagLists.size(); i++) {
            common.retainAll(hashtagLists.get(i));
        }
        
        return new ArrayList<>(common);
    }

    /**
     * 生成 hashtag 建議（基於部分輸入）
     * @param partialInput 部分輸入
     * @param limit 返回數量
     * @return 建議的 Hashtag 列表
     */
    public List<String> suggestHashtags(String partialInput, int limit) {
        if (partialInput == null || partialInput.length() < 1) {
            return new ArrayList<>();
        }
        
        String input = partialInput.toLowerCase();
        
        // 從所有關鍵字中找出匹配的
        return keywordService.getAllKeywordsName().stream()
                .filter(keyword -> keyword.toLowerCase().contains(input))
                .filter(keyword -> isValidHashtag(keyword))
                .limit(limit)
                .collect(Collectors.toList());
    }

    /**
     * 獲取 hashtag 統計資訊
     * @param hashtags Hashtag 列表
     * @return 統計資訊
     */
    public Map<String, Object> getHashtagStatistics(List<String> hashtags) {
        Map<String, Object> stats = new HashMap<>();
        
        stats.put("totalHashtags", hashtags.size());
        stats.put("averageLength", 
            hashtags.stream()
                .mapToInt(String::length)
                .average()
                .orElse(0.0)
        );
        
        // 統計各層級關鍵字數量
        Map<Integer, Long> tierCounts = hashtags.stream()
                .collect(Collectors.groupingBy(
                    keywordService::getKeywordTier,
                    Collectors.counting()
                ));
        stats.put("tierDistribution", tierCounts);
        
        // 總相關性分數
        stats.put("relevanceScore", calculateHashtagScore(hashtags));
        
        return stats;
    }

    /**
     * 檢查服務狀態
     */
    public Map<String, Object> getServiceStatus() {
        Map<String, Object> status = new HashMap<>();
        status.put("service", "HashtagService");
        status.put("status", "running");
        status.put("maxHashtags", MAX_HASHTAGS);
        status.put("maxUserKeywords", MAX_USER_KEYWORDS);
        status.put("maxContentKeywords", MAX_CONTENT_KEYWORDS);
        status.put("timestamp", System.currentTimeMillis());
        
        return status;
    }
}


