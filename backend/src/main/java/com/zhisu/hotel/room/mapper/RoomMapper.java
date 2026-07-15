package com.zhisu.hotel.room.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zhisu.hotel.common.enums.RoomStatus;
import com.zhisu.hotel.room.entity.Room;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface RoomMapper extends BaseMapper<Room> {

    /**
     * 带条件的房间状态更新
     * 确保并发安全，只有当当前状态匹配时才更新
     */
    @Update("UPDATE room " +
            "SET room_status = #{newStatus}, " +
            "    maintenance_reason = #{maintenanceReason}, " +
            "    version = version + 1, " +
            "    updated_at = NOW() " +
            "WHERE id = #{roomId} " +
            "  AND room_status = #{oldStatus} " +
            "  AND version = #{version} " +
            "  AND is_deleted = 0")
    int updateStatusWithCondition(
            @Param("roomId") Long roomId,
            @Param("oldStatus") RoomStatus oldStatus,
            @Param("newStatus") RoomStatus newStatus,
            @Param("maintenanceReason") String maintenanceReason,
            @Param("version") Integer version
    );
}
