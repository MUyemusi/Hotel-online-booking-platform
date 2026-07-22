package com.zhisu.hotel.auth.security;

import java.util.List;

public record AuthenticatedUser(Long id, String username, List<String> roles) {
}