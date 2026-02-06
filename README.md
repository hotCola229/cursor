# Vibe Coding Project

Spring Boot 2.7.x (Java 8) 多模块项目，提供后端服务模块。

## 项目结构

```
.
├── pom.xml                    # 根 pom（多模块父 pom）
├── README.md                 # 项目说明文档
├── TASKS.md                  # 待办清单
└── backend                   # 后端服务模块
    ├── pom.xml
    └── src
        ├── main
        │   ├── java/com/example/backend/
        │   │   ├── BackendApplication.java
        │   │   └── controller/HealthController.java
        │   └── resources/application.yml
        └── test
            └── java/com/example/backend/controller/HealthControllerTest.java
```

## 构建与测试

### 运行测试

```bash
mvn test
```

**期望结果**：所有测试通过，无错误。

### 启动应用

```bash
mvn -pl backend spring-boot:run
```

或者：

```bash
cd backend
mvn spring-boot:run
```

**期望结果**：应用启动成功，日志显示类似：
```
Started BackendApplication in X.XXX seconds
```

应用将在 `http://localhost:8080` 启动。

## 验证健康检查

### 验证 Actuator 健康检查

```bash
curl http://localhost:8080/actuator/health
```

**期望结果**：
```json
{"status":"UP"}
```

### 验证自定义健康检查

```bash
curl http://localhost:8080/health
```

**期望结果**：
```json
{"status":"UP"}
```

## 技术栈

- Java 8
- Spring Boot 2.7.18
- Maven 多模块构建
- Spring Boot Actuator（健康检查）
- Spring Boot Test（测试框架）

## 模块说明

- **backend**: 后端服务模块，提供 REST API 和健康检查接口
