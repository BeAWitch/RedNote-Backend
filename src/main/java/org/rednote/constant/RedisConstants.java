package org.rednote.constant;

import java.time.Duration;

public class RedisConstants {
    // 登录
    public static final String LOGIN_CODE_KEY = "login:code:";
    public static final Long LOGIN_CODE_TTL = 2L;
    public static final String LOGIN_USER_KEY = "login:token:";
    public static final Long LOGIN_USER_TTL = 30L;

    // 搜索
    public static final String SEARCH_RECORD_KEY = "search:records";
    public static final Duration SEARCH_RECORD_TTL = Duration.ofDays(7);

    // 点赞与收藏
    public static final String NOTE_LIKE_KEY = "note:like:";
    public static final String NOTE_FAVORITE_KEY = "note:favorite:";
    public static final String COMMENT_LIKE_KEY = "comment:like:";
    public static final String ALBUM_FAVORITE_KEY = "album:favorite:";
}
