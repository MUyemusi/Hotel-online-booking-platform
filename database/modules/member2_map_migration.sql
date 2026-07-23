-- 为 hotel_info 表添加经纬度字段，用于地图展示
USE zhisu_hotel;
SET NAMES utf8mb4;

ALTER TABLE hotel_info
    ADD COLUMN IF NOT EXISTS latitude  DOUBLE DEFAULT NULL COMMENT '纬度',
    ADD COLUMN IF NOT EXISTS longitude DOUBLE DEFAULT NULL COMMENT '经度';

-- 更新默认酒店数据的坐标（以厦门为例，可根据实际位置修改）
UPDATE hotel_info SET latitude = 24.4798, longitude = 118.0894 WHERE latitude IS NULL LIMIT 1;
