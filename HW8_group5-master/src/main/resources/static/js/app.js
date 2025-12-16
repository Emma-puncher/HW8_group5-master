// 主應用程式初始化
class CafeSearchApp {
    constructor() {
        this.initialized = false;
    }

    // 初始化應用程式
    async init() {
        if (this.initialized) return;

        try {
            console.log('Initializing Cafe Search App...');

            // 初始化各個模組
            this.initModules();

            // 設定全域事件監聽
            this.setupGlobalListeners();

            // 檢查後端連線
            await this.checkBackendConnection();

            this.initialized = true;
            console.log('Cafe Search App initialized successfully');

        } catch (error) {
            console.error('Failed to initialize app:', error);
            this.showConnectionError();
        }
    }

    // 初始化各個模組
    initModules() {
        // 初始化工具模組 (已載入)
        console.log('Utils module loaded');

        // 初始化收藏功能
        if (window.favoriteModule) {
            window.favoriteModule.init();
            console.log('Favorite module initialized');
        }

        // 初始化瀏覽紀錄
        if (window.historyModule) {
            window.historyModule.init();
            console.log('History module initialized');
        }

        // 初始化篩選功能
        if (window.filterModule) {
            window.filterModule.init();
            console.log('Filter module initialized');
        }

        // 初始化回報功能
        if (window.reportModule) {
            window.reportModule.init();
            console.log('Report module initialized');
        }

        // 初始化搜尋功能 (最後初始化,因為可能觸發 API 請求)
        if (window.searchModule) {
            window.searchModule.init();
            console.log('Search module initialized');
        }
    }

    // 設定全域事件監聽
    setupGlobalListeners() {
        // 監聽視窗大小變化
        window.addEventListener('resize', window.utils.debounce(() => {
            this.handleResize();
        }, 250));

        // 監聽線上/離線狀態
        window.addEventListener('online', () => {
            window.utils.showToast('網路連線已恢復', 'success');
        });

        window.addEventListener('offline', () => {
            window.utils.showToast('網路連線已中斷', 'error');
        });

        // 監聽頁面可見性變化
        document.addEventListener('visibilitychange', () => {
            if (document.visibilityState === 'visible') {
                this.handlePageVisible();
            }
        });

        // 防止表單預設提交行為
        document.querySelectorAll('form').forEach(form => {
            form.addEventListener('submit', (e) => {
                e.preventDefault();
            });
        });
    }

    // 檢查後端連線
    async checkBackendConnection() {
        try {
            // 直接嘗試呼叫實際的 API 來檢查連線
            const response = await fetch(`${window.utils.API_BASE_URL}/search?q=test`, {
                method: 'GET'
            }).catch(() => null);

            if (!response || !response.ok) {
                console.warn('Backend connection check failed, using mock data mode');
                this.useMockData = true;
            } else {
                console.log('✅ Backend connected successfully');
                this.useMockData = false;
            }
        } catch (error) {
            console.warn('Backend not available, using mock data mode');
            this.useMockData = true;
        }
    }

    // 處理視窗大小變化
    handleResize() {
        // 更新響應式相關的 UI
        console.log('Window resized:', window.innerWidth, 'x', window.innerHeight);
    }

    // 處理頁面重新可見
    handlePageVisible() {
        // 可以在這裡重新載入數據或更新狀態
        console.log('Page visible again');
    }

    // 顯示連線錯誤
    showConnectionError() {
        const errorHtml = `
            <div style="
                position: fixed;
                top: 50%;
                left: 50%;
                transform: translate(-50%, -50%);
                background: white;
                padding: 2rem;
                border-radius: 12px;
                box-shadow: 0 8px 24px rgba(0, 0, 0, 0.15);
                text-align: center;
                z-index: 9999;
            ">
                <i class="fas fa-exclamation-triangle" style="font-size: 3rem; color: #FF9800; margin-bottom: 1rem;"></i>
                <h2 style="margin-bottom: 1rem;">無法連線到伺服器</h2>
                <p style="color: #666; margin-bottom: 1.5rem;">請確認後端服務是否正在運行</p>
                <button onclick="location.reload()" style="
                    padding: 0.75rem 1.5rem;
                    background: #6B4423;
                    color: white;
                    border: none;
                    border-radius: 8px;
                    cursor: pointer;
                    font-weight: 600;
                ">
                    重新載入
                </button>
            </div>
        `;
        
        document.body.insertAdjacentHTML('beforeend', errorHtml);
    }
}

// 建立應用程式實例
const app = new CafeSearchApp();

// 等待 DOM 載入完成後初始化
if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', () => {
        app.init();
    });
} else {
    // DOM 已經載入完成
    app.init();
}

// 匯出應用程式實例供除錯使用
window.app = app;

// 開發模式輔助函式
if (window.location.hostname === 'localhost' || window.location.hostname === '127.0.0.1') {
    window.debug = {
        // 取得當前狀態
        getState: () => ({
            favorites: window.favoriteManager?.getFavorites() || [],
            history: window.historyManager?.getHistory() || [],
            filters: window.filterManager?.getFilterState() || {},
            comparison: window.comparisonManager?.getSelectedCafes() || [],
            currentResults: window.searchManager?.getCurrentResults() || []
        }),

        // 清除所有資料
        clearAll: () => {
            localStorage.clear();
            location.reload();
        },

        // 模擬搜尋
        mockSearch: (query) => {
            const searchInput = document.getElementById('searchInput');
            if (searchInput) {
                searchInput.value = query;
                window.searchModule.performSearch();
            }
        },

        // 新增測試收藏
        addTestFavorite: () => {
            const testCafe = {
                id: 'test-' + Date.now(),
                name: '測試咖啡廳',
                address: '台北市大安區測試路123號',
                features: ['不限時', '有插座', '有wifi'],
                hashtags: ['安靜', '適合讀書', '咖啡好喝'],
                score: 4.5
            };
            window.favoriteManager.addFavorite(testCafe);
        },

        // 顯示當前過濾器
        showFilters: () => {
            console.log('Current filters:', window.filterManager.getFilterState());
        }
    };

    console.log('%c🎯 Debug Tools Available', 'color: #6B4423; font-size: 16px; font-weight: bold;');
    console.log('Use window.debug for development helpers');
    console.log('Available commands:', Object.keys(window.debug));
}

// Service Worker 註冊 (可選,用於 PWA)
if ('serviceWorker' in navigator && window.location.protocol === 'https:') {
    window.addEventListener('load', () => {
        navigator.serviceWorker.register('/sw.js')
            .then(registration => {
                console.log('ServiceWorker registered:', registration);
            })
            .catch(error => {
                console.log('ServiceWorker registration failed:', error);
            });
    });
}