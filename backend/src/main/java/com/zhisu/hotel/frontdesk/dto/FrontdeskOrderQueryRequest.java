package com.zhisu.hotel.frontdesk.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class FrontdeskOrderQueryRequest {

    private String orderNo;
    private String phone;
    private String contactName;
    private LocalDate checkInDate;
    private String orderStatus;
    private Integer page = 1;
    private Integer size = 10;
}
