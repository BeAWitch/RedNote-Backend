package org.rednote.recommendation.api.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.Getter;

/**
 * 用户操作类型枚举
 */

@Getter
public enum UserOperationEnum {
    LIKE(0, "点赞"),
    FAVORITE(1, "收藏"),
    COMMENT(2, "评论"),
    ;

    @EnumValue
    private final Integer code;
    private final String message;

    UserOperationEnum(Integer code, String message) {
        this.code = code;
        this.message = message;
    }
}
