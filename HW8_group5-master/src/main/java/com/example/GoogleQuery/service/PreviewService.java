package com.example.GoogleQuery.service;

import com.example.GoogleQuery.model.WebPage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * PreviewService - 預覽服務
 * 提供網站內容預覽功能
 * 顯示網站標題下的文字，可預覽網站前幾句內容
 */
@Service
public class PreviewService {

    @Autowired
    private GoogleService googleService;

    private static final int DEFAULT_PREVIEW_LENGTH = 200;
    private static final int SHORT_PREVIEW_LENGTH = 100;
    private static final int LONG_PREVIEW_LENGTH = 300;
    private static final int MAX_PREVIEW_LENGTH = 500;

    /**
     * 獲取預覽內容（預設長度）
     * @param webPage 網頁
     * @return 預覽文字
     */
    public String getPreview(WebPage webPage) {
        return getPreview(webPage, DEFAULT_PREVIEW_LENGTH);
    }

    /**
     * 獲取預覽內容（指定長度）
     * @param webPage 網頁
     * @param length 預覽長度
     * @return 預覽文字
     */
    public String getPreview(WebPage webPage, int length) {
        if (webPage == null || webPage.getContent() == null) {
            return "無預覽內容";
        }
        
        String content = webPage.getContent().trim();
        
        if (content.isEmpty()) {
            return "無預覽內容";
        }
        
        // 限制最大長度
        int actualLength = Math.min(length, MAX_PREVIEW_LENGTH);
        
        if (content.length() <= actualLength) {
            return content;
        }
        
        // 截取指定長度並加上省略號
        String preview = content.substring(0, actualLength);
        
        // 嘗試在句子結束處截斷
        preview = truncateAtSentence(preview);
        
        return preview + "...";
    }

    /**
     * 在句子結束處截斷
     * @param text 文字
     * @return 截斷後的文字
     */
    private String truncateAtSentence(String text) {
        // 尋找最後一個句號、問號或驚嘆號
        int lastPeriod = Math.max(
            Math.max(text.lastIndexOf('。'), text.lastIndexOf('.')),
            Math.max(text.lastIndexOf('？'), text.lastIndexOf('!'))
        );
        
        if (lastPeriod > text.length() / 2) {
            // 如果句子結束位置在後半部，就在那裡截斷
            return text.substring(0, lastPeriod + 1);
        }
        
        return text;
    }

    /**
     * 獲取短預覽
     * @param webPage 網頁
     * @return 短預覽文字
     */
    public String getShortPreview(WebPage webPage) {
        return getPreview(webPage, SHORT_PREVIEW_LENGTH);
    }

    /**
     * 獲取長預覽
     * @param webPage 網頁
     * @return 長預覽文字
     */
    public String getLongPreview(WebPage webPage) {
        return getPreview(webPage, LONG_PREVIEW_LENGTH);
    }

    /**
     * 獲取前 N 句內容
     * @param webPage 網頁
     * @param sentenceCount 句子數量
     * @return 前 N 句內容
     */
    public String getFirstSentences(WebPage webPage, int sentenceCount) {
        if (webPage == null || webPage.getContent() == null) {
            return "無預覽內容";
        }
        
        String content = webPage.getContent().trim();
        
        if (content.isEmpty()) {
            return "無預覽內容";
        }
        
        // 分割句子（以句號、問號、驚嘆號為分隔符）
        List<String> sentences = splitIntoSentences(content);
        
        if (sentences.isEmpty()) {
            return getPreview(webPage, DEFAULT_PREVIEW_LENGTH);
        }
        
        // 取前 N 句
        int count = Math.min(sentenceCount, sentences.size());
        StringBuilder preview = new StringBuilder();
        
        for (int i = 0; i < count; i++) {
            preview.append(sentences.get(i));
            if (i < count - 1) {
                preview.append(" ");
            }
        }
        
        return preview.toString();
    }

