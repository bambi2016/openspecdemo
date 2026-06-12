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
