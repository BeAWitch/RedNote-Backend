package org.rednote.ai.controller;

import lombok.RequiredArgsConstructor;
import org.rednote.ai.api.dto.AINoteOptimizeRequestDTO;
import org.rednote.ai.api.dto.AIRequestDTO;
import org.rednote.ai.api.vo.AIResponseVO;
import org.rednote.ai.router.AiNoteOptimizationRouter;
import org.rednote.ai.router.AiTextGenerationRouter;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/web/ai")
@RequiredArgsConstructor
public class AIController {

    private final AiTextGenerationRouter aiTextGenerationRouter;
    private final AiNoteOptimizationRouter aiNoteOptimizationRouter;

    @PostMapping("generate")
    public AIResponseVO generate(@RequestBody AIRequestDTO aiRequestDTO) {
        return aiTextGenerationRouter.routeAndGenerateText(aiRequestDTO);
    }

    @PostMapping("optimize-note")
    public AIResponseVO optimizeNote(@RequestBody AINoteOptimizeRequestDTO aiNoteOptimizeRequestDTO) {
        return aiNoteOptimizationRouter.routeAndOptimizeNote(aiNoteOptimizeRequestDTO);
    }
}