package com.example.GoogleQuery.service;

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
import java.util.Map;

/**
 * ComparisonServiceTest - ComparisonService 單元測試
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ComparisonService 測試")
class ComparisonServiceTest {
    
    @Mock
    private ComparisonService comparisonService;
    
    private List<Cafe> testCafes;
    
    @BeforeEach
    void setUp() {
        // 準備測試咖啡廳資料
        testCafes = new ArrayList<>();
        
        Cafe cafe1 = new Cafe("cafe_001", "咖啡廳A", "https://example.com/a", "大安區", "地址A");
        cafe1.setNoTimeLimit(true);
        cafe1.setHasSocket(true);
        cafe1.setHasWifi(true);
        cafe1.setQuiet(true);
        cafe1.setRating(4.5);
        cafe1.setScore(90.0);
        testCafes.add(cafe1);
        
        Cafe cafe2 = new Cafe("cafe_002", "咖啡廳B", "https://example.com/b", "中山區", "地址B");
        cafe2.setNoTimeLimit(false);
        cafe2.setHasSocket(true);
        cafe2.setHasWifi(true);
        cafe2.setQuiet(false);
        cafe2.setRating(4.2);
        cafe2.setScore(75.0);
        testCafes.add(cafe2);
        
        Cafe cafe3 = new Cafe("cafe_003", "咖啡廳C", "https://example.com/c", "信義區", "地址C");
        cafe3.setNoTimeLimit(true);
        cafe3.setHasSocket(false);
        cafe3.setHasWifi(true);
        cafe3.setQuiet(true);
        cafe3.setRating(4.0);
        cafe3.setScore(70.0);
        testCafes.add(cafe3);
    }
    
    @Test
    @DisplayName("測試比較兩家咖啡廳")
    void testCompareTwoCafes() {
        // Arrange
        List<String> cafeIds = Arrays.asList("cafe_001", "cafe_002");
        ComparisonResult mockResult = new ComparisonResult(testCafes.subList(0, 2));
        
        when(comparisonService.compareCafes(cafeIds)).thenReturn(mockResult);
        
        // Act
        ComparisonResult result = comparisonService.compareCafes(cafeIds);
        
        // Assert
        assertNotNull(result);
        assertEquals(2, result.getCafes().size());
        verify(comparisonService, times(1)).compareCafes(cafeIds);
    }
    
    @Test
    @DisplayName("測試比較三家咖啡廳")
    void testCompareThreeCafes() {
        // Arrange
        List<String> cafeIds = Arrays.asList("cafe_001", "cafe_002", "cafe_003");
        ComparisonResult mockResult = new ComparisonResult(testCafes);
        
        when(comparisonService.compareCafes(cafeIds)).thenReturn(mockResult);
        
        // Act
        ComparisonResult result = comparisonService.compareCafes(cafeIds);
        
        // Assert
        assertNotNull(result);
        assertEquals(3, result.getCafes().size());
    }
    
    @Test
    @DisplayName("測試比較少於兩家咖啡廳（應拋出例外）")
    void testCompareLessThanTwo() {
        // Arrange
        List<String> cafeIds = Arrays.asList("cafe_001");
        
        when(comparisonService.compareCafes(cafeIds))
            .thenThrow(new IllegalArgumentException("請選擇 2-3 家咖啡廳進行比較"));
        
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            comparisonService.compareCafes(cafeIds);
        });
    }
    
    @Test
    @DisplayName("測試比較超過三家咖啡廳（應拋出例外）")
    void testCompareMoreThanThree() {
        // Arrange
        List<String> cafeIds = Arrays.asList("cafe_001", "cafe_002", "cafe_003", "cafe_004");
        
        when(comparisonService.compareCafes(cafeIds))
            .thenThrow(new IllegalArgumentException("請選擇 2-3 家咖啡廳進行比較"));
        
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            comparisonService.compareCafes(cafeIds);
        });
    }
    
    @Test
    @DisplayName("測試詳細比較（指定功能）")
    void testDetailedComparison() {
        // Arrange
        List<String> cafeIds = Arrays.asList("cafe_001", "cafe_002");
        List<String> features = Arrays.asList("不限時", "有插座", "有wifi");
        ComparisonResult mockResult = new ComparisonResult(testCafes.subList(0, 2));
        
        when(comparisonService.detailedComparison(cafeIds, features)).thenReturn(mockResult);
        
        // Act
        ComparisonResult result = comparisonService.detailedComparison(cafeIds, features);
        
        // Assert
        assertNotNull(result);
        verify(comparisonService, times(1)).detailedComparison(cafeIds, features);
    }
    
    @Test
    @DisplayName("測試生成比較摘要")
    void testGenerateComparisonSummary() {
        // Arrange
        List<String> cafeIds = Arrays.asList("cafe_001", "cafe_002");
        Map<String, Object> mockSummary = new java.util.HashMap<>();
        mockSummary.put("cafe_001", Map.of("advantages", Arrays.asList("不限時", "安靜")));
        mockSummary.put("cafe_002", Map.of("advantages", Arrays.asList("有插座")));
        
        when(comparisonService.generateComparisonSummary(cafeIds)).thenReturn(mockSummary);
        
        // Act
        Map<String, Object> summary = comparisonService.generateComparisonSummary(cafeIds);
        
        // Assert
        assertNotNull(summary);
        assertTrue(summary.containsKey("cafe_001"));
        assertTrue(summary.containsKey("cafe_002"));
    }
    
    @Test
    @DisplayName("測試根據優先條件推薦")
    void testGetRecommendationBasedOnPriorities() {
        // Arrange
        List<String> cafeIds = Arrays.asList("cafe_001", "cafe_002", "cafe_003");
        List<String> priorities = Arrays.asList("不限時", "安靜");
        
        Map<String, Object> mockRecommendation = new java.util.HashMap<>();
        mockRecommendation.put("recommended", "cafe_001");
        mockRecommendation.put("reason", "符合 2/2 項優先條件");
        
        when(comparisonService.getRecommendationBasedOnPriorities(cafeIds, priorities))
            .thenReturn(mockRecommendation);
        
        // Act
        Map<String, Object> recommendation = 
            comparisonService.getRecommendationBasedOnPriorities(cafeIds, priorities);
        
        // Assert
        assertNotNull(recommendation);
        assertEquals("cafe_001", recommendation.get("recommended"));
        assertTrue(recommendation.containsKey("reason"));
    }
    
    @Test
    @DisplayName("測試比較特定功能")
    void testCompareByFeature() {
        // Arrange
        List<String> cafeIds = Arrays.asList("cafe_001", "cafe_002");
        String feature = "不限時";
        
        Map<String, Object> mockComparison = new java.util.HashMap<>();
        mockComparison.put("cafe_001", true);
        mockComparison.put("cafe_002", false);
        
        when(comparisonService.compareByFeature(cafeIds, feature)).thenReturn(mockComparison);
        
        // Act
        Map<String, Object> comparison = comparisonService.compareByFeature(cafeIds, feature);
        
        // Assert
        assertNotNull(comparison);
        assertEquals(true, comparison.get("cafe_001"));
        assertEquals(false, comparison.get("cafe_002"));
    }
    
    @Test
    @DisplayName("測試匯出比較結果")
    void testExportComparison() {
        // Arrange
        List<String> cafeIds = Arrays.asList("cafe_001", "cafe_002");
        String format = "json";
        
        Map<String, Object> mockExportData = new java.util.HashMap<>();
        mockExportData.put("format", "json");
        mockExportData.put("cafes", testCafes.subList(0, 2));
        
        when(comparisonService.exportComparison(cafeIds, format)).thenReturn(mockExportData);
        
        // Act
        Map<String, Object> exportData = comparisonService.exportComparison(cafeIds, format);
        
        // Assert
        assertNotNull(exportData);
        assertEquals("json", exportData.get("format"));
        assertTrue(exportData.containsKey("cafes"));
    }
    
    @Test
    @DisplayName("測試比較不存在的咖啡廳")
    void testCompareNonExistentCafes() {
        // Arrange
        List<String> cafeIds = Arrays.asList("cafe_999", "cafe_888");
        
        when(comparisonService.compareCafes(cafeIds))
            .thenThrow(new IllegalArgumentException("找不到指定的咖啡廳"));
        
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            comparisonService.compareCafes(cafeIds);
        });
    }
    
    @Test
    @DisplayName("測試 null 咖啡廳 ID 列表")
    void testNullCafeIdList() {
        // Arrange
        when(comparisonService.compareCafes(null))
            .thenThrow(new IllegalArgumentException("咖啡廳 ID 列表不可為空"));
        
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            comparisonService.compareCafes(null);
        });
    }
    
    @Test
    @DisplayName("測試空咖啡廳 ID 列表")
    void testEmptyCafeIdList() {
        // Arrange
        List<String> emptyList = new ArrayList<>();
        
        when(comparisonService.compareCafes(emptyList))
            .thenThrow(new IllegalArgumentException("請選擇至少 2 家咖啡廳"));
        
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            comparisonService.compareCafes(emptyList);
        });
    }
    
    @Nested
    @DisplayName("ComparisonResult 物件測試")
    class ComparisonResultTests {
        
        @Test
        @DisplayName("測試建立 ComparisonResult")
        void testCreateComparisonResult() {
            // Act
            ComparisonResult result = new ComparisonResult(testCafes.subList(0, 2));
            
            // Assert
            assertNotNull(result);
            assertEquals(2, result.getCafes().size());
        }
        
        @Test
        @DisplayName("測試優劣勢分析")
        void testAdvantagesAndDisadvantages() {
            // Arrange
            ComparisonResult result = new ComparisonResult(testCafes.subList(0, 2));
            
            // Act
            result.analyzeAdvantagesAndDisadvantages();
            
            // Assert
            Map<String, List<String>> advantages = result.getAdvantages();
            assertNotNull(advantages);
            
            // 咖啡廳A 應該有優勢（不限時、安靜）
            assertTrue(advantages.get("cafe_001").contains("不限時") || 
                      advantages.get("cafe_001").contains("安靜"));
        }
        
        @Test
        @DisplayName("測試生成推薦建議")
        void testGenerateRecommendation() {
            // Arrange
            ComparisonResult result = new ComparisonResult(testCafes.subList(0, 2));
            List<String> priorities = Arrays.asList("不限時", "安靜");
            
            // Act
            result.generateRecommendation(priorities);
            
            // Assert
            String recommendation = result.getRecommendation();
            assertNotNull(recommendation);
            assertFalse(recommendation.isEmpty());
            assertTrue(recommendation.contains("推薦"));
        }
        
        @Test
        @DisplayName("測試取得比較摘要")
        void testGetSummary() {
            // Arrange
            ComparisonResult result = new ComparisonResult(testCafes);
            result.analyzeAdvantagesAndDisadvantages();
            
            // Act
            String summary = result.getSummary();
            
            // Assert
            assertNotNull(summary);
            assertFalse(summary.isEmpty());
            assertTrue(summary.contains("咖啡廳比較結果"));
        }
    }
    
    @Nested
    @DisplayName("錯誤處理測試")
    class ErrorHandlingTests {
        
        @Test
        @DisplayName("測試混合存在和不存在的咖啡廳 ID")
        void testMixedExistingAndNonExistingIds() {
            // Arrange
            List<String> mixedIds = Arrays.asList("cafe_001", "cafe_999");
            
            when(comparisonService.compareCafes(mixedIds))
                .thenThrow(new IllegalArgumentException("部分咖啡廳不存在"));
            
            // Act & Assert
            assertThrows(IllegalArgumentException.class, () -> {
                comparisonService.compareCafes(mixedIds);
            });
        }
        
        @Test
        @DisplayName("測試重複的咖啡廳 ID")
        void testDuplicateCafeIds() {
            // Arrange
            List<String> duplicateIds = Arrays.asList("cafe_001", "cafe_001");
            
            // 行為取決於實作：可以過濾重複或拋出例外
            when(comparisonService.compareCafes(duplicateIds))
                .thenThrow(new IllegalArgumentException("請選擇不同的咖啡廳"));
            
            // Act & Assert
            assertThrows(IllegalArgumentException.class, () -> {
                comparisonService.compareCafes(duplicateIds);
            });
        }
    }
    
    @AfterEach
    void tearDown() {
        testCafes = null;
    }
}

/**
 * 注意：ComparisonService 類別需要由你同學實作
 * 
 * 建議的 ComparisonService.java 結構：
 * 
 * @Service
 * public class ComparisonService {
 *     
 *     @Autowired
 *     private CafeRepository cafeRepository;
 *     
 *     public ComparisonResult compareCafes(List<String> cafeIds) {
 *         // 驗證輸入
 *         if (cafeIds == null || cafeIds.size() < 2 || cafeIds.size() > 3) {
 *             throw new IllegalArgumentException("請選擇 2-3 家咖啡廳進行比較");
 *         }
 *         
 *         // 取得咖啡廳資料
 *         List<Cafe> cafes = getCafesByIds(cafeIds);
 *         
 *         // 建立比較結果
 *         ComparisonResult result = new ComparisonResult(cafes);
 *         result.analyzeAdvantagesAndDisadvantages();
 *         
 *         return result;
 *     }
 *     
 *     public ComparisonResult detailedComparison(List<String> cafeIds, 
 *                                                List<String> features) {
 *         // 實作詳細比較邏輯
 *     }
 *     
 *     // ... 其他方法
 * }
 */

