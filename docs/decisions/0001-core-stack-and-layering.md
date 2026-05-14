# ADR-0001: 核心技术栈与分层架构

- Status: Accepted
- Date: 2026-05-14
- Related: `../spec.md`, `../architecture.md`, `../../AGENTS.md`

## Context

FixLedger 是面试项目，也是真实家庭场景工具。它需要覆盖 Java Web 项目常见能力，同时不能为了展示技术而扩大成企业资产管理平台。

项目需要满足：

- 前后端分离，便于展示接口设计和页面交互。
- 后端结构清晰，能讲清 Controller、Service、Mapper 和基础设施层职责。
- 数据模型稳定，适合保修、耗材、维修、提醒等关系型业务。
- 本地和 Docker 都能运行，便于面试演示。

## Decision

采用 JDK 21 + Spring Boot 3.3.x + MyBatis Plus + MySQL 8 + Redis 7 + Vue 3 + TypeScript + Vite + Element Plus。

后端保持单应用模块化分包，不拆微服务。核心调用方向为：

```text
Controller -> Service -> Mapper
                  ↕
          Infrastructure
```

业务模块放在 `backend/src/main/java/com/fixledger/modules/`，Redis、文件、AI、通知、定时任务等技术能力放在 `infrastructure/`，通用响应、异常、安全、配置放在 `common/`。

## Consequences

### Positive

- 技术栈贴近常见 Java 面试项目，方便讲解认证、权限、事务、文件、缓存和定时任务。
- 单应用避免微服务复杂度，适合个人项目快速完成闭环。
- MyBatis Plus 降低 CRUD 成本，复杂查询仍可通过 XML 或 Wrapper 明确表达。
- Vue 3 + TypeScript 让前端具备类型约束和工程化展示价值。

### Trade-offs

- 单应用无法展示服务治理、注册中心、链路追踪等微服务能力，但这些不是家庭工具 MVP 的核心。
- Element Plus 容易显得像管理后台，因此 UI 文案和布局必须继续保持家庭场景化。
- MyBatis Plus 需要约束 Entity 暴露和查询边界，避免把数据库结构直接泄露给前端。

## Verification

- `backend/pom.xml` 固定 Spring Boot 3.3.x、Java 21、MyBatis Plus、SpringDoc 和 AWS S3 SDK 等版本。
- `frontend/package.json` 固定 Vue 3、Vite、TypeScript、Element Plus、Pinia、Vue Router 和 Axios。
- `docs/architecture.md` 与 `AGENTS.md` 共同约束分层和模块边界。
