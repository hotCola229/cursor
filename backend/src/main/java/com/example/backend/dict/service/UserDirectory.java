package com.example.backend.dict.service;

import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 用户目录：负责根据 userKey 查找用户展示名，并带有本地缓存。
 *
 * 线程安全修复点：
 *  - 使用 ConcurrentHashMap 代替 HashMap；
 *  - 使用 computeIfAbsent 保证「检查-加载-写入」的原子性，避免竞态条件。
 */
@Component
public class UserDirectory {

    /**
     * 本地缓存：key 为 "user:1001" 这种形式。
     */
    private final Map<String, String> localCache = new ConcurrentHashMap<>();

    /**
     * 模拟的后端存储。
     */
    private final Map<String, String> store = new ConcurrentHashMap<>();

    public UserDirectory() {
        // 初始化模拟数据，与测试中期望的 Alice / Bob 对应
        store.put("user:1001", "Alice");
        store.put("user:1002", "Bob");
    }

    /**
     * 根据 userKey 查找展示名。
     *
     * @param userKey 例如 "user:1001"
     * @return 展示名，可能为 null（未找到）
     */
    public String findDisplayName(String userKey) {
        // 使用 computeIfAbsent 保证原子性：不存在时再从 store 加载
        return localCache.computeIfAbsent(userKey, key -> store.get(key));
    }
}

