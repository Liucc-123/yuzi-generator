package com.liucc.web.service;

import cn.hutool.core.util.IdUtil;
import com.liucc.web.model.entity.Generator;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

@SpringBootTest
class GeneratorServiceTest {

    @Resource
    private GeneratorService generatorService;
    /**
     * 插入 10w 条数据
     */
    @Test
    public void insertTest(){
        List<Generator> insertList = new ArrayList<>();
        Generator generator = generatorService.getById(19L);
        for (int i = 0; i < 100000; i++) {
            generator.setId(null);
            generatorService.save(generator);
        }
    }
}