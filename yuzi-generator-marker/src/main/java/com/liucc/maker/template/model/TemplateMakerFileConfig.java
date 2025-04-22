package com.liucc.maker.template.model;

import lombok.Data;

import java.util.List;

/**
 * 过滤文件配置
 */
@Data
public class TemplateMakerFileConfig {
    private List<FileInfoConfig> files;

    private FileGroupConfig fileGroupConfig;

    @Data
    public static class FileGroupConfig {
        private String condition;
        private String groupKey;
        private String groupName;
    }

    @Data
    public static class FileInfoConfig {
        /**
         * 文件路径(相对路径)
         */
        private String path;
        /**
         * 文件过滤器
         */
        private List<FileFilterConfig> filters;
    }
}
