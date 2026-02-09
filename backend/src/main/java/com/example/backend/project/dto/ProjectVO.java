package com.example.backend.project.dto;

import com.example.backend.project.enums.ProjectStatus;

import java.time.LocalDateTime;

/**
 * Project 对外展示 VO：
 * - 用于 Controller 返回给前端，避免直接暴露 Entity
 * - 字段与当前对外 JSON 结构保持一致（包含 id/name/owner/status/createdAt/updatedAt/deleted）
 */
public class ProjectVO {

    private Long id;
    private String name;
    private String owner;
    private ProjectStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    /**
     * 逻辑删除标记：为保持现有返回结构不变，继续透出该字段
     */
    private Integer deleted;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public ProjectStatus getStatus() {
        return status;
    }

    public void setStatus(ProjectStatus status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Integer getDeleted() {
        return deleted;
    }

    public void setDeleted(Integer deleted) {
        this.deleted = deleted;
    }
}

