package org.rednote.ai.router;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.rednote.ai.api.dto.AINoteOptimizeRequestDTO;
import org.rednote.ai.service.IAiNoteOptimizationService;
import org.rednote.ai.api.vo.AIResponseVO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class AiNoteOptimizationRouter {

    private final List<IAiNoteOptimizationService> noteOptimizationServices;

    @Value("${ai.provider.note-optimize:openai}")
    private String noteOptimizeProvider;

    public AIResponseVO routeAndOptimizeNote(AINoteOptimizeRequestDTO requestDTO) {
        log.info("AI 笔记优化路由 - 当前提供商配置: {}", noteOptimizeProvider);

        IAiNoteOptimizationService selectedService = noteOptimizationServices.stream()
                .filter(service -> service.provider().equalsIgnoreCase(noteOptimizeProvider))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("未找到对应的 AI 笔记优化服务提供商: " + noteOptimizeProvider));

        return selectedService.optimizeNote(requestDTO);
    }
}
