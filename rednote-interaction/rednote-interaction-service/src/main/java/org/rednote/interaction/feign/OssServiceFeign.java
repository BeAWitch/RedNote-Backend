package org.rednote.interaction.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.multipart.MultipartFile;

@FeignClient(name = "oss-service")
public interface OssServiceFeign {

    @PostMapping("/web/oss/uploadFile")
    String uploadFile(MultipartFile file);

    @PostMapping("/web/oss/uploadBase64")
    String uploadBase64(String base64String);
}
