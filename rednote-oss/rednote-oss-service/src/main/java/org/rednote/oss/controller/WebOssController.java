package org.rednote.oss.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.rednote.oss.service.IWebOssService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Tag(name = "文件存储", description = "文件存储相关接口")
@RequestMapping("/web/oss")
@RestController
@RequiredArgsConstructor
public class WebOssController {

    private final IWebOssService ossService;

    /**
     * 以下用于远程调用
     */

    @Operation(hidden = true)
    @PostMapping("uploadFile")
    String uploadFile(MultipartFile file) {
        return ossService.save(file);
    }

    @Operation(hidden = true)
    @PostMapping("uploadBase64")
    String uploadBase64(String base64String) {
        return ossService.save(base64String);
    }

    @Operation(hidden = true)
    @PostMapping("uploadBatchFiles")
    List<String> uploadBatchFiles(MultipartFile[] files) {
        return ossService.saveBatch(files);
    }
}
