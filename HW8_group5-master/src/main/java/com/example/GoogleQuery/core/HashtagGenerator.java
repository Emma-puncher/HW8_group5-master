package com.example.GoogleQuery.core;

import com.example.GoogleQuery.model.Keyword;
import java.util.*;
import java.util.regex.Pattern;

/**
 * HashtagGenerator - Hashtag 生成器
 * 生成 3-5 個 Hashtags：
 * - 從使用者輸入中提取重要詞彙（最多 2 個）
 * - 從網站內容中挑出出現次數最多的 3 個 domain keywords
 * - 合併成最多 5 個 Hashtag
 * - 過濾錯字、停用詞、超長句子
 */
public class HashtagGenerator {
    
    // 停用詞列表（常見的無意義詞彙）
    private static final Set<String> STOP_WORDS = new HashSet<>(Arrays.asList(
        "的", "是", "在", "有", "和", "與", "或", "但", "了", "著", "也", "就", "都",
        "而", "及", "等", "這", "那", "我", "你", "他", "她", "它", "們", "會", "能",
        "可", "要", "不", "沒", "很", "更", "最", "些", "個", "裡", "上", "下",
        "a", "an", "the", "is", "are", "was", "were", "be", "been", "being",
        "have", "has", "had", "do", "does", "did", "will", "would", "should",
        "could", "may", "might", "must", "can", "of", "to", "for", "with", "on",
        "at", "by", "from", "in", "into", "through", "during", "before", "after"
    ));
    
    // 最大 Hashtag 長度（避免超長句子）
    private static final int MAX_HASHTAG_LENGTH = 10;
    
    // 最小 Hashtag 長度（避免單字元）
    private static final int MIN_HASHTAG_LENGTH = 2;
    
    /**
     * 生成 Hashtags
     * @param content 網站內容
     * @param keywords 所有 domain keywords
     * @param userQuery 使用者查詢
     * @param topN 從網站內容取前 N 個關鍵字（預設 3）
     * @param userTopN 從使用者查詢取前 N 個詞（預設 2）
     * @return Hashtag 字串（例：#不限時 #安靜 #咖啡）
     */
    public String generate(String content, ArrayList<Keyword> keywords, 
                          String userQuery, int topN, int userTopN) {
        
        Set<String> hashtagSet = new LinkedHashSet<>(); // 使用 Set 避免重複
        
        // 1. 從使用者查詢中提取重要詞彙（最多 userTopN 個）
        List<String> userKeywords = extractUserKeywords(userQuery, keywords, userTopN);
        hashtagSet.addAll(userKeywords);
        
        // 2. 從網站內容中提取 Top N 關鍵字
        List<String> contentKeywords = extractTopKeywordsFromContent(content, keywords, topN);
        hashtagSet.addAll(contentKeywords);
        
        // 3. 限制最多 5 個 Hashtags
        List<String> finalHashtags = new ArrayList<>(hashtagSet);
        if (finalHashtags.size() > 5) {
            finalHashtags = finalHashtags.subList(0, 5);
        }
        
        // 4. 格式化為 Hashtag 字串
        return formatHashtags(finalHashtags);
    }
    
    /**
     * 從使用者查詢中提取重要詞彙
     * @param userQuery 使用者查詢字串
     * @param keywords 所有 domain keywords
     * @param topN 取前 N 個（預設 2）
     * @return 提取的關鍵詞列表
     */
    private List<String> extractUserKeywords(String userQuery, ArrayList<Keyword> keywords, int topN) {
        if (userQuery == null || userQuery.trim().isEmpty()) {
            return new ArrayList<>();
        }
        
        List<String> extracted = new ArrayList<>();
        String lowerQuery = userQuery.toLowerCase();
        
        // 找出查詢中包含的 domain keywords
        List<Keyword> matchedKeywords = new ArrayList<>();
        for (Keyword keyword : keywords) {
            if (lowerQuery.contains(keyword.name.toLowerCase())) {
                matchedKeywords.add(keyword);
            }
        }
        
        // 按權重排序
        matchedKeywords.sort((k1, k2) -> Double.compare(k2.weight, k1.weight));
        
        // 取前 topN 個
        int limit = Math.min(topN, matchedKeywords.size());
        for (int i = 0; i < limit; i++) {
            String keyword = matchedKeywords.get(i).name;
            if (isValidHashtag(keyword)) {
                extracted.add(keyword);
            }
        }
        
        return extracted;
    }
    
