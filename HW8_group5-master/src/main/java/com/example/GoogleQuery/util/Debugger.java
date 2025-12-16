package com.example.GoogleQuery.util;

import com.example.GoogleQuery.model.WebPage;
import com.example.GoogleQuery.model.WebTree;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Debugger - Debug 工具
 * 用於記錄系統運行時的日誌、錯誤和效能資訊
 */
public class Debugger {
    
    private boolean enabled;  // 是否啟用 debug 模式
    private DateTimeFormatter timeFormatter;
    private long startTime;   // 程式開始時間
    
    /**
     * 建構子
     * @param enabled 是否啟用 debug 模式
     */
    public Debugger(boolean enabled) {
        this.enabled = enabled;
        this.timeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");
        this.startTime = System.currentTimeMillis();
    }
    
    /**
     * 預設建構子（啟用 debug）
     */
    public Debugger() {
        this(true);
    }
    
    /**
     * 列印一般日誌訊息
     * @param message 訊息內容
     */
    public void log(String message) {
        if (!enabled) return;
        
        String timestamp = getCurrentTimestamp();
        System.out.println(String.format("[LOG] %s - %s", timestamp, message));
    }
    
    /**
     * 列印網頁分數（用於 debug 評分系統）
     * @param page 網頁
     * @param score 分數
     */
    public void logScore(WebPage page, double score) {
        if (!enabled) return;
        
        String timestamp = getCurrentTimestamp();
        System.out.println(String.format(
            "[SCORE] %s - %s: %.2f", 
            timestamp, 
            page.getName(), 
            score
        ));
    }
    
    /**
     * 列印網站樹結構（用於 debug Tree 建構）
     * @param tree 網站樹
     */
    public void logTree(WebTree tree) {
        if (!enabled) return;
        
        System.out.println("\n" + "=".repeat(60));
        System.out.println("[TREE DEBUG]");
        System.out.println("樹結構資訊：" + tree.toString());
        tree.eulerPrintTree();
        System.out.println("=".repeat(60) + "\n");
    }
    
    /**
     * 列印錯誤訊息
     * @param e 例外物件
     */
    public void logError(Exception e) {
        if (!enabled) return;
        
        String timestamp = getCurrentTimestamp();
        System.err.println(String.format("[ERROR] %s - %s", timestamp, e.getClass().getName()));
        System.err.println("錯誤訊息: " + e.getMessage());
        
        // 列印 stack trace（在 debug 模式）
        e.printStackTrace();
    }
    
    /**
     * 列印錯誤訊息（帶自訂訊息）
     * @param message 自訂錯誤訊息
     * @param e 例外物件
     */
    public void logError(String message, Exception e) {
        if (!enabled) return;
        
        String timestamp = getCurrentTimestamp();
        System.err.println(String.format("[ERROR] %s - %s", timestamp, message));
        System.err.println("例外類型: " + e.getClass().getName());
        System.err.println("錯誤訊息: " + e.getMessage());
        e.printStackTrace();
    }
    
    /**
     * 時間檢查點（記錄執行階段的時間戳）
     * @param stageName 階段名稱
     */
    public void timeCheck(String stageName) {
        if (!enabled) return;
        
        long currentTime = System.currentTimeMillis();
        long elapsed = currentTime - startTime;
        
        String timestamp = getCurrentTimestamp();
        System.out.println(String.format(
            "[TIME] %s - %s (已執行: %d ms)", 
            timestamp, 
            stageName, 
            elapsed
        ));
    }
    
    /**
     * 開始計時（返回起始時間戳）
     * @param taskName 任務名稱
     * @return 起始時間戳
     */
    public long startTimer(String taskName) {
        if (!enabled) return 0;
        
        long start = System.currentTimeMillis();
        log(String.format("開始執行: %s", taskName));
        return start;
    }
    
    /**
     * 結束計時（計算執行時間）
     * @param taskName 任務名稱
     * @param startTime 起始時間戳
     */
    public void endTimer(String taskName, long startTime) {
        if (!enabled) return;
        
        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;
        
        log(String.format("完成執行: %s (耗時: %d ms)", taskName, duration));
    }
    
