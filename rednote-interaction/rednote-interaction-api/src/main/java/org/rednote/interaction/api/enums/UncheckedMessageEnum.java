package org.rednote.interaction.api.enums;

import lombok.Getter;

@Getter
public enum UncheckedMessageEnum {

    LIKE_OR_FAVORITE(0, "点赞或收藏"),
    COMMENT(1, "评论"),
    FOLLOW(2, "关注"),
    CHAT(3, "聊天"),
    TREND(4, "动态");

    private final Integer code;
    private final String type;

    UncheckedMessageEnum(Integer code, String type) {
        this.code = code;
        this.type = type;
    }
}
