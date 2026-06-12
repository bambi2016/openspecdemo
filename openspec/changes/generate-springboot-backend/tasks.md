## 1. 工程骨架

- [x] 1.1 创建 Maven `pom.xml`，包含 Spring Boot 3.2.x、Java 17、MyBatis、MySQL Driver、JWT、Validation、Lombok、JUnit5、Mockito、MockMvc、JaCoCo 和测试数据库依赖。
- [x] 1.2 创建 `src/main/java/com/enterprise/Application.java`，并添加 `@SpringBootApplication`。
- [x] 1.3 创建 `src/main/resources/application.yml`，包含 datasource、MyBatis mapper 路径、JWT、日志和服务端口占位配置。
- [x] 1.4 创建 `com.enterprise.common`、`com.enterprise.arch`、`com.enterprise.user`、`com.enterprise.permission` 包目录。
- [x] 1.5 配置 JaCoCo report 和 check，使 `mvn verify` 强制校验整体和核心业务包行覆盖率不低于 80%。

## 2. common 公共基础

- [x] 2.1 实现 `Result<T>`，提供成功和失败的 builder 或静态工厂方法。
- [x] 2.2 实现 `PageResult<T>`，包含 `records`、`total`、`pageNum`、`pageSize`、`pages`。
- [x] 2.3 实现全局 `ErrorCode` 契约和 `10001` 到 `10005` 的公共错误码。
- [x] 2.4 实现 `BizException` 和公共异常辅助能力。
- [x] 2.5 实现 `@Anonymous`、`@Perm`、`@Password`、`@Sensitive` 注解。
- [x] 2.6 实现 BCrypt 密码工具，以及手机号、邮箱、密码、token 的敏感信息脱敏工具。
- [x] 2.7 增加 common 单元测试，覆盖 Result 构建、错误码映射、密码加密/匹配和脱敏行为。

## 3. arch 架构基础

- [x] 3.1 实现 JWT 工具，支持 token 生成、解析、过期处理和 claims 提取。
- [x] 3.2 实现登录用户上下文，支持 ThreadLocal set、get、clear。
- [x] 3.3 实现认证拦截器，跳过 `@Anonymous`，校验 `Authorization: Bearer {token}`，并写入当前用户上下文。
- [x] 3.4 实现权限模板或权限检查器，加载用户权限并判断 `@Perm` 权限要求。
- [x] 3.5 实现权限拦截器，对未登录或无权限请求返回公共错误码。
- [x] 3.6 实现全局异常处理器，覆盖 `BizException`、参数校验异常、认证异常和未知异常。
- [x] 3.7 实现请求日志 AOP，对密码、token、手机号、邮箱进行脱敏。
- [x] 3.8 注册拦截器和 Web MVC 配置。
- [x] 3.9 增加 arch 测试，覆盖 JWT 成功/失败、上下文清理、匿名访问、缺失 token、无效 token、权限通过、权限拒绝、异常映射和日志脱敏。

## 4. user 用户模块

- [x] 4.1 实现 `SysUser` 实体，包含通用字段和 `sys_user` 表字段映射。
- [x] 4.2 实现 `RegisterDTO`、`LoginDTO`、修改密码 DTO、`UserVO`、`LoginVO` 和登录类型枚举，并添加 Bean Validation 注解。
- [x] 4.3 实现 `UserErrorCode`，包含 `20001` 到 `20004`。
- [x] 4.4 实现 `SysUserMapper` 和 `src/main/resources/mybatis/user/SysUserMapper.xml`，覆盖用户名查询、插入、密码更新、最后登录时间更新、逻辑删除和状态过滤。
- [x] 4.5 实现 `LoginStrategy`、`PasswordLoginStrategy` 和 `LoginContext`。
- [x] 4.6 实现用户 Service，包含注册、登录、当前用户查询和修改密码。
- [x] 4.7 实现用户 Controller 接口：`POST /api/user/register`、`POST /api/user/login`、`GET /api/user/me`、`PUT /api/user/password`。
- [x] 4.8 增加用户 Service 和策略测试，覆盖注册成功、用户名重复、密码加密、登录成功、用户名不存在、密码错误、用户禁用、当前用户查询和修改密码分支。
- [x] 4.9 增加用户 MockMvc 测试，覆盖匿名登录/注册、参数校验失败、已登录获取当前用户、未登录访问、修改密码成功和旧密码错误。
- [x] 4.10 增加用户 Mapper 测试，覆盖用户名唯一性查询、正常用户过滤、逻辑删除过滤和密码更新 SQL。

## 5. permission 权限模块

- [x] 5.1 实现 `SysRole`、`SysPermission`、`SysUserRole`、`SysRolePermission` 实体。
- [x] 5.2 实现角色创建、角色权限绑定、用户角色绑定、角色响应、权限响应相关 DTO 和 VO。
- [x] 5.3 实现 `PermissionErrorCode`，包含 `30001`、`30002`、`30003`。
- [x] 5.4 实现权限相关 Mapper 和 XML，覆盖角色、权限、用户角色关系、角色权限关系、当前用户权限查询、唯一性检查、逻辑删除和状态过滤。
- [x] 5.5 实现权限 Service，包含角色创建、角色权限绑定、用户角色绑定和当前用户权限标识查询。
- [x] 5.6 实现 `/api/permission` 下的权限 Controller，并添加合适的 `@Perm` 注解。
- [x] 5.7 将权限 Service 接入 arch 权限检查器，并避免直接 import user 模块实体。
- [x] 5.8 增加权限 Service 测试，覆盖角色创建成功、角色编码重复、权限编码重复、权限编码非法、绑定成功、角色不存在、权限不存在、用户不存在、重复绑定、替换绑定和失败不写入部分关联数据。
- [x] 5.9 增加权限查询和鉴权测试，覆盖权限去重、禁用角色过滤、禁用权限过滤、逻辑删除过滤、未登录、无效 token、权限通过、权限拒绝和接口未声明 `@Perm`。
- [x] 5.10 增加权限 Mapper 测试，覆盖核心关联查询、唯一性检查、状态过滤和逻辑删除过滤。

## 6. 数据库和资源

- [x] 6.1 创建 `src/main/resources/sql/init.sql`，包含 `sys_user`、`sys_role`、`sys_permission`、`sys_user_role`、`sys_role_permission`。
- [x] 6.2 为用户名、角色编码、权限编码和关联关系组合添加索引与唯一约束。
- [x] 6.3 添加默认管理员角色和核心权限初始化数据。
- [x] 6.4 确保所有业务表都包含 `id`、`create_time`、`update_time`、`deleted`、`status`。

## 7. 验证

- [x] 7.1 执行 `mvn test`，修复编译错误或测试失败。
- [x] 7.2 执行 `mvn verify`，确认生成 JaCoCo HTML/XML 报告。
- [x] 7.3 确认项目整体行覆盖率不低于 80%。
- [x] 7.4 确认 `com.enterprise.user` 行覆盖率不低于 80%。
- [x] 7.5 确认 `com.enterprise.permission` 行覆盖率不低于 80%。
- [x] 7.6 执行 `openspec validate --all --strict`，修复所有 OpenSpec 校验错误。
- [x] 7.7 人工检查生成代码，确认不存在物理删除、明文密码日志、遗漏 `@Anonymous`、遗漏 `@Perm`、业务模块直接 import 对方实体等问题。
