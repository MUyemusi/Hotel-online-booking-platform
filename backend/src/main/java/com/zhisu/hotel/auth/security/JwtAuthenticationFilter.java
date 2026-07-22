package com.zhisu.hotel.auth.security;

import com.zhisu.hotel.common.context.UserContext;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final JwtService jwtService;

    public JwtAuthenticationFilter(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String authorization = request.getHeader("Authorization");
        if (authorization != null && authorization.startsWith("Bearer ")) {
            try {
                Claims claims = jwtService.parse(authorization.substring(7));
                Long userId = claims.get("uid", Long.class);
                List<?> rawRoles = claims.get("roles", List.class);
                List<String> roles = rawRoles == null ? List.of() : rawRoles.stream().map(String::valueOf).toList();
                AuthenticatedUser principal = new AuthenticatedUser(userId, claims.getSubject(), roles);
                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                        principal, null, roles.stream().map(role -> new SimpleGrantedAuthority("ROLE_" + role)).toList());
                SecurityContextHolder.getContext().setAuthentication(authentication);
                UserContext.setUserId(userId);
                UserContext.setUserType(roles.isEmpty() ? null : roles.get(0));
            } catch (Exception invalidToken) {
                SecurityContextHolder.clearContext();
                UserContext.clear();
            }
        }
        try {
            chain.doFilter(request, response);
        } finally {
            UserContext.clear();
        }
    }
}