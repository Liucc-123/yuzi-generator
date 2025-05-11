package com.liucc.web.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.core.util.ZipUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.liucc.maker.generator.main.GeneratorTemplate;
import com.liucc.maker.generator.main.ZipGenerator;
import com.liucc.maker.meta.Meta;
import com.liucc.maker.meta.MetaValidator;
import com.liucc.web.common.ErrorCode;
import com.liucc.web.constant.CommonConstant;
import com.liucc.web.exception.BusinessException;
import com.liucc.web.exception.ThrowUtils;
import com.liucc.web.manager.CosManager;
import com.liucc.web.mapper.GeneratorMapper;
import com.liucc.web.model.dto.generator.GeneratorMakeRequest;
import com.liucc.web.model.dto.generator.GeneratorQueryRequest;
import com.liucc.web.model.dto.generator.GeneratorUseRequest;
import com.liucc.web.model.entity.Generator;
import com.liucc.web.model.entity.User;
import com.liucc.web.model.vo.GeneratorVO;
import com.liucc.web.model.vo.UserVO;
import com.liucc.web.service.GeneratorService;
import com.liucc.web.service.UserService;
import com.liucc.web.utils.SqlUtils;
import freemarker.template.TemplateException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.io.*;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 帖子服务实现
 *
 * @author liucc
 * @from <a href="https://github.com/dashboard">tiga</a>
 */
@Service
@Slf4j
public class GeneratorServiceImpl extends ServiceImpl<GeneratorMapper, Generator> implements GeneratorService {

    @Resource
    private UserService userService;
    @Resource
    private CosManager cosManager;

