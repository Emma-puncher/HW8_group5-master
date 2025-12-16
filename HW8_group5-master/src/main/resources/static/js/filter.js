// 篩選管理器
class FilterManager {
    constructor() {
        this.selectedDistrict = '';
        this.selectedFeatures = new Set();
        this.listeners = [];
    }

    // 新增監聽器
    addListener(callback) {
        this.listeners.push(callback);
    }

    // 通知所有監聽器
    notifyListeners() {
        this.listeners.forEach(callback => callback(this.getFilterState()));
    }

    // 設定地區篩選
    setDistrict(district) {
        this.selectedDistrict = district;
        this.notifyListeners();
    }

    // 取得地區篩選
    getDistrict() {
        return this.selectedDistrict;
    }

    // 切換功能篩選
    toggleFeature(feature) {
        if (this.selectedFeatures.has(feature)) {
            this.selectedFeatures.delete(feature);
        } else {
            this.selectedFeatures.add(feature);
        }
        this.notifyListeners();
    }

    // 取得功能篩選
    getFeatures() {
        return Array.from(this.selectedFeatures);
    }

    // 檢查是否有功能被選中
    hasFeature(feature) {
        return this.selectedFeatures.has(feature);
    }

    // 清除所有篩選
    clearAll() {
        this.selectedDistrict = '';
        this.selectedFeatures.clear();
        this.notifyListeners();
    }

    // 取得篩選狀態
    getFilterState() {
        return {
            district: this.selectedDistrict,
            features: this.getFeatures()
        };
    }

    // 檢查是否有任何篩選
    hasAnyFilter() {
        return this.selectedDistrict !== '' || this.selectedFeatures.size > 0;
    }

    // 篩選咖啡廳列表
    filterCafes(cafes) {
        return cafes.filter(cafe => {
            // 地區篩選
            if (this.selectedDistrict && cafe.district !== this.selectedDistrict) {
                return false;
            }

            // 功能篩選 - 咖啡廳必須包含所有選中的功能
            if (this.selectedFeatures.size > 0) {
                const cafeFeatures = new Set(cafe.features || []);
                for (const feature of this.selectedFeatures) {
                    if (!cafeFeatures.has(feature)) {
                        return false;
                    }
                }
            }

            return true;
        });
    }

    // 從 URL 載入篩選狀態
    loadFromURL() {
        const district = window.utils.getQueryParam('district');
        const features = window.utils.getQueryParam('features');

        if (district) {
            this.selectedDistrict = district;
            const districtSelect = document.getElementById('districtFilter');
            if (districtSelect) {
                districtSelect.value = district;
            }
        }

        if (features) {
            const featureList = features.split(',');
            featureList.forEach(feature => {
                this.selectedFeatures.add(feature);
                const checkbox = document.querySelector(`input[name="feature"][value="${feature}"]`);
                if (checkbox) {
                    checkbox.checked = true;
                }
            });
        }
    }

    // 儲存篩選狀態到 URL
    saveToURL() {
        if (this.selectedDistrict) {
            window.utils.setQueryParam('district', this.selectedDistrict);
        } else {
            window.utils.setQueryParam('district', null);
        }

        if (this.selectedFeatures.size > 0) {
            window.utils.setQueryParam('features', Array.from(this.selectedFeatures).join(','));
        } else {
            window.utils.setQueryParam('features', null);
        }
    }
}

// 初始化篩選管理器
window.filterManager = new FilterManager();

// 應用篩選並更新結果
async function applyFilters() {
    const searchInput = document.getElementById('searchInput');
    const query = searchInput?.value.trim() || '';

    // 無論有沒有關鍵字，都使用搜尋功能（如果有篩選條件的話）
    const hasFilters = window.filterManager.hasAnyFilter();
    
    if (query || hasFilters) {
        // 使用搜尋功能（支援空關鍵字 + 篩選條件）
        if (window.searchModule && window.searchModule.performSearch) {
            await window.searchModule.performSearch();
        }
    } else {
        // 完全沒有搜尋條件和篩選時，顯示熱門推薦
        if (window.searchModule && window.searchModule.loadRecommendations) {
            await window.searchModule.loadRecommendations();
        }
    }

    // 儲存篩選狀態到 URL
    window.filterManager.saveToURL();
}

// 初始化篩選功能
function initFilters() {
    // 從 URL 載入篩選狀態
    window.filterManager.loadFromURL();

    // 地區篩選
    const districtFilter = document.getElementById('districtFilter');
    if (districtFilter) {
        districtFilter.addEventListener('change', (e) => {
            window.filterManager.setDistrict(e.target.value);
            applyFilters();
        });
    }

    // 功能篩選
    const featureCheckboxes = document.querySelectorAll('input[name="feature"]');
    featureCheckboxes.forEach(checkbox => {
        checkbox.addEventListener('change', (e) => {
            window.filterManager.toggleFeature(e.target.value);
            applyFilters();
        });
    });

    // 監聽篩選變化
    window.filterManager.addListener((filterState) => {
        // 可以在這裡添加其他需要響應篩選變化的邏輯
        console.log('Filter state changed:', filterState);
    });
}

// 重置篩選
function resetFilters() {
    // 重置地區選擇
    const districtFilter = document.getElementById('districtFilter');
    if (districtFilter) {
        districtFilter.value = '';
    }

    // 重置功能選擇
    const featureCheckboxes = document.querySelectorAll('input[name="feature"]');
    featureCheckboxes.forEach(checkbox => {
        checkbox.checked = false;
    });

    // 清除篩選狀態
    window.filterManager.clearAll();
    window.utils.setQueryParam('district', null);
    window.utils.setQueryParam('features', null);

    window.utils.showToast('已重置所有篩選', 'success');
}

// 取得篩選參數用於 API 請求
function getFilterParams() {
    const filterState = window.filterManager.getFilterState();
    const params = {};

    if (filterState.district) {
        params.district = filterState.district;
    }

    if (filterState.features.length > 0) {
        params.features = filterState.features.join(',');
    }

    return params;
}

// 匯出函式
window.filterModule = {
    init: initFilters,
    apply: applyFilters,
    reset: resetFilters,
    getParams: getFilterParams
};
