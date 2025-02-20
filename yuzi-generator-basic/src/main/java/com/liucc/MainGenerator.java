package com.liucc;

import com.liucc.model.MainTemplateConfig;
import freemarker.template.TemplateException;

import java.io.File;
import java.io.IOException;

/**
 * 核心模板生成器（静态+动态）
 */
public class MainGenerator {

    public static void main(String[] args) throws TemplateException, IOException {
        MainTemplateConfig model = new MainTemplateConfig();
        model.setAuthor("liucc");
        model.setLoop(true);
        model.setOutputText("求和结果：");
        doGenerate(model);
    }

    public static void doGenerate(Object model) throws TemplateException, IOException {
        String projectPath = System.getProperty("user.dir"); // /Users/liuchuangchuang/code/yuzi-generator/yuzi-generator-basic
        String parentPath = new File(projectPath).getParentFile().getAbsolutePath();
        // 输入路径
        String inputPath = parentPath + File.separatorChar + "yuzi-generator-demo-projects/acm-template";
        String outputPath = projectPath;
        // 生成静态文件
        StaticGenerator.copyFilesByRecursive(inputPath, outputPath);
        String dynamicInputPath = projectPath + File.separatorChar + "src/main/resources/templates/MainTemplate.java.ftl";
        String dynamicOutputPath = projectPath + File.separatorChar + "MainTemplate.java";
        // 生成动态文件
        DynamicGenerator.doGenerate(dynamicInputPath, dynamicOutputPath, model);
    }
}