    /**
     * 將文字分割成句子
     * @param text 文字
     * @return 句子列表
     */
    private List<String> splitIntoSentences(String text) {
        List<String> sentences = new ArrayList<>();
        
        // 使用正則表達式分割（保留分隔符）
        Pattern pattern = Pattern.compile("[^。！？.!?]+[。！？.!?]");
        Matcher matcher = pattern.matcher(text);
        
        while (matcher.find()) {
            String sentence = matcher.group().trim();
            if (!sentence.isEmpty()) {
                sentences.add(sentence);
            }
        }
        
        return sentences;
    }

    /**
     * 獲取智能預覽（根據關鍵字提取相關片段）
     * @param webPage 網頁
     * @param keywords 關鍵字列表
     * @param length 預覽長度
     * @return 包含關鍵字的預覽片段
     */
    public String getSmartPreview(WebPage webPage, List<String> keywords, int length) {
        if (webPage == null || webPage.getContent() == null) {
            return "無預覽內容";
        }
        
        String content = webPage.getContent();
        
        if (keywords == null || keywords.isEmpty()) {
            return getPreview(webPage, length);
        }
        
        // 尋找第一個關鍵字出現的位置
        int firstKeywordPos = -1;
        String foundKeyword = null;
        
        for (String keyword : keywords) {
            int pos = content.toLowerCase().indexOf(keyword.toLowerCase());
            if (pos != -1 && (firstKeywordPos == -1 || pos < firstKeywordPos)) {
                firstKeywordPos = pos;
                foundKeyword = keyword;
            }
        }
        
        if (firstKeywordPos == -1) {
            // 沒有找到關鍵字，返回普通預覽
            return getPreview(webPage, length);
        }
        
        // 從關鍵字前後各取一部分
        int startPos = Math.max(0, firstKeywordPos - length / 3);
        int endPos = Math.min(content.length(), firstKeywordPos + foundKeyword.length() + length * 2 / 3);
        
        String preview = content.substring(startPos, endPos).trim();
        
        if (startPos > 0) {
            preview = "..." + preview;
        }
        if (endPos < content.length()) {
            preview = preview + "...";
        }
        
        return preview;
    }

    /**
     * 高亮關鍵字的預覽
     * @param webPage 網頁
     * @param keywords 關鍵字列表
     * @param length 預覽長度
     * @return 帶高亮標記的預覽文字
     */
    public String getHighlightedPreview(WebPage webPage, List<String> keywords, int length) {
        String preview = getSmartPreview(webPage, keywords, length);
        
        if (keywords == null || keywords.isEmpty()) {
            return preview;
        }
        
        // 高亮每個關鍵字（使用 <mark> 標籤）
        String highlighted = preview;
        for (String keyword : keywords) {
            // 使用正則表達式進行大小寫不敏感的替換
            Pattern pattern = Pattern.compile(keyword, Pattern.CASE_INSENSITIVE);
            Matcher matcher = pattern.matcher(highlighted);
            highlighted = matcher.replaceAll("<mark>$0</mark>");
        }
        
        return highlighted;
    }

    /**
     * 批次獲取預覽
     * @param webPages 網頁列表
     * @param length 預覽長度
     * @return Map（URL -> 預覽文字）
     */
    public Map<String, String> getPreviewsBatch(List<WebPage> webPages, int length) {
        Map<String, String> previews = new HashMap<>();
        
        for (WebPage page : webPages) {
            String preview = getPreview(page, length);
            previews.put(page.getUrl(), preview);
        }
        
        return previews;
    }

    /**
     * 獲取摘要（提取最重要的句子）
     * @param webPage 網頁
     * @param sentenceCount 句子數量
     * @return 摘要文字
     */
    public String getSummary(WebPage webPage, int sentenceCount) {
        if (webPage == null || webPage.getContent() == null) {
            return "無摘要內容";
        }
        
        String content = webPage.getContent().trim();
        List<String> sentences = splitIntoSentences(content);
        
        if (sentences.isEmpty()) {
            return getPreview(webPage, DEFAULT_PREVIEW_LENGTH);
        }
        
        // 簡單策略：取前面的句子（通常包含重要資訊）
        int count = Math.min(sentenceCount, sentences.size());
        StringBuilder summary = new StringBuilder();
        
        for (int i = 0; i < count; i++) {
            summary.append(sentences.get(i));
            if (i < count - 1) {
                summary.append(" ");
            }
        }
        
        return summary.toString();
    }

