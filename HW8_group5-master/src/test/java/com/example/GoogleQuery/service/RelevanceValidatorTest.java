package com.example.GoogleQuery.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

/**
 * RelevanceValidator 測試類
 */
@SpringBootTest
public class RelevanceValidatorTest {

    @Autowired
    private RelevanceValidator relevanceValidator;

    /**
     * 測試不相關詞彙（應返回 false）
     */
    @Test
    public void testIrrelevantKeywords() {
        // 不相關詞彙
        RelevanceValidator.RelevanceResult result1 = relevanceValidator.validateRelevance("鋼鐵");
        assertFalse(result1.isRelevant(), "鋼鐵 應該被判定為不相關");

        RelevanceValidator.RelevanceResult result2 = relevanceValidator.validateRelevance("星巴克");
        assertFalse(result2.isRelevant(), "星巴克 應該被判定為不相關");

        RelevanceValidator.RelevanceResult result3 = relevanceValidator.validateRelevance("汽車");
        assertFalse(result3.isRelevant(), "汽車 應該被判定為不相關");
    }

    /**
     * 測試相關詞彙（應返回 true）
     */
    @Test
    public void testRelevantKeywords() {
        // 相關詞彙
        RelevanceValidator.RelevanceResult result1 = relevanceValidator.validateRelevance("咖啡");
        assertTrue(result1.isRelevant(), "咖啡 應該被判定為相關");

        RelevanceValidator.RelevanceResult result2 = relevanceValidator.validateRelevance("不限時");
        assertTrue(result2.isRelevant(), "不限時 應該被判定為相關");

        RelevanceValidator.RelevanceResult result3 = relevanceValidator.validateRelevance("插座");
        assertTrue(result3.isRelevant(), "插座 應該被判定為相關");

        RelevanceValidator.RelevanceResult result4 = relevanceValidator.validateRelevance("讀書");
        assertTrue(result4.isRelevant(), "讀書 應該被判定為相關");
    }

    /**
     * 測試建議詞彙是否返回
     */
    @Test
    public void testSuggestions() {
        RelevanceValidator.RelevanceResult result = relevanceValidator.validateRelevance("鋼鐵");
        
        assertFalse(result.isRelevant());
        assertNotNull(result.getSuggestions());
        assertFalse(result.getSuggestions().isEmpty(), "不相關搜尋應該返回建議詞彙");
        
        // 建議詞彙應該包含咖啡廳相關的詞彙
        assertTrue(result.getSuggestions().size() <= 5, "建議詞彙應該不超過 5 個");
    }

    /**
     * 測試空搜尋詞
     */
    @Test
    public void testEmptyQuery() {
        RelevanceValidator.RelevanceResult result = relevanceValidator.validateRelevance("");
        assertFalse(result.isRelevant(), "空搜尋詞應該被判定為不相關");
    }

    /**
     * 測試 null 搜尋詞
     */
    @Test
    public void testNullQuery() {
        RelevanceValidator.RelevanceResult result = relevanceValidator.validateRelevance(null);
        assertFalse(result.isRelevant(), "null 搜尋詞應該被判定為不相關");
    }

    /**
     * 測試模糊匹配
     */
    @Test
    public void testFuzzyMatching() {
        // 類似於現有關鍵字的詞彙應該被視為相關
        RelevanceValidator.RelevanceResult result = relevanceValidator.validateRelevance("咖啡館");
        assertTrue(result.isRelevant() || result.getSuggestions().contains("咖啡"), 
                  "類似詞彙應該被視為相關或返回相關建議");
    }
}
