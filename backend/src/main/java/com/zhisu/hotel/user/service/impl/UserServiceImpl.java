package com.zhisu.hotel.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhisu.hotel.common.context.UserContext;
import com.zhisu.hotel.common.exception.BusinessException;
import com.zhisu.hotel.common.exception.ErrorCode;
import com.zhisu.hotel.common.response.PageResponse;
import com.zhisu.hotel.user.dto.ChangePasswordRequest;
import com.zhisu.hotel.user.dto.UpdateProfileRequest;
import com.zhisu.hotel.user.dto.UpdateUserRolesRequest;
import com.zhisu.hotel.user.dto.UpdateUserStatusRequest;
import com.zhisu.hotel.user.entity.SysRole;
import com.zhisu.hotel.user.entity.SysUser;
import com.zhisu.hotel.user.entity.SysUserRole;
import com.zhisu.hotel.user.mapper.SysRoleMapper;
import com.zhisu.hotel.user.mapper.SysUserMapper;
import com.zhisu.hotel.user.mapper.SysUserRoleMapper;
import com.zhisu.hotel.user.service.UserService;
import com.zhisu.hotel.user.vo.UserVO;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

@Service
public class UserServiceImpl implements UserService {

    private static final Set<String> ALLOWED_STATUSES =
            Set.of("ENABLED", "DISABLED");

    private final SysUserMapper sysUserMapper;
    private final SysRoleMapper sysRoleMapper;
    private final SysUserRoleMapper sysUserRoleMapper;
    private final PasswordEncoder passwordEncoder;

    public UserServiceImpl(
            SysUserMapper sysUserMapper,
            SysRoleMapper sysRoleMapper,
            SysUserRoleMapper sysUserRoleMapper,
            PasswordEncoder passwordEncoder) {
        this.sysUserMapper = sysUserMapper;
        this.sysRoleMapper = sysRoleMapper;
        this.sysUserRoleMapper = sysUserRoleMapper;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public UserVO getCurrentUser() {
        return getUserById(requireCurrentUserId());
    }

    @Override
    @Transactional
    public UserVO updateCurrentUser(UpdateProfileRequest request) {
        Long userId = requireCurrentUserId();
        SysUser user = requireUser(userId);

        String nickname = request.getNickname().trim();
        String phone = request.getPhone().trim();
        String email = normalizeNullable(request.getEmail());
        String avatarUrl = normalizeNullable(request.getAvatarUrl());

        ensurePhoneAvailable(phone, userId);
        ensureEmailAvailable(email, userId);

        user.setNickname(nickname);
        user.setPhone(phone);
        user.setEmail(email);
        user.setAvatarUrl(avatarUrl);

        if (sysUserMapper.updateById(user) != 1) {
            throw new BusinessException(ErrorCode.INTERNAL_ERROR, "个人资料更新失败");
        }

        return getUserById(userId);
    }

    @Override
    @Transactional
    public void changeCurrentUserPassword(ChangePasswordRequest request) {
        SysUser user = requireUser(requireCurrentUserId());

        if (!passwordEncoder.matches(request.getOldPassword(), user.getPasswordHash())) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "原密码错误");
        }

