package org.rednote.gateway.filter;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import lombok.RequiredArgsConstructor;
import org.rednote.common.constant.RedisConstants;
import org.rednote.common.enums.ResultCodeEnum;
import org.rednote.common.utils.RedisUtil;
import org.rednote.gateway.config.AuthProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
@EnableConfigurationProperties(AuthProperties.class)
public class AuthGlobalFilter implements GlobalFilter, Ordered {

    private final AuthProperties authProperties;
    private final AntPathMatcher antPathMatcher = new AntPathMatcher();
    private final RedisUtil redisUtil;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        // 获取 Request
        ServerHttpRequest request = exchange.getRequest();
        // 判断是否不需要拦截
        if (isExclude(request.getPath().toString())) {
            // 无需拦截，直接放行
            return chain.filter(exchange);
        }
        // 获取请求头中的 token
        String token = null;
        List<String> headers = request.getHeaders().get("accessToken");
        if (!CollUtil.isEmpty(headers)) {
            token = headers.get(0);
        }
        // 基于 token 获取 redis 中的用户
        String key = RedisConstants.LOGIN_USER_KEY + token;
        String userId = (String) redisUtil.get(key);
        // 判断用户是否存在
        if (StrUtil.isEmpty(userId)) {
            ServerHttpResponse response = exchange.getResponse();
            response.setRawStatusCode(ResultCodeEnum.TOKEN_NOT_EXIST.getCode());
            return response.setComplete();
        }
        // 刷新 token 有效期
        redisUtil.expire(key, RedisConstants.LOGIN_USER_TTL, TimeUnit.MINUTES);

        // 传递用户 ID
        ServerWebExchange serverWebExchange = exchange.mutate()
                .request(builder -> builder.header("userId", userId))
                .build();

        // 放行
        return chain.filter(serverWebExchange);
    }

    private boolean isExclude(String antPath) {
        for (String pathPattern : authProperties.getExcludePaths()) {
            if (antPathMatcher.match(pathPattern, antPath)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public int getOrder() {
        return 0;
    }
}