package com.example.GoogleQuery.util;

import java.util.List;
import java.util.regex.Pattern;

/**
 * ValidationUtil - 驗證工具
 * 提供各種輸入驗證功能，防止無效資料和注入攻擊
 */
public class ValidationUtil {
    
    // URL 驗證正則表達式
    private static final Pattern URL_PATTERN = Pattern.compile(
        "^(https?://)?(www\\.)?[-a-zA-Z0-9@:%._\\+~#=]{1,256}\\.[a-zA-Z0-9()]{1,6}\\b([-a-zA-Z0-9()@:%_\\+.~#?&/=]*)$"
    );
    
    // Email 驗證正則表達式
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
        "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
    );
    
    // 電話號碼驗證（台灣）
    private static final Pattern PHONE_PATTERN = Pattern.compile(
        "^(\\+886|0)?[2-9]\\d{7,9}$"
    );
    
    // 危險字元（用於防止注入攻擊）
    private static final Pattern DANGEROUS_CHARS = Pattern.compile(
        "[<>\"'%;()&+]"
    );
    
    // SQL 注入關鍵字
    private static final Pattern SQL_INJECTION = Pattern.compile(
        "('.+--)|(--)|(;)|(/\\*|\\*/)|((\\bOR\\b|\\bAND\\b).*(=|>|<|!|LIKE))",
        Pattern.CASE_INSENSITIVE
    );
    
    // XSS 攻擊模式
    private static final Pattern XSS_PATTERN = Pattern.compile(
        "<script|javascript:|onerror=|onload=|eval\\(|expression\\(",
        Pattern.CASE_INSENSITIVE
    );
    
    /**
     * 驗證字串不為空
     * @param str 字串
     * @return true 如果不為空
     */
    public static boolean isNotEmpty(String str) {
        return str != null && !str.trim().isEmpty();
    }
    
    /**
     * 驗證字串長度
     * @param str 字串
     * @param minLength 最小長度
     * @param maxLength 最大長度
     * @return true 如果長度在範圍內
     */
    public static boolean isValidLength(String str, int minLength, int maxLength) {
        if (str == null) return false;
        int length = str.length();
        return length >= minLength && length <= maxLength;
    }
    
    /**
     * 驗證 URL 格式
     * @param url URL 字串
     * @return true 如果格式正確
     */
    public static boolean isValidUrl(String url) {
        if (url == null || url.isEmpty()) {
            return false;
        }
        return URL_PATTERN.matcher(url).matches();
    }
    
    /**
     * 驗證 Email 格式
     * @param email Email 字串
     * @return true 如果格式正確
     */
    public static boolean isValidEmail(String email) {
        if (email == null || email.isEmpty()) {
            return false;
        }
        return EMAIL_PATTERN.matcher(email).matches();
    }
    
    /**
     * 驗證電話號碼（台灣格式）
     * @param phone 電話號碼
     * @return true 如果格式正確
     */
    public static boolean isValidPhone(String phone) {
        if (phone == null || phone.isEmpty()) {
            return false;
        }
        return PHONE_PATTERN.matcher(phone).matches();
    }
    
    /**
     * 檢查是否包含危險字元
     * @param input 輸入字串
     * @return true 如果包含危險字元
     */
    public static boolean containsDangerousChars(String input) {
        if (input == null) return false;
        return DANGEROUS_CHARS.matcher(input).find();
    }
    
    /**
     * 檢查是否為 SQL 注入攻擊
     * @param input 輸入字串
     * @return true 如果疑似 SQL 注入
     */
    public static boolean isSqlInjection(String input) {
        if (input == null) return false;
        return SQL_INJECTION.matcher(input).find();
    }
    
    /**
     * 檢查是否為 XSS 攻擊
     * @param input 輸入字串
     * @return true 如果疑似 XSS 攻擊
     */
    public static boolean isXssAttack(String input) {
        if (input == null) return false;
        return XSS_PATTERN.matcher(input).find();
    }
    
    /**
     * 清理輸入（移除危險字元）
     * @param input 輸入字串
     * @return 清理後的字串
     */
    public static String sanitizeInput(String input) {
        if (input == null) return "";
        
        // 移除 HTML 標籤
        String sanitized = input.replaceAll("<[^>]+>", "");
        
        // 移除 JavaScript
        sanitized = sanitized.replaceAll("javascript:", "");
        
        // 移除危險字元
        sanitized = sanitized.replaceAll("[<>\"'%;()&+]", "");
        
        return sanitized.trim();
    }
    
    /**
     * 驗證搜尋關鍵字
     * @param keyword 搜尋關鍵字
     * @return true 如果有效
     */
    public static boolean isValidSearchKeyword(String keyword) {
        if (keyword == null || keyword.isEmpty()) {
            return false;
        }
        
        // 檢查長度（1-50 字元）
        if (!isValidLength(keyword, 1, 50)) {
            return false;
        }
        
        // 檢查注入攻擊
        if (isSqlInjection(keyword) || isXssAttack(keyword)) {
            return false;
        }
        
        return true;
    }
    
    /**
     * 驗證台北市行政區
     * @param district 行政區名稱
     * @return true 如果是有效的台北市行政區
     */
    public static boolean isValidTaipeiDistrict(String district) {
        if (district == null || district.isEmpty()) {
            return false;
        }
        
        String[] validDistricts = {
            "中正區", "大同區", "中山區", "松山區", "大安區", "萬華區",
            "信義區", "士林區", "北投區", "內湖區", "南港區", "文山區"
        };
        
        for (String valid : validDistricts) {
            if (district.equals(valid)) {
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * 驗證咖啡廳功能標籤
     * @param feature 功能標籤
     * @return true 如果是有效的功能標籤
     */
    public static boolean isValidCafeFeature(String feature) {
        if (feature == null || feature.isEmpty()) {
            return false;
        }
        
        String[] validFeatures = {
            "不限時", "有插座", "有wifi", "CP值高", 
            "寵物友善", "戶外座位", "安靜", "燈光充足"
        };
        
        for (String valid : validFeatures) {
            if (feature.equals(valid)) {
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * 驗證數值範圍
     * @param value 數值
     * @param min 最小值
     * @param max 最大值
     * @return true 如果在範圍內
     */
    public static boolean isInRange(double value, double min, double max) {
        return value >= min && value <= max;
    }
    
    /**
     * 驗證整數範圍
     * @param value 整數
     * @param min 最小值
     * @param max 最大值
     * @return true 如果在範圍內
     */
    public static boolean isInRange(int value, int min, int max) {
        return value >= min && value <= max;
    }
    
    /**
     * 驗證列表不為空
     * @param list 列表
     * @return true 如果不為空
     */
    public static boolean isNotEmpty(List<?> list) {
        return list != null && !list.isEmpty();
    }
    
    /**
     * 驗證列表大小
     * @param list 列表
     * @param minSize 最小大小
     * @param maxSize 最大大小
     * @return true 如果大小在範圍內
     */
    public static boolean isValidListSize(List<?> list, int minSize, int maxSize) {
        if (list == null) return false;
        int size = list.size();
        return size >= minSize && size <= maxSize;
    }
    
    /**
     * 驗證評分（1-5 星）
     * @param rating 評分
     * @return true 如果有效
     */
    public static boolean isValidRating(double rating) {
        return isInRange(rating, 0.0, 5.0);
    }
    
    /**
     * 驗證分數（0-100）
     * @param score 分數
     * @return true 如果有效
     */
    public static boolean isValidScore(double score) {
        return isInRange(score, 0.0, 100.0);
    }
    
    /**
     * 驗證咖啡廳 ID 格式
     * @param cafeId 咖啡廳 ID
     * @return true 如果格式正確
     */
    public static boolean isValidCafeId(String cafeId) {
        if (cafeId == null || cafeId.isEmpty()) {
            return false;
        }
        
        // 格式：cafe_001, cafe_002, ...
        Pattern cafeIdPattern = Pattern.compile("^cafe_\\d{3}$");
        return cafeIdPattern.matcher(cafeId).matches();
    }
    
    /**
     * 驗證排序方式
     * @param sortBy 排序方式
     * @return true 如果有效
     */
    public static boolean isValidSortBy(String sortBy) {
        if (sortBy == null) return false;
        
        String[] validSortOptions = {"score", "rating", "distance", "name"};
        
        for (String valid : validSortOptions) {
            if (sortBy.equals(valid)) {
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * 批次驗證（返回驗證結果）
     * @param input 輸入字串
     * @return ValidationResult 物件
     */
    public static ValidationResult validate(String input) {
        ValidationResult result = new ValidationResult();
        result.setValid(true);
        
        if (input == null || input.trim().isEmpty()) {
            result.setValid(false);
            result.addError("輸入不可為空");
            return result;
        }
        
        if (containsDangerousChars(input)) {
            result.setValid(false);
            result.addError("包含危險字元");
        }
        
        if (isSqlInjection(input)) {
            result.setValid(false);
            result.addError("疑似 SQL 注入攻擊");
        }
        
        if (isXssAttack(input)) {
            result.setValid(false);
            result.addError("疑似 XSS 攻擊");
        }
        
        return result;
    }
    
    /**
     * ValidationResult - 驗證結果內部類別
     */
    public static class ValidationResult {
        private boolean valid;
        private List<String> errors;
        
        public ValidationResult() {
            this.valid = true;
            this.errors = new java.util.ArrayList<>();
        }
        
        public boolean isValid() {
            return valid;
        }
        
        public void setValid(boolean valid) {
            this.valid = valid;
        }
        
        public List<String> getErrors() {
            return errors;
        }
        
        public void addError(String error) {
            this.errors.add(error);
            this.valid = false;
        }
        
        @Override
        public String toString() {
            if (valid) {
                return "驗證通過";
            } else {
                return "驗證失敗: " + String.join(", ", errors);
            }
        }
    }
}

