package com.zhisu.hotel.common.context;

/**
 * 用户上下文工具类
 * 待成员1认证模块完成后实现
 */
public class UserContext {

    private static final ThreadLocal<Long> CURRENT_USER_ID = new ThreadLocal<>();
    private static final ThreadLocal<String> CURRENT_USER_TYPE = new ThreadLocal<>();

    /**
     * 设置当前用户ID
     * 待成员1认证模块完成后，从JWT或SecurityContext中获取
     */
    public static void setUserId(Long userId) {
        CURRENT_USER_ID.set(userId);
    }

    /**
     * 获取当前用户ID
     * @return 当前用户ID，若未认证则返回null
     */
    public static Long getUserId() {
        return CURRENT_USER_ID.get();
    }

    /**
     * 设置当前用户类型
     */
    public static void setUserType(String userType) {
        CURRENT_USER_TYPE.set(userType);
    }

    /**
     * 获取当前用户类型
     */
    public static String getUserType() {
        return CURRENT_USER_TYPE.get();
    }

    /**
     * 清除上下文
     */
    public static void clear() {
        CURRENT_USER_ID.remove();
        CURRENT_USER_TYPE.remove();
    }
}
