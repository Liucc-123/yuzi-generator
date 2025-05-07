package com.liucc.maker.generator.file;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.lang.ResourceClassLoader;
import freemarker.cache.ClassTemplateLoader;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;

import java.io.*;
import java.nio.charset.StandardCharsets;

/**
 * 动态模板文件生成
 */
public class DynamicFileGenerator {

    /**
     * 生成文件
     *
     * @param inputPath 模板文件输入路径
     * @param outputPath 动态模板生成路径
     * @param model 数据模型
     * @throws IOException
     * @throws TemplateException
     */
    public static void doGenerateByPath(String inputPath, String outputPath, Object model) throws IOException, TemplateException {
        // 1、new 出 Configuration 对象，参数为 FreeMarker 版本号
        Configuration configuration = new Configuration(Configuration.VERSION_2_3_32);
        File templateFile = new File(inputPath).getParentFile();
        // 指定模板文件所在的路径
        configuration.setDirectoryForTemplateLoading(templateFile);
        // 设置模板文件使用的字符集
        configuration.setDefaultEncoding("utf-8");
        // 数字格式设置
        configuration.setNumberFormat("0.##########");
        // 2、创建模板对象，加载指定模板
        String templateName = new File(inputPath).getName();
        Template template = configuration.getTemplate(templateName);
        // 3、文件如果不存在，则创建
        if (!FileUtil.exist(outputPath)){
            FileUtil.touch(outputPath);
        }
        // 4、生成
        Writer out = new OutputStreamWriter(new FileOutputStream(outputPath), StandardCharsets.UTF_8);
        template.process(model, out);

        // 生成文件后别忘了关闭哦
        out.close();
    }

    /**
     * 生成文件
     *
     * @param relativeInputPath 模板文件相对路径
     * @param outputPath 动态模板生成路径
     * @param model 数据模型
     * @throws IOException
     * @throws TemplateException
     */
    public static void doGenerate(String relativeInputPath, String outputPath, Object model) throws IOException, TemplateException {
        // 1、new 出 Configuration 对象，参数为 FreeMarker 版本号
        Configuration configuration = new Configuration(Configuration.VERSION_2_3_32);
        int lastIndexOf = relativeInputPath.lastIndexOf("/");
        String basePackage = relativeInputPath.substring(0, lastIndexOf);
        String templateName = relativeInputPath.substring(lastIndexOf + 1);
        ClassTemplateLoader templateLoader = new ClassTemplateLoader(DynamicFileGenerator.class, basePackage);
        // 指定模板文件所在的路径
        configuration.setTemplateLoader(templateLoader);
        // 设置模板文件使用的字符集
        configuration.setDefaultEncoding("utf-8");
        // 数字格式设置
        configuration.setNumberFormat("0.##########");
        // 2、创建模板对象，加载指定模板
        Template template = configuration.getTemplate(templateName);
        // 3、文件如果不存在，则创建
        if (!FileUtil.exist(outputPath)){
            FileUtil.touch(outputPath);
        }
        // 4、生成
        Writer out = new OutputStreamWriter(new FileOutputStream(outputPath), StandardCharsets.UTF_8);
        template.process(model, out);

        // 生成文件后别忘了关闭哦
        out.close();
    }
}
