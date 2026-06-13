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

### Requirement: 生成 Redis 查询缓存基础设施
生成的后端项目 SHALL 包含 Redis 查询缓存基础设施，用于支持同一用户同一只读查询在 30 分钟内优先读取缓存。

#### Scenario: 生成 Redis 缓存配置
- **WHEN** 后端项目生成完成
- **THEN** `pom.xml` 包含 Spring Data Redis 相关依赖
- **AND** `application.yml` 包含 Redis host、port、password、database 和 timeout 占位配置
- **AND** `com.enterprise.arch` 包含 Redis 序列化配置、缓存键工具和查询缓存组件

#### Scenario: Redis 缓存命中和未命中
- **WHEN** 同一用户第一次执行受缓存保护的只读查询
- **THEN** 系统查询数据库并将查询结果写入 Redis，TTL 为 30 分钟
- **WHEN** 同一用户在 30 分钟内再次执行相同查询
- **THEN** 系统 SHALL 直接返回 Redis 缓存结果，并且不再次查询数据库

#### Scenario: Redis 不可用时降级
- **WHEN** Redis 读取或写入失败
- **THEN** 系统 SHALL 记录脱敏日志并降级查询数据库
- **AND** 只读查询接口不得因为缓存故障直接失败

#### Scenario: 缓存键隔离
- **WHEN** 不同用户执行相同业务查询
- **THEN** Redis 缓存键 SHALL 包含用户标识，避免一个用户读取到另一个用户的缓存数据
- **WHEN** 同一用户执行不同查询参数的查询
- **THEN** Redis 缓存键 SHALL 包含查询参数摘要，避免不同查询条件复用错误结果
