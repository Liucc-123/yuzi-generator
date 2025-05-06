package com.liucc.web.controller;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.core.util.ZipUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.liucc.web.annotation.AuthCheck;
import com.liucc.web.common.BaseResponse;
import com.liucc.web.common.DeleteRequest;
import com.liucc.web.common.ErrorCode;
import com.liucc.web.common.ResultUtils;
import com.liucc.web.constant.UserConstant;
import com.liucc.web.exception.BusinessException;
import com.liucc.web.exception.ThrowUtils;
import com.liucc.web.manager.CosManager;
import com.liucc.web.meta.Meta;
import com.liucc.web.model.dto.generator.*;
import com.liucc.web.model.entity.Generator;
import com.liucc.web.model.entity.User;
import com.liucc.web.model.vo.GeneratorVO;
import com.liucc.web.service.GeneratorService;
import com.liucc.web.service.UserService;
import com.qcloud.cos.model.COSObject;
import com.qcloud.cos.utils.IOUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.CharSet;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.List;

/**
 * 生成器接口
 *
 * @author <a href="https://github.com/liliucc">程序员鱼皮</a>
 * @from <a href="https://liucc.icu">编程导航知识星球</a>
 */
@RestController
@RequestMapping("/generator")
@Slf4j
public class GeneratorController {

    @Resource
    private GeneratorService generatorService;

    @Resource
    private UserService userService;

    @Resource
    private CosManager cosManager;

    // region 增删改查

    @PostMapping("/use")
    public void useGenerator(@RequestBody GeneratorUseRequest generatorUseRequest, HttpServletRequest request, HttpServletResponse response) throws IOException {

        // 参数合法性校验
        Long id = generatorUseRequest.getId();
        if (id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        if (BeanUtil.isEmpty(loginUser)) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        // 追踪事件
        log.info("生成器 {} 下载了id 为{} 的生成器", loginUser, id);

        String ProjectPath = System.getProperty("user.dir");
        // 独立工作空间
        String tempDirPath = String.format("%s/.temp/use/%s", ProjectPath, id);
        File generatedZip = generatorService.useGenerator(generatorUseRequest, tempDirPath);
        FileUtil.writeToStream(generatedZip, response.getOutputStream());
        // 最后清理临时文件
        FileUtil.del(tempDirPath);
    }

    @GetMapping("/download")
    public void download(Long id, HttpServletRequest request, HttpServletResponse response) throws IOException {
        // 参数合法性校验
        if (id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        if (BeanUtil.isEmpty(loginUser)) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        // 获取对应的generator
        Generator generator = generatorService.getById(id);
        if (BeanUtil.isEmpty(generator)) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }
        // 获取产物包路径(相对)
        String filePath = generator.getDistPath();
        // 追踪事件
        log.info("生成器 {} 下载了 {}", loginUser, generator);


        // 获取文件输入流
        InputStream cosObjectInput = null;
        // 获取文件对象
        COSObject cosObject = cosManager.getObject(filePath);
        try {
            // 获取文件内容
            cosObjectInput = cosObject.getObjectContent();
            // 处理下载到的流
            // 这里是直接读取，按实际情况来处理
            byte[] bytes = null;
            bytes = IOUtils.toByteArray(cosObjectInput);
            // 设置响应头
            response.setContentType("application/octet-stream");
            response.setHeader("Content-Disposition", "attachment; filename=" + filePath);
            response.setContentLength(bytes.length);
            // 将文件内容写入响应流
            response.getOutputStream().write(bytes);
            response.getOutputStream().flush();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cosObjectInput != null) {
                cosObjectInput.close();
            }
        }
    }

