package com.zhisu.hotel.user.service;

import com.zhisu.hotel.common.response.PageResponse;
import com.zhisu.hotel.user.dto.ChangePasswordRequest;
import com.zhisu.hotel.user.dto.UpdateProfileRequest;
import com.zhisu.hotel.user.dto.UpdateUserRolesRequest;
import com.zhisu.hotel.user.dto.UpdateUserStatusRequest;
import com.zhisu.hotel.user.vo.UserVO;

public interface UserService {

    UserVO getCurrentUser();

    UserVO updateCurrentUser(UpdateProfileRequest request);

    void changeCurrentUserPassword(ChangePasswordRequest request);

    PageResponse<UserVO> getUserPage(long current, long size, String keyword, String status);

    UserVO getUserById(Long userId);

    void updateUserStatus(Long userId, UpdateUserStatusRequest request);

    void updateUserRoles(Long userId, UpdateUserRolesRequest request);
}
