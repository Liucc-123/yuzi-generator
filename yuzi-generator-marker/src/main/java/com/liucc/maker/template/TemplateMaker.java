package com.liucc.maker.template;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.liucc.maker.meta.Meta;
import com.liucc.maker.meta.enums.FileGenerateTypeEnum;
import com.liucc.maker.meta.enums.FileTypeEnum;
import com.liucc.maker.template.enums.FileFilterRangeEnum;
import com.liucc.maker.template.enums.FileFilterRuleEnum;
import com.liucc.maker.template.model.*;

import java.io.File;
import java.nio.file.Paths;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 模板生成器，生成动态模板文件和元信息配置文件
 */
public class TemplateMaker {

    /**
     * 模板制作
     *
     * @param templateMakerConfig 模板制作配置 对象
     * @return
     */
    public static Long makeTemplate(TemplateMakerConfig templateMakerConfig) {
        Long id = templateMakerConfig.getId();
        String sourceRootPath = templateMakerConfig.getSourceRootPath();
        Meta meta = templateMakerConfig.getMeta();
        TemplateMakerFileConfig fileConfig = templateMakerConfig.getFileConfig();
        TemplateMakerModelConfig modelConfig = templateMakerConfig.getModelConfig();
        TemplateMakerOutputConfig outputConfig = templateMakerConfig.getOutputConfig();
        return makeTemplate(id, sourceRootPath, meta, fileConfig, modelConfig, outputConfig);
    }

    /**
     * 制作模板
     *
     * @param id                       id不存在，表示首次制作模板；id存在，更新模板
     * @param sourceRootPath           项目根路径
     * @param meta                     元信息
     * @param templateMakerFileConfig      文件过滤配置
     * @param templateMakerModelConfig 模型过滤配置
     * @param templateMakerOutputConfig 文件冲突处理配置
     * @return
     */
    public static Long makeTemplate(Long id, String sourceRootPath, Meta meta, TemplateMakerFileConfig templateMakerFileConfig,
                                    TemplateMakerModelConfig templateMakerModelConfig, TemplateMakerOutputConfig templateMakerOutputConfig) {
        if (id == null) {
            id = IdUtil.getSnowflakeNextId();
        }
        // 拷贝原始项目到临时目录（工作空间）中
        String projectPath = System.getProperty("user.dir");
        String tempFilePath = projectPath + File.separator + ".temp" + File.separator + id;
        // 非首次制作，不需要重复拷贝原始项目文件
        if (!FileUtil.exist(tempFilePath)) { // 首次制作
            FileUtil.mkdir(tempFilePath);
            FileUtil.copy(sourceRootPath, tempFilePath, true);
            sourceRootPath = tempFilePath + File.separator + FileUtil.getLastPathEle(Paths.get(sourceRootPath)).toString();
        } else { // 非首次制作
            // 说明工作空间项目文件已经存在，程序可以直接读取到，不需要用户重复在配置文件中指定 sourceRootPath 了。
            sourceRootPath = FileUtil.loopFiles(new File(tempFilePath), 1, null)
                    .stream()
                    .filter(File::isDirectory)
                    .findFirst()
                    .orElseThrow(RuntimeException::new)
                    .getAbsolutePath();

        }
        // windows系统需要对文件路径进行转移
        sourceRootPath = sourceRootPath.replaceAll("\\\\", "/");
        // 一、处理输入文件
        List<Meta.FileConfigDTO.FileInfo> newFileInfoList = makeTemplateFiles(sourceRootPath, templateMakerFileConfig, templateMakerModelConfig);
        // 二、处理模型信息
        List<Meta.ModelConfigDTO.ModelInfo> newModelInfoList = getModelInfos(templateMakerModelConfig);
        // 三、生成元信息配置文件
        generateMetaConfigFile(sourceRootPath, meta, tempFilePath, newFileInfoList, newModelInfoList, templateMakerOutputConfig);
        return id;
    }

