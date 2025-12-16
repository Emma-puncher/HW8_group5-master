package com.example.GoogleQuery.service;

import com.example.GoogleQuery.model.WebPage;
import com.example.GoogleQuery.model.WebNode;
import com.example.GoogleQuery.model.WebTree;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * GoogleService - Google 搜尋服務
 * 提供搜尋、網頁抓取等功能的高階封裝
 * 這是 Spring Boot 服務層，整合 GoogleQuery 的功能
 */
@Service
public class GoogleService {
    
    /**
     * 執行 Google 搜尋
     * @param keyword 搜尋關鍵字
     * @return 搜尋結果 Map（標題 -> URL）
     * @throws IOException
     */
    public HashMap<String, String> search(String keyword) throws IOException {
        GoogleQuery googleQuery = new GoogleQuery(keyword);
        return googleQuery.getSearchResults();
    }
    
    /**
     * 搜尋並建立 WebPage 物件列表
     * @param keyword 搜尋關鍵字
     * @return WebPage 列表
     */
    public ArrayList<WebPage> searchAndCreatePages(String keyword) {
        try {
            HashMap<String, String> searchResults = search(keyword);
            return GoogleQuery.createWebPages(searchResults);
            
        } catch (IOException e) {
            System.out.println("搜尋錯誤: " + e.getMessage());
            return new ArrayList<>();
        }
    }
    
    /**
     * 從指定 URL 建立 WebPage
     * @param url 網頁 URL
     * @param name 網頁名稱
     * @return WebPage 物件
     */
    public WebPage createWebPage(String url, String name) {
        return GoogleQuery.createWebPage(url, name);
    }
    
    /**
     * 批次建立 WebPage
     * @param cafeDataList 咖啡廳資料列表（包含 url 和 name）
     * @return WebPage 列表
     */
    public ArrayList<WebPage> createWebPagesFromList(List<Map<String, String>> cafeDataList) {
        ArrayList<WebPage> webPages = new ArrayList<>();
        
        for (Map<String, String> cafeData : cafeDataList) {
            String url = cafeData.get("url");
            String name = cafeData.get("name");
            
            if (url != null && name != null) {
                WebPage page = GoogleQuery.createWebPage(url, name);
                if (page != null && !page.getContent().isEmpty()) {
                    webPages.add(page);
                }
            }
        }
        
        return webPages;
    }
    
    /**
     * 抓取網頁內容
     * @param url 網頁 URL
     * @return HTML 內容
     */
    public String fetchContent(String url) {
        return GoogleQuery.fetchContent(url);
    }
    
    /**
     * 從 HTML 提取純文字
     * @param htmlContent HTML 內容
     * @return 純文字
     */
    public String extractText(String htmlContent) {
        return GoogleQuery.extractText(htmlContent);
    }
    
    /**
     * 抓取網頁並提取文字
     * @param url 網頁 URL
     * @return 純文字內容
     */
    public String fetchAndExtractText(String url) {
        String htmlContent = fetchContent(url);
        return extractText(htmlContent);
    }
    
    /**
     * 建立網站樹狀結構
     * @param rootUrl 根 URL
     * @param rootName 根名稱
     * @param depth 深度
     * @return WebTree 物件
     */
    public WebTree buildWebTree(String rootUrl, String rootName, int depth) {
        WebNode rootNode = GoogleQuery.buildWebTree(rootUrl, rootName, depth);
        return new WebTree(rootNode);
    }
    
    /**
     * 抓取子連結
     * @param url 網頁 URL
     * @param maxLinks 最大連結數
     * @return 子連結列表
     */
    public ArrayList<String> fetchSubLinks(String url, int maxLinks) {
        return GoogleQuery.fetchSubLinks(url, maxLinks);
    }
    
    /**
     * 檢查 URL 是否可訪問
     * @param url URL
     * @return 是否可訪問
     */
    public boolean isUrlAccessible(String url) {
        return GoogleQuery.isUrlAccessible(url);
    }
    
    /**
     * 獲取網頁標題
     * @param url URL
     * @return 網頁標題
     */
    public String getPageTitle(String url) {
        return GoogleQuery.getPageTitle(url);
    }
    
    /**
     * 獲取網頁描述
     * @param url URL
     * @return 網頁描述
     */
    public String getPageDescription(String url) {
        return GoogleQuery.getPageDescription(url);
    }
    
    /**
     * 批次檢查 URL 可訪問性
     * @param urls URL 列表
     * @return Map（URL -> 是否可訪問）
     */
    public Map<String, Boolean> checkUrlsAccessibility(List<String> urls) {
        Map<String, Boolean> results = new HashMap<>();
        
        for (String url : urls) {
            results.put(url, isUrlAccessible(url));
        }
        
        return results;
    }
    
    /**
     * 批次獲取網頁資訊（標題和描述）
     * @param urls URL 列表
     * @return Map（URL -> 網頁資訊）
     */
    public Map<String, Map<String, String>> getPageInfoBatch(List<String> urls) {
        Map<String, Map<String, String>> results = new HashMap<>();
        
        for (String url : urls) {
            Map<String, String> info = new HashMap<>();
            info.put("title", getPageTitle(url));
            info.put("description", getPageDescription(url));
            info.put("accessible", String.valueOf(isUrlAccessible(url)));
            
            results.put(url, info);
        }
        
        return results;
    }
    
