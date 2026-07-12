// gateway-service/src/main/java/.../config/CorsConfig.java
package com.university_digital_library.gateway_service.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

@Configuration
public class CorsConfig {

    @Bean
    public CorsWebFilter corsWebFilter() {
        CorsConfiguration corsConfig = new CorsConfiguration();
        
        // Cho phép frontend
        corsConfig.addAllowedOrigin("http://localhost:5173");
        corsConfig.addAllowedOrigin("http://localhost:3000");
        
        // Cho phép tất cả methods
        corsConfig.addAllowedMethod("*");
        
        // Cho phép tất cả headers
        corsConfig.addAllowedHeader("*");
        
        // Cho phép credentials
        corsConfig.setAllowCredentials(true);
        
        // Expose headers
        corsConfig.addExposedHeader("Authorization");
        corsConfig.addExposedHeader("Content-Disposition");
        
        // Cache CORS preflight
        corsConfig.setMaxAge(3600L);
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", corsConfig);
        
        return new CorsWebFilter(source);
    }
}
