package org.rednote.oss;

import org.rednote.common.config.FeignAutoConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

@SpringBootApplication(exclude = { DataSourceAutoConfiguration.class, FeignAutoConfiguration.class })
public class OssServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(OssServiceApplication.class, args);
    }
}
