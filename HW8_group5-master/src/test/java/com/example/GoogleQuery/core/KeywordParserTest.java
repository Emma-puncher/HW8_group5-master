package com.example.GoogleQuery.core;

import com.example.GoogleQuery.model.Keyword;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.assertj.core.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * KeywordParserTest - KeywordParser 單元測試
 */
@DisplayName("KeywordParser 測試")
class KeywordParserTest {
    
    private KeywordParser parser;
    private String testContent;
    private ArrayList<Keyword> testKeywords;
    
    @BeforeEach
    void setUp() {
        // 準備測試內容
        testContent = "這是一家位於台北的咖啡廳，提供不限時的閱讀空間。" +
                     "我們的招牌是舒芙蕾鬆餅和手沖咖啡。" +
                     "店內有免費 wifi 和插座，非常適合工作或讀書。" +
                     "環境安靜，採用日式文青風格裝潢。咖啡咖啡咖啡。";
        
        parser = new KeywordParser(testContent);
        
        // 準備測試關鍵字
        testKeywords = new ArrayList<>();
        testKeywords.add(new Keyword("咖啡", 1.5));
        testKeywords.add(new Keyword("不限時", 2.0));
        testKeywords.add(new Keyword("舒芙蕾", 1.5));
        testKeywords.add(new Keyword("wifi", 1.5));
        testKeywords.add(new Keyword("插座", 1.8));
        testKeywords.add(new Keyword("安靜", 1.4));
        testKeywords.add(new Keyword("文青", 1.3));
    }
    
    @Test
    @DisplayName("測試計算單一關鍵字出現次數")
    void testCountKeyword() {
        // Act
        int count1 = parser.countKeyword("咖啡");
        int count2 = parser.countKeyword("不限時");
        int count3 = parser.countKeyword("舒芙蕾");
        
        // Assert
        assertEquals(4, count1, "咖啡應該出現 4 次");
        assertEquals(1, count2, "不限時應該出現 1 次");
        assertEquals(1, count3, "舒芙蕾應該出現 1 次");
    }
    
    @Test
    @DisplayName("測試計算不存在的關鍵字")
    void testCountNonExistentKeyword() {
        // Act
        int count = parser.countKeyword("早午餐");
        
        // Assert
        assertEquals(0, count, "不存在的關鍵字應該返回 0");
    }
    
    @Test
    @DisplayName("測試空關鍵字")
    void testCountEmptyKeyword() {
        // Act & Assert
        assertEquals(0, parser.countKeyword(""));
        assertEquals(0, parser.countKeyword(null));
    }
    
    @Test
    @DisplayName("測試批次計算關鍵字")
    void testCountAllKeywords() {
        // Act
        Map<String, Integer> results = parser.countAllKeywords(testKeywords);
        
        // Assert
        assertNotNull(results);
        assertEquals(7, results.size());
        assertEquals(4, results.get("咖啡"));
        assertEquals(1, results.get("不限時"));
        assertEquals(1, results.get("wifi"));
    }
    
    @Test
    @DisplayName("測試計算加權分數")
    void testCalculateWeightedScore() {
        // Act
        double score = parser.calculateWeightedScore(testKeywords);
        
        // Assert
        assertTrue(score > 0, "分數應該大於 0");
        
        // 驗證計算邏輯
        // 咖啡(4次 × 1.5) + 不限時(1次 × 2.0) + 舒芙蕾(1次 × 1.5) + 
        // wifi(1次 × 1.5) + 插座(1次 × 1.8) + 安靜(1次 × 1.4) + 文青(1次 × 1.3)
        double expected = (4 * 1.5) + (1 * 2.0) + (1 * 1.5) + 
                         (1 * 1.5) + (1 * 1.8) + (1 * 1.4) + (1 * 1.3);
        assertEquals(expected, score, 0.01);
    }
    
    @Test
    @DisplayName("測試取得 Top N 關鍵字")
    void testGetTopKeywords() {
        // Act
        List<Keyword> topKeywords = parser.getTopKeywords(testKeywords, 3);
        
        // Assert
        assertEquals(3, topKeywords.size());
        
        // 第一名應該是 "咖啡"（出現 4 次）
        assertEquals("咖啡", topKeywords.get(0).name);
        
        // 驗證是按出現次數降序排列
        int count1 = parser.countKeyword(topKeywords.get(0).name);
        int count2 = parser.countKeyword(topKeywords.get(1).name);
        assertTrue(count1 >= count2);
    }
    
