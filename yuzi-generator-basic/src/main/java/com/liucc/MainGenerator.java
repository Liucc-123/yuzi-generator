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

        String inputRootPath = "D:\\code\\yuzi-generator\\yuzi-generator-demo-projects\\acm-template-pro";
        String outputRootPath = "D:\\code\\yuzi-generator\\yuzi-generator-demo-projects\\generated";
        String inputPath;
        String outputPath;

        inputPath = new File(inputRootPath, "src/com/liucc/acm/MainTemplate.java.ftl").getAbsolutePath();
        outputPath = new File(outputRootPath, "src/com/liucc/acm/MainTemplate.java").getAbsolutePath();
        // 生成动态文件
        DynamicGenerator.doGenerate(inputPath, outputPath, model);

        inputPath = new File(inputRootPath, ".gitignore").getAbsolutePath();
        outputPath = new File(outputRootPath, ".gitignore").getAbsolutePath();
        // 生成静态文件
        StaticGenerator.copyFilesByHutool(inputPath, outputPath);

        inputPath = new File(inputRootPath, "README.md").getAbsolutePath();
        outputPath = new File(outputRootPath, "README.md").getAbsolutePath();
        // 生成静态文件
        StaticGenerator.copyFilesByHutool(inputPath, outputPath);
    }
}
