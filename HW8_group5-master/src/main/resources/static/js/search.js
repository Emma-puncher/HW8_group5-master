// 搜尋管理器
class SearchManager {
    constructor() {
        this.currentResults = [];
        this.currentQuery = '';
    }

    // 執行搜尋
    async search(query, filterParams = {}) {
        try {
            // 先嘗試呼叫後端 API
            try {
                // 檢查是否有篩選參數
                const hasFilters = filterParams.district || filterParams.features;
                
                let data;
                if (hasFilters) {
                    // 使用進階搜尋 API（支援空關鍵字）
                    const params = new URLSearchParams({
                        keyword: query || '', // 允許空關鍵字
                        ...(filterParams.district && { districts: filterParams.district }),
                        ...(filterParams.features && { features: filterParams.features })
                    });
                    console.log('🔍 Calling advanced search API with filters:', params.toString());
                    data = await window.utils.apiRequest(`/search/advanced?${params}`);
                } else if (query) {
                    // 使用基礎搜尋 API（需要關鍵字）
                    const params = new URLSearchParams({ q: query });
                    console.log('🔍 Calling basic search API:', params.toString());
                    data = await window.utils.apiRequest(`/search?${params}`);
                } else {
                    // 沒有關鍵字也沒有篩選，回傳空結果
                    this.currentResults = [];
                    this.currentQuery = '';
                    return [];
                }
                
                this.currentResults = data.results || [];
                console.log(`✅ Found ${this.currentResults.length} results from backend API`);
                console.log('🔍 第一筆資料的 ID:', this.currentResults[0]?.id, '完整資料:', this.currentResults[0]);
            } catch (apiError) {
                // 如果 API 失敗,使用 Mock Data
                console.log('⚠️ Backend API not available, using mock data');
                this.currentResults = await window.mockDataLoader.searchCafes(query, filterParams);
            }
            
            this.currentQuery = query;
            return this.currentResults;
        } catch (error) {
            console.error('Search failed:', error);
            throw error;
        }
    }

    // 載入熱門推薦
    async loadRecommendations(filterParams = {}) {
        try {
            // 先嘗試呼叫後端 API
            try {
                // 格式化篩選參數
                const params = new URLSearchParams();
                if (filterParams.district) {
                    params.append('districts', filterParams.district);
                }
                if (filterParams.features) {
                    params.append('features', filterParams.features);
                }
                
                console.log('🌟 Calling recommendations API with filters:', params.toString());
                const data = await window.utils.apiRequest(`/recommendations?${params}`);
                console.log(`✅ Found ${data.recommendations?.length || 0} recommendations from backend API`);
                return data.recommendations || [];
            } catch (apiError) {
                // 如果 API 失敗,使用 Mock Data
                console.log('⚠️ Backend API not available, using mock data for recommendations');
                return await window.mockDataLoader.getRecommendations(filterParams);
            }
        } catch (error) {
            console.error('Load recommendations failed:', error);
            throw error;
        }
    }

    // 取得當前結果
    getCurrentResults() {
        return [...this.currentResults];
    }

    // 取得當前查詢
    getCurrentQuery() {
        return this.currentQuery;
    }
}

// 初始化搜尋管理器
window.searchManager = new SearchManager();

// 顯示載入中
function showLoading() {
    const loadingIndicator = document.getElementById('loadingIndicator');
    const resultsSection = document.getElementById('resultsSection');
    const recommendationsSection = document.getElementById('recommendationsSection');
    
    if (loadingIndicator) loadingIndicator.style.display = 'block';
    if (resultsSection) resultsSection.style.display = 'none';
    if (recommendationsSection) recommendationsSection.style.display = 'none';
}

// 隱藏載入中
function hideLoading() {
    const loadingIndicator = document.getElementById('loadingIndicator');
    if (loadingIndicator) loadingIndicator.style.display = 'none';
}

