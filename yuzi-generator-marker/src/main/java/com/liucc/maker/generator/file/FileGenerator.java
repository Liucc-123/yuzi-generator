package com.liucc.maker.generator.file;

import com.liucc.maker.model.DataModel;
import freemarker.template.TemplateException;

import java.io.File;
import java.io.IOException;

/**
 * 核心模板生成器（静态+动态）
 */
public class FileGenerator {
    private static final String BASIC_PATH = "yuzi-generator-marker";

    public static void main(String[] args) throws TemplateException, IOException {
        DataModel model = new DataModel();
        model.setAuthor("liucc");
        model.setLoop(true);
        model.setOutputText("求和结果：");
        doGenerate(model);
    }

    public static void doGenerate(Object model) throws TemplateException, IOException {
        String projectPath = System.getProperty("user.dir"); // /Users/liuchuangchuang/code/yuzi-generator/yuzi-generator-marker
//        String parentPath = new File(projectPath).getParentFile().getAbsolutePath();
        // 输入路径
//        String inputPath = parentPath + File.separatorChar + "yuzi-generator-demo-projects/acm-template";
        // 如果projectPath 直接取到yuzi-generator根目录，则不需要取parentPath了
        String bathPath = projectPath + File.separatorChar + BASIC_PATH;;
        String inputPath = projectPath + File.separatorChar + "yuzi-generator-demo-projects/acm-template";
        String outputPath = bathPath;
        // 生成静态文件
        StaticFileGenerator.copyFilesByHutool(inputPath, outputPath);
        String dynamicInputPath = bathPath + File.separatorChar + "src/main/resources/templates/MainTemplate.java.ftl";
        String dynamicOutputPath = bathPath + File.separatorChar + "MainTemplate.java";
        // 生成动态文件
        DynamicFileGenerator.doGenerate(dynamicInputPath, dynamicOutputPath, model);
    }
}
