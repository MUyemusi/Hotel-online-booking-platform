package com.zhisu.hotel.auth.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zhisu.hotel.auth.dto.LoginRequest;
import com.zhisu.hotel.auth.dto.LoginResponse;
import com.zhisu.hotel.auth.dto.RegisterRequest;
import com.zhisu.hotel.auth.service.AuthService;
import com.zhisu.hotel.auth.service.JwtTokenService;
import com.zhisu.hotel.common.exception.BusinessException;
import com.zhisu.hotel.common.exception.ErrorCode;
import com.zhisu.hotel.user.entity.SysRole;
import com.zhisu.hotel.user.entity.SysUser;
import com.zhisu.hotel.user.entity.SysUserRole;
import com.zhisu.hotel.user.mapper.SysRoleMapper;
import com.zhisu.hotel.user.mapper.SysUserMapper;
import com.zhisu.hotel.user.mapper.SysUserRoleMapper;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class AuthServiceImpl implements AuthService {

    private final SysUserMapper sysUserMapper;
    private final SysRoleMapper sysRoleMapper;
    private final SysUserRoleMapper sysUserRoleMapper;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenService jwtTokenService;
    private final Clock clock;

    public AuthServiceImpl(
            SysUserMapper sysUserMapper,
            SysRoleMapper sysRoleMapper,
            SysUserRoleMapper sysUserRoleMapper,
            PasswordEncoder passwordEncoder,
            AuthenticationManager authenticationManager,
            JwtTokenService jwtTokenService,
            Clock clock) {
        this.sysUserMapper = sysUserMapper;
        this.sysRoleMapper = sysRoleMapper;
        this.sysUserRoleMapper = sysUserRoleMapper;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtTokenService = jwtTokenService;
        this.clock = clock;
    }

    @Override
    @Transactional
    public Long register(RegisterRequest request) {
        String username = request.getUsername().trim();
        String phone = request.getPhone().trim();
        String email = normalizeNullable(request.getEmail());

        if (existsByUsername(username)) {
            throw new BusinessException(ErrorCode.CONFLICT, "用户名已存在");
        }

        if (existsByPhone(phone)) {
            throw new BusinessException(ErrorCode.CONFLICT, "手机号已被注册");
        }

        if (email != null && existsByEmail(email)) {
            throw new BusinessException(ErrorCode.CONFLICT, "邮箱已被注册");
        }

        SysUser user = SysUser.builder()
                .username(username)
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .nickname(StringUtils.hasText(request.getNickname())
                        ? request.getNickname().trim()
                        : username)
                .phone(phone)
                .email(email)
                .status("ENABLED")
                .consecutiveCancelCount(0)
                .build();

        if (sysUserMapper.insert(user) != 1 || user.getId() == null) {
            throw new BusinessException(
                    ErrorCode.INTERNAL_ERROR,
                    "用户注册失败"
            );
        }

        SysRole userRole = sysRoleMapper.selectOne(
                new LambdaQueryWrapper<SysRole>()
                        .eq(SysRole::getRoleCode, "USER")
                        .last("LIMIT 1")
        );

        if (userRole == null) {
            throw new BusinessException(
                    ErrorCode.INTERNAL_ERROR,
                    "系统缺少普通用户角色"
            );
        }

        SysUserRole relation = SysUserRole.builder()
                .userId(user.getId())
                .roleId(userRole.getId())
                .build();

        sysUserRoleMapper.insert(relation);
        return user.getId();
    }

    @Override
    public LoginResponse login(LoginRequest request) {
        Authentication authentication;

        try {
            authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getUsername().trim(),
                            request.getPassword()
                    )
            );
        } catch (DisabledException exception) {
            throw new BusinessException(
                    ErrorCode.FORBIDDEN,
                    "账号已被禁用"
            );
        } catch (AuthenticationException exception) {
            throw new BusinessException(
                    ErrorCode.UNAUTHORIZED,
                    "用户名或密码错误"
            );
        }

        SysUser user = sysUserMapper.selectOne(
                new LambdaQueryWrapper<SysUser>()
                        .eq(SysUser::getUsername, request.getUsername().trim())
                        .last("LIMIT 1")
        );

        if (user == null) {
            throw new BusinessException(
                    ErrorCode.UNAUTHORIZED,
                    "用户名或密码错误"
            );
        }

        List<String> roles = authentication.getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority)
                .map(role -> role.startsWith("ROLE_")
                        ? role.substring(5)
                        : role)
                .toList();

        String token = jwtTokenService.generateToken(
                user.getId(),
                user.getUsername(),
                roles
        );

        SysUser updateUser = new SysUser();
        updateUser.setId(user.getId());
        updateUser.setLastLoginAt(LocalDateTime.now(clock));
        sysUserMapper.updateById(updateUser);

        return LoginResponse.builder()
                .token(token)
                .tokenType("Bearer")
                .expiresIn(jwtTokenService.getExpirationSeconds())
                .userId(user.getId())
                .username(user.getUsername())
                .nickname(user.getNickname())
                .roles(roles)
                .build();
    }

    private boolean existsByUsername(String username) {
        return sysUserMapper.selectCount(
                new LambdaQueryWrapper<SysUser>()
                        .eq(SysUser::getUsername, username)
        ) > 0;
    }

    private boolean existsByPhone(String phone) {
        return sysUserMapper.selectCount(
                new LambdaQueryWrapper<SysUser>()
                        .eq(SysUser::getPhone, phone)
        ) > 0;
    }

    private boolean existsByEmail(String email) {
        return sysUserMapper.selectCount(
                new LambdaQueryWrapper<SysUser>()
                        .eq(SysUser::getEmail, email)
        ) > 0;
    }

    private String normalizeNullable(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }
}