        if (passwordEncoder.matches(request.getNewPassword(), user.getPasswordHash())) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "新密码不能与原密码相同");
        }

        SysUser update = new SysUser();
        update.setId(user.getId());
        update.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));

        if (sysUserMapper.updateById(update) != 1) {
            throw new BusinessException(ErrorCode.INTERNAL_ERROR, "密码修改失败");
        }
    }

    @Override
    public PageResponse<UserVO> getUserPage(
            long current,
            long size,
            String keyword,
            String status) {
        long safeCurrent = Math.max(current, 1);
        long safeSize = Math.min(Math.max(size, 1), 100);

        LambdaQueryWrapper<SysUser> query = new LambdaQueryWrapper<>();

        if (StringUtils.hasText(keyword)) {
            String value = keyword.trim();
            query.and(wrapper -> wrapper
                    .like(SysUser::getUsername, value)
                    .or()
                    .like(SysUser::getNickname, value)
                    .or()
                    .like(SysUser::getPhone, value));
        }

        if (StringUtils.hasText(status)) {
            String normalizedStatus = normalizeStatus(status);
            query.eq(SysUser::getStatus, normalizedStatus);
        }

        query.orderByDesc(SysUser::getId);

        Page<SysUser> page = sysUserMapper.selectPage(
                new Page<>(safeCurrent, safeSize),
                query
        );

        List<UserVO> records = page.getRecords()
                .stream()
                .map(this::toUserVO)
                .toList();

        return PageResponse.<UserVO>builder()
                .records(records)
                .total(page.getTotal())
                .current(page.getCurrent())
                .size(page.getSize())
                .pages(page.getPages())
                .build();
    }

    @Override
    public UserVO getUserById(Long userId) {
        return toUserVO(requireUser(userId));
    }

    @Override
    @Transactional
    public void updateUserStatus(Long userId, UpdateUserStatusRequest request) {
        Long operatorId = requireCurrentUserId();
        if (operatorId.equals(userId)) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "不能修改自己的账号状态");
        }

        requireUser(userId);
        String status = normalizeStatus(request.getStatus());

        SysUser update = new SysUser();
        update.setId(userId);
        update.setStatus(status);

        if (sysUserMapper.updateById(update) != 1) {
            throw new BusinessException(ErrorCode.INTERNAL_ERROR, "用户状态更新失败");
        }
    }

    @Override
    @Transactional
    public void updateUserRoles(Long userId, UpdateUserRolesRequest request) {
        Long operatorId = requireCurrentUserId();
        if (operatorId.equals(userId)) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "不能修改自己的角色");
        }

        requireUser(userId);

        Set<String> requestedCodes = request.getRoleCodes()
                .stream()
                .filter(StringUtils::hasText)
                .map(String::trim)
                .map(value -> value.toUpperCase(Locale.ROOT))
                .collect(
                        LinkedHashSet::new,
                        Set::add,
                        Set::addAll
                );

        if (requestedCodes.isEmpty()) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "用户角色不能为空");
        }

        List<SysRole> roles = sysRoleMapper.selectList(
                new LambdaQueryWrapper<SysRole>()
                        .in(SysRole::getRoleCode, requestedCodes)
        );

        if (roles.size() != requestedCodes.size()) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "包含不存在的角色");
        }

        sysUserRoleMapper.delete(
                new LambdaQueryWrapper<SysUserRole>()
                        .eq(SysUserRole::getUserId, userId)
        );

        for (SysRole role : roles) {
            SysUserRole relation = SysUserRole.builder()
                    .userId(userId)
                    .roleId(role.getId())
                    .build();
            if (sysUserRoleMapper.insert(relation) != 1) {
                throw new BusinessException(ErrorCode.INTERNAL_ERROR, "用户角色更新失败");
            }
        }
    }

    private SysUser requireUser(Long userId) {
        if (userId == null) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "用户编号不能为空");
        }

        SysUser user = sysUserMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "用户不存在");
        }
        return user;
    }

    private Long requireCurrentUserId() {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "未登录或登录已失效");
        }
        return userId;
    }

    private UserVO toUserVO(SysUser user) {
        return UserVO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .nickname(user.getNickname())
                .phone(user.getPhone())
                .email(user.getEmail())
                .avatarUrl(user.getAvatarUrl())
                .status(user.getStatus())
                .consecutiveCancelCount(user.getConsecutiveCancelCount())
                .banUntil(user.getBanUntil())
                .lastLoginAt(user.getLastLoginAt())
                .roles(getRoleCodes(user.getId()))
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }

    private List<String> getRoleCodes(Long userId) {
        List<Long> roleIds = sysUserRoleMapper.selectList(
                        new LambdaQueryWrapper<SysUserRole>()
                                .eq(SysUserRole::getUserId, userId)
                )
                .stream()
                .map(SysUserRole::getRoleId)
                .toList();

        if (roleIds.isEmpty()) {
            return List.of();
        }

        return sysRoleMapper.selectBatchIds(roleIds)
                .stream()
                .map(SysRole::getRoleCode)
                .sorted()
                .toList();
    }

    private void ensurePhoneAvailable(String phone, Long excludedUserId) {
        long count = sysUserMapper.selectCount(
                new LambdaQueryWrapper<SysUser>()
                        .eq(SysUser::getPhone, phone)
                        .ne(SysUser::getId, excludedUserId)
        );
        if (count > 0) {
            throw new BusinessException(ErrorCode.CONFLICT, "手机号已被使用");
        }
    }

    private void ensureEmailAvailable(String email, Long excludedUserId) {
        if (email == null) {
            return;
        }

        long count = sysUserMapper.selectCount(
                new LambdaQueryWrapper<SysUser>()
                        .eq(SysUser::getEmail, email)
                        .ne(SysUser::getId, excludedUserId)
        );
        if (count > 0) {
            throw new BusinessException(ErrorCode.CONFLICT, "邮箱已被使用");
        }
    }

    private String normalizeStatus(String status) {
        String value = status.trim().toUpperCase(Locale.ROOT);
        if (!ALLOWED_STATUSES.contains(value)) {
            throw new BusinessException(
                    ErrorCode.INVALID_REQUEST,
                    "用户状态只能是 ENABLED 或 DISABLED"
            );
        }
        return value;
    }

    private String normalizeNullable(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }
}
