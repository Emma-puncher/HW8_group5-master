// 比較管理器
class ComparisonManager {
    constructor() {
        this.maxItems = 3; // 最多比較 3 間
        this.selectedCafes = [];
        this.listeners = [];
    }

    // 新增監聽器
    addListener(callback) {
        this.listeners.push(callback);
    }

    // 通知所有監聽器
    notifyListeners() {
        this.listeners.forEach(callback => callback(this.selectedCafes));
    }

    // 檢查是否已選擇
    isSelected(cafeId) {
        return this.selectedCafes.some(cafe => cafe.id === cafeId);
    }

    // 新增咖啡廳到比較清單
    addCafe(cafe) {
        if (this.isSelected(cafe.id)) {
            return false;
        }

        if (this.selectedCafes.length >= this.maxItems) {
            window.utils.showToast(`最多只能比較 ${this.maxItems} 間咖啡廳`, 'warning');
            return false;
        }

        this.selectedCafes.push(cafe);
        this.notifyListeners();
        return true;
    }

    // 移除咖啡廳
    removeCafe(cafeId) {
        this.selectedCafes = this.selectedCafes.filter(cafe => cafe.id !== cafeId);
        this.notifyListeners();
        return true;
    }

    // 切換選擇狀態
    toggleCafe(cafe) {
        if (this.isSelected(cafe.id)) {
            return this.removeCafe(cafe.id);
        } else {
            return this.addCafe(cafe);
        }
    }

    // 取得選中的咖啡廳
    getSelectedCafes() {
        return [...this.selectedCafes];
    }

    // 取得數量
    getCount() {
        return this.selectedCafes.length;
    }

    // 清空選擇
    clearAll() {
        this.selectedCafes = [];
        this.notifyListeners();
        
        // 取消所有勾選框
        document.querySelectorAll('.compare-checkbox:checked').forEach(checkbox => {
            checkbox.checked = false;
        });
    }

    // 檢查是否可以比較
    canCompare() {
        return this.selectedCafes.length >= 2;
    }
}

// 初始化比較管理器
window.comparisonManager = new ComparisonManager();

// 更新比較按鈕狀態
function updateCompareButton() {
    const compareBtn = document.getElementById('compareBtn');
    const compareCount = document.getElementById('compareCount');
    
    if (compareBtn && compareCount) {
        const count = window.comparisonManager.getCount();
        const canCompare = window.comparisonManager.canCompare();
        
        compareCount.textContent = count;
        compareBtn.disabled = !canCompare;
        
        if (canCompare) {
            compareBtn.style.opacity = '1';
        } else {
            compareBtn.style.opacity = '0.6';
        }
    }
}

// 生成比較表格
function generateComparisonTable(cafes) {
    if (cafes.length < 2) {
        return '<p style="text-align: center; padding: 2rem;">請至少選擇 2 間咖啡廳進行比較</p>';
    }

    // 收集所有可能的功能
    const allFeatures = new Set();
    cafes.forEach(cafe => {
        (cafe.features || []).forEach(feature => allFeatures.add(feature));
    });

    const featureList = Array.from(allFeatures);

    return `
        <table class="comparison-table">
            <thead>
                <tr>
                    <th style="width: 150px;">項目</th>
                    ${cafes.map(cafe => `
                        <th>${cafe.name}</th>
                    `).join('')}
                </tr>
            </thead>
            <tbody>
                <tr>
                    <td><strong>評分</strong></td>
                    ${cafes.map(cafe => `
                        <td>
                            <span class="cafe-score">
                                <i class="fas fa-star"></i>
                                ${cafe.score ? cafe.score.toFixed(1) : 'N/A'}
                            </span>
                        </td>
                    `).join('')}
                </tr>
                <tr>
                    <td><strong>地址</strong></td>
                    ${cafes.map(cafe => `
                        <td style="font-size: 0.9rem;">
                            <a href="https://www.google.com/maps/search/?api=1&query=${encodeURIComponent(cafe.address)}" 
                               target="_blank" 
                               rel="noopener noreferrer"
                               style="color: var(--primary-color); text-decoration: none;">
                                ${cafe.address || 'N/A'}
                            </a>
                        </td>
                    `).join('')}
                </tr>
                ${featureList.map(feature => `
                    <tr>
                        <td><strong>${feature}</strong></td>
                        ${cafes.map(cafe => {
                            const hasFeature = (cafe.features || []).includes(feature);
                            return `
                                <td class="${hasFeature ? 'has-feature' : 'no-feature'}">
                                    <i class="fas fa-${hasFeature ? 'check-circle' : 'times-circle'}"></i>
                                    ${hasFeature ? '是' : '否'}
                                </td>
                            `;
                        }).join('')}
                    </tr>
                `).join('')}
                <tr>
                    <td><strong>標籤</strong></td>
                    ${cafes.map(cafe => `
                        <td>
                            ${(cafe.hashtags || []).slice(0, 3).map(tag => `
                                <span class="hashtag" style="font-size: 0.8rem; margin: 2px;">
                                    <i class="fas fa-hashtag"></i>
                                    ${tag}
                                </span>
                            `).join('')}
                        </td>
                    `).join('')}
                </tr>
            </tbody>
        </table>
    `;
}

