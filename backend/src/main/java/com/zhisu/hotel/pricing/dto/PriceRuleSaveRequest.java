package com.zhisu.hotel.pricing.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class PriceRuleSaveRequest {
    @NotBlank(message = "规则名称不能为空")
    @Size(max = 100, message = "规则名称不能超过100个字符")
    private String ruleName;
    @NotNull(message = "开始日期不能为空")
    private LocalDate startDate;
    @NotNull(message = "结束日期不能为空")
    private LocalDate endDate;
    private Long roomTypeId;
    @NotNull(message = "价格系数不能为空")
    @DecimalMin(value = "0.01", message = "价格系数必须大于0")
    @Digits(integer = 3, fraction = 2, message = "价格系数最多保留2位小数")
    private BigDecimal coefficient;
    @NotNull(message = "优先级不能为空")
    @Min(value = 0, message = "优先级不能小于0")
    @Max(value = 9999, message = "优先级不能超过9999")
    private Integer priority;
    @NotBlank(message = "状态不能为空")
    private String status;
    @Size(max = 255, message = "备注不能超过255个字符")
    private String remark;
}
