package org.rednote.interaction.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.multipart.MultipartFile;

@FeignClient(name = "oss-service")
public interface OssServiceFeign {

    @PostMapping("/oss/uploadFile")
    String uploadFile(MultipartFile file);

    @PostMapping("/oss/uploadFile")
    String uploadFile(String base64String);
}
