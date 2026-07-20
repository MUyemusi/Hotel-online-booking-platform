package com.zhisu.hotel.user.service.impl;

import com.zhisu.hotel.common.context.UserContext;
import com.zhisu.hotel.common.exception.BusinessException;
import com.zhisu.hotel.common.exception.ErrorCode;
import com.zhisu.hotel.user.dto.ChangePasswordRequest;
import com.zhisu.hotel.user.dto.UpdateProfileRequest;
import com.zhisu.hotel.user.dto.UpdateUserStatusRequest;
import com.zhisu.hotel.user.entity.SysUser;
import com.zhisu.hotel.user.mapper.SysRoleMapper;
import com.zhisu.hotel.user.mapper.SysUserMapper;
import com.zhisu.hotel.user.mapper.SysUserRoleMapper;
import com.zhisu.hotel.user.vo.UserVO;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private SysUserMapper sysUserMapper;

    @Mock
    private SysRoleMapper sysRoleMapper;

    @Mock
    private SysUserRoleMapper sysUserRoleMapper;

    @Mock
    private PasswordEncoder passwordEncoder;

    private UserServiceImpl userService;

    @BeforeEach
    void setUp() {
        userService = new UserServiceImpl(
                sysUserMapper,
                sysRoleMapper,
                sysUserRoleMapper,
                passwordEncoder
        );
        UserContext.setUserId(4L);
        UserContext.setUserType("USER");
    }

    @AfterEach
    void tearDown() {
        UserContext.clear();
    }

    @Test
    void shouldReturnCurrentUserProfile() {
        SysUser user = createUser(4L);
        when(sysUserMapper.selectById(4L)).thenReturn(user);
        when(sysUserRoleMapper.selectList(any())).thenReturn(List.of());

        UserVO result = userService.getCurrentUser();

        assertEquals(4L, result.getId());
        assertEquals("student", result.getUsername());
        assertEquals("测试用户", result.getNickname());
        assertEquals(List.of(), result.getRoles());
    }

    @Test
    void shouldUpdateCurrentUserProfile() {
        SysUser user = createUser(4L);
        UpdateProfileRequest request = new UpdateProfileRequest();
        request.setNickname("新昵称");
        request.setPhone("13900000001");
        request.setEmail(null);
        request.setAvatarUrl("https://example.com/avatar.png");

        when(sysUserMapper.selectById(4L)).thenReturn(user);
        when(sysUserMapper.selectCount(any())).thenReturn(0L);
        when(sysUserMapper.updateById(any(SysUser.class))).thenReturn(1);
        when(sysUserRoleMapper.selectList(any())).thenReturn(List.of());

        UserVO result = userService.updateCurrentUser(request);

        assertEquals("新昵称", result.getNickname());
        assertEquals("13900000001", result.getPhone());
        verify(sysUserMapper).updateById(user);
    }

    @Test
    void shouldRejectDuplicatePhone() {
        SysUser user = createUser(4L);
        UpdateProfileRequest request = new UpdateProfileRequest();
        request.setNickname("测试用户");
        request.setPhone("13900000002");

        when(sysUserMapper.selectById(4L)).thenReturn(user);
        when(sysUserMapper.selectCount(any())).thenReturn(1L);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> userService.updateCurrentUser(request)
        );

        assertEquals(ErrorCode.CONFLICT, exception.getCode());
        assertEquals("手机号已被使用", exception.getMessage());
    }

    @Test
    void shouldRejectIncorrectOldPassword() {
        SysUser user = createUser(4L);
        ChangePasswordRequest request = new ChangePasswordRequest();
        request.setOldPassword("wrong-password");
        request.setNewPassword("NewPassword123");

        when(sysUserMapper.selectById(4L)).thenReturn(user);
        when(passwordEncoder.matches("wrong-password", user.getPasswordHash()))
                .thenReturn(false);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> userService.changeCurrentUserPassword(request)
        );

        assertEquals(ErrorCode.INVALID_REQUEST, exception.getCode());
        assertEquals("原密码错误", exception.getMessage());
        verify(sysUserMapper, never()).updateById(any(SysUser.class));
    }

    @Test
    void shouldPreventAdministratorFromDisablingSelf() {
        UserContext.setUserId(1L);
        UserContext.setUserType("ADMIN");
        UpdateUserStatusRequest request = new UpdateUserStatusRequest();
        request.setStatus("DISABLED");

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> userService.updateUserStatus(1L, request)
        );

        assertEquals(ErrorCode.INVALID_REQUEST, exception.getCode());
        assertEquals("不能修改自己的账号状态", exception.getMessage());
        verify(sysUserMapper, never()).updateById(any(SysUser.class));
    }

    @Test
    void shouldAllowAdministratorToDisableAnotherUser() {
        UserContext.setUserId(1L);
        UserContext.setUserType("ADMIN");
        UpdateUserStatusRequest request = new UpdateUserStatusRequest();
        request.setStatus("disabled");

        when(sysUserMapper.selectById(4L)).thenReturn(createUser(4L));
        when(sysUserMapper.updateById(any(SysUser.class))).thenReturn(1);

        userService.updateUserStatus(4L, request);

        verify(sysUserMapper).updateById(any(SysUser.class));
    }

    private SysUser createUser(Long id) {
        return SysUser.builder()
                .id(id)
                .username("student")
                .passwordHash("encoded-password")
                .nickname("测试用户")
                .phone("13900000000")
                .email("student@example.com")
                .status("ENABLED")
                .consecutiveCancelCount(0)
                .build();
    }
}