// 渲染搜尋結果
function renderSearchResults(results, query = '') {
    const resultsSection = document.getElementById('resultsSection');
    const resultsContainer = document.getElementById('resultsContainer');
    const resultsTitle = document.getElementById('resultsTitle');
    const recommendationsSection = document.getElementById('recommendationsSection');

    if (!resultsSection || !resultsContainer) return;

    // 隱藏熱門推薦
    if (recommendationsSection) {
        recommendationsSection.style.display = 'none';
    }

    if (results.length === 0) {
        resultsSection.style.display = 'block';
        resultsTitle.textContent = '搜尋結果';
        resultsContainer.innerHTML = window.utils.createEmptyState(
            'fa-search',
            '找不到相關咖啡廳',
            '試試其他關鍵字或調整篩選條件'
        );
        return;
    }

    // 更新標題
    resultsTitle.textContent = `搜尋結果 (${results.length} 間咖啡廳)`;

    // 渲染結果卡片
    resultsContainer.innerHTML = results.map(cafe => 
        window.utils.createCafeCard(cafe, {
            showCheckbox: true,
            showFavorite: true,
            highlightKeywords: query ? [query] : []
        })
    ).join('');

    resultsSection.style.display = 'block';

    // 綁定事件監聽器
    window.favoriteModule.attach();
    window.comparisonModule.attach();
    attachCafeCardClickListeners();
    
    // 更新勾選框狀態
    updateComparisonCheckboxes();
}

// 渲染熱門推薦
function renderRecommendations(recommendations) {
    const recommendationsSection = document.getElementById('recommendationsSection');
    const recommendationsContainer = document.getElementById('recommendationsContainer');

    if (!recommendationsSection || !recommendationsContainer) return;

    if (recommendations.length === 0) {
        recommendationsSection.style.display = 'none';
        return;
    }

    recommendationsContainer.innerHTML = recommendations.map(cafe => 
        window.utils.createCafeCard(cafe, {
            showCheckbox: true,
            showFavorite: true
        })
    ).join('');

    recommendationsSection.style.display = 'block';

    // 綁定事件監聽器
    window.favoriteModule.attach();
    window.comparisonModule.attach();
    attachCafeCardClickListeners();
    
    // 更新勾選框狀態
    updateComparisonCheckboxes();
}

// 更新比較勾選框狀態
function updateComparisonCheckboxes() {
    document.querySelectorAll('.compare-checkbox').forEach(checkbox => {
        const cafeId = checkbox.dataset.cafeId;
        checkbox.checked = window.comparisonManager.isSelected(cafeId);
    });
}

// 綁定咖啡廳卡片點擊事件
function attachCafeCardClickListeners() {
    document.querySelectorAll('.cafe-card').forEach(card => {
        card.addEventListener('click', (e) => {
            // 如果點擊的是收藏按鈕、勾選框或地址,不執行跳轉
            if (e.target.closest('.favorite-btn') || 
                e.target.closest('.compare-checkbox') || 
                e.target.closest('.cafe-address')) {
                return;
            }
            
            const cafeUrl = card.dataset.cafeUrl;
            if (cafeUrl && cafeUrl !== '#') {
                window.open(cafeUrl, '_blank', 'noopener,noreferrer');
            }
        });
    });
}

// 檢查搜尋詞相關性
async function checkSearchRelevance(query) {
    try {
        const params = new URLSearchParams({ q: query });
        const response = await window.utils.apiRequest(`/search/validate?${params}`);
        return {
            isRelevant: response.isRelevant,
            message: response.message,
            suggestions: response.suggestions || []
        };
    } catch (error) {
        console.warn('相關性檢查失敗,繼續執行搜尋:', error);
        // 如果檢查失敗,預設允許搜尋
        return { isRelevant: true, message: '', suggestions: [] };
    }
}

