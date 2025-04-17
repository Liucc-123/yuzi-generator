package ${basePackage}.generator;

import freemarker.template.TemplateException;

import java.io.File;
import java.io.IOException;
import ${basePackage}.model.DataModel;

<#macro generateFile indent fileInfo>
${indent}inputPath = new File(inputRootPath, "${fileInfo.inputPath}").getAbsolutePath();
${indent}outputPath = new File(outputRootPath, "${fileInfo.outputPath}").getAbsolutePath();
<#if fileInfo.generateType == "dynamic">
${indent}DynamicGenerator.doGenerate(inputPath, outputPath, model);
<#else>
${indent}StaticGenerator.copyFilesByHutool(inputPath, outputPath);
</#if>
</#macro>

/**
 * 核心模板生成器（静态+动态）
 */
public class MainGenerator {

    public static void doGenerate(DataModel model) throws TemplateException, IOException {

        String inputRootPath = "${fileConfig.inputRootPath}";
        String outputRootPath = "${fileConfig.outputRootPath}";
        String inputPath;
        String outputPath;
<#list  modelConfig.models as modelInfo>
    <#--有分组-->
    <#if modelInfo.groupKey??>
        <#list modelInfo.models as subModelInfo>
        ${subModelInfo.type} ${subModelInfo.fieldName} = model.${modelInfo.groupKey}.${subModelInfo.fieldName};
        </#list>
    <#--无分组-->
    <#else>
        ${modelInfo.type} ${modelInfo.fieldName} = model.${modelInfo.fieldName};
    </#if>
</#list>
<#list fileConfig.files as fileInfo>
    <#if fileInfo.condition??>
        if (${fileInfo.condition}) {
        <#if fileInfo.groupKey??>
            // 说明是一组文件 groupKey = ${fileInfo.groupKey}
        <#list fileInfo.files as fileInfo>
            <@generateFile indent="            " fileInfo=fileInfo/>
        </#list>
        <#else >
            // 说明是一个文件
            <@generateFile indent="            " fileInfo=fileInfo/>
        </#if>
        }
    <#else>
        <@generateFile indent="        " fileInfo=fileInfo/>
    </#if>
</#list>
    }
}
