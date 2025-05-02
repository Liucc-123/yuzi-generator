package com.liucc.maker.generator.main;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.resource.ClassPathResource;
import com.liucc.maker.generator.GitGenerator;
import com.liucc.maker.generator.JarGenerator;
import com.liucc.maker.generator.ScriptGenerator;
import com.liucc.maker.generator.file.DynamicFileGenerator;
import com.liucc.maker.meta.Meta;
import com.liucc.maker.meta.MetaManager;
import freemarker.template.TemplateException;

import java.io.File;
import java.io.IOException;

public abstract class GeneratorTemplate  {
    public void doGenerate() throws TemplateException, IOException, InterruptedException  {
        Meta meta = MetaManager.getMetaObject();
        String projectPath = System.getProperty("user.dir");
        String outputPath = projectPath + File.separator + "generated" + File.separator +meta.getName();// 生成目标项目的路径
        // 路径不存在，则创建
        if (!FileUtil.exist(outputPath)) {
            FileUtil.mkdir(outputPath);
        }
        // 1、拷贝原始模板文件
        String sourceCopyDestPath = copySourceFiles(meta, outputPath);
        // 2、生成代码文件
        generateCode(meta, outputPath);
        // 3、使用git托管项目
        gitProject(meta.getGit(), outputPath);
        // 4、构建jar包
        String jarPath = buildJar(outputPath, meta);
        // 5、构建shell脚本
        buildShell(outputPath, jarPath);
        // 6、生成精简版代码生成器
        generateDist(sourceCopyDestPath, outputPath, jarPath);
    }

    /**
     * 生成精简版产物包
     *
     * @param sourceCopyDestPath 原始模板文件
     * @param outputPath 目标路径
     * @param jarPath jar包路径
     * @return
     */
    protected String generateDist(String sourceCopyDestPath, String outputPath, String jarPath) {
        // 生成精简版的代码生成器（仅保留 原始模板文件、jar 包、脚本文件）
        // - 原始模板文件
        FileUtil.copy(sourceCopyDestPath, outputPath + "-dist", true);
        // - jar 包
        String jarCopySourcePath = outputPath + File.separator + jarPath;
        String jarCopyDestPath = outputPath + "-dist" + File.separator + jarPath;
        FileUtil.copy(jarCopySourcePath, jarCopyDestPath, true);
        // - 脚本文件
        String shellCopySourcePath = outputPath + File.separator + "generator";
        String shellCopyDestPath = outputPath + "-dist";
        FileUtil.copy(shellCopySourcePath, shellCopyDestPath, true);
        shellCopySourcePath = outputPath + File.separator + "generator.bat";
        FileUtil.copy(shellCopySourcePath, shellCopyDestPath, true);
        return shellCopyDestPath;
    }

    protected void buildShell(String outputPath, String jarPath) throws IOException {
        // 封装脚本
        String shellOutputFilePath = outputPath + File.separator + "generator";
        ScriptGenerator.doGenerate(shellOutputFilePath, jarPath);
    }

    /**
     * 构建jar包
     * @param outputPath
     * @param meta
     * @return 返回jar包所在路径
     * @throws IOException
     * @throws InterruptedException
     */
    protected String buildJar(String outputPath, Meta meta) throws IOException, InterruptedException {
        // 构建jar包
        JarGenerator.doGenerate(outputPath);
        String jarName = String.format("%s-%s-jar-with-dependencies.jar", meta.getName(), meta.getVersion());
        String jarPath = "target/" + jarName;
        return jarPath;
    }

    protected void gitProject(Meta.Git git, String outputPath) {
        // 使用 git 托管项目
        if (git != null && git.getEnable()){
            GitGenerator.doGenerator(outputPath, git.getGitignore());
        }
    }

