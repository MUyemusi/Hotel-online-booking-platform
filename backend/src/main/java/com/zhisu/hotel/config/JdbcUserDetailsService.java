package com.zhisu.hotel.config;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class JdbcUserDetailsService implements UserDetailsService {

    private final JdbcTemplate jdbcTemplate;

    public JdbcUserDetailsService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        AuthUserRecord user = jdbcTemplate.query(
                "SELECT id, username, password_hash AS password, status FROM sys_user WHERE username = ? LIMIT 1",
                rs -> rs.next() ? new AuthUserRecord(
                        rs.getLong("id"),
                        rs.getString("username"),
                        rs.getString("password"),
                        rs.getString("status")
                ) : null,
                username
        );

        if (user == null) {
            throw new UsernameNotFoundException("User not found");
        }

        List<GrantedAuthority> authorities = jdbcTemplate.query(
                "SELECT r.role_code FROM sys_role r INNER JOIN sys_user_role ur ON ur.role_id = r.id WHERE ur.user_id = ?",
                (rs, rowNum) -> new SimpleGrantedAuthority("ROLE_" + rs.getString("role_code")),
                user.id()
        );

        return User.withUsername(user.username())
                .password(user.password())
                .authorities(authorities)
                .disabled(!"ENABLED".equalsIgnoreCase(user.status()))
                .build();
    }

    private record AuthUserRecord(Long id, String username, String password, String status) {
    }
}