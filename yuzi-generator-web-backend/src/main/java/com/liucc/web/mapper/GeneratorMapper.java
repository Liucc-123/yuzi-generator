package com.liucc.web.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.liucc.web.model.entity.Generator;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
* @author Administrator
* @description 针对表【generator(代码生成器)】的数据库操作Mapper
* @createDate 2025-04-27 10:34:55
* @Entity com.liucc.web.model.entity.Generator
*/
public interface GeneratorMapper extends BaseMapper<Generator> {

    @Select("select id, distPath from generator where isDelete = 1")
    List<Generator> selectDeletedList();
}




