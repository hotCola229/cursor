package com.example.backend.project.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.backend.common.ErrorCode;
import com.example.backend.exception.BusinessException;
import com.example.backend.project.dto.ProjectConverter;
import com.example.backend.project.dto.ProjectCreateRequest;
import com.example.backend.project.dto.ProjectPageResponse;
import com.example.backend.project.dto.ProjectUpdateRequest;
import com.example.backend.project.dto.ProjectVO;
import com.example.backend.project.entity.Project;
import com.example.backend.project.mapper.ProjectMapper;
import com.example.backend.project.service.ProjectService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

@Service
public class ProjectServiceImpl extends ServiceImpl<ProjectMapper, Project> implements ProjectService {

    @Override
    public ProjectVO createProject(ProjectCreateRequest request) {
        Project project = ProjectConverter.toEntity(request);
        save(project);
        return ProjectConverter.toVO(project);
    }

    @Override
    public ProjectVO getProjectDetail(Long id) {
        Project project = getById(id);
        if (project == null) {
            throw new BusinessException(ErrorCode.PROJECT_NOT_FOUND);
        }
        return ProjectConverter.toVO(project);
    }

    @Override
    public ProjectPageResponse<ProjectVO> pageProjects(Page<Project> page, String keyword) {
        LambdaQueryWrapper<Project> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.isNotBlank(keyword)) {
            wrapper.and(w -> w.like(Project::getName, keyword)
                    .or()
                    .like(Project::getOwner, keyword));
        }
        Page<Project> result = this.page(page, wrapper);
        return ProjectConverter.toPageResponse(result, ProjectConverter.toVOList(result.getRecords()));
    }

    @Override
    public ProjectVO updateProject(Long id, ProjectUpdateRequest request) {
        Project exist = getById(id);
        if (exist == null) {
            throw new BusinessException(ErrorCode.PROJECT_NOT_FOUND);
        }
        ProjectConverter.applyUpdate(exist, request);
        updateById(exist);
        return ProjectConverter.toVO(exist);
    }

    @Override
    public void deleteProject(Long id) {
        Project exist = getById(id);
        if (exist == null) {
            throw new BusinessException(ErrorCode.PROJECT_NOT_FOUND);
        }
        removeById(id);
    }
}

