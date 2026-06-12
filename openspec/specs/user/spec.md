# OpenSpec 用户模块业务契约 V1.0

## Purpose
定义用户注册、账号密码登录、JWT 签发、当前用户信息、密码修改和用户状态校验能力。该模块必须落地登录策略模式，并依赖 common 的统一返回、错误码、注解和 arch 的认证上下文。

## Requirements

### Requirement: 用户实体和数据表
系统 SHALL 生成 `SysUser` 实体、`sys_user` 数据表和对应 MyBatis Mapper。

#### Scenario: 生成用户字段
- **WHEN** 生成用户模块
- **THEN** `SysUser` 包含 `id`、`username`、`password`、`nickname`、`phone`、`email`、`status`、`lastLoginTime`、`createTime`、`updateTime`、`deleted`
- **AND** `username` 唯一
- **AND** `password` 标记 `@Password`

#### Scenario: 查询正常用户
- **WHEN** Mapper 根据用户名查询用户
- **THEN** SQL 必须过滤 `deleted=0`
- **AND** 只返回 `status=1` 的可登录用户，除非查询方法明确用于后台管理

### Requirement: 用户注册接口
系统 SHALL 提供匿名用户注册接口并加密保存密码。

#### Scenario: 注册成功
- **WHEN** 客户端调用 `POST /api/user/register` 并提交合法 `RegisterDTO`
- **THEN** 系统保存新用户
- **AND** 密码使用 BCrypt 加密
- **AND** 返回 `Result<UserVO>`

#### Scenario: 用户名重复
- **WHEN** 注册请求中的用户名已存在且未逻辑删除
- **THEN** 系统返回用户模块错误码 `20001 USERNAME_EXISTS`
- **AND** 不写入新用户数据

### Requirement: 账号密码登录策略
系统 SHALL 使用策略模式实现账号密码登录，并保留扩展短信、三方和验证码登录策略的位置。

#### Scenario: 登录策略分发
- **WHEN** Controller 收到 `POST /api/user/login`
- **THEN** Controller 调用 `LoginContext.login(LoginDTO)`
- **AND** `LoginContext` 根据 `loginType` 选择 `PasswordLoginStrategy`
- **AND** Controller 不直接依赖 `PasswordLoginStrategy`

#### Scenario: 新增登录类型
- **WHEN** 后续增加短信或三方登录
- **THEN** 系统只新增对应 `LoginStrategy` 实现类
- **AND** 不修改既有账号密码登录核心逻辑

### Requirement: 登录请求和响应模型
系统 SHALL 生成清晰的登录 DTO、VO 和登录类型枚举。

#### Scenario: 生成 LoginDTO
- **WHEN** 生成登录入参
- **THEN** `LoginDTO` 包含 `loginType`、`username`、`password`
- **AND** `username` 和 `password` 使用 Bean Validation 校验非空
- **AND** `password` 标记 `@Password`

#### Scenario: 生成 LoginVO
- **WHEN** 登录成功
- **THEN** `LoginVO` 包含 `token`、`tokenType`、`expiresIn`、`user`
- **AND** `tokenType` 固定为 `Bearer`
- **AND** `user` 使用 `UserVO` 且不包含密码字段

### Requirement: 登录成功流程
系统 SHALL 在账号密码校验通过后签发 JWT 并更新最后登录时间。

#### Scenario: 密码登录成功
- **WHEN** 用户提交存在的用户名和正确密码
- **THEN** 系统返回 `Result<LoginVO>`
- **AND** JWT claims 包含 `userId`、`username`
- **AND** 系统更新 `lastLoginTime`

#### Scenario: 返回用户脱敏信息
- **WHEN** 登录成功响应包含用户信息
- **THEN** `phone` 和 `email` 按 common 脱敏规则返回
- **AND** 响应中不得包含 `password`

### Requirement: 登录失败流程
系统 SHALL 对用户不存在、密码错误、用户禁用和 Token 生成失败提供明确失败行为。

