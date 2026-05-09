package org.rednote.ai;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.cloud.openfeign.EnableFeignClients;

@EnableFeignClients
@SpringBootApplication(exclude = { DataSourceAutoConfiguration.class })
public class AIServiceApplication {
    public static void main(String[] args) {
        org.springframework.boot.SpringApplication.run(AIServiceApplication.class, args);
    }
}
