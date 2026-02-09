package com.example.backend.project.controller;

import com.example.backend.project.mapper.ProjectMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class ProjectControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private DataSource dataSource;

    @Autowired
    private ProjectMapper projectMapper;

    @BeforeEach
    void cleanTable() throws Exception {
        try (Connection connection = dataSource.getConnection();
             Statement stmt = connection.createStatement()) {
            stmt.execute("DELETE FROM project");
        }
    }

    @Test
    void createProjectSuccess() throws Exception {
        Map<String, Object> body = new HashMap<>();
        body.put("name", "项目A");
        body.put("owner", "张三");
        body.put("status", 1);

        mockMvc.perform(post("/api/projects")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.name").value("项目A"))
                .andExpect(jsonPath("$.data.owner").value("张三"));
    }

    @Test
    void createProjectStatusInvalid() throws Exception {
        Map<String, Object> body = new HashMap<>();
        body.put("name", "项目B");
        body.put("owner", "李四");
        body.put("status", 9);

        mockMvc.perform(post("/api/projects")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(40001))
                .andExpect(jsonPath("$.message").isString());
    }

    @Test
    void getDeletedOrNotExistProject() throws Exception {
        // 插入并逻辑删除
        com.example.backend.project.entity.Project project = new com.example.backend.project.entity.Project();
        project.setName("项目C");
        project.setOwner("王五");
        project.setStatus(com.example.backend.project.enums.ProjectStatus.ACTIVE);
        projectMapper.insert(project);
        projectMapper.deleteById(project.getId());

        mockMvc.perform(get("/api/projects/" + project.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(40401));
    }

    @Test
    void pageProjectsWithKeyword() throws Exception {
        // 插入两条项目数据
        com.example.backend.project.entity.Project p1 = new com.example.backend.project.entity.Project();
        p1.setName("关键项目1");
        p1.setOwner("王一");
        p1.setStatus(com.example.backend.project.enums.ProjectStatus.ACTIVE);
        projectMapper.insert(p1);

        com.example.backend.project.entity.Project p2 = new com.example.backend.project.entity.Project();
        p2.setName("普通项目");
        p2.setOwner("李二");
        p2.setStatus(com.example.backend.project.enums.ProjectStatus.ACTIVE);
        projectMapper.insert(p2);

        mockMvc.perform(get("/api/projects")
                        .param("page", "1")
                        .param("size", "10")
                        .param("keyword", "关键"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.total").value(1))
                .andExpect(jsonPath("$.data.records[0].name").value("关键项目1"));
    }
}

