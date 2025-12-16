package com.example.GoogleQuery.model;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/**
 * WordCounter - 字數統計工具
 * 從網頁 URL 抓取內容並計算關鍵字出現次數
 */
public class WordCounter {
    
    private String urlStr;           // 網頁 URL
    private String content;          // 網頁內容（純文字）
    private Map<String, Integer> wordCountCache;  // 關鍵字計數快取
    
    // HTTP 請求設定
    private static final int TIMEOUT = 5000;  // 5 秒超時
    private static final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36";
    
    /**
     * 建構子
     * @param urlStr 網頁 URL
     */
    public WordCounter(String urlStr) {
        this.urlStr = urlStr;
        this.content = "";
        this.wordCountCache = new HashMap<>();
        
        // 自動抓取內容
        try {
            this.content = fetchContent();
        } catch (Exception e) {
            System.err.println("無法抓取網頁內容: " + urlStr);
            // 內容保持為空字串
        }
    }
    
    /**
     * 建構子（直接提供內容）
     * @param urlStr 網頁 URL
     * @param content 網頁內容
     */
    public WordCounter(String urlStr, String content) {
        this.urlStr = urlStr;
        this.content = content != null ? content : "";
        this.wordCountCache = new HashMap<>();
    }
    
    /**
     * 從網頁抓取內容
     * @return 網頁純文字內容
     * @throws IOException 如果抓取失敗
     */
    private String fetchContent() throws IOException {
        if (urlStr == null || urlStr.isEmpty()) {
            return "";
        }
        
        StringBuilder contentBuilder = new StringBuilder();
        
        try {
            URL url = new URL(urlStr);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            
            // 設定請求參數
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(TIMEOUT);
            connection.setReadTimeout(TIMEOUT);
            connection.setRequestProperty("User-Agent", USER_AGENT);
            
            // 讀取回應
            int responseCode = connection.getResponseCode();
            
            if (responseCode == HttpURLConnection.HTTP_OK) {
                BufferedReader reader = new BufferedReader(
                    new InputStreamReader(connection.getInputStream(), "UTF-8")
                );
                
                String line;
                while ((line = reader.readLine()) != null) {
                    contentBuilder.append(line).append("\n");
                }
                
                reader.close();
            } else {
                System.err.println("HTTP 錯誤: " + responseCode + " for URL: " + urlStr);
            }
            
            connection.disconnect();
            
        } catch (IOException e) {
            throw new IOException("抓取網頁失敗: " + urlStr, e);
        }
        
        // 移除 HTML 標籤（簡單版本）
        String rawContent = contentBuilder.toString();
        return removeHtmlTags(rawContent);
    }
    
    /**
     * 移除 HTML 標籤（簡單實作）
     * 實際專案中應使用 Jsoup
     * @param html HTML 內容
     * @return 純文字內容
     */
    private String removeHtmlTags(String html) {
        if (html == null || html.isEmpty()) {
            return "";
        }
        
        // 移除 script 和 style 標籤及其內容
        String text = html.replaceAll("<script[^>]*>.*?</script>", "");
        text = text.replaceAll("<style[^>]*>.*?</style>", "");
        
        // 移除所有 HTML 標籤
        text = text.replaceAll("<[^>]+>", " ");
        
        // 移除多餘空白
        text = text.replaceAll("\\s+", " ");
        
        // 移除 HTML 實體
        text = text.replaceAll("&nbsp;", " ");
        text = text.replaceAll("&[a-z]+;", " ");
        
        return text.trim();
    }
    
    /**
     * 取得網頁內容
     * @return 網頁純文字內容
     */
    public String getContent() {
        return content;
    }
    
    /**
     * 設定網頁內容（用於手動設定）
     * @param content 網頁內容
     */
    public void setContent(String content) {
        this.content = content != null ? content : "";
        this.wordCountCache.clear(); // 清除快取
    }
    
    /**
     * 計算關鍵字出現次數
     * @param keyword 關鍵字
     * @return 出現次數
     */
    public int countKeyword(String keyword) {
        if (keyword == null || keyword.isEmpty()) {
            return 0;
        }
        
        String lowerKeyword = keyword.toLowerCase().trim();
        
        // 檢查快取
        if (wordCountCache.containsKey(lowerKeyword)) {
            return wordCountCache.get(lowerKeyword);
        }
        
        // 計算出現次數
        int count = 0;
        String lowerContent = content.toLowerCase();
        
        // 使用簡單的字串搜尋
        int index = 0;
        while ((index = lowerContent.indexOf(lowerKeyword, index)) != -1) {
            count++;
            index += lowerKeyword.length();
        }
        
        // 存入快取
        wordCountCache.put(lowerKeyword, count);
        
        return count;
    }
    
    /**
     * 批次計算多個關鍵字的出現次數
     * @param keywords 關鍵字陣列
     * @return Map<關鍵字, 出現次數>
     */
    public Map<String, Integer> countKeywords(String[] keywords) {
        Map<String, Integer> results = new HashMap<>();
        
        if (keywords != null) {
            for (String keyword : keywords) {
                int count = countKeyword(keyword);
                results.put(keyword, count);
            }
        }
        
        return results;
    }
    
    /**
     * 取得內容長度（字元數）
     * @return 字元數
     */
    public int getContentLength() {
        return content.length();
    }
    
    /**
     * 取得內容字數（以空白分隔）
     * @return 字數
     */
    public int getWordCount() {
        if (content.isEmpty()) {
            return 0;
        }
        
        String[] words = content.split("\\s+");
        return words.length;
    }
    
    /**
     * 檢查內容是否包含關鍵字
     * @param keyword 關鍵字
     * @return true 如果包含
     */
    public boolean contains(String keyword) {
        return countKeyword(keyword) > 0;
    }
    
    /**
     * 取得 URL
     * @return URL
     */
    public String getUrl() {
        return urlStr;
    }
    
    /**
     * 清除關鍵字計數快取
     */
    public void clearCache() {
        wordCountCache.clear();
    }
    
    /**
     * 取得快取的關鍵字計數
     * @return 快取的 Map
     */
    public Map<String, Integer> getCache() {
        return new HashMap<>(wordCountCache);
    }
    
    /**
     * 重新抓取網頁內容
     * @return true 如果抓取成功
     */
    public boolean refresh() {
        try {
            this.content = fetchContent();
            this.wordCountCache.clear();
            return true;
        } catch (IOException e) {
            System.err.println("重新抓取失敗: " + urlStr);
            return false;
        }
    }
    
    /**
     * 檢查內容是否為空
     * @return true 如果內容為空
     */
    public boolean isEmpty() {
        return content == null || content.trim().isEmpty();
    }
    
    /**
     * 取得內容摘要（前 N 個字元）
     * @param length 摘要長度
     * @return 內容摘要
     */
    public String getSummary(int length) {
        if (content.isEmpty()) {
            return "";
        }
        
        if (content.length() <= length) {
            return content;
        }
        
        return content.substring(0, length) + "...";
    }
    
    /**
     * toString 方法
     */
    @Override
    public String toString() {
        return String.format(
            "WordCounter{url='%s', contentLength=%d, cachedKeywords=%d}",
            urlStr, getContentLength(), wordCountCache.size()
        );
    }
}

