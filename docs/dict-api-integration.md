# 字典查询接口集成说明

## 1. application.yml 配置项说明

| 配置 key | 说明 | 示例值 | 环境变量覆盖 |
|----------|------|--------|--------------|
| `dict.client.base-url` | 第三方 baseUrl | `http://172.20.4.32:18022/` | `DICT_BASE_URL` |
| `dict.client.app-key` | 第三方 AppKey | `28dc15bb-2e2c-45e4-b435-525853f69173` | `DICT_APP_KEY` |
| `dict.client.app-secret` | 第三方 AppSecret | `0e27fdf2820802cdea8e0eb22b695c93` | `DICT_APP_SECRET` |
| `dict.rest.connect-timeout` | 连接超时(ms) | `3000` | `DICT_CONNECT_TIMEOUT` |
| `dict.rest.read-timeout` | 读取超时(ms) | `5000` | `DICT_READ_TIMEOUT` |
| `dict.retry.max-attempts` | 最大尝试次数 | `3` | - |
| `dict.retry.initial-delay` | 退避初始延迟(ms) | `100` | `DICT_RETRY_INITIAL_DELAY` |
| `dict.retry.multiplier` | 退避倍数 | `2.0` | `DICT_RETRY_MULTIPLIER` |
| `dict.retry.max-delay` | 退避最大延迟(ms) | `2000` | `DICT_RETRY_MAX_DELAY` |
| `dict.rate-limit.capacity` | 桶容量 | `100` | `DICT_RATE_LIMIT_CAPACITY` |
| `dict.rate-limit.refill-tokens` | 每周期补充令牌数 | `100` | `DICT_RATE_LIMIT_REFILL_TOKENS` |
| `dict.rate-limit.refill-duration` | 补充周期 | `1s` | `DICT_RATE_LIMIT_REFILL_DURATION` |

## 2. 验证步骤

### 2.1 curl 成功示例

```bash
curl -s "http://localhost:8080/api/dict/query?pageNum=1&pageSize=10&dictType=job_type" \
  -H "X-Trace-Id: my-trace-123"
```

**期望结果：** `code=0`，`message=ok`，`data` 为第三方返回的 JSON（如 total、data、totalPage 等）。

### 2.2 curl 失败示例（参数校验）

```bash
curl -s "http://localhost:8080/api/dict/query?pageNum=0&pageSize=10&dictType=job_type"
```

**期望结果：** `code=40001`，`message` 为中文校验信息（如 "pageNum 必须大于等于 1"）。

### 2.3 按 trace_id 查询日志表

```sql
SELECT * FROM test.external_call_log WHERE trace_id = 'my-trace-123' ORDER BY created_at;
```

## 3. 关键文件清单（新增/修改路径）

- **根目录**
  - 修改：`db.sql`（新增 `external_call_log` 表）
- **配置与依赖**
  - 修改：`backend/pom.xml`（spring-retry、bucket4j、wiremock）
  - 修改：`backend/src/main/resources/application.yml`（dict 相关配置）
- **签名与配置**
  - 新增：`backend/src/main/java/com/example/backend/dict/util/ThirdPartySignatureUtil.java`
  - 新增：`backend/src/main/java/com/example/backend/dict/config/DictClientProperties.java`
  - 新增：`backend/src/main/java/com/example/backend/dict/config/DictRestTemplateConfig.java`
  - 新增：`backend/src/main/java/com/example/backend/dict/config/DictRetryConfig.java`
  - 新增：`backend/src/main/java/com/example/backend/dict/config/DictRateLimitConfig.java`
- **实体与 Mapper**
  - 新增：`backend/src/main/java/com/example/backend/dict/entity/ExternalCallLog.java`
  - 新增：`backend/src/main/java/com/example/backend/dict/mapper/ExternalCallLogMapper.java`
- **服务与控制器**
  - 新增：`backend/src/main/java/com/example/backend/dict/service/DictRetryableException.java`
  - 新增：`backend/src/main/java/com/example/backend/dict/service/DictRetryListener.java`
  - 新增：`backend/src/main/java/com/example/backend/dict/service/DictClientService.java`
  - 新增：`backend/src/main/java/com/example/backend/dict/controller/DictController.java`
- **全局错误码**
  - 修改：`backend/src/main/java/com/example/backend/common/ErrorCode.java`（RATE_LIMIT）
- **Mapper 扫描**
  - 修改：`backend/src/main/java/com/example/backend/config/MybatisPlusConfig.java`（增加 dict.mapper）
- **测试**
  - 新增：`backend/src/test/java/com/example/backend/dict/controller/DictControllerTest.java`（WireMock 成功/500 重试）
