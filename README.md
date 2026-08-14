# flood-traffic-platform — 内涝交通仿真任务中台

城市内涝—交通联合仿真系统的 Java 后端：负责任务调度、参数校验与转发、
PostgreSQL 持久化、仿真进度与结果查询，是前端与 Python 算法引擎之间的中枢。

## 系统架构

前端 corridor-3d(:5173) → 本服务(:9999) → 算法引擎 swmm-wca2d(:8000)

本服务职责：接收仿真参数 → 落库生成任务记录 → RestTemplate 转发至算法引擎 →
接收回调更新任务状态 → 供前端轮询进度与结果。

## 技术栈

Java 8 · Spring Boot 2.6 · MyBatis-Plus 3.5 · PostgreSQL · Maven · Lombok

## 主要功能

- 内涝仿真任务：创建/进度/结果（节点溢流、管段满水度）/断点续跑/级联删除
- 交通仿真任务：创建（OD、多车型、信号配时）/进度/回调/断点续跑/OD 缓存查询
- 数据资产管理：转发算法引擎的资产树、预览、删除接口

## 本地运行

```bash
# 1. 初始化数据库（PostgreSQL）
createdb flood_traffic_db
psql -d flood_traffic_db -f db/schema.sql

# 2. 配置环境变量（或参考 application.yml.example）
#    DB_HOST / DB_PORT / DB_NAME / DB_USERNAME / DB_PASSWORD
#    PYTHON_BASE_URL / PYTHON_PROJECT_DIR

# 3. 启动（端口 9999）
mvn spring-boot:run
```

## 目录结构

- `src/main/java/.../controller/`   REST 接口层（内涝/交通/数据管理）
- `src/main/java/.../service/`      业务层（含 Python 引擎调用）
- `src/main/java/.../dto|entity|mapper/`  参数对象 / 实体 / MyBatis-Plus Mapper
- `db/schema.sql`                   数据库初始化脚本