    private static void generateMetaConfigFile(String sourceRootPath, Meta meta, String tempFilePath, List<Meta.FileConfigDTO.FileInfo> newFileInfoList,
                                               List<Meta.ModelConfigDTO.ModelInfo> newModelInfoList, TemplateMakerOutputConfig templateMakerOutputConfig) {
        String metaOutputPath = tempFilePath + File.separator + "meta.json";
        // 非首次制作，不需要重复输入已有的元信息，而是在此基础上，可以覆盖或追加元信息配置
        if (FileUtil.exist(metaOutputPath)) {
            // 1、构造配置参数
            Meta newMeta = JSONUtil.toBean(FileUtil.readUtf8String(metaOutputPath), Meta.class);
            // 文件配置
            List<Meta.FileConfigDTO.FileInfo> fileInfoList = newMeta.getFileConfig().getFiles();
            fileInfoList.addAll(newFileInfoList);
            // 模型配置
            List<Meta.ModelConfigDTO.ModelInfo> modelInfoList = newMeta.getModelConfig().getModels();
            modelInfoList.addAll(newModelInfoList);
            // 文件去重
            newMeta.getFileConfig().setFiles(distinctFiles(fileInfoList));
            // 配置去重
            newMeta.getModelConfig().setModels(distinctModels(modelInfoList));

            // 处理冲突文件
            if (templateMakerOutputConfig != null && templateMakerOutputConfig.isRemoveMakerOutputFromRoot()) {
                List<Meta.FileConfigDTO.FileInfo> resultList = TemplateMakerUtils.removeNotGroupFileFromRoot(newMeta.getFileConfig().getFiles());
                newMeta.getFileConfig().setFiles(resultList);
            }
            // 2、输出元信息配置文件
            FileUtil.writeUtf8String(JSONUtil.toJsonPrettyStr(newMeta), metaOutputPath);
        } else {
            // 1、构造配置参数
            Meta.FileConfigDTO fileConfig = new Meta.FileConfigDTO();
            meta.setFileConfig(fileConfig);
            fileConfig.setSourceRootPath(sourceRootPath);
            fileConfig.setType("dir");
            List<Meta.FileConfigDTO.FileInfo> fileInfoList = new ArrayList<>();
            fileInfoList.addAll(newFileInfoList);
            fileConfig.setFiles(fileInfoList);

            Meta.ModelConfigDTO modelConfig = new Meta.ModelConfigDTO();
            List<Meta.ModelConfigDTO.ModelInfo> modelInfoList = new ArrayList<>();
            modelInfoList.addAll(newModelInfoList);
            modelConfig.setModels(modelInfoList);
            meta.setModelConfig(modelConfig);
            // 2、输出元信息配置文件
            String metaJson = JSONUtil.toJsonPrettyStr(meta);
            FileUtil.writeUtf8String(metaJson, metaOutputPath);
        }
    }

        private static List<Meta.ModelConfigDTO.ModelInfo> getModelInfos(TemplateMakerModelConfig templateMakerModelConfig) {
        // - 本次新增的模型配置列表
        List<Meta.ModelConfigDTO.ModelInfo> newModelInfoList = new ArrayList<>();
        List<TemplateMakerModelConfig.ModelInfoConfig> models = templateMakerModelConfig.getModels();
        if (CollUtil.isEmpty(models)) {
            System.out.println("未填写模型配置");
            return newModelInfoList;
        }
        // - 转换为元信息可接受的ModelInfo对象
        List<Meta.ModelConfigDTO.ModelInfo> inputModelInfoList = models.stream().map(modelInfoConfig -> {
            Meta.ModelConfigDTO.ModelInfo modelInfo = new Meta.ModelConfigDTO.ModelInfo();
            BeanUtil.copyProperties(modelInfoConfig, modelInfo);
            return modelInfo;
        }).collect(Collectors.toList());

        // - 针对模型分组进行处理
        TemplateMakerModelConfig.ModelGroupConfig modelGroupConfig = templateMakerModelConfig.getModelGroupConfig();
        if (modelGroupConfig != null) {
            // 是分组，将所有的模型列表添加到分组里
            Meta.ModelConfigDTO.ModelInfo newModelInfo = new Meta.ModelConfigDTO.ModelInfo();
            BeanUtil.copyProperties(modelGroupConfig, newModelInfo);
            newModelInfo.setModels(inputModelInfoList);
            newModelInfoList.add(newModelInfo);
        } else {
            // 不是分组，添加所有的模型列表
            newModelInfoList.addAll(inputModelInfoList);
        }
        return newModelInfoList;
    }

