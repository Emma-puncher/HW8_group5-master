package com.example.GoogleQuery.core;

import java.util.*;
import java.util.regex.*;
import com.example.GoogleQuery.model.*;

/**
 * KeywordParser - 負責解析網頁文字並計算關鍵字出現次數
 * 支援中英文混合文字、多語系處理
 */
public class KeywordParser {
    
    private String content;
    private Map<String, Integer> keywordCountMap;
    
    /**
     * 建構子
     * @param content 要解析的網頁文字內容
     */
    public KeywordParser(String content) {
        this.content = content != null ? content.toLowerCase() : "";
        this.keywordCountMap = new HashMap<>();
    }
    
    /**
     * 計算單一關鍵字在文字中出現的次數
     * @param keyword 要搜尋的關鍵字
     * @return 出現次數
     */
    public int countKeyword(String keyword) {
        if (keyword == null || keyword.isEmpty()) {
            return 0;
        }
        
        String lowerKeyword = keyword.toLowerCase().trim();
        
        // 如果已經計算過，直接返回
        if (keywordCountMap.containsKey(lowerKeyword)) {
            return keywordCountMap.get(lowerKeyword);
        }
        
        int count = 0;
        
        // 方法1: 使用正則表達式（適用於英文單字邊界）
        if (isEnglishWord(lowerKeyword)) {
            Pattern pattern = Pattern.compile("\\b" + Pattern.quote(lowerKeyword) + "\\b");
            Matcher matcher = pattern.matcher(content);
            while (matcher.find()) {
                count++;
            }
        } else {
            // 方法2: 直接字串搜尋（適用於中文或混合文字）
            int index = 0;
            while ((index = content.indexOf(lowerKeyword, index)) != -1) {
                count++;
                index += lowerKeyword.length();
            }
        }
        
        keywordCountMap.put(lowerKeyword, count);
        return count;
    }
    
    /**
     * 批次計算多個關鍵字的出現次數
     * @param keywords 關鍵字列表
     * @return Map<關鍵字, 出現次數>
     */
    public Map<String, Integer> countAllKeywords(ArrayList<Keyword> keywords) {
        Map<String, Integer> result = new HashMap<>();
        
        for (Keyword keyword : keywords) {
            int count = countKeyword(keyword.name);
            result.put(keyword.name, count);
        }
        
        return result;
    }
    
    /**
     * 計算加權後的總分數
     * @param keywords 帶權重的關鍵字列表
     * @return 總分數（次數 × 權重 的總和）
     */
    public double calculateWeightedScore(ArrayList<Keyword> keywords) {
        double totalScore = 0.0;
        
        for (Keyword keyword : keywords) {
            int count = countKeyword(keyword.name);
            double weightedScore = count * keyword.weight;
            totalScore += weightedScore;
        }
        
        return totalScore;
    }
    
    /**
     * 取得前 N 個出現次數最多的關鍵字（用於生成 Hashtags）
     * @param keywords 關鍵字列表
     * @param topN 要取前幾名（通常是 2-3）
     * @return 排序後的前 N 個關鍵字列表
     */
    public List<Keyword> getTopKeywords(ArrayList<Keyword> keywords, int topN) {
        // 計算所有關鍵字的出現次數
        Map<String, Integer> counts = countAllKeywords(keywords);
        
        // 建立關鍵字與次數的配對列表
        List<Map.Entry<String, Integer>> sortedList = new ArrayList<>(counts.entrySet());
        
        // 按出現次數降序排序
        sortedList.sort((a, b) -> b.getValue().compareTo(a.getValue()));
        
        // 取前 N 個
        List<Keyword> topKeywords = new ArrayList<>();
        int limit = Math.min(topN, sortedList.size());
        
        for (int i = 0; i < limit; i++) {
            String keywordName = sortedList.get(i).getKey();
            // 從原始列表找到對應的 Keyword 物件
            for (Keyword kw : keywords) {
                if (kw.name.equalsIgnoreCase(keywordName)) {
                    topKeywords.add(kw);
                    break;
                }
            }
        }
        
        return topKeywords;
    }
    
    /**
     * 生成 Hashtags 字串（用於前端顯示）
     * @param keywords 關鍵字列表
     * @param topN 取前幾名
     * @return Hashtag 字串（例如：#舒芙蕾 #不限時 #台北咖啡）
     */
    public String generateHashtags(ArrayList<Keyword> keywords, int topN) {
        List<Keyword> topKeywords = getTopKeywords(keywords, topN);
        
        StringBuilder hashtags = new StringBuilder();
        for (int i = 0; i < topKeywords.size(); i++) {
            hashtags.append("#").append(topKeywords.get(i).name);
            if (i < topKeywords.size() - 1) {
                hashtags.append(" ");
            }
        }
        
        return hashtags.toString();
    }
    
    /**
     * 取得詳細的關鍵字分析結果（用於 Debug）
     * @param keywords 關鍵字列表
     * @return 格式化的分析字串
     */
    public String getDetailedAnalysis(ArrayList<Keyword> keywords) {
        StringBuilder analysis = new StringBuilder();
        analysis.append("=== 關鍵字分析 ===\n");
        
        for (Keyword keyword : keywords) {
            int count = countKeyword(keyword.name);
            double score = count * keyword.weight;
            analysis.append(String.format("%-15s | 次數: %3d | 權重: %.2f | 得分: %.2f\n", 
                keyword.name, count, keyword.weight, score));
        }
        
        double totalScore = calculateWeightedScore(keywords);
        analysis.append(String.format("\n總分: %.2f\n", totalScore));
        analysis.append("Top Hashtags: ").append(generateHashtags(keywords, 3)).append("\n");
        
        return analysis.toString();
    }
    
    /**
     * 判斷是否為純英文單字
     * @param word 要判斷的字串
     * @return true 如果是純英文
     */
    private boolean isEnglishWord(String word) {
        return word.matches("^[a-zA-Z]+$");
    }
    
    /**
     * 清除快取（當內容更新時使用）
     */
    public void clearCache() {
        keywordCountMap.clear();
    }
    
    /**
     * 取得內容長度
     * @return 文字內容的字元數
     */
    public int getContentLength() {
        return content.length();
    }
    
    /**
     * 取得已計算過的關鍵字快取
     * @return 關鍵字計數 Map（唯讀）
     */
    public Map<String, Integer> getKeywordCountCache() {
        return Collections.unmodifiableMap(keywordCountMap);
    }
}

