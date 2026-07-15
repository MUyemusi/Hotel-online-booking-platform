USE zhisu_hotel;

-- =========================================================
-- 成员1：用户、角色、信用禁订和操作日志
-- 外键统一在03_indexes_constraints.sql中添加
-- =========================================================

-- 1. 用户表
CREATE TABLE IF NOT EXISTS sys_user (
                                        id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '用户唯一编号',
                                        username VARCHAR(50) NOT NULL COMMENT '登录用户名',
    password_hash VARCHAR(255) NOT NULL COMMENT 'BCrypt密码摘要',
    nickname VARCHAR(50) COMMENT '用户昵称',
    phone VARCHAR(20) NOT NULL COMMENT '用户手机号',
    email VARCHAR(100) COMMENT '用户邮箱',
    avatar_url VARCHAR(500) COMMENT '用户头像地址',
    status VARCHAR(20) NOT NULL DEFAULT 'ENABLED'
    COMMENT '账户状态：ENABLED-正常，DISABLED-禁用',
    consecutive_cancel_count INT NOT NULL DEFAULT 0
    COMMENT '当前连续有效取消次数',
    ban_until DATETIME NULL COMMENT '禁订截止时间，空值表示未禁订',
    last_login_at DATETIME NULL COMMENT '最后登录时间',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
    COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
    ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',

    UNIQUE KEY uk_user_username (username),
    UNIQUE KEY uk_user_phone (phone),
    UNIQUE KEY uk_user_email (email),
    INDEX idx_user_status (status),
    INDEX idx_user_ban_until (ban_until)
    ) ENGINE=InnoDB
    DEFAULT CHARSET=utf8mb4
    COLLATE=utf8mb4_unicode_ci
    COMMENT='用户表';


-- 2. 角色表
CREATE TABLE IF NOT EXISTS sys_role (
                                        id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '角色唯一编号',
                                        role_code VARCHAR(30) NOT NULL COMMENT '角色编码',
    role_name VARCHAR(50) NOT NULL COMMENT '角色名称',
    description VARCHAR(200) COMMENT '角色说明',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
    COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
    ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',

    UNIQUE KEY uk_role_code (role_code)
    ) ENGINE=InnoDB
    DEFAULT CHARSET=utf8mb4
    COLLATE=utf8mb4_unicode_ci
    COMMENT='角色表';


-- 3. 用户角色关联表
CREATE TABLE IF NOT EXISTS sys_user_role (
                                             id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '关联记录编号',
                                             user_id BIGINT NOT NULL COMMENT '用户编号',
                                             role_id BIGINT NOT NULL COMMENT '角色编号',
                                             created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
                                             COMMENT '创建时间',

                                             UNIQUE KEY uk_user_role (user_id, role_id),
    INDEX idx_user_role_user_id (user_id),
    INDEX idx_user_role_role_id (role_id)
    ) ENGINE=InnoDB
    DEFAULT CHARSET=utf8mb4
    COLLATE=utf8mb4_unicode_ci
    COMMENT='用户角色关联表';


-- 4. 信用变动记录表
CREATE TABLE IF NOT EXISTS credit_record (
                                             id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '信用记录编号',
                                             user_id BIGINT NOT NULL COMMENT '关联用户编号',
                                             order_id BIGINT NULL COMMENT '关联订单编号',
                                             change_type VARCHAR(30) NOT NULL
    COMMENT '变动类型：CANCEL、COMPLETE、BAN、UNBAN',
    before_count INT NOT NULL DEFAULT 0
    COMMENT '变动前连续取消次数',
    after_count INT NOT NULL DEFAULT 0
    COMMENT '变动后连续取消次数',
    ban_until DATETIME NULL COMMENT '本次变动后的禁订截止时间',
    reason VARCHAR(255) NULL COMMENT '信用变动原因',
    operator_id BIGINT NULL COMMENT '管理员操作人编号',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
    COMMENT '信用变动时间',

    INDEX idx_credit_user_id (user_id),
    INDEX idx_credit_order_id (order_id),
    INDEX idx_credit_change_type (change_type),
    INDEX idx_credit_created_at (created_at),
    UNIQUE KEY uk_credit_order_type (order_id, change_type)
    ) ENGINE=InnoDB
    DEFAULT CHARSET=utf8mb4
    COLLATE=utf8mb4_unicode_ci
    COMMENT='用户信用变动记录表';


-- 5. 操作日志表
CREATE TABLE IF NOT EXISTS operation_log (
                                             id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '日志唯一编号',
                                             operator_id BIGINT NULL COMMENT '操作人员编号',
                                             module VARCHAR(50) NOT NULL COMMENT '操作所属模块',
    action VARCHAR(50) NOT NULL COMMENT '操作类型',
    request_method VARCHAR(10) NULL COMMENT 'HTTP请求方法',
    request_uri VARCHAR(255) NULL COMMENT '请求地址',
    result VARCHAR(20) NOT NULL COMMENT '操作结果：SUCCESS或FAIL',
    ip_address VARCHAR(45) NULL COMMENT '操作者IP地址',
    detail VARCHAR(500) NULL COMMENT '操作对象、原因及结果说明',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
    COMMENT '操作发生时间',

    INDEX idx_log_operator_id (operator_id),
    INDEX idx_log_module (module),
    INDEX idx_log_action (action),
    INDEX idx_log_created_at (created_at)
    ) ENGINE=InnoDB
    DEFAULT CHARSET=utf8mb4
    COLLATE=utf8mb4_unicode_ci
    COMMENT='系统操作日志表';