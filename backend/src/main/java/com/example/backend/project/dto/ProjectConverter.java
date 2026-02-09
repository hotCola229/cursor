package com.example.backend.project.dto;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.backend.project.entity.Project;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Project 模块 DTO / Entity / VO 统一转换工具。
 * 抽离映射逻辑，避免在 Controller / Service 中散落手写转换代码。
 */
public final class ProjectConverter {

    private ProjectConverter() {
    }

    public static Project toEntity(com.example.backend.project.dto.ProjectCreateRequest request) {
        if (request == null) {
            return null;
        }
        Project project = new Project();
        project.setName(request.getName());
        project.setOwner(request.getOwner());
        if (request.getStatus() != null) {
            project.setStatus(com.example.backend.project.enums.ProjectStatus.fromCode(request.getStatus()));
        }
        return project;
    }

    public static void applyUpdate(Project project, com.example.backend.project.dto.ProjectUpdateRequest request) {
        if (project == null || request == null) {
            return;
        }
        project.setName(request.getName());
        project.setOwner(request.getOwner());
        if (request.getStatus() != null) {
            project.setStatus(com.example.backend.project.enums.ProjectStatus.fromCode(request.getStatus()));
        }
    }

    public static ProjectVO toVO(Project project) {
        if (project == null) {
            return null;
        }
        ProjectVO vo = new ProjectVO();
        vo.setId(project.getId());
        vo.setName(project.getName());
        vo.setOwner(project.getOwner());
        vo.setStatus(project.getStatus());
        vo.setCreatedAt(project.getCreatedAt());
        vo.setUpdatedAt(project.getUpdatedAt());
        vo.setDeleted(project.getDeleted());
        return vo;
    }

    public static List<ProjectVO> toVOList(List<Project> projects) {
        if (projects == null) {
            return null;
        }
        return projects.stream()
                .map(ProjectConverter::toVO)
                .collect(Collectors.toList());
    }

    public static <T> com.example.backend.project.dto.ProjectPageResponse<T> toPageResponse(Page<?> page, List<T> records) {
        com.example.backend.project.dto.ProjectPageResponse<T> resp = new com.example.backend.project.dto.ProjectPageResponse<>();
        resp.setRecords(records);
        resp.setPage(page.getCurrent());
        resp.setSize(page.getSize());
        resp.setTotal(page.getTotal());
        return resp;
    }
}

