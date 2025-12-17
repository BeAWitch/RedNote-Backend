package org.rednote.enums;

import lombok.Getter;

@Getter
public enum UncheckedMessageEnum {

    LIKE_OR_FAVORITE_COUNT(0, "likeOrFavorite"),
    COMMENT_COUNT(1, "comment"),
    FOLLOW_COUNT(2, "follow"),
    MESSAGE_COUNT(3, "message");

    private final Integer code;
    private final String type;

    UncheckedMessageEnum(Integer code, String type) {
        this.code = code;
        this.type = type;
    }
}