#### Scenario: 用户名或密码错误
- **WHEN** 用户名不存在或密码不匹配
- **THEN** 系统返回用户模块错误码 `20002 USERNAME_OR_PASSWORD_ERROR`
- **AND** 响应不得提示具体是用户名错误还是密码错误

#### Scenario: 用户被禁用
- **WHEN** 用户存在但 `status=0`
- **THEN** 系统返回用户模块错误码 `20003 USER_DISABLED`
- **AND** 不签发 JWT

### Requirement: 当前用户信息接口
系统 SHALL 提供获取当前登录用户资料的受保护接口。

#### Scenario: 获取当前用户
- **WHEN** 已登录用户调用 `GET /api/user/me`
- **THEN** 系统从 ThreadLocal 上下文读取当前用户 ID
- **AND** 返回 `Result<UserVO>`

#### Scenario: 未登录访问
- **WHEN** 未携带 Token 的客户端调用 `GET /api/user/me`
- **THEN** 系统返回错误码 `10001`

### Requirement: 修改密码接口
系统 SHALL 提供登录用户修改密码能力，并校验旧密码。

#### Scenario: 修改密码成功
- **WHEN** 已登录用户调用 `PUT /api/user/password` 并提交正确旧密码和合法新密码
- **THEN** 系统使用 BCrypt 保存新密码
- **AND** 返回 `Result<Void>`

#### Scenario: 旧密码错误
- **WHEN** 修改密码请求中的旧密码不正确
- **THEN** 系统返回用户模块错误码 `20004 OLD_PASSWORD_ERROR`
- **AND** 不修改密码

### Requirement: 用户模块错误码
系统 SHALL 为用户模块生成独立错误码枚举并接入统一错误码接口。

#### Scenario: 生成错误码枚举
- **WHEN** 生成用户模块
- **THEN** 系统生成 `UserErrorCode`
- **AND** 包含 `20001 USERNAME_EXISTS`、`20002 USERNAME_OR_PASSWORD_ERROR`、`20003 USER_DISABLED`、`20004 OLD_PASSWORD_ERROR`

#### Scenario: 抛出用户业务异常
- **WHEN** 用户业务校验失败
- **THEN** Service 抛出 `BizException(UserErrorCode.xxx)`
- **AND** 不裸抛 `Exception`

### Requirement: 用户模块测试
系统 SHALL 为注册、登录、当前用户、修改密码和用户安全边界生成覆盖核心业务分支的自动化测试。

#### Scenario: 登录测试
- **WHEN** 生成用户模块测试
- **THEN** 测试必须覆盖登录成功、用户名不存在、密码错误、用户禁用、参数校验失败
- **AND** 测试必须校验 JWT 返回字段、`lastLoginTime` 更新和 Controller 只依赖 `LoginContext`

#### Scenario: 注册测试
- **WHEN** 生成注册测试
- **THEN** 测试必须覆盖注册成功、用户名重复、参数校验失败、密码 BCrypt 加密保存
- **AND** 测试必须校验响应 `UserVO` 不包含密码字段

#### Scenario: 当前用户测试
- **WHEN** 生成当前用户信息测试
- **THEN** 测试必须覆盖已登录获取成功、未登录访问失败、用户不存在、用户禁用
- **AND** 测试必须校验手机号和邮箱脱敏输出

#### Scenario: 修改密码测试
- **WHEN** 生成修改密码测试
- **THEN** 测试必须覆盖修改成功、旧密码错误、新密码参数非法、未登录访问失败
- **AND** 测试必须校验新密码使用 BCrypt 重新加密且旧密码不可继续登录

#### Scenario: 用户模块覆盖率
- **WHEN** 执行 `mvn verify`
- **THEN** `com.enterprise.user` 模块行覆盖率不得低于 80%
- **AND** 登录策略、用户 Service、用户 Controller 的核心分支必须纳入覆盖率统计

## Generation Notes
- Controller 路径统一以 `/api/user` 开头。
- `POST /api/user/login` 和 `POST /api/user/register` 必须标记 `@Anonymous`。
- 其他用户接口默认需要登录。
