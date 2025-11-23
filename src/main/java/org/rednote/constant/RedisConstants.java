package org.rednote.constant;

import java.time.Duration;

public class RedisConstants {
    public static final String LOGIN_CODE_KEY = "login:code:";
    public static final Long LOGIN_CODE_TTL = 2L;
    public static final String LOGIN_USER_KEY = "login:token:";
    public static final Long LOGIN_USER_TTL = 30L;
    public static final String SEARCH_RECORD_KEY = "search:records";
    public static final Duration SEARCH_RECORD_TTL = Duration.ofDays(7);
}
