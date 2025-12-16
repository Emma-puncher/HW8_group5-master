package com.example.GoogleQuery.core;

import com.example.GoogleQuery.model.*;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.assertj.core.api.Assertions.*;

import java.util.ArrayList;

/**
 * RankerTest - Ranker 單元測試
 */
@DisplayName("Ranker 測試")
class RankerTest {
    
    private Ranker ranker;
    private ArrayList<WebPage> testPages;
    private ArrayList<Keyword> testKeywords;
    
    @BeforeEach
    void setUp() {
        // 準備測試網站
        testPages = new ArrayList<>();
        
        WebPage page1 = new WebPage("https://example.com/cafe1", "高分咖啡廳", "大安區", "不限時", "地址1");
        page1.setContent("不限時 安靜 咖啡 插座 wifi 適合讀書 舒適 環境好");
        testPages.add(page1);
        
        WebPage page2 = new WebPage("https://example.com/cafe2", "中分咖啡廳", "中山區", "有插座", "地址2");
        page2.setContent("咖啡 插座 wifi 舒適");
        testPages.add(page2);
        
        WebPage page3 = new WebPage("https://example.com/cafe3", "低分咖啡廳", "信義區", "甜點", "地址3");
        page3.setContent("咖啡 甜點");
        testPages.add(page3);
        
        // 準備測試關鍵字
        testKeywords = new ArrayList<>();
        testKeywords.add(new Keyword("不限時", 3.0));
        testKeywords.add(new Keyword("安靜", 2.8));
        testKeywords.add(new Keyword("插座", 2.7));
        testKeywords.add(new Keyword("wifi", 2.6));
        testKeywords.add(new Keyword("咖啡", 1.5));
        
        // 建立 Ranker
        ranker = new Ranker(testPages);
    }
    
    @Test
    @DisplayName("測試計算最終分數")
    void testComputeFinalScores() {
        // Act
        ranker.computeFinalScores(testKeywords);
        
        // Assert
        for (WebPage page : testPages) {
            assertTrue(page.getScore() > 0, "每個網站都應該有分數");
        }
        
        // 高分咖啡廳應該有最高分
        assertTrue(testPages.get(0).getScore() > testPages.get(1).getScore());
        assertTrue(testPages.get(1).getScore() > testPages.get(2).getScore());
    }
    
    @Test
    @DisplayName("測試排序功能")
    void testSortByScore() {
        // Arrange
        ranker.computeFinalScores(testKeywords);
        
        // Act
        ArrayList<WebPage> sorted = ranker.sortByScore();
        
        // Assert
        assertEquals(3, sorted.size());
        
        // 驗證降序排列
        for (int i = 0; i < sorted.size() - 1; i++) {
            assertTrue(sorted.get(i).getScore() >= sorted.get(i + 1).getScore(),
                      "應該按分數降序排列");
        }
        
        // 第一名應該是高分咖啡廳
        assertEquals("高分咖啡廳", sorted.get(0).getName());
    }
    
    @Test
    @DisplayName("測試分數標準化")
    void testNormalizeScores() {
        // Arrange
        ranker.computeFinalScores(testKeywords);
        
        // Act
        ranker.normalizeScores();
        
        // Assert
        for (WebPage page : testPages) {
            double score = page.getScore();
            assertTrue(score >= 0 && score <= 100, 
                      "標準化後的分數應該在 0-100 之間");
        }
        
        // 最高分應該是 100
        ArrayList<WebPage> sorted = ranker.sortByScore();
        assertEquals(100.0, sorted.get(0).getScore(), 0.01);
        
        // 最低分應該是 0
        assertEquals(0.0, sorted.get(sorted.size() - 1).getScore(), 0.01);
    }
    
    @Test
    @DisplayName("測試所有分數相同時的標準化")
    void testNormalizeScoresWhenAllEqual() {
        // Arrange - 所有網站內容相同
        ArrayList<WebPage> samePages = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            WebPage page = new WebPage("https://example.com/cafe" + i, "咖啡廳" + i, "地區", "分類", "地址");
            page.setContent("咖啡");
            samePages.add(page);
        }
        
        Ranker sameRanker = new Ranker(samePages);
        sameRanker.computeFinalScores(testKeywords);
        
        // Act
        sameRanker.normalizeScores();
        
