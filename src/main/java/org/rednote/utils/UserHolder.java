package org.rednote.utils;


public class UserHolder {

    private static final ThreadLocal<String> userId = new ThreadLocal<>();

    public static void setUserId(String _userId) {
        userId.set(_userId);
    }

    public static String getUserId() {
        return userId.get();
    }

    public static void removeUserId() {
        userId.remove();
    }
}
