package com.example.GoogleQuery.filter;

import com.example.GoogleQuery.model.SearchResult;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * DistrictFilter - 地區篩選器
 * 根據台北市行政區篩選咖啡廳
 */
public class DistrictFilter implements Filter {

    private List<String> allowedDistricts;

    /**
     * 建構子
     * @param districts 允許的地區列表
     */
    public DistrictFilter(List<String> districts) {
        this.allowedDistricts = districts != null ? new ArrayList<>(districts) : new ArrayList<>();
    }

    /**
     * 建構子（單一地區）
     * @param district 允許的地區
     */
    public DistrictFilter(String district) {
        this.allowedDistricts = new ArrayList<>();
        if (district != null && !district.isEmpty()) {
            this.allowedDistricts.add(district);
        }
    }

    /**
     * 執行篩選
     * @param results 搜尋結果列表
     * @return 篩選後的結果列表
     */
    @Override
    public ArrayList<SearchResult> filter(ArrayList<SearchResult> results) {
        // 如果沒有指定地區，返回所有結果
        if (allowedDistricts == null || allowedDistricts.isEmpty()) {
            return results;
        }

        // 篩選符合地區條件的結果
        return results.stream()
                .filter(this::matchesDistrict)
                .collect(Collectors.toCollection(ArrayList::new));
    }

    /**
     * 檢查搜尋結果是否符合地區條件
     * @param result 搜尋結果
     * @return 是否符合
     */
    private boolean matchesDistrict(SearchResult result) {
        if (result.getDistrict() == null) {
            return false;
        }

        // 檢查是否在允許的地區列表中
        return allowedDistricts.stream()
                .anyMatch(district -> district.equals(result.getDistrict()));
    }

    /**
     * 新增允許的地區
     * @param district 地區名稱
     */
    public void addDistrict(String district) {
        if (district != null && !district.isEmpty() && !allowedDistricts.contains(district)) {
            allowedDistricts.add(district);
        }
    }

    /**
     * 移除允許的地區
     * @param district 地區名稱
     */
    public void removeDistrict(String district) {
        allowedDistricts.remove(district);
    }

    /**
     * 設定允許的地區列表
     * @param districts 地區列表
     */
    public void setDistricts(List<String> districts) {
        this.allowedDistricts = districts != null ? new ArrayList<>(districts) : new ArrayList<>();
    }

    /**
     * 獲取允許的地區列表
     * @return 地區列表
     */
    public List<String> getDistricts() {
        return new ArrayList<>(allowedDistricts);
    }

    /**
     * 清空地區列表
     */
    public void clearDistricts() {
        allowedDistricts.clear();
    }

    /**
     * 檢查是否包含特定地區
     * @param district 地區名稱
     * @return 是否包含
     */
    public boolean containsDistrict(String district) {
        return allowedDistricts.contains(district);
    }

    /**
     * 獲取地區數量
     * @return 地區數量
     */
    public int getDistrictCount() {
        return allowedDistricts.size();
    }

    /**
     * 檢查是否為空（沒有設定任何地區）
     * @return 是否為空
     */
    public boolean isEmpty() {
        return allowedDistricts.isEmpty();
    }

    /**
     * 統計篩選結果
     * @param originalResults 原始結果
     * @param filteredResults 篩選後結果
     * @return 統計資訊
     */
    public String getFilterStatistics(ArrayList<SearchResult> originalResults, 
                                     ArrayList<SearchResult> filteredResults) {
        int originalCount = originalResults.size();
        int filteredCount = filteredResults.size();
        int removedCount = originalCount - filteredCount;
        
        StringBuilder stats = new StringBuilder();
        stats.append("地區篩選統計:\n");
        stats.append("  原始結果: ").append(originalCount).append(" 筆\n");
        stats.append("  篩選後: ").append(filteredCount).append(" 筆\n");
        stats.append("  移除: ").append(removedCount).append(" 筆\n");
        stats.append("  保留率: ").append(String.format("%.1f%%", 
            originalCount > 0 ? (double) filteredCount / originalCount * 100 : 0)).append("\n");
        stats.append("  篩選地區: ").append(String.join(", ", allowedDistricts));
        
        return stats.toString();
    }

    /**
     * 獲取篩選器描述
     * @return 描述文字
     */
    @Override
    public String getDescription() {
        if (allowedDistricts.isEmpty()) {
            return "地區篩選器（無限制）";
        }
        return "地區篩選器（" + String.join(", ", allowedDistricts) + "）";
    }

    /**
     * 驗證地區名稱是否合法（台北市12行政區）
     * @param district 地區名稱
     * @return 是否合法
     */
    public static boolean isValidDistrict(String district) {
        List<String> validDistricts = List.of(
            "中正區", "大同區", "中山區", "松山區", "大安區",
            "萬華區", "信義區", "士林區", "北投區", "內湖區",
            "南港區", "文山區"
        );
        return validDistricts.contains(district);
    }

    /**
     * 取得所有有效的台北市行政區
     * @return 行政區列表
     */
    public static List<String> getAllValidDistricts() {
        return List.of(
            "中正區", "大同區", "中山區", "松山區", "大安區",
            "萬華區", "信義區", "士林區", "北投區", "內湖區",
            "南港區", "文山區"
        );
    }

    /**
     * 複製篩選器
     * @return 新的篩選器實例
     */
    public DistrictFilter copy() {
        return new DistrictFilter(new ArrayList<>(this.allowedDistricts));
    }

    @Override
    public String toString() {
        return getDescription();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        
        DistrictFilter that = (DistrictFilter) obj;
        return allowedDistricts.equals(that.allowedDistricts);
    }

    @Override
    public int hashCode() {
        return allowedDistricts.hashCode();
    }
}

