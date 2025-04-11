package ${basePackage}.cli.command;

import cn.hutool.core.bean.BeanUtil;
import ${basePackage}.generator.file.FileGenerator;
import ${basePackage}.model.DataModel;
import lombok.Data;
import picocli.CommandLine;

import java.util.concurrent.Callable;

@CommandLine.Command(name = "generate", description = "生成代码", mixinStandardHelpOptions = true)
@Data
public class GenerateCommand implements Callable<Integer> {
<#list modelConfig.models as modelInfo>
    @CommandLine.Option(names = {<#if modelInfo.abbr??>"-${modelInfo.abbr}"</#if>, <#if modelInfo.fieldName??>"--${modelInfo.fieldName}"</#if>}, arity = "0..1", <#if modelInfo.description??>description = "${modelInfo.description}"</#if>, interactive = true, echo = true)
    private ${modelInfo.type} ${modelInfo.fieldName};
</#list>

    public Integer call() throws Exception {
        DataModel mainTemplateConfig = new DataModel();
        BeanUtil.copyProperties(this, mainTemplateConfig);
        System.out.println("配置信息：" + mainTemplateConfig);
        FileGenerator.doGenerate(mainTemplateConfig);
        return 0;
    }
}