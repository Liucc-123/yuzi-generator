package ${basePackage}.cli.command;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import ${basePackage}.model.DataModel;
import ${basePackage}.generator.MainGenerator;
import lombok.Data;
import picocli.CommandLine;

import java.util.concurrent.Callable;

<#--宏定义-->
<#--生成选项-->
<#macro generateOption indent modelInfo>
${indent}@CommandLine.Option(names = {<#if modelInfo.abbr??>"-${modelInfo.abbr}"</#if>, <#if modelInfo.fieldName??>"--${modelInfo.fieldName}"</#if>}, arity = "0..1", <#if modelInfo.description??>description = "${modelInfo.description}"</#if>, interactive = true, echo = true)
${indent}private ${modelInfo.type} ${modelInfo.fieldName} <#if modelInfo.defaultValue??>= ${modelInfo.defaultValue?c}</#if>;
</#macro>

@CommandLine.Command(name = "generate", description = "生成代码", mixinStandardHelpOptions = true)
@Data
public class GenerateCommand implements Callable<Integer> {
<#list modelConfig.models as modelInfo>
    <#--有分组-->
    <#if modelInfo.groupKey??>
    static DataModel.${modelInfo.type} ${modelInfo.groupKey} = new DataModel.${modelInfo.type}();

    @CommandLine.Command(name = "${modelInfo.groupKey}", description = "${modelInfo.description}", mixinStandardHelpOptions = true)
    @Data
    static class ${modelInfo.type}Command implements Runnable{
    <#list modelInfo.models as subModelInfo>
        <@generateOption indent="        " modelInfo=subModelInfo/>
    </#list>

        @Override
        public void run() {
            <#list modelInfo.models as subModelInfo>
            ${modelInfo.groupKey}.${subModelInfo.fieldName} = ${subModelInfo.fieldName};
            </#list>
        }
    }
    <#--无分组-->
    <#else>
    <@generateOption indent="    " modelInfo=modelInfo/>
    </#if>
</#list>

    public Integer call() throws Exception {
        <#list modelConfig.models as modelInfo>
            <#if modelInfo.condition??>
            // 如果用户开启 loop，再让用户进一步填写核心配置信息
                if (${modelInfo.condition}){
                System.out.println("请输入${modelInfo.groupName}信息：");
                CommandLine commandLine = new CommandLine(${modelInfo.type}Command.class);
                commandLine.execute(${modelInfo.allArgsStr});
            }
            </#if>
        </#list>
        // 填充数据模型
        DataModel dataModel = new DataModel();
        BeanUtil.copyProperties(this, dataModel);
    <#list modelConfig.models as modelInfo>
        <#if modelInfo.groupKey??>
        dataModel.${modelInfo.groupKey} = ${modelInfo.groupKey};
        </#if>
    </#list>
        MainGenerator.doGenerate(dataModel);
        return 0;
    }
}