    @Override
    public void validGenerator(Generator generator, boolean add) {
        if (generator == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        String name = generator.getName();
        String description = generator.getDescription();
        String tags = generator.getTags();
        // 创建时，参数不能为空
        if (add) {
            ThrowUtils.throwIf(StringUtils.isAnyBlank(name, description, tags), ErrorCode.PARAMS_ERROR);
        }
        // 有参数则校验
        if (StringUtils.isNotBlank(name) && name.length() > 80) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "标题过长");
        }
        if (StringUtils.isNotBlank(description) && description.length() > 8192) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "内容过长");
        }
    }

    /**
     * 获取查询包装类
     *
     * @param generatorQueryRequest
     * @return
     */
    @Override
    public QueryWrapper<Generator> getQueryWrapper(GeneratorQueryRequest generatorQueryRequest) {
        QueryWrapper<Generator> queryWrapper = new QueryWrapper<>();
        if (generatorQueryRequest == null) {
            return queryWrapper;
        }
        String searchText = generatorQueryRequest.getSearchText();
        String name = generatorQueryRequest.getName();
        String sortField = generatorQueryRequest.getSortField();
        String sortOrder = generatorQueryRequest.getSortOrder();
        Long id = generatorQueryRequest.getId();
        String title = generatorQueryRequest.getTitle();
        String description = generatorQueryRequest.getDescription();
        List<String> tagList = generatorQueryRequest.getTags();
        Long userId = generatorQueryRequest.getUserId();
        // 拼接查询条件
        if (StringUtils.isNotBlank(searchText)) {
            queryWrapper.and(qw -> qw.like("title", searchText).or().like("content", searchText));
        }
        queryWrapper.like(StringUtils.isNotBlank(name), "name", name);
        queryWrapper.like(StringUtils.isNotBlank(title), "title", title);
        queryWrapper.like(StringUtils.isNotBlank(description), "description", description);
        if (CollUtil.isNotEmpty(tagList)) {
            for (String tag : tagList) {
                queryWrapper.like("tags", "\"" + tag + "\"");
            }
        }
        queryWrapper.eq(ObjectUtils.isNotEmpty(id), "id", id);
        queryWrapper.eq(ObjectUtils.isNotEmpty(userId), "userId", userId);
        queryWrapper.orderBy(SqlUtils.validSortField(sortField), sortOrder.equals(CommonConstant.SORT_ORDER_ASC),
                sortField);
        return queryWrapper;
    }

    @Override
    public GeneratorVO getGeneratorVO(Generator generator, HttpServletRequest request) {
        GeneratorVO generatorVO = GeneratorVO.objToVo(generator);
        long generatorId = generator.getId();
        // 1. 关联查询用户信息
        Long userId = generator.getUserId();
        User user = null;
        if (userId != null && userId > 0) {
            user = userService.getById(userId);
        }
        UserVO userVO = userService.getUserVO(user);
        generatorVO.setUser(userVO);
        return generatorVO;
    }

    @Override
    public Page<GeneratorVO> getGeneratorVOPage(Page<Generator> generatorPage, HttpServletRequest request) {
        List<Generator> generatorList = generatorPage.getRecords();
        Page<GeneratorVO> generatorVOPage = new Page<>(generatorPage.getCurrent(), generatorPage.getSize(), generatorPage.getTotal());
        if (CollUtil.isEmpty(generatorList)) {
            return generatorVOPage;
        }
        // 1. 关联查询用户信息
        Set<Long> userIdSet = generatorList.stream().map(Generator::getUserId).collect(Collectors.toSet());
        Map<Long, List<User>> userIdUserListMap = userService.listByIds(userIdSet).stream()
                .collect(Collectors.groupingBy(User::getId));
        // 填充信息
        List<GeneratorVO> generatorVOList = generatorList.stream().map(generator -> {
            GeneratorVO generatorVO = GeneratorVO.objToVo(generator);
            Long userId = generator.getUserId();
            User user = null;
            if (userIdUserListMap.containsKey(userId)) {
                user = userIdUserListMap.get(userId).get(0);
            }
            generatorVO.setUser(userService.getUserVO(user));
            return generatorVO;
        }).collect(Collectors.toList());
        generatorVOPage.setRecords(generatorVOList);
        return generatorVOPage;
    }

    @Override
    public File useGenerator(GeneratorUseRequest generatorUseRequest, String tempDirPath) {
        // 1、获取id对应的 生成器
        Long id = generatorUseRequest.getId();
        Generator generator = getById(id);
        if (BeanUtil.isEmpty(generator)) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }
        // 2、下载生成器到指定本地文件
        // 使用固定压缩名
        String zipPath = tempDirPath+ "/dist.zip";
        if (!FileUtil.exist(zipPath)){
            FileUtil.touch(zipPath);
        }
        try {
            cosManager.download(generator.getDistPath(), zipPath);
        } catch (InterruptedException e) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "下载失败");
        }
        // 3、解压文件，得到代码生成器项目
        File unzipDirFile = ZipUtil.unzip(zipPath);
        // 4、将用户填写的数据，写入到指定 json 文件
        String dataModelJsonPath = tempDirPath + File.separator + "dataModel.json";
        FileUtil.touch(dataModelJsonPath);
        FileUtil.writeUtf8String(JSONUtil.toJsonStr(generatorUseRequest.getDataModel()), dataModelJsonPath);
        // 5、执行脚本
        // 创建一个 ProcessBuilder 实例
        // 给脚本文件添加可执行权限
        List<File> files = FileUtil.loopFiles(unzipDirFile);
        File scriptFile = files.stream()
                .filter(file -> FileUtil.isFile(file) && StrUtil.equals(file.getName(), "generator"))
                .findFirst()
                .orElseThrow(() -> new BusinessException(ErrorCode.SYSTEM_ERROR, "脚本文件不存在"));
        scriptFile.setExecutable(true);
        String absoluteScriptPath  = scriptFile.getAbsolutePath();
        String[] commands = new String[]{"sh", absoluteScriptPath, "json-generate", "--file=" + dataModelJsonPath};
        ProcessBuilder processBuilder = new ProcessBuilder(commands);

        // 设置工作目录
        processBuilder.directory(unzipDirFile);
        // 启动进程
        Process process = null;
        try {
            process = processBuilder.start();
            InputStream inputStream = process.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
            }
            // 等待进程完成
            int exitCode = process.waitFor();
            System.out.println("脚本执行完成，退出码：" + exitCode);
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "脚本执行失败");
        }
        // 将生成的代码压缩，返回给前端
        String generatedDir = unzipDirFile + File.separator + "generated";
        File generatedZip = ZipUtil.zip(generatedDir);
        return generatedZip;
    }

    @Override
    public File makeGenerator(GeneratorMakeRequest generatorMakeRequest, String tempDirPath) {
        Meta meta = generatorMakeRequest.getMeta();
        // 原始模板文件
        String zipFilePath = generatorMakeRequest.getZipFilePath();
        if (StrUtil.isBlank(zipFilePath)){
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "模板文件路径不能为空");
        }
        // 1、下载模板文件
        // 使用固定压缩名
        String templateZipPath = tempDirPath+ "/template.zip";
        if (!FileUtil.exist(templateZipPath)){
            FileUtil.touch(templateZipPath);
        }
        try {
            cosManager.download(zipFilePath, templateZipPath);
        } catch (InterruptedException e) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "");
        }
        // 解压缩，得到项目模板文件
        File unzipDirFile = ZipUtil.unzip(templateZipPath);
        // 2、构造制作工具所需参数
        String sourceRootPath = unzipDirFile.getAbsolutePath();
        meta.getFileConfig().setSourceRootPath(sourceRootPath);
        // 制作代码生成器的生成位置
        String outputPath = String.format("%s"+ "/generated/%s", tempDirPath, meta.getName());
        // 校验和处理默认值
        MetaValidator.doValidAndFill(meta);
        // 3、调用 maker 工具，制作代码生成器
        GeneratorTemplate generatorTemplate = new ZipGenerator();
        try {
            generatorTemplate.doGenerate(meta, outputPath);
        } catch (Exception e) {
            e.printStackTrace();
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "代码生成器制作失败");
        }
        // 4、返回代码生成器压缩包（产物包）
        String suffix = "-dist.zip";
        String generatedZipPath = outputPath + suffix;
        return new File(generatedZipPath);
    }

}




