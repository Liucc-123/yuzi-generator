package com.liucc.web.model.dto.generator;

import com.liucc.maker.meta.Meta;
import lombok.Data;

/**
 * 在线制作生成器 请求对象
 */
@Data
public class GeneratorMakeRequest {

    /**
     * 元信息
     */
    private Meta meta;

    /**
     * 模板压缩文件路径
     */
    private String zipFilePath;
}