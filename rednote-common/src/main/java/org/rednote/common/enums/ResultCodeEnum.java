package org.rednote.common.enums;

import lombok.Getter;

/**
 * 统一返回结果状态信息
 */
@Getter
public enum ResultCodeEnum {

    SUCCESS(200, "成功"),
    FAIL(201, "失败"),
    TOKEN_FAIL(501, "token 异常"),
    TOKEN_NOT_EXIST(401, "token 过期"),
    ERROR_PASSWORD(502, "密码有误，请检查重新输入"),

    NOT_NULL(10001, "为空");

    private final Integer code;

    private final String message;

    ResultCodeEnum(Integer code, String message) {
        this.code = code;
        this.message = message;
    }
}
