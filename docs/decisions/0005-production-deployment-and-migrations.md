# ADR 0005：独立生产编排与版本化数据库迁移

## 状态

已接受。

## 背景

现有 `docker-compose.yml` 面向本地演示，默认启用 `dev` Profile、SQL 初始化、演示数据和多个宿主机
端口，并包含便于开发的示例凭据。直接把该文件用于生产，容易因变量遗漏而暴露数据库、缓存、
对象存储控制台或 Swagger，也无法可靠升级已有数据卷。

P30 需要在不改变本地一键演示体验的前提下，提供可验证、可备份和可回滚的生产部署路径。

## 决策

1. 新增独立 `docker-compose.prod.yml`，不使用覆盖文件继承本地编排。
2. 生产只公开 Nginx Gateway 的 80/443，其他服务只加入内部网络。
3. TLS 证书由部署主机只读挂载；仓库不保存证书、私钥或真实环境文件。
4. 后端使用 `prod` Profile，关闭 SQL 初始化和 Swagger，并校验关键凭据。
5. 生产数据库使用 Flyway 迁移；`schema.sql` 只保留给本地演示和测试。
6. 发布前以同一批次号备份 MySQL 与 RustFS，Redis 不作为业务恢复点。
7. 应用回滚和数据恢复分离：普通故障只回退镜像，确认数据损坏后才恢复备份。

## 原因

- 独立编排比 Compose 覆盖更容易审计最终公开端口和默认值。
- 单入口 HTTPS 能集中处理证书、安全头、上传限制和代理行为。
- Flyway 提供已执行版本记录，避免 `CREATE TABLE IF NOT EXISTS` 无法升级已有表的问题。
- 分离应用回滚与数据恢复可降低误覆盖新数据的风险。

## 影响

- 生产部署必须准备 `.env.production`、版本化应用镜像和 TLS 证书。
- 数据库变更以后必须新增迁移文件，不能只修改 `schema.sql`。
- 发布流程增加准备检查、备份和恢复演练，但能明确失败点和回退路径。
- 本地 `docker compose up` 行为保持不变，不要求开发者配置生产凭据。

## 参考

- Spring Boot Flyway：<https://docs.spring.io/spring-boot/how-to/data-initialization.html#howto.data-initialization.migration-tool.flyway>
- Spring Boot 优雅停机：<https://docs.spring.io/spring-boot/reference/web/graceful-shutdown.html>
- Docker Compose 变量插值：<https://docs.docker.com/compose/how-tos/environment-variables/variable-interpolation/>
- Nginx SSL 模块：<https://nginx.org/en/docs/http/ngx_http_ssl_module.html>
