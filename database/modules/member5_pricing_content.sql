USE zhisu_hotel;

CREATE TABLE IF NOT EXISTS hotel_info (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '闂備胶澧楃敮鎺旇姳閸偆鈹嶉柍鈺佸暕缁辨牕鈽夐幘鎰佺吋闁轰胶鎷',
    hotel_name VARCHAR(100) NOT NULL COMMENT '闂備胶澧楃敮鎺旇姳閻㈢瑙︾€广儱娉?,
    logo_url VARCHAR(500) DEFAULT NULL COMMENT '闂備胶澧楃敮鎺旇姳缁€姝癵o闂侀潻闄勫妯侯焽?,
    introduction TEXT COMMENT '闂備胶澧楃敮鎺旇姳閸偆顩烽悗锝庡墰閻?,
    address VARCHAR(255) DEFAULT NULL COMMENT '闂備胶澧楃敮鎺旇姳閻㈢鎹堕柡澶嬪缁?,
    contact_phone VARCHAR(30) DEFAULT NULL COMMENT '闂佽壈椴稿濠氭焾閹绢喗鍋ㄥù锝呭暟濡?,
    facilities TEXT COMMENT '闂備胶澧楃敮鎺旇姳閻㈠灚濯奸柣鎰靛墯閻撱垽鎮归崶褏鎽犳俊?,
    checkin_notice TEXT COMMENT '闂佺绻堥崕鎵礊閸ャ劊浜滈柤濮愬€楅崣鈧?,
    cancellation_policy TEXT COMMENT '闂佸憡鐟﹂悧妤冪矓妞嬪孩鍠嗛柛鏇ㄥ亜閻?,
    traffic_info TEXT COMMENT '婵炲瓨鍤庨崐婵嬪焵椤掍胶鐓ǎ鍥э躬楠?,
    faq_content TEXT COMMENT '闁汇埄鍨遍悺鏇綖閸℃稒鈷掓い鏇楀亾妞?,
    status VARCHAR(20) NOT NULL DEFAULT 'ENABLED' COMMENT '闂佺粯顭堥崺鏍焵椤戣法绛忕紒杈╂NABLED-闂佸憡鍑归崹鎶藉极閵堝鏅悗娑樼摜SABLED-闂佺顑嗙划搴ㄥ极?,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '闂佸憡甯楃粙鎴犵磽閹捐绫嶉柛顐ｆ礃閿?,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '闂佸搫娲ら悺銊╁蓟婵犲洤绫嶉柛顐ｆ礃閿?
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci
  COMMENT='闂備胶澧楃敮鎺旇姳閻㈢鏄ラ柣鎴炆戦幏鍗炃庨崶锝呭⒉濞寸厧鎳愰幃?;

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
    '闂佸搫鎳庨幖顐︻敊閺嶎厽鐓€闁圭儤鍨归弳?,
    '闂佸搫鎳庨幖顐︻敊閺嶎厽鐓€闁圭儤鍨归弳妤呮煛閸曢潧鐏熺紒鏂款嚟閳ь剛鎳撻悥鐓庮焽娴兼潙瑙﹂柟瀛樼箖濞呭矂鏌涢弬琛″亾閸愬弶鐦旈柣鐐寸☉婵傛梻绮仦鎯ь嚤闁硅揪闄勯敍婵嬫煛閸愩劍鍣洪柣鏍煐閹峰懓绠涢幙鍐ㄦ倕闂佹眹鍔岀€氼亪鎳欓幋鐘辩剨闊洦鎸鹃埀顒傚厴瀹曪繝寮村杈┬梻浣哄鐢帞鑺遍悽绋课?,
    '閻庡灚婢橀幊鎰邦敇閹间焦鍋犻柛鈩冾殕閸犲懐绱撴担鍝ユ憼濠?,
    '閻庡灚婢橀幊鎰邦敇閹间焦鍋犻柛鈩冾殕閸犲懐绱撴担鍝ユ憼濠?,
    'ENABLED'
)
ON DUPLICATE KEY UPDATE
    hotel_name = VALUES(hotel_name);

