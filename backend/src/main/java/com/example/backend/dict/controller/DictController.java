package com.example.backend.dict.controller;

import com.example.backend.common.ApiResponse;
import com.example.backend.dict.service.DictClientService;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.MDC;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.util.UUID;

@RestController
@RequestMapping("/api/dict")
@Validated
public class DictController {

    private final DictClientService dictClientService;

    public DictController(DictClientService dictClientService) {
        this.dictClientService = dictClientService;
    }

    @GetMapping("/query")
    public ApiResponse<Object> query(
            HttpServletRequest request,
            @RequestParam("pageNum") @Min(value = 1, message = "pageNum 必须大于等于 1") int pageNum,
            @RequestParam("pageSize") @Min(value = 1, message = "pageSize 必须在 1 到 100 之间") @Max(value = 100, message = "pageSize 必须在 1 到 100 之间") int pageSize,
            @RequestParam("dictType") @NotBlank(message = "dictType 不能为空") @Size(max = 50, message = "dictType 长度不能超过 50") String dictType) {
        String traceId = StringUtils.isNotBlank(request.getHeader("X-Trace-Id"))
                ? request.getHeader("X-Trace-Id")
                : UUID.randomUUID().toString();
        MDC.put("traceId", traceId);
        try {
            Object data = dictClientService.dictQuery(traceId, pageNum, pageSize, dictType);
            return ApiResponse.ok(data);
        } finally {
            MDC.remove("traceId");
        }
    }
}
