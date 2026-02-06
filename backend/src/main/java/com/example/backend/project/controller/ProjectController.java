package com.example.backend.project.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.backend.common.ApiResponse;
import com.example.backend.common.ErrorCode;
import com.example.backend.exception.BusinessException;
import com.example.backend.project.dto.ProjectCreateRequest;
import com.example.backend.project.dto.ProjectPageResponse;
import com.example.backend.project.dto.ProjectUpdateRequest;
import com.example.backend.project.entity.Project;
import com.example.backend.project.enums.ProjectStatus;
import com.example.backend.project.service.ProjectService;
import org.apache.commons.lang3.StringUtils;
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
    public ApiResponse<Project> create(@RequestBody @Validated ProjectCreateRequest request) {
        Project project = new Project();
        project.setName(request.getName());
        project.setOwner(request.getOwner());
        project.setStatus(ProjectStatus.fromCode(request.getStatus()));
        projectService.save(project);
        return ApiResponse.ok(project);
    }

    @GetMapping("/{id}")
    public ApiResponse<Project> getById(@PathVariable("id") Long id) {
        Project project = projectService.getById(id);
        if (project == null) {
            throw new BusinessException(ErrorCode.PROJECT_NOT_FOUND);
        }
        return ApiResponse.ok(project);
    }

    @GetMapping
    public ApiResponse<ProjectPageResponse<Project>> pageList(
            @RequestParam(value = "page", defaultValue = "1")
            @Min(value = 1, message = "页码必须从1开始") int page,
            @RequestParam(value = "size", defaultValue = "10")
            @Min(value = 1, message = "分页大小最小为1")
            @Max(value = 100, message = "分页大小最大为100") int size,
            @RequestParam(value = "keyword", required = false)
            @Size(max = 50, message = "关键字长度不能超过50") String keyword
    ) {
        Page<Project> p = new Page<>(page, size);
        LambdaQueryWrapper<Project> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.isNotBlank(keyword)) {
            wrapper.and(w -> w.like(Project::getName, keyword)
                    .or()
                    .like(Project::getOwner, keyword));
        }
        Page<Project> result = projectService.page(p, wrapper);
        ProjectPageResponse<Project> resp = new ProjectPageResponse<>();
        resp.setRecords(result.getRecords());
        resp.setPage(result.getCurrent());
        resp.setSize(result.getSize());
        resp.setTotal(result.getTotal());
        return ApiResponse.ok(resp);
    }

    @PutMapping("/{id}")
    public ApiResponse<Project> update(
            @PathVariable("id") Long id,
            @RequestBody @Validated ProjectUpdateRequest request
    ) {
        Project exist = projectService.getById(id);
        if (exist == null) {
            throw new BusinessException(ErrorCode.PROJECT_NOT_FOUND);
        }
        exist.setName(request.getName());
        exist.setOwner(request.getOwner());
        exist.setStatus(ProjectStatus.fromCode(request.getStatus()));
        projectService.updateById(exist);
        return ApiResponse.ok(exist);
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable("id") Long id) {
        Project exist = projectService.getById(id);
        if (exist == null) {
            throw new BusinessException(ErrorCode.PROJECT_NOT_FOUND);
        }
        projectService.removeById(id);
        return ApiResponse.ok(null);
    }
}

