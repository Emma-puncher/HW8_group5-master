package com.example.GoogleQuery.service;

import com.example.GoogleQuery.model.WebPage;
import com.example.GoogleQuery.model.WebNode;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * GoogleQuery - 網頁查詢與內容抓取類別
 * 負責從網頁 URL 抓取內容並解析
 */
public class GoogleQuery {
    
    private String searchKeyword;
    private String url;
    private String content;
    
    /**
     * 建構子
     * @param searchKeyword 搜尋關鍵字
     */
    public GoogleQuery(String searchKeyword) {
        this.searchKeyword = searchKeyword;
        this.url = "https://www.google.com/search?q=" + searchKeyword + "&oe=utf8&num=20";
    }
    
    /**
     * 從 Google 搜尋結果頁面抓取內容
     * @return HashMap 包含標題和 URL
     * @throws IOException
     */
    private HashMap<String, String> query() throws IOException {
        String retVal = "";
        String line = "";
        
        try {
            URL u = new URL(url);
            URLConnection conn = u.openConnection();
            conn.setRequestProperty("User-agent", "Chrome/107.0.5304.107");
            InputStream in = conn.getInputStream();
            InputStreamReader inReader = new InputStreamReader(in, "utf-8");
            BufferedReader bufReader = new BufferedReader(inReader);
            
            while ((line = bufReader.readLine()) != null) {
                retVal += line;
            }
            in.close();
        } catch (Exception e) {
            System.out.println("Google Query 錯誤: " + e.getMessage());
        }
        
        content = retVal;
        return parseContent(content);
    }
    
    /**
     * 解析 Google 搜尋結果頁面
     * @param content HTML 內容
     * @return HashMap 包含標題和對應的 URL
     */
    private HashMap<String, String> parseContent(String content) {
        HashMap<String, String> retVal = new HashMap<>();
        
        try {
            Document doc = Jsoup.parse(content);
            
            // 找出所有搜尋結果的 <a> 標籤
            Elements lis = doc.select("div.kCrYT > a");
            
            for (Element li : lis) {
                try {
                    String citeUrl = li.attr("href");
                    String title = li.select("h3").text();
                    
                    if (title.isEmpty()) {
                        continue;
                    }
                    
                    // 過濾掉不是 http/https 開頭的 URL
                    if (citeUrl.startsWith("/url?q=")) {
                        citeUrl = citeUrl.substring(7);
                        citeUrl = citeUrl.split("&")[0];
                    }
                    
                    if (citeUrl.startsWith("http") && !retVal.containsKey(title)) {
                        retVal.put(title, citeUrl);
                    }
                    
                } catch (Exception e) {
                    // 忽略單個項目的錯誤，繼續處理其他項目
                }
            }
            
        } catch (Exception e) {
            System.out.println("解析內容錯誤: " + e.getMessage());
        }
        
        return retVal;
    }
    
    /**
     * 執行查詢並返回搜尋結果
     * @return HashMap 包含標題和 URL
     * @throws IOException
     */
    public HashMap<String, String> getSearchResults() throws IOException {
        return query();
    }
    
    /**
     * 從指定 URL 抓取網頁內容
     * @param url 網頁 URL
     * @return 網頁的文字內容
     */
    public static String fetchContent(String url) {
        StringBuilder retVal = new StringBuilder();
        
        try {
            URL u = new URL(url);
            URLConnection conn = u.openConnection();
            conn.setRequestProperty("User-agent", "Chrome/107.0.5304.107");
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(5000);
            
            InputStream in = conn.getInputStream();
            InputStreamReader inReader = new InputStreamReader(in, "utf-8");
            BufferedReader bufReader = new BufferedReader(inReader);
            
            String line;
            while ((line = bufReader.readLine()) != null) {
                retVal.append(line);
            }
            
            bufReader.close();
            inReader.close();
            in.close();
            
        } catch (Exception e) {
            System.out.println("抓取網頁內容錯誤 [" + url + "]: " + e.getMessage());
            return "";
        }
        
        return retVal.toString();
    }
    
    /**
     * 從 HTML 內容中提取純文字
     * @param htmlContent HTML 內容
     * @return 純文字內容
     */
    public static String extractText(String htmlContent) {
        try {
            Document doc = Jsoup.parse(htmlContent);
            
            // 移除 script 和 style 標籤
            doc.select("script, style, nav, footer, header").remove();
            
            // 提取文字
            String text = doc.body().text();
            
            // 移除多餘空白
            text = text.replaceAll("\\s+", " ").trim();
            
            return text;
            
        } catch (Exception e) {
            System.out.println("提取文字錯誤: " + e.getMessage());
            return "";
        }
    }
    
