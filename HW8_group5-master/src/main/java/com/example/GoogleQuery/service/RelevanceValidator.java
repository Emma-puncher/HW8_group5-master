package com.example.GoogleQuery.service;

import com.example.GoogleQuery.model.Keyword;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * RelevanceValidator - 搜尋詞相關性檢查服務
 * 判斷搜尋關鍵字是否與咖啡廳、讀書、工作相關
 */
@Service
public class RelevanceValidator {

    @Autowired
    private KeywordService keywordService;

    // 咖啡廳相關領域的核心詞彙
    private static final Set<String> COFFEE_DOMAIN_KEYWORDS = new HashSet<>(Arrays.asList(
            "咖啡", "咖啡廳", "咖啡館", "咖啡店", "cafe", "coffee",
            "濃縮", "美式", "拿鐵", "卡布奇諾", "摩卡", "espresso",
            "閣樓", "文青", "下午茶", "早午餐",
            "讀書", "工作", "不限時", "自修", "自習",
            "插座", "wifi", "無線網路", "充電",
            "安靜", "舒適", "環境", "座位",
            "新竹", "台中", "台北", "高雄", "台南", "花蓮", "宜蘭", "基隆", "苗栗", "南投", "雲林", "嘉義", "屏東", "澎湖", "金門", "馬祖",
            "大安", "中山", "信義", "松山", "南港", "內湖", "士林", "北投", "西城", "萬華", "中正", "文山", "區"
    ));

    // 明顯不相關的詞彙範例
    private static final Set<String> IRRELEVANT_KEYWORDS = new HashSet<>(Arrays.asList(
            "鋼鐵", "汽車", "電子產品", "電腦", "手機", "科技", "機械",
            "星巴克", "便利店", "超市", "百貨", "百貨公司",
            "醫院", "診所", "藥房", "藥局",
            "銀行", "郵局", "政府機構",
            "飛機", "火車", "高鐵", "地鐵", "公車", "計程車",
            "學校", "大學", "小學", "中學",
            "公園", "山林", "海邊", "山區",
            "股票", "投資", "金融", "理財",
            "運動", "籃球", "足球", "網球", "乒乓球",
            "遊戲", "電玩", "遊樂園"
    ));

    /**
     * 檢驗搜尋詞的相關性
     * @param searchTerm 搜尋詞彙
     * @return RelevanceResult 物件
     */
    public RelevanceResult validateRelevance(String searchTerm) {
        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            return new RelevanceResult(false, "搜尋詞為空", null);
        }

        String normalizedTerm = searchTerm.trim().toLowerCase();
        
        // 檢查是否在明顯不相關詞彙中
        if (isExplicitlyIrrelevant(normalizedTerm)) {
            return new RelevanceResult(false, "搜尋詞與咖啡廳相關資訊無關", 
                    getSuggestedKeywords());
        }

        // 檢查是否在咖啡廳領域詞彙中
        if (isCoffeeDomainRelevant(normalizedTerm)) {
            return new RelevanceResult(true, "相關搜尋詞", null);
        }

        // 使用編輯距離進行模糊匹配
        List<String> similarKeywords = findSimilarKeywords(normalizedTerm);
        if (!similarKeywords.isEmpty()) {
            return new RelevanceResult(true, "可能相關，找到類似詞彙", similarKeywords);
        }

        // 檢查是否與現有咖啡廳名稱匹配
        if (hasCafeNameMatch(normalizedTerm)) {
            return new RelevanceResult(true, "匹配現有咖啡廳名稱", null);
        }

        // 都不符合則判定為不相關
        return new RelevanceResult(false, "搜尋詞可能不相關，無法找到相關咖啡廳", 
                getSuggestedKeywords());
    }

    /**
     * 檢查是否明顯不相關
     */
    private boolean isExplicitlyIrrelevant(String term) {
        return IRRELEVANT_KEYWORDS.stream()
                .anyMatch(keyword -> term.contains(keyword) || keyword.contains(term));
    }

    /**
     * 檢查是否與咖啡廳領域相關
     */
    private boolean isCoffeeDomainRelevant(String term) {
        // 直接匹配
        if (COFFEE_DOMAIN_KEYWORDS.stream()
                .anyMatch(keyword -> term.contains(keyword) || keyword.contains(term))) {
            return true;
        }

        // 檢查是否在 KeywordService 的關鍵字中
        Keyword keyword = keywordService.getKeywordInfo(term);
        return keyword != null;
    }

    /**
     * 尋找類似的關鍵字（使用編輯距離）
     */
    private List<String> findSimilarKeywords(String term) {
        List<String> similar = new ArrayList<>();

        // 從 KeywordService 的所有關鍵字中尋找相似詞
        List<Keyword> allKeywords = keywordService.getAllKeywords();
        
        for (Keyword kw : allKeywords) {
            String kwTerm = kw.getName().toLowerCase();
            if (calculateLevenshteinDistance(term, kwTerm) <= 2) {
                similar.add(kw.getName());
            }
        }

        // 也檢查預定義的咖啡廳詞彙
        for (String keyword : COFFEE_DOMAIN_KEYWORDS) {
            if (calculateLevenshteinDistance(term, keyword) <= 2) {
                similar.add(keyword);
            }
        }

        return similar.stream().distinct().limit(5).collect(Collectors.toList());
    }

    /**
     * 檢查是否與現有咖啡廳名稱匹配
     */
    private boolean hasCafeNameMatch(String term) {
        // 這個方法可以依照需要實現
        // 例如查詢咖啡廳列表中是否有名稱包含搜尋詞
        return false; // 預設實現
    }

    /**
     * 獲得推薦的關鍵字
     */
    private List<String> getSuggestedKeywords() {
        // 返回前 5 個高權重的核心關鍵字
        return keywordService.getAllKeywords().stream()
            .filter(kw -> "core".equals(kw.getCategory()))
            .sorted(Comparator.comparingDouble(Keyword::getWeight).reversed())
            .limit(5)
            .map(Keyword::getName)
            .collect(Collectors.toList());
    }

    /**
     * 計算 Levenshtein 距離（編輯距離）
     */
    private int calculateLevenshteinDistance(String s1, String s2) {
        int len1 = s1.length();
        int len2 = s2.length();
        int[][] dp = new int[len1 + 1][len2 + 1];

        for (int i = 0; i <= len1; i++) {
            dp[i][0] = i;
        }
        for (int j = 0; j <= len2; j++) {
            dp[0][j] = j;
        }

        for (int i = 1; i <= len1; i++) {
            for (int j = 1; j <= len2; j++) {
                if (s1.charAt(i - 1) == s2.charAt(j - 1)) {
                    dp[i][j] = dp[i - 1][j - 1];
                } else {
                    dp[i][j] = 1 + Math.min(dp[i - 1][j], 
                                      Math.min(dp[i][j - 1], dp[i - 1][j - 1]));
                }
            }
        }

        return dp[len1][len2];
    }

    /**
     * 相關性檢查結果物件
     */
    public static class RelevanceResult {
        private boolean relevant;
        private String message;
        private List<String> suggestions;

        public RelevanceResult(boolean relevant, String message, List<String> suggestions) {
            this.relevant = relevant;
            this.message = message;
            this.suggestions = suggestions != null ? suggestions : new ArrayList<>();
        }

        // Getters
        public boolean isRelevant() { return relevant; }
        public String getMessage() { return message; }
        public List<String> getSuggestions() { return suggestions; }
    }
}
