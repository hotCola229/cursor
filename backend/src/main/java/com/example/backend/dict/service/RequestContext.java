package com.example.backend.dict.service;

import java.util.HashMap;
import java.util.Map;

public class RequestContext {

    private static final ThreadLocal<Map<String, Object>> LOCAL = new ThreadLocal<>();

    public void bind(String traceId, String userId) {
        Map<String, Object> ctx = LOCAL.get();
        if (ctx == null) {
            ctx = new HashMap<>();
            LOCAL.set(ctx);
        }
        ctx.put("traceId", traceId);
        ctx.put("userId", userId);
        ctx.put("payload", new byte[2 * 1024 * 1024]);
    }

    public Object read(String key) {
        Map<String, Object> ctx = LOCAL.get();
        return ctx == null ? null : ctx.get(key);
    }

    public void reset() {
        // 使用 remove 而不是仅仅清空 Map，确保：
        // 1. 彻底移除当前线程绑定的上下文，避免在线程池场景下残留；
        // 2. 释放整个 Map 对象，减少长生命周期线程中的内存占用。
        LOCAL.remove();
    }
}
