# 🥣 小红碗点评 · 本地生活点评平台（微服务版）

> 一个仿大众点评/小红书的本地生活点评平台。前端 Vue2 + Element UI，后端 Spring Cloud 微服务（Nacos + Gateway + OpenFeign），Redis 缓存 + H2 数据库，支持商户浏览、探店笔记、评分收藏、优惠券秒杀、消息通知与管理后台。

---

## 一、功能总览

### 👤 用户端

| 模块 | 功能明细 |
|---|---|
| **账号登录** | 手机验证码登录（Mock 短信回显）、新用户自动注册、Token 会话（Redis）、退出登录 |
| **首页信息流** | 探店笔记双列瀑布流、点赞/取消点赞、滚动加载更多、返回顶部悬浮按钮 |
| **商户浏览** | 5 大分类（美食/KTV/酒店/足浴/按摩，共 15 家演示商户）、评分/人气/距离三种排序、卡片悬浮动效 |
| **关键字搜索** | 首页搜索栏按商户名/地址/地区模糊搜索，结果页独立展示 |
| **商户详情** | 商户信息卡、三维评分（口味/环境/服务）、好评榜、图片墙、营业时间、代金券列表、网友评论 |
| **地图找店** | Leaflet + 高德瓦片实时地图（无需地图 Key）、全部商户标记、地点/地址/商户名搜索、点击定位 |
| **探店笔记** | 发布笔记（图片上传）、笔记详情、点赞、**发表评论**（评论数统计 + 消息通知作者） |
| **消息中心** | 点赞/评论/系统通知、未读数角标、单条已读/全部已读、类型筛选（全部/点赞/系统） |
| **评分细分** | 口味/环境/服务三维评分、评语、综合分聚合、商户评分详情、全城排行榜 |
| **收藏心愿单** | 一键收藏/取消、收藏列表、收藏状态实时同步 |
| **优惠券秒杀** | 店铺优惠券列表、秒杀券库存/时间展示、限时抢购 |
| **个人中心** | 资料编辑（昵称/头像上传/介绍/性别/城市/生日）、省市区三级级联、我的笔记、退出登录 |

### 🛠 管理端（独立后台，口令进入）

| 模块 | 功能明细 |
|---|---|
| **口令门** | 管理口令验证（演示口令 admin123） |
| **数据概览** | 商户/用户/笔记/优惠券四维统计卡片、平台速览（最多赞笔记/最高分商户/总点赞/总库存） |
| **商户管理** | 列表/新增/**地图点选定位**（免输经纬度）/**图片上传**/删除 |
| **用户管理** | 注册用户列表、注册时间 |
| **笔记管理** | 全部笔记列表（含作者）、删除 |
| **优惠券管理** | 列表（含秒杀信息）、新增普通券 |

### 🌐 地址选择（省市区三级联动）

- 数据：全国 **31 省 / 342 市 / 2978 区县**（真实行政区划）
- 应用位置：首页城市切换（省→市→区三列选择器）、管理后台商户地址（级联+详细地址）、个人资料所在地区

---

## 二、技术架构

```
浏览器 → nginx:8080 (静态页面 + /api 反向代理)
            └→ Gateway:8090（统一路由，鉴权扩展点）
                ├→ hmdp-user    8082  用户/登录/头像/资料     → H2 + Redis(会话)
                ├→ hmdp-shop    8083  商户/分类/评分/收藏     → H2
                ├→ hmdp-voucher 8084  优惠券/秒杀券           → H2
                └→ hmdp-blog    8085  探店笔记/评论/消息/关注 → H2 + Redis(Feed)
                        │
                        └─ OpenFeign：blog → user（作者/评论人信息批量查询）
注册中心：Nacos 2.0.3（standalone，8848 + gRPC 9848/9849）
```

| 层 | 技术 |
|---|---|
| 前端 | Vue 2 + Element UI + axios + Leaflet（高德瓦片） |
| 网关 | Spring Cloud Gateway（路由 + 全局日志过滤器） |
| 微服务 | Spring Boot 2.3.12 + Spring Cloud Hoxton.SR12 + Spring Cloud Alibaba 2.2.7 + OpenFeign + MyBatis-Plus |
| 数据 | H2（内存，MySQL 兼容模式，启动自动建表灌数据）+ Redis（会话/Feed/去重） |
| 注册中心 | Nacos 2.0.3 |

---

## 三、核心接口清单

