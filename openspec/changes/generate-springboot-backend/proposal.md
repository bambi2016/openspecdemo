## Why

当前仓库已经具备可解析的 OpenSpec 主规格，但还没有任何可运行的 Spring Boot 后端代码。需要创建一个标准 OpenSpec change，把 arch、common、user、permission 四个基线能力落地为可编译、可测试、带覆盖率门禁的 Java 后端项目。

## What Changes

- **arch:** 生成 Maven + Spring Boot 3.2.x 工程骨架、JWT 认证基础设施、权限校验模板、全局异常处理、请求日志 AOP、JaCoCo 覆盖率门禁。
- **common:** 生成统一返回、分页模型、错误码接口、公共注解、业务异常、密码加密和敏感字段脱敏工具。
- **user:** 生成用户注册、账号密码登录、当前用户信息、修改密码、用户错误码、用户 Mapper XML 和用户模块测试。
- **permission:** 生成角色、权限、用户角色绑定、角色权限绑定、当前用户权限查询、`@Perm` 鉴权、初始化 SQL 和权限模块测试。
- 生成 `pom.xml`、`application.yml`、`src/main/java`、`src/main/resources`、`src/test/java` 和 `src/main/resources/sql/init.sql`。
- 配置 JUnit5、Mockito、MockMvc、MyBatis XML、MySQL Driver、JWT、Validation、Lombok、JaCoCo，并保证 `mvn verify` 可执行。
- 非目标：不生成前端页面、不实现短信/三方登录、不引入多租户、不实现完整后台管理 UI、不接入外部真实短信或 OAuth 服务。

## Capabilities

### New Capabilities
- None.

### Modified Capabilities
- `arch`: 将现有架构约束落地为真实工程骨架、安全基础设施、异常处理、测试分层和覆盖率门禁。
- `common`: 将现有公共契约落地为可复用 Java 类型、注解、错误码、异常和工具类。
- `user`: 将现有用户契约落地为 REST 接口、DTO、VO、Entity、Mapper、Service、登录策略和测试。
- `permission`: 将现有权限契约落地为 REST 接口、Entity、Mapper、Service、鉴权模板、初始化数据和测试。

## Impact

- 新增完整 Java 后端工程目录和 Maven 构建文件。
- 新增 `com.enterprise.arch`、`com.enterprise.common`、`com.enterprise.user`、`com.enterprise.permission` 包。
- 新增 MyBatis XML、初始化 SQL、JUnit5/Mockito/MockMvc 测试和 JaCoCo 配置。
- 构建命令以 `mvn verify` 作为主要验收入口，覆盖率低于 80% 时构建失败。
