package com.example.GoogleQuery.repository;

import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * ErrorReportRepository - 錯誤回報資料存取
 * 管理錯誤回報的儲存和查詢
 * 注意：目前使用記憶體儲存，實際應用應使用資料庫
 */
@Repository
public class ErrorReportRepository {

    // 使用 ConcurrentHashMap 確保執行緒安全
    private Map<String, ErrorReport> errorReports = new ConcurrentHashMap<>();
    private long reportIdCounter = 1;

    /**
     * 錯誤回報實體
     */
    public static class ErrorReport {
        private String reportId;
        private String cafeId;
        private String cafeName;
        private String errorType;
        private String description;
        private String correctInfo;
        private String reporterEmail;
        private String status; // pending, reviewing, resolved, rejected
        private long timestamp;
        private String adminNote;
        private long resolvedTime;
        
        // Constructors
        public ErrorReport() {}
        
        public ErrorReport(String reportId, String cafeId, String cafeName, 
                          String errorType, String description) {
            this.reportId = reportId;
            this.cafeId = cafeId;
            this.cafeName = cafeName;
            this.errorType = errorType;
            this.description = description;
            this.status = "pending";
            this.timestamp = System.currentTimeMillis();
        }
        
        // Getters and Setters
        public String getReportId() { return reportId; }
        public void setReportId(String reportId) { this.reportId = reportId; }
        
        public String getCafeId() { return cafeId; }
        public void setCafeId(String cafeId) { this.cafeId = cafeId; }
        
        public String getCafeName() { return cafeName; }
        public void setCafeName(String cafeName) { this.cafeName = cafeName; }
        
        public String getErrorType() { return errorType; }
        public void setErrorType(String errorType) { this.errorType = errorType; }
        
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        
        public String getCorrectInfo() { return correctInfo; }
        public void setCorrectInfo(String correctInfo) { this.correctInfo = correctInfo; }
        
        public String getReporterEmail() { return reporterEmail; }
        public void setReporterEmail(String reporterEmail) { this.reporterEmail = reporterEmail; }
        
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        
        public long getTimestamp() { return timestamp; }
        public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
        
        public String getAdminNote() { return adminNote; }
        public void setAdminNote(String adminNote) { this.adminNote = adminNote; }
        
        public long getResolvedTime() { return resolvedTime; }
        public void setResolvedTime(long resolvedTime) { this.resolvedTime = resolvedTime; }
    }

    /**
     * 儲存錯誤回報
     * @param report 錯誤回報
     * @return 儲存的回報
     */
    public ErrorReport save(ErrorReport report) {
        if (report.getReportId() == null || report.getReportId().isEmpty()) {
            report.setReportId(generateReportId());
        }
        
        if (report.getTimestamp() == 0) {
            report.setTimestamp(System.currentTimeMillis());
        }
        
        if (report.getStatus() == null || report.getStatus().isEmpty()) {
            report.setStatus("pending");
        }
        
        errorReports.put(report.getReportId(), report);
        return report;
    }

    /**
     * 生成回報 ID
     * @return 回報 ID
     */
    private synchronized String generateReportId() {
        return "report_" + String.format("%05d", reportIdCounter++);
    }

    /**
     * 根據 ID 查詢回報
     * @param reportId 回報 ID
     * @return 錯誤回報，不存在則返回 null
     */
    public ErrorReport findById(String reportId) {
        return errorReports.get(reportId);
    }

    /**
     * 查詢所有回報
     * @return 錯誤回報列表
     */
    public List<ErrorReport> findAll() {
        return new ArrayList<>(errorReports.values());
    }

    /**
     * 根據咖啡廳 ID 查詢回報
     * @param cafeId 咖啡廳 ID
     * @return 該咖啡廳的回報列表
     */
    public List<ErrorReport> findByCafeId(String cafeId) {
        return errorReports.values().stream()
                .filter(report -> report.getCafeId().equals(cafeId))
                .sorted((a, b) -> Long.compare(b.getTimestamp(), a.getTimestamp()))
                .collect(Collectors.toList());
    }

    /**
     * 根據狀態查詢回報
     * @param status 狀態
     * @return 該狀態的回報列表
     */
    public List<ErrorReport> findByStatus(String status) {
        return errorReports.values().stream()
                .filter(report -> report.getStatus().equals(status))
                .sorted((a, b) -> Long.compare(b.getTimestamp(), a.getTimestamp()))
                .collect(Collectors.toList());
    }

    /**
     * 根據錯誤類型查詢回報
     * @param errorType 錯誤類型
     * @return 該類型的回報列表
     */
    public List<ErrorReport> findByErrorType(String errorType) {
        return errorReports.values().stream()
                .filter(report -> report.getErrorType().equals(errorType))
                .sorted((a, b) -> Long.compare(b.getTimestamp(), a.getTimestamp()))
                .collect(Collectors.toList());
    }

