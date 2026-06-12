## 背景

当前仓库已经包含 `arch`、`common`、`user`、`permission` 四个 OpenSpec 基线规格，以及 `openspec/openspec-gen.yaml` 中定义的生成约定，但还没有 Java 源码目录。本次 change 的目标是把这些约束落地为一个可运行的 Java 17 + Spring Boot 3.2.x 后端项目，包含 MyBatis XML 持久层、JWT 认证、权限校验、自动化测试和 JaCoCo 覆盖率门禁。

关键约束：
- 基础包名为 `com.enterprise`。
- 公共基础能力放在 `com.enterprise.common` 和 `com.enterprise.arch`。
- 业务模块放在 `com.enterprise.user` 和 `com.enterprise.permission`。
- 业务删除必须使用逻辑删除。
- 受保护接口必须使用 JWT，权限接口通过 `@Perm` 校验。
- 每个 OpenSpec 业务场景都应有对应的有效测试路径。

## 目标 / 非目标

**目标：**
- 生成一个 Maven 工程，能够使用 Spring Boot 3.2.x 编译和启动。
- 实现统一返回、分页、错误码、异常、公共注解、密码加密和敏感字段脱敏工具。
- 实现架构基础设施：JWT 工具、登录上下文、认证拦截器、权限模板、全局异常处理、请求日志 AOP 和 Web MVC 配置。
- 实现用户注册、账号密码登录、当前用户查询、修改密码、MyBatis Mapper XML 和用户模块测试。
- 实现角色、权限、用户角色绑定、角色权限绑定、当前用户权限查询、`@Perm` 鉴权、初始化 SQL 和权限模块测试。
- 配置 JaCoCo，使 `mvn verify` 在覆盖率低于 80% 时失败。

**非目标：**
- 不生成前端页面或后台管理 UI。
- 不实现短信登录、OAuth 登录、微信登录或真实第三方集成。
- 不引入多租户模型。
- 不生成生产部署脚本、Docker 镜像或 Kubernetes 配置。
- 不实现任何业务数据物理删除。

## 技术决策

### 决策 1: 生成常规 Maven 单体工程

使用单个 Maven Spring Boot 应用，而不是 Maven 多模块工程。

原因：
- 当前 OpenSpec 基线描述的是一个按包隔离的后端服务。
- 单 Maven 模块能降低生成、测试和 JaCoCo 门禁复杂度。
- 包边界仍然可以表达 `common`、`arch`、`user`、`permission` 的架构分层。

备选方案：Maven 多模块工程。它适合后续独立部署或强模块边界，但当前阶段会增加不必要的构建复杂度。

### 决策 2: 按基础能力优先的顺序生成

生成顺序使用 `common`、`arch`、`user`、`permission`，与 `openspec/openspec-gen.yaml` 保持一致。

原因：
- `arch` 依赖 common 中的错误码、响应结构和异常类型。
- `user` 和 `permission` 都依赖 common 注解、异常和 arch 认证上下文。
- 这个顺序可以避免实现时出现循环依赖。

### 决策 3: 以 MyBatis XML 作为持久层边界

Mapper 接口放在各模块的 `mapper` 包下，SQL 放在 `src/main/resources/mybatis/<module>/*Mapper.xml`。

原因：
- 基线规格明确要求使用 MyBatis XML。
- 逻辑删除过滤、状态过滤、权限关联查询和唯一性查询写在 XML 中更容易审查。

备选方案：MyBatis 注解 SQL。基线允许简单 CRUD 使用注解，但统一使用 XML 更适合代码生成和后续维护。

### 决策 4: 将策略模式和模板方法落成真实类

用户登录生成 `LoginStrategy`、`PasswordLoginStrategy`、`LoginContext`。权限校验生成抽象鉴权模板或权限检查器，用于封装 token 解析、用户加载、权限匹配和失败处理。

原因：
- 规格要求这些设计模式必须体现为真实代码结构，而不是注释。
- 后续扩展短信登录、三方登录或自定义权限规则时，可以新增实现类而不是改写 Controller。

### 决策 5: 测试生成必须能追踪到业务场景

每个核心业务场景至少映射一个测试方法。Controller 使用 MockMvc；Service、Strategy 和鉴权逻辑使用 JUnit5 + Mockito；Mapper 测试覆盖核心 SQL、逻辑删除过滤和唯一性查询。

原因：
- 用户明确要求核心业务尽量覆盖，并且行覆盖率不低于 80%。
- 场景到测试的映射可以让测试覆盖和 OpenSpec 契约之间有清晰追踪关系。

### 决策 6: JaCoCo 在 `mvn verify` 阶段强制执行

生成的 `pom.xml` 需要把 JaCoCo report 和 check 绑定到 `mvn verify`。

原因：
- 覆盖率应该是构建门禁，而不是人工查看报告。
- 排除范围只允许覆盖 DTO、VO、Entity、常量、配置等无业务逻辑代码，不能排除核心业务逻辑。

## 风险 / 权衡

- 覆盖率门禁可能在早期生成阶段失败 -> 同步生成测试，并在任务完成前运行 `mvn verify`。
- Mapper 测试比纯单元测试更重 -> 使用轻量测试数据库或独立测试 profile，并保持测试数据最小化。
- JWT 和权限测试可能需要大量重复准备数据 -> 提供测试数据构造工具，复用用户、角色、权限和 token 构建逻辑。
- 生成代码范围可能失控 -> 首次实现只覆盖规格和任务中列出的接口与行为。
- 密码和敏感信息可能误入日志 -> 尽量增加响应脱敏和日志脱敏测试。

## 迁移计划

1. 生成工程骨架和 Maven 依赖。
2. 生成 common 和 arch 基础能力。
3. 生成 user 和 permission 业务模块。
4. 生成 SQL、Mapper XML、测试和覆盖率配置。
5. 执行 `mvn test` 和 `mvn verify`。
6. 修复编译错误、测试失败或覆盖率缺口，再标记任务完成。

回滚策略：本次是新增项目骨架，若需要回滚，可以删除本次生成的源码和资源文件，或在归档前撤销该 change 对应实现。

## 待确认问题

- `application.yml` 中的数据库连接信息默认使用占位值，除非后续提供真实本地连接配置。
- 如果本地没有 MySQL，Mapper 测试应使用独立测试 profile 或轻量测试数据库，避免依赖开发者本机数据库。
