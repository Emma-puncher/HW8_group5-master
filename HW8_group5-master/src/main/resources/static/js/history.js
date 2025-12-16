// 瀏覽紀錄管理器
class HistoryManager {
    constructor() {
        this.storageKey = 'cafe_history';
        this.maxItems = 20; // 最多保存 20 筆記錄
        this.history = this.loadHistory();
    }

    // 從記憶體載入紀錄
    loadHistory() {
        try {
            const stored = localStorage.getItem(this.storageKey);
            return stored ? JSON.parse(stored) : [];
        } catch (error) {
            console.error('Failed to load history:', error);
            return [];
        }
    }

    // 儲存紀錄到記憶體
    saveHistory() {
        try {
            localStorage.setItem(this.storageKey, JSON.stringify(this.history));
        } catch (error) {
            console.error('Failed to save history:', error);
        }
    }

    // 新增瀏覽紀錄
    addHistory(cafe) {
        console.log('🔍 新增瀏覽紀錄:', cafe);
        console.log('📊 目前紀錄數量:', this.history.length);
        
        // 移除相同的舊紀錄
        this.history = this.history.filter(item => item.id !== cafe.id);

        // 新增到開頭
        const historyItem = {
            id: cafe.id,
            name: cafe.name,
            address: cafe.address,
            features: cafe.features || [],
            hashtags: cafe.hashtags || [],
            score: cafe.score,
            viewedAt: new Date().toISOString()
        };

        this.history.unshift(historyItem);

        // 限制數量
        if (this.history.length > this.maxItems) {
            this.history = this.history.slice(0, this.maxItems);
        }

        this.saveHistory();
        console.log('✅ 儲存後紀錄數量:', this.history.length);
        console.log('💾 LocalStorage 內容:', localStorage.getItem(this.storageKey));
    }

    // 移除單筆紀錄
    removeHistory(cafeId) {
        this.history = this.history.filter(item => item.id !== cafeId);
        this.saveHistory();
    }

    // 取得所有紀錄
    getHistory() {
        return [...this.history];
    }

    // 清空所有紀錄
    clearAll() {
        if (confirm('確定要清空所有瀏覽紀錄嗎?')) {
            this.history = [];
            this.saveHistory();
            window.utils.showToast('已清空瀏覽紀錄', 'success');
            return true;
        }
        return false;
    }

    // 取得最近瀏覽的 N 筆
    getRecent(count = 5) {
        return this.history.slice(0, count);
    }
}

// 初始化瀏覽紀錄管理器（只在不存在時創建）
if (!window.historyManager) {
    window.historyManager = new HistoryManager();
}

// 渲染瀏覽紀錄
function renderHistory() {
    const container = document.getElementById('historyContent');
    const history = window.historyManager.getHistory();

    if (history.length === 0) {
        container.innerHTML = window.utils.createEmptyState(
            'fa-history',
            '尚無瀏覽紀錄',
            '開始探索咖啡廳吧!'
        );
        return;
    }

    container.innerHTML = `
        <div class="history-actions" style="margin-bottom: 1rem; display: flex; justify-content: flex-end;">
            <button class="btn-small btn-danger" id="clearAllHistory">
                <i class="fas fa-trash"></i> 清空紀錄
            </button>
        </div>
        <div class="history-list">
            ${history.map(item => `
                <div class="history-item" data-cafe-id="${item.id}">
                    <div class="history-info">
                        <div class="history-name">${item.name}</div>
                        <div class="history-time">
                            <i class="fas fa-clock"></i>
                            ${window.utils.formatDateTime(item.viewedAt)}
                        </div>
                    </div>
                    <div class="history-actions">
                        <button class="btn-small btn-primary view-cafe-btn" data-cafe-id="${item.id}">
                            查看
                        </button>
                        <button class="btn-small btn-danger remove-history-btn" data-cafe-id="${item.id}">
                            <i class="fas fa-times"></i>
                        </button>
                    </div>
                </div>
            `).join('')}
        </div>
    `;

    // 綁定清空按鈕
    document.getElementById('clearAllHistory')?.addEventListener('click', () => {
        if (window.historyManager.clearAll()) {
            renderHistory();
        }
    });

    // 綁定查看按鈕
    document.querySelectorAll('.view-cafe-btn').forEach(btn => {
        btn.addEventListener('click', () => {
            const cafeId = btn.dataset.cafeId;
            const cafe = history.find(item => item.id === cafeId);
            if (cafe) {
                closeHistory();
                // 觸發搜尋該咖啡廳
                const searchInput = document.getElementById('searchInput');
                if (searchInput) {
                    searchInput.value = cafe.name;
                    document.getElementById('searchBtn')?.click();
                }
            }
        });
    });

    // 綁定移除按鈕
    document.querySelectorAll('.remove-history-btn').forEach(btn => {
        btn.addEventListener('click', (e) => {
            e.stopPropagation();
            const cafeId = btn.dataset.cafeId;
            window.historyManager.removeHistory(cafeId);
            renderHistory();
        });
    });
}

// 開啟瀏覽紀錄
function openHistory() {
    const modal = document.getElementById('historyModal');
    modal.classList.add('show');
    renderHistory();
}

// 關閉瀏覽紀錄
function closeHistory() {
    const modal = document.getElementById('historyModal');
    modal.classList.remove('show');
}

// 記錄咖啡廳點擊
function recordCafeView(cafe) {
    window.historyManager.addHistory(cafe);
}

// 初始化瀏覽紀錄功能
function initHistory() {
    // 綁定開啟按鈕
    document.getElementById('historyBtn')?.addEventListener('click', openHistory);
    
    // 綁定關閉按鈕
    document.getElementById('closeHistoryModal')?.addEventListener('click', closeHistory);
    
    // 點擊背景關閉
    document.getElementById('historyModal')?.addEventListener('click', (e) => {
        if (e.target.id === 'historyModal') {
            closeHistory();
        }
    });

    // ESC 鍵關閉
    document.addEventListener('keydown', (e) => {
        if (e.key === 'Escape') {
            const modal = document.getElementById('historyModal');
            if (modal.classList.contains('show')) {
                closeHistory();
            }
        }
    });

    // 綁定卡片點擊事件以記錄瀏覽
    document.addEventListener('click', (e) => {
        const card = e.target.closest('.cafe-card');
        if (card && !e.target.closest('.favorite-btn') && 
            !e.target.closest('.compare-checkbox') && 
            !e.target.closest('.cafe-address')) {
            const cafeId = card.dataset.cafeId;
            
            console.log('🎯 點擊咖啡廳卡片，ID:', cafeId);
            
            // 從 DOM 中取得咖啡廳資訊
            const cafe = {
                id: cafeId,
                name: card.querySelector('.cafe-title')?.textContent.trim().split('\n')[0] || '',
                address: card.querySelector('.address-link')?.textContent.trim() || '',
                features: Array.from(card.querySelectorAll('.feature-tag')).map(tag => 
                    tag.textContent.trim()
                ),
                hashtags: Array.from(card.querySelectorAll('.hashtag')).map(tag => 
                    tag.textContent.trim().replace('#', '')
                ),
                score: parseFloat(card.querySelector('.cafe-score')?.textContent.trim()) || 0,
                url: card.dataset.cafeUrl || ''
            };

            recordCafeView(cafe);
        }
    });
}

// 匯出函式
window.historyModule = {
    init: initHistory,
    render: renderHistory,
    open: openHistory,
    close: closeHistory,
    record: recordCafeView
};