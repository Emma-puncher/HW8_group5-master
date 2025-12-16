package com.example.GoogleQuery.filter;

import com.example.GoogleQuery.model.SearchResult;
import java.util.ArrayList;

/**
 * Filter - 篩選器介面
 * 所有篩選器都必須實作此介面
 */
public interface Filter {
    
    /**
     * 執行篩選
     * @param results 搜尋結果列表
     * @return 篩選後的結果列表
     */
    ArrayList<SearchResult> filter(ArrayList<SearchResult> results);
    
    /**
     * 獲取篩選器描述
     * @return 描述文字
     */
    default String getDescription() {
        return this.getClass().getSimpleName();
    }
}

