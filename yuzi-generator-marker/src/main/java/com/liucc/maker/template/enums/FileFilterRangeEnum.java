package com.liucc.maker.template.enums;

import lombok.Getter;

/**
 * 文件过滤范围枚举
 */
public enum FileFilterRangeEnum {
    FILENAME("文件名称", "filename"),
    FILE_CONTENT("文件内容", "file_content");

    private String name;
    private String type;

    FileFilterRangeEnum(String name, String type) {
        this.name = name;
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    /**
     * 根据type获取枚举
     * @param type
     * @return
     */
    public static FileFilterRangeEnum getByType(String type) {
        for (FileFilterRangeEnum fileFilterRangeEnum : FileFilterRangeEnum.values()) {
            if (fileFilterRangeEnum.getType().equals(type)) {
                return fileFilterRangeEnum;
            }
        }
        return null;
    }
}