    /**
     * 列印分隔線
     */
    public void separator() {
        if (!enabled) return;
        System.out.println("=".repeat(80));
    }
    
    /**
     * 列印標題
     * @param title 標題內容
     */
    public void printTitle(String title) {
        if (!enabled) return;
        
        separator();
        System.out.println(centerText(title, 80));
        separator();
    }
    
    /**
     * 列印警告訊息
     * @param message 警告訊息
     */
    public void warn(String message) {
        if (!enabled) return;
        
        String timestamp = getCurrentTimestamp();
        System.out.println(String.format("[WARN] %s - %s", timestamp, message));
    }
    
    /**
     * 列印資訊訊息（高亮顯示）
     * @param message 資訊訊息
     */
    public void info(String message) {
        if (!enabled) return;
        
        String timestamp = getCurrentTimestamp();
        System.out.println(String.format("[INFO] %s - %s", timestamp, message));
    }
    
    /**
     * 列印成功訊息
     * @param message 成功訊息
     */
    public void success(String message) {
        if (!enabled) return;
        
        String timestamp = getCurrentTimestamp();
        System.out.println(String.format("[SUCCESS] %s - ✓ %s", timestamp, message));
    }
    
    /**
     * 列印除錯物件（詳細資訊）
     * @param label 標籤
     * @param obj 物件
     */
    public void debug(String label, Object obj) {
        if (!enabled) return;
        
        String timestamp = getCurrentTimestamp();
        System.out.println(String.format("[DEBUG] %s - %s:", timestamp, label));
        System.out.println("  " + obj.toString());
    }
    
    /**
     * 檢查條件並記錄（Assert）
     * @param condition 條件
     * @param message 訊息
     */
    public void assertCondition(boolean condition, String message) {
        if (!enabled) return;
        
        if (!condition) {
            String timestamp = getCurrentTimestamp();
            System.err.println(String.format("[ASSERT FAILED] %s - %s", timestamp, message));
        }
    }
    
    /**
     * 列印記憶體使用狀況
     */
    public void logMemoryUsage() {
        if (!enabled) return;
        
        Runtime runtime = Runtime.getRuntime();
        long totalMemory = runtime.totalMemory();
        long freeMemory = runtime.freeMemory();
        long usedMemory = totalMemory - freeMemory;
        long maxMemory = runtime.maxMemory();
        
        System.out.println("\n[MEMORY] 記憶體使用狀況:");
        System.out.println(String.format("  已使用: %.2f MB", usedMemory / 1024.0 / 1024.0));
        System.out.println(String.format("  可用: %.2f MB", freeMemory / 1024.0 / 1024.0));
        System.out.println(String.format("  總計: %.2f MB", totalMemory / 1024.0 / 1024.0));
        System.out.println(String.format("  最大: %.2f MB\n", maxMemory / 1024.0 / 1024.0));
    }
    
    /**
     * 取得當前時間戳字串
     * @return 格式化的時間戳
     */
    private String getCurrentTimestamp() {
        return LocalDateTime.now().format(timeFormatter);
    }
    
    /**
     * 將文字置中
     * @param text 文字
     * @param width 寬度
     * @return 置中後的文字
     */
    private String centerText(String text, int width) {
        int padding = (width - text.length()) / 2;
        return " ".repeat(Math.max(0, padding)) + text;
    }
    
    /**
     * 啟用 debug 模式
     */
    public void enable() {
        this.enabled = true;
        log("Debug 模式已啟用");
    }
    
    /**
     * 停用 debug 模式
     */
    public void disable() {
        log("Debug 模式已停用");
        this.enabled = false;
    }
    
    /**
     * 檢查是否啟用
     * @return true 如果啟用
     */
    public boolean isEnabled() {
        return enabled;
    }
    
    /**
     * 重設起始時間
     */
    public void reset() {
        this.startTime = System.currentTimeMillis();
        log("計時器已重設");
    }
}

