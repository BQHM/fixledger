# Architecture Decision Records

本目录记录 FixLedger 的关键架构和产品取舍。ADR 只记录对后续开发有约束力的决定，不替代 `docs/architecture.md`、`docs/tasks.md` 和 `AGENTS.md`。

新增 ADR 的触发条件：

- 修改核心技术栈、部署方式或外部依赖。
- 修改认证、权限、数据隔离、文件存储、AI、通知等基础能力边界。
- 修改会影响面试讲解口径的产品定位或架构分层。
- 放弃一个看似合理但被明确排除的方案。

当前 ADR：

| ADR | 决策 | 状态 |
| --- | --- | --- |
| [0001](0001-core-stack-and-layering.md) | 核心技术栈与分层架构 | Accepted |
| [0002](0002-rustfs-file-storage.md) | Docker 默认使用 RustFS，业务通过 FileStorageService 抽象文件存储 | Accepted |
| [0003](0003-ai-as-auxiliary-capability.md) | AI 作为可关闭的辅助能力，而不是核心依赖 | Accepted |
| [0004](0004-household-scene-first-ui.md) | UI 以家庭场景优先，避免泛后台管理平台表达 | Accepted |
