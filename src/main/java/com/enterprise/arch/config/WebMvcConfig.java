package com.enterprise.arch.config;

import com.enterprise.arch.interceptor.AuthenticationInterceptor;
import com.enterprise.arch.interceptor.PermissionInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    private final AuthenticationInterceptor authenticationInterceptor;
    private final PermissionInterceptor permissionInterceptor;

    public WebMvcConfig(AuthenticationInterceptor authenticationInterceptor, PermissionInterceptor permissionInterceptor) {
        this.authenticationInterceptor = authenticationInterceptor;
        this.permissionInterceptor = permissionInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(authenticationInterceptor).addPathPatterns("/api/**").order(0);
        registry.addInterceptor(permissionInterceptor).addPathPatterns("/api/**").order(1);
    }
}
