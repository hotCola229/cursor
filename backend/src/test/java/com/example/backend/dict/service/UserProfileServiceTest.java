package com.example.backend.dict.service;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class UserProfileServiceTest {

    @Test
    void contextShouldBeClearedWhenExceptionThrown() throws Exception {
        UserProfileService service = new UserProfileService();

        // 触发不存在用户场景，预期抛出 IllegalArgumentException
        assertThrows(IllegalArgumentException.class, () -> service.loadDisplayName("9999"));

        // 通过反射拿到内部的 RequestContext 以验证上下文是否被清理
        Field field = UserProfileService.class.getDeclaredField("requestContext");
        field.setAccessible(true);
        RequestContext ctx = (RequestContext) field.get(service);

        // 修复前：这里会读到 traceId / userId / payload，说明线程上下文泄漏
        // 修复后：reset() 使用 ThreadLocal.remove()，应当读不到任何上下文信息
        assertNull(ctx.read("traceId"), "异常后应清理 traceId");
        assertNull(ctx.read("userId"), "异常后应清理 userId");
        assertNull(ctx.read("payload"), "异常后应清理 payload");
    }
}
