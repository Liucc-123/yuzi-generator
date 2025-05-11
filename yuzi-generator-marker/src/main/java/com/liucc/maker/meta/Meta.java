package com.liucc.maker.meta;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * 元信息配置类，与 Meta.json文件建立映射
 */
@NoArgsConstructor
@Data
public class Meta implements Serializable{

    private String name;
    private String description;
    private String basePackage;
    private String version;
    private String author;
    private String createTime;
    private Git git;
    private FileConfigDTO fileConfig;
    private ModelConfigDTO modelConfig;

    @NoArgsConstructor
    @Data
    public static class Git{
        private Boolean enable;
        private String gitignore;
    }

    @NoArgsConstructor
    @Data
    public static class FileConfigDTO implements Serializable {
        private String inputRootPath;
        private String outputRootPath;
        private String sourceRootPath; // 源模板项目路径
        private String type;
        private List<FileInfo> files;

        @NoArgsConstructor
        @Data
        public static class FileInfo implements Serializable{
            private String inputPath;
            private String outputPath;
            private String type;
            private String generateType;
            private String condition;
            private String groupKey;
            private String groupName;
            private List<FileInfo> files;
        }
    }

    @NoArgsConstructor
    @Data
    public static class ModelConfigDTO {
        private List<ModelInfo> models;

        @NoArgsConstructor
        @Data
        public static class ModelInfo {
            private String fieldName;
            private String type;
            private String description;
            private Object defaultValue;
            private String abbr;
            private String groupKey;
            private String groupName;
            private String condition;
            private List<ModelInfo> models;
            // 中间参数 该分组下所有参数拼接字符串 "--author", "--outputText"
            private String allArgsStr;
        }
    }
}