    /**
     * 從網站內容中提取出現次數最多的 Top N 關鍵字
     * @param content 網站內容
     * @param keywords 所有 domain keywords
     * @param topN 取前 N 個（預設 3）
     * @return Top N 關鍵字列表
     */
    private List<String> extractTopKeywordsFromContent(String content, 
                                                       ArrayList<Keyword> keywords, 
                                                       int topN) {
        if (content == null || content.isEmpty()) {
            return new ArrayList<>();
        }
        
        // 計算每個關鍵字的出現次數
        Map<String, Integer> keywordCounts = new HashMap<>();
        String lowerContent = content.toLowerCase();
        
        for (Keyword keyword : keywords) {
            String lowerKeyword = keyword.name.toLowerCase();
            int count = countOccurrences(lowerContent, lowerKeyword);
            if (count > 0) {
                keywordCounts.put(keyword.name, count);
            }
        }
        
        // 按出現次數降序排序
        List<Map.Entry<String, Integer>> sortedEntries = new ArrayList<>(keywordCounts.entrySet());
        sortedEntries.sort((e1, e2) -> e2.getValue().compareTo(e1.getValue()));
        
        // 取前 topN 個
        List<String> topKeywords = new ArrayList<>();
        int limit = Math.min(topN, sortedEntries.size());
        
        for (int i = 0; i < limit; i++) {
            String keyword = sortedEntries.get(i).getKey();
            if (isValidHashtag(keyword)) {
                topKeywords.add(keyword);
            }
        }
        
        return topKeywords;
    }
    
    /**
     * 計算關鍵字在文字中出現的次數
     * @param text 文字內容
     * @param keyword 關鍵字
     * @return 出現次數
     */
    private int countOccurrences(String text, String keyword) {
        int count = 0;
        int index = 0;
        
        while ((index = text.indexOf(keyword, index)) != -1) {
            count++;
            index += keyword.length();
        }
        
        return count;
    }
    
    /**
     * 驗證 Hashtag 是否有效
     * 過濾條件：
     * - 不是停用詞
     * - 長度在 MIN 和 MAX 之間
     * - 不包含特殊符號（只允許中英文和數字）
     * 
     * @param hashtag 要驗證的 Hashtag
     * @return true 如果有效
     */
    private boolean isValidHashtag(String hashtag) {
        if (hashtag == null || hashtag.trim().isEmpty()) {
            return false;
        }
        
        String trimmed = hashtag.trim();
        
        // 檢查長度
        if (trimmed.length() < MIN_HASHTAG_LENGTH || trimmed.length() > MAX_HASHTAG_LENGTH) {
            return false;
        }
        
        // 檢查是否為停用詞
        if (STOP_WORDS.contains(trimmed.toLowerCase())) {
            return false;
        }
        
        // 檢查是否只包含中英文和數字（不允許特殊符號）
        Pattern validPattern = Pattern.compile("^[\\u4e00-\\u9fa5a-zA-Z0-9]+$");
        if (!validPattern.matcher(trimmed).matches()) {
            return false;
        }
        
        return true;
    }
    
    /**
     * 格式化 Hashtags 為字串
     * @param hashtags Hashtag 列表
     * @return 格式化的字串（例：#不限時 #安靜 #咖啡）
     */
    private String formatHashtags(List<String> hashtags) {
        if (hashtags == null || hashtags.isEmpty()) {
            return "";
        }
        
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < hashtags.size(); i++) {
            sb.append("#").append(hashtags.get(i));
            if (i < hashtags.size() - 1) {
                sb.append(" ");
            }
        }
        
        return sb.toString();
    }
    
    /**
     * 新增自訂停用詞
     * @param stopWord 停用詞
     */
    public void addStopWord(String stopWord) {
        if (stopWord != null && !stopWord.trim().isEmpty()) {
            STOP_WORDS.add(stopWord.toLowerCase());
        }
    }
    
    /**
     * 批次新增停用詞
     * @param stopWords 停用詞列表
     */
    public void addStopWords(List<String> stopWords) {
        if (stopWords != null) {
            for (String word : stopWords) {
                addStopWord(word);
            }
        }
    }
    
    /**
     * 取得目前的停用詞列表
     * @return 停用詞列表
     */
    public Set<String> getStopWords() {
        return new HashSet<>(STOP_WORDS);
    }
    
    /**
     * 清除所有停用詞（用於測試）
     */
    public void clearStopWords() {
        STOP_WORDS.clear();
    }
    
    /**
     * 重設停用詞為預設值
     */
    public void resetStopWords() {
        STOP_WORDS.clear();
        STOP_WORDS.addAll(Arrays.asList(
            "的", "是", "在", "有", "和", "與", "或", "但", "了", "著", "也", "就", "都",
            "而", "及", "等", "這", "那", "我", "你", "他", "她", "它", "們", "會", "能",
            "可", "要", "不", "沒", "很", "更", "最", "些", "個", "裡", "上", "下"
        ));
    }
}