    protected void generateCode(Meta meta, String outputPath) throws IOException, TemplateException {
        // 获取 resources 目录
        ClassPathResource classPathResource = new ClassPathResource("");
        String inputResourcePath = classPathResource.getAbsolutePath();

        // Java包基础路径
        // com.liucc
        String outputBasePackage = meta.getBasePackage();
        // 转为 com/liucc
        String outputBasePackagePath = outputBasePackage.replaceAll("\\.", "/");
        String outputBaseJavaPackagePath = outputPath + File.separator + "src/main/java/" + outputBasePackagePath;

        String inputFilePath;
        String outputFilePath;
        // 生成数据模型文件
        inputFilePath = inputResourcePath + File.separator + "templates/java/model/DataModel.java.ftl";
        outputFilePath = outputBaseJavaPackagePath + File.separator + "model/DataModel.java";
        DynamicFileGenerator.doGenerate(inputFilePath, outputFilePath, meta);
        // MainGenerator.java
        inputFilePath = inputResourcePath + File.separator + "templates/java/generator/MainGenerator.java.ftl";
        outputFilePath = outputBaseJavaPackagePath + File.separator + "generator/MainGenerator.java";
        DynamicFileGenerator.doGenerate(inputFilePath, outputFilePath, meta);
        // 生成Picocli 命令类文件
        // cli.command.ConfigCommand
        inputFilePath = inputResourcePath + File.separator + "templates/java/cli/command/ConfigCommand.java.ftl";
        outputFilePath = outputBaseJavaPackagePath + File.separator + "cli/command/ConfigCommand.java";
        DynamicFileGenerator.doGenerate(inputFilePath, outputFilePath, meta);
        // cli.command.GenerateCommand
        inputFilePath = inputResourcePath + File.separator + "templates/java/cli/command/GenerateCommand.java.ftl";
        outputFilePath = outputBaseJavaPackagePath + File.separator + "cli/command/GenerateCommand.java";
        DynamicFileGenerator.doGenerate(inputFilePath, outputFilePath, meta);
        // cli.command.JsonGenerateCommand
        inputFilePath = inputResourcePath + File.separator + "templates/java/cli/command/JsonGenerateCommand.java.ftl";
        outputFilePath = outputBaseJavaPackagePath + File.separator + "cli/command/JsonGenerateCommand.java";
        DynamicFileGenerator.doGenerate(inputFilePath, outputFilePath, meta);
        // cli.command.ListCommand
        inputFilePath = inputResourcePath + File.separator + "templates/java/cli/command/ListCommand.java.ftl";
        outputFilePath = outputBaseJavaPackagePath + File.separator + "cli/command/ListCommand.java";
        DynamicFileGenerator.doGenerate(inputFilePath, outputFilePath, meta);
        // cli.CommandExecutor
        inputFilePath = inputResourcePath + File.separator + "templates/java/cli/CommandExecutor.java.ftl";
        outputFilePath = outputBaseJavaPackagePath + File.separator + "cli/CommandExecutor.java";
        DynamicFileGenerator.doGenerate(inputFilePath, outputFilePath, meta);
        // java.Main
        inputFilePath = inputResourcePath + File.separator + "templates/java/Main.java.ftl";
        outputFilePath = outputBaseJavaPackagePath + File.separator + "Main.java";
        DynamicFileGenerator.doGenerate(inputFilePath, outputFilePath, meta);


        // DynamicGenerator.java
        inputFilePath = inputResourcePath + File.separator + "templates/java/generator/DynamicGenerator.java.ftl";
        outputFilePath = outputBaseJavaPackagePath + File.separator + "generator/DynamicGenerator.java";
        DynamicFileGenerator.doGenerate(inputFilePath, outputFilePath, meta);
        // StaticGenerator.java
        inputFilePath = inputResourcePath + File.separator + "templates/java/generator/StaticGenerator.java.ftl";
        outputFilePath = outputBaseJavaPackagePath + File.separator + "generator/StaticGenerator.java";
        DynamicFileGenerator.doGenerate(inputFilePath, outputFilePath, meta);
        // pom.xml
        inputFilePath = inputResourcePath + File.separator + "templates/pom.xml.ftl";
        outputFilePath = outputPath + File.separator + "pom.xml";
        DynamicFileGenerator.doGenerate(inputFilePath, outputFilePath, meta);
        // README.md
//        inputFilePath = inputResourcePath + File.separator + "templates/README.md.ftl";
//        outputFilePath = outputPath + File.separator + "README.md";
//        DynamicFileGenerator.doGenerate(inputFilePath, outputFilePath, meta);
    }

    protected String copySourceFiles(Meta meta, String outputPath) {
        // 将模板项目 copy 到.source目录下
        String sourcePath = meta.getFileConfig().getSourceRootPath();
        String sourceCopyDestPath = outputPath + File.separator + ".source";
        FileUtil.copy(sourcePath, sourceCopyDestPath, true);
        return sourceCopyDestPath;
    }
}
