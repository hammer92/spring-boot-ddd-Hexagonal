package com.pragma.config;

import com.pragma.shared.config.AuthenticationProperties;
import com.pragma.shared.security.GoogleAuthInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {

    private final GoogleAuthInterceptor googleAuthInterceptor;
    private final AuthenticationProperties authProperties;

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins("*") // O tu frontend: http://localhost:4321
                .allowedMethods("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS") // <--- Incluye PATCH
                .allowedHeaders("*");
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(googleAuthInterceptor)
                .addPathPatterns(authProperties.getIncludePathPatterns().toArray(new String[0]))
                .excludePathPatterns(authProperties.getExcludePathPatterns().toArray(new String[0]));
    }
}
