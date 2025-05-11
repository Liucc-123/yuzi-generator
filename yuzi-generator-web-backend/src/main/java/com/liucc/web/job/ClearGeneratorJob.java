package com.liucc.web.job;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.liucc.web.manager.CosManager;
import com.liucc.web.mapper.GeneratorMapper;
import com.liucc.web.model.entity.Generator;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 定时任务
 */
@Component
@Slf4j
public class ClearGeneratorJob {
    @Resource
    private CosManager cosManager;
    @Resource
    private GeneratorMapper generatorMapper;

    /**
     * 清理生成器无用文件
     *
     * @throws Exception
     */
    @XxlJob("clearGeneratorJob")
    public void clearGeneratorJob(){
        log.info("clearGeneratorJob start");
        // 业务逻辑
        // 1. 清理所有模板文件
        cosManager.deleteDir("make_generator/");
        // 2. 清理已删除生成器对应的产物包
        // 2.1. 查询所有已删除的生成器
        List<Generator> generatorList = generatorMapper.selectDeletedList();
        // 2.2. 得到已删除产物包路径列表(注意，不能以/开头)
        Set<String> deletedPathSet = generatorList.stream()
                .map(Generator::getDistPath)
                .filter(StrUtil::isNotBlank)
                .map(distPath -> distPath.substring(1))
                .collect(Collectors.toSet());
        // 2.3. 删除产物包
        cosManager.deleteObjects(new ArrayList<>(deletedPathSet));
        log.info("clearGeneratorJob end");
    }
}
