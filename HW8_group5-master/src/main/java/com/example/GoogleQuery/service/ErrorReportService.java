package com.example.GoogleQuery.service;

import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * ErrorReportService - 錯誤回報服務
 * 處理使用者回報咖啡廳資訊錯誤（如限時、插座等規則變動）
 */
@Service
public class ErrorReportService {

    // 記憶體中的錯誤回報記錄（實際應使用資料庫）
    private Map<String, ErrorReport> errorReports = new LinkedHashMap<>();
    private long reportIdCounter = 1;

    /**
     * 錯誤回報記錄
     */
    private static class ErrorReport {
        String reportId;
        String cafeId;
        String cafeName;
        String errorType;
        String description;
        String correctInfo;
        String reporterEmail;
        String status; // pending, reviewing, resolved, rejected
        long timestamp;
        String adminNote;
        long resolvedTime;
        
        ErrorReport(String reportId, Map<String, Object> data) {
            this.reportId = reportId;
            this.cafeId = (String) data.get("cafeId");
            this.cafeName = (String) data.get("cafeName");
            this.errorType = (String) data.get("errorType");
            this.description = (String) data.get("description");
            this.correctInfo = (String) data.get("correctInfo");
            this.reporterEmail = (String) data.get("reporterEmail");
            this.status = "pending";
            this.timestamp = System.currentTimeMillis();
            this.adminNote = "";
            this.resolvedTime = 0;
        }
    }

    /**
     * 提交錯誤回報
     * @param reportData 回報資料
     * @return 回報 ID
     */
    public String submitReport(Map<String, Object> reportData) {
        String reportId = "report_" + String.format("%05d", reportIdCounter++);
        
        ErrorReport report = new ErrorReport(reportId, reportData);
        errorReports.put(reportId, report);
        
        return reportId;
    }

    /**
     * 獲取可用的錯誤類型
     * @return 錯誤類型列表
     */
    public List<Map<String, String>> getAvailableErrorTypes() {
        List<Map<String, String>> types = new ArrayList<>();
        
        types.add(createErrorType("incorrect_hours", "營業時間錯誤", "營業時間已更改"));
        types.add(createErrorType("incorrect_features", "功能特性錯誤", "插座、WiFi、限時等資訊有誤"));
        types.add(createErrorType("incorrect_address", "地址錯誤", "地址或位置資訊錯誤"));
        types.add(createErrorType("incorrect_phone", "電話錯誤", "電話號碼錯誤或已更改"));
        types.add(createErrorType("closed_permanently", "永久停業", "咖啡廳已停業"));
        types.add(createErrorType("temporarily_closed", "暫時停業", "咖啡廳暫時停業"));
        types.add(createErrorType("incorrect_rating", "評分不準確", "評分與實際不符"));
        types.add(createErrorType("other", "其他", "其他問題"));
        
        return types;
    }

    /**
     * 建立錯誤類型物件
     */
    private Map<String, String> createErrorType(String code, String name, String description) {
        Map<String, String> type = new HashMap<>();
        type.put("code", code);
        type.put("name", name);
        type.put("description", description);
        return type;
    }

    /**
     * 獲取特定咖啡廳的回報記錄
     * @param cafeId 咖啡廳 ID
     * @return 回報記錄列表
     */
    public List<Map<String, Object>> getReportsByCafeId(String cafeId) {
        return errorReports.values().stream()
                .filter(report -> report.cafeId.equals(cafeId))
                .sorted((a, b) -> Long.compare(b.timestamp, a.timestamp))
                .map(this::convertToMap)
                .collect(Collectors.toList());
    }

    /**
     * 查詢回報狀態
     * @param reportId 回報 ID
     * @return 回報狀態資訊
     */
    public Map<String, Object> getReportStatus(String reportId) {
        ErrorReport report = errorReports.get(reportId);
        
        if (report == null) {
            return null;
        }
        
        return convertToMap(report);
    }

    /**
     * 將 ErrorReport 轉換為 Map
     */
    private Map<String, Object> convertToMap(ErrorReport report) {
        Map<String, Object> map = new HashMap<>();
        map.put("reportId", report.reportId);
        map.put("cafeId", report.cafeId);
        map.put("cafeName", report.cafeName);
        map.put("errorType", report.errorType);
        map.put("description", report.description);
        map.put("correctInfo", report.correctInfo);
        map.put("reporterEmail", report.reporterEmail);
        map.put("status", report.status);
        map.put("timestamp", report.timestamp);
        map.put("adminNote", report.adminNote);
        map.put("resolvedTime", report.resolvedTime);
        
        // 計算處理時間（如果已處理）
        if (report.resolvedTime > 0) {
            long processingTime = report.resolvedTime - report.timestamp;
            map.put("processingTimeMs", processingTime);
            map.put("processingTimeHours", processingTime / (1000 * 60 * 60));
        }
        
        return map;
    }