// 顯示相關性警告和建議
function showRelevanceWarning(query, relevanceResult) {
    const resultsSection = document.getElementById('resultsSection');
    const resultsContainer = document.getElementById('resultsContainer');
    const resultsTitle = document.getElementById('resultsTitle');
    const recommendationsSection = document.getElementById('recommendationsSection');

    if (!resultsSection || !resultsContainer) return;

    // 隱藏熱門推薦
    if (recommendationsSection) {
        recommendationsSection.style.display = 'none';
    }

    // 構建警告 HTML
    let warningHTML = `
        <div class="relevance-warning" style="padding: 20px; background-color: #fff3cd; border: 1px solid #ffc107; border-radius: 8px; margin-bottom: 20px;">
            <div style="display: flex; align-items: flex-start; gap: 12px;">
                <i class="fas fa-exclamation-triangle" style="color: #ff6b6b; font-size: 20px; margin-top: 4px; flex-shrink: 0;"></i>
                <div style="flex-grow: 1;">
                    <h3 style="margin: 0 0 8px 0; color: #333;">搜尋詞可能不相關</h3>
                    <p style="margin: 0 0 12px 0; color: #666; font-size: 14px;">${relevanceResult.message}</p>
                    
                    ${relevanceResult.suggestions && relevanceResult.suggestions.length > 0 ? `
                    <div style="margin-bottom: 12px;">
                        <p style="margin: 0 0 8px 0; color: #666; font-size: 14px; font-weight: 600;">建議搜尋:</p>
                        <div style="display: flex; flex-wrap: wrap; gap: 8px;">
                            ${relevanceResult.suggestions.map(suggestion => `
                                <button class="suggestion-btn" style="
                                    padding: 6px 12px;
                                    background-color: #fff;
                                    border: 1px solid #ffc107;
                                    border-radius: 4px;
                                    color: #ff6b6b;
                                    cursor: pointer;
                                    font-size: 13px;
                                    transition: all 0.2s;
                                " onclick="window.searchModule.suggestSearch('${suggestion}')" 
                                   title="搜尋: ${suggestion}">
                                    ${suggestion}
                                </button>
                            `).join('')}
                        </div>
                    </div>
                    ` : ''}
                    
                    <div style="display: flex; gap: 8px; margin-top: 12px;">
                        <button class="continue-btn" style="
                            padding: 8px 16px;
                            background-color: #ff6b6b;
                            color: white;
                            border: none;
                            border-radius: 4px;
                            cursor: pointer;
                            font-size: 14px;
                            transition: all 0.2s;
                        " onclick="window.searchModule.forceSearch('${query.replace(/'/g, "\\'")}')">
                            仍要搜尋
                        </button>
                        <button class="cancel-btn" style="
                            padding: 8px 16px;
                            background-color: transparent;
                            color: #666;
                            border: 1px solid #ddd;
                            border-radius: 4px;
                            cursor: pointer;
                            font-size: 14px;
                            transition: all 0.2s;
                        " onclick="window.searchModule.clear()">
                            清除搜尋
                        </button>
                    </div>
                </div>
            </div>
        </div>
    `;

    resultsTitle.textContent = '搜尋結果';
    resultsContainer.innerHTML = warningHTML;
    resultsSection.style.display = 'block';

    // 添加懸停效果
    document.querySelectorAll('.suggestion-btn, .continue-btn, .cancel-btn').forEach(btn => {
        btn.addEventListener('mouseover', function() {
            this.style.opacity = '0.8';
        });
        btn.addEventListener('mouseout', function() {
            this.style.opacity = '1';
        });
    });
}

// 執行搜尋
async function performSearch() {
    const searchInput = document.getElementById('searchInput');
    const query = searchInput?.value.trim() || '';

    // 驗證輸入
    const validation = window.utils.validateSearchInput(query);
    if (!validation.valid) {
        window.utils.showToast(validation.message, 'warning');
        return;
    }

    // 顯示載入中
    showLoading();

    try {
        // 檢查搜尋詞的相關性
        const relevanceResult = await checkSearchRelevance(query);
        
        if (!relevanceResult.isRelevant) {
            // 不相關的搜尋詞 - 顯示警告提示
            showRelevanceWarning(query, relevanceResult);
            hideLoading();
            return;
        }

        // 取得篩選參數
        const filterParams = window.filterModule.getParams();

        // 執行搜尋
        const results = await window.searchManager.search(query, filterParams);

        // 應用前端篩選 (以防後端沒有完全實作)
        const filteredResults = window.filterManager.filterCafes(results);

        // 渲染結果
        renderSearchResults(filteredResults, query);

        // 儲存查詢到 URL
        window.utils.setQueryParam('q', query);

    } catch (error) {
        console.error('Search error:', error);
        window.utils.showToast('搜尋時發生錯誤', 'error');
        
        // 顯示空結果
        renderSearchResults([], query);
    } finally {
        hideLoading();
    }
}

