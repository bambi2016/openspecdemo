# OpenSpec 系统架构顶层约束 V1.0

## Purpose
定义后端项目生成时必须遵守的技术栈、工程分层、跨模块边界、安全基础设施和测试约束。该规格面向 Java 17 + Spring Boot 3.2.x + MyBatis XML + MySQL 8.0 的单体后端项目，生成器应以此作为所有业务模块的基础架构契约。

## Requirements

### Requirement: Maven Spring Boot 工程骨架
系统 SHALL 生成一个可直接编译启动的 Maven + Spring Boot 3.2.x 工程骨架。

#### Scenario: 生成基础工程文件
- **WHEN** 根据本 OpenSpec 项目生成后端工程
- **THEN** 根目录包含 `pom.xml`
- **AND** `pom.xml` 使用 Java 17、Spring Boot 3.2.x、JUnit5、Mockito、MyBatis、MySQL Driver、JWT、Validation、Lombok
- **AND** 根目录包含 `src/main/java`、`src/main/resources`、`src/test/java`

#### Scenario: 生成应用入口
- **WHEN** 生成 Java 源码
- **THEN** 系统在 `src/main/java/com/enterprise/Application.java` 生成 Spring Boot 启动类
- **AND** 启动类使用 `@SpringBootApplication`

### Requirement: 统一包结构分层
系统 SHALL 以 `com.enterprise` 为基础包，并按 arch、common、user、permission 四个能力包生成代码。

#### Scenario: 生成公共包
- **WHEN** 生成架构公共代码
- **THEN** `com.enterprise.arch` 包包含拦截器、AOP、全局异常、认证上下文和安全模板类
- **AND** `com.enterprise.common` 包包含统一返回、分页、错误码、注解、工具类和通用模型

#### Scenario: 生成业务模块包
- **WHEN** 生成业务模块代码
- **THEN** `com.enterprise.user` 和 `com.enterprise.permission` 均包含 `controller`、`dto`、`vo`、`entity`、`mapper`、`service`、`service.impl`
- **AND** 不得使用 `service.interface` 作为包名

### Requirement: MyBatis XML 持久层
系统 SHALL 使用 MyBatis Mapper 接口配合 XML 映射文件访问 MySQL 数据库。

#### Scenario: 生成 Mapper 资源
- **WHEN** 生成任一实体的持久层
- **THEN** 系统生成对应 Mapper 接口到模块 `mapper` 包
- **AND** 系统生成对应 XML 到 `src/main/resources/mybatis/<module>/*Mapper.xml`
- **AND** 复杂 SQL 必须写在 XML 中，简单 CRUD 也应优先保持 XML 风格一致

#### Scenario: 生成初始化 SQL
- **WHEN** 生成数据库脚本
- **THEN** 系统生成 `src/main/resources/sql/init.sql`
- **AND** SQL 包含 `sys_user`、`sys_role`、`sys_permission`、`sys_user_role`、`sys_role_permission`
- **AND** 所有业务表包含 `id`、`create_time`、`update_time`、`deleted`、`status`

### Requirement: JWT 认证基础设施
系统 SHALL 使用 JWT 完成登录态表达，并通过拦截器处理受保护接口的认证。

#### Scenario: 请求携带有效 Token
- **WHEN** 客户端访问非 `@Anonymous` 接口且请求头包含 `Authorization: Bearer {token}`
- **THEN** 认证拦截器解析 JWT
- **AND** 将登录用户上下文写入 ThreadLocal
- **AND** Controller 和 Service 可通过上下文工具获取当前用户

#### Scenario: 请求未携带 Token
- **WHEN** 客户端访问非 `@Anonymous` 接口且未携带 Authorization 请求头
- **THEN** 系统返回错误码 `10001`
- **AND** 不进入业务方法

### Requirement: 权限校验模板方法
系统 SHALL 使用模板方法模式封装认证解析、用户加载、权限校验和失败处理流程。

#### Scenario: 接口声明权限标识
- **WHEN** Controller 方法标记 `@Perm("role:create")`
- **THEN** 权限拦截器调用鉴权模板执行公共认证流程
- **AND** 子类或扩展点只负责具体权限匹配规则

#### Scenario: 用户缺少权限
- **WHEN** 当前用户已登录但不拥有接口声明的权限标识
- **THEN** 系统返回错误码 `10003`
- **AND** 不进入业务方法

### Requirement: 统一异常和响应出口
系统 SHALL 通过 `BizException`、`GlobalExceptionHandler` 和 `Result<T>` 统一处理业务异常、参数异常和系统异常。

#### Scenario: 业务异常
- **WHEN** Service 抛出 `BizException`
- **THEN** 全局异常处理器返回 `Result.fail(code, msg)`
- **AND** HTTP 响应体包含 `code`、`msg`、`data`、`timestamp`

#### Scenario: 参数校验异常
- **WHEN** Controller 入参违反 Bean Validation 规则
- **THEN** 系统返回错误码 `10004`
- **AND** `msg` 包含可读的参数错误提示

