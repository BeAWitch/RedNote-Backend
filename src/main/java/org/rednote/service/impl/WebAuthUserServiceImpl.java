package org.rednote.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.lang.UUID;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.SecureUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.rednote.constant.AuthConstants;
import org.rednote.constant.RedisConstants;
import org.rednote.domain.dto.AuthUserDTO;
import org.rednote.domain.dto.Result;
import org.rednote.domain.entity.WebUser;
import org.rednote.enums.ResultCodeEnum;
import org.rednote.exception.RedNoteException;
import org.rednote.mapper.WebUserMapper;
import org.rednote.service.IWebAuthUserService;
import org.rednote.utils.AddressUtils;
import org.rednote.utils.IpUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class WebAuthUserServiceImpl extends ServiceImpl<WebUserMapper, WebUser> implements IWebAuthUserService {

    private final StringRedisTemplate stringRedisTemplate;

    /**
     * 用户登录
     *
     * @param authUserDTO 用户
     */
    @Override
    @Transactional
    public Map<String, Object> login(AuthUserDTO authUserDTO) {
        Map<String, Object> map = new HashMap<>();
        WebUser authUser;
        // 查询用户
        authUser = this.getOne(new QueryWrapper<WebUser>().eq("phone", authUserDTO.getPhone()));
        // 注册新用户
        if (ObjectUtil.isEmpty(authUser)) {
            authUser = this.register(authUserDTO);
        } else {
            // 校验登录密码
            if (!authUser.getPassword().equals(SecureUtil.md5(authUserDTO.getPassword()))) {
                throw new RedNoteException(ResultCodeEnum.ERROR_PASSWORD);
            }
        }
        // 更新登录信息
        authUser.setLoginDate(new Date());
        authUser.setLoginIp(IpUtils.getHostIp());
        authUser.setAddress(AddressUtils.getRealAddressByIP(authUser.getLoginIp()));
        this.updateById(authUser);
        // 缓存用户信息和 token
        this.setUserInfoAndToken(map, authUser);
        return map;
    }

    /**
     * 验证码登录
     *
     * @param authUserDTO 用户
     */
    @Override
    public Map<String, Object> loginByCode(AuthUserDTO authUserDTO) {
        Map<String, Object> map = new HashMap<>(2);
        WebUser currentUser;
        if (StrUtil.isNotBlank(authUserDTO.getPhone())) {
            currentUser = this.getOne(new QueryWrapper<WebUser>().eq("phone", authUserDTO.getPhone()));
        } else {
            currentUser = this.getOne(new QueryWrapper<WebUser>().eq("email", authUserDTO.getEmail()));
        }
        if (!this.checkCode(authUserDTO) || currentUser == null) {
            throw new RedNoteException(AuthConstants.LOGIN_FAIL);
        }
        this.setUserInfoAndToken(map, currentUser);
        return map;
    }

    /**
     * 发送验证码
     * TODO 调用第三方服务
     *
     * @param authUserDTO 用户
     */
    @Override
    public Result<?> sendCode(AuthUserDTO authUserDTO) {
        // 符合，生成验证码
        String code = RandomUtil.randomNumbers(6);

        // 保存验证码到 redis
        stringRedisTemplate.opsForValue().set(
                RedisConstants.LOGIN_CODE_KEY + authUserDTO.getPhone(),
                code,
                RedisConstants.LOGIN_CODE_TTL,
                TimeUnit.MINUTES);

        // 发送验证码
        // 调用第三方服务，省略
        log.debug("发送验证码成功，验证码：{}", code);

        return Result.ok();
    }

    /**
     * 用户注册
     *
     * @param authUserDTO 前台传递用户信息
     */
    @Override
    public WebUser register(AuthUserDTO authUserDTO) {
        WebUser user = BeanUtil.copyProperties(authUserDTO, WebUser.class);
        user.setHsId(Long.valueOf(RandomUtil.randomNumbers(10)));
        user.setUsername("momo");
        user.setAvatar(AuthConstants.DEFAULT_AVATAR);
        user.setUserCover(AuthConstants.DEFAULT_COVER);
        user.setPassword(SecureUtil.md5(authUserDTO.getPassword()));
        user.setLoginIp(IpUtils.getHostIp());
        user.setAddress(AddressUtils.getRealAddressByIP(user.getLoginIp()));
        user.setCreateTime(new Date());
        user.setUpdateTime(new Date());
        this.save(user);
        return user;
    }

    /**
     * 退出登录
     *
     * @param token token
     */
    @Override
    public void logout(String token) {
        String tokenKey = RedisConstants.LOGIN_USER_KEY + token;
        stringRedisTemplate.delete(tokenKey);
    }

    /**
     * 修改密码
     *
     * @param authUserDTO 用户
     */
    @Override
    public boolean updatePassword(AuthUserDTO authUserDTO) {
        if (!authUserDTO.getPassword().equals(authUserDTO.getCheckPassword())) {
            return false;
        }
        String pwd = SecureUtil.md5(authUserDTO.getPassword());
        WebUser user;
        if (StrUtil.isBlank(authUserDTO.getId())) {
            user = this.getOne(new QueryWrapper<WebUser>().eq("phone", authUserDTO.getPhone()).or().eq("email", authUserDTO.getEmail()));
        } else {
            user = this.getById(authUserDTO.getId());
        }
        user.setPassword(pwd);
        return this.updateById(user);
    }

    /**
     * 校验验证码
     */
    private boolean checkCode(AuthUserDTO authUserDTO) {
        String code;
        String phoneCode = stringRedisTemplate.opsForValue().get(RedisConstants.LOGIN_CODE_KEY + authUserDTO.getPhone());
        String emailCode = stringRedisTemplate.opsForValue().get(RedisConstants.LOGIN_CODE_KEY + authUserDTO.getEmail());
        if (StrUtil.isNotBlank(phoneCode)) {
            code = phoneCode;
        } else if (StrUtil.isNotBlank(emailCode)) {
            code = emailCode;
        } else {
            return false;
        }
        return code.equals(authUserDTO.getCode());
    }

    /**
     * 缓存用户信息和 token
     */
    private void setUserInfoAndToken(Map<String, Object> map, WebUser authUser) {
        // 保存用户信息到 redis 中
        // 生成 token 作为登录令牌
        String token = UUID.randomUUID().toString(true);
        // 存储
        String tokenKey = RedisConstants.LOGIN_USER_KEY + token;
        stringRedisTemplate.opsForValue().set(tokenKey, authUser.getId());
        // 设置 token 有效期
        stringRedisTemplate.expire(tokenKey, RedisConstants.LOGIN_USER_TTL, TimeUnit.MINUTES);

        map.put(AuthConstants.ACCESS_TOKEN, token);
        map.put(AuthConstants.USER_INFO, authUser);
    }
}
