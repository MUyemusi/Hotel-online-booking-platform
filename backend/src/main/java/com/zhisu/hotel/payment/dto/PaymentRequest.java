package com.zhisu.hotel.payment.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentRequest {

    @NotNull(message = "订单ID不能为空")
    private Long orderId;

    @NotBlank(message = "支付方式不能为空")
    @Pattern(regexp = "WECHAT|ALIPAY|BANK_CARD|MOCK", message = "不支持的支付方式")
    private String paymentMethod;

    @NotBlank(message = "支付幂等键不能为空")
    private String idempotencyKey;
}
