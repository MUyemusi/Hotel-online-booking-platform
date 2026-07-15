package com.zhisu.hotel.booking.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zhisu.hotel.booking.entity.BookingOrder;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface BookingOrderMapper extends BaseMapper<BookingOrder> {
}
