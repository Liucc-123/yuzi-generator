package com.liucc.maker.generator.main;

import cn.hutool.core.util.ZipUtil;

public class ZipGenerator extends GeneratorTemplate {
    /**
     * 扩展父类 对生成的产物包进行打包压缩
     * @param sourceCopyDestPath
     * @param outputPath
     * @param jarPath
     */
    @Override
    protected String generateDist(String sourceCopyDestPath, String outputPath, String jarPath) {
        String distPath = super.generateDist(sourceCopyDestPath, outputPath, jarPath);
        // 扩展父类 对生成的产物包进行打包压缩
        String zipPath = distPath + ".zip";
        ZipUtil.zip(distPath,zipPath);
        return zipPath;
    }
}
