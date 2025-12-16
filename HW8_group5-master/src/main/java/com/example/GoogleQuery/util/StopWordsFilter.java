package com.example.GoogleQuery.util;

import java.util.*;
import java.io.*;

/**
 * StopWordsFilter - 停用詞過濾器
 * 用於過濾無意義的常見詞彙（如：的、是、在等）
 */
public class StopWordsFilter {
    
    // 停用詞集合
    private static final Set<String> STOP_WORDS = new HashSet<>();
    
    // 靜態初始化停用詞
    static {
        initializeDefaultStopWords();
    }
    
    /**
     * 初始化預設停用詞
     */
    private static void initializeDefaultStopWords() {
        // 中文停用詞
        String[] chineseStopWords = {
            "的", "是", "在", "有", "和", "與", "或", "但", "了", "著", 
            "也", "就", "都", "而", "及", "等", "這", "那", "我", "你", 
            "他", "她", "它", "們", "會", "能", "可", "要", "不", "沒", 
            "很", "更", "最", "些", "個", "裡", "上", "下", "中", "大", 
            "小", "來", "去", "出", "到", "把", "被", "對", "從", "讓",
            "給", "為", "以", "將", "用", "又", "因", "所", "其", "於"
        };
        
        // 英文停用詞
        String[] englishStopWords = {
            "a", "an", "the", "is", "are", "was", "were", "be", "been", "being",
            "have", "has", "had", "do", "does", "did", "will", "would", "should",
            "could", "may", "might", "must", "can", "of", "to", "for", "with",
            "on", "at", "by", "from", "in", "into", "through", "during", "before",
            "after", "above", "below", "between", "under", "over", "about", "against",
            "as", "or", "and", "but", "if", "then", "than", "so", "such", "no",
            "not", "only", "own", "same", "too", "very", "just", "where", "when",
            "why", "how", "all", "each", "every", "both", "few", "more", "most",
            "other", "some", "any", "there", "here", "this", "that", "these", "those"
        };
        
        // 加入停用詞
        STOP_WORDS.addAll(Arrays.asList(chineseStopWords));
        STOP_WORDS.addAll(Arrays.asList(englishStopWords));
    }
    
    /**
     * 過濾停用詞（單一字串）
     * @param text 文字
     * @return 過濾後的文字
     */
    public static String filter(String text) {
        if (text == null || text.isEmpty()) {
            return "";
        }
        
        String[] words = text.split("\\s+");
        List<String> filtered = new ArrayList<>();
        
        for (String word : words) {
            String lowerWord = word.toLowerCase().trim();
            if (!STOP_WORDS.contains(lowerWord) && !lowerWord.isEmpty()) {
                filtered.add(word);
            }
        }
        
        return String.join(" ", filtered);
    }
    
    /**
     * 過濾停用詞（詞彙列表）
     * @param tokens 詞彙列表
     * @return 過濾後的列表
     */
    public static List<String> filter(List<String> tokens) {
        if (tokens == null || tokens.isEmpty()) {
            return new ArrayList<>();
        }
        
        List<String> filtered = new ArrayList<>();
        
        for (String token : tokens) {
            String lowerToken = token.toLowerCase().trim();
            if (!STOP_WORDS.contains(lowerToken) && !lowerToken.isEmpty()) {
                filtered.add(token);
            }
        }
        
        return filtered;
    }
    
    /**
     * 檢查是否為停用詞
     * @param word 詞彙
     * @return true 如果是停用詞
     */
    public static boolean isStopWord(String word) {
        if (word == null || word.isEmpty()) {
            return true;
        }
        return STOP_WORDS.contains(word.toLowerCase().trim());
    }
    
    /**
     * 新增停用詞
     * @param word 停用詞
     */
    public static void addStopWord(String word) {
        if (word != null && !word.isEmpty()) {
            STOP_WORDS.add(word.toLowerCase().trim());
        }
    }
    
    /**
     * 批次新增停用詞
     * @param words 停用詞列表
     */
    public static void addStopWords(List<String> words) {
        if (words != null) {
            for (String word : words) {
                addStopWord(word);
            }
        }
    }
    
    /**
     * 移除停用詞
     * @param word 停用詞
     */
    public static void removeStopWord(String word) {
        if (word != null) {
            STOP_WORDS.remove(word.toLowerCase().trim());
        }
    }
    
    /**
     * 從檔案載入停用詞
     * @param filePath 檔案路徑
     * @throws IOException 如果讀取失敗
     */
    public static void loadFromFile(String filePath) throws IOException {
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String word = line.trim();
                if (!word.isEmpty() && !word.startsWith("#")) {  // 忽略空行和註解
                    addStopWord(word);
                }
            }
        }
    }
    
    /**
     * 儲存停用詞到檔案
     * @param filePath 檔案路徑
     * @throws IOException 如果寫入失敗
     */
    public static void saveToFile(String filePath) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
            for (String word : STOP_WORDS) {
                writer.write(word);
                writer.newLine();
            }
        }
    }
    
    /**
     * 取得所有停用詞
     * @return 停用詞集合
     */
    public static Set<String> getAllStopWords() {
        return new HashSet<>(STOP_WORDS);
    }
    
    /**
     * 取得停用詞數量
     * @return 停用詞數量
     */
    public static int getStopWordsCount() {
        return STOP_WORDS.size();
    }
    
    /**
     * 清空所有停用詞
     */
    public static void clearAll() {
        STOP_WORDS.clear();
    }
    
    /**
     * 重設為預設停用詞
     */
    public static void resetToDefault() {
        STOP_WORDS.clear();
        initializeDefaultStopWords();
    }
    
    /**
     * 計算文字中停用詞的比例
     * @param text 文字
     * @return 停用詞比例（0-1）
     */
    public static double calculateStopWordRatio(String text) {
        if (text == null || text.isEmpty()) {
            return 0.0;
        }
        
        String[] words = text.split("\\s+");
        if (words.length == 0) {
            return 0.0;
        }
        
        int stopWordCount = 0;
        for (String word : words) {
            if (isStopWord(word)) {
                stopWordCount++;
            }
        }
        
        return (double) stopWordCount / words.length;
    }
    
    /**
     * 過濾並統計（返回過濾結果和統計資訊）
     * @param tokens 詞彙列表
     * @return Map，包含 "filtered" 和 "stats"
     */
    public static Map<String, Object> filterWithStats(List<String> tokens) {
        if (tokens == null || tokens.isEmpty()) {
            Map<String, Object> result = new HashMap<>();
            result.put("filtered", new ArrayList<String>());
            result.put("stats", new HashMap<String, Integer>() {{
                put("original", 0);
                put("filtered", 0);
                put("removed", 0);
            }});
            return result;
        }
        
        List<String> filtered = filter(tokens);
        
        Map<String, Integer> stats = new HashMap<>();
        stats.put("original", tokens.size());
        stats.put("filtered", filtered.size());
        stats.put("removed", tokens.size() - filtered.size());
        
        Map<String, Object> result = new HashMap<>();
        result.put("filtered", filtered);
        result.put("stats", stats);
        
        return result;
    }
}

