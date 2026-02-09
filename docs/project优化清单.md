## Project 模块重构优化清单

### 一、背景与目标

- Project 模块提供项目的增删改查与分页检索等能力，已有对外接口路径、入参/出参结构以及统一返回体 `ApiResponse` 和 `ErrorCode`。
- 本次重构的核心目标是在 **不改变任何对外行为（接口路径、字段结构、错误码语义、业务规则与数据库含义）** 的前提下：
  - 明确 DTO / VO / Entity 的边界；
  - 抽取重复逻辑，降低样板代码；
  - 让分层职责更清晰（Controller 变薄，Service 更聚焦业务，Mapper 只做数据访问）；
  - 统一 Project 模块内的错误码与返回体构造方式，提升一致性。

---

### 二、必做优化项落地结果

#### 1. 统一 DTO / VO 边界

- **改动点与涉及文件**
  - 新增对外 VO：
    - `backend/src/main/java/com/example/backend/project/dto/ProjectVO.java`
  - Controller 签名从直接返回 Entity 改为返回 VO：
    - `backend/src/main/java/com/example/backend/project/controller/ProjectController.java`
      - `create`：`ApiResponse<Project>` → `ApiResponse<ProjectVO>`
      - `getById`：`ApiResponse<Project>` → `ApiResponse<ProjectVO>`
      - `pageList`：`ApiResponse<ProjectPageResponse<Project>>` → `ApiResponse<ProjectPageResponse<ProjectVO>>`
      - `update`：`ApiResponse<Project>` → `ApiResponse<ProjectVO>`
  - Entity 继续仅用于持久化与内部业务逻辑：
    - `backend/src/main/java/com/example/backend/project/entity/Project.java`

- **前后对比要点**
  - **之前**：
    - Controller 直接操作并返回 `Project` Entity，分页接口返回 `ProjectPageResponse<Project>`，对外暴露了持久化字段结构。
  - **现在**：
    - Controller 仅接收 `ProjectCreateRequest` / `ProjectUpdateRequest` 两个 Request DTO，返回 `ProjectVO` 或 `ProjectPageResponse<ProjectVO>` 作为 Response VO。
    - VO 字段与原先 JSON 结构对齐（包括 `id/name/owner/status/createdAt/updatedAt/deleted`），因此对外返回结构保持不变，只是内部通过 VO 进行封装。

#### 2. 抽取重复逻辑（映射 / 异常转换 / 日志钩子位）

- **改动点与涉及文件**
  - 新增统一映射工具类：
    - `backend/src/main/java/com/example/backend/project/dto/ProjectConverter.java`
      - `toEntity(ProjectCreateRequest)`：Request DTO → Entity
      - `applyUpdate(Project, ProjectUpdateRequest)`：更新 DTO → Entity
      - `toVO(Project)` / `toVOList(List<Project>)`：Entity → VO / 列表
      - `toPageResponse(Page<?> page, List<T> records)`：分页结果 → `ProjectPageResponse<T>`
  - Service 实现使用统一转换工具：
    - `backend/src/main/java/com/example/backend/project/service/impl/ProjectServiceImpl.java`
      - 创建、更新、查询、分页都通过 `ProjectConverter` 完成 Entity 与 DTO/VO 之间的转换。
  - 异常转换集中在 Service 层：
    - 同一处使用 `BusinessException(ErrorCode.PROJECT_NOT_FOUND)` 表达「项目不存在」，避免在多个 Controller 方法中重复判断。

- **前后对比要点**
  - **之前**：
    - Controller 中手写 `Project` 字段赋值和 `ProjectStatus.fromCode` 逻辑；
    - Controller 自己判断空值并抛 `BusinessException`。
  - **现在**：
    - 所有 DTO ↔ Entity ↔ VO 的映射逻辑集中在 `ProjectConverter`；
    - 「项目不存在」的异常判断集中在 `ProjectServiceImpl`，Controller 不再重复空值判断。

#### 3. 改善分层（Controller 变薄 / Service 表达业务 / Mapper 只做数据访问）

- **改动点与涉及文件**
  - Service 接口新增明确的业务方法：
    - `backend/src/main/java/com/example/backend/project/service/ProjectService.java`
      - `ProjectVO createProject(ProjectCreateRequest request);`
      - `ProjectVO getProjectDetail(Long id);`
      - `ProjectPageResponse<ProjectVO> pageProjects(Page<Project> page, String keyword);`
      - `ProjectVO updateProject(Long id, ProjectUpdateRequest request);`
      - `void deleteProject(Long id);`
  - Service 实现承担业务流程与查询条件构建：
    - `backend/src/main/java/com/example/backend/project/service/impl/ProjectServiceImpl.java`
      - 在 Service 内部构建 `LambdaQueryWrapper<Project>` 和 `Page<Project>`，根据 keyword 拼装查询条件；
      - 统一进行「存在性校验 → 抛业务异常 → 映射 VO」等流程。
  - Controller 仅做参数接收与调用：
    - `backend/src/main/java/com/example/backend/project/controller/ProjectController.java`
      - 不再直接创建 `Project` 或拼装分页对象，只负责：
        - 参数接收与校验（`@RequestBody @Validated` + `@Min/@Max/@Size` 等）；
        - 调用 Service 业务方法；
        - 使用 `ApiResponse.ok(...)` 返回。

