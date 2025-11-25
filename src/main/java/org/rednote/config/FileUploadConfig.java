package org.rednote.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@Data
public class FileUploadConfig implements WebMvcConfigurer {

    /**
     * 文件类型
     */
    @Value("${oss.type}")
    private Integer type;

    /**
     * 文件保存路径
     */
    @Value("${file.upload.path}")
    private String path;

    /**
     * 文件虚拟地址
     */
    private String virtualPathPrefix = "/uploads/";

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler(virtualPathPrefix + "**")
                .addResourceLocations("file:" + path);
    }
}
