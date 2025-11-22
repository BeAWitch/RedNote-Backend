package org.rednote.interceptor;

import cn.hutool.core.util.StrUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.rednote.constant.RedisConstants;
import org.rednote.utils.UserHolder;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.concurrent.TimeUnit;

public class RefreshTokenInterceptor implements HandlerInterceptor {

    private final StringRedisTemplate stringRedisTemplate;

    public RefreshTokenInterceptor(StringRedisTemplate stringRedisTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        // 获取请求头中的 token
        String token = request.getHeader("accessToken");
        if (StrUtil.isBlank(token)) {
            return true;
        }
        // 基于 token 获取 redis 中的用户
        String key  = RedisConstants.LOGIN_USER_KEY + token;
        String userId = stringRedisTemplate.opsForValue().get(key);
        // 判断用户是否存在
        if (StrUtil.isEmpty(userId)) {
            return true;
        }
        // 存在，保存用户 id 到 ThreadLocal
        UserHolder.setUserId(userId);
        // 刷新 token 有效期
        stringRedisTemplate.expire(key, RedisConstants.LOGIN_USER_TTL, TimeUnit.MINUTES);
        // 放行
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        // 移除用户
        UserHolder.removeUserId();
    }
}
