package com.roadwarnings.narino.config;

import com.roadwarnings.narino.interceptor.RateLimitInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Configuración de Web MVC
 * Registra interceptores para rate limiting y otras funcionalidades cross-cutting
 */
@Configuration
@RequiredArgsConstructor
public class WebMvcConfig implements WebMvcConfigurer {

    private final RateLimitInterceptor rateLimitInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(rateLimitInterceptor)
                .addPathPatterns("/api/**")
                .excludePathPatterns(
                        "/api/auth/**",           // No rate limit en login/register
                        "/api/health/**",         // No rate limit en health checks
                        "/api/actuator/**",       // No rate limit en actuator
                        "/swagger-ui/**",         // No rate limit en Swagger UI
                        "/v3/api-docs/**"         // No rate limit en API docs
                );
    }
}
