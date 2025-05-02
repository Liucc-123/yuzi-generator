package com.liucc.web.model.dto.generator;

import lombok.Data;

import java.util.Map;

/**
 * 在线使用构造器 请求对象
 */
@Data
public class GeneratorUseRequest {

    /**
     * 生成器 id
     */
    private Long id;

    /**
     * 用户填写的表单数据
     */
    private Map<String, Object> dataModel;
}