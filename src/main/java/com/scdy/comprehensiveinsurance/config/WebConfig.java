package com.scdy.comprehensiveinsurance.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**") //允许所有的请求
                .allowedHeaders("*") //允许所有的头部
                .allowedMethods("*") //允许所有的请求方式
                .maxAge(30 * 1000);//设置post的探测请求options的有效期
    }
}