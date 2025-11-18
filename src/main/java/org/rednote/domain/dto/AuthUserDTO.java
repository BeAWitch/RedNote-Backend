package org.rednote.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import org.rednote.validator.group.AuthValidGroup;

import java.io.Serializable;

@Schema(name = "登录用户 DTO")
@Data
public class AuthUserDTO implements Serializable {

    @Schema(description = "用户 id")
    private String id;

    @Schema(description = "用户名")
    private String username;

    @Schema(description = "登录密码")
    @Pattern(regexp = "^(?![0-9]+$)(?![a-zA-Z]+$)[0-9A-Za-z]{8,20}$",
            message = "请输入8-20位由字母和数字组成的密码",
            groups = AuthValidGroup.Password.class)
    private String password;

    @Schema(description = "校验密码")
    private String checkPassword;

    @Schema(description = "手机号")
    @Pattern(regexp = "^1[0-9]{10}$",
            message = "手机号格式有误",
            groups = AuthValidGroup.Phone.class)
    private String phone;

    @Schema(description = "邮箱")
    private String email;

    @Schema(description = "登录验证码")
    private String code;
}
