package org.rednote.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.groups.Default;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.rednote.domain.dto.AuthUserDTO;
import org.rednote.domain.dto.Result;
import org.rednote.domain.entity.WebUser;
import org.rednote.service.IWebAuthUserService;
import org.rednote.validator.group.AuthValidGroup;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/web/auth")
@Slf4j
@RequiredArgsConstructor
@Tag(name = "认证管理", description = "用户认证相关接口")
public class WebAuthController {

    private final IWebAuthUserService authUserService;

    @Operation(summary = "用户登录", description = "用户账号密码登录")
    @PostMapping("login")
    public Result<Map<String, Object>> login(@Parameter(description = "用户信息")
                                                 @RequestBody @Validated(AuthValidGroup.Password.class)
                                                 AuthUserDTO authUserDTO) {
        Map<String, Object> map = authUserService.login(authUserDTO);
        return Result.ok(map);
    }

    @Operation(summary = "验证码登录", description = "用户通过验证码登录")
    @PostMapping("loginByCode")
    public Result<Map<String, Object>> loginByCode(@Parameter(description = "用户信息") @RequestBody
                                                       @Validated(AuthValidGroup.Phone.class)
                                                       AuthUserDTO authUserDTO) {
        Map<String, Object> map = authUserService.loginByCode(authUserDTO);
        return Result.ok(map);
    }

    @Operation(summary = "用户注册", description = "新用户注册账号")
    @PostMapping("register")
    public Result<WebUser> register(@Parameter(description = "用户信息") @RequestBody @Validated(Default.class) AuthUserDTO authUserDTO) {
        WebUser user = authUserService.register(authUserDTO);
        return Result.ok(user);
    }

    @Operation(summary = "退出登录", description = "用户退出登录")
    @GetMapping("logout")
    public Result<String> loginOut(@Parameter(description = "token") String token) {
        authUserService.logout(token);
        return Result.ok("退出成功");
    }

    @Operation(summary = "修改密码", description = "用户修改登录密码")
    @PostMapping("updatePassword")
    public Result<Boolean> updatePassword(@Parameter(description = "用户登录信息") @RequestBody @Validated(Default.class) AuthUserDTO authUserDTO) {
        Boolean flag = authUserService.updatePassword(authUserDTO);
        return Result.ok(flag);
    }
}
