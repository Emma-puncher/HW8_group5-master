package com.example.GoogleQuery.controller;

import com.example.GoogleQuery.service.ErrorReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * ErrorReportController - 錯誤回報 API 控制器
 * 處理使用者回報咖啡廳資訊錯誤相關的 HTTP 請求
 * 用於處理咖啡廳規則變動（如限時、插座等）的回報
 */
@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/error-reports")
public class ErrorReportController {

    @Autowired
    private ErrorReportService errorReportService;

    /**
     * 提交錯誤回報
     * POST /api/error-reports/submit
     * Body: {
     *   "cafeId": "cafe_001",
     *   "cafeName": "某咖啡廳",
     *   "errorType": "incorrect_hours",
     *   "description": "營業時間已更改",
     *   "correctInfo": "現在是 09:00-22:00",
     *   "reporterEmail": "user@example.com" (optional)
     * }
     * 
     * @param reportData 錯誤回報資料
     * @return 提交結果
     */
    @PostMapping("/submit")
    public ResponseEntity<Map<String, Object>> submitErrorReport(
            @RequestBody Map<String, Object> reportData) {
        
        try {
            // 驗證必填欄位
            String cafeId = (String) reportData.get("cafeId");
            String errorType = (String) reportData.get("errorType");
            String description = (String) reportData.get("description");
            
            if (cafeId == null || cafeId.isEmpty()) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("error", "咖啡廳 ID 不能為空");
                
                return ResponseEntity.badRequest().body(errorResponse);
            }
            
            if (errorType == null || errorType.isEmpty()) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("error", "錯誤類型不能為空");
                
                return ResponseEntity.badRequest().body(errorResponse);
            }
            
            if (description == null || description.isEmpty()) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("error", "錯誤描述不能為空");
                
                return ResponseEntity.badRequest().body(errorResponse);
            }
            
            // 提交錯誤回報
            String reportId = errorReportService.submitReport(reportData);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "感謝您的回報！我們會盡快處理");
            response.put("reportId", reportId);
            response.put("cafeId", cafeId);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "提交失敗: " + e.getMessage());
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                 .body(errorResponse);
        }
    }

    /**
     * 獲取錯誤類型列表
     * GET /api/error-reports/types
     * 
     * @return 所有可選的錯誤類型
     */
    @GetMapping("/types")
    public ResponseEntity<Map<String, Object>> getErrorTypes() {
        try {
            List<Map<String, String>> errorTypes = 
                errorReportService.getAvailableErrorTypes();
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("errorTypes", errorTypes);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "獲取錯誤類型失敗: " + e.getMessage());
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                 .body(errorResponse);
        }
    }

    /**
     * 查詢特定咖啡廳的回報記錄
     * GET /api/error-reports/cafe/cafe_001
     * 
     * @param cafeId 咖啡廳 ID
     * @return 該咖啡廳的回報記錄列表
     */
    @GetMapping("/cafe/{cafeId}")
    public ResponseEntity<Map<String, Object>> getReportsByCafe(
            @PathVariable String cafeId) {
        
        try {
            List<Map<String, Object>> reports = 
                errorReportService.getReportsByCafeId(cafeId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("cafeId", cafeId);
            response.put("total", reports.size());
            response.put("reports", reports);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "查詢失敗: " + e.getMessage());
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                 .body(errorResponse);
        }
    }

    /**
     * 查詢回報狀態
     * GET /api/error-reports/status/report_12345
     * 
     * @param reportId 回報 ID
     * @return 回報狀態資訊
     */
    @GetMapping("/status/{reportId}")
    public ResponseEntity<Map<String, Object>> getReportStatus(
            @PathVariable String reportId) {
        
        try {
            Map<String, Object> reportStatus = 
                errorReportService.getReportStatus(reportId);
            
            if (reportStatus == null) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("error", "找不到該回報記錄");
                
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                                     .body(errorResponse);
            }
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("reportId", reportId);
            response.put("status", reportStatus);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "查詢狀態失敗: " + e.getMessage());
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                 .body(errorResponse);
        }
    }

    /**
     * 獲取所有待處理的回報（管理用）
     * GET /api/error-reports/pending?limit=20
     * 
     * @param limit 返回數量（預設 20）
     * @return 待處理回報列表
     */
    @GetMapping("/pending")
    public ResponseEntity<Map<String, Object>> getPendingReports(
            @RequestParam(defaultValue = "20") int limit) {
        
        try {
            List<Map<String, Object>> pendingReports = 
                errorReportService.getPendingReports(limit);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("total", pendingReports.size());
            response.put("reports", pendingReports);
            response.put("note", "此 API 僅供管理員使用");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "獲取待處理回報失敗: " + e.getMessage());
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                 .body(errorResponse);
        }
    }

    /**
     * 更新回報狀態（管理用）
     * PUT /api/error-reports/update/report_12345
     * Body: { "status": "resolved", "adminNote": "已更新資料" }
     * 
     * @param reportId 回報 ID
     * @param updateData 更新資料
     * @return 更新結果
     */
    @PutMapping("/update/{reportId}")
    public ResponseEntity<Map<String, Object>> updateReportStatus(
            @PathVariable String reportId,
            @RequestBody Map<String, Object> updateData) {
        
        try {
            String status = (String) updateData.get("status");
            
            if (status == null || status.isEmpty()) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("error", "狀態不能為空");
                
                return ResponseEntity.badRequest().body(errorResponse);
            }
            
            boolean updated = errorReportService.updateReportStatus(
                reportId, updateData
            );
            
            if (!updated) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("error", "找不到該回報記錄");
                
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                                     .body(errorResponse);
            }
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "回報狀態已更新");
            response.put("reportId", reportId);
            response.put("newStatus", status);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "更新失敗: " + e.getMessage());
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                 .body(errorResponse);
        }
    }

    /**
     * 獲取回報統計資訊
     * GET /api/error-reports/statistics
     * 
     * @return 統計資訊（總數、各類型數量等）
     */
    @GetMapping("/statistics")
    public ResponseEntity<Map<String, Object>> getReportStatistics() {
        try {
            Map<String, Object> statistics = 
                errorReportService.getReportStatistics();
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("statistics", statistics);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "獲取統計失敗: " + e.getMessage());
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                 .body(errorResponse);
        }
    }

    /**
     * 刪除回報記錄（管理用）
     * DELETE /api/error-reports/delete/report_12345
     * 
     * @param reportId 回報 ID
     * @return 刪除結果
     */
    @DeleteMapping("/delete/{reportId}")
    public ResponseEntity<Map<String, Object>> deleteReport(
            @PathVariable String reportId) {
        
        try {
            boolean deleted = errorReportService.deleteReport(reportId);
            
            if (!deleted) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("error", "找不到該回報記錄");
                
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                                     .body(errorResponse);
            }
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "回報記錄已刪除");
            response.put("reportId", reportId);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "刪除失敗: " + e.getMessage());
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                 .body(errorResponse);
        }
    }

    /**
     * 健康檢查端點
     * GET /api/error-reports/health
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> healthCheck() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "ok");
        response.put("service", "ErrorReportController");
        response.put("timestamp", System.currentTimeMillis());
        
        return ResponseEntity.ok(response);
    }
}


