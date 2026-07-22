package com.zhisu.hotel.auth.filter;

import com.zhisu.hotel.auth.service.JwtTokenService;
import com.zhisu.hotel.common.context.UserContext;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtTokenService jwtTokenService;
    private final UserDetailsService userDetailsService;

    public JwtAuthenticationFilter(
            JwtTokenService jwtTokenService,
            UserDetailsService userDetailsService) {
        this.jwtTokenService = jwtTokenService;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain)
            throws ServletException, IOException {

        try {
            String token = resolveToken(request);

            if (token != null
                    && SecurityContextHolder.getContext().getAuthentication() == null
                    && jwtTokenService.isTokenValid(token)) {

                authenticateRequest(request, token);
            }

            filterChain.doFilter(request, response);
        } catch (JwtException
                 | IllegalArgumentException
                 | UsernameNotFoundException exception) {
            SecurityContextHolder.clearContext();
            filterChain.doFilter(request, response);
        } finally {
            UserContext.clear();
        }
    }

    private void authenticateRequest(
            HttpServletRequest request,
            String token) {

        String username = jwtTokenService.getUsername(token);
        Long userId = jwtTokenService.getUserId(token);
        UserDetails userDetails =
                userDetailsService.loadUserByUsername(username);

        if (!userDetails.isEnabled()) {
            throw new UsernameNotFoundException("用户已被禁用");
        }

        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null,
                        userDetails.getAuthorities()
                );

        authentication.setDetails(
                new WebAuthenticationDetailsSource()
                        .buildDetails(request)
        );

        SecurityContextHolder.getContext()
                .setAuthentication(authentication);

        UserContext.setUserId(userId);

        userDetails.getAuthorities()
                .stream()
                .findFirst()
                .ifPresent(authority ->
                        UserContext.setUserType(
                                authority.getAuthority()
                                        .replaceFirst("^ROLE_", "")
                        )
                );
    }

    private String resolveToken(HttpServletRequest request) {
        String authorization =
                request.getHeader(HttpHeaders.AUTHORIZATION);

        if (authorization == null
                || !authorization.startsWith(BEARER_PREFIX)) {
            return null;
        }

        return authorization
                .substring(BEARER_PREFIX.length())
                .trim();
    }
}