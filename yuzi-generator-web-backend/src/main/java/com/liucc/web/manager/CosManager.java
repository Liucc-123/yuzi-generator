package com.liucc.web.manager;

import cn.hutool.core.collection.CollUtil;
import com.liucc.web.config.CosClientConfig;
import com.qcloud.cos.COSClient;
import com.qcloud.cos.model.*;
import com.qcloud.cos.transfer.Download;
import com.qcloud.cos.transfer.TransferManager;
import com.qcloud.cos.utils.IOUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Cos 对象存储操作
 *
 * @author liucc
 * @from <a href="https://github.com/dashboard">tiga</a>
 */
@Component
@Slf4j
public class CosManager {

    @Resource
    private CosClientConfig cosClientConfig;

    @Resource
    private COSClient cosClient;
    private TransferManager transferManager;

    // bean 加载后执行
    @PostConstruct
    public void init() {
        // 自定义线程池大小，建议在客户端与 COS 网络充足（例如使用腾讯云的 CVM，同地域上传 COS）的情况下，设置成16或32即可，可较充分的利用网络资源
        // 对于使用公网传输且网络带宽质量不高的情况，建议减小该值，避免因网速过慢，造成请求超时。
        ExecutorService threadPool = Executors.newFixedThreadPool(32);
        // 传入一个 threadpool, 若不传入线程池，默认 TransferManager 中会生成一个单线程的线程池。
        transferManager = new TransferManager(cosClient, threadPool);
    }

    /**
     * 下载对象到本地文件
     *
     * @param key           下载对象相对路径
     * @param localFilepath 本地文件路径
     * @return
     */
    public Download download(String key, String localFilepath) throws InterruptedException {
        GetObjectRequest getObjectRequest = new GetObjectRequest(cosClientConfig.getBucket(), key);
        // 返回一个异步结果 Download, 可同步的调用 waitForCompletion 等待下载结束, 成功返回 void, 失败抛出异常
        File downloadFile = new File(localFilepath);
        Download download = transferManager.download(getObjectRequest, downloadFile);
        download.waitForCompletion();
        return download;
    }

    /**
     * 上传对象
     *
     * @param key           唯一键
     * @param localFilePath 本地文件路径
     * @return
     */
    public PutObjectResult putObject(String key, String localFilePath) {
        PutObjectRequest putObjectRequest = new PutObjectRequest(cosClientConfig.getBucket(), key,
                new File(localFilePath));
        return cosClient.putObject(putObjectRequest);
    }

    /**
     * 上传对象
     *
     * @param key  唯一键
     * @param file 文件
     * @return
     */
    public PutObjectResult putObject(String key, File file) {
        PutObjectRequest putObjectRequest = new PutObjectRequest(cosClientConfig.getBucket(), key,
                file);
        return cosClient.putObject(putObjectRequest);
    }

    /**
     * 下载对象
     *
     * @param key 对象键(Key)是对象在存储桶中的唯一标识。详情请参见 [对象键](https://cloud.tencent.com/document/product/436/13324)
     */
    public COSObject getObject(String key) {

        GetObjectRequest getObjectRequest = new GetObjectRequest(cosClientConfig.getBucket(), key);
        COSObject cosObject = cosClient.getObject(getObjectRequest);
        return cosObject;
    }

    /**
     * 删除单个对象
     *
     * @param key
     */
    public void deletebject(String key) {
        cosClient.deleteObject(cosClientConfig.getBucket(), key);
    }

    /**
     * 批量删除对象
     *
     * @param keys key不能以 / 或者 \ 开头
     */
    public void deleteObjects(List<String> keys) {

        DeleteObjectsRequest deleteObjectsRequest = new DeleteObjectsRequest(cosClientConfig.getBucket());
        // 设置要删除的key列表, 最多一次删除1000个
        ArrayList<DeleteObjectsRequest.KeyVersion> keyList = new ArrayList<>();
        // 传入要删除的文件名
        // 注意文件名不允许以正斜线/或者反斜线\开头，例如：
        // 存储桶目录下有a/b/c.txt文件，如果要删除，只能是 keyList.add(new KeyVersion("a/b/c.txt")),
        // 若使用 keyList.add(new KeyVersion("/a/b/c.txt"))会导致删除不成功
        for (String key : keys) {
            keyList.add(new DeleteObjectsRequest.KeyVersion(key));
        }
        deleteObjectsRequest.setKeys(keyList);
        DeleteObjectsResult deleteObjectsResult = cosClient.deleteObjects(deleteObjectsRequest);
        List<DeleteObjectsResult.DeletedObject> deletedObjects = deleteObjectsResult.getDeletedObjects();
        log.info("deletedObjects:{}", deletedObjects);
    }

    /**
     * 删除目录
     *
     * @param delPrefix 删除目录前缀 一定要带上后缀。正确：/test/，错误：/test
     *                  /test会把所有以 test 开头的文件全部删除
     */
    public void deleteDir(String delPrefix) {
        ListObjectsRequest listObjectsRequest = new ListObjectsRequest();
        listObjectsRequest.setBucketName(cosClientConfig.getBucket());
        listObjectsRequest.setPrefix(delPrefix);
        listObjectsRequest.setMaxKeys(1000);
        ObjectListing objectListing = null;
        // 删除请求对象
        DeleteObjectsRequest deleteObjectsRequest = new DeleteObjectsRequest(cosClientConfig.getBucket());

        do {
            objectListing = cosClient.listObjects(listObjectsRequest);
            List<COSObjectSummary> objectSummaries = objectListing.getObjectSummaries();
            if (CollUtil.isEmpty(objectSummaries)) {
                break;
            }
            for (COSObjectSummary objectSummary : objectSummaries) {
                // 待删除的对象集合  重置
                List<DeleteObjectsRequest.KeyVersion> toDeleteKeyList = new ArrayList<>();
                toDeleteKeyList.add(new DeleteObjectsRequest.KeyVersion(objectSummary.getKey()));
                deleteObjectsRequest.setKeys(toDeleteKeyList);
                // 批量删除指定目录下的所有对象
                cosClient.deleteObjects(deleteObjectsRequest);
            }
            String nextMarker = listObjectsRequest.getMarker();
            listObjectsRequest.setMarker(nextMarker);
        } while (objectListing.isTruncated());
    }

}
