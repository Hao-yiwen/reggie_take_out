# 瑞吉外卖

基于 Spring Boot 的外卖练习项目：一个进程同时提供管理端和用户端静态页面，以及对应的 REST 接口。用来学习员工与菜品管理、购物车下单、Redis 缓存和 ShardingSphere 读写分离，仓库里没有独立的前端工程。

![Java](https://img.shields.io/badge/Java-1.8-007396)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-2.4.5-6DB33F)
![MyBatis--Plus](https://img.shields.io/badge/MyBatis--Plus-3.4.2-000000)
![Redis](https://img.shields.io/badge/Redis-缓存-DC382D)

用户端页面标题是「菩提阁」，店名文案是「菩提阁餐厅」；管理端标题是「瑞吉外卖管理端」。Maven 坐标为 `com.itheima:reggie_take_out:1.0-SNAPSHOT`。

## 已实现的功能

登录态放在 HttpSession 里：员工键为 `employee`，移动端用户键为 `user`。`LoginCheckFilter` 拦截未登录的业务接口，放行登录、静态资源和 Knife4j。统一响应体是 `R`（`code = 1` 成功，`code = 0` 失败）。

| 模块 | 后端实际提供的能力 |
| --- | --- |
| 员工 | 登录、退出；分页查询；新增（初始密码 `123456`，入库前做 MD5）；按 id 查询和修改（含启用/禁用） |
| 分类 | 新增、分页、修改、条件列表；删除前检查是否仍关联菜品或套餐 |
| 菜品 | 新增、修改（同时维护口味）、分页、按 id 查询、按分类查询在售菜品。列表结果写入 Redis，键为 `dish_{分类id}_{状态}`，60 分钟 |
| 套餐 | 新增（同时写入套餐菜品）、分页、按分类查询、删除停售套餐。查询走 Spring Cache 的 `setmealCache`，新增和删除会清空该缓存 |
| 文件 | 上传到 `reggie.path`，下载时按文件名读回，响应类型固定为 `image/jpeg` |
| 用户 | 生成 4 位验证码，写入 Redis（5 分钟）并打印到日志；验证码正确则登录，手机号不存在时自动注册 |
| 地址簿 | 新增、列表、按 id 查询、查询默认地址、设置默认地址 |
| 购物车 | 添加（同一菜品或套餐数量 +1）、当前用户列表、清空 |
| 订单 | `POST /order/submit`：读取购物车和地址，写入 `orders` 与 `order_detail`，然后清空购物车。订单状态直接写成 `2` |

公共字段（`createTime`、`updateTime`、`createUser`、`updateUser`）由 `MyMetaObjectHandler` 在插入和更新时填充，当前用户 id 来自 `BaseContext`。主键策略是 MyBatis-Plus 的 `ASSIGN_ID`。菜品和套餐价格在页面上除以 100 显示，库里按「分」存储。

接口文档由 Knife4j 生成，标题为「瑞吉外卖」。套餐的新增和分页接口带有 Swagger 注解。

## 页面已调用、后端尚未实现的接口

静态页比控制器更完整。下面这些请求在前端脚本里存在，当前 Java 代码没有对应实现：

| 场景 | 前端请求 | 现状 |
| --- | --- | --- |
| 菜品删除、起售/停售 | `DELETE /dish`、`POST /dish/status/{status}` | 无映射 |
| 套餐修改、详情、起售/停售 | `PUT /setmeal`、`GET /setmeal/{id}`、`POST /setmeal/status/{status}` | 无映射 |
| 套餐内菜品详情 | `GET /setmeal/dish/{id}` | 无映射 |
| 购物车减数量 | `POST /shoppingCart/sub` | 方法体直接 `return null` |
| 管理端订单 | `GET /order/page`、`PUT /order`、`GET /orderDetail/{id}` | `OrderDetailController` 为空类 |
| 用户端订单 | `GET /order/list`、`GET /order/userPage`、`POST /order/again` | 只有提交订单 |
| 地址修改、删除、最近地址 | `PUT /addressBook`、`DELETE /addressBook`、`GET /addressBook/lastUpdate` | 无映射 |
| 用户退出 | `POST /user/loginout` | 无映射 |
| 短信 | `SMSUtils.sendMessage` | `UserController` 中调用已被注释，验证码只进日志和 Redis |

另外两处和字面预期不一致，排查问题时可以直接看代码：

- `OrderServiceImpl.submit` 在购物车非空判断之后，又用购物车变量判断地址；地址簿记录为空时会在后续取值处抛空指针。
- `SetmealServiceImpl.deleteWithDish` 删除套餐菜品关系时，条件写在 `setmeal_dish.dish_id` 上，传入的却是套餐 id。

## 技术栈

| 组件 | 版本 | 用途 |
| --- | --- | --- |
| Java | 1.8（`pom.xml` 的 `java.version`） | 编译目标 |
| Spring Boot | 2.4.5 | Web、事务、缓存 |
| MyBatis-Plus | 3.4.2 | Mapper 与分页插件 |
| MySQL Connector/J | 8.0.23（由 Spring Boot 依赖管理带入） | JDBC |
| Druid | 1.1.23 | 主从数据源连接池 |
| ShardingSphere Sharding-JDBC | 4.0.0-RC1 | 一主一从，读库轮询 |
| Spring Data Redis / Spring Cache | 跟随 Boot 2.4.5 | 验证码、菜品缓存、套餐缓存 |
| Knife4j | 3.0.2 | `/doc.html` |
| Lombok | 1.18.20 | 样板代码 |
| Fastjson | 1.2.76 | 过滤器里序列化未登录响应 |
| commons-lang | 2.6 | 字符串判断 |
| 阿里云短信 SDK | `alibabacloud-dysmsapi20170525` 2.0.24 | 工具类已引入，登录流程未调用 |
| 管理端页面 | Vue 2.6.12、Element UI、Axios 0.21.1 | `resources/backend` |
| 用户端页面 | Vue、Vant、同一份 Axios | `resources/front` |

页面脚本随仓库提交，不需要 Node.js 或单独的前端构建。

## 目录结构

```text
.
├── pom.xml
├── README.md
├── QA.md                          # 一条关于菜品分页里 getById 的学习笔记
└── src
    ├── main
    │   ├── java/com/itheima/reggie
    │   │   ├── ReggieApplication.java
    │   │   ├── common/            # R、全局异常、字段自动填充、ThreadLocal
    │   │   ├── config/            # MVC 静态资源与 Knife4j、分页、RedisTemplate
    │   │   ├── controller/
    │   │   ├── dto/               # DishDto、SetmealDto
    │   │   ├── entity/
    │   │   ├── filter/LoginCheckFilter.java
    │   │   ├── mapper/
    │   │   ├── service/
    │   │   └── utils/             # 验证码、阿里云短信工具
    │   └── resources
    │       ├── application.yml
    │       ├── backend/           # 管理端：员工、分类、菜品、套餐、订单明细页面
    │       └── front/             # 用户端：首页、登录、地址、下单、订单、个人中心
    └── test/java/com/itheima/test/UploadFileTest.java
```

实体与 MyBatis-Plus 默认表名（类名转下划线，代码里没有 `@TableName`）：

| 实体 | 表名 |
| --- | --- |
| `Employee` | `employee` |
| `Category` | `category` |
| `Dish` / `DishFlavor` | `dish` / `dish_flavor` |
| `Setmeal` / `SetmealDish` | `setmeal` / `setmeal_dish` |
| `User` | `user` |
| `AddressBook` | `address_book` |
| `ShoppingCart` | `shopping_cart` |
| `Orders` / `OrderDetail` | `orders` / `order_detail` |

仓库没有建表 SQL。表结构需要和 `entity` 包中的字段对齐，包含自动填充字段和逻辑删除字段 `isDeleted`（`Category` 里该字段被注释掉了）。

## 环境要求

- JDK 8。项目声明 `java.version` 为 1.8，Spring Boot 2.4.5 不能用当前环境默认的 JDK 21 直接编译。
- Maven 3.6 及以上（本仓库用 Maven 3.8.7 验证过打包）。
- MySQL，并准备 ShardingSphere 配置中的主库和从库。两边都要有同名库 `reggie` 和同一套表。
- Redis。验证码、菜品缓存和套餐缓存都会用到，启动时就会创建 `RedisTemplate`。
- 上传目录可写。默认是 `application.yml` 里的 `reggie.path: /usr/local/tmp/`。

## 配置

编辑 `src/main/resources/application.yml` 后再启动。当前文件写死了主从库地址、数据库账号和 Redis 密码，请换成自己的实例，不要沿用仓库里的连接信息。

需要改的项：

- `spring.shardingsphere.datasource.master`、`slave`：驱动、URL、用户名、密码。读请求按 `round_robin` 分到从库，`props.sql.show` 为 `true` 时会打印 SQL。
- `spring.redis`：主机、端口、密码、库编号。
- `reggie.path`：图片保存目录，进程需要对它有写权限。

管理端登录页预填了用户名 `admin`、密码 `123456`。这只是页面初始值。登录接口会把密码做 MD5 后和 `employee.password` 比较，账号必须事先存在于员工表，且 `status != 0`。新增员工的初始密码同样是 `123456`。

## 安装与运行

```bash
export JAVA_HOME=/usr/lib/jvm/java-8-openjdk-amd64   # 按本机 JDK 8 路径修改
export PATH="$JAVA_HOME/bin:$PATH"

# 先改 application.yml，并准备好主从库、Redis 和上传目录
mkdir -p /usr/local/tmp

mvn -DskipTests package
mvn spring-boot:run
```

也可以运行打好的包：

```bash
java -jar target/reggie_take_out-1.0-SNAPSHOT.jar
```

默认端口 `8080`。启动成功时日志会打印「项目启动成功~」。

| 入口 | 地址 |
| --- | --- |
| 管理端登录 | http://localhost:8080/backend/page/login/login.html |
| 管理端首页 | http://localhost:8080/backend/index.html |
| 用户端首页 | http://localhost:8080/front/index.html |
| Knife4j | http://localhost:8080/doc.html |

用户端登录：调用 `/user/sendMsg` 后，在应用日志里查找 `code=` 后面的 4 位数字，再用该验证码调用 `/user/login`。页面上的「获取验证码」不会收到真实短信。

## 本次验证

在本环境使用 OpenJDK 1.8.0_512 与 Maven 3.8.7：

- `mvn -DskipTests compile` 成功。
- `mvn test` 成功。唯一用例是 `UploadFileTest`，只截取文件名后缀，不启动 Spring，也不访问数据库。
- `mvn -DskipTests package` 成功，产物为 `target/reggie_take_out-1.0-SNAPSHOT.jar`。

没有执行 `spring-boot:run`。启动会连接 `application.yml` 中的远程主从 MySQL 和本机带密码的 Redis，本次未改配置，也未连接这些地址。静态页和接口联调因此还没有在浏览器里走通。

## 说明

- 仓库中的 png、jpg 是页面素材（登录背景、图标、菜品占位图），没有单独的运行截图。
- `src/main/resources/front/cartData.json` 是一份空购物车示例。首页脚本里的静态地址已被注释，实际请求 `/shoppingCart/list`。
- `QA.md` 记录了一个学习问题：菜品分页里用 `categoryService.getById` 取分类名，而不是 `LambdaQueryWrapper`。
- `.gitignore` 忽略了 IDE 文件和 `logs`，没有忽略 `target/`。
