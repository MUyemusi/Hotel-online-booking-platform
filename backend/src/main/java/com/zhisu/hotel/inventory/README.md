# Inventory Module - 库存模块
## 负责人：成员2

## 负责范围
- 每日库存管理
- 日期查询
- 跨日期可售库存
- 库存日志

## 依赖的其他模块
- common, roomtype

## 对外提供的Service
- InventoryService
  - checkInventory
  - lockInventory
  - confirmInventory
  - releaseInventory

## 禁止直接访问的其他模块Mapper
- 所有其他模块的Mapper