    /**
     * 根據時間範圍查詢回報
     * @param startTime 開始時間（毫秒）
     * @param endTime 結束時間（毫秒）
     * @return 該時間範圍的回報列表
     */
    public List<ErrorReport> findByTimestampBetween(long startTime, long endTime) {
        return errorReports.values().stream()
                .filter(report -> report.getTimestamp() >= startTime && 
                                 report.getTimestamp() <= endTime)
                .sorted((a, b) -> Long.compare(b.getTimestamp(), a.getTimestamp()))
                .collect(Collectors.toList());
    }

    /**
     * 查詢待處理的回報
     * @return 待處理回報列表
     */
    public List<ErrorReport> findPendingReports() {
        return findByStatus("pending");
    }

    /**
     * 查詢已處理的回報
     * @return 已處理回報列表
     */
    public List<ErrorReport> findResolvedReports() {
        return findByStatus("resolved");
    }

    /**
     * 查詢審核中的回報
     * @return 審核中回報列表
     */
    public List<ErrorReport> findReviewingReports() {
        return findByStatus("reviewing");
    }

    /**
     * 搜尋回報（關鍵字）
     * @param keyword 關鍵字
     * @return 符合的回報列表
     */
    public List<ErrorReport> search(String keyword) {
        if (keyword == null || keyword.isEmpty()) {
            return new ArrayList<>();
        }
        
        String lowerKeyword = keyword.toLowerCase();
        
        return errorReports.values().stream()
                .filter(report -> 
                    report.getCafeName().toLowerCase().contains(lowerKeyword) ||
                    report.getDescription().toLowerCase().contains(lowerKeyword) ||
                    (report.getCorrectInfo() != null && 
                     report.getCorrectInfo().toLowerCase().contains(lowerKeyword))
                )
                .sorted((a, b) -> Long.compare(b.getTimestamp(), a.getTimestamp()))
                .collect(Collectors.toList());
    }

    /**
     * 進階搜尋（支援多條件）
     * @param criteria 搜尋條件
     * @return 符合條件的回報列表
     */
    public List<ErrorReport> advancedSearch(Map<String, Object> criteria) {
        List<ErrorReport> results = new ArrayList<>(errorReports.values());
        
        // 咖啡廳 ID 篩選
        if (criteria.containsKey("cafeId")) {
            String cafeId = (String) criteria.get("cafeId");
            results = results.stream()
                    .filter(report -> report.getCafeId().equals(cafeId))
                    .collect(Collectors.toList());
        }
        
        // 狀態篩選
        if (criteria.containsKey("status")) {
            String status = (String) criteria.get("status");
            results = results.stream()
                    .filter(report -> report.getStatus().equals(status))
                    .collect(Collectors.toList());
        }
        
        // 錯誤類型篩選
        if (criteria.containsKey("errorType")) {
            String errorType = (String) criteria.get("errorType");
            results = results.stream()
                    .filter(report -> report.getErrorType().equals(errorType))
                    .collect(Collectors.toList());
        }
        
        // 時間範圍篩選
        if (criteria.containsKey("startTime") && criteria.containsKey("endTime")) {
            long startTime = ((Number) criteria.get("startTime")).longValue();
            long endTime = ((Number) criteria.get("endTime")).longValue();
            results = results.stream()
                    .filter(report -> report.getTimestamp() >= startTime && 
                                     report.getTimestamp() <= endTime)
                    .collect(Collectors.toList());
        }
        
        // 關鍵字搜尋
        if (criteria.containsKey("keyword")) {
            String keyword = ((String) criteria.get("keyword")).toLowerCase();
            results = results.stream()
                    .filter(report -> 
                        report.getCafeName().toLowerCase().contains(keyword) ||
                        report.getDescription().toLowerCase().contains(keyword)
                    )
                    .collect(Collectors.toList());
        }
        
        // 依時間排序
        results.sort((a, b) -> Long.compare(b.getTimestamp(), a.getTimestamp()));
        
        return results;
    }

    /**
     * 更新回報
     * @param report 錯誤回報
     * @return 更新後的回報
     */
    public ErrorReport update(ErrorReport report) {
        if (report.getReportId() != null && errorReports.containsKey(report.getReportId())) {
            errorReports.put(report.getReportId(), report);
            return report;
        }
        return null;
    }

    /**
     * 刪除回報
     * @param reportId 回報 ID
     * @return 是否刪除成功
     */
    public boolean deleteById(String reportId) {
        return errorReports.remove(reportId) != null;
    }

    /**
     * 批次刪除回報
     * @param reportIds 回報 ID 列表
     * @return 刪除的數量
     */
    public int deleteByIds(List<String> reportIds) {
        int count = 0;
        for (String reportId : reportIds) {
            if (deleteById(reportId)) {
                count++;
            }
        }
        return count;
    }

