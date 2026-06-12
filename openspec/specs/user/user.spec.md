# OpenSpec 用户模块业务契约 V1.0
依赖引用：
../common/base.spec.md
../arch/arch.spec.md

## 架构落地说明
本模块登录功能严格遵循 arch.spec.md 规定：登录采用策略模式实现
支持策略：账号密码登录（首期），预留扩展短信、三方登录策略

## 1 实体、DTO、VO、错误码、接口（沿用之前纯净意图写法）
## 2 业务分层实现约束
1. LoginStrategy 顶层策略接口定义登录统一执行方法
2. PasswordLoginStrategy：账号密码登录实现类
3. LoginContext：策略调度上下文，对外统一暴露login入口
4. UserService 内部调用LoginContext，不直接依赖具体策略实现

## 3 接口
POST /api/user/login @Anonymous
入参LoginDTO，出参Result<LoginVO>