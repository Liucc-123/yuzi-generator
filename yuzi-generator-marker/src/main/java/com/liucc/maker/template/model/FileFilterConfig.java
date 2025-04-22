package com.liucc.maker.template.model;

import lombok.Builder;
import lombok.Data;

/**
 * 文件过滤配置
 */
@Data
@Builder
public class FileFilterConfig {
    /**
     * 过滤范围（文件名称、文件内容）
     */
    private String range;
    /**
     * 过滤规则（包含、前缀匹配、后缀匹配、正则、相等）
     */
    private String rule;
    /**
     * 过滤值
     */
    private String value;

}
