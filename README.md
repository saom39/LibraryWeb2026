# 图书馆管理系统（LibraryWeb2026）

基于 **Java Servlet + JDBC + MySQL** 的图书借阅管理平台，服务端使用自研的轻量级 ORM 框架完成对象与数据表映射，前端采用原生 HTML + CSS + Vue + jQuery + Axios，打包为单个 war 由 Tomcat 统一提供（不使用 JSP、不做前后端分离部署）。

> 作者：`__你的姓名__`　学号：`__你的学号__`

## 技术栈

| 层次 | 技术 |
| ---- | ---- |
| 后端 | Java 11、Servlet 4.0（javax.servlet）、原生 JDBC |
| ORM | 自研 `JDBCTool`（反射 + 注解 `@Table` / `@Id` / `@Column`） |
| 数据库 | MySQL 8.x |
| 前端 | HTML5、CSS3、Vue 3、jQuery、Axios |
| JSON | Jackson（jackson-databind、jackson-datatype-jsr310） |
| 构建 | Maven（war 打包） |

## 功能清单

- **登录 / 注册**：图形验证码登录、退出；注册时按角色联动展示不同字段，并校验同名与手机号唯一
- **用户管理**：管理员按姓名 / 手机号 / 角色 / 专业模糊查询用户（只读）
- **图书管理**：操作员添加（按库存自动生成副本）、编辑、废弃图书；关键字模糊查询
- **副本管理**：操作员新增副本（库存 +1）、删除副本（库存 -1，借阅中不可删）
- **借书 / 还书**：事务内完成借阅，应还日期按图书期限自动计算；还书自动判断是否超期
- **借阅记录查询**：管理员支持关键字 / 超期 / 日期范围三种查询；普通用户查询个人借阅
- **数据统计**：管理员按图书类型、按用户专业统计借阅次数（条形图展示）
- **图书推荐与审阅**：教师推荐新书（馆内查重），管理员审阅后入库或填写理由驳回
- **修改密码**：登录用户校验原密码后修改自己的密码（新增功能）
- **逾期罚金**：借阅记录自动计算逾期罚金（逾期每天 1.5 元），在借阅记录与还书界面展示（新增功能）

## 项目结构

```
LibraryWeb2026
├── pom.xml
├── sql
│   └── libraryms_init.sql        # 数据库初始化脚本（建表 + 演示数据）
├── src/main
│   ├── java/com/libraryms
│   │   ├── annotation            # @Table / @Id / @Column 注解
│   │   ├── entity                # Base、User 继承体系、Book、BookCopy、Loan、BookRecommend
│   │   ├── dao                   # 数据访问层（UserDao / BookDao / BookCopyDao / LoanDao / RecommendDao）
│   │   ├── servlet               # 控制器层（AuthServlet / BookServlet / LoanServlet ...）
│   │   ├── filter                # AuthFilter（会话校验）、EncodingFilter（UTF-8）
│   │   └── util                  # JDBCTool（ORM）、EntityMapper、JsonUtil、Result 等
│   ├── resources/db.properties   # 数据库连接配置
│   └── webapp                    # 前端页面（HTML/CSS/JS）
└── .idea                         # IDEA 工程配置（含一键运行配置）
```

## 环境要求

- JDK 11 及以上（本机使用 JDK 21，`JAVA_HOME` 指向 `D:\java`）
- MySQL 8.x（本机 root 密码 `123456`）
- Tomcat 9（本机位于 `D:\java\env\tomcat9`）
- Maven 3.6+（用于构建）

## 快速开始

### 1. 初始化数据库

使用 MySQL 客户端执行初始化脚本（会自动重建 `libraryms` 库并写入演示数据）：

```bash
mysql -uroot -p123456 --default-character-set=utf8mb4 < sql/libraryms_init.sql
```

如需修改数据库连接，编辑 `src/main/resources/db.properties`。

### 2. 构建 war 包

```bash
mvn clean package -DskipTests
```

构建产物为 `target/LibraryWeb2026.war`。

### 3. 部署并启动

把 `target/libraryms.war` 复制到 Tomcat 的 `webapps` 目录，然后启动 Tomcat：

```bash
copy target\LibraryWeb2026.war D:\java\env\tomcat9\webapps\
D:\java\env\tomcat9\bin\startup.bat
```

或在 IDEA 中直接运行项目内置的运行配置 **「启动 图书馆管理系统 (Tomcat 9)」**（调用 `TomcatRunner`，自动启动 Tomcat 并打开浏览器）。

### 4. 访问系统

浏览器打开：`http://127.0.0.1:8080/LibraryWeb2026/login.html`

### 测试账号

| 账号 | 密码 | 角色 |
| ---- | ---- | ---- |
| admin | admin123 | 管理员 |
| operator | operator123 | 操作员 |
| student1 | student123 | 学生 |
| teacher | teacher123 | 教师 |

## 核心设计说明

### 1. 轻量级 ORM（JDBCTool）

通过运行时反射读取实体上的 `@Table`、`@Id`、`@Column` 注解，自动生成 `INSERT / UPDATE / DELETE / SELECT` 语句，并把 `ResultSet` 映射为实体对象。字段扫描递归遍历整个继承体系，因此 `User` 及其子类可以共用一张 `user` 表。

### 2. 单表继承

四种角色（管理员 / 操作员 / 教师 / 学生）映射到同一张 `user` 表，通过 `role` 字段区分，`UserFactory` 根据 `role` 实例化对应的子类，每个子类拥有各自的特有字段（如学生的 `grade`、教师的 `title`）。

### 3. Servlet 路由与统一响应

所有接口集中在 `@WebServlet("/api/*")` 下，`BaseServlet` 统一封装 JSON 响应、参数读取、登录态与角色校验；`Result` 提供 `{success, message, data}` 统一结构。`AuthFilter` 拦截 `/api/*`，除白名单外强制登录，`EncodingFilter` 统一 UTF-8 编码。

### 4. 事务处理

借书、还书、添加图书、副本增删、推荐审阅等涉及多表写入的操作均使用 JDBC 事务（`setAutoCommit(false)` + `commit/rollback`），保证库存与副本数据一致。
