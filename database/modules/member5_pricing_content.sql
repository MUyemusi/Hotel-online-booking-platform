USE zhisu_hotel;

CREATE TABLE IF NOT EXISTS hotel_info (
                                          id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '酒店信息主键ID',
                                          hotel_name VARCHAR(100) NOT NULL COMMENT '酒店名称',
    logo_url VARCHAR(500) DEFAULT NULL COMMENT '酒店Logo地址',
    introduction TEXT COMMENT '酒店介绍',
    address VARCHAR(255) DEFAULT NULL COMMENT '酒店地址',
    contact_phone VARCHAR(30) DEFAULT NULL COMMENT '联系电话',
    facilities TEXT COMMENT '酒店设施说明',
    checkin_notice TEXT COMMENT '入住须知',
    cancellation_policy TEXT COMMENT '取消规则',
    traffic_info TEXT COMMENT '交通信息',
    faq_content TEXT COMMENT '常见问题',
    status VARCHAR(20) NOT NULL DEFAULT 'ENABLED'
    COMMENT '状态：ENABLED-启用，DISABLED-停用',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
    COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
    ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
    ) ENGINE=InnoDB
    DEFAULT CHARSET=utf8mb4
    COLLATE=utf8mb4_unicode_ci
    COMMENT='酒店基本信息表';

INSERT INTO hotel_info (
    id,
    hotel_name,
    introduction,
    address,
    contact_phone,
    status
)
VALUES (
           1,
           '智宿酒店',
           '智宿酒店是一家面向商务出行与休闲旅游住客的中小型单体酒店。',
           '待管理员维护',
           '待管理员维护',
           'ENABLED'
       )
    ON DUPLICATE KEY UPDATE
                         hotel_name = VALUES(hotel_name);