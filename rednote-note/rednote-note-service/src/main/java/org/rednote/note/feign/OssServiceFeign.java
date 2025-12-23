package org.rednote.note.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@FeignClient(name = "oss-service")
public interface OssServiceFeign {

    @PostMapping("/web/oss/uploadFile")
    String uploadFile(MultipartFile file);

    @PostMapping("/web/oss/uploadBase64")
    String uploadBase64(String base64String);

    @PostMapping("/web/oss/uploadBatchFiles")
    List<String> uploadBatchFiles(MultipartFile[] files);

    @PostMapping("/web/oss/deleteFile")
    void deleteFile(String path);

    @PostMapping("/web/oss/deleteBatchFiles")
    void deleteBatchFiles(List<String> paths);
}
