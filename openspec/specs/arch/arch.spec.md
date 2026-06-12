# OpenSpec 系统架构顶层约束 V1.0
## 1 工程基础技术栈约束
1. 后端开发语言：Java 17
2. 主框架：SpringBoot 3.2.x
3. ORM持久层：MyBatis XML（复杂SQL强制XML，简单CRUD允许注解）
4. 安全令牌：JWT
5. 测试框架：JUnit5 + Mockito
6. 日志框架：SLF4J + Logback
7. 数据库：MySQL 8.0
8. 构建工具：Maven

## 2 统一项目目录分层约束
src/main/java/com/enterprise
1. arch：架构公共组件（拦截器、AOP、全局异常、上下文工具）
2. common：通用返回、分页、枚举、工具类
3. xxx.module（user/permission等每个业务独立包）
   - dto：请求入参
   - vo：响应出参
   - entity：数据库实体
   - mapper：MyBatis接口
   - service
     - interface：业务接口
     - impl：业务实现类
   - controller：REST接口
src/main/resources
1. mybatis：所有*.xml映射文件
2. sql：初始化建表脚本
3. application.yml：全局配置
src/test/java：对应业务模块单元测试

## 3 全局强制设计模式约束
### 3.1 登录认证场景：强制使用策略模式
业务场景：账号密码登录、后续可扩展短信登录、微信登录、验证码登录
意图约束：
1. 定义顶层 LoginStrategy 策略顶层接口
2. 每种登录方式单独实现一个策略实现类
3. LoginContext 上下文类统一调度分发不同策略
4. Controller只调用上下文，不直接耦合具体登录实现
5. 新增登录类型只新增策略类，不修改原有登录逻辑（开闭原则）

### 3.2 权限校验场景：模板方法模式
1. 顶层鉴权模板，封装通用token解析、用户加载逻辑
2. 子类可扩展自定义权限额外校验规则

### 3.3 全局统一返回：建造者模式构建Result对象
### 3.4 数据脱敏/密码加密：策略模式
多种加密、脱敏规则可切换替换

## 4 代码工程编码约束
1. 所有业务异常统一抛出自定义BizException，禁止裸抛Exception
2. 数据库删除统一逻辑删除，禁止物理Delete
3. 所有Controller入参必须开启参数校验
4. 敏感数据（密码、手机号）日志禁止打印明文
5. Service层禁止直接操作HttpServletRequest/Response，使用ThreadLocal上下文获取登录用户

## 5 模块拆分约束
1. 用户业务、权限业务完全分包隔离，各自独立spec文档
2. 业务之间禁止互相import对方spec，跨模块调用只能通过对外暴露VO/接口
3. 公共能力统一下沉到common、arch包，业务模块只依赖公共层