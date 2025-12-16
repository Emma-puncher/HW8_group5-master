package com.example.GoogleQuery.config;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCache;
import org.springframework.cache.support.SimpleCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;

/**
 * CacheConfig - 快取配置
 * 使用 Spring Cache 來提升搜尋效能
 * 
 * 注意：這是使用 ConcurrentHashMap 的簡單實作
 * 生產環境建議使用 Redis 或 EhCache
 */
@Configuration
@EnableCaching  // 啟用 Spring Cache
public class CacheConfig {
    
    /**
     * 配置快取管理器
     * @return CacheManager
     */
    @Bean
    public CacheManager cacheManager() {
        SimpleCacheManager cacheManager = new SimpleCacheManager();
        
        // 定義快取區域
        cacheManager.setCaches(Arrays.asList(
            // 搜尋結果快取（10 分鐘過期）
            new ConcurrentMapCache("searchResults"),
            
            // 咖啡廳資料快取（長期）
            new ConcurrentMapCache("cafes"),
            
            // 關鍵字快取（長期）
            new ConcurrentMapCache("keywords"),
            
            // 篩選器選項快取（長期）
            new ConcurrentMapCache("filters"),
            
            // 比較結果快取（5 分鐘過期）
            new ConcurrentMapCache("comparisons"),
            
            // 推薦結果快取（30 分鐘過期）
            new ConcurrentMapCache("recommendations")
        ));
        
        return cacheManager;
    }
    
    /**
     * 自訂快取鍵生成器（可選）
     * 用於自訂如何生成快取的 key
     */
    /*
    @Bean
    public KeyGenerator customKeyGenerator() {
        return (target, method, params) -> {
            StringBuilder key = new StringBuilder();
            key.append(target.getClass().getSimpleName()).append("_");
            key.append(method.getName()).append("_");
            
            for (Object param : params) {
                if (param != null) {
                    key.append(param.toString()).append("_");
                }
            }
            
            return key.toString();
        };
    }
    */
}

/**
 * 快取使用說明：
 * 
 * 在 Service 層的方法上使用 @Cacheable 註解
 * 
 * 範例：
 * 
 * @Service
 * public class SearchService {
 *     
 *     @Cacheable(value = "searchResults", key = "#keyword")
 *     public ArrayList<SearchResult> search(String keyword) {
 *         // 搜尋邏輯
 *         return results;
 *     }
 *     
 *     @CacheEvict(value = "searchResults", allEntries = true)
 *     public void clearSearchCache() {
 *         // 清除搜尋快取
 *     }
 *     
 *     @CachePut(value = "cafes", key = "#cafe.id")
 *     public Cafe updateCafe(Cafe cafe) {
 *         // 更新咖啡廳資料
 *         return cafe;
 *     }
 * }
 * 
 * 
 * 快取策略說明：
 * 
 * 1. searchResults：搜尋結果快取
 *    - 快取鍵：搜尋關鍵字
 *    - 過期時間：10 分鐘
 *    - 適用於：頻繁搜尋相同關鍵字
 * 
 * 2. cafes：咖啡廳資料快取
 *    - 快取鍵：咖啡廳 ID
 *    - 過期時間：長期（直到手動清除）
 *    - 適用於：咖啡廳基本資料不常變動
 * 
 * 3. filters：篩選器選項快取
 *    - 快取鍵：篩選器類型（districts/features）
 *    - 過期時間：長期
 *    - 適用於：地區、功能列表不常變動
 * 
 * 4. recommendations：推薦結果快取
 *    - 快取鍵：推薦類型和參數
 *    - 過期時間：30 分鐘
 *    - 適用於：熱門推薦不需要即時更新
 * 
 * 
 * 如何清除快取：
 * 
 * 1. 手動清除：
 *    @Autowired
 *    private CacheManager cacheManager;
 *    
 *    public void clearCache() {
 *        cacheManager.getCache("searchResults").clear();
 *    }
 * 
 * 2. 定時清除：
 *    @Scheduled(fixedRate = 600000)  // 每 10 分鐘
 *    public void scheduledCacheClear() {
 *        clearCache();
 *    }
 * 
 * 
 * Redis 配置範例（生產環境推薦）：
 * 
 * pom.xml 新增依賴：
 * <dependency>
 *     <groupId>org.springframework.boot</groupId>
 *     <artifactId>spring-boot-starter-data-redis</artifactId>
 * </dependency>
 * 
 * application.properties：
 * spring.redis.host=localhost
 * spring.redis.port=6379
 * spring.cache.type=redis
 * spring.cache.redis.time-to-live=600000
 * 
 * @Configuration
 * @EnableCaching
 * public class RedisCacheConfig {
 *     
 *     @Bean
 *     public RedisCacheConfiguration cacheConfiguration() {
 *         return RedisCacheConfiguration.defaultCacheConfig()
 *             .entryTtl(Duration.ofMinutes(10))
 *             .disableCachingNullValues()
 *             .serializeValuesWith(
 *                 RedisSerializationContext.SerializationPair.fromSerializer(
 *                     new GenericJackson2JsonRedisSerializer()
 *                 )
 *             );
 *     }
 * }
 */

