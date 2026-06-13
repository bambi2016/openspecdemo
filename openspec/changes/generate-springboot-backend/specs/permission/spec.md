## ADDED Requirements

### Requirement: 生成 permission 模块实现
生成的后端项目 SHALL 实现 permission 基线规格中描述的角色、权限、绑定关系、当前权限查询、鉴权、Mapper XML 和初始化 SQL。

#### Scenario: 生成权限模块代码
- **WHEN** 后端项目生成完成
- **THEN** `com.enterprise.permission` 包含 Controller、DTO、VO、Entity、Mapper、Service、Service 实现、权限错误码类和鉴权集成
- **AND** 角色创建、角色权限绑定、用户角色绑定、当前权限查询和 `@Perm` 鉴权都必须实现

### Requirement: 生成 permission 模块测试
生成的后端项目 SHALL 包含权限模块测试，覆盖成功路径、失败路径、过滤规则和鉴权分支。

#### Scenario: 验证权限模块测试
- **WHEN** 执行 `mvn verify`
- **THEN** 测试覆盖角色创建成功、角色编码重复、权限编码重复、权限编码格式错误、角色权限绑定成功、角色不存在、权限不存在、用户角色绑定成功、用户不存在、权限去重、禁用角色过滤、禁用权限过滤、未登录、无效 token、权限通过和权限拒绝
- **AND** `com.enterprise.permission` 行覆盖率至少达到 80%

### Requirement: 当前用户权限查询使用 30 分钟 Redis 缓存
生成的 permission 模块 SHALL 对当前用户权限标识查询使用用户维度 Redis 读缓存，同一用户同一权限查询在 30 分钟内不得重复访问数据库。

#### Scenario: 当前用户权限查询缓存未命中
- **WHEN** 已登录用户第一次查询当前用户权限标识
- **THEN** 系统查询数据库获取该用户有效角色和有效权限
- **AND** 系统将权限标识集合写入 Redis，缓存键为 `cache:permission:codes:user:{userId}`，TTL 为 30 分钟

#### Scenario: 当前用户权限查询缓存命中
- **GIVEN** 用户已经在 30 分钟内成功查询过当前用户权限标识，并且 Redis 中存在 `cache:permission:codes:user:{userId}`
- **WHEN** 同一用户再次查询当前用户权限标识
- **THEN** 系统 SHALL 直接返回 Redis 缓存中的权限标识集合
- **AND** 系统 SHALL 不再次调用权限 Mapper 查询数据库

#### Scenario: 权限绑定变更后失效权限缓存
- **WHEN** 用户角色绑定、角色权限绑定、角色状态、权限状态或权限逻辑删除发生变化
- **THEN** 系统 SHALL 删除受影响用户的权限缓存
- **AND** 受影响用户下一次查询当前权限时必须重新查询数据库并刷新 Redis 缓存

#### Scenario: 当前用户权限缓存测试
- **WHEN** 执行 `mvn verify`
- **THEN** 权限模块测试 SHALL 覆盖权限查询缓存未命中、缓存命中、30 分钟 TTL、绑定变更后的缓存失效和 Redis 异常降级分支
- **AND** 测试 SHALL 断言缓存命中时权限 Mapper 不被重复调用
