package com.example.backend.project.dto;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

public class ProjectUpdateRequest {

    @NotBlank(message = "项目名称不能为空")
    @Size(min = 1, max = 50, message = "项目名称长度需在1到50之间")
    private String name;

    @Size(max = 50, message = "负责人长度不能超过50")
    private String owner;

    @Min(value = 0, message = "状态只能是0、1或2")
    @Max(value = 2, message = "状态只能是0、1或2")
    private Integer status;

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

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }
}

