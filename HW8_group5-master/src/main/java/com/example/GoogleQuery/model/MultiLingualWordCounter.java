package com.example.GoogleQuery.model;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

/**
 * MultiLingualWordCounter - 多語翻譯工具
 * 直接使用線上翻譯 + 快取
 */
public class MultiLingualWordCounter {

    // 翻譯快取
    private static final Map<String, String> translationCache = new HashMap<>();

    /**
     * 將文字翻譯成中文
     * @param text 原文（例如英文）
     * @return 中文翻譯，如果翻譯失敗則回傳原文
     */
    public static String translateToChinese(String text) {
        if (text == null || text.isEmpty()) return text;

        // 先查快取
        if (translationCache.containsKey(text)) {
            return translationCache.get(text);
        }

        // 呼叫線上翻譯
        String translated = translateViaLibreTranslate(text);

        // 存入快取
        translationCache.put(text, translated);
        return translated;
    }

    /**
     * 使用 LibreTranslate 線上翻譯
     */
    private static String translateViaLibreTranslate(String text) {
        try {
            URL url = new URL("https://libretranslate.com/translate");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            conn.setDoOutput(true);

            String params = "q=" + URLEncoder.encode(text, "UTF-8") +
                            "&source=auto&target=zh&format=text";

            try (OutputStream os = conn.getOutputStream()) {
                os.write(params.getBytes());
                os.flush();
            }

            BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) response.append(line);
            reader.close();
            conn.disconnect();

            String result = response.toString();
            int start = result.indexOf("\"translatedText\":\"") + 18;
            int end = result.indexOf("\"", start);
            return (start >= 0 && end > start) ? result.substring(start, end) : text;

        } catch (Exception e) {
            System.err.println("翻譯失敗: " + text + "，錯誤: " + e.getMessage());
            return text;
        }
    }

    /**
     * 清除翻譯快取
     */
    public static void clearCache() {
        translationCache.clear();
    }

    /**
     * 取得目前快取內容（方便除錯）
     */
    public static Map<String, String> getCache() {
        return new HashMap<>(translationCache);
    }
}
