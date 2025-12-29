package org.rednote.common.config;

import org.rednote.common.interceptor.FeignUserContextInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FeignAutoConfiguration {

    /**
     * 自定义Feign请求拦截器
     */
    @Bean
    public FeignUserContextInterceptor feignUserContextInterceptor() {
        return new FeignUserContextInterceptor();
    }
}