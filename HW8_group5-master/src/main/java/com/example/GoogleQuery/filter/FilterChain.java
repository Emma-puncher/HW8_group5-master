package com.example.GoogleQuery.filter;

import com.example.GoogleQuery.model.SearchResult;

import java.util.ArrayList;
import java.util.List;

/**
 * FilterChain - 篩選器鏈
 * 允許串接多個篩選器，依序執行篩選
 * 使用責任鏈模式（Chain of Responsibility Pattern）
 */
public class FilterChain implements Filter {

    private List<Filter> filters;
    private boolean logStatistics; // 是否記錄每個篩選器的統計資訊

    /**
     * 建構子
     */
    public FilterChain() {
        this.filters = new ArrayList<>();
        this.logStatistics = false;
    }

    /**
     * 建構子（帶初始篩選器）
     * @param initialFilters 初始篩選器列表
     */
    public FilterChain(List<Filter> initialFilters) {
        this.filters = new ArrayList<>(initialFilters);
        this.logStatistics = false;
    }

    /**
     * 執行篩選鏈
     * @param results 搜尋結果列表
     * @return 篩選後的結果列表
     */
    @Override
    public ArrayList<SearchResult> filter(ArrayList<SearchResult> results) {
        ArrayList<SearchResult> filteredResults = results;
        
        if (logStatistics) {
            System.out.println("=== 篩選器鏈開始執行 ===");
            System.out.println("初始結果數量: " + results.size());
        }

        // 依序執行每個篩選器
        for (int i = 0; i < filters.size(); i++) {
            Filter filter = filters.get(i);
            int beforeCount = filteredResults.size();
            
            filteredResults = filter.filter(filteredResults);
            
            if (logStatistics) {
                int afterCount = filteredResults.size();
                int removed = beforeCount - afterCount;
                System.out.println("\n第 " + (i + 1) + " 個篩選器: " + filter.getDescription());
                System.out.println("  篩選前: " + beforeCount + " 筆");
                System.out.println("  篩選後: " + afterCount + " 筆");
                System.out.println("  移除: " + removed + " 筆");
                System.out.println("  保留率: " + String.format("%.1f%%", 
                    beforeCount > 0 ? (double) afterCount / beforeCount * 100 : 0));
            }
        }

        if (logStatistics) {
            System.out.println("\n=== 篩選器鏈執行完畢 ===");
            System.out.println("最終結果數量: " + filteredResults.size());
            System.out.println("總保留率: " + String.format("%.1f%%", 
                results.size() > 0 ? (double) filteredResults.size() / results.size() * 100 : 0));
        }

        return filteredResults;
    }

    /**
     * 新增篩選器到鏈的末端
     * @param filter 篩選器
     * @return FilterChain（支援鏈式呼叫）
     */
    public FilterChain addFilter(Filter filter) {
        if (filter != null) {
            filters.add(filter);
        }
        return this;
    }

    /**
     * 在指定位置插入篩選器
     * @param index 位置
     * @param filter 篩選器
     * @return FilterChain（支援鏈式呼叫）
     */
    public FilterChain addFilter(int index, Filter filter) {
        if (filter != null && index >= 0 && index <= filters.size()) {
            filters.add(index, filter);
        }
        return this;
    }

    /**
     * 移除指定篩選器
     * @param filter 篩選器
     * @return 是否移除成功
     */
    public boolean removeFilter(Filter filter) {
        return filters.remove(filter);
    }

    /**
     * 移除指定位置的篩選器
     * @param index 位置
     * @return 被移除的篩選器
     */
    public Filter removeFilter(int index) {
        if (index >= 0 && index < filters.size()) {
            return filters.remove(index);
        }
        return null;
    }

    /**
     * 獲取指定位置的篩選器
     * @param index 位置
     * @return 篩選器
     */
    public Filter getFilter(int index) {
        if (index >= 0 && index < filters.size()) {
            return filters.get(index);
        }
        return null;
    }

    /**
     * 獲取所有篩選器
     * @return 篩選器列表
     */
    public List<Filter> getFilters() {
        return new ArrayList<>(filters);
    }

    /**
     * 清空所有篩選器
     */
    public void clearFilters() {
        filters.clear();
    }

    /**
     * 獲取篩選器數量
     * @return 數量
     */
    public int getFilterCount() {
        return filters.size();
    }

    /**
     * 檢查是否為空（沒有任何篩選器）
     * @return 是否為空
     */
    public boolean isEmpty() {
        return filters.isEmpty();
    }

    /**
     * 兼容性方法：apply 為 filter 的別名
     */
    public ArrayList<SearchResult> apply(ArrayList<SearchResult> results) {
        return filter(results);
    }
}
