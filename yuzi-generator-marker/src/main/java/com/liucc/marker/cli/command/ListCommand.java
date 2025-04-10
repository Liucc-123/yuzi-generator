package com.liucc.marker.cli.command;

import cn.hutool.core.io.FileUtil;
import picocli.CommandLine;

import java.io.File;
import java.util.List;

@CommandLine.Command(name =  "list", description = "查看文件列表", mixinStandardHelpOptions = true)
public class ListCommand implements Runnable {

    public void run() {
        // 整个项目的根路径
        String projectPath = System.getProperty("user.dir");
        // 输入路径
        String inputPath = new File(projectPath, "yuzi-generator-demo-projects/acm-template").getAbsolutePath();
        List<File> files = FileUtil.loopFiles(inputPath);
        System.out.println("模板文件列表：");
        for (File file : files) {
            System.out.println(file);
        }
    }
}

