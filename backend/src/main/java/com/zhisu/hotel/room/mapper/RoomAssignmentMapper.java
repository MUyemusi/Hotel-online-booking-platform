package com.zhisu.hotel.room.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zhisu.hotel.common.enums.AssignmentStatus;
import com.zhisu.hotel.room.entity.RoomAssignment;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

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
}
