USE zhisu_hotel;

-- =========================================================
-- 系统基础角色初始化
-- =========================================================

INSERT INTO sys_role (
    role_code,
    role_name,
    description
) VALUES
      ('USER', '普通用户', '浏览房型、提交预订、查看订单和个人信用'),
      ('FRONT_DESK', '前台工作人员', '办理分房、入住、退房、清洁和房态管理'),
      ('ADMIN', '酒店管理员', '管理用户、角色、酒店内容、价格和运营数据')
    ON DUPLICATE KEY UPDATE
                         role_name = VALUES(role_name),
                         description = VALUES(description);