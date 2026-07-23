[OPEN] Debug Session: run-full-project

## 目标
- 修复当前阻塞“后端 + 前端完整启动与成员4流程跑通”的报错
- 以可复现的运行时证据为准：构建日志、启动日志、HTTP 调用结果

## 复现路径（待补充/确认）
- 后端：backend 下打包并启动，访问 /api/health
- 前端：frontend 下构建并启动，页面访问成员4工作台与房态地图

## 假设（可证伪）
H1：后端启动失败由配置/环境变量（数据源、端口、Profile）不一致或缺失导致。
H2：后端启动失败由数据库 Schema 与实体/Mapper 不一致导致（启动校验器或运行期 SQL 报错）。
H3：后端启动失败由当前工作区存在未提交/未跟踪文件导致类冲突或 Bean 冲突（重复 Controller/路径映射）。
H4：前端构建/启动失败由依赖锁文件变更、路由 meta 契约或接口路径不一致导致。
H5：成员4流程无法跑通并非“报错”，而是权限/角色限制或状态机前置条件导致（401/403/业务异常）。

## 证据记录
- 构建：记录 mvn / npm 的完整输出摘要（关键错误行、错误码、堆栈关键段）
- 启动：记录 Spring Boot profile、Hikari、Tomcat、Started、health 响应体
- 前端：记录浏览器 Network/Console 中的关键请求与响应码（不记录二维码明文）

## 当前结论
- 后端已可打包并成功启动（dev profile），/api/health 返回 code=SUCCESS。
- 前端构建阻塞点已定位为 Node 不在 PATH（npm 脚本内部调用 node 失败）。通过在会话级 PATH 追加 `C:\Program Files\nodejs` 后，`npm run build` 成功，dev server 可启动。
- 成员4核心闭环（生成二维码 → 前台核验 → 分房确认入住 → 确认退房 → 清洁完成）已在真实 HTTP 下跑通；过程中未输出二维码明文。

## 关键证据摘要
- 后端打包：`mvn -DskipTests package` -> BUILD SUCCESS
- 后端启动日志关键行：
  - active profile: dev
  - Tomcat started on port 8080
  - Started ZhisuHotelApplication
  - HikariPool Start completed
  - Database schema validation passed successfully
- 健康检查：
  - GET http://127.0.0.1:8080/api/health -> code=SUCCESS
- 成员4闭环（以新注册 USER + MOCK 支付生成可入住订单）：
  - 订单：PENDING_CHECK_IN + PAID
  - 生成二维码：tokenStatus=ACTIVE（不展示 qrContent）
  - 前台核验：valid=true
  - 分房入住：orderStatus=CHECKED_IN，2 间房分配成功（示例房号：306, 307）
  - 退房：orderStatus=COMPLETED，2 间房退房成功
  - 清洁完成：房间状态最终回到 AVAILABLE
