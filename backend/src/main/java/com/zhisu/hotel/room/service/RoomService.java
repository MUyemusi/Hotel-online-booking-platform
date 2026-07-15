package com.zhisu.hotel.room.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhisu.hotel.common.enums.RoomStatus;
import com.zhisu.hotel.room.dto.RoomCreateRequest;
import com.zhisu.hotel.room.dto.RoomQueryRequest;
import com.zhisu.hotel.room.dto.RoomStatusChangeRequest;
import com.zhisu.hotel.room.dto.RoomUpdateRequest;
import com.zhisu.hotel.room.entity.Room;
import com.zhisu.hotel.room.vo.FloorRoomStatisticsVO;
import com.zhisu.hotel.room.vo.RoomDetailVO;
import com.zhisu.hotel.room.vo.RoomStatusLogVO;
import com.zhisu.hotel.room.vo.RoomVO;

import java.util.List;

public interface RoomService {

    Room createRoom(RoomCreateRequest request);

    Room updateRoom(Long id, RoomUpdateRequest request);

    RoomDetailVO getRoomDetail(Long id);

    Page<RoomVO> pageRooms(int page, int size, RoomQueryRequest request);

    List<RoomVO> getRoomsByFloor(Integer floor);

    void changeRoomStatus(Long id, RoomStatusChangeRequest request);

    List<RoomStatusLogVO> getRoomStatusLogs(Long roomId);

    List<FloorRoomStatisticsVO> getFloorStatistics();

    boolean checkRoomNoUnique(String roomNo);

    boolean validateRoomNoAndFloor(String roomNo, Integer floor);
}