### 用户
| 方法 | 接口 | 说明 |
|---|---|---|
| POST | /user/code?phone= | 发送验证码（Redis，2 分钟有效） |
| POST | /user/login | 登录/自动注册，返回 token |
| POST | /user/logout | 登出 |
| GET | /user/me | 当前用户（未登录 401） |
| PUT | /user/nickName | 修改昵称（同步会话） |
| PUT | /user/icon | 修改头像 |
| POST | /user/info/update | 保存资料（介绍/性别/城市/生日） |
| GET | /user/list、/user/batch | 用户列表 / 批量查询（Feign 内部接口） |

### 商户
| 方法 | 接口 | 说明 |
|---|---|---|
| GET | /shop/{id}、/shop/list | 详情 / 全量 |
| GET | /shop/of/type | 分类查询（sortBy=score/comments/distance + x/y） |
| GET | /shop/of/name、/shop/search | 名称搜索 / 名称+地址+地区联合搜索 |
| GET | /shop/geo/list | 地图坐标数据 |
| POST/PUT/DELETE | /shop | 新增/更新/删除 |

### 评分 · 收藏 · 优惠券
| 方法 | 接口 | 说明 |
|---|---|---|
| POST | /score | 三维评分提交（覆盖式） |
| GET | /score/detail?shopId=、/score/rank | 详情 / 排行榜 |
| POST | /favorite/toggle、GET /favorite/list | 收藏切换 / 心愿单 |
| GET | /voucher/list/{shopId}、/voucher/all、/voucher/seckill/list | 券查询 |
| POST | /voucher、/voucher-order/seckill/{id} | 新增券 / 秒杀下单 |

### 笔记 · 评论 · 消息
| 方法 | 接口 | 说明 |
|---|---|---|
| GET | /blog/hot、/blog/list、/blog/{id}、/blog/of/me、/blog/of/follow | 查询组 |
| POST | /blog、PUT /blog/like/{id}、DELETE /blog/{id} | 发布 / 点赞 / 删除 |
| POST | /blog-comments、GET /blog-comments/list | 发表评论 / 评论列表 |
| GET | /message/list、/message/unread、POST /message/read | 消息中心 |
| PUT | /follow/{id}/{isFollow}、GET /follow/or/not/{id} | 关注 |

---

## 四、启动指南

```
一键启动  D:\dianping-app\cloud-scripts\start-cloud.cmd   （约 2 分钟）
一键停止  D:\dianping-app\cloud-scripts\stop-cloud.cmd
```

- MySQL / Redis 为 Windows 服务，开机自启，无需手动管理
- 访问入口：主站 `localhost:8080` ｜ 管理后台 `/admin.html` ｜ Nacos `localhost:8848/nacos`
- H2 为内存数据库：重启后评分/收藏/笔记等运行时数据清空，种子数据与账号自动恢复（重新登录即可）

---

## 五、源码地图

| 内容 | 路径 |
|---|---|
| 微服务父工程 | `D:\dianping-app\hmdp-cloud\`（hmdp-common / user / shop / voucher / blog / gateway） |
| 前端页面 | `D:\dianping-app\HM-dianping\hmdp\nginx-1.18.0\html\hmdp\` |
| 数据库脚本 | 各服务 `src\main\resources\db\hmdp.sql` |
| nginx 配置 | `hmdp\nginx-1.18.0\conf\nginx.conf`（8080/8081 双端口） |
| 启动脚本 | `D:\dianping-app\cloud-scripts\`（start-cloud / stop-cloud / svc-*） |

---

## 六、研发踩坑记录（技术要点）

1. **多网卡环境 Nacos 注册 IP 漂移**：服务注册到虚拟网卡 IP 导致网关转发超时，须显式指定 `spring.cloud.nacos.discovery.ip=127.0.0.1`
2. **Nacos 客户端/服务端版本配对**：Spring Cloud Alibaba 2.2.7 内置 gRPC 客户端，必须搭配 Nacos Server 2.x
3. **环境变量劫持端口**：Windows 全局 `SERVER_PORT` 环境变量会被 Spring 宽松绑定识别为 `server.port`，所有服务以命令行参数显式锁端口
4. **MyBatis-Plus 主键策略**：`IdType.AUTO` 会丢弃手动主键，业务主键须用 `IdType.INPUT`
5. **前端图片空值防御**：`images` 字段判空后再 `split`，避免无图笔记导致列表渲染崩溃
6. **输入法回车竞态**：搜索框 `keyup.enter` 需过滤 `isComposing`，避免选字回车误触发搜索
7. **nginx 正则 location 优先级**：正则块会覆盖前缀块，`root` 应提升到 server 级
8. **页面无缓存策略**：HTML/CSS/JS 全部 `no-cache`（开发期），杜绝浏览器旧缓存导致的"改了没生效"
