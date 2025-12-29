package org.rednote.user.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.multipart.MultipartFile;

@FeignClient(name = "oss-service")
public interface OssServiceFeign {

    @PostMapping(value = "/web/oss/uploadFile", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    String uploadFile(MultipartFile file);
}
