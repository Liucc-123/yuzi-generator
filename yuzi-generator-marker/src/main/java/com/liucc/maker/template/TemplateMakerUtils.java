package com.liucc.maker.template;

import cn.hutool.core.util.StrUtil;
import com.liucc.maker.meta.Meta;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 模板制作工具类
 */
public class TemplateMakerUtils {

    /**
     * 文件冲突处理，保留组内同名文件
     *
     * @param fileInfoList
     * @return
     */
    public static List<Meta.FileConfigDTO.FileInfo> removeNotGroupFileFromRoot(List<Meta.FileConfigDTO.FileInfo> fileInfoList){
        // 1、获取所有分组文件
        List<Meta.FileConfigDTO.FileInfo> groupFileList = fileInfoList.stream()
                .filter(file -> StrUtil.isNotBlank(file.getGroupKey()))
                .collect(Collectors.toList());
        // 2、将所有分组文件打散到一个列表中
        List<Meta.FileConfigDTO.FileInfo> groupFileFlattenList = groupFileList.stream().flatMap(file -> file.getFiles().stream()).collect(Collectors.toList());
        // 3、获取所有分组文件的inputPath 集合
        Set<String> groupInputPathSet = groupFileFlattenList.stream().map(file -> file.getInputPath()).collect(Collectors.toSet());
        // 4、获取所有未分组文件，过滤掉 inputPath 包含在 分组文件inputPath集合 的文件列表
        return fileInfoList.stream()
                .filter(fileInfo -> !groupInputPathSet.contains(fileInfo.getInputPath()))
                .collect(Collectors.toList());
    }
}
