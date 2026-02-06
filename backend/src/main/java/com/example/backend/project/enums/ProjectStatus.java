package com.example.backend.project.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum ProjectStatus {

    DRAFT(0, "草稿"),
    ACTIVE(1, "进行中"),
    ARCHIVED(2, "已归档");

    @EnumValue
    private final int code;
    private final String desc;

    ProjectStatus(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    @JsonValue
    public int getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }

    @JsonCreator
    public static ProjectStatus fromCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (ProjectStatus value : ProjectStatus.values()) {
            if (value.code == code) {
                return value;
            }
        }
        throw new IllegalArgumentException("项目状态不合法");
    }
}

