package com.example.backend.dict.service;

import com.example.backend.dict.entity.ExternalCallLog;
import com.example.backend.dict.mapper.ExternalCallLogMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.retry.RetryCallback;
import org.springframework.retry.RetryContext;
import org.springframework.retry.listener.RetryListenerSupport;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpStatusCodeException;

/**
 * Spring Retry 1.3.x 的 RetryListener 仅有 open/onError/close，无 onSuccess。
 * 成功时的日志在 DictClientService.doExchange() 返回前写入。
 */
@Component
public class DictRetryListener extends RetryListenerSupport {

    private static final Logger log = LoggerFactory.getLogger(DictRetryListener.class);

    private final ExternalCallLogMapper externalCallLogMapper;

    public DictRetryListener(ExternalCallLogMapper externalCallLogMapper) {
        this.externalCallLogMapper = externalCallLogMapper;
    }

    @Override
    public void onError(RetryContext context, RetryCallback callback, Throwable throwable) {
        try {
            int attempt = context.getRetryCount() + 1;
            Long start = (Long) context.getAttribute("attemptStartTime");
            long durationMs = start != null ? System.currentTimeMillis() - start : 0;
            Integer httpStatus = null;
            String exType = throwable != null ? throwable.getClass().getSimpleName() : null;
            String exMsg = throwable != null ? throwable.getMessage() : null;
            if (throwable instanceof DictRetryableException) {
                httpStatus = ((DictRetryableException) throwable).getHttpStatus();
            } else if (throwable instanceof HttpStatusCodeException) {
                httpStatus = ((HttpStatusCodeException) throwable).getStatusCode().value();
            }
            if (exMsg != null && exMsg.length() > 1024) {
                exMsg = exMsg.substring(0, 1024);
            }
            ExternalCallLog logEntry = buildLog(context, httpStatus, 0, attempt, durationMs, exType, exMsg);
            externalCallLogMapper.insert(logEntry);
        } catch (Exception e) {
            log.warn("写入 external_call_log 失败", e);
        }
    }

    private ExternalCallLog buildLog(RetryContext context, Integer httpStatus, int success, int attempt,
                                     long durationMs, String exceptionType, String exceptionMessage) {
        ExternalCallLog entry = new ExternalCallLog();
        entry.setTraceId((String) context.getAttribute("traceId"));
        entry.setRequestId((String) context.getAttribute("requestId"));
        entry.setService((String) context.getAttribute("service"));
        entry.setTargetUrl((String) context.getAttribute("targetUrl"));
        entry.setHttpMethod("GET");
        entry.setQueryString((String) context.getAttribute("queryString"));
        entry.setHttpStatus(httpStatus);
        entry.setSuccess(success);
        entry.setAttempt(attempt);
        entry.setDurationMs(durationMs);
        entry.setExceptionType(exceptionType);
        entry.setExceptionMessage(exceptionMessage);
        return entry;
    }
}
