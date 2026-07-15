package com.zhisu.hotel.booking.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zhisu.hotel.booking.entity.BookingOrder;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.time.LocalDateTime;

@Mapper
public interface BookingOrderMapper extends BaseMapper<BookingOrder> {

    @Select("SELECT * FROM booking_order WHERE id = #{orderId} FOR UPDATE")
    BookingOrder selectByIdForUpdate(@Param("orderId") Long orderId);

    @Update("UPDATE booking_order " +
            "SET order_status = 'CHECKED_IN', updated_at = #{updatedAt} " +
            "WHERE id = #{orderId} " +
            "  AND order_status = 'PENDING_CHECK_IN' " +
            "  AND payment_status = 'PAID'")
    int markCheckedInIfPending(@Param("orderId") Long orderId,
                               @Param("updatedAt") LocalDateTime updatedAt);
}