// 渲染比較結果
function renderComparison() {
    const container = document.getElementById('comparisonContent');
    const selectedCafes = window.comparisonManager.getSelectedCafes();

    if (selectedCafes.length === 0) {
        container.innerHTML = window.utils.createEmptyState(
            'fa-balance-scale',
            '尚未選擇咖啡廳',
            '請勾選想要比較的咖啡廳 (2-3 間)'
        );
        return;
    }

    container.innerHTML = `
        <div class="comparison-header" style="margin-bottom: 1rem; display: flex; justify-content: space-between; align-items: center;">
            <p style="color: var(--text-secondary);">
                已選擇 ${selectedCafes.length} 間咖啡廳
            </p>
            <button class="btn-small btn-danger" id="clearComparison">
                <i class="fas fa-times"></i> 清除選擇
            </button>
        </div>
        ${generateComparisonTable(selectedCafes)}
    `;

    // 綁定清除按鈕
    document.getElementById('clearComparison')?.addEventListener('click', () => {
        window.comparisonManager.clearAll();
        renderComparison();
        updateCompareButton();
    });
}

// 開啟比較視窗
function openComparison() {
    const modal = document.getElementById('comparisonModal');
    
    if (!window.comparisonManager.canCompare()) {
        window.utils.showToast('請至少選擇 2 間咖啡廳', 'warning');
        return;
    }

    modal.classList.add('show');
    renderComparison();
}

// 關閉比較視窗
function closeComparison() {
    const modal = document.getElementById('comparisonModal');
    modal.classList.remove('show');
}

// 綁定比較勾選框
function attachComparisonListeners() {
    document.querySelectorAll('.compare-checkbox').forEach(checkbox => {
        checkbox.addEventListener('change', (e) => {
            const cafeId = checkbox.dataset.cafeId;
            const card = checkbox.closest('.cafe-card');
            
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
                score: parseFloat(card.querySelector('.cafe-score')?.textContent.trim()) || 0
            };

            if (e.target.checked) {
                if (!window.comparisonManager.addCafe(cafe)) {
                    e.target.checked = false;
                }
            } else {
                window.comparisonManager.removeCafe(cafeId);
            }

            updateCompareButton();
        });
    });
}

// 初始化比較功能
function initComparison() {
    // 監聽比較狀態變化
    window.comparisonManager.addListener(() => {
        updateCompareButton();
    });

    // 綁定比較按鈕
    document.getElementById('compareBtn')?.addEventListener('click', openComparison);
    
    // 綁定關閉按鈕
    document.getElementById('closeComparisonModal')?.addEventListener('click', closeComparison);
    
    // 點擊背景關閉
    document.getElementById('comparisonModal')?.addEventListener('click', (e) => {
        if (e.target.id === 'comparisonModal') {
            closeComparison();
        }
    });

    // ESC 鍵關閉
    document.addEventListener('keydown', (e) => {
        if (e.key === 'Escape') {
            const modal = document.getElementById('comparisonModal');
            if (modal.classList.contains('show')) {
                closeComparison();
            }
        }
    });

    // 初始化按鈕狀態
    updateCompareButton();
}

// 匯出函式
window.comparisonModule = {
    init: initComparison,
    attach: attachComparisonListeners,
    render: renderComparison,
    open: openComparison,
    close: closeComparison
};