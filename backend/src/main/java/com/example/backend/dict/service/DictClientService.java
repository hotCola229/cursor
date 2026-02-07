package com.example.backend.dict.service;

import com.example.backend.common.ErrorCode;
import com.example.backend.dict.config.DictClientProperties;
import com.example.backend.dict.entity.ExternalCallLog;
import com.example.backend.dict.mapper.ExternalCallLogMapper;
import com.example.backend.dict.util.ThirdPartySignatureUtil;
import com.example.backend.exception.BusinessException;
import io.github.bucket4j.Bucket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.retry.RetryContext;
import org.springframework.retry.support.RetrySynchronizationManager;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
public class DictClientService {

    private static final String SERVICE_NAME = "DICT_QUERY";
    private static final String PATH = "/api/v1/dataapi/execute/dict/query";
    private static final Logger log = LoggerFactory.getLogger(DictClientService.class);

    private final RestTemplate dictRestTemplate;
    private final DictClientProperties properties;
    private final Bucket dictRateLimitBucket;
    private final org.springframework.retry.support.RetryTemplate dictRetryTemplate;
    private final ExternalCallLogMapper externalCallLogMapper;

    public DictClientService(@Qualifier("dictRestTemplate") RestTemplate dictRestTemplate,
                             DictClientProperties properties,
                             @Qualifier("dictRateLimitBucket") Bucket dictRateLimitBucket,
                             @Qualifier("dictRetryTemplate") org.springframework.retry.support.RetryTemplate dictRetryTemplate,
                             ExternalCallLogMapper externalCallLogMapper,
                             DictRetryListener dictRetryListener) {
        this.dictRestTemplate = dictRestTemplate;
        this.properties = properties;
        this.dictRateLimitBucket = dictRateLimitBucket;
        this.dictRetryTemplate = dictRetryTemplate;
        this.externalCallLogMapper = externalCallLogMapper;
        this.dictRetryTemplate.registerListener(dictRetryListener);
    }

    @SuppressWarnings("unchecked")
    public Object dictQuery(String traceId, int pageNum, int pageSize, String dictType) {
        String requestId = UUID.randomUUID().toString();
        String baseUrl = properties.getClient().getBaseUrl();
        if (!baseUrl.endsWith("/")) {
            baseUrl = baseUrl + "/";
        }
        String targetUrl = baseUrl + PATH.replaceFirst("^/", "");
        Map<String, Object> queryParams = new HashMap<>();
        queryParams.put("pageNum", pageNum);
        queryParams.put("pageSize", pageSize);
        queryParams.put("dictType", dictType);
        String queryString = "pageNum=" + pageNum + "&pageSize=" + pageSize + "&dictType=" + dictType;

        if (!dictRateLimitBucket.tryConsume(1)) {
            saveLog(traceId, requestId, targetUrl, queryString, null, 0, 1, null, "RATE_LIMIT", "请求被限流", null);
            throw new BusinessException(ErrorCode.RATE_LIMIT);
        }

        try {
            ResponseEntity<Object> responseEntity = dictRetryTemplate.execute(
                    context -> doExchange(context, traceId, requestId, targetUrl, queryString, queryParams),
                    ctx -> {
                        int attempt = ctx.getRetryCount() + 1;
                        Long start = (Long) ctx.getAttribute("attemptStartTime");
                        long duration = start != null ? System.currentTimeMillis() - start : 0;
                        Throwable last = ctx.getLastThrowable();
                        Integer status = null;
                        String exType = last != null ? last.getClass().getSimpleName() : null;
                        String exMsg = last != null ? last.getMessage() : null;
                        if (last instanceof DictRetryableException) {
                            status = ((DictRetryableException) last).getHttpStatus();
                        } else if (last instanceof HttpStatusCodeException) {
                            status = ((HttpStatusCodeException) last).getStatusCode().value();
                        }
                        saveLog((String) ctx.getAttribute("traceId"), (String) ctx.getAttribute("requestId"),
                                (String) ctx.getAttribute("targetUrl"), (String) ctx.getAttribute("queryString"),
                                status, 0, attempt, duration, exType, exMsg, null);
                        throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR);
                    });
            return responseEntity != null ? responseEntity.getBody() : null;
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }

    private ResponseEntity<Object> doExchange(RetryContext context, String traceId, String requestId, String targetUrl,
                                              String queryString, Map<String, Object> queryParams) {
        if (context.getRetryCount() == 0) {
            context.setAttribute("traceId", traceId);
            context.setAttribute("requestId", requestId);
            context.setAttribute("targetUrl", targetUrl);
            context.setAttribute("queryString", queryString);
            context.setAttribute("service", SERVICE_NAME);
        }
        context.setAttribute("attemptStartTime", System.currentTimeMillis());

        String timestamp = ThirdPartySignatureUtil.generateTimestamp();
        String signature = ThirdPartySignatureUtil.generateSignature(
                "GET", PATH, queryParams,
                properties.getClient().getAppKey(),
                properties.getClient().getAppSecret(),
                timestamp);

        HttpHeaders headers = new HttpHeaders();
        headers.set("AppKey", properties.getClient().getAppKey());
        headers.set("Signature", signature);
        headers.set("Timestamp", timestamp);

        String fullUrl = targetUrl + "?" + queryString;
        URI uri = URI.create(fullUrl);

        try {
            ResponseEntity<Object> response = dictRestTemplate.exchange(
                    uri,
                    HttpMethod.GET,
                    new HttpEntity<>(headers),
                    Object.class);
            int status = response.getStatusCode().value();
            if (status >= 500) {
                throw new DictRetryableException("Third party returned " + status, status);
            }
            // Spring Retry 1.3 无 onSuccess，成功时在此写入一条日志
            int attempt = context.getRetryCount() + 1;
            long durationMs = System.currentTimeMillis() - (Long) context.getAttribute("attemptStartTime");
            saveLog(traceId, requestId, targetUrl, queryString, status, 1, attempt, durationMs, null, null, null);
            return response;
        } catch (HttpStatusCodeException e) {
            int status = e.getStatusCode().value();
            if (status >= 500) {
                throw new DictRetryableException(e.getMessage(), status);
            }
            throw e;
        } catch (ResourceAccessException e) {
            throw new DictRetryableException(e.getMessage(), e);
        }
    }

    void saveLog(String traceId, String requestId, String targetUrl, String queryString,
                 Integer httpStatus, int success, int attempt, Long durationMs,
                 String exceptionType, String exceptionMessage, LocalDateTime createdAt) {
        try {
            ExternalCallLog logEntry = new ExternalCallLog();
            logEntry.setTraceId(traceId);
            logEntry.setRequestId(requestId);
            logEntry.setService(SERVICE_NAME);
            logEntry.setTargetUrl(targetUrl != null ? targetUrl : "");
            logEntry.setHttpMethod("GET");
            logEntry.setQueryString(queryString);
            logEntry.setHttpStatus(httpStatus);
            logEntry.setSuccess(success);
            logEntry.setAttempt(attempt);
            logEntry.setDurationMs(durationMs);
            logEntry.setExceptionType(exceptionType);
            logEntry.setExceptionMessage(exceptionMessage != null && exceptionMessage.length() > 1024
                    ? exceptionMessage.substring(0, 1024) : exceptionMessage);
            if (createdAt != null) {
                logEntry.setCreatedAt(createdAt);
            }
            externalCallLogMapper.insert(logEntry);
        } catch (Exception e) {
            log.warn("写入 external_call_log 失败", e);
        }
    }
}
