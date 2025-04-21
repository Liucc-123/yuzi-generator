package com.liucc.maker.template;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.liucc.maker.meta.Meta;
import com.liucc.maker.meta.enums.FileGenerateTypeEnum;
import com.liucc.maker.meta.enums.FileTypeEnum;

import java.io.File;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 模板生成器，生成动态模板文件和元信息配置文件
 */
public class TemplateMaker {

    /**
     * 制作模板
     *
     * @param id id不存在，表示首次制作模板；id存在，更新模板
     * @return
     */
    private static Long makeTemplate(Long id, String sourceRootPath, Meta meta, List<String> fileInputPaths, Meta.ModelConfigDTO.ModelInfo modelInfo, String searchStr) {
        if (CollUtil.isEmpty(fileInputPaths)) {
            System.out.println("输入文件为空fileInputPaths：" + fileInputPaths);
            return null;
        }
        if (id == null) {
            id = IdUtil.getSnowflakeNextId();
        }
        // 拷贝原始项目到临时目录（工作空间）中
        String projectPath = System.getProperty("user.dir");
        String tempFilePath = projectPath + File.separator + ".temp" + File.separator + id;
        // 非首次制作，不需要重复拷贝原始项目文件
        if (!FileUtil.exist(tempFilePath)) {
            FileUtil.mkdir(tempFilePath);
            FileUtil.copy(sourceRootPath, tempFilePath, true);
        }
        // 一、输入信息
        // 2、输入文件信息(相对路径)
        List<Meta.FileConfigDTO.FileInfo> fileInfos = new ArrayList<>();
        sourceRootPath = tempFilePath + File.separator + FileUtil.getLastPathEle(Paths.get(sourceRootPath)).toString();
        for (String fileInputPath : fileInputPaths) {
            File inputFile = new File(sourceRootPath + File.separator + fileInputPath);
            // windows系统需要对文件路径进行转移
            sourceRootPath = sourceRootPath.replaceAll("\\\\", "/");

            // 如果输入文件是目录
            if (FileUtil.isDirectory(inputFile)) {
                List<File> fileList = FileUtil.loopFiles(inputFile);
                for (File file : fileList) {
                    Meta.FileConfigDTO.FileInfo fileInfo = makeFileTemplate(sourceRootPath, file, modelInfo, searchStr);
                    fileInfos.add(fileInfo);
                }
            } else { // 单个文件
                Meta.FileConfigDTO.FileInfo fileInfo = makeFileTemplate(sourceRootPath, inputFile, modelInfo, searchStr);
                fileInfos.add(fileInfo);
            }
        }

        // 三、生成元信息配置文件
        String metaOutputPath = sourceRootPath + File.separator + "meta.json";
        // 非首次制作，不需要重复输入已有的元信息，而是在此基础上，可以覆盖或追加元信息配置
        if (FileUtil.exist(metaOutputPath)) {
            // 1、构造配置参数
            Meta newMeta = JSONUtil.toBean(FileUtil.readUtf8String(metaOutputPath), Meta.class);
            // 文件配置
            List<Meta.FileConfigDTO.FileInfo> fileInfoList = newMeta.getFileConfig().getFiles();
            fileInfoList.addAll(fileInfos);
            // 模型配置
            List<Meta.ModelConfigDTO.ModelInfo> modelInfoList = newMeta.getModelConfig().getModels();
            modelInfoList.add(modelInfo);
            // 文件去重
            newMeta.getFileConfig().setFiles(distinctFiles(fileInfoList));
            // 配置去重
            newMeta.getModelConfig().setModels(distinctModels(modelInfoList));
            // 2、输出元信息配置文件
            FileUtil.writeUtf8String(JSONUtil.toJsonPrettyStr(newMeta), metaOutputPath);
        } else {
            // 1、构造配置参数
            Meta.FileConfigDTO fileConfig = new Meta.FileConfigDTO();
            meta.setFileConfig(fileConfig);
            fileConfig.setSourceRootPath(sourceRootPath);
            fileConfig.setType("dir");
            List<Meta.FileConfigDTO.FileInfo> fileInfoList = new ArrayList<>();
            fileInfoList.addAll(fileInfos);
            fileConfig.setFiles(fileInfoList);

            Meta.ModelConfigDTO modelConfig = new Meta.ModelConfigDTO();
            List<Meta.ModelConfigDTO.ModelInfo> modelInfoList = new ArrayList<>();
            modelInfoList.add(modelInfo);
            modelConfig.setModels(modelInfoList);
            meta.setModelConfig(modelConfig);
            // 2、输出元信息配置文件
            String metaJson = JSONUtil.toJsonPrettyStr(meta);
            FileUtil.writeUtf8String(metaJson, metaOutputPath);
        }
        return id;
    }

