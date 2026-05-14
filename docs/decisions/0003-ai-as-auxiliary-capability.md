# ADR-0003: AI 作为可关闭的辅助能力，而不是核心依赖

- Status: Accepted
- Date: 2026-05-14
- Related: `../requirements.md`, `../architecture.md`, `../api.md`

## Context

FixLedger 的核心价值是家庭设备生命周期管理。AI 能减少录入和整理成本，但真实 AI Provider 会引入 API Key、网络稳定性、费用、隐私和模型输出不确定性。

如果 AI 变成核心依赖，系统会在无 Key、无网络或模型失败时无法完成设备、保修、耗材、维修和提醒等主流程，这不符合项目定位。

## Decision

AI 只作为辅助能力，统一通过 `AiClient` 或 AI Service 调用。默认支持 Mock Provider，真实 Provider 通过配置选择，并且可以完全关闭。

AI 使用边界：

- 可以辅助发票文本提取、故障排查建议、维修总结和保养建议。
- 不允许直接修改用户核心数据，AI 结果必须由用户确认。
- 不允许发送密码、JWT、完整手机号、身份证号等敏感信息。
- Prompt 模板放在 `resources/prompts/`，不在业务代码中散落长 Prompt。
- AI 失败不能影响设备创建、维修保存、提醒生成等核心流程。

## Consequences

### Positive

- 面试时可以讲清 AI Client 抽象、Mock 策略、失败兜底和隐私边界。
- 无真实 API Key 时项目仍能启动、测试和演示。
- 后续接入 OpenAI-compatible Provider 或 Spring AI 时不需要重写业务模块。

### Trade-offs

- Mock AI 的演示效果有限，不代表真实模型能力。
- AI 结果需要用户确认，交互步骤比全自动多，但能避免错误覆盖业务数据。
- 真实 Provider 的重试、限流、成本统计和异步任务队列仍属于后续增强。

## Verification

- `docs/requirements.md` 明确 AI 不是核心业务依赖。
- `docs/api.md` 将 AI 接口放在辅助模块，并保留 Mock 演示口径。
- `backend/src/main/resources/prompts/` 保存当前 Prompt 模板。
