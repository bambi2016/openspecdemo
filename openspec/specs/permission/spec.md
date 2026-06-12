# OpenSpec 权限角色模块契约 V1.0

## Purpose
定义角色、权限、用户角色关系、角色权限关系、权限标识查询和接口权限校验能力。该模块必须与 arch 的认证模板方法配合，并通过 common 的 `@Perm` 注解驱动接口级权限控制。

## Requirements

### Requirement: 权限数据模型
系统 SHALL 生成角色、权限和关联关系实体、数据表、Mapper 与 XML。

#### Scenario: 生成权限表
- **WHEN** 生成权限模块
- **THEN** 系统生成 `sys_role`、`sys_permission`、`sys_user_role`、`sys_role_permission`
- **AND** 每张表包含通用字段 `id`、`create_time`、`update_time`、`deleted`、`status`

#### Scenario: 生成实体字段
- **WHEN** 生成实体
- **THEN** `SysRole` 包含 `roleCode`、`roleName`、`description`
- **AND** `SysPermission` 包含 `permCode`、`permName`、`description`
- **AND** 关联表包含对应外键 ID 字段

### Requirement: 权限标识规范
系统 SHALL 使用稳定的字符串权限标识表达接口权限。

#### Scenario: 生成权限标识
- **WHEN** 定义权限编码
- **THEN** 权限标识使用 `<domain>:<action>` 格式
- **AND** 示例包括 `role:create`、`role:update`、`role:bind-permission`、`permission:list`

#### Scenario: 权限标识唯一
- **WHEN** 新增或初始化权限数据
- **THEN** `permCode` 必须唯一
- **AND** 重复权限标识返回权限模块错误码 `30001 PERMISSION_CODE_EXISTS`

### Requirement: 角色新增接口
系统 SHALL 提供创建角色的受保护接口。

#### Scenario: 创建角色成功
- **WHEN** 拥有 `role:create` 权限的用户调用 `POST /api/permission/roles`
- **THEN** 系统创建角色
- **AND** 返回 `Result<RoleVO>`

#### Scenario: 角色编码重复
- **WHEN** 创建角色时 `roleCode` 已存在且未逻辑删除
- **THEN** 系统返回权限模块错误码 `30002 ROLE_CODE_EXISTS`
- **AND** 不写入新角色

### Requirement: 角色绑定权限接口
系统 SHALL 提供将权限集合绑定到角色的接口。

#### Scenario: 绑定权限成功
- **WHEN** 拥有 `role:bind-permission` 权限的用户调用 `PUT /api/permission/roles/{roleId}/permissions`
- **THEN** 系统校验角色存在且未删除
- **AND** 系统校验所有权限 ID 存在且未删除
- **AND** 系统替换该角色的权限集合
- **AND** 返回 `Result<Void>`

#### Scenario: 角色不存在
- **WHEN** 绑定权限时角色不存在或已逻辑删除
- **THEN** 系统返回错误码 `10005`
- **AND** 不修改角色权限关系

### Requirement: 查询当前用户权限标识
系统 SHALL 提供基于当前用户查询权限标识集合的能力。

#### Scenario: 查询当前权限
- **WHEN** 已登录用户调用 `GET /api/permission/current`
- **THEN** 系统根据用户角色查询所有有效权限
- **AND** 返回 `Result<List<String>>`
- **AND** 返回值只包含 `permCode`

#### Scenario: 过滤无效数据
- **WHEN** 查询用户权限
- **THEN** 已禁用或已逻辑删除的角色、权限和关联关系不得出现在结果中

### Requirement: 用户角色绑定能力
系统 SHALL 提供将角色绑定到用户的业务能力，供初始化或后台管理使用。

#### Scenario: 绑定用户角色
- **WHEN** 拥有 `user:bind-role` 权限的用户调用 `PUT /api/permission/users/{userId}/roles`
- **THEN** 系统校验用户和角色存在
- **AND** 系统替换该用户的角色集合
- **AND** 返回 `Result<Void>`

