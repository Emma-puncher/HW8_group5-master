// API 基礎 URL
const API_BASE_URL = 'http://localhost:8080/api';

// Toast 通知
function showToast(message, type = 'info') {
    const toast = document.getElementById('toast');
    toast.textContent = message;
    toast.className = `toast show ${type}`;
    
    setTimeout(() => {
        toast.classList.remove('show');
    }, 3000);
}

// API 請求封裝
async function apiRequest(endpoint, options = {}) {
    try {
        const response = await fetch(`${API_BASE_URL}${endpoint}`, {
            headers: {
                'Content-Type': 'application/json',
                ...options.headers
            },
            ...options
        });

        if (!response.ok) {
            throw new Error(`HTTP error! status: ${response.status}`);
        }

        return await response.json();
    } catch (error) {
        console.error('API request failed:', error);
        showToast('連線失敗，請稍後再試', 'error');
        throw error;
    }
}

// 格式化日期時間
function formatDateTime(date) {
    const now = new Date();
    const targetDate = new Date(date);
    const diff = now - targetDate;
    const minutes = Math.floor(diff / 60000);
    const hours = Math.floor(diff / 3600000);
    const days = Math.floor(diff / 86400000);

    if (minutes < 1) return '剛剛';
    if (minutes < 60) return `${minutes} 分鐘前`;
    if (hours < 24) return `${hours} 小時前`;
    if (days < 7) return `${days} 天前`;
    
    return targetDate.toLocaleDateString('zh-TW', {
        year: 'numeric',
        month: '2-digit',
        day: '2-digit'
    });
}

// 高亮關鍵字
function highlightKeywords(text, keywords) {
    if (!keywords || keywords.length === 0) return text;
    
    let highlightedText = text;
    keywords.forEach(keyword => {
        const regex = new RegExp(`(${keyword})`, 'gi');
        highlightedText = highlightedText.replace(regex, '<span class="highlight">$1</span>');
    });
    
    return highlightedText;
}

// 截取預覽文字
function truncateText(text, maxLength = 150) {
    if (text.length <= maxLength) return text;
    return text.substring(0, maxLength) + '...';
}

// 建立咖啡廳卡片 HTML
function createCafeCard(cafe, options = {}) {
    const {
        showCheckbox = false,
        showFavorite = true,
        highlightKeywords: keywords = []
    } = options;

    const isFavorite = window.favoriteManager ? 
        window.favoriteManager.isFavorite(cafe.id) : false;

    const previewText = cafe.preview || cafe.description || '';
    const highlightedPreview = keywords.length > 0 ?
        highlightKeywords(truncateText(previewText), keywords) :
        truncateText(previewText);

    // 生成 Google Maps 連結
    const mapsUrl = `https://www.google.com/maps/search/?api=1&query=${encodeURIComponent(cafe.address)}`;
    
    // 咖啡廳網站連結
    const cafeUrl = cafe.url || '#';

    return `
        <div class="cafe-card" data-cafe-id="${cafe.id}" data-cafe-url="${cafeUrl}">
            <div class="cafe-card-header">
                <div class="cafe-title-wrapper">
                    <div class="cafe-title">
                        ${cafe.name}
                        ${cafe.score ? `
                            <span class="cafe-score">
                                <i class="fas fa-star"></i>
                                ${cafe.score.toFixed(1)}
                            </span>
                        ` : ''}
                    </div>
                    ${previewText ? `
                        <div class="cafe-preview-inline">
                            ${highlightedPreview}
                        </div>
                    ` : ''}
                </div>
                <div class="cafe-actions">
                    
                    ${showFavorite ? `
                        <button 
                            class="icon-btn favorite-btn ${isFavorite ? 'active' : ''}" 
                            data-cafe-id="${cafe.id}"
                            aria-label="${isFavorite ? '取消收藏' : '加入收藏'}"
                            onclick="event.stopPropagation();"
                        >
                            <i class="fas fa-heart"></i>
                        </button>
                    ` : ''}
                </div>
            </div>

            ${cafe.hashtags && cafe.hashtags.length > 0 ? `
                <div class="cafe-hashtags">
                    ${cafe.hashtags.map(tag => `
                        <span class="hashtag">
                            <i class="fas fa-hashtag"></i>
                            ${tag}
                        </span>
                    `).join('')}
                </div>
            ` : ''}

            ${cafe.address ? `
                <div class="cafe-address" onclick="event.stopPropagation(); window.open('${mapsUrl}', '_blank');">
                    <i class="fas fa-map-marker-alt"></i>
                    <span class="address-link">
                        ${cafe.address}
                    </span>
                </div>
            ` : ''}

            ${cafe.features && cafe.features.length > 0 ? `
                <div class="cafe-features">
                    ${cafe.features.map(feature => {
                        const iconMap = {
                            '不限時': 'fa-clock',
                            '有插座': 'fa-plug',
                            '安靜': 'fa-volume-mute',
                            '有wifi': 'fa-wifi'
                        };
                        return `
                            <span class="feature-tag">
                                <i class="fas ${iconMap[feature] || 'fa-check'}"></i>
                                ${feature}
                            </span>
                        `;
                    }).join('')}
                </div>
            ` : ''}
        </div>
    `;
}

// 空狀態 HTML
function createEmptyState(icon, title, message) {
    return `
        <div class="empty-state">
            <i class="fas ${icon}"></i>
            <h3>${title}</h3>
            <p>${message}</p>
        </div>
    `;
}

// 防抖函數
function debounce(func, wait) {
    let timeout;
    return function executedFunction(...args) {
        const later = () => {
            clearTimeout(timeout);
            func(...args);
        };
        clearTimeout(timeout);
        timeout = setTimeout(later, wait);
    };
}

// 節流函數
function throttle(func, limit) {
    let inThrottle;
    return function(...args) {
        if (!inThrottle) {
            func.apply(this, args);
            inThrottle = true;
            setTimeout(() => inThrottle = false, limit);
        }
    };
}

// 驗證搜尋輸入
function validateSearchInput(query) {
    if (!query || query.trim().length === 0) {
        return { valid: false, message: '請輸入搜尋關鍵字' };
    }
    
    if (query.trim().length < 2) {
        return { valid: false, message: '關鍵字至少需要 2 個字元' };
    }
    
    if (query.length > 100) {
        return { valid: false, message: '關鍵字過長，請縮短至 100 字元以內' };
    }
    
    return { valid: true };
}

// 取得查詢參數
function getQueryParam(param) {
    const urlParams = new URLSearchParams(window.location.search);
    return urlParams.get(param);
}

// 設定查詢參數
function setQueryParam(param, value) {
    const url = new URL(window.location);
    if (value) {
        url.searchParams.set(param, value);
    } else {
        url.searchParams.delete(param);
    }
    window.history.pushState({}, '', url);
}

// 複製到剪貼簿
async function copyToClipboard(text) {
    try {
        await navigator.clipboard.writeText(text);
        showToast('已複製到剪貼簿', 'success');
        return true;
    } catch (error) {
        console.error('Copy failed:', error);
        showToast('複製失敗', 'error');
        return false;
    }
}

// 匯出工具函式
window.utils = {
    showToast,
    apiRequest,
    formatDateTime,
    highlightKeywords,
    truncateText,
    createCafeCard,
    createEmptyState,
    debounce,
    throttle,
    validateSearchInput,
    getQueryParam,
    setQueryParam,
    copyToClipboard,
    API_BASE_URL
};