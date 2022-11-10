package com.atguigu.gmall.gateway.config;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

@Configuration
public class CorsConfig {


    @Bean
    public CorsWebFilter corsWebFilter(){

        //创建跨域配置对象
        CorsConfiguration  corsConfiguration=new CorsConfiguration();

        //允许访问的域
        corsConfiguration.addAllowedOrigin("*");
        //允许携带的头
        corsConfiguration.addAllowedHeader("*");
        //允许访问的方式
        corsConfiguration.addAllowedMethod("*");

        corsConfiguration.setAllowCredentials(true);


        UrlBasedCorsConfigurationSource configurationSource=new UrlBasedCorsConfigurationSource();
        //设置
        configurationSource.registerCorsConfiguration("/**",corsConfiguration);

        return new CorsWebFilter(configurationSource);
    }
}
