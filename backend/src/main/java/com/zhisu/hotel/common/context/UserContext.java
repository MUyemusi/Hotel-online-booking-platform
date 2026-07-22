package com.zhisu.hotel.common.context;

/**
 * Stores the authenticated user for the lifetime of the current HTTP request.
 * JwtAuthenticationFilter populates and clears this context.
 */
public final class UserContext {

    private static final ThreadLocal<Long> CURRENT_USER_ID = new ThreadLocal<>();
    private static final ThreadLocal<String> CURRENT_USER_TYPE = new ThreadLocal<>();

    private UserContext() {
    }

    public static void setUserId(Long userId) {
        CURRENT_USER_ID.set(userId);
    }

    public static Long getUserId() {
        return CURRENT_USER_ID.get();
    }

    public static void setUserType(String userType) {
        CURRENT_USER_TYPE.set(userType);
    }

    public static String getUserType() {
        return CURRENT_USER_TYPE.get();
    }

    public static void clear() {
        CURRENT_USER_ID.remove();
        CURRENT_USER_TYPE.remove();
    }
}