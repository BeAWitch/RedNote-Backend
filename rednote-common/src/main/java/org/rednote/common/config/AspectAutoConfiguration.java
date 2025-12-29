package org.rednote.common.config;

import org.rednote.common.aspect.LogAspect;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@Configuration
@EnableAspectJAutoProxy(proxyTargetClass = true)
public class AspectAutoConfiguration {

    @Bean
    public LogAspect logAspect() {
        return new LogAspect();
    }
}
