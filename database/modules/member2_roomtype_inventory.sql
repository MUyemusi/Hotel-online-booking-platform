USE zhisu_hotel;
-- 1. 房型表
CREATE TABLE IF NOT EXISTS room_type (
    id              BIGINT PRIMARY KEY AUTO_INCREMENT  COMMENT '房型主键ID',
    name            VARCHAR(100) NOT NULL               COMMENT '房型名称',
    category        VARCHAR(20) NOT NULL                COMMENT '类别：SINGLE/STANDARD/TWIN/SUPERIOR/DELUXE/FAMILY/SUITE',
    base_price      DECIMAL(10,2) NOT NULL              COMMENT '基础价格（元）/ 每晚',
    area            INT                                 COMMENT '房间面积（平方米）',
    bed_type        VARCHAR(30)                         COMMENT '床型',
    bed_count       INT DEFAULT 1                       COMMENT '床的数量',
    max_guests      INT DEFAULT 2                       COMMENT '最多可住人数',
    has_breakfast   TINYINT DEFAULT 0                   COMMENT '含早餐：0-否，1-是',
    has_window      TINYINT DEFAULT 1                   COMMENT '有窗户：0-否，1-是',
    has_wifi        TINYINT DEFAULT 1                   COMMENT '有WiFi：0-否，1-是',
    has_ac          TINYINT DEFAULT 1                   COMMENT '有空调：0-否，1-是',
    allow_pets      TINYINT DEFAULT 0                   COMMENT '允许宠物：0-否，1-是',
    description     TEXT                                COMMENT '房型介绍',
    status          VARCHAR(20) NOT NULL DEFAULT 'ONLINE' COMMENT '状态：ONLINE-上架，OFFLINE-下架',
    is_deleted      TINYINT DEFAULT 0                   COMMENT '逻辑删除：0-正常，1-已删除',
    created_at      DATETIME DEFAULT CURRENT_TIMESTAMP  COMMENT '创建时间',
    updated_at      DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_status (status),
    INDEX idx_category (category),
    INDEX idx_is_deleted (is_deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='房型表';

-- 2. 房型图片表
CREATE TABLE IF NOT EXISTS room_type_image (
    id              BIGINT PRIMARY KEY AUTO_INCREMENT  COMMENT '图片主键ID',
    room_type_id    BIGINT NOT NULL                     COMMENT '所属房型ID',
    image_url       VARCHAR(500) NOT NULL               COMMENT '图片URL地址',
    image_type      VARCHAR(10) NOT NULL DEFAULT 'DETAIL' COMMENT '图片类型：MAIN-主图，DETAIL-详情图',
    sort_order      INT DEFAULT 0                       COMMENT '排序序号，越小越靠前',
    created_at      DATETIME DEFAULT CURRENT_TIMESTAMP  COMMENT '上传时间',
    INDEX idx_room_type (room_type_id),
    INDEX idx_image_type (image_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='房型图片表';

-- 3. 房型设施表
CREATE TABLE IF NOT EXISTS room_type_facility (
    id              BIGINT PRIMARY KEY AUTO_INCREMENT  COMMENT '设施主键ID',
    room_type_id    BIGINT NOT NULL                     COMMENT '所属房型ID',
    facility_name   VARCHAR(50) NOT NULL                COMMENT '设施名称',
    facility_icon   VARCHAR(100)                        COMMENT '设施图标标识',
    created_at      DATETIME DEFAULT CURRENT_TIMESTAMP  COMMENT '创建时间',
    INDEX idx_room_type (room_type_id),
    UNIQUE KEY uk_room_facility (room_type_id, facility_name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='房型设施表';

-- 4. 每日库存表
CREATE TABLE IF NOT EXISTS daily_inventory (
    id              BIGINT PRIMARY KEY AUTO_INCREMENT  COMMENT '库存主键ID',
    room_type_id    BIGINT NOT NULL                     COMMENT '房型ID',
    inventory_date  DATE NOT NULL                       COMMENT '库存日期',
    total_rooms     INT NOT NULL DEFAULT 0              COMMENT '总房间数',
    locked_rooms    INT NOT NULL DEFAULT 0              COMMENT '锁定库存（待支付订单占用）',
    booked_rooms    INT NOT NULL DEFAULT 0              COMMENT '已预订库存（已支付订单占用）',
    available_rooms INT NOT NULL DEFAULT 0              COMMENT '可售库存（总-锁定-已预订）',
    status          VARCHAR(20) NOT NULL DEFAULT 'ACTIVE' COMMENT '销售状态：ACTIVE-正常销售，PAUSED-暂停销售',
    created_at      DATETIME DEFAULT CURRENT_TIMESTAMP  COMMENT '创建时间',
    updated_at      DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_room_date (room_type_id, inventory_date),
    INDEX idx_room_type (room_type_id),
    INDEX idx_inventory_date (inventory_date),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='每日库存表';

-- 5. 库存变更日志表
CREATE TABLE IF NOT EXISTS inventory_log (
    id              BIGINT PRIMARY KEY AUTO_INCREMENT  COMMENT '日志主键ID',
    room_type_id    BIGINT NOT NULL                     COMMENT '房型ID',
    inventory_date  DATE NOT NULL                       COMMENT '库存日期',
    change_type     VARCHAR(30) NOT NULL                COMMENT '变更类型：LOCK-锁定，CONFIRM-确认，RELEASE-释放，CANCEL-取消，MANUAL-手动修改，PAUSE-暂停销售，RESUME-恢复销售',
    before_value    INT NOT NULL                        COMMENT '变更前可售数量',
    after_value     INT NOT NULL                        COMMENT '变更后可售数量',
    change_amount   INT NOT NULL                        COMMENT '变更数量（正数增加，负数减少）',
    operator        VARCHAR(50)                         COMMENT '操作人',
    change_reason   VARCHAR(200)                        COMMENT '变更原因',
    order_id        BIGINT                              COMMENT '关联订单ID',
    created_at      DATETIME DEFAULT CURRENT_TIMESTAMP  COMMENT '操作时间',
    INDEX idx_room_type (room_type_id),
    INDEX idx_inventory_date (inventory_date),
    INDEX idx_change_type (change_type),
    INDEX idx_order_id (order_id),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='库存变更日志表';


-- 初始化数据

-- 房型数据（7种，从经济到豪华）
INSERT INTO room_type (id, name, category, base_price, area, bed_type, bed_count, max_guests, has_breakfast, has_window, has_wifi, has_ac, allow_pets, description, status) VALUES
(1, '经济单人房',   'SINGLE',   128.00, 18, '单人床',     1, 1, 0, 1, 1, 1, 0, '小巧实惠，适合单人出行，基础配套齐全，性价比之选',                           'ONLINE'),
(2, '标准大床房',   'STANDARD', 228.00, 28, '大床',       1, 2, 0, 1, 1, 1, 0, '温馨舒适的标准大床房，简约装修风格，适合情侣出游或商务出行',                 'ONLINE'),
(3, '高级大床房',   'SUPERIOR', 328.00, 35, '大床',       1, 2, 1, 1, 1, 1, 0, '宽敞明亮高级大床房，城市景观落地窗，含双人自助早餐，享品质体验',             'ONLINE'),
(4, '商务双床房',   'TWIN',     298.00, 32, '双床',       2, 2, 1, 1, 1, 1, 0, '两张舒适单人床，配备双人办公桌，含双人早餐，出差拼房和旅行首选',             'ONLINE'),
(5, '豪华大床房',   'DELUXE',   458.00, 42, '大床',       1, 2, 1, 1, 1, 1, 0, '豪华装修大床房，独立浴缸+胶囊咖啡机，含行政早餐，享尊贵礼遇',               'ONLINE'),
(6, '家庭亲子房',   'FAMILY',   398.00, 45, '大床+小床',   2, 3, 1, 1, 1, 1, 1, '专为亲子家庭设计，儿童主题布置+玩具绘本，含三人早餐，宝宝友好型房型',       'ONLINE'),
(7, '豪华套房',     'SUITE',    688.00, 60, '大床',       1, 4, 1, 1, 1, 1, 0, '一室一厅独立套房，会客区与卧室分离，含双人早餐+下午茶+私人管家服务',       'ONLINE');

-- 各房型设施
INSERT INTO room_type_facility (room_type_id, facility_name, facility_icon) VALUES
-- 经济单人房：基础五件套
(1, '免费WiFi',        'wifi'),
(1, '空调',            'ac'),
(1, '24小时热水',      'hot-water'),
(1, '一次性洗漱用品',   'toiletries'),
(1, '液晶电视',        'tv'),
-- 标准大床房：基础六件套+办公
(2, '免费WiFi',        'wifi'),
(2, '空调',            'ac'),
(2, '24小时热水',      'hot-water'),
(2, '一次性洗漱用品',   'toiletries'),
(2, '液晶电视',        'tv'),
(2, '办公桌',          'desk'),
-- 高级大床房：升级版
(3, '高速WiFi',        'wifi'),
(3, '中央空调',        'ac'),
(3, '24小时热水',      'hot-water'),
(3, '品牌洗漱用品',     'toiletries'),
(3, '智能电视',        'tv'),
(3, '办公桌',          'desk'),
(3, '迷你吧',          'minibar'),
(3, '电子保险箱',      'safe'),
-- 商务双床房：双人办公
(4, '高速WiFi',        'wifi'),
(4, '中央空调',        'ac'),
(4, '24小时热水',      'hot-water'),
(4, '品牌洗漱用品',     'toiletries'),
(4, '智能电视',        'tv'),
(4, '双人办公桌',      'desk'),
(4, '电子保险箱',      'safe'),
-- 豪华大床房：全配套
(5, '高速WiFi',        'wifi'),
(5, '中央空调',        'ac'),
(5, '24小时热水',      'hot-water'),
(5, '高端洗护用品',     'toiletries'),
(5, '大屏智能电视',     'tv'),
(5, '办公桌',          'desk'),
(5, '迷你吧',          'minibar'),
(5, '电子保险箱',      'safe'),
(5, '独立浴缸',        'bathtub'),
(5, '胶囊咖啡机',      'coffee'),
(5, '欢迎水果',        'fruit'),
-- 家庭亲子房：亲子特色
(6, '免费WiFi',        'wifi'),
(6, '中央空调',        'ac'),
(6, '24小时热水',      'hot-water'),
(6, '儿童专属洗漱包',   'toiletries'),
(6, '智能电视',        'tv'),
(6, '儿童拖鞋浴袍',     'kids-slippers'),
(6, '儿童玩具套装',     'toys'),
(6, '宝宝床围栏',      'safety-rail'),
(6, '绘本读物',        'books'),
-- 豪华套房：顶配
(7, '高速WiFi',        'wifi'),
(7, '中央空调',        'ac'),
(7, '24小时热水',      'hot-water'),
(7, '奢华洗护用品',     'toiletries'),
(7, '超大智能电视',     'tv'),
(7, '独立会客厅',      'living-room'),
(7, '迷你吧',          'minibar'),
(7, '电子保险箱',      'safe'),
(7, '独立大浴缸',      'bathtub'),
(7, '胶囊咖啡机',      'coffee'),
(7, '欢迎水果+点心',    'fruit'),
(7, '私人管家服务',     'butler');