    /**
     * 獲取待處理的回報
     * @param limit 返回數量
     * @return 待處理回報列表
     */
    public List<Map<String, Object>> getPendingReports(int limit) {
        return errorReports.values().stream()
                .filter(report -> "pending".equals(report.status))
                .sorted((a, b) -> Long.compare(b.timestamp, a.timestamp))
                .limit(limit)
                .map(this::convertToMap)
                .collect(Collectors.toList());
    }

    /**
     * 更新回報狀態
     * @param reportId 回報 ID
     * @param updateData 更新資料
     * @return 是否更新成功
     */
    public boolean updateReportStatus(String reportId, Map<String, Object> updateData) {
        ErrorReport report = errorReports.get(reportId);
        
        if (report == null) {
            return false;
        }
        
        String newStatus = (String) updateData.get("status");
        if (newStatus != null) {
            report.status = newStatus;
            
            // 如果狀態變為 resolved 或 rejected，記錄處理時間
            if ("resolved".equals(newStatus) || "rejected".equals(newStatus)) {
                report.resolvedTime = System.currentTimeMillis();
            }
        }
        
        String adminNote = (String) updateData.get("adminNote");
        if (adminNote != null) {
            report.adminNote = adminNote;
        }
        
        return true;
    }

    /**
     * 刪除回報記錄
     * @param reportId 回報 ID
     * @return 是否刪除成功
     */
    public boolean deleteReport(String reportId) {
        return errorReports.remove(reportId) != null;
    }

    /**
     * 獲取回報統計資訊
     * @return 統計資訊
     */
    public Map<String, Object> getReportStatistics() {
        Map<String, Object> stats = new HashMap<>();
        
        stats.put("totalReports", errorReports.size());
        
        // 依狀態統計
        Map<String, Long> statusCounts = errorReports.values().stream()
                .collect(Collectors.groupingBy(
                    report -> report.status,
                    Collectors.counting()
                ));
        stats.put("statusDistribution", statusCounts);
        
        // 依錯誤類型統計
        Map<String, Long> typeCounts = errorReports.values().stream()
                .collect(Collectors.groupingBy(
                    report -> report.errorType,
                    Collectors.counting()
                ));
        stats.put("errorTypeDistribution", typeCounts);
        
        // 最常回報的咖啡廳
        Map<String, Long> cafeCounts = errorReports.values().stream()
                .collect(Collectors.groupingBy(
                    report -> report.cafeId,
                    Collectors.counting()
                ));
        
        String mostReportedCafe = cafeCounts.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(entry -> {
                    ErrorReport sample = errorReports.values().stream()
                            .filter(r -> r.cafeId.equals(entry.getKey()))
                            .findFirst()
                            .orElse(null);
                    return sample != null ? sample.cafeName : entry.getKey();
                })
                .orElse(null);
        
        if (mostReportedCafe != null) {
            stats.put("mostReportedCafe", mostReportedCafe);
            stats.put("mostReportedCafeCount", Collections.max(cafeCounts.values()));
        }
        
        // 平均處理時間（已處理的回報）
        List<ErrorReport> resolvedReports = errorReports.values().stream()
                .filter(report -> report.resolvedTime > 0)
                .collect(Collectors.toList());
        
        if (!resolvedReports.isEmpty()) {
            double avgProcessingTime = resolvedReports.stream()
                    .mapToLong(report -> report.resolvedTime - report.timestamp)
                    .average()
                    .orElse(0.0);
            
            stats.put("averageProcessingTimeMs", avgProcessingTime);
            stats.put("averageProcessingTimeHours", avgProcessingTime / (1000 * 60 * 60));
        }
        
        // 最近 24 小時的回報數
        long oneDayAgo = System.currentTimeMillis() - (24 * 60 * 60 * 1000);
        long recentReports = errorReports.values().stream()
                .filter(report -> report.timestamp > oneDayAgo)
                .count();
        stats.put("reportsLast24Hours", recentReports);
        
        return stats;
    }

    /**
     * 依錯誤類型獲取回報
     * @param errorType 錯誤類型
     * @param limit 返回數量
     * @return 回報列表
     */
    public List<Map<String, Object>> getReportsByType(String errorType, int limit) {
        return errorReports.values().stream()
                .filter(report -> report.errorType.equals(errorType))
                .sorted((a, b) -> Long.compare(b.timestamp, a.timestamp))
                .limit(limit)
                .map(this::convertToMap)
                .collect(Collectors.toList());
    }

