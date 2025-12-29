package org.rednote.common.interceptor;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.rednote.common.utils.UserHolder;
import org.springframework.stereotype.Component;

@Component
public class FeignUserContextInterceptor implements RequestInterceptor {

    @Override
    public void apply(RequestTemplate template) {
        Long userId = UserHolder.getUserId();
        if (userId != null) {
            template.header("userId", userId.toString());
        }
    }
}
