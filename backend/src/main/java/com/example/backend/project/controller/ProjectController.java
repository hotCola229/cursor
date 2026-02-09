package com.example.backend.project.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.backend.common.ApiResponse;
import com.example.backend.project.dto.ProjectCreateRequest;
import com.example.backend.project.dto.ProjectPageResponse;
import com.example.backend.project.dto.ProjectUpdateRequest;
import com.example.backend.project.dto.ProjectVO;
import com.example.backend.project.service.ProjectService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.Size;
import java.util.List;

@RestController
@RequestMapping("/api/projects")
@Validated
public class ProjectController {

    private final ProjectService projectService;

    public ProjectController(ProjectService projectService) {
        this.projectService = projectService;
    }

    @PostMapping
    public ApiResponse<ProjectVO> create(@RequestBody @Validated ProjectCreateRequest request) {
        ProjectVO project = projectService.createProject(request);
        return ApiResponse.ok(project);
    }

    @GetMapping("/{id}")
    public ApiResponse<ProjectVO> getById(@PathVariable("id") Long id) {
        ProjectVO project = projectService.getProjectDetail(id);
        return ApiResponse.ok(project);
    }

    @GetMapping
    public ApiResponse<ProjectPageResponse<ProjectVO>> pageList(
            @RequestParam(value = "page", defaultValue = "1")
            @Min(value = 1, message = "页码必须从1开始") int page,
            @RequestParam(value = "size", defaultValue = "10")
            @Min(value = 1, message = "分页大小最小为1")
            @Max(value = 100, message = "分页大小最大为100") int size,
            @RequestParam(value = "keyword", required = false)
            @Size(max = 50, message = "关键字长度不能超过50") String keyword
    ) {
        Page<com.example.backend.project.entity.Project> p = new Page<>(page, size);
        ProjectPageResponse<ProjectVO> resp = projectService.pageProjects(p, keyword);
        return ApiResponse.ok(resp);
    }

    @PutMapping("/{id}")
    public ApiResponse<ProjectVO> update(
            @PathVariable("id") Long id,
            @RequestBody @Validated ProjectUpdateRequest request
    ) {
        ProjectVO updated = projectService.updateProject(id, request);
        return ApiResponse.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable("id") Long id) {
        projectService.deleteProject(id);
        return ApiResponse.ok(null);
    }
}

