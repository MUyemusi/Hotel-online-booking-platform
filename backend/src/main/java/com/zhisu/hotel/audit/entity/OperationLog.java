package com.zhisu.hotel.audit.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("operation_log")
public class OperationLog {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long operatorId;
    private String module;
    private String action;
    private String requestMethod;
    private String requestUri;
    private String result;
    private String ipAddress;
    private String detail;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
