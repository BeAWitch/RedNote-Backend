package org.rednote.interaction.api.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.Getter;

@Getter
public enum ChatTypeEnum {

    PRIVATE(0, "私聊"),
    GROUP(1, "群聊");

    @EnumValue
    private final Integer code;
    private final String type;

    ChatTypeEnum(Integer code, String type) {
        this.code = code;
        this.type = type;
    }
}
