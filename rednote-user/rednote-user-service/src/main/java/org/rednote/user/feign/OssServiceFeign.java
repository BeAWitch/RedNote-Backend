package org.rednote.user.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.multipart.MultipartFile;

@FeignClient(name = "oss-service")
public interface OssServiceFeign {

    @PostMapping("/web/oss/uploadFile")
    String uploadFile(MultipartFile file);
}