    /**
     * 依狀態獲取回報
     * @param status 狀態
     * @param limit 返回數量
     * @return 回報列表
     */
    public List<Map<String, Object>> getReportsByStatus(String status, int limit) {
        return errorReports.values().stream()
                .filter(report -> report.status.equals(status))
                .sorted((a, b) -> Long.compare(b.timestamp, a.timestamp))
                .limit(limit)
                .map(this::convertToMap)
                .collect(Collectors.toList());
    }

    /**
     * 依時間範圍獲取回報
     * @param startTime 開始時間
     * @param endTime 結束時間
     * @return 回報列表
     */
    public List<Map<String, Object>> getReportsByTimeRange(long startTime, long endTime) {
        return errorReports.values().stream()
                .filter(report -> report.timestamp >= startTime && report.timestamp <= endTime)
                .sorted((a, b) -> Long.compare(b.timestamp, a.timestamp))
                .map(this::convertToMap)
                .collect(Collectors.toList());
    }

    /**
     * 批次更新回報狀態
     * @param reportIds 回報 ID 列表
     * @param status 新狀態
     * @return 成功更新的數量
     */
    public int batchUpdateStatus(List<String> reportIds, String status) {
        int count = 0;
        
        for (String reportId : reportIds) {
            Map<String, Object> updateData = new HashMap<>();
            updateData.put("status", status);
            
            if (updateReportStatus(reportId, updateData)) {
                count++;
            }
        }
        
        return count;
    }

    /**
     * 批次刪除回報
     * @param reportIds 回報 ID 列表
     * @return 成功刪除的數量
     */
    public int batchDeleteReports(List<String> reportIds) {
        int count = 0;
        
        for (String reportId : reportIds) {
            if (deleteReport(reportId)) {
                count++;
            }
        }
        
        return count;
    }

    /**
     * 搜尋回報（依關鍵字）
     * @param keyword 關鍵字
     * @return 符合的回報列表
     */
    public List<Map<String, Object>> searchReports(String keyword) {
        String lowerKeyword = keyword.toLowerCase();
        
        return errorReports.values().stream()
                .filter(report -> 
                    report.cafeName.toLowerCase().contains(lowerKeyword) ||
                    report.description.toLowerCase().contains(lowerKeyword) ||
                    (report.correctInfo != null && report.correctInfo.toLowerCase().contains(lowerKeyword))
                )
                .sorted((a, b) -> Long.compare(b.timestamp, a.timestamp))
                .map(this::convertToMap)
                .collect(Collectors.toList());
    }

    /**
     * 獲取最近的回報
     * @param limit 返回數量
     * @return 最近的回報列表
     */
    public List<Map<String, Object>> getRecentReports(int limit) {
        return errorReports.values().stream()
                .sorted((a, b) -> Long.compare(b.timestamp, a.timestamp))
                .limit(limit)
                .map(this::convertToMap)
                .collect(Collectors.toList());
    }

    /**
     * 匯出回報記錄（CSV 格式）
     * @return CSV 字串
     */
    public String exportReportsAsCSV() {
        StringBuilder csv = new StringBuilder();
        csv.append("報告ID,咖啡廳ID,咖啡廳名稱,錯誤類型,描述,正確資訊,狀態,回報時間\n");
        
        for (ErrorReport report : errorReports.values()) {
            csv.append(report.reportId).append(",")
               .append(report.cafeId).append(",")
               .append(report.cafeName).append(",")
               .append(report.errorType).append(",")
               .append("\"").append(report.description).append("\",")
               .append("\"").append(report.correctInfo != null ? report.correctInfo : "").append("\",")
               .append(report.status).append(",")
               .append(new Date(report.timestamp)).append("\n");
        }
        
        return csv.toString();
    }

    /**
     * 清除舊的已處理回報（超過 N 天）
     * @param daysOld 天數
     * @return 清除的數量
     */
    public int cleanOldResolvedReports(int daysOld) {
        long cutoffTime = System.currentTimeMillis() - (daysOld * 24L * 60 * 60 * 1000);
        
        List<String> toRemove = errorReports.values().stream()
                .filter(report -> 
                    ("resolved".equals(report.status) || "rejected".equals(report.status)) &&
                    report.resolvedTime > 0 &&
                    report.resolvedTime < cutoffTime
                )
                .map(report -> report.reportId)
                .collect(Collectors.toList());
        
        return batchDeleteReports(toRemove);
    }

    /**
     * 檢查服務狀態
     */
    public Map<String, Object> getServiceStatus() {
        Map<String, Object> status = new HashMap<>();
        status.put("service", "ErrorReportService");
        status.put("status", "running");
        status.put("totalReports", errorReports.size());
        status.put("pendingReports", errorReports.values().stream()
                .filter(r -> "pending".equals(r.status))
                .count()
        );
        status.put("timestamp", System.currentTimeMillis());
        
        return status;
    }
}