    /**
     * 從網頁建立 WebPage 物件
     * @param url 網頁 URL
     * @param name 網頁名稱/標題
     * @return WebPage 物件
     */
    public static WebPage createWebPage(String url, String name) {
        try {
            String htmlContent = fetchContent(url);
            String textContent = extractText(htmlContent);
            
            return new WebPage(url, name, textContent);
            
        } catch (Exception e) {
            System.out.println("建立 WebPage 錯誤 [" + url + "]: " + e.getMessage());
            return new WebPage(url, name, "");
        }
    }
    
    /**
     * 批次建立多個 WebPage
     * @param urlMap URL 和名稱的對應表
     * @return WebPage 列表
     */
    public static ArrayList<WebPage> createWebPages(HashMap<String, String> urlMap) {
        ArrayList<WebPage> webPages = new ArrayList<>();
        
        for (HashMap.Entry<String, String> entry : urlMap.entrySet()) {
            String name = entry.getKey();
            String url = entry.getValue();
            
            WebPage page = createWebPage(url, name);
            if (page != null && !page.getContent().isEmpty()) {
                webPages.add(page);
            }
        }
        
        return webPages;
    }
    
    /**
     * 從網頁 URL 抓取子連結
     * @param url 網頁 URL
     * @param maxLinks 最大連結數量
     * @return 子連結列表
     */
    public static ArrayList<String> fetchSubLinks(String url, int maxLinks) {
        ArrayList<String> links = new ArrayList<>();
        
        try {
            String htmlContent = fetchContent(url);
            Document doc = Jsoup.parse(htmlContent);
            
            Elements linkElements = doc.select("a[href]");
            int count = 0;
            
            for (Element link : linkElements) {
                if (count >= maxLinks) break;
                
                String href = link.attr("abs:href");
                
                // 過濾有效的 http/https 連結
                if (href.startsWith("http") && !links.contains(href)) {
                    links.add(href);
                    count++;
                }
            }
            
        } catch (Exception e) {
            System.out.println("抓取子連結錯誤 [" + url + "]: " + e.getMessage());
        }
        
        return links;
    }
    
    /**
     * 建立網站樹狀結構
     * @param rootUrl 根 URL
     * @param rootName 根名稱
     * @param depth 深度（層數）
     * @return WebNode 根節點
     */
    public static WebNode buildWebTree(String rootUrl, String rootName, int depth) {
        WebPage rootPage = createWebPage(rootUrl, rootName);
        WebNode rootNode = new WebNode(rootPage);
        
        if (depth > 0) {
            ArrayList<String> subLinks = fetchSubLinks(rootUrl, 5);
            
            for (String subLink : subLinks) {
                WebNode childNode = buildWebTree(subLink, "子頁面", depth - 1);
                rootNode.addChild(childNode);
            }
        }
        
        return rootNode;
    }
    
    /**
     * 檢查 URL 是否可訪問
     * @param url URL
     * @return 是否可訪問
     */
    public static boolean isUrlAccessible(String url) {
        try {
            URL u = new URL(url);
            URLConnection conn = u.openConnection();
            conn.setRequestProperty("User-agent", "Chrome/107.0.5304.107");
            conn.setConnectTimeout(3000);
            conn.setReadTimeout(3000);
            
            InputStream in = conn.getInputStream();
            in.close();
            
            return true;
            
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * 獲取網頁標題
     * @param url URL
     * @return 網頁標題
     */
    public static String getPageTitle(String url) {
        try {
            String htmlContent = fetchContent(url);
            Document doc = Jsoup.parse(htmlContent);
            return doc.title();
            
        } catch (Exception e) {
            System.out.println("獲取標題錯誤 [" + url + "]: " + e.getMessage());
            return "未知標題";
        }
    }
    
    /**
     * 獲取網頁描述（meta description）
     * @param url URL
     * @return 網頁描述
     */
    public static String getPageDescription(String url) {
        try {
            String htmlContent = fetchContent(url);
            Document doc = Jsoup.parse(htmlContent);
            
            Element metaDesc = doc.select("meta[name=description]").first();
            if (metaDesc != null) {
                return metaDesc.attr("content");
            }
            
            // 如果沒有 meta description，返回前 200 字
            String text = extractText(htmlContent);
            return text.length() > 200 ? text.substring(0, 200) + "..." : text;
            
        } catch (Exception e) {
            System.out.println("獲取描述錯誤 [" + url + "]: " + e.getMessage());
            return "";
        }
    }
    
    // Getters
    public String getSearchKeyword() {
        return searchKeyword;
    }
    
    public String getUrl() {
        return url;
    }
    
    public String getContent() {
        return content;
    }
}


