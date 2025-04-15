package com.liucc.maker.generator;

import cn.hutool.core.io.FileUtil;

import java.io.File;
import java.io.IOException;

/**
 * git 仓库生成器
 */
public class GitGenerator {

    /**
     * 使用 git 托管代码生成器
     * @param projectPath 代码生成器所在文件路径
     * @param gitignorePath git ignore 文件路径
     */
    public static void doGenerator(String projectPath, String gitignorePath) {
        try {
            // 创建一个 ProcessBuilder 实例
            ProcessBuilder processBuilder = new ProcessBuilder("git", "init");

            // 设置工作目录
            processBuilder.directory(new File(projectPath));

            // 启动进程
            Process process = processBuilder.start();

            // 等待进程完成
            int exitCode = process.waitFor();

            if (exitCode == 0) {
                System.out.println("Git repository initialized successfully.");
            } else {
                System.err.println("Failed to initialize Git repository. Exit code: " + exitCode);
            }
            // 复制.gitignore 文件
            FileUtil.copy(gitignorePath, projectPath, true);
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        // 示例调用
//        doGenerator("/Users/liuchuangchuang/code/yuzi-generator/yuzi-generator-marker/generated/acm-template-pro-generator",
//                "/Users/liuchuangchuang/code/yuzi-generator/.gitignore");
        String suffix = FileUtil.getSuffix("D:\\code\\yuzi-generator\\yuzi-generator-marker\\src\\main\\resources\\templates\\java\\generator\\MainGenerator.java.ftl");
        System.out.println(suffix);
    }
}
