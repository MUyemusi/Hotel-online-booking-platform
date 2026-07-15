package com.zhisu.hotel.booking.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zhisu.hotel.booking.entity.BookingOrder;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface BookingOrderMapper extends BaseMapper<BookingOrder> {

    @Select("SELECT * FROM booking_order WHERE id = #{orderId} FOR UPDATE")
    BookingOrder selectByIdForUpdate(@Param("orderId") Long orderId);
}
