package org.rednote.service.impl;

import cn.hutool.core.lang.UUID;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.rednote.config.FileUploadConfig;
import org.rednote.service.IWebOssService;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * OSS
 *
 * TODO 云存储
 */
@Service
@RequiredArgsConstructor
public class WebOssServiceImpl implements IWebOssService {

    private final FileUploadConfig fileUploadConfig;

    /**
     * 上传文件
     *
     * @param file 文件
     */
    @SneakyThrows
    @Override
    public String save(MultipartFile file) {
        Integer type = fileUploadConfig.getType();
        switch (type) {
            case 0:
                // 本地存储
                return localSave(file);
            default:
                throw new IllegalArgumentException("不支持的存储类型: " + type);
        }
    }

    /**
     * 批量上传文件
     *
     * @param files 文件集
     */
    @Override
    public List<String> saveBatch(MultipartFile[] files) {
        List<String> result = new ArrayList<>();
        // 需要进行加锁，不然会出现多次添加
        for (MultipartFile file : files) {
            result.add(this.save(file));
        }
        return result;
    }

    private String localSave(MultipartFile file) throws IOException {
        String path = fileUploadConfig.getPath();
        File directory = new File(path);
        if (!directory.exists()) {
            directory.mkdirs();
        }

        String originalFilename = file.getOriginalFilename();
        String fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
        String fileName = System.currentTimeMillis() + "_" + UUID.randomUUID() + fileExtension;

        File dest = new File(path + File.separator + fileName);
        file.transferTo(dest);

        return fileUploadConfig.getVirtualPathPrefix() + fileName; // 返回虚拟访问路径
    }

    /**
     * 删除文件
     *
     * @param path 路径
     */
    @Override
    public void delete(String path) {
        Integer type = fileUploadConfig.getType();
        switch (type) {
            case 0:
                // 本地删除图片
                localDelete(path);
                break;
            default:
                throw new IllegalArgumentException("不支持的存储类型: " + type);
        }
    }

    private void localDelete(String path) {
        int index = path.lastIndexOf('/');
        String fileName = path.substring(index + 1);
        File file = new File(fileUploadConfig.getPath() + File.separator + fileName);
        if (file.exists()) {
            file.delete();
        }
    }

    /**
     * 批量删除文件
     *
     * @param filePaths 文件路径集
     */
    @Override
    public void batchDelete(List<String> filePaths) {
        for (String path : filePaths) {
            delete(path);
        }
    }
}
