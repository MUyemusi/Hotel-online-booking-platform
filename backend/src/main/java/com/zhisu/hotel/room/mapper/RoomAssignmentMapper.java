package com.zhisu.hotel.room.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zhisu.hotel.common.enums.AssignmentStatus;
import com.zhisu.hotel.room.entity.RoomAssignment;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Mapper
public interface RoomAssignmentMapper extends BaseMapper<RoomAssignment> {

    @Select({
            "<script>",
            "SELECT * FROM room_assignment",
            "WHERE order_id = #{orderId}",
            "AND assignment_status IN",
            "<foreach collection='statuses' item='status' open='(' separator=',' close=')'>",
            "#{status}",
            "</foreach>",
            "AND is_deleted = 0",
            "ORDER BY id",
            "</script>"
    })
    List<RoomAssignment> selectActiveByOrderId(@Param("orderId") Long orderId,
                                               @Param("statuses") Set<AssignmentStatus> statuses);

    @Select({
            "<script>",
            "SELECT * FROM room_assignment",
            "WHERE room_id IN",
            "<foreach collection='roomIds' item='roomId' open='(' separator=',' close=')'>",
            "#{roomId}",
            "</foreach>",
            "AND assignment_status IN",
            "<foreach collection='statuses' item='status' open='(' separator=',' close=')'>",
            "#{status}",
            "</foreach>",
            "AND is_deleted = 0",
            "ORDER BY room_id, id",
            "</script>"
    })
    List<RoomAssignment> selectActiveByRoomIds(@Param("roomIds") List<Long> roomIds,
                                               @Param("statuses") Set<AssignmentStatus> statuses);

    @Select({
            "<script>",
            "SELECT * FROM room_assignment",
            "WHERE room_id IN",
            "<foreach collection='roomIds' item='roomId' open='(' separator=',' close=')'>",
            "#{roomId}",
            "</foreach>",
            "AND assignment_status = 'CHECKED_OUT'",
            "AND is_deleted = 0",
            "ORDER BY room_id, check_out_time DESC, id DESC",
            "</script>"
    })
    List<RoomAssignment> selectCheckedOutByRoomIds(@Param("roomIds") List<Long> roomIds);

    @Select("SELECT * FROM room_assignment " +
            "WHERE order_id = #{orderId} AND is_deleted = 0 " +
            "ORDER BY room_id, id FOR UPDATE")
    List<RoomAssignment> selectByOrderIdForUpdate(@Param("orderId") Long orderId);

    @Update("UPDATE room_assignment " +
            "SET assignment_status = 'CHECKED_OUT', check_out_time = #{checkOutTime}, " +
            "    version = version + 1, updated_at = #{updatedAt} " +
            "WHERE id = #{assignmentId} " +
            "  AND assignment_status = 'CHECKED_IN' " +
            "  AND version = #{version} " +
            "  AND is_deleted = 0")
    int markCheckedOutWithVersion(@Param("assignmentId") Long assignmentId,
                                  @Param("version") Integer version,
                                  @Param("checkOutTime") LocalDateTime checkOutTime,
                                  @Param("updatedAt") LocalDateTime updatedAt);
}
