# 生产 TLS 证书目录

生产主机需要在此目录或 `TLS_CERTIFICATE_DIR` 指定目录放置：

- `fullchain.pem`：站点证书与中间证书链。
- `privkey.pem`：与站点证书匹配的私钥。

证书和私钥已被 `.gitignore` 排除，禁止提交到仓库。证书可以由 Certbot、云证书服务或部署平台
生成，并应由主机侧任务在到期前续期。续期后执行 `docker compose --env-file .env.production
-f docker-compose.prod.yml exec gateway nginx -s reload` 重新加载。
