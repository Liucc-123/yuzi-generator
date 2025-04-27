package generator.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.liucc.web.mapper.GeneratorMapper;
import com.liucc.web.model.entity.Generator;
import generator.service.GeneratorService;
import org.springframework.stereotype.Service;

/**
* @author Administrator
* @description 针对表【generator(代码生成器)】的数据库操作Service实现
* @createDate 2025-04-27 10:34:55
*/
@Service
public class GeneratorServiceImpl extends ServiceImpl<GeneratorMapper, Generator>
    implements GeneratorService{

}