    @Test
    @DisplayName("測試生成 Hashtags")
    void testGenerateHashtags() {
        // Act
        String hashtags = parser.generateHashtags(testKeywords, 3);
        
        // Assert
        assertNotNull(hashtags);
        assertFalse(hashtags.isEmpty());
        assertTrue(hashtags.startsWith("#"));
        assertTrue(hashtags.contains("咖啡"));
        
        // 驗證格式
        String[] tags = hashtags.split(" ");
        assertEquals(3, tags.length);
        
        for (String tag : tags) {
            assertTrue(tag.startsWith("#"));
        }
    }
    
    @Test
    @DisplayName("測試生成超過可用數量的 Hashtags")
    void testGenerateHashtagsExceedingAvailable() {
        // Arrange
        ArrayList<Keyword> fewKeywords = new ArrayList<>();
        fewKeywords.add(new Keyword("咖啡", 1.5));
        
        // Act
        String hashtags = parser.generateHashtags(fewKeywords, 5);
        
        // Assert
        String[] tags = hashtags.split(" ");
        assertTrue(tags.length <= 1, "Hashtags 數量不應超過可用關鍵字數量");
    }
    
    @Test
    @DisplayName("測試詳細分析")
    void testGetDetailedAnalysis() {
        // Act
        String analysis = parser.getDetailedAnalysis(testKeywords);
        
        // Assert
        assertNotNull(analysis);
        assertFalse(analysis.isEmpty());
        assertTrue(analysis.contains("關鍵字分析"));
        assertTrue(analysis.contains("總分"));
        assertTrue(analysis.contains("咖啡"));
    }
    
    @Test
    @DisplayName("測試快取機制")
    void testCache() {
        // Act - 第一次計算
        long start1 = System.nanoTime();
        int count1 = parser.countKeyword("咖啡");
        long time1 = System.nanoTime() - start1;
        
        // Act - 第二次計算（應該使用快取）
        long start2 = System.nanoTime();
        int count2 = parser.countKeyword("咖啡");
        long time2 = System.nanoTime() - start2;
        
        // Assert
        assertEquals(count1, count2);
        // 快取應該更快（但這個測試可能不穩定）
        // assertTrue(time2 < time1, "快取查詢應該更快");
        
        // 驗證快取內容
        Map<String, Integer> cache = parser.getKeywordCountCache();
        assertTrue(cache.containsKey("咖啡"));
    }
    
    @Test
    @DisplayName("測試清除快取")
    void testClearCache() {
        // Arrange
        parser.countKeyword("咖啡");
        
        // Act
        parser.clearCache();
        
        // Assert
        Map<String, Integer> cache = parser.getKeywordCountCache();
        assertTrue(cache.isEmpty());
    }
    
    @Test
    @DisplayName("測試內容長度")
    void testGetContentLength() {
        // Act
        int length = parser.getContentLength();
        
        // Assert
        assertTrue(length > 0);
        assertEquals(testContent.length(), length);
    }
    
    @Test
    @DisplayName("測試空內容")
    void testEmptyContent() {
        // Arrange
        KeywordParser emptyParser = new KeywordParser("");
        
        // Act
        double score = emptyParser.calculateWeightedScore(testKeywords);
        
        // Assert
        assertEquals(0.0, score);
    }
    
    @Test
    @DisplayName("測試大小寫不敏感")
    void testCaseInsensitive() {
        // Arrange
        String content = "WIFI wifi WiFi Wifi";
        KeywordParser caseParser = new KeywordParser(content);
        
        // Act
        int count = caseParser.countKeyword("wifi");
        
        // Assert
        assertEquals(4, count, "應該對大小寫不敏感");
    }
    
    @Nested
    @DisplayName("邊界測試")
    class EdgeCaseTests {
        
        @Test
        @DisplayName("測試 null 關鍵字列表")
        void testNullKeywordList() {
            assertDoesNotThrow(() -> {
                parser.calculateWeightedScore(null);
            });
        }
        
        @Test
        @DisplayName("測試空關鍵字列表")
        void testEmptyKeywordList() {
            // Arrange
            ArrayList<Keyword> emptyList = new ArrayList<>();
            
            // Act
            double score = parser.calculateWeightedScore(emptyList);
            
            // Assert
            assertEquals(0.0, score);
        }
        
        @Test
        @DisplayName("測試超長文字")
        void testLongContent() {
            // Arrange
            StringBuilder longContent = new StringBuilder();
            for (int i = 0; i < 1000; i++) {
                longContent.append("咖啡 ");
            }
            
            KeywordParser longParser = new KeywordParser(longContent.toString());
            
            // Act
            int count = longParser.countKeyword("咖啡");
            
            // Assert
            assertEquals(1000, count);
        }
    }
    
    @AfterEach
    void tearDown() {
        parser = null;
        testKeywords = null;
    }
}


