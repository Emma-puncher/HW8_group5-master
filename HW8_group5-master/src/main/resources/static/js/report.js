// 錯誤回報管理器
class ReportManager {
    constructor() {
        this.storageKey = 'cafe_reports';
        this.reports = this.loadReports();
    }

    // 從記憶體載入回報
    loadReports() {
        try {
            const stored = localStorage.getItem(this.storageKey);
            return stored ? JSON.parse(stored) : [];
        } catch (error) {
            console.error('Failed to load reports:', error);
            return [];
        }
    }

    // 儲存回報到記憶體
    saveReports() {
        try {
            localStorage.setItem(this.storageKey, JSON.stringify(this.reports));
        } catch (error) {
            console.error('Failed to save reports:', error);
        }
    }

    // 新增回報
    addReport(report) {
        const newReport = {
            id: 'report-' + Date.now(),
            cafeName: report.cafeName,
            errorType: report.errorType,
            details: report.details,
            email: report.email || '',
            reportedAt: new Date().toISOString(),
            status: 'pending' // pending, reviewed, resolved
        };

        this.reports.unshift(newReport);
        this.saveReports();
        
        console.log('New report added:', newReport);
        return newReport;
    }

    // 取得所有回報
    getReports() {
        return [...this.reports];
    }

    // 取得回報數量
    getCount() {
        return this.reports.length;
    }

    // 清空回報 (管理用)
    clearAll() {
        this.reports = [];
        this.saveReports();
    }
}

// 初始化回報管理器
window.reportManager = new ReportManager();

// 開啟回報視窗
function openReport() {
    const modal = document.getElementById('reportModal');
    modal.classList.add('show');
    
    // 重置表單
    document.getElementById('reportForm').reset();
}

// 關閉回報視窗
function closeReport() {
    const modal = document.getElementById('reportModal');
    modal.classList.remove('show');
}

// 處理表單送出
function handleReportSubmit(e) {
    e.preventDefault();
    
    const cafeName = document.getElementById('reportCafe').value.trim();
    const errorType = document.getElementById('reportType').value;
    const details = document.getElementById('reportDetails').value.trim();
    const email = document.getElementById('reportEmail').value.trim();
    
    // 驗證必填欄位
    if (!cafeName || !errorType || !details) {
        window.utils.showToast('請填寫所有必填欄位', 'warning');
        return;
    }
    
    // 儲存回報
    const report = window.reportManager.addReport({
        cafeName,
        errorType,
        details,
        email
    });
    
    // 顯示成功訊息
    window.utils.showToast('回報已送出，感謝您的反饋！', 'success');
    
    // 關閉視窗
    closeReport();
    
    // 如果有提供 API，可以在這裡送到後端
    // await window.utils.apiRequest('/report', {
    //     method: 'POST',
    //     body: JSON.stringify(report)
    // });
}

// 初始化回報功能
function initReport() {
    // 綁定開啟按鈕
    document.getElementById('reportBtn')?.addEventListener('click', openReport);
    
    // 綁定關閉按鈕
    document.getElementById('closeReportModal')?.addEventListener('click', closeReport);
    document.getElementById('cancelReport')?.addEventListener('click', closeReport);
    
    // 綁定表單送出
    document.getElementById('reportForm')?.addEventListener('submit', handleReportSubmit);
    
    // 點擊背景關閉
    document.getElementById('reportModal')?.addEventListener('click', (e) => {
        if (e.target.id === 'reportModal') {
            closeReport();
        }
    });

    // ESC 鍵關閉
    document.addEventListener('keydown', (e) => {
        if (e.key === 'Escape') {
            const modal = document.getElementById('reportModal');
            if (modal.classList.contains('show')) {
                closeReport();
            }
        }
    });
    
    console.log('✅ Report module initialized');
}

// 匯出函式
window.reportModule = {
    init: initReport,
    open: openReport,
    close: closeReport
};