package org.rednote.note.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@FeignClient(name = "oss-service")
public interface OssServiceFeign {

    @PostMapping(value = "/web/oss/uploadFile", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    String uploadFile(@RequestPart("file") MultipartFile file);

    @PostMapping("/web/oss/uploadBase64")
    String uploadBase64(@RequestBody String base64String);

    @PostMapping(value = "/web/oss/uploadBatchFiles", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    List<String> uploadBatchFiles(@RequestPart("files") MultipartFile[] files);

    @PostMapping("/web/oss/deleteFile")
    void deleteFile(@RequestBody String path);

    @PostMapping("/web/oss/deleteBatchFiles")
    void deleteBatchFiles(@RequestBody List<String> paths);
}
