package com.zhisu.hotel.auth.security;

import com.zhisu.hotel.common.context.UserContext;
import com.zhisu.hotel.common.exception.BusinessException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public final class CurrentUserProvider {
    private CurrentUserProvider() {}

    public static AuthenticatedUser require() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof AuthenticatedUser user)) {
            throw new BusinessException("UNAUTHORIZED", "请先登录");
        }
        return user;
    }

    public static Long requireUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof AuthenticatedUser user) {
            return user.id();
        }
        Long userId = UserContext.getUserId();
        if (userId == null) {
            throw new BusinessException("UNAUTHORIZED", "请先登录");
        }
        return userId;
    }
}
