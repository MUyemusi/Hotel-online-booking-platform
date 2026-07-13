# 协作规范

## 分支管理

### 主要分支
- `main`: 主分支，保持稳定
- `develop`: 开发分支，用于合并各功能分支

### 功能分支
命名格式：`feature/{member}-{module}`

- `feature/member1-auth-user-credit`
- `feature/member2-roomtype-inventory`
- `feature/member3-booking-payment`
- `feature/member4-room-frontdesk`
- `feature/member5-pricing-content-dashboard`

## 提交信息格式

- `feat: ` 新功能
- `fix: ` 修复bug
- `docs: ` 文档更新
- `refactor: ` 重构
- `test: ` 测试
- `chore: ` 构建/工具相关

## 协作约束

1. 禁止直接提交到 `main` 分支
2. 功能分支合并到 `develop` 前需同步最新代码
3. 公共配置由项目负责人审核
4. 不提交密码、密钥、数据库账号或个人配置
5. 不提交 target、node_modules、dist、IDE 配置和日志
