# RedNote-Backend

小红书的后端部分。

前端地址：[RedNote-Frontend](https://github.com/BeAWitch/RedNote-Frontend)。

> 本项目为软件工程专业的毕业设计，水平有限，不嫌弃可以直接拿去用。

## 项目概览

RedNote-Backend 是一个受小红书启发的基于微服务架构的社交媒体平台后端。它采用现代 Java 技术构建，提供了一个功能全面的社交网络平台，包括内容创作、社交互动、实时消息、AI 辅助和个性化推荐等功能。

AI 生成的项目文档：

- 中文：

  - [前端 | RedNote-Frontend | Zread](https://zread.ai/BeAWitch/RedNote-Frontend)

  - [后端 | RedNote-Backend | Zread](https://zread.ai/BeAWitch/RedNote-Backend)

- 英文：
  - [前端 | RedNote-Frontend | DeepWiki](https://deepwiki.com/BeAWitch/RedNote-Frontend)
  - [后端 | RedNote-Backend | DeepWiki](https://deepwiki.com/BeAWitch/RedNote-Backend)

### 技术栈

**核心框架：**

| 组件                     | 版本           | 用途                                             |
| ------------------------ | -------------- | ------------------------------------------------ |
| **Spring Boot**          | 3.4.13         | 应用框架和自动配置。                             |
| **Spring Cloud**         | 2024.0.1       | 微服务基础设施（Gateway、Feign、LoadBalancer）。 |
| **Spring Cloud Alibaba** | 2023.0.1.2     | 服务网格组件（Nacos、Seata）。                   |
| **Spring AI**            | 1.0.0-SNAPSHOT | 用于智谱 AI 的 AI 集成。                         |

**数据存储：**

| 组件              | 版本                      | 用途                  |
| ----------------- | ------------------------- | --------------------- |
| **MySQL**         | 8.4.0                     | 主关系数据库。        |
| **MyBatis Plus**  | 3.5.7                     | ORM 框架和 SQL 增强。 |
| **Redis**         | Spring Data Redis         | 分布式缓存。          |
| **Elasticsearch** | Spring Data Elasticsearch | 笔记搜索。            |

**消息传递与事件处理**

| 组件          | 版本             | 用途                 |
| ------------- | ---------------- | -------------------- |
| **RabbitMQ**  | Spring AMQP      | 消息队列。           |
| **WebSocket** | Spring WebSocket | 实时通信、消息通知。 |

**AI 与机器学习**

| 组件                 | 版本            | 用途               |
| -------------------- | --------------- | ------------------ |
| **ZhiPu AI Starter** | Spring AI ZhiPu | 内容生成和分析。   |
| **Apache Mahout**    | 0.9             | 协同过滤推荐算法。 |

**文件存储**

| 组件           | 版本   | 用途       |
| -------------- | ------ | ---------- |
| **Aliyun OSS** | 3.15.0 | 文件存储。 |

**文档**

| 工具         | 版本    | 用途                               |
| ------------ | ------- | ---------------------------------- |
| **Knife4j**  | 4.5.0   | 增强的 Swagger UI。                |
| **Hutool**   | 5.8.27  | 综合的 Java 工具库，用于常见操作。 |
| **Lombok**   | 1.18.32 | 代码生成。                         |
| **Fastjson** | 2.0.43  | 高性能 JSON 序列化。               |

### 模块介绍

| 模块                       | 功能                                              |
| -------------------------- | ------------------------------------------------- |
| **rednote-gateway**        | 网关，所有请求的单一入口点，处理路由、负载均衡。  |
| **rednote-common**         | 服务中使用的共享工具、配置等。                    |
| **rednote-user**           | 用户、权限管理。                                  |
| **rednote-note**           | 笔记、标签、分类管理。                            |
| **rednote-interaction**    | 社交功能，包括点赞、评论、关注和实时聊天。        |
| **rednote-search**         | 笔记搜索、搜索记录管理。                          |
| **rednote-recommendation** | 使用基于物品协同过滤（ItemCF）的个性化内容推荐。  |
| **rednote-ai**             | AI 功能，目前只实现了简单的问答功能，前端未同步。 |
| **rednote-oss**            | 图片、视频和其他媒体的文件上传和管理。            |

#### 组织结构

```shell
rednote-backend/
├── rednote-common/               # 共享工具和配置
├── rednote-gateway/              # 网关
├── rednote-user/                 # 用户域
│   ├── rednote-user-api/         # 用户 API 接口
│   └── rednote-user-service/     # 用户服务实现
├── rednote-note/                 # 笔记内容域
│   ├── rednote-note-api/         # 笔记 API 接口
│   └── rednote-note-service/     # 笔记服务实现
├── rednote-interaction/          # 社交交互域
│   ├── rednote-interaction-api/  # 交互 API 接口
│   └── rednote-interaction-service/ # 交互服务实现
├── rednote-search/               # 搜索域
│   ├── rednote-search-api/       # 搜索 API 接口
│   └── rednote-search-service/   # 搜索服务实现
├── rednote-recommendation/       # 推荐域
│   ├── rednote-recommendation-api/ # 推荐 API 接口
│   └── rednote-recommendation-service/ # 推荐服务实现
├── rednote-ai/                   # AI 能力域
│   ├── rednote-ai-api/           # AI API 接口
│   └── rednote-ai-service/       # AI 服务实现
├── rednote-oss/                  # 对象存储域
│   └── rednote-oss-service/      # OSS 服务实现
└── resources/                    # 基础设施配置
    ├── nacos/                    # Nacos 配置文件
    └── sql/                      # 数据库脚本
```

