package com.example.backend.dict.service;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * 线程安全的会话注册表。
 *
 * 设计目标：
 *  - 支持高并发下的会话注册操作；
 *  - 提供准确的会话数量统计接口，供测试与监控使用；
 *  - 实现保持简单，作为演示用最小可用实现。
 */
@Component
public class SessionRegistry {

    /**
     * 使用 CopyOnWriteArrayList 保证并发写入与读取时的可见性与安全性。
     * 对于本示例场景，会话注册频率相对较低，读多写少，适合该数据结构。
     */
    private static final List<String> REGISTRY = new CopyOnWriteArrayList<>();

    /**
     * 注册一个用户会话。
     *
     * @param userId 用户标识
     */
    public void register(String userId) {
        REGISTRY.add(userId);
    }

    /**
     * 当前已注册会话数量。
     */
    public int getSessionCount() {
        return REGISTRY.size();
    }
}

