package org.rednote.ai;

import org.rednote.common.config.FeignAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

@SpringBootApplication(exclude = { DataSourceAutoConfiguration.class, FeignAutoConfiguration.class })
public class AIServiceApplication {
    public static void main(String[] args) {
        org.springframework.boot.SpringApplication.run(AIServiceApplication.class, args);
    }
}
