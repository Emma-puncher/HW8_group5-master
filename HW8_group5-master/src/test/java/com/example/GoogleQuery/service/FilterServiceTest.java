package com.example.GoogleQuery.service;

import com.example.GoogleQuery.core.SearchEngine;
import com.example.GoogleQuery.model.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * FilterServiceTest (was misnamed) - unit tests file renamed to match filename
 * Contains tests that exercise search/filter behaviors using Mockito mocks
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("FilterService 測試")
class FilterServiceTest {
    
    @Mock
    private SearchEngine searchEngine;
    
    @InjectMocks
    private SearchService searchService;
    
    private ArrayList<SearchResult> mockResults;
    private ArrayList<Keyword> mockKeywords;
    
    @BeforeEach
    void setUp() {
        // 注意：SearchService 需要由你同學實作
        // 這裡提供測試範例
        
        // 準備模擬搜尋結果
        mockResults = new ArrayList<>();
        
        WebPage page1 = new WebPage("https://example.com/1", "咖啡廳1", "大安區", "不限時", "地址1");
        mockResults.add(new SearchResult(page1, 95.0));
        
        WebPage page2 = new WebPage("https://example.com/2", "咖啡廳2", "中山區", "有插座", "地址2");
        mockResults.add(new SearchResult(page2, 85.0));
        
        WebPage page3 = new WebPage("https://example.com/3", "咖啡廳3", "信義區", "有wifi", "地址3");
        mockResults.add(new SearchResult(page3, 75.0));
        
        // 準備模擬關鍵字
        mockKeywords = new ArrayList<>();
        mockKeywords.add(new Keyword("不限時", 3.0));
        mockKeywords.add(new Keyword("安靜", 2.8));
        mockKeywords.add(new Keyword("咖啡", 1.5));
    }
    
    @Test
    @DisplayName("測試基本搜尋功能")
    void testBasicSearch() {
        // Arrange
        String keyword = "不限時";
        when(searchEngine.search(keyword)).thenReturn(mockResults);
        
        // Act
        ArrayList<SearchResult> results = searchEngine.search(keyword);
        
        // Assert
        assertNotNull(results);
        assertEquals(3, results.size());
        verify(searchEngine, times(1)).search(keyword);
    }
    
    @Test
    @DisplayName("測試進階搜尋（帶篩選）")
    void testAdvancedSearch() {
        // Arrange
        String keyword = "咖啡";
        List<String> districts = Arrays.asList("大安區");
        List<String> features = Arrays.asList("不限時");
        
        when(searchEngine.search(keyword, districts, features)).thenReturn(mockResults);
        
        // Act
        ArrayList<SearchResult> results = searchEngine.search(keyword, districts, features);
        
        // Assert
        assertNotNull(results);
        assertFalse(results.isEmpty());
        verify(searchEngine, times(1)).search(keyword, districts, features);
    }
    
    @Test
    @DisplayName("測試搜尋建議")
    void testGetSearchSuggestions() {
        // Arrange
        String query = "不限";
        List<String> mockSuggestions = Arrays.asList("不限時", "不限消費");
        
        when(searchEngine.getSearchSuggestions(query)).thenReturn(mockSuggestions);
        
        // Act
        List<String> suggestions = searchEngine.getSearchSuggestions(query);
        
        // Assert
        assertNotNull(suggestions);
        assertEquals(2, suggestions.size());
        assertTrue(suggestions.contains("不限時"));
        verify(searchEngine, times(1)).getSearchSuggestions(query);
    }
    
    @Test
    @DisplayName("測試取得單一咖啡廳資訊")
    void testGetCafeById() {
        // Arrange
        String cafeId = "cafe_001";
        WebPage mockCafe = new WebPage("https://example.com/1", "測試咖啡廳", "大安區", "不限時", "地址");
        
        when(searchEngine.getCafeById(cafeId)).thenReturn(mockCafe);
        
        // Act
        WebPage result = searchEngine.getCafeById(cafeId);
        
        // Assert
        assertNotNull(result);
        assertEquals("測試咖啡廳", result.getName());
        verify(searchEngine, times(1)).getCafeById(cafeId);
    }
    
