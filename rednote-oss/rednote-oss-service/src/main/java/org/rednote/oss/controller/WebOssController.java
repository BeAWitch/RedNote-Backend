package org.rednote.oss.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
public class WebOssController {

    private final IWebOssService ossService;

    /**
     * 以下用于远程调用
     */

    @Operation(hidden = true)
    @PostMapping(value = "uploadFile", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    String uploadFile(@RequestPart("file") MultipartFile file) {
        return ossService.save(file);
    }

    @Operation(hidden = true)
    @PostMapping("uploadBase64")
    String uploadBase64(@RequestBody String base64String) {
        return ossService.save(base64String);
    }

    @Operation(hidden = true)
    @PostMapping(value = "uploadBatchFiles", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    List<String> uploadBatchFiles(@RequestPart("files") MultipartFile[] files) {
        log.info("上传文件数量：{}", files.length);
        return ossService.saveBatch(files);
    }

    @Operation(hidden = true)
    @PostMapping("deleteFile")
    void deleteFile(@RequestBody String path) {
        ossService.delete(path);
    }

    @Operation(hidden = true)
    @PostMapping("deleteBatchFiles")
    void deleteBatchFiles(@RequestBody List<String> paths) {
        ossService.batchDelete(paths);
    }
}
