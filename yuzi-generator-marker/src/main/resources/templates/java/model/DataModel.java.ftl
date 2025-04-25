package ${basePackage}.model;

import lombok.Data;

<#macro generateModel indent modelInfo>
<#if modelInfo.description??>
${indent}/**
${indent} * ${modelInfo.description}
${indent} */
</#if>
${indent}public ${modelInfo.type} ${modelInfo.fieldName} <#if modelInfo.defaultValue??>= ${modelInfo.defaultValue?c}</#if>;
</#macro>

/**
 * 动态模板配置
 */
@Data
public class DataModel {

<#list modelConfig.models as modelInfo>
    <#--是模型组-->
    <#if modelInfo.groupKey??>
    /**
     * ${modelInfo.groupName}
     */
    public ${modelInfo.type} ${modelInfo.groupKey} = new ${modelInfo.type}();


    /**
     * 用于生成核心模板文件
     */
    @Data
    public static class ${modelInfo.type} {
    <#list modelInfo.models as subModelInfo>
        <@generateModel indent="        " modelInfo=subModelInfo/>
    </#list>
    }
    <#--非模型组-->
    <#else>
        <@generateModel indent="    " modelInfo=modelInfo/>
    </#if>
    </#list>
}