#### Scenario: 用户不存在
- **WHEN** 绑定角色时目标用户不存在
- **THEN** 系统返回错误码 `10005`
- **AND** 不写入用户角色关系

### Requirement: @Perm 接口鉴权
系统 SHALL 通过 `@Perm` 注解和鉴权模板方法执行接口级权限校验。

#### Scenario: 权限校验通过
- **WHEN** 当前用户拥有接口声明的权限标识
- **THEN** 请求进入 Controller 方法

#### Scenario: 权限校验失败
- **WHEN** 当前用户缺少接口声明的权限标识
- **THEN** 系统返回错误码 `10003`
- **AND** 不进入 Controller 方法

### Requirement: 权限模块错误码
系统 SHALL 为权限模块生成独立错误码枚举并接入统一错误码接口。

#### Scenario: 生成权限错误码
- **WHEN** 生成权限模块
- **THEN** 系统生成 `PermissionErrorCode`
- **AND** 包含 `30001 PERMISSION_CODE_EXISTS`、`30002 ROLE_CODE_EXISTS`、`30003 ROLE_HAS_USERS`

#### Scenario: 抛出权限业务异常
- **WHEN** 权限业务校验失败
- **THEN** Service 抛出 `BizException(PermissionErrorCode.xxx)`
- **AND** 不裸抛 `Exception`

### Requirement: 权限初始化数据
系统 SHALL 在初始化 SQL 中生成基础管理员角色和核心权限。

#### Scenario: 初始化管理员角色
- **WHEN** 生成 `init.sql`
- **THEN** SQL 包含 `ADMIN` 角色
- **AND** SQL 包含用户登录后可进行后台管理所需的核心权限

#### Scenario: 初始化权限编码
- **WHEN** 生成权限初始化数据
- **THEN** 核心权限至少包含 `role:create`、`role:update`、`role:bind-permission`、`permission:list`、`user:bind-role`

### Requirement: 权限模块测试
系统 SHALL 为角色、权限、绑定关系、当前用户权限查询和 `@Perm` 鉴权生成覆盖核心业务分支的自动化测试。

#### Scenario: 角色测试
- **WHEN** 生成权限 Service 测试
- **THEN** 测试必须覆盖角色创建成功、角色编码重复、参数校验失败、逻辑删除过滤

#### Scenario: 权限标识测试
- **WHEN** 生成权限标识相关测试
- **THEN** 测试必须覆盖权限编码唯一性、权限编码格式非法、禁用权限过滤

#### Scenario: 绑定关系测试
- **WHEN** 生成角色权限和用户角色绑定测试
- **THEN** 测试必须覆盖绑定成功、角色不存在、权限不存在、用户不存在、重复绑定、替换绑定集合
- **AND** 测试必须校验失败场景不会写入部分关联数据

#### Scenario: 当前权限查询测试
- **WHEN** 生成当前用户权限查询测试
- **THEN** 测试必须覆盖用户拥有多个角色、角色权限去重、禁用角色过滤、禁用权限过滤、逻辑删除关系过滤

#### Scenario: 鉴权测试
- **WHEN** 生成权限拦截器或鉴权模板测试
- **THEN** 测试必须覆盖未登录、Token 无效、权限通过、权限不足、接口未声明 `@Perm`
- **AND** 测试必须断言权限不足返回错误码 `10003`

#### Scenario: 权限模块覆盖率
- **WHEN** 执行 `mvn verify`
- **THEN** `com.enterprise.permission` 模块行覆盖率不得低于 80%
- **AND** 权限 Service、鉴权模板、权限拦截器和权限 Controller 的核心分支必须纳入覆盖率统计

## Generation Notes
- Controller 路径统一以 `/api/permission` 开头。
- 权限模块可以读取用户 ID，但不得直接依赖 user 模块实体。
