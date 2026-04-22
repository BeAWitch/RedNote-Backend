package org.rednote.ai.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(name = "AI 文本翻译请求参数 DTO")
@Data
public class AITranslationRequestDTO {

    @Schema(description = "用户 ID")
    private Long userId;

    @Schema(description = "需要翻译的原文")
    private String originalText;

    @Schema(description = "目标语言，例如：英文、日文、繁体中文等")
    private String targetLanguage;
}
