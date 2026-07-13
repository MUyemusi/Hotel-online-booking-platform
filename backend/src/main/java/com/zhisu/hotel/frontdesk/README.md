# Frontdesk Module - 前台模块
## 负责人：成员4

## 负责范围
- 分房
- 入住办理
- 退房办理
- 房间清洁
- 维修管理
- 房态日志

## 依赖的其他模块
- common, booking, room

## 对外提供的Service
- FrontdeskService
  - checkIn
  - checkOut
  - markRoomCleaned

## 禁止直接访问的其他模块Mapper
- booking的Mapper（通过Service调用）
- user的Mapper（通过Service调用）
- 其他模块的Mapper
