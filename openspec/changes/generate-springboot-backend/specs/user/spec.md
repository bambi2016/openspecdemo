## ADDED Requirements

### Requirement: 生成 user 模块实现
生成的后端项目 SHALL 实现 user 基线规格中描述的用户接口、服务层、策略层、Mapper XML 和测试。

#### Scenario: 生成用户模块代码
- **WHEN** 后端项目生成完成
- **THEN** `com.enterprise.user` 包含 Controller、DTO、VO、Entity、Mapper、Service、Service 实现、登录策略、登录上下文和用户错误码类
- **AND** `/api/user/register`、`/api/user/login`、`/api/user/me`、`/api/user/password` 按基线规格实现

### Requirement: 生成 user 模块测试
生成的后端项目 SHALL 包含用户模块测试，覆盖成功路径、失败路径、边界条件和安全敏感行为。

#### Scenario: 验证用户模块测试
- **WHEN** 执行 `mvn verify`
- **THEN** 测试覆盖注册成功、用户名重复、密码加密、登录成功、用户名不存在、密码错误、用户禁用、当前用户查询、未登录访问、修改密码成功和旧密码错误
- **AND** `com.enterprise.user` 行覆盖率至少达到 80%

### Requirement: 当前用户查询使用 30 分钟 Redis 缓存
生成的 user 模块 SHALL 对当前用户信息查询使用用户维度 Redis 读缓存，同一用户同一查询在 30 分钟内不得重复访问数据库。

#### Scenario: 当前用户查询缓存未命中
- **WHEN** 已登录用户第一次请求 `GET /api/user/me`
- **THEN** 系统查询数据库获取当前用户信息
- **AND** 系统将当前用户信息写入 Redis，缓存键为 `cache:user:me:user:{userId}`，TTL 为 30 分钟

#### Scenario: 当前用户查询缓存命中
- **GIVEN** 用户已经在 30 分钟内成功查询过 `GET /api/user/me`，并且 Redis 中存在 `cache:user:me:user:{userId}`
- **WHEN** 同一用户再次请求 `GET /api/user/me`
- **THEN** 系统 SHALL 直接返回 Redis 缓存中的当前用户信息
- **AND** 系统 SHALL 不再次调用用户 Mapper 查询数据库

#### Scenario: 用户变更后失效用户缓存
- **WHEN** 用户成功修改密码、用户状态被更新或用户被逻辑删除
- **THEN** 系统 SHALL 删除该用户相关的当前用户信息缓存
- **AND** 该用户下一次请求 `GET /api/user/me` 时必须重新查询数据库并刷新 Redis 缓存

#### Scenario: 当前用户查询缓存测试
- **WHEN** 执行 `mvn verify`
- **THEN** 用户模块测试 SHALL 覆盖当前用户查询缓存未命中、缓存命中、30 分钟 TTL、缓存失效和 Redis 异常降级分支
- **AND** 测试 SHALL 断言缓存命中时用户 Mapper 不被重复调用
