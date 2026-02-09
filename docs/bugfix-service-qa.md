## 缺陷修复 QA 文档：UserProfileService / RequestContext 线程上下文泄漏

### 1. 问题是什么 / 影响范围

**问：问题是什么？**

答：`UserProfileService.loadDisplayName` 在绑定请求上下文（traceId、userId、payload）后，如果业务逻辑抛出异常（例如用户不存在），不会执行上下文清理逻辑，导致当前线程的 `ThreadLocal` 中仍残留旧的 traceId / userId / payload 数据。在线程池复用场景下，后续请求可能在“脏上下文”下继续执行。

**问：影响范围有哪些？**

答：
- 所有通过 `UserProfileService.loadDisplayName` 查询用户展示名的调用链；
- 使用线程池（如 Web 线程池、业务线程池）时，后续请求可能读取到前一个请求的 traceId / userId；
- 残留的 `payload`（2MB 数组）在异常路径上未及时释放，会增加线程的峰值内存占用。

### 2. 如何复现（命令）

**问：如何稳定复现这个问题？**

答：
1. 运行 Maven 测试：
   - 在仓库根目录执行：`mvn test -pl backend -Dtest=UserProfileServiceTest#contextShouldBeClearedWhenExceptionThrown`
2. 在修复前版本中，若在测试中通过反射读取 `RequestContext`，会发现异常抛出后仍能读到 traceId / userId / payload，说明上下文未被清理。

### 3. 根因是什么（指向代码）

**问：根因具体在哪里？**

答：
- `UserProfileService.loadDisplayName` 中调用顺序为：
  - 先执行 `requestContext.bind(...)` 绑定上下文；
  - 然后执行用户查询逻辑并在用户不存在时抛出 `IllegalArgumentException`；
  - `requestContext.reset()` 在方法尾部直接调用，而不是放在 `finally` 中。
- 因此，当抛出异常时，`reset()` 根本不会执行，导致：
  - `RequestContext.LOCAL` 中仍持有包含 traceId / userId / payload 的 Map。

相关代码位置：
- 类：`com.example.backend.dict.service.UserProfileService`
- 方法：`loadDisplayName(String userId)`
- 以及：`com.example.backend.dict.service.RequestContext.reset()`

### 4. 怎么修（改动点）

**问：本次是如何修复的？**

答：
1. **确保异常场景也清理上下文**
   - 将 `UserProfileService.loadDisplayName` 中的上下文清理移动到 `finally` 代码块中：
     - 无论业务逻辑成功还是抛出异常，都会执行 `requestContext.reset()`。
2. **彻底释放线程本地上下文**
   - 将 `RequestContext.reset()` 实现从清空 Map 改为调用 `ThreadLocal.remove()`：
     - 原实现：只对 Map 调用 `clear()`，仍然在当前线程上保留一个空 Map 实例；
     - 新实现：直接 `LOCAL.remove()`，彻底移除当前线程绑定的上下文对象，减少线程长期存活时的内存占用，并避免潜在的线程池场景下上下文残留。

主要改动文件：
- `backend/src/main/java/com/example/backend/dict/service/UserProfileService.java`
- `backend/src/main/java/com/example/backend/dict/service/RequestContext.java`

### 5. 如何验证 / 回归

**问：如何验证修复有效且不引入新问题？**

答：
1. **回归用例验证**
   - 执行命令：`mvn test -pl backend -Dtest=UserProfileServiceTest`
   - 关注测试用例：
     - `contextShouldBeClearedWhenExceptionThrown`
   - 预期：
     - 测试通过，且在异常抛出后，通过 `RequestContext.read("traceId"/"userId"/"payload")` 均返回 `null`，说明上下文已被正确清理。
2. **全量测试回归**
   - 在仓库根目录执行：`mvn test`
   - 预期：
     - 所有模块测试全部通过，无新增失败用例。

### 6. 风险与监控建议

**问：本次修改可能带来哪些风险？**

答：
- `ThreadLocal.remove()` 会在每次调用后彻底移除上下文：
  - 依赖于 `UserProfileService.loadDisplayName` 执行完成后仍要读取上下文的代码（如果存在）将不再能读到 traceId / userId / payload；
  - 目前代码中未发现此类依赖，但后续开发需要注意不要在方法返回后再去读取该上下文。

**问：推荐的监控与后续观察点有哪些？**

答：
- 监控维度：
  - JVM 堆内存与线程数：观察在高并发 / 异常高发场景下，内存占用是否更加平稳；
  - 日志中的 traceId / userId 关联：确保不存在“一个请求的日志被打到另一个请求 traceId 下”的情况。
- 建议：
  - 在后续接入真实链路追踪体系（如 Sleuth、Logback MDC 等）时，继续遵循“绑定-业务-清理（finally 中执行）”的模式；
  - 对其它使用 `ThreadLocal` 的组件做一次排查，确保都在正确位置执行 `remove()`。

