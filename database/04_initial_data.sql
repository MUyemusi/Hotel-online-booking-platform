USE zhisu_hotel;

-- 初始化角色数据
INSERT INTO sys_role (role_code, role_name, description) VALUES
('USER', '普通用户', '普通用户角色'),
('FRONT_DESK', '前台工作人员', '前台工作人员角色'),
('ADMIN', '酒店管理员', '酒店管理员角色')
ON DUPLICATE KEY UPDATE role_name=VALUES(role_name);