CREATE TABLE IF NOT EXISTS hotel_banner (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '闁哄鍎愰崰妤呭箰闁秴鐐婄紓宥庢暓',
    title VARCHAR(100) NOT NULL COMMENT '闁哄鍎愰崰妤呭箰闁秴鐐婇柣鎰靛墰閸ㄧ厧螞?,
    image_url VARCHAR(500) NOT NULL COMMENT '闂佹悶鍎辨晶鑺ユ櫠閺嶎厼鎹堕柡澶嬪缁?,
    link_url VARCHAR(500) DEFAULT NULL COMMENT '闁荤姴鎼悿鍥归崱娑樻嵍闁哄瀵х徊?,
    sort_no INT NOT NULL DEFAULT 0 COMMENT '闂佸湱鍎ょ敮鎺旇姳椤撶喐鍎熼煫鍥ㄦ尭婵炲洭鏌ㄥ☉妯肩劮缂佸娅ｆ禍姝岀疀濮樻瘷銊╂⒒閸垹孝濠?,
    status VARCHAR(20) NOT NULL DEFAULT 'OFFLINE' COMMENT '闂佺粯顭堥崺鏍焵椤戣法绛忕紒杈╂珒UBLISHED-閻庣懓鎲¤ぐ鍐亹閸屾粍鏆滈柛鎰硶缁€濉怓FLINE-閻庤鐡曞鎾剁箔閸涙潙鍑?,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '闂佸憡甯楃粙鎴犵磽閹捐绫嶉柛顐ｆ礃閿?,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '闂佸搫娲ら悺銊╁蓟婵犲洤绫嶉柛顐ｆ礃閿?,
    INDEX idx_banner_status_sort (status, sort_no)
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci
  COMMENT='闂備胶澧楃敮鎺旇姳閸偓绱旈柡宥庡幑閳ь剙顦板顏堫敊閼恒儱鍟婇梺鎼炲劜瀹曟﹢濡?;

CREATE TABLE IF NOT EXISTS announcement (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '闂佺娴氶崜娆撳箟椤″粚',
    title VARCHAR(150) NOT NULL COMMENT '闂佺娴氶崜娆撳箟閿熺姴鍐€闁搞儺鍓﹂弳?,
    content TEXT NOT NULL COMMENT '闂佺娴氶崜娆撳箟閿熺姴绀冮柛娑卞弾閸?,
    status VARCHAR(20) NOT NULL DEFAULT 'DRAFT' COMMENT '闂佺粯顭堥崺鏍焵椤戣法绛忕紒杈╂RAFT-闂佽壈妫勯ˇ閬嶁€﹂崼銉︽櫖閻庢稓鎸夿LISHED-閻庣懓鎲¤ぐ鍐亹閸屾粍鏆滈柛鎰硶缁€濉怓FLINE-閻庤鐡曞鎾剁箔閸涙潙鍑?,
    is_top TINYINT NOT NULL DEFAULT 0 COMMENT '闂佸搫瀚烽崹浼村箚娴ｈ櫣纾炬い鏇楀亾闁靛棗锕弫?-闂佸憡鐔拋锝囨?-闂?,
    publish_time DATETIME DEFAULT NULL COMMENT '闂佸憡鐟﹂崹鐢电博閻戣棄绫嶉柛顐ｆ礃閿?,
    offline_time DATETIME DEFAULT NULL COMMENT '婵炴垶鎸搁鍡涙倶閿曞倸绫嶉柛顐ｆ礃閿?,
    created_by BIGINT DEFAULT NULL COMMENT '闂佸憡甯楃粙鎴犵磽閹惧顩茬紒鈩冨',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '闂佸憡甯楃粙鎴犵磽閹捐绫嶉柛顐ｆ礃閿?,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '闂佸搫娲ら悺銊╁蓟婵犲洤绫嶉柛顐ｆ礃閿?,
    INDEX idx_announcement_status_time (status, publish_time),
    INDEX idx_announcement_top_time (is_top, publish_time)
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci
  COMMENT='闂備胶澧楃敮鎺旇姳閻㈢绀傛い鎺嗗亾闁硅绱曢幃?;

CREATE TABLE IF NOT EXISTS system_config (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '缂備緡鍨靛畷鐢靛垝濞差亜鐭楅柛灞剧⊕濞堢D',
    config_key VARCHAR(100) NOT NULL COMMENT '闂佸憡鐟ラ崐褰掑汲閻斿吋鐓?,
    config_value VARCHAR(500) NOT NULL COMMENT '闂佸憡鐟ラ崐褰掑汲閻旂厧纾?,
    config_name VARCHAR(100) NOT NULL COMMENT '闂佸憡鐟ラ崐褰掑汲閻旂厧瑙︾€广儱娉?,
    value_type VARCHAR(20) NOT NULL DEFAULT 'STRING' COMMENT '闂佺锕ㄩ崑鎰偖椤愶箑鍨傞悗锝呭缁愭〗TRING闂侀潧妫斿ù鐢員EGER闂侀潧妫旂粭宀碈IMAL闂侀潧妫旂粻鎱朚E',
    config_group VARCHAR(50) NOT NULL DEFAULT 'GENERAL' COMMENT '闂佸憡鐟ラ崐褰掑汲閻旂厧绀嗛柛鈩冪懅閻?,
    description VARCHAR(500) DEFAULT NULL COMMENT '闂佸憡鐟ラ崐褰掑汲閻旂儤瀚氶悗娑櫳戦～?,
    status VARCHAR(20) NOT NULL DEFAULT 'ENABLED' COMMENT '闂佺粯顭堥崺鏍焵椤戣法绛忕紒杈╂NABLED闂侀潧妫旂粭宀籗ABLED',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '闂佸憡甯楃粙鎴犵磽閹捐绫嶉柛顐ｆ礃閿?,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '闂佸搫娲ら悺銊╁蓟婵犲洤绫嶉柛顐ｆ礃閿?,
    UNIQUE KEY uk_system_config_key (config_key),
    INDEX idx_system_config_group (config_group)
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci
  COMMENT='缂備緡鍨靛畷鐢靛垝濞差亜鐭楅柛灞剧⊕濞堝爼姊洪弶璺ㄐら柣銈呮閹?;

INSERT IGNORE INTO system_config
(config_key, config_value, config_name, value_type, config_group, description)
VALUES
('MAX_BOOKING_DAYS', '7', '闂佸搫鐗冮崑鎾愁熆閸棗鍟拋鎻捨涢弶鍨伂妞ゆ捁顫夊鍕煛閸屾稒顔?, 'INTEGER', 'BOOKING',
 '闂佹椿娼块崝宥夊春濞戙垹瀚夐柍褜鍓氬鍕潩椤掆偓鐠佹煡姊洪銏╂Ч閻庢哎鍔戝鐢割敂閸涱噣妾锋繝銏ｅ煐閼归箖鎯冮娑樼窞闁宠桨绀侀弫鍫曟煟閵娿儱顏╅柛娆忔閹峰懓绠涢弮鈧敍澶愭煛?),
('PAYMENT_TIMEOUT_MINUTES', '15', '闂佽　鍋撴い鏍ㄧ懅鐢盯鎮洪幒鎴炲櫤婵＄偛鍊垮顕€宕奸弴鐕傜吹', 'INTEGER', 'ORDER',
 '閻庡灚婢橀幊蹇涘极椤旂晫顩锋俊顖涱儥閸氬倿鏌涘Δ浣圭缂佸鐡ㄥ濠氬炊椤忓秴娈欓梺鍛婂笒濡繈骞愰幘璇叉瀬闁哄瀵ч崐鐢告煠婵傚绨诲┑顔规櫊瀹曟骞庨懞銉川'),
('CANCEL_THRESHOLD', '3', '闁哄鏅濋崑鐔兼偨婵犳艾鐭楅柡宥冨妿鍟搁梻鍌氬暙閻楀棝鍩€?, 'INTEGER', 'CREDIT',
 '闁哄鏅濋崑鐔兼偨婵犳艾瀚夊璺侯儐濞呭繘鏌涘▎鎰缂佸鏀卞蹇涘箚瑜嶉悡鍌炴偣閸ャ儱鍔滄い鎰偢瀵偊寮堕幐搴♀偓鐢告偡濞嗘瑧鐣辩憸鏉垮€荤划瀣媴缁涘鎮?),
('BAN_DAYS', '7', '缂備礁鍊峰ù鍥敇鐟欏嫬绶為柍鍝勫€瑰▓?, 'INTEGER', 'CREDIT',
 '闁哄鐗婇崕鎶藉春鐏炵偓浜ら柣鎰暩閺侀箖鏌涘▎鎰缂佸鍏樺濂稿醇濠靛啫浜鹃柣鎾冲閸婄敻鏌ｉ妸銉ヮ伀妞も晪濡囬幏瀣閻橆喕鍑介梺?),
('CHECKIN_TIME', '14:00', '婵帗绋掗…鍫ヮ敇婵犳艾绀傞柕澶堝€楃粔鍫曟煛閸愩劎鍩ｆ俊?, 'TIME', 'FRONTDESK',
 '闂備胶澧楃敮鎺旇姳閸偒娓舵俊顖涱儥閸氬洭鏌涢弮鍌氫户闁诡喖锕畷妤呭Ψ閵堝洨肖闂佸搫鍟悥鐓幬?),
('CHECKOUT_TIME', '12:00', '婵帗绋掗…鍫ヮ敇婵犳碍鐒婚柍褜鍓熼獮瀣箰鎼淬埄妲梻?, 'TIME', 'FRONTDESK',
 '闂備胶澧楃敮鎺旇姳閸偒娓舵俊顖涱儥閸氬洭姊洪鐔蜂壕闂佺懓鐡ㄧ€笛囧春閸涱収娼伴柕蹇婂墲椤ρ囨⒒?),
('INVENTORY_WARNING_VALUE', '2', '闁圭厧鐡ㄩ幐鎼佹偤閵婏讣绱ｉ柛鏇ㄥ櫘閸斿懘鏌?, 'INTEGER', 'INVENTORY',
 '闂佸憡鐟崹浼村极椤撶喐鍎熼柟鎯х－閹界娀鎮樿箛鎾剁缂侇煈鍓熼獮瀣冀椤愩倖鎯ｆ繛瀛樼矊濮橈箓顢氬璺虹９闁稿繐鍚嬮ˇ褔鏌熺紒妯哄闁靛洦纰嶉幆鏃堝箻瀹曞洦鎲肩紓浣圭槺閸愵亞鍊?),
('IMAGE_MAX_SIZE_MB', '5', '闂佹悶鍎辨晶鑺ユ櫠閺嶎厼瀚夐柍褜鍓氬鍕槺缂佹鍊圭€靛ジ鎮╃仦鍌欐捣闁?, 'INTEGER', 'CONTENT',
 '闂佹悶鍎辨晶鑺ユ櫠閺嶃劎鈻斿┑鐘辫兌閻愬﹪鏌￠崒姘煑婵炲棎鍨介幆鍐礋椤掍椒绮繝銏犵垻閸愭儳鏅ｉ梻浣瑰絻妤犲繒妲愬┑瀣闁哄洦姘ㄧ粔纰滲'),
('DEFAULT_HOLIDAY_COEFFICIENT', '1.20', '婵帗绋掗…鍫ヮ敇婵犳碍鍤嶉柛灞剧煯濞寸兘鏌￠崘锕€鍔欓柣婊庡灦瀵粙鎳栭埡鍛帣闂?, 'DECIMAL', 'PRICING',
 '闂佸搫鍊瑰姗€路閸愵喗鍤嶉柛灞剧煯濞寸兘鏌￠崘锕€鍔欓柣婊庡灦瀵粙鎳濋柇锔界秷闂佸憡甯楅悷锕€顪冮崒娑欏閻犳亽鍔嶉弳蹇涙煟閵娿儱顏紒顕呭灣閹峰濡堕崶顒€鍘欓梺?);

CREATE TABLE IF NOT EXISTS price_operation_log (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '婵炲濞€閺€閬嶆偋閹间礁绠肩€广儱瀚粙濠囨煛閸愶箑鍔氱紒缁樻憼D',
    room_type_id BIGINT NOT NULL COMMENT '闂佽娼欓悘婵嬫倵缁嬨€?,
    room_type_name VARCHAR(100) NOT NULL COMMENT '闂佺懓鐏濈粔宕囩礊閺冨牆绫嶉柤鎼佹涧閻擄綁鏌涢妸銉剰闁诡喗娲滅划?,
    old_price DECIMAL(10,2) NOT NULL COMMENT '婵烇絽娴傞崰妤呭极婵傜绀堢€广儱鎳愰崬銊х棯椤撴稑浜炬繛瀵稿█閺€閬嶆偋?,
    new_price DECIMAL(10,2) NOT NULL COMMENT '婵烇絽娴傞崰妤呭极婵傜瑙﹂幖杈剧磿閸炪劎鐥娑樹壕婵炲濞€閺€閬嶆偋?,
    change_reason VARCHAR(200) NOT NULL COMMENT '婵炲濞€閺€閬嶆偋閸涘﹦鈹嶆い鏃囧Г閺嗩參鏌涘Ο鐓庢瀻婵?,
    operator_id BIGINT DEFAULT NULL COMMENT '闂佺懓鐏濈粔宕囩礊閺冣偓缁傚秶寮ч幍?,
    operator_name VARCHAR(100) DEFAULT NULL COMMENT '闂佺懓鐏濈粔宕囩礊閺冣偓缁傚秹宕崟顒€鈧磭绱?,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '闂佺懓鐏濈粔宕囩礊閺冨牆绫嶉柛顐ｆ礃閿?,
    INDEX idx_price_log_room_type (room_type_id),
    INDEX idx_price_log_created_at (created_at)
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci
  COMMENT='闂佺硶鏅炲▍锝夈€侀崨顔绢浄闂佸灝顑囨竟鎰庨崶璺烘灍闁轰礁銈稿顕€濡烽妷褏顔嶉柣?;
CREATE TABLE IF NOT EXISTS holiday_calendar (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT 'special date id',
    business_date DATE NOT NULL COMMENT 'business date',
    holiday_name VARCHAR(100) NOT NULL COMMENT 'holiday or makeup workday name',
    holiday_code VARCHAR(50) NOT NULL COMMENT 'holiday code',
    day_type VARCHAR(30) NOT NULL COMMENT 'HOLIDAY or MAKEUP_WORKDAY',
    coefficient DECIMAL(5,2) NOT NULL DEFAULT 1.00 COMMENT 'date price coefficient',
    holiday_year INT NOT NULL COMMENT 'holiday year',
    source_status VARCHAR(20) NOT NULL DEFAULT 'OFFICIAL' COMMENT 'OFFICIAL or PROVISIONAL',
    status VARCHAR(20) NOT NULL DEFAULT 'ENABLED' COMMENT 'ENABLED or DISABLED',
    remark VARCHAR(255) DEFAULT NULL COMMENT 'remark',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_holiday_business_date (business_date),
    INDEX idx_holiday_year (holiday_year),
    INDEX idx_holiday_type_status (day_type, status)
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci
  COMMENT='holiday calendar';

INSERT IGNORE INTO system_config
(config_key, config_value, config_name, value_type, config_group, description)
VALUES
('FRIDAY_PRICE_COEFFICIENT', '1.05', 'Friday price coefficient', 'DECIMAL', 'PRICING', 'Friday room price coefficient'),
('SATURDAY_PRICE_COEFFICIENT', '1.10', 'Saturday price coefficient', 'DECIMAL', 'PRICING', 'Saturday room price coefficient'),
('SUNDAY_PRICE_COEFFICIENT', '1.05', 'Sunday price coefficient', 'DECIMAL', 'PRICING', 'Sunday room price coefficient'),
('MAJOR_HOLIDAY_COEFFICIENT', '1.30', 'Major holiday coefficient', 'DECIMAL', 'PRICING', 'Spring Festival and National Day coefficient');

INSERT INTO holiday_calendar (
    business_date,
    holiday_name,
    holiday_code,
    day_type,
    coefficient,
    holiday_year,
    source_status,
    status,
    remark
) VALUES
('2026-09-20', 'National Day makeup workday', 'NATIONAL_DAY', 'MAKEUP_WORKDAY', 1.00, 2026, 'OFFICIAL', 'ENABLED', 'Sunday workday by official holiday arrangement'),
('2026-09-25', 'Mid-Autumn Festival holiday', 'MID_AUTUMN', 'HOLIDAY', 1.20, 2026, 'OFFICIAL', 'ENABLED', 'Holiday day 1'),
('2026-09-26', 'Mid-Autumn Festival holiday', 'MID_AUTUMN', 'HOLIDAY', 1.20, 2026, 'OFFICIAL', 'ENABLED', 'Holiday day 2'),
('2026-09-27', 'Mid-Autumn Festival holiday', 'MID_AUTUMN', 'HOLIDAY', 1.20, 2026, 'OFFICIAL', 'ENABLED', 'Holiday day 3'),
('2026-10-01', 'National Day holiday', 'NATIONAL_DAY', 'HOLIDAY', 1.30, 2026, 'OFFICIAL', 'ENABLED', 'Holiday day 1'),
('2026-10-02', 'National Day holiday', 'NATIONAL_DAY', 'HOLIDAY', 1.30, 2026, 'OFFICIAL', 'ENABLED', 'Holiday day 2'),
('2026-10-03', 'National Day holiday', 'NATIONAL_DAY', 'HOLIDAY', 1.30, 2026, 'OFFICIAL', 'ENABLED', 'Holiday day 3'),
('2026-10-04', 'National Day holiday', 'NATIONAL_DAY', 'HOLIDAY', 1.30, 2026, 'OFFICIAL', 'ENABLED', 'Holiday day 4'),
('2026-10-05', 'National Day holiday', 'NATIONAL_DAY', 'HOLIDAY', 1.30, 2026, 'OFFICIAL', 'ENABLED', 'Holiday day 5'),
('2026-10-06', 'National Day holiday', 'NATIONAL_DAY', 'HOLIDAY', 1.30, 2026, 'OFFICIAL', 'ENABLED', 'Holiday day 6'),
('2026-10-07', 'National Day holiday', 'NATIONAL_DAY', 'HOLIDAY', 1.30, 2026, 'OFFICIAL', 'ENABLED', 'Holiday day 7'),
('2026-10-10', 'National Day makeup workday', 'NATIONAL_DAY', 'MAKEUP_WORKDAY', 1.00, 2026, 'OFFICIAL', 'ENABLED', 'Saturday workday by official holiday arrangement'),
('2027-01-01', 'New Year', 'NEW_YEAR', 'HOLIDAY', 1.20, 2027, 'PROVISIONAL', 'ENABLED', 'Core statutory date pending official release'),
('2027-02-05', 'Spring Festival Eve', 'SPRING_FESTIVAL', 'HOLIDAY', 1.30, 2027, 'PROVISIONAL', 'ENABLED', 'Core statutory date pending official release'),
('2027-02-06', 'Spring Festival Day 1', 'SPRING_FESTIVAL', 'HOLIDAY', 1.30, 2027, 'PROVISIONAL', 'ENABLED', 'Core statutory date pending official release'),
('2027-02-07', 'Spring Festival Day 2', 'SPRING_FESTIVAL', 'HOLIDAY', 1.30, 2027, 'PROVISIONAL', 'ENABLED', 'Core statutory date pending official release'),
('2027-02-08', 'Spring Festival Day 3', 'SPRING_FESTIVAL', 'HOLIDAY', 1.30, 2027, 'PROVISIONAL', 'ENABLED', 'Core statutory date pending official release'),
('2027-04-05', 'Qingming Festival', 'QINGMING', 'HOLIDAY', 1.20, 2027, 'PROVISIONAL', 'ENABLED', 'Pending official holiday arrangement'),
('2027-05-01', 'Labor Day', 'LABOR_DAY', 'HOLIDAY', 1.20, 2027, 'PROVISIONAL', 'ENABLED', 'Core statutory date pending official release'),
('2027-05-02', 'Labor Day', 'LABOR_DAY', 'HOLIDAY', 1.20, 2027, 'PROVISIONAL', 'ENABLED', 'Core statutory date pending official release'),
('2027-06-09', 'Dragon Boat Festival', 'DRAGON_BOAT', 'HOLIDAY', 1.20, 2027, 'PROVISIONAL', 'ENABLED', 'Pending official holiday arrangement')
ON DUPLICATE KEY UPDATE
    holiday_name = VALUES(holiday_name),
    holiday_code = VALUES(holiday_code),
    day_type = VALUES(day_type),
    coefficient = VALUES(coefficient),
    holiday_year = VALUES(holiday_year),
    source_status = VALUES(source_status),
    status = VALUES(status),
    remark = VALUES(remark);
-- =========================================================
-- 鎴愬憳5锛氭埧鍨嬫瘡鏃ラ攢鍞环鏍?-- 姣忎釜鎴垮瀷銆佹瘡涓棩鏈熶粎鍏佽瀛樺湪涓€鏉′环鏍艰褰?-- =========================================================

CREATE TABLE IF NOT EXISTS daily_price (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '姣忔棩浠锋牸ID',

    room_type_id BIGINT NOT NULL COMMENT '鎴垮瀷ID',

    business_date DATE NOT NULL COMMENT '涓氬姟鏃ユ湡',

    base_price DECIMAL(10, 2) NOT NULL COMMENT '鐢熸垚鏃剁殑鎴垮瀷鍩虹浠锋牸',

    date_coefficient DECIMAL(5, 2) NOT NULL DEFAULT 1.00
        COMMENT '鏃ユ湡浠锋牸绯绘暟',

    final_price DECIMAL(10, 2) NOT NULL COMMENT '鏈€缁堥攢鍞环鏍?,

    price_source VARCHAR(50) NOT NULL
        COMMENT '浠锋牸鏉ユ簮锛歂ORMAL_DAY銆丗RIDAY銆丼ATURDAY銆丼UNDAY銆丮AKEUP_WORKDAY鎴栬妭鍋囨棩浠ｇ爜',

    price_description VARCHAR(150) DEFAULT NULL
        COMMENT '浠锋牸鏉ユ簮璇存槑',

    holiday_calendar_id BIGINT DEFAULT NULL
        COMMENT '鍏宠仈鐗规畩鏃ユ湡ID锛屾櫘閫氭棩鏈熶负绌?,

    source_status VARCHAR(20) NOT NULL DEFAULT 'SYSTEM'
        COMMENT '鏁版嵁鏉ユ簮鐘舵€侊細SYSTEM銆丱FFICIAL銆丳ROVISIONAL',

    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
        COMMENT '鍒涘缓鏃堕棿',

    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
        ON UPDATE CURRENT_TIMESTAMP COMMENT '鏇存柊鏃堕棿',

    UNIQUE KEY uk_daily_price_room_date (
        room_type_id,
        business_date
    ),

    INDEX idx_daily_price_date (business_date),

    INDEX idx_daily_price_room_type (room_type_id),

    INDEX idx_daily_price_source (price_source)

) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci
  COMMENT = '鎴垮瀷姣忔棩閿€鍞环鏍艰〃';
INSERT INTO system_config (
    config_key,
    config_value,
    config_name,
    value_type,
    config_group,
    description
)
VALUES
(
    'BOOKING_WINDOW_MONTHS',
    '12',
    'Booking window months',
    'INTEGER',
    'BOOKING',
    'Maximum months ahead for check-in date'
),
(
    'MAX_STAY_NIGHTS',
    '30',
    'Maximum stay nights',
    'INTEGER',
    'BOOKING',
    'Maximum number of nights allowed in one booking'
)
ON DUPLICATE KEY UPDATE
    config_value = VALUES(config_value),
    config_name = VALUES(config_name),
    value_type = VALUES(value_type),
    config_group = VALUES(config_group),
    description = VALUES(description);
-- Rolling booking window support
ALTER TABLE daily_price
    ADD COLUMN IF NOT EXISTS manual_price DECIMAL(10,2) DEFAULT NULL COMMENT 'manual override price',
    ADD COLUMN IF NOT EXISTS manual_override TINYINT NOT NULL DEFAULT 0 COMMENT 'whether manual override is enabled',
    ADD COLUMN IF NOT EXISTS override_reason VARCHAR(200) DEFAULT NULL COMMENT 'manual override reason';
