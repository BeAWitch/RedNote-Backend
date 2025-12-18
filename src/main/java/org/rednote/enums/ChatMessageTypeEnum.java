package org.rednote.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.Getter;

@Getter
public enum ChatMessageTypeEnum {
    NOTICE(0, "通知"),
    TEXT(1, "文本"),
    IMAGE(2, "图片"),
    AUDIO(3, "语音"),
    VIDEO(4, "视频"),
    CUSTOM(5, "自定义");

    @EnumValue
    private final Integer code;
    private final String message;

    ChatMessageTypeEnum(Integer code, String message) {
        this.code = code;
        this.message = message;
    }
}
