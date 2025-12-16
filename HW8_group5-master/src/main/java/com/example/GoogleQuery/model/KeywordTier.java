package com.example.GoogleQuery.model;

/**
 * KeywordTier - 關鍵字分級枚舉
 * 定義三種關鍵字層級：核心詞、次要詞、參考詞
 */
public enum KeywordTier {
    
    /**
     * 核心關鍵詞（最重要）
     * 權重範圍：2.0 - 3.0
     * 用於計算基準分數（baseline score）
     * 例如：不限時、安靜、插座、wifi、適合讀書
     */
    CORE("核心詞", 2.0, 3.0),
    
    /**
     * 次要關鍵詞
     * 權重範圍：1.0 - 1.9
     * 用於輔助搜尋和評分
     * 例如：咖啡、舒適、文青、環境好、座位多
     */
    SECONDARY("次要詞", 1.0, 1.9),
    
    /**
     * 參考關鍵詞
     * 權重範圍：0.5 - 1.0
     * 提供額外的搜尋維度
     * 例如：甜點、早午餐、戶外座位、寵物友善
     */
    REFERENCE("參考詞", 0.5, 1.0);
    
    private final String displayName;  // 顯示名稱
    private final double minWeight;    // 最小權重
    private final double maxWeight;    // 最大權重
    
    /**
     * 建構子
     * @param displayName 顯示名稱
     * @param minWeight 最小權重
     * @param maxWeight 最大權重
     */
    KeywordTier(String displayName, double minWeight, double maxWeight) {
        this.displayName = displayName;
        this.minWeight = minWeight;
        this.maxWeight = maxWeight;
    }
    
    /**
     * 取得顯示名稱
     * @return 顯示名稱
     */
    public String getDisplayName() {
        return displayName;
    }
    
    /**
     * 取得最小權重
     * @return 最小權重
     */
    public double getMinWeight() {
        return minWeight;
    }
    
    /**
     * 取得最大權重
     * @return 最大權重
     */
    public double getMaxWeight() {
        return maxWeight;
    }
    
    /**
     * 檢查權重是否在此層級的範圍內
     * @param weight 權重
     * @return true 如果在範圍內
     */
    public boolean isInRange(double weight) {
        return weight >= minWeight && weight <= maxWeight;
    }
    
    /**
     * 根據權重取得對應的層級
     * @param weight 權重
     * @return 關鍵字層級
     */
    public static KeywordTier fromWeight(double weight) {
        if (weight >= CORE.minWeight) {
            return CORE;
        } else if (weight >= SECONDARY.minWeight) {
            return SECONDARY;
        } else {
            return REFERENCE;
        }
    }
    
    /**
     * 取得所有層級的描述
     * @return 描述字串
     */
    public static String getDescription() {
        return String.format(
            "關鍵字分級說明：\n" +
            "  %s：權重 %.1f-%.1f（用於計算基準分數）\n" +
            "  %s：權重 %.1f-%.1f（輔助搜尋評分）\n" +
            "  %s：權重 %.1f-%.1f（提供額外維度）",
            CORE.displayName, CORE.minWeight, CORE.maxWeight,
            SECONDARY.displayName, SECONDARY.minWeight, SECONDARY.maxWeight,
            REFERENCE.displayName, REFERENCE.minWeight, REFERENCE.maxWeight
        );
    }
    
    /**
     * toString 方法
     */
    @Override
    public String toString() {
        return displayName;
    }
}