// 載入熱門推薦
async function loadRecommendations() {
    showLoading();

    try {
        // 取得篩選參數
        const filterParams = window.filterModule.getParams();

        // 載入推薦
        const recommendations = await window.searchManager.loadRecommendations(filterParams);

        // 應用前端篩選
        const filteredRecommendations = window.filterManager.filterCafes(recommendations);

        // 渲染推薦
        renderRecommendations(filteredRecommendations);

        // 隱藏搜尋結果區塊
        const resultsSection = document.getElementById('resultsSection');
        if (resultsSection) {
            resultsSection.style.display = 'none';
        }

    } catch (error) {
        console.error('Load recommendations error:', error);
        window.utils.showToast('載入推薦時發生錯誤', 'error');
        renderRecommendations([]);
    } finally {
        hideLoading();
    }
}

// 清除搜尋
function clearSearch() {
    const searchInput = document.getElementById('searchInput');
    const clearBtn = document.getElementById('clearSearch');
    
    if (searchInput) {
        searchInput.value = '';
        searchInput.focus();
    }
    
    if (clearBtn) {
        clearBtn.style.display = 'none';
    }

    // 清除 URL 參數
    window.utils.setQueryParam('q', null);

    // 重新載入推薦
    loadRecommendations();
}

// 初始化搜尋功能
function initSearch() {
    const searchInput = document.getElementById('searchInput');
    const searchBtn = document.getElementById('searchBtn');
    const clearBtn = document.getElementById('clearSearch');

    // 搜尋按鈕點擊
    if (searchBtn) {
        searchBtn.addEventListener('click', performSearch);
    }

    // Enter 鍵搜尋
    if (searchInput) {
        searchInput.addEventListener('keypress', (e) => {
            if (e.key === 'Enter') {
                performSearch();
            }
        });

        // 輸入時顯示/隱藏清除按鈕
        searchInput.addEventListener('input', (e) => {
            if (clearBtn) {
                clearBtn.style.display = e.target.value ? 'block' : 'none';
            }
        });
    }

    // 清除按鈕點擊
    if (clearBtn) {
        clearBtn.addEventListener('click', clearSearch);
    }

    // 從 URL 載入查詢
    const urlQuery = window.utils.getQueryParam('q');
    if (urlQuery && searchInput) {
        searchInput.value = urlQuery;
        if (clearBtn) {
            clearBtn.style.display = 'block';
        }
        performSearch();
    } else {
        // 載入熱門推薦
        loadRecommendations();
    }
}

// 匯出函式
window.searchModule = {
    init: initSearch,
    performSearch: performSearch,
    loadRecommendations: loadRecommendations,
    clear: clearSearch,
    renderResults: renderSearchResults,
    // 新增: 強制執行不相關詞的搜尋
    forceSearch: async function(query) {
        const searchInput = document.getElementById('searchInput');
        if (searchInput) {
            searchInput.value = query;
        }
        
        showLoading();
        try {
            // 取得篩選參數
            const filterParams = window.filterModule.getParams();
            
            // 直接執行搜尋,不進行相關性檢查
            const results = await window.searchManager.search(query, filterParams);
            const filteredResults = window.filterManager.filterCafes(results);
            renderSearchResults(filteredResults, query);
            window.utils.setQueryParam('q', query);
        } catch (error) {
            console.error('強制搜尋失敗:', error);
            window.utils.showToast('搜尋失敗', 'error');
            renderSearchResults([], query);
        } finally {
            hideLoading();
        }
    },
    // 新增: 使用建議詞彙搜尋
    suggestSearch: function(suggestion) {
        const searchInput = document.getElementById('searchInput');
        if (searchInput) {
            searchInput.value = suggestion;
            if (document.getElementById('clearSearch')) {
                document.getElementById('clearSearch').style.display = 'block';
            }
            performSearch();
        }
    }
};