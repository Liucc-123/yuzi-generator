package com.liucc.web.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.liucc.web.model.dto.generator.GeneratorMakeRequest;
import com.liucc.web.model.dto.generator.GeneratorQueryRequest;
import com.liucc.web.model.dto.generator.GeneratorUseRequest;
import com.liucc.web.model.entity.Generator;
import com.liucc.web.model.vo.GeneratorVO;

import javax.servlet.http.HttpServletRequest;
import java.io.File;

/**
 * 帖子服务
 *
 * @author <a href="https://github.com/liliucc">程序员鱼皮</a>
 * @from <a href="https://liucc.icu">编程导航知识星球</a>
 */
public interface GeneratorService extends IService<Generator> {

    /**
     * 校验
     *
     * @param generator
     * @param add
     */
    void validGenerator(Generator generator, boolean add);

    /**
     * 获取查询条件
     *
     * @param generatorQueryRequest
     * @return
     */
    QueryWrapper<Generator> getQueryWrapper(GeneratorQueryRequest generatorQueryRequest);


    /**
     * 获取帖子封装
     *
     * @param generator
     * @param request
     * @return
     */
    GeneratorVO getGeneratorVO(Generator generator, HttpServletRequest request);

    /**
     * 分页获取帖子封装
     *
     * @param generatorPage
     * @param request
     * @return
     */
    Page<GeneratorVO> getGeneratorVOPage(Page<Generator> generatorPage, HttpServletRequest request);

    /**
     * 在线使用生成器
     *
     * @param generatorUseRequest
     * @param workspace 独立工作空间
     * @return 压缩后的生成代码
     */
    File useGenerator(GeneratorUseRequest generatorUseRequest, String workspace);

    /**
     * 制作生成器
     *
     * @param generatorMakeRequest 制作生成器请求对象
     * @param tempDirPath 工作空间
     * @return 制作好的代码生成器压缩包路径
     */
    File makeGenerator(GeneratorMakeRequest generatorMakeRequest, String tempDirPath);
}
