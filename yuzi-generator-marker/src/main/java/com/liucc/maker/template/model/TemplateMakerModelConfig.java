package com.liucc.maker.template.model;

import com.liucc.maker.meta.Meta;
import lombok.Data;

import java.util.List;

/**
 * 过滤模型配置
 */
@Data
public class TemplateMakerModelConfig {
    private List<ModelInfoConfig> models;

    private ModelGroupConfig modelGroupConfig;

    @Data
    public static class ModelGroupConfig {
        private String condition;
        private String groupKey;
        private String groupName;
    }

    @Data
    public static class ModelInfoConfig {
        private String fieldName;
        private String type;
        private String description;
        private Object defaultValue;
        private String abbr;
        // 要替换的文本
        private String replaceText;
    }
}
