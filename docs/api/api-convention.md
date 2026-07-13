# API 接口规范

## 统一返回格式

```json
{
  "code": "SUCCESS",
  "message": "操作成功",
  "data": {},
  "timestamp": "2026-07-13T12:00:00"
}
```

## 分页格式

```json
{
  "records": [],
  "total": 0,
  "current": 1,
  "size": 10,
  "pages": 0
}
```

## 日期格式

- 日期：yyyy-MM-dd
- 时间：yyyy-MM-dd HH:mm:ss

## 金额格式

- 单位：分（Integer / Long）
- 或：元（BigDecimal）

## 状态枚举

### 订单状态
- PENDING_PAYMENT - 待支付
- PENDING_CHECK_IN - 待入住
- CHECKED_IN - 已入住
- COMPLETED - 已完成
- CANCELLED - 已取消
- CLOSED - 已关闭

### 支付状态
- UNPAID - 未支付
- PAID - 已支付
- FAILED - 支付失败
- REFUNDED - 已退款

### 房间状态
- AVAILABLE - 可订
- RESERVED - 已预订
- OCCUPIED - 已入住
- DIRTY - 待清洁
- MAINTENANCE - 维修中
- DISABLED - 不可用

### 用户状态
- ENABLED - 正常
- DISABLED - 禁用

## HTTP 状态码

- 200 - 成功
- 400 - 参数错误
- 401 - 未认证
- 403 - 无权限
- 404 - 不存在
- 500 - 服务器错误

## 业务错误码

- SUCCESS - 成功
- BUSINESS_ERROR - 业务错误
- VALIDATION_ERROR - 参数错误
- UNAUTHORIZED - 未认证
- FORBIDDEN - 无权限
- NOT_FOUND - 不存在
- INTERNAL_ERROR - 服务器错误

## 权限约定

- 使用 Spring Security + JWT
- 使用注解控制权限

## 接口命名规范

- 统一前缀：/api
- 认证：/api/auth
- 用户：/api/users
- 房型：/api/room-types
- 库存：/api/inventory
- 订单：/api/orders
- 支付：/api/payments
- 前台：/api/frontdesk
- 管理端价格：/api/admin/pricing
- 管理端内容：/api/admin/content
- 管理端看板：/api/admin/dashboard
- 管理端配置：/api/admin/config
