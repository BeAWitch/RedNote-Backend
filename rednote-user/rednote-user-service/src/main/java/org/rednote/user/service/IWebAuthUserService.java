package org.rednote.user.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.rednote.common.domain.dto.Result;
import org.rednote.user.api.dto.AuthUserDTO;
import org.rednote.user.api.entity.WebUser;

import java.util.Map;

/**
 * 用户认证
 */
public interface IWebAuthUserService extends IService<WebUser> {

    /**
     * 用户登录
     *
     * @param authUserDTO 用户
     */
    Map<String, Object> login(AuthUserDTO authUserDTO);

    /**
     * 验证码登录
     *
     * @param authUserDTO 用户
     */
    Map<String, Object> loginByCode(AuthUserDTO authUserDTO);

    /**
     * 发送验证码
     *
     * @param authUserDTO 用户
     */
    Result sendCode(AuthUserDTO authUserDTO);

    /**
     * 用户注册
     *
     * @param authUserDTO 前台传递用户信息
     */
    WebUser register(AuthUserDTO authUserDTO);

    /**
     * 退出登录
     *
     * @param token token
     */
    void logout(String token);

    /**
     * 修改密码
     *
     * @param authUserDTO 用户
     */
    boolean updatePassword(AuthUserDTO authUserDTO);

}