        // Assert
        for (WebPage page : samePages) {
            assertEquals(50.0, page.getScore(), 0.01, 
                        "所有分數相同時應該標準化為 50");
        }
    }
    
    @Test
    @DisplayName("測試取得排名結果")
    void testGetRankedResults() {
        // Arrange
        ranker.computeFinalScores(testKeywords);
        ranker.normalizeScores();
        
        // Act
        ArrayList<SearchResult> results = ranker.getRankedResults();
        
        // Assert
        assertEquals(3, results.size());
        
        // 驗證 SearchResult 包含正確的資訊
        SearchResult firstResult = results.get(0);
        assertNotNull(firstResult.getPage());
        assertTrue(firstResult.getScore() > 0);
        
        // 驗證排序
        for (int i = 0; i < results.size() - 1; i++) {
            assertTrue(results.get(i).getScore() >= results.get(i + 1).getScore());
        }
    }
    
    @Test
    @DisplayName("測試取得 Top N")
    void testGetTopN() {
        // Arrange
        ranker.computeFinalScores(testKeywords);
        ranker.normalizeScores();
        
        // Act
        ArrayList<WebPage> top2 = ranker.getTopN(2);
        
        // Assert
        assertEquals(2, top2.size());
        assertEquals("高分咖啡廳", top2.get(0).getName());
    }
    
    @Test
    @DisplayName("測試取得特定網站的分數")
    void testGetScore() {
        // Arrange
        ranker.computeFinalScores(testKeywords);
        ranker.normalizeScores();
        
        // Act
        double score = ranker.getScore(testPages.get(0));
        
        // Assert
        assertTrue(score >= 0 && score <= 100);
    }
    
    @Test
    @DisplayName("測試取得特定網站的排名")
    void testGetRank() {
        // Arrange
        ranker.computeFinalScores(testKeywords);
        ranker.normalizeScores();
        
        // Act
        int rank1 = ranker.getRank(testPages.get(0));
        int rank3 = ranker.getRank(testPages.get(2));
        
        // Assert
        assertEquals(1, rank1, "高分咖啡廳應該是第 1 名");
        assertEquals(3, rank3, "低分咖啡廳應該是第 3 名");
    }
    
    @Test
    @DisplayName("測試計算平均分數")
    void testGetAverageScore() {
        // Arrange
        ranker.computeFinalScores(testKeywords);
        ranker.normalizeScores();
        
        // Act
        double average = ranker.getAverageScore();
        
        // Assert
        assertTrue(average >= 0 && average <= 100);
        
        // 驗證平均值
        double sum = 0;
        for (WebPage page : testPages) {
            sum += page.getScore();
        }
        double expected = sum / testPages.size();
        assertEquals(expected, average, 0.01);
    }
    
    @Test
    @DisplayName("測試分數範圍篩選")
    void testFilterByScoreRange() {
        // Arrange
        ranker.computeFinalScores(testKeywords);
        ranker.normalizeScores();
        
        // Act
        ArrayList<WebPage> filtered = ranker.filterByScoreRange(40.0, 100.0);
        
        // Assert
        assertNotNull(filtered);
        for (WebPage page : filtered) {
            double score = ranker.getScore(page);
            assertTrue(score >= 40.0 && score <= 100.0);
        }
    }
    
    @Test
    @DisplayName("測試新增網站")
    void testAddPage() {
        // Arrange
        WebPage newPage = new WebPage("https://example.com/new", "新咖啡廳", "地區", "分類", "地址");
        
        // Act
        ranker.addPage(newPage);
        
        // Assert
        ArrayList<WebPage> allPages = ranker.getTopN(10);
        assertTrue(allPages.contains(newPage));
    }
    
    @Test
    @DisplayName("測試移除網站")
    void testRemovePage() {
        // Arrange
        WebPage pageToRemove = testPages.get(0);
        
        // Act
        ranker.removePage(pageToRemove);
        
        // Assert
        ArrayList<WebPage> sorted = ranker.sortByScore();
        assertFalse(sorted.contains(pageToRemove));
    }
    
    @Test
    @DisplayName("測試重設 Ranker")
    void testReset() {
        // Arrange
        ranker.computeFinalScores(testKeywords);
        ranker.normalizeScores();
        
        // Act
        ranker.reset();
        
        // Assert
        double average = ranker.getAverageScore();
        assertEquals(0.0, average, "重設後平均分數應該是 0");
    }
    
    @Test
    @DisplayName("測試列印 Top N 結果")
    void testPrintTopResults() {
        // Arrange
        ranker.computeFinalScores(testKeywords);
        ranker.normalizeScores();
        
        // Act & Assert - 不應該拋出例外
        assertDoesNotThrow(() -> {
            ranker.printTopResults(2);
        });
    }
    
    @Test
    @DisplayName("測試列印詳細分數")
    void testPrintDetailedScores() {
        // Arrange
        ranker.computeFinalScores(testKeywords);
        ranker.normalizeScores();
        
        // Act & Assert
        assertDoesNotThrow(() -> {
            ranker.printDetailedScores();
        });
    }
    
    @Nested
    @DisplayName("邊界測試")
    class EdgeCaseTests {
        
        @Test
        @DisplayName("測試空網站列表")
        void testEmptyPageList() {
            // Arrange
            Ranker emptyRanker = new Ranker(new ArrayList<>());
            
            // Act
            emptyRanker.computeFinalScores(testKeywords);
            emptyRanker.normalizeScores();
            
            // Assert
            ArrayList<WebPage> sorted = emptyRanker.sortByScore();
            assertTrue(sorted.isEmpty());
        }
        
        @Test
        @DisplayName("測試空關鍵字列表")
        void testEmptyKeywordList() {
            // Arrange
            ArrayList<Keyword> emptyKeywords = new ArrayList<>();
            
            // Act
            ranker.computeFinalScores(emptyKeywords);
            
            // Assert
            for (WebPage page : testPages) {
                assertEquals(0.0, page.getScore());
            }
        }
        
        @Test
        @DisplayName("測試單一網站")
        void testSinglePage() {
            // Arrange
            ArrayList<WebPage> singlePage = new ArrayList<>();
            singlePage.add(testPages.get(0));
            Ranker singleRanker = new Ranker(singlePage);
            
            // Act
            singleRanker.computeFinalScores(testKeywords);
            singleRanker.normalizeScores();
            
            // Assert
            assertEquals(50.0, singlePage.get(0).getScore(), 0.01);
        }
        
        @Test
        @DisplayName("測試 Top N 超過網站數量")
        void testGetTopNExceedingSize() {
            // Arrange
            ranker.computeFinalScores(testKeywords);
            
            // Act
            ArrayList<WebPage> top10 = ranker.getTopN(10);
            
            // Assert
            assertEquals(3, top10.size(), "返回數量不應超過實際網站數量");
        }
    }
    
    @AfterEach
    void tearDown() {
        ranker = null;
        testPages = null;
        testKeywords = null;
    }
}

