package com.liucc;

import cn.hutool.core.io.FileUtil;
import com.liucc.model.MainTemplateConfig;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;

import java.io.*;
import java.nio.charset.StandardCharsets;

/**
 * 动态模板文件生成
 */
public class DynamicGenerator {


    public static void main(String[] args) throws IOException, TemplateException {
        // 获取整个项目的根路径
        String projectPath = System.getProperty("user.dir");
        System.out.println("user.dir:" + projectPath);
        // 输入路径：FTL 示例代码模板目录
        String inputPath = projectPath + File.separatorChar + "src/main/resources/templates/MainTemplate.java.ftl";
        // 输出路径：直接输出到项目的根目录
        String outputPath = projectPath + File.separatorChar + "MainTemplate.java";
        // 读取模板配置
        MainTemplateConfig config = new MainTemplateConfig();
        config.setAuthor("liucc");
        config.setLoop(true);
        config.setOutputText("求和结果：");
        // 生成模板
        doGenerate(inputPath, outputPath, config);
    }

    /**
     *
     * @param inputPath 模板读取路径
     * @param outputPath 动态模板生成路径
     * @param model 数据模型
     * @throws IOException
     * @throws TemplateException
     */
    public static void doGenerate(String inputPath, String outputPath, Object model) throws IOException, TemplateException {
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
}
