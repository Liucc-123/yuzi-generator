package com.liucc.model;

import lombok.Data;

/**
 * 动态模板配置
 */
@Data
public class MainTemplateConfig {

    /**
     * 作者注释信息
     */
    private String author = "liucc"; // 默认值

    /**
     * 是否生成循环
     */
    private boolean isLoop;

    /**
     * 输出信息
     */
    private String outputText = "求和结果：";
}
