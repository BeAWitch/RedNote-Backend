package org.rednote.common.utils;


public class UserHolder {

    private static final ThreadLocal<Long> userId = new ThreadLocal<>();

    public static void setUserId(Long _userId) {
        userId.set(_userId);
    }

    public static Long getUserId() {
        return userId.get();
    }

    public static void removeUserId() {
        userId.remove();
    }
}
