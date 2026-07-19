# Spec: FixLedger 家庭设备生命周期管理系统

## Objective

FixLedger 要做的是一个面向家庭场景的设备生命周期管理工具，帮助用户管理家电、数码设备、网络设备等从购买、保修、耗材更换、维修到归档的全过程。

核心用户是家庭设备负责人、家庭成员、租房用户和数码爱好者。项目成功的标志不是“做出一套通用后台”，而是让用户能在一个入口里回答这些生活问题：

- 家里有哪些设备，分别放在哪里、什么时候买的、还值不值得修。
- 哪些设备即将过保，哪些耗材该换，哪些维修还没处理完。
- 发票、保修卡、说明书、维修单和售后截图能不能快速找到。
- AI 能否减少录入和整理成本，但不影响核心业务运行。

## Tech Stack

| 层级 | 技术 | 当前约定 |
| --- | --- | --- |
| 后端 | JDK 21 / Spring Boot 3.3.x / Spring Security / MyBatis Plus 3.5.x | 单应用、模块化分包、REST API |
| 数据 | MySQL 8 / Redis 7 | MySQL 保存业务事实，Redis 用于提醒去重、JWT 黑名单和首页刷新标记/缓存钩子 |
| 文件 | RustFS / S3 兼容对象存储 / 本地文件兜底 | Docker 默认 RustFS，测试环境默认本地文件 |
| AI | 自定义 AiClient / Mock / OpenAI-compatible | AI 可关闭、可 Mock、不能成为核心依赖 |
| 前端 | Vue 3 / TypeScript / Vite / Element Plus / Pinia / Vue Router / Axios / ECharts | 场景化家庭应用，不做泛后台模板 |
| 工程 | Maven / npm / Docker Compose / SpringDoc OpenAPI | 支持本地开发和 Docker 一键演示 |
| 文档 | `docs/` / `docs/decisions/` / `AGENTS.md` | 需求、架构、接口、数据库、UI、任务和 ADR 同步维护 |

## Commands

以下命令以项目根目录为执行起点；Windows PowerShell 本地开发可按需先设置 JDK 和 Maven 路径。

| 场景 | 命令 |
| --- | --- |
| Docker 构建并启动完整项目 | `docker compose up -d --build` |
| Docker 启动已有镜像 | `docker compose up -d` |
| 查看 Docker 服务状态 | `docker compose ps` |
| 查看后端日志 | `docker compose logs -f backend` |
| 查看前端日志 | `docker compose logs -f frontend` |
| 停止服务并保留数据卷 | `docker compose down` |
| 校验 Compose 配置 | `docker compose config --quiet` |
| 后端测试 | `cd backend; mvn test` |
| 后端本地启动 | `cd backend; mvn spring-boot:run` |
| 前端安装依赖 | `cd frontend; npm install` |
| 前端开发启动 | `cd frontend; npm run dev` |
| 前端类型检查和构建 | `cd frontend; npm run build` |

本机已知 JDK 与 Maven 路径可用于面试演示环境：

```powershell
$env:JAVA_HOME="D:\Software\Tools\Java tools\jdk\jdk-21.0.11"
$env:Path="$env:JAVA_HOME\bin;D:\Software\Tools\Java tools\Maven\apache-maven-3.9.15\bin;$env:Path"
cd D:\work\work_space\Project\FixLedger\backend
mvn test
```

## Project Structure

```text
FixLedger/
├── backend/                 # Spring Boot 后端单应用
│   ├── src/main/java/com/fixledger/common/          # 统一响应、异常、安全、配置、工具
│   ├── src/main/java/com/fixledger/infrastructure/  # Redis、文件、AI、定时任务等技术封装
│   ├── src/main/java/com/fixledger/modules/         # auth/family/asset/warranty/consumable 等业务模块
│   └── src/main/resources/                          # application、SQL、MyBatis XML、Prompt 模板
├── frontend/                # Vue3 + TypeScript 前端应用
│   └── src/                 # api、components、layouts、router、stores、styles、types、views
├── docs/                    # 需求、架构、接口、数据库、UI、任务和规格文档
│   └── decisions/           # ADR 架构决策记录
├── docker-compose.yml       # MySQL、Redis、RustFS、后端、前端编排
├── .env.example             # 环境变量示例，不放真实密钥
├── AGENTS.md                # 项目编码规范和边界
└── README.md                # 面向展示和运行的入口文档
```

## Code Style

后端遵守 Controller -> Service -> Mapper 分层。Controller 只做参数校验和调用 Service，返回 `Result<T>`；Service 负责事务、业务编排和状态流转；Mapper 只做数据访问。Entity 不直接返回给前端。

