USE zhisu_hotel;

-- 初始化角色数据
INSERT INTO sys_role (role_code, role_name, description) VALUES
('USER', '普通用户', '普通用户角色'),
('FRONT_DESK', '前台工作人员', '前台工作人员角色'),
('ADMIN', '酒店管理员', '酒店管理员角色')
ON DUPLICATE KEY UPDATE role_name=VALUES(role_name);

-- 默认管理员账号：admin / admin123
INSERT INTO sys_user (username, password, nickname, phone, email, status, credit_score, is_blacklisted)
VALUES (
    'admin',
    '$2a$10$JuRMjKGLYEPU/lkVlQdw6Oxk03xzuLlaicInPXjIThY0ZfFrR6kV6',
    '系统管理员',
    '13800000000',
    'admin@zhisu.local',
    'ENABLED',
    100,
    FALSE
)
ON DUPLICATE KEY UPDATE
    password = VALUES(password),
    nickname = VALUES(nickname),
    phone = VALUES(phone),
    email = VALUES(email),
    status = VALUES(status),
    credit_score = VALUES(credit_score),
    is_blacklisted = VALUES(is_blacklisted);

INSERT INTO sys_user_role (user_id, role_id)
SELECT u.id, r.id
FROM sys_user u
JOIN sys_role r ON r.role_code = 'ADMIN'
WHERE u.username = 'admin'
ON DUPLICATE KEY UPDATE user_id = VALUES(user_id), role_id = VALUES(role_id);
