# OpenSpec 公共基础契约
## 1 统一返回结构
字段：
code：数字编码
msg：提示文本
data：业务返回数据，可空
timestamp：毫秒时间戳

## 2 分页通用结构
records：数据数组
total：总条数
pageNum：当前页码
pageSize：每页条数
pages：总页数

## 3 全局公共错误码
0：成功 SUCCESS
10001：TOKEN_EMPTY 未携带登录凭证
10002：TOKEN_INVALID 凭证失效
10003：PERMISSION_DENIED 无操作权限
10004：PARAM_VALID_ERROR 参数校验失败
10005：DATA_NOT_FOUND 数据不存在

## 4 数据库通用字段约束
id：大整型主键
createTime：创建时间，数据库自动填充
updateTime：更新时间，自动刷新
deleted：逻辑删除，0未删 1已删，标记@LogicDelete
status：状态通用 0禁用 1正常

## 5 HTTP全局约束
鉴权头：Authorization: Bearer {token}
请求格式：application/json
免登接口仅限登录、注册
## 6 通用标记意图
@Anonymous：接口免登录
@Perm(权限标识)：接口需权限校验
@Password：密码字段，加密存储、脱敏返回、日志屏蔽
@Sensitive：敏感字段，响应自动脱敏