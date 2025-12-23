package org.rednote.interaction.api.config;

import org.rednote.interaction.api.util.WebSocketServer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class WebSocketServerAutoConfiguration {

    @Bean
    public WebSocketServer webSocketServer() {
        return new WebSocketServer();
    }
}
