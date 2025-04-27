package com.liucc.web.model.dto.generator;

import com.liucc.web.meta.Meta;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 创建请求
 *
 * @author <a href="https://github.com/liliucc">程序员鱼皮</a>
 * @from <a href="https://liucc.icu">编程导航知识星球</a>
 */
@Data
public class GeneratorAddRequest implements Serializable {

    /**
     * 名称
     */
    private String name;

    /**
     * 描述
     */
    private String description;

    /**
     * 基础包
     */
    private String basePackage;

    /**
     * 版本
     */
    private String version;

    /**
     * 作者
     */
    private String author;

    /**
     * 标签列表（json 数组）
     */
    private List<String> tags;

    /**
     * 图片
     */
    private String picture;

    /**
     * 文件配置（json字符串）
     */
    private Meta.FileConfigDTO fileConfig;

    /**
     * 模型配置（json字符串）
     */
    private Meta.ModelConfigDTO modelConfig;

    /**
     * 代码生成器产物路径
     */
    private String distPath;

    /**
     * 创建用户 id
     */
    private Long userId;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

    private static final long serialVersionUID = 1L;
}