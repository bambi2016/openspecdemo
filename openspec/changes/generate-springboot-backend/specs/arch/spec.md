## ADDED Requirements

### Requirement: 生成后端构建门禁
生成的后端项目 SHALL 提供 Maven 验证门禁，在实现完成前必须完成编译、测试和覆盖率检查。

#### Scenario: 验证生成后的后端项目
- **WHEN** 在生成后的项目根目录执行 `mvn verify`
- **THEN** Maven 编译 main 和 test 源码
- **AND** JUnit5、Mockito、MockMvc、MyBatis Mapper 测试和 JaCoCo 检查自动执行
- **AND** 覆盖率低于配置阈值时构建失败

### Requirement: 生成架构基础设施
生成的后端项目 SHALL 包含 arch 基线规格要求的真实架构代码，而不是空类或占位代码。

#### Scenario: 生成架构类
- **WHEN** 后端项目生成完成
- **THEN** `com.enterprise.arch` 包含 JWT 工具、认证上下文、认证拦截器、权限拦截器或权限检查器、全局异常处理器、请求日志 AOP 和 Web MVC 配置
- **AND** 这些类必须被测试覆盖，而不是只声明占位类