    /**
     * 更新 WebPage 的內容（重新抓取）
     * @param webPage WebPage 物件
     * @return 更新後的 WebPage
     */
    public WebPage refreshWebPage(WebPage webPage) {
        String url = webPage.getUrl();
        String name = webPage.getName();
        
        return GoogleQuery.createWebPage(url, name);
    }
    
    /**
     * 驗證網頁內容完整性
     * @param webPage WebPage 物件
     * @return 是否有效（內容不為空）
     */
    public boolean isWebPageValid(WebPage webPage) {
        return webPage != null 
            && webPage.getContent() != null 
            && !webPage.getContent().isEmpty()
            && webPage.getContent().length() > 100; // 至少 100 字元
    }
    
    /**
     * 過濾無效的 WebPage
     * @param webPages WebPage 列表
     * @return 過濾後的有效 WebPage 列表
     */
    public ArrayList<WebPage> filterValidWebPages(ArrayList<WebPage> webPages) {
        ArrayList<WebPage> validPages = new ArrayList<>();
        
        for (WebPage page : webPages) {
            if (isWebPageValid(page)) {
                validPages.add(page);
            }
        }
        
        return validPages;
    }
    
    /**
     * 獲取網頁內容預覽（前 N 個字元）
     * @param webPage WebPage 物件
     * @param length 預覽長度
     * @return 預覽文字
     */
    public String getContentPreview(WebPage webPage, int length) {
        String content = webPage.getContent();
        
        if (content == null || content.isEmpty()) {
            return "無內容";
        }
        
        if (content.length() <= length) {
            return content;
        }
        
        return content.substring(0, length) + "...";
    }
    
    /**
     * 統計網頁字數
     * @param webPage WebPage 物件
     * @return 字數統計
     */
    public int countWords(WebPage webPage) {
        String content = webPage.getContent();
        
        if (content == null || content.isEmpty()) {
            return 0;
        }
        
        // 移除多餘空白並分割
        String[] words = content.trim().split("\\s+");
        return words.length;
    }
    
    /**
     * 批次統計字數
     * @param webPages WebPage 列表
     * @return Map（WebPage 名稱 -> 字數）
     */
    public Map<String, Integer> countWordsBatch(ArrayList<WebPage> webPages) {
        Map<String, Integer> wordCounts = new HashMap<>();
        
        for (WebPage page : webPages) {
            wordCounts.put(page.getName(), countWords(page));
        }
        
        return wordCounts;
    }
    
    /**
     * 檢查網頁是否包含特定關鍵字
     * @param webPage WebPage 物件
     * @param keyword 關鍵字
     * @return 是否包含
     */
    public boolean containsKeyword(WebPage webPage, String keyword) {
        String content = webPage.getContent();
        
        if (content == null || content.isEmpty()) {
            return false;
        }
        
        return content.toLowerCase().contains(keyword.toLowerCase());
    }
    
    /**
     * 計算關鍵字出現次數
     * @param webPage WebPage 物件
     * @param keyword 關鍵字
     * @return 出現次數
     */
    public int countKeywordOccurrences(WebPage webPage, String keyword) {
        String content = webPage.getContent();
        
        if (content == null || content.isEmpty()) {
            return 0;
        }
        
        String lowerContent = content.toLowerCase();
        String lowerKeyword = keyword.toLowerCase();
        
        int count = 0;
        int index = 0;
        
        while ((index = lowerContent.indexOf(lowerKeyword, index)) != -1) {
            count++;
            index += lowerKeyword.length();
        }
        
        return count;
    }
    
    /**
     * 批次計算關鍵字出現次數
     * @param webPage WebPage 物件
     * @param keywords 關鍵字列表
     * @return Map（關鍵字 -> 出現次數）
     */
    public Map<String, Integer> countKeywordsBatch(WebPage webPage, List<String> keywords) {
        Map<String, Integer> counts = new HashMap<>();
        
        for (String keyword : keywords) {
            counts.put(keyword, countKeywordOccurrences(webPage, keyword));
        }
        
        return counts;
    }
    
    /**
     * 從多個 WebPage 中找出包含最多關鍵字的頁面
     * @param webPages WebPage 列表
     * @param keyword 關鍵字
     * @return 包含最多關鍵字的 WebPage
     */
    public WebPage findPageWithMostKeywords(ArrayList<WebPage> webPages, String keyword) {
        WebPage bestPage = null;
        int maxCount = 0;
        
        for (WebPage page : webPages) {
            int count = countKeywordOccurrences(page, keyword);
            if (count > maxCount) {
                maxCount = count;
                bestPage = page;
            }
        }
        
        return bestPage;
    }
    
    /**
     * 取得服務狀態資訊
     * @return 狀態資訊 Map
     */
    public Map<String, Object> getServiceStatus() {
        Map<String, Object> status = new HashMap<>();
        status.put("service", "GoogleService");
        status.put("status", "running");
        status.put("timestamp", System.currentTimeMillis());
        status.put("features", List.of(
            "search", "fetch_content", "create_pages", 
            "build_tree", "keyword_analysis"
        ));
        
        return status;
    }
}

