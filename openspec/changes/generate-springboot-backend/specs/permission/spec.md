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
