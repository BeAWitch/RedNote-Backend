package org.rednote.oss.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.rednote.oss.service.IWebOssService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Tag(name = "文件存储", description = "文件存储相关接口")
@RequestMapping("/web/oss")
@RestController
@RequiredArgsConstructor
public class WebOssController {

    private final IWebOssService ossService;

    @PostMapping(value = "uploadFile", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    String uploadFile(@RequestPart("file") MultipartFile file) {
        return ossService.save(file);
    }

    @PostMapping("uploadBase64")
    String uploadBase64(@RequestBody String base64String) {
        return ossService.save(base64String);
    }

    @PostMapping(value = "uploadBatchFiles", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    List<String> uploadBatchFiles(@RequestPart("files") MultipartFile[] files) {
        return ossService.saveBatch(files);
    }

    @PostMapping("deleteFile")
    void deleteFile(@RequestBody String path) {
        ossService.delete(path);
    }

    @PostMapping("deleteBatchFiles")
    void deleteBatchFiles(@RequestBody List<String> paths) {
        ossService.batchDelete(paths);
    }
}
