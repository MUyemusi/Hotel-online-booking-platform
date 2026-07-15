package com.zhisu.hotel.room.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zhisu.hotel.room.entity.RoomStatusLog;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface RoomStatusLogMapper extends BaseMapper<RoomStatusLog> {

    @Select({
            "<script>",
            "SELECT * FROM room_status_log",
            "WHERE room_id IN",
            "<foreach collection='roomIds' item='roomId' open='(' separator=',' close=')'>",
            "#{roomId}",
            "</foreach>",
            "AND after_status = 'DIRTY'",
            "ORDER BY room_id, created_at DESC, id DESC",
            "</script>"
    })
    List<RoomStatusLog> selectDirtyLogsByRoomIds(@Param("roomIds") List<Long> roomIds);
}
