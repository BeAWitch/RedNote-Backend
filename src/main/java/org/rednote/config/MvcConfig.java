package org.rednote.config;

import lombok.RequiredArgsConstructor;
import org.rednote.interceptor.LoginInterceptor;
import org.rednote.interceptor.RefreshTokenInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@RequiredArgsConstructor
public class MvcConfig implements WebMvcConfigurer {

    private final StringRedisTemplate stringRedisTemplate;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 登录拦截器
        registry.addInterceptor(new LoginInterceptor())
                .excludePathPatterns(
                        "/web/auth/**",      // 登录
                        "/web/search/note/**",
                        "/web/search/record/**",
                        "/web/category/getCategoryTreeData/**",
                        "/uploads/**",       // 本地文件访问路径
                        "/swagger-ui.html",
                        "/web/ws/**",
                        "/swagger-ui/**",
                        "/v3/api-docs/**"
                ).order(1);
        // token 刷新拦截器
        registry.addInterceptor(new RefreshTokenInterceptor(stringRedisTemplate)).addPathPatterns("/**").order(0);
    }
}