- **前后对比要点**
  - **之前**：
    - Controller 掌握了查询条件、Entity 构造、异常抛出等多种职责；
    - Service 只是 MyBatis-Plus 的默认实现，业务逻辑非常薄。
  - **现在**：
    - Controller 变为「HTTP 适配层」：只管参数与响应；
    - Service 负责真实业务流程与数据访问编排；
    - Mapper 继续只负责 CRUD（`ProjectMapper` 未增加额外职责）。

#### 4. 统一错误码与返回体构造

- **改动点与涉及文件**
  - 错误码仍统一集中在：
    - `backend/src/main/java/com/example/backend/common/ErrorCode.java`
  - 返回体构造继续复用统一封装：
    - `backend/src/main/java/com/example/backend/common/ApiResponse.java`
      - 成功：`ApiResponse.ok(...)`
      - 失败：`ApiResponse.fail(code, message)` / `ApiResponse.fail(code, message, data)`
  - Project 模块内的错误处理逻辑一律通过：
    - 业务异常：`BusinessException(ErrorCode.PROJECT_NOT_FOUND)` → 全局异常处理器 `GlobalExceptionHandler` → `ApiResponse.fail(...)`
    - 参数校验错误：`GlobalExceptionHandler.handleValidationException` 返回 `ErrorCode.PARAM_VALID_ERROR`。

- **前后对比要点**
  - **之前**：
    - Controller 内手动抛 `BusinessException(ErrorCode.PROJECT_NOT_FOUND)`，其它错误交由全局异常处理；
    - 成功响应统一使用 `ApiResponse.ok(...)`，但错误构造分散在 Controller 与 Service。
  - **现在**：
    - 所有 Project 相关的「项目不存在」错误都由 Service 抛出统一的 `BusinessException(ErrorCode.PROJECT_NOT_FOUND)`；
    - Controller 全部通过 `ApiResponse.ok(...)` 构造成功响应，失败统一走全局异常处理逻辑；
    - 错误码和返回体的定义继续集中在 `ErrorCode` 与 `ApiResponse` 中，没有新增 magic number。

---

### 三、回归与测试覆盖说明

- **新增 / 更新的测试用例（≥3 个）**
  - 文件：`backend/src/test/java/com/example/backend/project/controller/ProjectControllerTest.java`
    1. `createProjectSuccess`（**更新**）
       - 验证点：
         - `$.code == 0`
         - `$.data.name == "项目A"`
         - `$.data.owner == "张三"`
       - 说明：确保通过 VO 返回的数据字段与原有语义一致。
    2. `createProjectStatusInvalid`（**保持**）
       - 验证点：
         - `$.code == 40001`（`PARAM_VALID_ERROR`）
         - `$.message` 为校验错误信息字符串。
       - 说明：参数校验失败路径未受重构影响，仍走统一异常处理。
    3. `getDeletedOrNotExistProject`（**更新**）
       - 通过 `ProjectMapper` 插入并逻辑删除一条项目记录；
       - 请求 `GET /api/projects/{id}`，期望：
         - `$.code == 40401`（`PROJECT_NOT_FOUND`）。
       - 说明：逻辑删除后查询仍被视为「不存在」，且由 Service 统一抛业务异常。
    4. `pageProjectsWithKeyword`（**新增**）
       - 插入两条项目数据，其中一条名称包含「关键」；
       - 请求 `GET /api/projects?page=1&size=10&keyword=关键`，期望：
         - `$.code == 0`
         - `$.data.total == 1`
         - `$.data.records[0].name == "关键项目1"`
       - 说明：验证分页与 keyword 查询逻辑仍然生效，并通过 VO 返回结果。

- **命令**
  - 在仓库根目录执行：
    - `mvn test`
  - 预期：
    - 所有模块测试均通过（含 Project 模块相关用例）。

---

### 四、重构收益（可核验、具体 3-5 条）

1. **DTO / VO / Entity 边界清晰，避免直接暴露持久化模型**
   - 现在 Controller 只使用 `ProjectCreateRequest` / `ProjectUpdateRequest` 和 `ProjectVO`，不会再直接返回 `Project` Entity。
   - 后续如需调整数据库字段（例如新增内部字段）时，只需在 VO 中按需暴露，不会破坏已有调用方。

2. **映射逻辑集中到 `ProjectConverter`，消除了重复样板代码**
   - 创建和更新接口不再在 Controller / Service 中重复写字段拷贝与状态码转换逻辑，统一在 `ProjectConverter` 中维护。
   - 当后续增加字段时，仅需更新单一转换类即可，降低维护成本和出错概率。

3. **分层职责更清晰，业务含义集中在 Service**
   - Controller 只负责 HTTP 协议相关的工作（参数接收、校验、调用 Service、返回统一响应）；
   - Service 集中表达业务流程（存在性校验、分页条件构建、逻辑删除等），便于阅读与后续扩展；
   - Mapper 仍然保持单一责任，只做数据库访问。

4. **错误码与返回体构造路径统一，便于排查与扩展**
   - 所有 Project 相关的业务错误通过 `BusinessException` + `ErrorCode` 统一定义；
   - 结合 `GlobalExceptionHandler` 与 `ApiResponse`，前端可以稳定依赖统一的错误结构进行处理。

5. **测试覆盖更聚焦关键路径，重构风险可控**
   - 针对「创建成功、参数校验失败、逻辑删除后查询、分页 keyword 生效」四个关键路径均有显式测试覆盖；
   - 重构后通过 `mvn test` 一键回归，可以较为确信 Project 模块对外行为未发生回退。

