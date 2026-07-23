package com.zhisu.hotel.room.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhisu.hotel.common.enums.RoomStatus;
import com.zhisu.hotel.room.entity.Room;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.time.LocalDateTime;
import java.util.List;

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

    @Update("UPDATE room " +
            "SET room_type_id = #{roomTypeId}, " +
            "    near_elevator = #{nearElevator}, " +
            "    head_room = #{headRoom}, " +
            "    tail_room = #{tailRoom}, " +
            "    cad_image_url = #{cadImageUrl}, " +
            "    remark = #{remark}, " +
            "    updated_at = #{updatedAt} " +
            "WHERE id = #{roomId} " +
            "  AND is_deleted = 0")
    int updateRoomProfile(@Param("roomId") Long roomId,
                          @Param("roomTypeId") Long roomTypeId,
                          @Param("nearElevator") Boolean nearElevator,
                          @Param("headRoom") Boolean headRoom,
                          @Param("tailRoom") Boolean tailRoom,
                          @Param("cadImageUrl") String cadImageUrl,
                          @Param("remark") String remark,
                          @Param("updatedAt") LocalDateTime updatedAt);

    @Select({
            "<script>",
            "SELECT * FROM room",
            "WHERE id IN",
            "<foreach collection='roomIds' item='roomId' open='(' separator=',' close=')'>",
            "#{roomId}",
            "</foreach>",
            "AND is_deleted = 0",
            "ORDER BY id",
            "FOR UPDATE",
            "</script>"
    })
    List<Room> selectByIdsForUpdate(@Param("roomIds") List<Long> roomIds);

    @Select({
            "<script>",
            "SELECT * FROM room",
            "WHERE room_status = 'DIRTY'",
            "AND is_deleted = 0",
            "<if test='floor != null'>AND floor = #{floor}</if>",
            "<if test='roomTypeId != null'>AND room_type_id = #{roomTypeId}</if>",
            "ORDER BY floor, room_no",
            "</script>"
    })
    Page<Room> pageDirtyRooms(Page<Room> page,
                              @Param("floor") Integer floor,
                              @Param("roomTypeId") Long roomTypeId);

    @Select("SELECT * FROM room WHERE id = #{roomId} AND is_deleted = 0 FOR UPDATE")
    Room selectByIdForUpdate(@Param("roomId") Long roomId);

    @Update("UPDATE room " +
            "SET room_status = 'OCCUPIED', version = version + 1, updated_at = #{updatedAt} " +
            "WHERE id = #{roomId} " +
            "  AND room_status = 'AVAILABLE' " +
            "  AND version = #{version} " +
            "  AND is_deleted = 0")
    int markOccupiedForCheckin(@Param("roomId") Long roomId,
                               @Param("version") Integer version,
                               @Param("updatedAt") LocalDateTime updatedAt);

    @Update("UPDATE room " +
            "SET room_status = 'DIRTY', version = version + 1, updated_at = #{updatedAt} " +
            "WHERE id = #{roomId} " +
            "  AND room_status = 'OCCUPIED' " +
            "  AND version = #{version} " +
            "  AND is_deleted = 0")
    int markDirtyForCheckout(@Param("roomId") Long roomId,
                             @Param("version") Integer version,
                             @Param("updatedAt") LocalDateTime updatedAt);

    @Update("UPDATE room " +
            "SET room_status = 'AVAILABLE', maintenance_reason = NULL, version = version + 1, updated_at = #{updatedAt} " +
            "WHERE id = #{roomId} " +
            "  AND room_status = 'DIRTY' " +
            "  AND version = #{version} " +
            "  AND is_deleted = 0")
    int markAvailableAfterCleaning(@Param("roomId") Long roomId,
                                   @Param("version") Integer version,
                                   @Param("updatedAt") LocalDateTime updatedAt);

    @Update("UPDATE room " +
            "SET room_status = 'MAINTENANCE', maintenance_reason = #{reason}, version = version + 1, updated_at = #{updatedAt} " +
            "WHERE id = #{roomId} " +
            "  AND room_status = 'DIRTY' " +
            "  AND version = #{version} " +
            "  AND is_deleted = 0")
    int markMaintenanceFromDirty(@Param("roomId") Long roomId,
                                 @Param("version") Integer version,
                                 @Param("reason") String reason,
                                 @Param("updatedAt") LocalDateTime updatedAt);
}
