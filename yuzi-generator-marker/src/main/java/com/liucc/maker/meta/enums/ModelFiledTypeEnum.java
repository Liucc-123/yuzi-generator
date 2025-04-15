package com.liucc.maker.meta.enums;

public enum ModelFiledTypeEnum {
    BOOLEAN("布尔", "boolean"),
    STRING("字符串", "String");

    private String name;
    private String type;

    ModelFiledTypeEnum(String name, String type) {
        this.name = name;
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }
}
