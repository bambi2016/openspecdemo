# OpenSpec 权限角色模块契约 V1.0
依赖引用：
../common/base.spec.md
../arch/arch.spec.md

## 架构落地
鉴权校验复用架构模板方法模式，拦截器作为模板调用方

## 实体：SysRole、SysPermission、中间关联表
## 接口：角色新增、权限绑定、权限标识查询
## 权限标记 @Perm(xxx:xxx)