    /**
     * 獲取關鍵字密度最高的片段
     * @param webPage 網頁
     * @param keywords 關鍵字列表
     * @param length 片段長度
     * @return 關鍵字密度最高的片段
     */
    public String getKeywordDenseSnippet(WebPage webPage, List<String> keywords, int length) {
        if (webPage == null || webPage.getContent() == null || keywords == null || keywords.isEmpty()) {
            return getPreview(webPage, length);
        }
        
        String content = webPage.getContent();
        int contentLength = content.length();
        
        if (contentLength <= length) {
            return content;
        }
        
        // 滑動窗口找出關鍵字密度最高的片段
        int maxKeywordCount = 0;
        int bestStartPos = 0;
        
        for (int i = 0; i <= contentLength - length; i += 20) { // 每 20 個字元移動一次
            String snippet = content.substring(i, Math.min(i + length, contentLength));
            int keywordCount = countKeywordsInText(snippet, keywords);
            
            if (keywordCount > maxKeywordCount) {
                maxKeywordCount = keywordCount;
                bestStartPos = i;
            }
        }
        
        String snippet = content.substring(bestStartPos, Math.min(bestStartPos + length, contentLength));
        
        if (bestStartPos > 0) {
            snippet = "..." + snippet;
        }
        if (bestStartPos + length < contentLength) {
            snippet = snippet + "...";
        }
        
        return snippet;
    }

    /**
     * 計算文字中包含的關鍵字數量
     * @param text 文字
     * @param keywords 關鍵字列表
     * @return 關鍵字總數
     */
    private int countKeywordsInText(String text, List<String> keywords) {
        int count = 0;
        String lowerText = text.toLowerCase();
        
        for (String keyword : keywords) {
            String lowerKeyword = keyword.toLowerCase();
            int index = 0;
            while ((index = lowerText.indexOf(lowerKeyword, index)) != -1) {
                count++;
                index += lowerKeyword.length();
            }
        }
        
        return count;
    }

    /**
     * 獲取預覽統計資訊
     * @param webPage 網頁
     * @return 統計資訊
     */
    public Map<String, Object> getPreviewStatistics(WebPage webPage) {
        Map<String, Object> stats = new HashMap<>();
        
        if (webPage == null || webPage.getContent() == null) {
            stats.put("hasContent", false);
            return stats;
        }
        
        String content = webPage.getContent();
        List<String> sentences = splitIntoSentences(content);
        
        stats.put("hasContent", true);
        stats.put("totalLength", content.length());
        stats.put("totalSentences", sentences.size());
        stats.put("shortPreviewLength", SHORT_PREVIEW_LENGTH);
        stats.put("defaultPreviewLength", DEFAULT_PREVIEW_LENGTH);
        stats.put("longPreviewLength", LONG_PREVIEW_LENGTH);
        
        // 預覽覆蓋率
        double defaultCoverage = Math.min(100.0, (double) DEFAULT_PREVIEW_LENGTH / content.length() * 100);
        stats.put("defaultPreviewCoverage", String.format("%.1f%%", defaultCoverage));
        
        return stats;
    }

    /**
     * 格式化預覽（移除多餘空白、換行等）
     * @param preview 原始預覽文字
     * @return 格式化後的文字
     */
    public String formatPreview(String preview) {
        if (preview == null || preview.isEmpty()) {
            return preview;
        }
        
        // 移除多餘空白和換行
        String formatted = preview.replaceAll("\\s+", " ").trim();
        
        return formatted;
    }

    /**
     * 檢查服務狀態
     */
    public Map<String, Object> getServiceStatus() {
        Map<String, Object> status = new HashMap<>();
        status.put("service", "PreviewService");
        status.put("status", "running");
        status.put("defaultPreviewLength", DEFAULT_PREVIEW_LENGTH);
        status.put("shortPreviewLength", SHORT_PREVIEW_LENGTH);
        status.put("longPreviewLength", LONG_PREVIEW_LENGTH);
        status.put("maxPreviewLength", MAX_PREVIEW_LENGTH);
        status.put("timestamp", System.currentTimeMillis());
        
        return status;
    }
}

