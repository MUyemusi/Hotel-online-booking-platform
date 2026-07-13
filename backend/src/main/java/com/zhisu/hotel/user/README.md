# User Module - 用户模块
## 负责人：成员1

## 负责范围
- 用户CRUD
- 用户中心
- 用户角色
- 信用管理
- 禁订管理

## 依赖的其他模块
- common

## 对外提供的Service
- CreditService
  - checkBookingPermission
  - recordUserCancellation
  - resetContinuousCancellation

## 禁止直接访问的其他模块Mapper
- 所有其他模块的Mapper