    /**
     * 刪除特定咖啡廳的所有回報
     * @param cafeId 咖啡廳 ID
     * @return 刪除的數量
     */
    public int deleteByCafeId(String cafeId) {
        List<String> toDelete = errorReports.values().stream()
                .filter(report -> report.getCafeId().equals(cafeId))
                .map(ErrorReport::getReportId)
                .collect(Collectors.toList());
        
        return deleteByIds(toDelete);
    }

    /**
     * 刪除舊的已處理回報
     * @param daysOld 天數
     * @return 刪除的數量
     */
    public int deleteOldResolvedReports(int daysOld) {
        long cutoffTime = System.currentTimeMillis() - (daysOld * 24L * 60 * 60 * 1000);
        
        List<String> toDelete = errorReports.values().stream()
                .filter(report -> 
                    ("resolved".equals(report.getStatus()) || 
                     "rejected".equals(report.getStatus())) &&
                    report.getResolvedTime() > 0 &&
                    report.getResolvedTime() < cutoffTime
                )
                .map(ErrorReport::getReportId)
                .collect(Collectors.toList());
        
        return deleteByIds(toDelete);
    }

    /**
     * 檢查回報是否存在
     * @param reportId 回報 ID
     * @return 是否存在
     */
    public boolean existsById(String reportId) {
        return errorReports.containsKey(reportId);
    }

    /**
     * 獲取回報總數
     * @return 總數
     */
    public long count() {
        return errorReports.size();
    }

    /**
     * 統計各狀態的回報數量
     * @return Map（狀態 -> 數量）
     */
    public Map<String, Long> countByStatus() {
        return errorReports.values().stream()
                .collect(Collectors.groupingBy(
                    ErrorReport::getStatus,
                    Collectors.counting()
                ));
    }

    /**
     * 統計各錯誤類型的回報數量
     * @return Map（錯誤類型 -> 數量）
     */
    public Map<String, Long> countByErrorType() {
        return errorReports.values().stream()
                .collect(Collectors.groupingBy(
                    ErrorReport::getErrorType,
                    Collectors.counting()
                ));
    }

    /**
     * 統計各咖啡廳的回報數量
     * @return Map（咖啡廳 ID -> 數量）
     */
    public Map<String, Long> countByCafeId() {
        return errorReports.values().stream()
                .collect(Collectors.groupingBy(
                    ErrorReport::getCafeId,
                    Collectors.counting()
                ));
    }

    /**
     * 獲取最常被回報的咖啡廳
     * @param limit 返回數量
     * @return 咖啡廳 ID 列表（依回報數量排序）
     */
    public List<String> findMostReportedCafes(int limit) {
        return countByCafeId().entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(limit)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }

    /**
     * 計算平均處理時間（已處理的回報）
     * @return 平均處理時間（毫秒）
     */
    public double calculateAverageProcessingTime() {
        return errorReports.values().stream()
                .filter(report -> report.getResolvedTime() > 0)
                .mapToLong(report -> report.getResolvedTime() - report.getTimestamp())
                .average()
                .orElse(0.0);
    }

    /**
     * 獲取最近的回報
     * @param limit 返回數量
     * @return 最近的回報列表
     */
    public List<ErrorReport> findRecentReports(int limit) {
        return errorReports.values().stream()
                .sorted((a, b) -> Long.compare(b.getTimestamp(), a.getTimestamp()))
                .limit(limit)
                .collect(Collectors.toList());
    }

    /**
     * 清空所有回報（慎用）
     */
    public void deleteAll() {
        errorReports.clear();
    }

    /**
     * 獲取統計資訊
     * @return 統計資訊 Map
     */
    public Map<String, Object> getStatistics() {
        Map<String, Object> stats = new HashMap<>();
        
        stats.put("totalReports", errorReports.size());
        stats.put("statusDistribution", countByStatus());
        stats.put("errorTypeDistribution", countByErrorType());
        stats.put("averageProcessingTimeMs", calculateAverageProcessingTime());
        stats.put("averageProcessingTimeHours", 
            calculateAverageProcessingTime() / (1000 * 60 * 60));
        
        // 最近 24 小時的回報數
        long oneDayAgo = System.currentTimeMillis() - (24 * 60 * 60 * 1000);
        long recentReports = errorReports.values().stream()
                .filter(report -> report.getTimestamp() > oneDayAgo)
                .count();
        stats.put("reportsLast24Hours", recentReports);
        
        // 最常被回報的咖啡廳
        List<String> topReported = findMostReportedCafes(5);
        stats.put("topReportedCafes", topReported);
        
        return stats;
    }

    /**
     * 檢查 Repository 狀態
     * @return 狀態資訊
     */
    public Map<String, Object> getStatus() {
        Map<String, Object> status = new HashMap<>();
        status.put("repository", "ErrorReportRepository");
        status.put("status", "running");
        status.put("totalReports", errorReports.size());
        status.put("pendingReports", findPendingReports().size());
        status.put("storageType", "in-memory");
        status.put("timestamp", System.currentTimeMillis());
        
        return status;
    }
}

