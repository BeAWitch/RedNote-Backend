package org.rednote.interaction.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

@FeignClient(name = "oss-service")
public interface OssServiceFeign {

    @PostMapping(value = "/web/oss/uploadFile", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    String uploadFile(@RequestPart("file") MultipartFile file);

    @PostMapping("/web/oss/uploadBase64")
    String uploadBase64(@RequestBody String base64String);
}