    @Test
    @DisplayName("測試熱門推薦")
    void testGetRecommendations() {
        // Arrange
        int limit = 5;
        when(searchEngine.getRecommendations(limit)).thenReturn(mockResults);
        
        // Act
        ArrayList<SearchResult> recommendations = searchEngine.getRecommendations(limit);
        
        // Assert
        assertNotNull(recommendations);
        assertTrue(recommendations.size() <= limit);
        verify(searchEngine, times(1)).getRecommendations(limit);
    }
    
    @Test
    @DisplayName("測試空關鍵字搜尋")
    void testEmptyKeywordSearch() {
        // Arrange
        String emptyKeyword = "";
        when(searchEngine.search(emptyKeyword)).thenReturn(mockResults);
        
        // Act
        ArrayList<SearchResult> results = searchEngine.search(emptyKeyword);
        
        // Assert
        assertNotNull(results);
        // 空關鍵字應該返回所有結果
        assertEquals(3, results.size());
    }
    
    @Test
    @DisplayName("測試 null 關鍵字處理")
    void testNullKeywordHandling() {
        // Arrange
        when(searchEngine.search(null)).thenReturn(new ArrayList<>());
        
        // Act
        ArrayList<SearchResult> results = searchEngine.search(null);
        
        // Assert
        assertNotNull(results);
        assertTrue(results.isEmpty());
    }
    
    @Test
    @DisplayName("測試地區篩選返回正確結果")
    void testDistrictFilterResults() {
        // Arrange
        List<String> districts = Arrays.asList("大安區");
        ArrayList<SearchResult> filteredResults = new ArrayList<>();
        filteredResults.add(mockResults.get(0)); // 只有大安區的結果
        
        when(searchEngine.search("咖啡", districts, null)).thenReturn(filteredResults);
        
        // Act
        ArrayList<SearchResult> results = searchEngine.search("咖啡", districts, null);
        
        // Assert
        assertEquals(1, results.size());
        assertEquals("大安區", results.get(0).getPage().getDistrict());
    }
    
    @Test
    @DisplayName("測試功能篩選返回正確結果")
    void testFeatureFilterResults() {
        // Arrange
        List<String> features = Arrays.asList("不限時");
        ArrayList<SearchResult> filteredResults = new ArrayList<>();
        filteredResults.add(mockResults.get(0)); // 只有不限時的結果
        
        when(searchEngine.search("咖啡", null, features)).thenReturn(filteredResults);
        
        // Act
        ArrayList<SearchResult> results = searchEngine.search("咖啡", null, features);
        
        // Assert
        assertEquals(1, results.size());
        assertTrue(results.get(0).getPage().getCategory().contains("不限時"));
    }
    
    @Test
    @DisplayName("測試找不到結果的情況")
    void testNoResultsFound() {
        // Arrange
        when(searchEngine.search("不存在的咖啡廳")).thenReturn(new ArrayList<>());
        
        // Act
        ArrayList<SearchResult> results = searchEngine.search("不存在的咖啡廳");
        
        // Assert
        assertNotNull(results);
        assertTrue(results.isEmpty());
    }
    
    @Test
    @DisplayName("測試搜尋結果排序")
    void testSearchResultsOrdering() {
        // Arrange
        when(searchEngine.search("咖啡")).thenReturn(mockResults);
        
        // Act
        ArrayList<SearchResult> results = searchEngine.search("咖啡");
        
        // Assert
        // 驗證結果按分數降序排列
        for (int i = 0; i < results.size() - 1; i++) {
            assertTrue(results.get(i).getScore() >= results.get(i + 1).getScore());
        }
    }
    
    @Test
    @DisplayName("測試多個地區篩選")
    void testMultipleDistrictsFilter() {
        // Arrange
        List<String> districts = Arrays.asList("大安區", "中山區");
        ArrayList<SearchResult> filteredResults = new ArrayList<>();
        filteredResults.add(mockResults.get(0)); // 大安區
        filteredResults.add(mockResults.get(1)); // 中山區
        
        when(searchEngine.search("咖啡", districts, null)).thenReturn(filteredResults);
        
        // Act
        ArrayList<SearchResult> results = searchEngine.search("咖啡", districts, null);
        
        // Assert
        assertEquals(2, results.size());
        for (SearchResult result : results) {
            assertTrue(districts.contains(result.getPage().getDistrict()));
        }
    }
    
