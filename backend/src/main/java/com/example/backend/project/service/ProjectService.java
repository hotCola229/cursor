package com.example.backend.project.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.backend.project.dto.ProjectCreateRequest;
import com.example.backend.project.dto.ProjectPageResponse;
import com.example.backend.project.dto.ProjectUpdateRequest;
import com.example.backend.project.dto.ProjectVO;
import com.example.backend.project.entity.Project;

public interface ProjectService extends IService<Project> {

    /**
     * 创建项目，返回对外 VO。
     */
    ProjectVO createProject(ProjectCreateRequest request);

    /**
     * 根据 ID 查询项目，不存在则抛出业务异常。
     */
    ProjectVO getProjectDetail(Long id);

    /**
     * 分页查询项目列表，支持关键字搜索。
     */
    ProjectPageResponse<ProjectVO> pageProjects(Page<Project> page, String keyword);

    /**
     * 更新项目，返回更新后的 VO。
     */
    ProjectVO updateProject(Long id, ProjectUpdateRequest request);

    /**
     * 逻辑删除项目，不存在时抛出业务异常。
     */
    void deleteProject(Long id);
}

