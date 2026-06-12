# OpenSpec 公共基础契约 V1.0

## Purpose
定义所有模块共享的返回结构、分页结构、错误码、通用字段、注解语义、脱敏规则和基础工具类。生成器应先生成 common 能力，再让 user、permission 和 arch 能力依赖这些公共契约。

## Requirements

### Requirement: 统一返回结构
系统 SHALL 使用 `Result<T>` 作为所有 REST 接口的响应体。

#### Scenario: 成功响应
- **WHEN** 业务处理成功
- **THEN** 接口返回 `code=0`
- **AND** `msg=SUCCESS`
- **AND** `data` 包含业务响应对象或为 `null`
- **AND** `timestamp` 为毫秒时间戳

#### Scenario: 失败响应
- **WHEN** 业务处理失败
- **THEN** 接口返回非 0 错误码
- **AND** `msg` 包含可读错误信息
- **AND** `data` 为 `null`

### Requirement: Result 建造方法
系统 SHALL 为 `Result<T>` 提供统一建造入口，禁止 Controller 重复拼装响应字段。

#### Scenario: 构建成功结果
- **WHEN** 业务代码调用 `Result.success(data)`
- **THEN** 系统返回包含成功码、成功消息、业务数据和时间戳的结果

#### Scenario: 构建失败结果
- **WHEN** 业务代码调用 `Result.fail(errorCode)`
- **THEN** 系统返回错误码定义中的 code 和 message

### Requirement: 分页通用结构
系统 SHALL 使用 `PageResult<T>` 表达分页接口响应。

#### Scenario: 返回分页列表
- **WHEN** 接口返回分页数据
- **THEN** `PageResult<T>` 包含 `records`、`total`、`pageNum`、`pageSize`、`pages`
- **AND** `records` 为空时返回空数组而不是 `null`

#### Scenario: 页码参数非法
- **WHEN** `pageNum < 1` 或 `pageSize < 1`
- **THEN** 系统返回错误码 `10004`

### Requirement: 全局错误码
系统 SHALL 生成稳定的全局错误码枚举，并允许业务模块扩展自己的错误码。

#### Scenario: 公共认证错误
- **WHEN** 未携带 Token
- **THEN** 系统使用 `10001 TOKEN_EMPTY`
- **AND** 当 Token 无效或过期时使用 `10002 TOKEN_INVALID`

#### Scenario: 公共业务错误
- **WHEN** 权限不足
- **THEN** 系统使用 `10003 PERMISSION_DENIED`
- **AND** 参数校验失败使用 `10004 PARAM_VALID_ERROR`
- **AND** 数据不存在使用 `10005 DATA_NOT_FOUND`

### Requirement: 通用数据字段
系统 SHALL 为所有数据库实体生成一致的审计字段和逻辑删除字段。

#### Scenario: 新增数据
- **WHEN** 插入任一业务实体
- **THEN** `createTime` 和 `updateTime` 自动填充
- **AND** `deleted=0`
- **AND** `status=1`，除非业务显式指定其他状态

#### Scenario: 删除数据
- **WHEN** 业务执行删除操作
- **THEN** 系统更新 `deleted=1`
- **AND** 不生成物理删除 SQL

### Requirement: HTTP 全局约束
系统 SHALL 统一约束请求格式、认证头和匿名接口边界。

#### Scenario: JSON 请求
- **WHEN** 客户端调用 REST 接口
- **THEN** 请求和响应 Content-Type 使用 `application/json`

#### Scenario: 匿名接口
- **WHEN** 接口标记 `@Anonymous`
- **THEN** 认证拦截器跳过登录校验
- **AND** 首期只允许登录、注册、健康检查接口匿名访问

### Requirement: 通用注解
系统 SHALL 生成 `@Anonymous`、`@Perm`、`@Password`、`@Sensitive` 四类通用注解。

#### Scenario: 权限标记
- **WHEN** Controller 方法标记 `@Perm("permission:code")`
- **THEN** 权限拦截器读取该权限标识并执行权限校验

#### Scenario: 敏感字段标记
- **WHEN** DTO、Entity 或 VO 字段标记 `@Password` 或 `@Sensitive`
- **THEN** 日志和响应处理器按注解语义进行加密、屏蔽或脱敏

### Requirement: 密码和脱敏工具
系统 SHALL 提供密码加密校验工具和敏感字段脱敏工具。

#### Scenario: 保存密码
- **WHEN** 注册或重置密码
- **THEN** 系统使用 BCrypt 对密码加密后存储
- **AND** 原始密码不得写入数据库或日志

#### Scenario: 脱敏手机号和邮箱
- **WHEN** 返回手机号或邮箱
- **THEN** 手机号保留前三位和后四位
- **AND** 邮箱保留用户名首尾字符和域名

## Generation Notes
- `Result<T>`、`PageResult<T>`、错误码枚举、公共注解和工具类应放在 `com.enterprise.common`。
- 字段命名采用 Java camelCase，数据库字段采用 snake_case。
