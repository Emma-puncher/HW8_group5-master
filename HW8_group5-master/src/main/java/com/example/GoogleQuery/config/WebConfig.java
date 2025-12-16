package com.example.GoogleQuery.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.*;

/**
 * WebConfig - Web 相關配置
 * 設定 CORS、攔截器、資源處理器等
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {
    
    /**
     * 設定 CORS（跨域資源共享）
     * 允許前端從不同域名訪問 API
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")  // 所有 /api 開頭的路徑
                .allowedOrigins("*")     // 允許所有來源（開發環境）
                                        // 生產環境建議改為：.allowedOrigins("https://yourdomain.com")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(false)  // 不允許攜帶憑證（因為允許所有來源）
                .maxAge(3600);           // 預檢請求快取時間（秒）
    }
    
    /**
     * 設定靜態資源處理器
     * 將靜態資源（CSS, JS, 圖片）映射到特定路徑
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 靜態資源路徑
        registry.addResourceHandler("/static/**")
                .addResourceLocations("classpath:/static/")
                .setCachePeriod(3600);  // 快取 1 小時
        
        // 圖片資源
        registry.addResourceHandler("/images/**")
                .addResourceLocations("classpath:/static/images/")
                .setCachePeriod(86400); // 快取 1 天
        
        // CSS 資源
        registry.addResourceHandler("/css/**")
                .addResourceLocations("classpath:/static/css/")
                .setCachePeriod(3600);
        
        // JavaScript 資源
        registry.addResourceHandler("/js/**")
                .addResourceLocations("classpath:/static/js/")
                .setCachePeriod(3600);
    }
    
    /**
     * 設定視圖控制器
     * 直接將 URL 映射到視圖，不需要 Controller
     */
    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        // 首頁
        registry.addViewController("/").setViewName("forward:/static/index.html");
        registry.addViewController("/index").setViewName("forward:/static/index.html");
    }
    
    /**
     * 設定路徑匹配規則
     */
    @Override
    public void configurePathMatch(PathMatchConfigurer configurer) {
        // 禁用尾隨斜線匹配（/api/search 和 /api/search/ 視為不同）
        configurer.setUseTrailingSlashMatch(false);
    }
    
    /**
     * 設定內容協商
     */
    @Override
    public void configureContentNegotiation(ContentNegotiationConfigurer configurer) {
        configurer
            .favorParameter(false)  // 不使用 URL 參數來指定媒體類型
            .ignoreAcceptHeader(false)  // 使用 Accept header
            .defaultContentType(org.springframework.http.MediaType.APPLICATION_JSON);  // 預設返回 JSON
    }
    
    /**
     * 自訂 MessageConverter（可選）
     * 用於自訂 JSON 序列化行為
     */
    /*
    @Override
    public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
        // 使用 Gson 替代預設的 Jackson
        GsonHttpMessageConverter gsonConverter = new GsonHttpMessageConverter();
        Gson gson = new GsonBuilder()
                .setDateFormat("yyyy-MM-dd HH:mm:ss")
                .create();
        gsonConverter.setGson(gson);
        converters.add(gsonConverter);
    }
    */
}

