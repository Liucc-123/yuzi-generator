package com.liucc.maker.template.enums;

/**
 * 文件过滤规则枚举
 */
public enum FileFilterRuleEnum {
    CONTAINS("文件名称", "contains"),
    START_WITH("文件名称", "startWith"),
    END_WITH("文件名称", "endWith"),
    REGEX("文件名称", "regex"),
    EQUALS("文件内容", "equals");

    private String name;
    private String type;

    FileFilterRuleEnum(String name, String type) {
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
    public static FileFilterRuleEnum getByType(String type) {
        for (FileFilterRuleEnum fileFilterRangeEnum : FileFilterRuleEnum.values()) {
            if (fileFilterRangeEnum.getType().equals(type)) {
                return fileFilterRangeEnum;
            }
        }
        return null;
    }
}
