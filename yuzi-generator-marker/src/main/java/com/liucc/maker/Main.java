package com.liucc.maker;

//import com.liucc.marker.cli.CommandExecutor;

import com.liucc.maker.generator.main.GeneratorTemplate;
import com.liucc.maker.generator.main.MainGenerator;
import com.liucc.maker.generator.main.ZipGenerator;
import freemarker.template.TemplateException;

import java.io.IOException;

public class Main {
    public static void main(String[] args) throws TemplateException, IOException, InterruptedException {
//        GeneratorTemplate generatorTemplate = new MainGenerator();
//        generatorTemplate.doGenerate();
        GeneratorTemplate generatorTemplate = new ZipGenerator();
        generatorTemplate.doGenerate();
    }
}
