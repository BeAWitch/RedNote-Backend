package org.rednote.ai.router;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.rednote.ai.api.dto.AITranslationRequestDTO;
import org.rednote.ai.service.IAiTranslationService;
import org.rednote.ai.api.vo.AIResponseVO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class AiTranslationRouter {

    private final List<IAiTranslationService> translationServices;

    @Value("${ai.provider.translate:openai}")
    private String translateProvider;

    public AIResponseVO routeAndTranslate(AITranslationRequestDTO requestDTO) {
        log.info("AI 文本翻译路由 - 当前提供商配置: {}", translateProvider);

        IAiTranslationService selectedService = translationServices.stream()
                .filter(service -> service.provider().equalsIgnoreCase(translateProvider))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("未找到对应的 AI 文本翻译服务提供商: " + translateProvider));

        return selectedService.translate(requestDTO);
    }
}
