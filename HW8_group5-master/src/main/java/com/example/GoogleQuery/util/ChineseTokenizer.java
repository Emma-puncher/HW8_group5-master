package com.example.GoogleQuery.util;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * ChineseTokenizer - 中文分詞器
 * 簡易版的中文分詞工具（基於規則）
 * 注意：實際專案建議使用 HanLP 或 jieba 等專業分詞工具
 */
public class ChineseTokenizer {
    
    // 常見中文詞彙字典（簡化版）
    private static final Set<String> DICTIONARY = new HashSet<>(Arrays.asList(
        // 咖啡廳相關
        "咖啡", "咖啡廳", "咖啡店", "不限時", "插座", "wifi", "無線網路",
        "舒芙蕾", "拿鐵", "義式", "手沖", "甜點", "早午餐", "輕食",
        
        // 環境相關
        "安靜", "舒適", "文青", "工業風", "日式", "戶外", "座位",
        "環境", "氛圍", "裝潢", "空間", "明亮", "燈光", "音樂",
        
        // 服務相關
        "服務", "態度", "價格", "消費", "低消", "時限", "限時",
        "友善", "寵物", "包場", "預約", "訂位",
        
        // 讀書工作相關
        "讀書", "工作", "自習", "辦公", "會議", "討論", "創作",
        "書桌", "獨立", "隔間", "包廂", "團體"
    ));
    
    // 最大詞長度
    private static final int MAX_WORD_LENGTH = 5;
    
    /**
     * 分詞（主要方法）
     * @param text 要分詞的文字
     * @return 分詞結果列表
     */
    public static List<String> tokenize(String text) {
        if (text == null || text.isEmpty()) {
            return new ArrayList<>();
        }
        
        List<String> tokens = new ArrayList<>();
        
        // 預處理：分離中英文
        List<String> segments = splitChineseAndEnglish(text);
        
        for (String segment : segments) {
            if (isChinese(segment)) {
                // 中文分詞
                tokens.addAll(tokenizeChinese(segment));
            } else if (isEnglish(segment)) {
                // 英文按空白分割
                String[] words = segment.toLowerCase().split("\\s+");
                tokens.addAll(Arrays.asList(words));
            }
        }
        
        return tokens;
    }
    
    /**
     * 中文分詞（使用最大匹配算法）
     * @param text 中文文字
     * @return 分詞結果
     */
    private static List<String> tokenizeChinese(String text) {
        List<String> result = new ArrayList<>();
        
        int i = 0;
        while (i < text.length()) {
            boolean matched = false;
            
            // 從最長詞開始匹配
            for (int len = Math.min(MAX_WORD_LENGTH, text.length() - i); len >= 1; len--) {
                String word = text.substring(i, i + len);
                
                if (DICTIONARY.contains(word)) {
                    result.add(word);
                    i += len;
                    matched = true;
                    break;
                }
            }
            
            // 如果沒有匹配到，以單字處理
            if (!matched) {
                result.add(String.valueOf(text.charAt(i)));
                i++;
            }
        }
        
        return result;
    }
    
    /**
     * 分離中英文（預處理）
     * @param text 混合文字
     * @return 分段列表
     */
    private static List<String> splitChineseAndEnglish(String text) {
        List<String> segments = new ArrayList<>();
        
        Pattern pattern = Pattern.compile("([\\u4e00-\\u9fa5]+)|([a-zA-Z]+)|([0-9]+)");
        Matcher matcher = pattern.matcher(text);
        
        while (matcher.find()) {
            segments.add(matcher.group());
        }
        
        return segments;
    }
    
    /**
     * 判斷是否為中文
     * @param text 文字
     * @return true 如果是中文
     */
    private static boolean isChinese(String text) {
        if (text == null || text.isEmpty()) {
            return false;
        }
        return text.matches("[\\u4e00-\\u9fa5]+");
    }
    
    /**
     * 判斷是否為英文
     * @param text 文字
     * @return true 如果是英文
     */
    private static boolean isEnglish(String text) {
        if (text == null || text.isEmpty()) {
            return false;
        }
        return text.matches("[a-zA-Z\\s]+");
    }
    
    /**
     * 新增自訂詞彙到字典
     * @param word 詞彙
     */
    public static void addWord(String word) {
        if (word != null && !word.isEmpty()) {
            DICTIONARY.add(word);
        }
    }
    
    /**
     * 批次新增詞彙
     * @param words 詞彙列表
     */
    public static void addWords(List<String> words) {
        if (words != null) {
            DICTIONARY.addAll(words);
        }
    }
    
    /**
     * 移除詞彙
     * @param word 詞彙
     */
    public static void removeWord(String word) {
        DICTIONARY.remove(word);
    }
    
    /**
     * 檢查詞彙是否在字典中
     * @param word 詞彙
     * @return true 如果在字典中
     */
    public static boolean containsWord(String word) {
        return DICTIONARY.contains(word);
    }
    
    /**
     * 取得字典大小
     * @return 詞彙數量
     */
    public static int getDictionarySize() {
        return DICTIONARY.size();
    }
    
    /**
     * 清空字典
     */
    public static void clearDictionary() {
        DICTIONARY.clear();
    }
    
    /**
     * 分詞並計算詞頻
     * @param text 文字
     * @return Map<詞彙, 出現次數>
     */
    public static Map<String, Integer> tokenizeWithFrequency(String text) {
        List<String> tokens = tokenize(text);
        Map<String, Integer> frequency = new HashMap<>();
        
        for (String token : tokens) {
            frequency.put(token, frequency.getOrDefault(token, 0) + 1);
        }
        
        return frequency;
    }
    
    /**
     * 分詞並過濾停用詞
     * @param text 文字
     * @return 過濾後的分詞結果
     */
    public static List<String> tokenizeWithStopWords(String text) {
        List<String> tokens = tokenize(text);
        return StopWordsFilter.filter(tokens);
    }
}

