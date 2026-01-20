package org.rednote.ai.controller;

import lombok.RequiredArgsConstructor;
import org.rednote.ai.api.dto.AIRequestDTO;
import org.rednote.ai.api.vo.AIResponseVO;
import org.rednote.ai.service.IZhiPuAIService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/web/ai")
@RequiredArgsConstructor
public class AIController {

    private final IZhiPuAIService aiService;

    @PostMapping("generate")
    public AIResponseVO generate(@RequestBody AIRequestDTO aiRequestDTO) {
        return aiService.generate(aiRequestDTO);
    }
}