    private static List<Meta.FileConfigDTO.FileInfo> makeTemplateFiles(String sourceRootPath, TemplateMakerFileConfig templateMakerFileConfig,
                                                                       TemplateMakerModelConfig templateMakerModelConfig) {
        // 新增文件信息列表
        List<Meta.FileConfigDTO.FileInfo> newFileInfoList = new ArrayList<>();
        List<TemplateMakerFileConfig.FileInfoConfig> fileInfoConfigList = templateMakerFileConfig.getFiles();
        if (CollUtil.isEmpty(fileInfoConfigList)) {
            System.out.println("文件配置列表为空 fileInfoConfigList：" + fileInfoConfigList);
            return newFileInfoList;
        }
        for (TemplateMakerFileConfig.FileInfoConfig fileInfoConfig : fileInfoConfigList) {
            String fileInputPath = fileInfoConfig.getPath(); // 相对路径
            String fileInputAbsolutePath = sourceRootPath + "/" + fileInputPath;
            // 文件过滤  获取所有符合条件的文件列表（都是文件，不存在目录）
            List<File> fileList = FileFilter.doFilter(fileInputAbsolutePath, fileInfoConfig.getFilters());
            // 过滤掉ftl后缀结尾的文件，避免在生成meta元信息文件中，出现将ftl文件作为输入路径的配置项
            fileList = fileList.stream().filter(file -> !file.getName().endsWith(".ftl")).collect(Collectors.toList());
            for (File file : fileList) {
                // 制作模板 FTL
                Meta.FileConfigDTO.FileInfo fileInfo = makeFileTemplate(sourceRootPath, file, templateMakerModelConfig, fileInfoConfig);
                newFileInfoList.add(fileInfo);
            }
        }

        // 新增文件组配置
        TemplateMakerFileConfig.FileGroupConfig fileGroupConfig = templateMakerFileConfig.getFileGroupConfig();
        if (BeanUtil.isNotEmpty(fileGroupConfig) && StrUtil.isNotBlank(fileGroupConfig.getGroupKey())) { // 说明是文件组
            String condition = fileGroupConfig.getCondition();
            String groupKey = fileGroupConfig.getGroupKey();
            String groupName = fileGroupConfig.getGroupName();

            Meta.FileConfigDTO.FileInfo fileInfoGroup = new Meta.FileConfigDTO.FileInfo();

            fileInfoGroup.setType(FileTypeEnum.GROUP.getType());
            fileInfoGroup.setCondition(condition);
            fileInfoGroup.setGroupKey(groupKey);
            fileInfoGroup.setGroupName(groupName);
            fileInfoGroup.setFiles(newFileInfoList);
            // 重置 fileInfos 为文件组
            newFileInfoList = new ArrayList<>();
            newFileInfoList.add(fileInfoGroup);
        }
        return newFileInfoList;
    }

