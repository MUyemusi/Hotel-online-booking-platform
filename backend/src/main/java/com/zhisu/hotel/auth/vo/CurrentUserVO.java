package com.zhisu.hotel.auth.vo;

import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
public class CurrentUserVO {
    private Long id;
    private String username;
    private String nickname;
    private String phone;
    private String email;
    private List<String> roles;
}