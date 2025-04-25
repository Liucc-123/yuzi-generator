package com.liucc.maker.template.model;

import lombok.Data;

/**
 * 模板生成器 输出配置
 */
@Data
public class TemplateMakerOutputConfig {

    /**
     * 移除组外重复文件配置
     */
    private boolean removeMakerOutputFromRoot = true;
}
