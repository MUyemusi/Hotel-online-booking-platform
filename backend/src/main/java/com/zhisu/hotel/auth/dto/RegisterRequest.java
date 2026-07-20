package com.zhisu.hotel.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RegisterRequest {

    @NotBlank(message = "用户名不能为空")
    @Size(min = 4, max = 50, message = "用户名长度必须为4到50个字符")
    private String username;

    @NotBlank(message = "密码不能为空")
    @Size(min = 6, max = 64, message = "密码长度必须为6到64个字符")
    private String password;

    @Size(max = 50, message = "昵称不能超过50个字符")
    private String nickname;

    @NotBlank(message = "手机号不能为空")
    @Pattern(regexp = "^1\\d{10}$", message = "手机号格式不正确")
    private String phone;

    @Email(message = "邮箱格式不正确")
    @Size(max = 100, message = "邮箱不能超过100个字符")
    private String email;
}