    private static Meta.FileConfigDTO.FileInfo makeFileTemplate(String sourceRootPath, File inputFile, Meta.ModelConfigDTO.ModelInfo modelInfo, String searchStr) {
        String fileInputAbsolutePath = inputFile.getAbsolutePath();
        // windows系统需要对文件路径进行转移
        fileInputAbsolutePath = fileInputAbsolutePath.replaceAll("\\\\", "/");
        String fileInputPath = fileInputAbsolutePath.replace(sourceRootPath + "/", "");
        String fileOutputPath = fileInputPath + ".ftl";
        // 二、使用字符串替换算法，生成模板文件
        String fileOutputAbsolutePath = sourceRootPath + File.separator + fileOutputPath;
        String originalContent;
        // 非首次制作，可以在已有模板文件的基础上再次挖坑
        if (FileUtil.exist(fileOutputAbsolutePath)) {
            originalContent = FileUtil.readUtf8String(fileOutputAbsolutePath);
        } else {
            originalContent = FileUtil.readUtf8String(fileInputAbsolutePath);
        }
        String replacement = String.format("${%s}", modelInfo.getFieldName());
        String newContent = StrUtil.replace(originalContent, searchStr, replacement);
        Meta.FileConfigDTO.FileInfo fileInfo = new Meta.FileConfigDTO.FileInfo();
        fileInfo.setInputPath(fileInputPath);
        fileInfo.setType(FileTypeEnum.FILE.getType());
        // 模板文件内容未发生变化，则生成静态文件
        if (StrUtil.equals(originalContent, newContent)) {
            fileInfo.setOutputPath(fileInputPath);
            fileInfo.setGenerateType(FileGenerateTypeEnum.STATIC.getType());
        } else { // 动态文件
            fileInfo.setOutputPath(fileOutputPath);
            fileInfo.setGenerateType(FileGenerateTypeEnum.DYNAMIC.getType());
            FileUtil.writeUtf8String(newContent, fileOutputAbsolutePath);
        }
        return fileInfo;
    }

    /**
     * 文件去重
     *
     * @param fileInfoList
     * @return
     */
    private static List<Meta.FileConfigDTO.FileInfo> distinctFiles(List<Meta.FileConfigDTO.FileInfo> fileInfoList) {
        if (CollUtil.isEmpty(fileInfoList)) {
            return fileInfoList;
        }
        Collection<Meta.FileConfigDTO.FileInfo> values = fileInfoList.stream().collect(Collectors.toMap(Meta.FileConfigDTO.FileInfo::getInputPath,
                Function.identity(), (existing, replacement) -> replacement)).values();
        List<Meta.FileConfigDTO.FileInfo> distinctList = new ArrayList<>(values);
        return distinctList;
    }

    /**
     * 配置去重
     *
     * @param modelInfoList
     * @return
     */
    private static List<Meta.ModelConfigDTO.ModelInfo> distinctModels(List<Meta.ModelConfigDTO.ModelInfo> modelInfoList) {
        if (CollUtil.isEmpty(modelInfoList)) {
            return modelInfoList;
        }
        Collection<Meta.ModelConfigDTO.ModelInfo> values = modelInfoList.stream().collect(Collectors.toMap(Meta.ModelConfigDTO.ModelInfo::getFieldName,
                Function.identity(), (existing, replacement) -> replacement)).values();
        List<Meta.ModelConfigDTO.ModelInfo> distinctList = new ArrayList<>(values);
        return distinctList;
    }

    public static void main(String[] args) {
        String projectPath = System.getProperty("user.dir");
        String sourceRootPath = new File(projectPath).getParent() + File.separator + "yuzi-generator-demo-projects/springboot-init";
        // 项目基础信息
        Meta meta = new Meta();
        String projectName = "acm-template-generator";
        String description = "ACM模板生成器";
        meta.setName(projectName);
        meta.setDescription(description);
        // 输入文件路径（相对路径）
        // yuzi-generator-demo-projects/springboot-init/src/main/java/com/liucc/springbootinit
        String fileInputPath = "src/main/java/com/liucc/springbootinit";
        // 模型参数信息（首次）
//        Meta.ModelConfigDTO.ModelInfo modelInfo = new Meta.ModelConfigDTO.ModelInfo();
//        modelInfo.setFieldName("outputText");
//        modelInfo.setDefaultValue("Sum: ");
        // 模型参数信息（第二次）
        Meta.ModelConfigDTO.ModelInfo modelInfo = new Meta.ModelConfigDTO.ModelInfo();
        modelInfo.setFieldName("className");
        // 替换变量（首次）
        String str = "BaseResponse";
        // 替换变量（第二次）
//        String str = "MainTemplate";
        String inputFilePath1 = "src/main/java/com/liucc/springbootinit/common";
        String inputFilePath2 = "src/main/java/com/liucc/springbootinit/config";
        Long id = makeTemplate(1914326387714437120L, sourceRootPath, meta, Arrays.asList(inputFilePath1, inputFilePath2), modelInfo, str);
        System.out.println("id = " + id);
    }
}