### Requirement: 敏感信息保护
系统 SHALL 禁止密码、手机号、邮箱和 Token 明文进入日志或非授权响应。

#### Scenario: 记录请求日志
- **WHEN** 请求日志 AOP 记录入参和出参
- **THEN** 标记 `@Password` 或 `@Sensitive` 的字段必须脱敏
- **AND** Authorization 请求头不得完整打印

#### Scenario: 返回用户信息
- **WHEN** 接口返回用户资料
- **THEN** 密码字段不得出现在 VO 中
- **AND** 手机号和邮箱按脱敏策略输出

### Requirement: 设计模式落地约束
系统 SHALL 在指定业务场景中生成明确的设计模式结构，而不是只写注释说明。

#### Scenario: 登录策略模式
- **WHEN** 生成用户登录能力
- **THEN** 系统生成 `LoginStrategy`、`PasswordLoginStrategy`、`LoginContext`
- **AND** Controller 不直接依赖具体登录策略

#### Scenario: Result 建造者模式
- **WHEN** 生成统一返回对象
- **THEN** `Result<T>` 提供 builder 或静态建造方法
- **AND** Controller 不手写重复响应结构

### Requirement: 业务场景测试映射
系统 SHALL 为每个 OpenSpec `#### Scenario` 生成至少一个对应的自动化测试用例。

#### Scenario: 生成场景测试用例
- **WHEN** 任一规格文件中存在 `#### Scenario`
- **THEN** 生成器必须生成对应的单元测试、Web 层测试或集成测试
- **AND** 测试方法名应体现业务场景名称或行为意图
- **AND** 不得只生成空测试、无断言测试或仅验证 Spring Context 启动的占位测试

#### Scenario: 场景测试追踪
- **WHEN** 生成测试类
- **THEN** 每个测试类应通过方法命名或注释标明覆盖的 Requirement 或 Scenario
- **AND** 核心业务场景不得遗漏测试映射

### Requirement: 分层测试覆盖
系统 SHALL 为 Controller、Service、Mapper、Strategy、Interceptor 和 AOP 核心逻辑生成分层测试。

#### Scenario: 生成 Web 层测试
- **WHEN** 生成 Controller
- **THEN** 系统使用 MockMvc 覆盖成功响应、参数校验失败、鉴权失败、权限不足和业务异常响应
- **AND** 测试应断言 HTTP 状态、`Result.code`、`Result.msg` 和关键 `data` 字段

#### Scenario: 生成 Service 层测试
- **WHEN** 生成核心 Service、LoginStrategy 或鉴权模板
- **THEN** 系统使用 JUnit5 + Mockito 覆盖成功路径、失败路径、边界条件和异常分支
- **AND** 测试应校验依赖调用、异常错误码和关键状态变更

#### Scenario: 生成 Mapper 层测试
- **WHEN** 生成 MyBatis Mapper 和 XML
- **THEN** 系统至少覆盖核心查询 SQL、唯一性查询、逻辑删除过滤和状态过滤
- **AND** Mapper 测试可使用内存数据库或测试容器，但必须能在 `mvn verify` 中自动执行

### Requirement: 测试覆盖率门禁
系统 SHALL 使用 JaCoCo 对自动化测试覆盖率进行统计，并在构建阶段执行覆盖率门禁。

#### Scenario: 覆盖率达标
- **WHEN** 执行 `mvn verify`
- **THEN** JaCoCo 生成 HTML 和 XML 覆盖率报告
- **AND** 项目整体 Java 行覆盖率不得低于 80%
- **AND** `com.enterprise.user` 和 `com.enterprise.permission` 核心业务包行覆盖率不得低于 80%

#### Scenario: 覆盖率不达标
- **WHEN** 覆盖率低于门禁阈值
- **THEN** Maven 构建必须失败
- **AND** 输出可定位到未达标包或类的覆盖率报告

### Requirement: 覆盖率统计范围
系统 SHALL 明确 JaCoCo 覆盖率统计范围，避免无业务逻辑代码影响门禁，同时禁止排除核心业务逻辑。

#### Scenario: 排除无业务逻辑代码
- **WHEN** 统计覆盖率
- **THEN** 可排除 `Application`、配置类、DTO、VO、Entity、枚举常量类、自动生成的常量类
- **AND** 排除规则必须写入 Maven JaCoCo 配置

#### Scenario: 禁止排除核心逻辑
- **WHEN** 配置覆盖率排除规则
- **THEN** 不得排除 Controller、Service、Strategy、Interceptor、AOP、Mapper 接口和核心工具类
- **AND** 不得通过扩大排除范围绕过 80% 行覆盖率门禁

## Generation Notes
- 生成器应优先根据 `openspec/openspec-gen.yaml` 的模块映射输出文件。
- 业务模块之间禁止直接引用对方实体；跨模块只通过 VO、DTO、Service 契约或上下文工具交互。
- 删除操作必须是逻辑删除，不生成物理 `DELETE` 业务代码。
