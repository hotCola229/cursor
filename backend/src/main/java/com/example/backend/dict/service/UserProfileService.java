package com.example.backend.dict.service;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class UserProfileService {

    private final RequestContext requestContext = new RequestContext();
    private final Map<String, String> store = new HashMap<>();

    public UserProfileService() {
        store.put("1001", "Alice");
        store.put("1002", "Bob");
    }

    /**
     * 获取用户展示名：方法内部会在当前线程绑定 traceId、userId 等上下文信息，
     * 以便日志/审计/埋点等后续环节读取使用。
     */
    public String loadDisplayName(String userId) {
        requestContext.bind(UUID.randomUUID().toString(), userId);
        try {
            // 执行业务逻辑（例如：参数校验、权限检查、查询用户信息、组装返回结果、记录日志等）
            String name = store.get(userId);
            if (name == null) {
                throw new IllegalArgumentException("user not found: " + userId);
            }
            return name;
        } finally {
            // 确保无论业务逻辑是否抛出异常，都能清理线程上下文，避免线程复用时出现“脏上下文”
            requestContext.reset();
        }
    }
}
