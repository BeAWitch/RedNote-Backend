package org.rednote.oss.service.impl;

import cn.hutool.core.lang.UUID;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.rednote.oss.config.FileUploadConfig;
import org.rednote.oss.service.IWebOssService;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Base64;
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

    @Override
    public String save(String base64) {
        return save(base64ToMultipartFile(base64));
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

    /**
     * Base64 转 MultipartFile
     *
     * @param base64 base64
     * @return MultipartFile
     */
    private MultipartFile base64ToMultipartFile(String base64) {
        return new Base64MultipartFile(base64);
    }

    private static class Base64MultipartFile implements MultipartFile {

        private final byte[] content;
        private final String header;
        private final String suffix;

        /**
         * 构造方法
         * Base64 字符串转 MultipartFile
         *
         * @param base64 base64
         */
        public Base64MultipartFile(String base64) {
            String[] parts = base64.split(",");
            this.header = parts[0];
            this.content = Base64.getDecoder().decode(parts[1]);

            this.suffix = header.substring(
                    header.indexOf("/") + 1,
                    header.indexOf(";")
            );
        }

        @Override
        public String getName() {
            return "file";
        }

        @Override
        public String getOriginalFilename() {
            return UUID.randomUUID() + "." + suffix;
        }

        @Override
        public String getContentType() {
            return header.substring(
                    header.indexOf(":") + 1,
                    header.indexOf(";")
            );
        }

        @Override
        public boolean isEmpty() {
            return content.length == 0;
        }

        @Override
        public long getSize() {
            return content.length;
        }

        @Override
        public byte[] getBytes() {
            return content;
        }

        @Override
        public InputStream getInputStream() {
            return new ByteArrayInputStream(content);
        }

        @Override
        public void transferTo(File dest) throws IOException {
            Files.write(dest.toPath(), content);
        }
    }

}