```java
@RestController
@RequestMapping("/api/families/{familyId}/devices")
class DeviceAssetController {
  private final DeviceAssetService deviceAssetService;

  @PostMapping
  public Result<DeviceDetailResponse> create(
      @PathVariable Long familyId,
      @Valid @RequestBody CreateDeviceRequest request) {
    return Result.success(deviceAssetService.create(familyId, request));
  }
}
```

前端页面使用 `<script setup lang="ts">`，页面只做展示和交互编排；接口请求集中在 `src/api/`，类型集中在 `src/types/`，复杂状态放入 Pinia Store 或 composable。

文档更新遵守“先补文档，再写代码”：需求变更更新 `requirements.md`，架构或依赖变更更新 `architecture.md` 和 ADR，接口变更更新 `api.md`，表结构变更更新 `database.md`，页面交互变更更新 `ui.md`，阶段计划和验证记录更新 `tasks.md`。

## Testing Strategy

| 层级 | 策略 | 重点 |
| --- | --- | --- |
| 后端单元/切片测试 | JUnit 5、Mockito、AssertJ、Spring Test | Service 业务规则、Controller 参数校验和权限拦截 |
| 后端集成测试 | `application-test.yml`、H2、本地文件存储、Mock AI | 不依赖真实 RustFS、真实 AI 或外部网络 |
| 前端验证 | `npm run build` | TypeScript 类型、路由、组件编译和 Vite 构建 |
| Docker 验证 | `docker compose config --quiet`、`docker compose up -d --build` | Compose 配置、服务健康检查、一键演示能力 |
| 手工冒烟 | 登录、设备、附件、保修、耗材、维修、提醒、AI Mock | 面试演示路径闭环 |

核心业务测试优先级：保修提醒、耗材更换后下次提醒日期、维修状态流转、提醒去重、附件鉴权、家庭空间数据隔离。

## Boundaries

### Always

- 始终围绕家庭设备生命周期管理设计功能。
- 始终先更新文档，再改接口、表结构、页面或实现。
- 始终通过 `FileStorageService`、`AiClient`、`RedisService` 等基础设施接口访问外部能力。
- 始终保证 AI 可关闭、可 Mock，AI 失败不影响设备、保修、耗材、维修和提醒主流程。
- 始终对带 `familyId` 的数据做家庭空间权限校验。
- 始终在任务记录中写清 Acceptance、Verify、Files，保证开发留痕。

### Ask First

- 新增第三方依赖、修改 Docker 基础镜像或引入真实外部服务。
- 修改核心表结构、主键策略、逻辑删除策略或已有接口契约。
- 从后端鉴权下载改为对象存储临时 URL 直连访问。
- 引入真实 AI Provider、OCR、邮件、Webhook、部署流水线、E2E 门禁或生产部署方案。
- 物理删除设备、附件、维修历史等生命周期数据。

### Never

- 不做商城、企业固定资产盘点系统、泛后台管理模板或以 AI 为主题的产品。
- 不直接返回 Entity 给前端，不在 Controller 写业务逻辑。
- 不在事务中调用 AI、对象存储、邮件、Webhook 等外部能力。
- 不硬编码密钥、Bucket、文件路径、Token、API Key 或真实账号密码。
- 不让 AI 结果自动覆盖用户数据，不把密码、JWT、完整手机号等敏感信息发送给 AI。

## Success Criteria

- Docker Compose 能一键启动前端、后端、MySQL、Redis 和 RustFS。
- 用户能完成注册登录、进入家庭空间、创建设备、上传凭证、维护保修/耗材/维修记录。
- 系统能定时或手动生成保修和耗材提醒，并用 Redis 做提醒去重。
- 我的家首页能展示家庭健康分、本周事项、家庭日历、房间概览和关键统计。
- RustFS 能保存上传文件；本地文件存储仍能支撑测试和兜底。
- AI Mock 可用，真实 Provider 未配置时系统仍可启动和演示。
- 家庭成员协作、操作日志、站内通知抽象和家庭 CSV 导出具备可演示、可测试的当前实现。
- `docs/`、`README.md`、`AGENTS.md` 与当前实现一致，重大决策有 ADR 留痕。

## Open Questions

- P25 设备二维码标签是否采用登录后详情页跳转，还是增加受限只读设备卡片。
- P26 OCR 与智能归档优先支持发票图片识别，还是优先支持说明书 PDF 文本解析。
- P27 移动端能力优先做响应式 Web/PWA，还是另起小程序端。
- P28 外部通知渠道优先接邮件、Webhook，还是先完善站内通知已读、归档和筛选体验。
- P29 是否需要把当前同步 CSV 导出升级为异步导出历史，触发阈值如何定义。
- P30 生产发布是否只做自托管 Docker 部署说明，还是进一步补云平台发布流水线。
