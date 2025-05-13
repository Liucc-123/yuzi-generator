# 定制化项目生成器

> 作者：程序员水冠

深入业务场景的企业级硬项目，基于 React + Spring Boot + Picocli + 对象存储 COS的 代码生成器共享平台 。

开发者可以在平台上制作并发布代码生成器，用户可以搜索、下载、在线使用代码生成器，管理员可以集中管理所有用户和生成器。

在线体验：http://175.178.223.245

# 项目简介

该项目经历三个循序渐进的阶段实现，每一个阶段都可以是一个独立运行的项目，依次是**基于命令行的本地代码生成器**、**代码生成器制作工具**、**在线代码生成器平台**。

## 项目展示

主页（代码生成器搜索列表）：

<img width="1470" alt="image-20250513160122819" src="https://github.com/user-attachments/assets/0e9099f6-02f0-4c04-8692-f4dac7902001" />


代码生成器创建页（分步表单）：

<img width="1470" alt="image-20250513160137579" src="https://github.com/user-attachments/assets/c51e6cd7-fae4-4d25-a729-936efff2186a" />


代码生成器创建页（复杂嵌套动态表单）：

<img width="1470" alt="image-20250513160206983" src="https://github.com/user-attachments/assets/55996e06-00a0-4bbb-968a-6cf6bb8165c3" />


在线上传和制作代码生成器：

<img width="1470" alt="image-20250513160224724" src="https://github.com/user-attachments/assets/9db5dd68-6071-4fa8-8257-0bae7edc6db2" />


代码生成器详情页：

<img width="1470" alt="image-20250513160249807" src="https://github.com/user-attachments/assets/12d294b0-dab2-4699-8685-ceec53f1ad4c" />


在线使用代码生成器：

<img width="1470" alt="image-20250513160304744" src="https://github.com/user-attachments/assets/3b0bc8e9-7e3f-4522-9b97-536a67fb3bce" />


# 技术选型

## 前端

- React 18 开发框架
- Ant Design Pro 脚手架（万用前端模板）
- Ant Design 组件库
- ⭐️ Ant Design Procomponents 高级组件
- OpenAPI 代码生成
- 前端工程化：ESLint + Prettier + TypeScript
- ⭐️ 前端通用文件上传下载

## 后端

- Java Spring Boot 开发框架（万用后端模板）
- MySQL 数据库
- MyBatis-Plus 及 MyBatis X 自动生成
- Maven 自动打包
- ⭐️ Picocli Java 命令行应用开发
- ⭐️ FreeMarker 模板引擎
- ⭐️ Caffeine + Redis 多级缓存
- ⭐️ XXL-JOB 分布式任务调度系统
- ⭐️ 腾讯云 COS 对象存储
- ⭐️ 多种设计模式
  - 命令模式
  - 模板方法模式
  - 双检锁单例模式
- ⭐️ 多角度项目优化
  - 可移植性、健壮性、可扩展性、圈复杂度优化
  - 7 种性能优化思路和实践
  - 7 种存储优化思路和实践
- ⭐️ Vert.x 响应式编程
- ⭐️ JMeter 压力测试
- Hutool 工具库和 Lambda 表达式编程

## 业务流程

制作工具、代码生成器和目标代码的关系如下图所示：

<img width="553" alt="image-20250513153037579" src="https://github.com/user-attachments/assets/4444a501-748d-490e-bce7-32903482abbb" />

核心原理：

![image-20250513155825804](https://github.com/user-attachments/assets/444671b6-547c-440a-87f2-8f40b5bff6b3)


开发者基于原始代码文件，通过模板制作工具快速生成动态模板 FTL 文件和元信息配置文件meta.json；

开发者通过基于 FTL 动态模板文件和 meta.json，通过生成器制作工具 `maker`  生成`代码生成器`；

用户拿到代码生成器，基于表单提示信息填写数据（模型参数），生成器基于模型参数生成目标代码。

# 项目上线

* 轻量应用服务器
* 宝塔 Linux 面板
* Nginx 反向代理