    /**
     * 单次制作模板文件
     *
     * @param sourceRootPath           输入文件根路径
     * @param inputFile                输入文件
     * @param templateMakerModelConfig 支持一组模型参数对单个文件进行挖坑
     * @param fileInfoConfig           文件配置信息
     * @return
     */
    private static Meta.FileConfigDTO.FileInfo makeFileTemplate(String sourceRootPath, File inputFile, TemplateMakerModelConfig templateMakerModelConfig,
                                                                TemplateMakerFileConfig.FileInfoConfig fileInfoConfig) {
        String fileInputAbsolutePath = inputFile.getAbsolutePath();
        // windows系统需要对文件路径进行转移
        fileInputAbsolutePath = fileInputAbsolutePath.replaceAll("\\\\", "/");
        String fileInputPath = fileInputAbsolutePath.replace(sourceRootPath + "/", "");
        String fileOutputPath = fileInputPath + ".ftl";
        // 二、使用字符串替换算法，生成模板文件
        String fileOutputAbsolutePath = sourceRootPath + "/" + fileOutputPath;
        String originalContent;
        // 非首次制作，可以在已有模板文件的基础上再次挖坑
        boolean hasTemplateFile = FileUtil.exist(fileOutputAbsolutePath);
        if (hasTemplateFile) {
            originalContent = FileUtil.readUtf8String(fileOutputAbsolutePath);
        } else {
            originalContent = FileUtil.readUtf8String(fileInputAbsolutePath);
        }

        String newContent = originalContent;
        List<TemplateMakerModelConfig.ModelInfoConfig> models = templateMakerModelConfig.getModels();
        TemplateMakerModelConfig.ModelGroupConfig modelGroupConfig = templateMakerModelConfig.getModelGroupConfig();
        for (TemplateMakerModelConfig.ModelInfoConfig modelInfo : models) {
            if (modelGroupConfig == null) {
                // 不是分组
                String replacement = String.format("${%s}", modelInfo.getFieldName());
                newContent = StrUtil.replace(newContent, modelInfo.getReplaceText(), replacement);
            } else {
                // 是分组，挖坑时要注意多一个层级
                String groupKey = modelGroupConfig.getGroupKey();
                String replacement = String.format("${%s.%s}", groupKey, modelInfo.getFieldName());
                newContent = StrUtil.replace(newContent, modelInfo.getReplaceText(), replacement);
            }
        }
        Meta.FileConfigDTO.FileInfo fileInfo = new Meta.FileConfigDTO.FileInfo();
        fileInfo.setType(FileTypeEnum.FILE.getType());
        fileInfo.setInputPath(fileInputPath);
        fileInfo.setCondition(fileInfoConfig.getCondition());
        // 模板文件内容未发生变化，则生成静态文件
        // 模板内容发生变化 || 存在FTL文件，则生成动态文件
        boolean isChanged = !StrUtil.equals(originalContent, newContent);
        if (isChanged || hasTemplateFile) {
            fileInfo.setInputPath(fileOutputPath);
            fileInfo.setOutputPath(fileInputPath);
            fileInfo.setGenerateType(FileGenerateTypeEnum.DYNAMIC.getType());
            FileUtil.writeUtf8String(newContent, fileOutputAbsolutePath);
        } else {
            fileInfo.setOutputPath(fileInputPath);
            fileInfo.setGenerateType(FileGenerateTypeEnum.STATIC.getType());
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
        // 1、将所有的文件列表（fileInfo）分为有分组和无分组的；
        List<Meta.FileConfigDTO.FileInfo> groupFileList = fileInfoList.stream()
                .filter(file -> StrUtil.isNotBlank(file.getGroupKey()))
                .collect(Collectors.toList());
        // 2、对于有分组的文件配置，按照 groupKey 进行分组，同分组内的相同文件进行合并，不同分组的相同文件进行保留；
        // testA -> [file1, file2, file3], testA -> [file2, file3, file4], testB -> [file2, file3, file4]
        // ==> testA -> [file1, file2, file3, file4], testB -> [file2, file3, file4]
        Map<String, List<Meta.FileConfigDTO.FileInfo>> groupKeyFileInfoListMap = groupFileList.stream()
                .collect(Collectors.groupingBy(Meta.FileConfigDTO.FileInfo::getGroupKey));
        // 同组内的文件进行合并
        Map<String, Meta.FileConfigDTO.FileInfo> groupKeyMergedFileInfoMap = new HashMap<>();
        for (Map.Entry<String, List<Meta.FileConfigDTO.FileInfo>> entry : groupKeyFileInfoListMap.entrySet()) {
            List<Meta.FileConfigDTO.FileInfo> tempFileInfoList = entry.getValue();
            // 合并后的文件列表
            List<Meta.FileConfigDTO.FileInfo> newFileInfoList = new ArrayList<>(tempFileInfoList.stream().flatMap(fileInfo -> fileInfo.getFiles().stream())
                    .collect(
                            Collectors.toMap(Meta.FileConfigDTO.FileInfo::getOutputPath, o -> o, (existing, replacement) -> replacement)
                    ).values());
            // 更新 group 组配置，使用最后一个组配置进行覆盖
            Meta.FileConfigDTO.FileInfo newestFileInfo = CollUtil.getLast(tempFileInfoList);
            newestFileInfo.setFiles(newFileInfoList);
            String groupKey = entry.getKey();
            groupKeyMergedFileInfoMap.put(groupKey, newestFileInfo);
        }
        // 3、创建一个新的文件配置列表（结果列表），将合并后的分组添加到结果列表
        List<Meta.FileConfigDTO.FileInfo> resultList = new ArrayList<>();
        resultList.addAll(groupKeyMergedFileInfoMap.values());
        // 4、将无分组的文件配置添加到结果列表
        List<Meta.FileConfigDTO.FileInfo> noGroupFileList = new ArrayList<>(fileInfoList.stream()
                .filter(file -> StrUtil.isBlank(file.getGroupKey()))
                .collect(Collectors.toMap(Meta.FileConfigDTO.FileInfo::getOutputPath,
                        Function.identity(), (existing, replacement) -> replacement)).values());
        resultList.addAll(noGroupFileList);
        return resultList;
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
        // 1、将所有的模型列表（modelInfo）分为有分组和无分组的；
        List<Meta.ModelConfigDTO.ModelInfo> groupModelList = modelInfoList.stream()
                .filter(model -> StrUtil.isNotBlank(model.getGroupKey()))
                .collect(Collectors.toList());
        // 2、对于有分组的模型配置，按照 groupKey 进行分组，同分组内的相同模型进行合并，不同分组的相同模型进行保留；
        // testA -> [model1, model2, model3], testA -> [model2, model3, model4], testB -> [model2, model3, model4]
        // ==> testA -> [model1, model2, model3, model4], testB -> [model2, model3, model4]
        Map<String, List<Meta.ModelConfigDTO.ModelInfo>> groupKeyModelInfoListMap = groupModelList.stream()
                .collect(Collectors.groupingBy(Meta.ModelConfigDTO.ModelInfo::getGroupKey));
        // 同组内的模型进行合并
        Map<String, Meta.ModelConfigDTO.ModelInfo> groupKeyMergedModelInfoMap = new HashMap<>();
        for (Map.Entry<String, List<Meta.ModelConfigDTO.ModelInfo>> entry : groupKeyModelInfoListMap.entrySet()) {
            List<Meta.ModelConfigDTO.ModelInfo> tempModelInfoList = entry.getValue();
            // 合并后的模型列表
            List<Meta.ModelConfigDTO.ModelInfo> newModelInfoList = new ArrayList<>(tempModelInfoList.stream().flatMap(modelInfo -> modelInfo.getModels().stream())
                    .collect(
                            Collectors.toMap(Meta.ModelConfigDTO.ModelInfo::getFieldName, o -> o, (existing, replacement) -> replacement)
                    ).values());
            // 更新 group 组配置，使用最后一个组配置进行覆盖
            Meta.ModelConfigDTO.ModelInfo newestModelInfo = CollUtil.getLast(tempModelInfoList);
            newestModelInfo.setModels(newModelInfoList);
            String groupKey = entry.getKey();
            groupKeyMergedModelInfoMap.put(groupKey, newestModelInfo);
        }
        // 3、创建一个新的模型配置列表（结果列表），将合并后的分组添加到结果列表
        List<Meta.ModelConfigDTO.ModelInfo> resultList = new ArrayList<>();
        resultList.addAll(groupKeyMergedModelInfoMap.values());
        // 4、将无分组的模型配置添加到结果列表
        List<Meta.ModelConfigDTO.ModelInfo> noGroupModelList = new ArrayList<>(modelInfoList.stream()
                .filter(model -> StrUtil.isBlank(model.getGroupKey()))
                .collect(Collectors.toMap(Meta.ModelConfigDTO.ModelInfo::getFieldName,
                        Function.identity(), (existing, replacement) -> replacement)).values());
        resultList.addAll(noGroupModelList);
        return resultList;
    }
}
