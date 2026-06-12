## ADDED Requirements

### Requirement: 生成 common 公共基础
生成的后端项目 SHALL 包含所有模块复用的 common Java 代码，避免各业务模块重复实现响应、错误和工具逻辑。

#### Scenario: 生成公共基础类
- **WHEN** 后端项目生成完成
- **THEN** `com.enterprise.common` 包含 `Result<T>`、`PageResult<T>`、全局错误码、`ErrorCode`、`BizException`、公共注解、密码工具和脱敏工具
- **AND** user 和 permission 模块必须依赖这些 common 公共能力

### Requirement: 生成 common 行为测试
生成的后端项目 SHALL 为 common 中包含行为逻辑的工具类生成测试。

#### Scenario: 测试公共工具
- **WHEN** 执行生成后的测试
- **THEN** 密码加密和密码匹配逻辑被覆盖
- **AND** 手机号、邮箱、密码和 token 的脱敏行为被覆盖