    /**
     * 创建
     *
     * @param generatorAddRequest
     * @param request
     * @return
     */
    @PostMapping("/add")
    public BaseResponse<Long> addGenerator(@RequestBody GeneratorAddRequest generatorAddRequest, HttpServletRequest request) {
        if (generatorAddRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Generator generator = new Generator();
        BeanUtils.copyProperties(generatorAddRequest, generator);
        List<String> tags = generatorAddRequest.getTags();
        if (tags != null) {
            generator.setTags(JSONUtil.toJsonStr(tags));
        }
        // modelConfig
        Meta.ModelConfigDTO modelConfig = generatorAddRequest.getModelConfig();
        if(BeanUtil.isNotEmpty(modelConfig)){
            generator.setModelConfig(JSONUtil.toJsonStr(modelConfig));
        }
        // fileConfig
        Meta.FileConfigDTO fileConfig = generatorAddRequest.getFileConfig();
        if(BeanUtil.isNotEmpty(fileConfig)){
            generator.setFileConfig(JSONUtil.toJsonStr(fileConfig));
        }
        generatorService.validGenerator(generator, true);
        User loginUser = userService.getLoginUser(request);
        generator.setUserId(loginUser.getId());
        boolean result = generatorService.save(generator);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        long newGeneratorId = generator.getId();
        return ResultUtils.success(newGeneratorId);
    }

    /**
     * 删除
     *
     * @param deleteRequest
     * @param request
     * @return
     */
    @PostMapping("/delete")
    public BaseResponse<Boolean> deleteGenerator(@RequestBody DeleteRequest deleteRequest, HttpServletRequest request) {
        if (deleteRequest == null || deleteRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User user = userService.getLoginUser(request);
        long id = deleteRequest.getId();
        // 判断是否存在
        Generator oldGenerator = generatorService.getById(id);
        ThrowUtils.throwIf(oldGenerator == null, ErrorCode.NOT_FOUND_ERROR);
        // 仅本人或管理员可删除
        if (!oldGenerator.getUserId().equals(user.getId()) && !userService.isAdmin(request)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        boolean b = generatorService.removeById(id);
        return ResultUtils.success(b);
    }

    /**
     * 更新（仅管理员）
     *
     * @param generatorUpdateRequest
     * @return
     */
    @PostMapping("/update")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> updateGenerator(@RequestBody GeneratorUpdateRequest generatorUpdateRequest) {
        if (generatorUpdateRequest == null || generatorUpdateRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Generator generator = new Generator();
        BeanUtils.copyProperties(generatorUpdateRequest, generator);
        List<String> tags = generatorUpdateRequest.getTags();
        if (tags != null) {
            generator.setTags(JSONUtil.toJsonStr(tags));
        }
        // 参数校验
        generatorService.validGenerator(generator, false);
        long id = generatorUpdateRequest.getId();
        // 判断是否存在
        Generator oldGenerator = generatorService.getById(id);
        ThrowUtils.throwIf(oldGenerator == null, ErrorCode.NOT_FOUND_ERROR);
        boolean result = generatorService.updateById(generator);
        return ResultUtils.success(result);
    }

    /**
     * 根据 id 获取
     *
     * @param id
     * @return
     */
    @GetMapping("/get/vo")
    public BaseResponse<GeneratorVO> getGeneratorVOById(long id, HttpServletRequest request) {
        if (id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Generator generator = generatorService.getById(id);
        if (generator == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }
        return ResultUtils.success(generatorService.getGeneratorVO(generator, request));
    }

    /**
     * 分页获取列表（仅管理员）
     *
     * @param generatorQueryRequest
     * @return
     */
    @PostMapping("/list/page")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Page<Generator>> listGeneratorByPage(@RequestBody GeneratorQueryRequest generatorQueryRequest) {
        long current = generatorQueryRequest.getCurrent();
        long size = generatorQueryRequest.getPageSize();
        Page<Generator> generatorPage = generatorService.page(new Page<>(current, size),
                generatorService.getQueryWrapper(generatorQueryRequest));
        return ResultUtils.success(generatorPage);
    }

    /**
     * 分页获取列表（封装类）
     *
     * @param generatorQueryRequest
     * @param request
     * @return
     */
    @PostMapping("/list/page/vo")
    public BaseResponse<Page<GeneratorVO>> listGeneratorVOByPage(@RequestBody GeneratorQueryRequest generatorQueryRequest,
                                                                 HttpServletRequest request) {
        long current = generatorQueryRequest.getCurrent();
        long size = generatorQueryRequest.getPageSize();
        // 限制爬虫
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
        Page<Generator> generatorPage = generatorService.page(new Page<>(current, size),
                generatorService.getQueryWrapper(generatorQueryRequest));
        return ResultUtils.success(generatorService.getGeneratorVOPage(generatorPage, request));
    }

    /**
     * 分页获取当前生成器创建的资源列表
     *
     * @param generatorQueryRequest
     * @param request
     * @return
     */
    @PostMapping("/my/list/page/vo")
    public BaseResponse<Page<GeneratorVO>> listMyGeneratorVOByPage(@RequestBody GeneratorQueryRequest generatorQueryRequest,
                                                                   HttpServletRequest request) {
        if (generatorQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        generatorQueryRequest.setUserId(loginUser.getId());
        long current = generatorQueryRequest.getCurrent();
        long size = generatorQueryRequest.getPageSize();
        // 限制爬虫
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
        Page<Generator> generatorPage = generatorService.page(new Page<>(current, size),
                generatorService.getQueryWrapper(generatorQueryRequest));
        return ResultUtils.success(generatorService.getGeneratorVOPage(generatorPage, request));
    }

    /**
     * 编辑（生成器）
     *
     * @param generatorEditRequest
     * @param request
     * @return
     */
    @PostMapping("/edit")
    public BaseResponse<Boolean> editGenerator(@RequestBody GeneratorEditRequest generatorEditRequest, HttpServletRequest request) {
        if (generatorEditRequest == null || generatorEditRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Generator generator = new Generator();
        BeanUtils.copyProperties(generatorEditRequest, generator);
        List<String> tags = generatorEditRequest.getTags();
        if (tags != null) {
            generator.setTags(JSONUtil.toJsonStr(tags));
        }
        // modelConfig
        Meta.ModelConfigDTO modelConfig = generatorEditRequest.getModelConfig();
        if(BeanUtil.isNotEmpty(modelConfig)){
            generator.setModelConfig(JSONUtil.toJsonStr(modelConfig));
        }
        // fileConfig
        Meta.FileConfigDTO fileConfig = generatorEditRequest.getFileConfig();
        if(BeanUtil.isNotEmpty(fileConfig)){
            generator.setFileConfig(JSONUtil.toJsonStr(fileConfig));
        }
        // 参数校验
        generatorService.validGenerator(generator, false);
        User loginUser = userService.getLoginUser(request);
        long id = generatorEditRequest.getId();
        // 判断是否存在
        Generator oldGenerator = generatorService.getById(id);
        ThrowUtils.throwIf(oldGenerator == null, ErrorCode.NOT_FOUND_ERROR);
        // 仅本人或管理员可编辑
        if (!oldGenerator.getUserId().equals(loginUser.getId()) && !userService.isAdmin(loginUser)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        boolean result = generatorService.updateById(generator);
        return ResultUtils.success(result);
    }

}