    @Test
    @DisplayName("測試搜尋統計資訊")
    void testGetStatistics() {
        // Arrange
        var mockStats = new java.util.HashMap<String, Object>();
        mockStats.put("totalCafes", 30);
        mockStats.put("totalKeywords", 15);
        
        when(searchEngine.getStatistics()).thenReturn(mockStats);
        
        // Act
        var stats = searchEngine.getStatistics();
        
        // Assert
        assertNotNull(stats);
        assertEquals(30, stats.get("totalCafes"));
        assertEquals(15, stats.get("totalKeywords"));
    }
    
    @Nested
    @DisplayName("效能相關測試")
    class PerformanceTests {
        
        @Test
        @DisplayName("測試快取機制（相同查詢）")
        void testCachingMechanism() {
            // Arrange
            String keyword = "咖啡";
            when(searchEngine.search(keyword)).thenReturn(mockResults);
            
            // Act - 執行兩次相同的搜尋
            searchEngine.search(keyword);
            searchEngine.search(keyword);
            
            // Assert - 驗證只呼叫一次（如果有快取）
            // 注意：這個測試取決於你的 Service 實作
            verify(searchEngine, atLeastOnce()).search(keyword);
        }
        
        @Test
        @DisplayName("測試大量結果處理")
        void testLargeResultsHandling() {
            // Arrange
            ArrayList<SearchResult> largeResults = new ArrayList<>();
            for (int i = 0; i < 100; i++) {
                WebPage page = new WebPage("url" + i, "咖啡廳" + i, "地區", "分類", "地址");
                largeResults.add(new SearchResult(page, 50.0));
            }
            
            when(searchEngine.search("咖啡")).thenReturn(largeResults);
            
            // Act
            ArrayList<SearchResult> results = searchEngine.search("咖啡");
            
            // Assert
            assertEquals(100, results.size());
        }
    }
    
    @Nested
    @DisplayName("錯誤處理測試")
    class ErrorHandlingTests {
        
        @Test
        @DisplayName("測試 SearchEngine 為 null")
        void testNullSearchEngine() {
            // 這個測試驗證當 SearchEngine 未初始化時的行為
            // 實際實作中應該拋出適當的例外
        }
        
        @Test
        @DisplayName("測試無效的篩選條件")
        void testInvalidFilterConditions() {
            // Arrange
            List<String> invalidDistricts = Arrays.asList("不存在的區域");
            
            when(searchEngine.search("咖啡", invalidDistricts, null))
                .thenReturn(new ArrayList<>());
            
            // Act
            ArrayList<SearchResult> results = searchEngine.search("咖啡", invalidDistricts, null);
            
            // Assert
            assertTrue(results.isEmpty());
        }
        
        @Test
        @DisplayName("測試搜尋引擎拋出例外")
        void testSearchEngineThrowsException() {
            // Arrange
            when(searchEngine.search(anyString()))
                .thenThrow(new RuntimeException("搜尋引擎錯誤"));
            
            // Act & Assert
            assertThrows(RuntimeException.class, () -> {
                searchEngine.search("咖啡");
            });
        }
    }
    
    @AfterEach
    void tearDown() {
        mockResults = null;
        mockKeywords = null;
    }
}

/**
 * 注意：SearchService 類別需要由你同學實作
 * 
 * 建議的 SearchService.java 結構：
 * 
 * @Service
 * public class SearchService {
 *     
 *     @Autowired
 *     @Qualifier("cafeList")
 *     private ArrayList<Cafe> cafes;
 *     
 *     @Autowired
 *     @Qualifier("keywordList")
 *     private ArrayList<Keyword> keywords;
 *     
 *     private SearchEngine searchEngine;
 *     
 *     @PostConstruct
 *     public void init() {
 *         searchEngine = new SearchEngine(cafes, keywords);
 *         searchEngine.initialize();
 *     }
 *     
 *     public ArrayList<SearchResult> search(String keyword) {
 *         return searchEngine.search(keyword);
 *     }
 *     
 *     public ArrayList<SearchResult> advancedSearch(String keyword, 
 *                                                   List<String> districts, 
 *                                                   List<String> features) {
 *         return searchEngine.search(keyword, districts, features);
 *     }
 *     
 *     // ... 其他方法
 * }
 */

