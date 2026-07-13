# 智宿酒店在线预订与运营管理系统

## 项目简介

智宿酒店管理系统是为中小型单体酒店建设的自有线上售卖与运营管理系统，提供用户预订、前台入住管理、酒店运营等完整功能。

## 技术栈

### 后端
- Java 17
- Spring Boot 3.x
- Maven
- MySQL 8
- MyBatis-Plus
- Spring Security
- JWT

### 前端
- Vue 3
- Vite
- JavaScript
- Vue Router
- Pinia
- Axios
- Element Plus

## 项目目录

```
zhisu-hotel-system/
├── backend/           # 后端项目
├── frontend/          # 前端项目
├── database/          # 数据库脚本
├── docs/              # 文档
├── test-data/         # 测试数据
├── deploy/            # 部署相关
├── .gitignore
├── README.md
└── CONTRIBUTING.md
```

## 模块分工

| 成员 | 负责模块 |
|------|----------|
| 成员1 | common, config, auth, user, 项目骨架, 登录注册, 权限管理, 用户中心, 信用与禁订, 统一异常, 统一响应, 最终集成 |
| 成员2 | roomtype, inventory, 房型, 房型图片, 房型设施, 每日库存, 日期查询, 跨日期可售库存, 库存日志 |
| 成员3 | booking, payment, 创建订单, 订单查询, 模拟支付, 取消, 退款, 支付超时, 订单状态, 调用库存锁定和释放服务 |
| 成员4 | room, frontdesk, 实体房间, 二维码令牌, 分房, 入住, 退房, 清洁, 维修, 楼层房态地图, 房态日志 |
| 成员5 | pricing, content, dashboard, systemconfig, 基础价格, 节假日系数, 每日价格, 酒店介绍, 轮播图, 公告, 入住须知, 运营看板, 系统参数 |

## 环境要求

- JDK 17+
- Node.js 18+
- MySQL 8.0+
- Maven 3.8+

## 数据库初始化

1. 创建数据库 `zhisu_hotel`
2. 按顺序执行 database/ 目录下的 SQL 脚本

## 后端启动方法

```bash
cd backend
mvn clean install
mvn spring-boot:run
```

## 前端启动方法

```bash
cd frontend
npm install
npm run dev
```

## 环境变量配置

### 后端
复制 `backend/src/main/resources/application-dev.example.yml` 为 `application-dev.yml` 并配置相关参数

### 前端
复制 `frontend/.env.example` 为 `.env.development.local` 并配置相关参数

## 默认端口

- 后端：8080
- 前端：5173

## 健康检查

GET http://localhost:8080/api/health

## Git分支规则

详见 [CONTRIBUTING.md](CONTRIBUTING.md)
