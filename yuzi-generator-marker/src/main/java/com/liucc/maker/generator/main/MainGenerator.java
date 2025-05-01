package com.liucc.maker.generator.main;

import freemarker.template.TemplateException;

import java.io.IOException;

public class MainGenerator extends GeneratorTemplate {
    /**
     * 扩展父类生成简化版 方法
     * @param sourceCopyDestPath
     * @param outputPath
     * @param jarPath
     */
    @Override
    protected String generateDist(String sourceCopyDestPath, String outputPath, String jarPath) {
        System.out.println("我不需要精简版代码，不要给我生成啦~~~");
        return "";
    }
}
