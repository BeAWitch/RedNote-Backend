package org.rednote.ai.router;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.rednote.ai.api.dto.AIRequestDTO;
import org.rednote.ai.api.service.IAiTextGenerationService;
import org.rednote.ai.api.vo.AIResponseVO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class AiTextGenerationRouter {

    private final List<IAiTextGenerationService> textGenerationServices;

    @Value("${ai.provider.text:zhipu}")
    private String textProvider;

    public AIResponseVO routeAndGenerateText(AIRequestDTO requestDTO) {
        log.info("AI 文本生成路由 - 当前提供商配置: {}", textProvider);
        
        IAiTextGenerationService selectedService = textGenerationServices.stream()
                .filter(service -> service.provider().equalsIgnoreCase(textProvider))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("未找到对应的 AI 服务提供商: " + textProvider));
                
        return selectedService.generateText(requestDTO);
    }
}
