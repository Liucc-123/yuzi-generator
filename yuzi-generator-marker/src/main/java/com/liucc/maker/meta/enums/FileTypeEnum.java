package com.liucc.maker.meta.enums;

public enum FileTypeEnum {
    DIR("目录", "dir"),
    GROUP("文件组", "group"),
    FILE("文件", "file");

    private String name;
    private String type;

    FileTypeEnum(String name, String type) {
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
