package com.example.backend.dict.controller;

import com.example.backend.dict.mapper.ExternalCallLogMapper;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.Statement;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class DictControllerTest {

    private static final WireMockServer wireMock = new WireMockServer(0);

    static {
        // 必须在 Spring 加载上下文前启动，否则 @DynamicPropertySource 里 wireMock.port() 会抛 Not listening on HTTP port
        wireMock.start();
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private DataSource dataSource;

    @Autowired
    private ExternalCallLogMapper externalCallLogMapper;

    @DynamicPropertySource
    static void dictBaseUrl(DynamicPropertyRegistry registry) {
        registry.add("dict.client.baseUrl", () -> "http://localhost:" + wireMock.port() + "/");
    }

    @BeforeEach
    void setUp() throws Exception {
        try (Connection c = dataSource.getConnection(); Statement s = c.createStatement()) {
            s.execute("DELETE FROM external_call_log");
        }
    }

    @AfterEach
    void tearDown() {
        wireMock.resetAll();
    }

    @Test
    void querySuccess_oneLogRecord() throws Exception {
        wireMock.stubFor(WireMock.get(WireMock.urlPathMatching("/api/v1/dataapi/execute/dict/query.*"))
                .willReturn(WireMock.aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"total\":1,\"data\":[{\"code\":\"1\",\"value\":\"test\"}],\"totalPage\":1,\"currentPageNum\":1,\"pageSize\":10}")));

        mockMvc.perform(get("/api/dict/query")
                        .param("pageNum", "1")
                        .param("pageSize", "10")
                        .param("dictType", "job_type"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.message").value("ok"))
                .andExpect(jsonPath("$.data.total").value(1));

        long count = externalCallLogMapper.selectCount(null);
        org.junit.jupiter.api.Assertions.assertEquals(1, count, "应插入 1 条调用日志");
    }

    @Test
    void queryThirdParty500_retryThreeTimes_threeLogRecords() throws Exception {
        wireMock.stubFor(WireMock.get(WireMock.urlPathMatching("/api/v1/dataapi/execute/dict/query.*"))
                .willReturn(WireMock.aResponse().withStatus(500)));

        mockMvc.perform(get("/api/dict/query")
                        .param("pageNum", "1")
                        .param("pageSize", "10")
                        .param("dictType", "job_type"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(50000));

        long count = externalCallLogMapper.selectCount(null);
        org.junit.jupiter.api.Assertions.assertEquals(3, count, "应插入 3 条调用日志（重试 2 次，共 3 次请求）");
    }
}
