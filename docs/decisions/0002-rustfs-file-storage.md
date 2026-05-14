# ADR-0002: Docker 默认使用 RustFS，业务通过 FileStorageService 抽象文件存储

- Status: Accepted
- Date: 2026-05-14
- Related: `../architecture.md`, `../database.md`, `../api.md`, `../../docker-compose.yml`

## Context

FixLedger 需要保存发票图片、保修卡、说明书 PDF、维修单和售后截图。第一版本地文件存储能快速完成上传、下载和鉴权闭环，但面试演示时更希望体现对象存储实践。

候选方案：

- 本地文件系统：简单、适合测试，但不适合展示真实部署能力。
- MinIO：成熟常见，但用户当前已有 RustFS 镜像，且 RustFS 同样兼容 S3 API。
- RustFS：适合 Docker 本地演示，可通过 S3 SDK 接入。

## Decision

保留 `FileStorageService` 作为业务层唯一文件存储接口。Docker 默认使用 RustFS，测试环境和兜底场景继续使用本地文件存储。

实现约束：

- `storage-type=rustfs`、`s3` 或 `minio` 时启用 S3 兼容实现。
- `storage-type=local` 时启用本地文件实现。
- MySQL 的 `fl_file_resource.storage_path` 保存对象 Key，不保存公开访问 URL。
- 下载附件必须先经过后端家庭空间权限校验，再由后端读取对象流返回。
- Bucket、Endpoint、Access Key、Secret Key 只来自配置或环境变量，不硬编码到代码。

## Consequences

### Positive

- 面试时可以讲清“元数据入库、文件内容进对象存储、后端鉴权下载”的完整设计。
- 业务代码不依赖 RustFS 私有协议，后续可切换 MinIO 或其他 S3 兼容服务。
- 测试环境不依赖 Docker RustFS，降低自动化测试成本。

### Trade-offs

- Docker 首次构建和启动需要额外拉取 RustFS 镜像。
- 当前不直接暴露对象存储临时 URL，文件预览体验后续还可以增强。
- S3 SDK 增加了后端依赖，需要在依赖升级和安全审计时一并关注。

## Verification

- `docker-compose.yml` 编排 `rustfs` 服务，并让后端默认使用 `FILE_STORAGE_TYPE=rustfs`。
- `FileStorageServiceConditionTest` 覆盖 `local`、`rustfs`、`minio` 的 Bean 选择。
- `docs/api.md` 说明上传/下载接口不因 RustFS 接入而改变。
- `docs/database.md` 说明 `fl_file_resource` 保存元数据和对象 Key。
