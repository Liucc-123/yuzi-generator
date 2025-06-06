package com.liucc.web.model.dto.generator;

import com.liucc.web.common.PageRequest;
import com.liucc.maker.meta.Meta;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 查询请求
 *
 * @author liucc
 * @from <a href="https://github.com/dashboard">tiga</a>
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class GeneratorQueryRequest extends PageRequest implements Serializable {

    private Long id;
    /**
     * 搜索词
     */
    private String searchText;

    /**
     * 标题
     */
    private String title;

    /**
     * 标签列表
     */
    private List<String> tags;
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
     * 状态
     */
    private Integer status;

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