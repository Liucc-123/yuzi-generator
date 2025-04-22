package com.liucc.maker.template;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.io.FileUtil;
import com.liucc.maker.template.enums.FileFilterRangeEnum;
import com.liucc.maker.template.enums.FileFilterRuleEnum;
import com.liucc.maker.template.model.FileFilterConfig;
import com.liucc.maker.template.model.TemplateMakerFileConfig;

import java.io.File;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 文件过滤
 */
public class FileFilter {

    /**
     * 文件过滤
     *
     * @param filePath 可以是单个文件或目录的路径
     * @param fileFilterConfigs 过滤器配置
     * @return
     */
    public static List<File> doFilter(String filePath, List<FileFilterConfig> fileFilterConfigs){
        List<File> fileList = FileUtil.loopFiles(filePath);
        return fileList.stream()
                .filter(file -> doSingleFileFilter(fileFilterConfigs, file))
                .collect(Collectors.toList());
    }

    /**
     * 单个文件过滤
     * @param fileFilterConfigs 文件过滤器
     * @param file
     * @return
     */
    public static boolean doSingleFileFilter(List<FileFilterConfig> fileFilterConfigs, File file){
        // 没有过滤器
        if (CollUtil.isEmpty(fileFilterConfigs)){
            return true;
        }
        // 文件名称
        String fileName = file.getName();
        // 文件内容
        String fileContent = FileUtil.readUtf8String(file);
        boolean result = true;
        for (FileFilterConfig fileFilterConfig : fileFilterConfigs) {
            String range = fileFilterConfig.getRange();
            String rule = fileFilterConfig.getRule();
            String value = fileFilterConfig.getValue();

            FileFilterRangeEnum filterRangeEnum = FileFilterRangeEnum.getByType(range);
            FileFilterRuleEnum filterRuleEnum = FileFilterRuleEnum.getByType(rule);
            // 过滤规则不存在，跳过本次过滤
            if (filterRuleEnum == null){
                continue;
            }
            String content = fileName;
            switch (filterRangeEnum){
                case FILENAME:
                    content = fileName;
                    break;
                case FILE_CONTENT:
                    content = fileContent;
                    break;
                default:
                    break;
            }

            // 根据规则进行过滤
            switch (filterRuleEnum){
                case CONTAINS:
                    result = content.contains(value);
                    break;
                case START_WITH:
                    result = content.startsWith(value);
                    break;
                case END_WITH:
                    result = content.endsWith(value);
                    break;
                case REGEX:
                    result = content.matches(value);
                    break;
                case EQUALS:
                    result = content.equals(value);
                    break;
                default:
                    break;
            }
            return result;
        }
        return true;
    }
}
