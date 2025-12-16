package com.example.GoogleQuery.model;

/**
 * Keyword - 關鍵字模型
 * 包含關鍵字名稱、權重、分級等資訊
 */
public class Keyword {
    
    public String name;              // 關鍵字名稱
    public double weight;            // 權重
    private double originalWeight;   // 原始權重（用於重置）
    private KeywordTier tier;        // 關鍵字分級（核心詞、次要詞、參考詞）
    private int tierNumber;          // 分級數字（1, 2, 3）用於向後兼容
    private String category;         // 分類（例：環境、服務、設備）
    
    /**
     * 建構子
     * @param name 關鍵字名稱
     * @param weight 權重
     */
    public Keyword(String name, double weight) {
        this.name = name;
        this.weight = weight;
        this.originalWeight = weight;
        this.tier = KeywordTier.fromWeight(weight);
        this.tierNumber = getTierAsNumber();
        this.category = "";
    }
    
    /**
     * 建構子（含分類）
     * @param name 關鍵字名稱
     * @param weight 權重
     * @param category 分類
     */
    public Keyword(String name, double weight, String category) {
        this(name, weight);
        this.category = category;
    }

    /**
     * 建構子（含層級數字，用於向後兼容）
     * @param name 關鍵字名稱
     * @param weight 權重
     * @param tierNumber 層級數字（1: 核心詞, 2: 次要詞, 3: 參考詞）
     */
    public Keyword(String name, double weight, int tierNumber) {
        this.name = name;
        this.weight = weight;
        this.originalWeight = weight;
        this.tierNumber = tierNumber;
        this.tier = tierNumberToTier(tierNumber);
        this.category = "";
    }

    /**
     * 完整建構子
     * @param name 關鍵字名稱
     * @param weight 權重
     * @param tier 關鍵字分級
     */
    public Keyword(String name, double weight, KeywordTier tier) {
        this.name = name;
        this.weight = weight;
        this.originalWeight = weight;
        this.tier = tier;
        this.tierNumber = getTierAsNumber();
        this.category = "";
    }

    /**
     * 將層級數字轉換為 KeywordTier
     */
    private KeywordTier tierNumberToTier(int tierNumber) {
        switch (tierNumber) {
            case 1: return KeywordTier.CORE;
            case 2: return KeywordTier.SECONDARY;
            case 3: return KeywordTier.REFERENCE;
            default: return KeywordTier.fromWeight(weight);
        }
    }

    /**
     * 將 KeywordTier 轉換為數字
     */
    private int getTierAsNumber() {
        switch (tier) {
            case CORE: return 1;
            case SECONDARY: return 2;
            case REFERENCE: return 3;
            default: return 3;
        }
    }
    
    /**
     * 根據權重判斷關鍵字分級
     * @param weight 權重
     * @return 關鍵字分級
    
    private KeywordTier determineTier(double weight) {
        if (weight >= 2.0) {
            return KeywordTier.CORE;        // 核心詞：2.0 - 3.0
        } else if (weight >= 1.0) {
            return KeywordTier.SECONDARY;   // 次要詞：1.0 - 1.9
        } else {
            return KeywordTier.REFERENCE;   // 參考詞：0.5 - 1.0
        }
    }
    */
    
    // ========== Getters and Setters ==========

    /**
     * 取得關鍵字名稱
     */
    public String getName() {
        return name;
    }
    
    /**
     * 設定關鍵字名稱
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * 取得權重
     */
    public double getWeight() {
        return weight;
    }
    
    /**
     * 設定權重
     */
    public void setWeight(double weight) {
        this.weight = weight;
        // 權重改變時，可能需要重新判斷層級
        if (this.tier == null) {
            this.tier = KeywordTier.fromWeight(weight);
            this.tierNumber = getTierAsNumber();
        }
    }

    /**
     * 取得原始權重
     * @return 原始權重
     */
    public double getOriginalWeight() {
        return originalWeight;
    }
    
    /**
     * 設定原始權重
     * @param originalWeight 原始權重
     */
    public void setOriginalWeight(double originalWeight) {
        this.originalWeight = originalWeight;
    }
    
    /**
     * 取得關鍵字分級（枚舉）
     * @return 關鍵字分級
     */
    public KeywordTier getTier() {
        return tier;
    }

    /**
     * 取得關鍵字分級（數字）
     * @return 1: 核心詞, 2: 次要詞, 3: 參考詞
     */
    public int getTierNumber() {
        return tierNumber;
    }
    
    /**
     * 設定關鍵字分級
     * @param tier 關鍵字分級
     */
    public void setTier(KeywordTier tier) {
        this.tier = tier;
        this.tierNumber = getTierAsNumber();
    }

    /**
     * 設定關鍵字分級（數字）
     */
    public void setTierNumber(int tierNumber) {
        this.tierNumber = tierNumber;
        this.tier = tierNumberToTier(tierNumber);
    }
    
    /**
     * 取得分類
     * @return 分類
     */
    public String getCategory() {
        return category;
    }
    
    /**
     * 設定分類
     * @param category 分類
     */
    public void setCategory(String category) {
        this.category = category;
    }

    // ========== 便利方法 ==========

    /**
     * 重置權重為原始值
     * 用於搜尋後恢復權重，避免累積效應
    */
    public void resetWeight() {
        this.weight = this.originalWeight;
    }
    
    /**
     * 檢查是否為核心關鍵字
     * @return true 如果是核心關鍵字
     */
    public boolean isCore() {
        return tier == KeywordTier.CORE;
    }
    
    /**
     * 檢查是否為次要關鍵字
     * @return true 如果是次要關鍵字
     */
    public boolean isSecondary() {
        return tier == KeywordTier.SECONDARY;
    }
    
    /**
     * 檢查是否為參考關鍵字
     * @return true 如果是參考關鍵字
     */
    public boolean isReference() {
        return tier == KeywordTier.REFERENCE;
    }
    
    /**
     * 暫時提升權重（用於動態調整）
     * @param multiplier 提升倍數
     */
    public void boostWeight(double multiplier) {
        this.weight = this.originalWeight * multiplier;
    }
    
    /**
     * 比較兩個關鍵字的權重
     * @param other 另一個關鍵字
     * @return 權重差異
     */
    public int compareWeight(Keyword other) {
        return Double.compare(this.weight, other.weight);
    }
    
    /**
     * 複製關鍵字
     * @return 新的 Keyword 物件
     */
    public Keyword clone() {
        Keyword cloned = new Keyword(this.name, this.weight, this.category);
        cloned.setOriginalWeight(this.originalWeight);
        cloned.setTier(this.tier);
        return cloned;
    }
    
    /**
     * 轉換為 JSON 格式
     * @return JSON 字串
     */
    public String toJson() {
        return String.format(
            "{\"name\": \"%s\", \"weight\": %.2f, \"tier\": \"%s\", \"tierNumber\": %d, \"category\": \"%s\"}",
            name, weight, tier, tierNumber, category
        );
    }
    
    /**
     * toString 方法
     */
    @Override
    public String toString() {
        return String.format("%s (權重: %.2f, 分級: %s)", name, weight, tier);
    }
    
    /**
     * equals 方法（基於名稱）
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Keyword other = (Keyword) obj;
        return name.equals(other.name);
    }
    
    /**
     * hashCode 方法
     */
    @Override
    public int hashCode() {
        return name.hashCode();
    }
}

