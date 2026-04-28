package com.branchmanagement.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // ✅ THÊM DẤU ./ VÀO TRƯỚC uploads/ để đảm bảo Server quét đúng thư mục
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:./uploads/"); 
    }
}