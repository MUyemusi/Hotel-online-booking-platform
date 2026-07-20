package com.zhisu.hotel.user.controller;

import com.zhisu.hotel.common.response.ApiResponse;
import com.zhisu.hotel.user.dto.ChangePasswordRequest;
import com.zhisu.hotel.user.dto.UpdateProfileRequest;
import com.zhisu.hotel.user.service.UserService;
import com.zhisu.hotel.user.vo.UserVO;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users/me")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public ApiResponse<UserVO> getCurrentUser() {
        return ApiResponse.success(userService.getCurrentUser());
    }

    @PutMapping
    public ApiResponse<UserVO> updateCurrentUser(
            @Valid @RequestBody UpdateProfileRequest request) {
        return ApiResponse.success(userService.updateCurrentUser(request));
    }

    @PutMapping("/password")
    public ApiResponse<Void> changePassword(
            @Valid @RequestBody ChangePasswordRequest request) {
        userService.changeCurrentUserPassword(request);
        return ApiResponse.success();
    }
}
