package org.rednote.ai.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(name = "AI 请求参数 DTO")
@Data
public class AIRequestDTO {

    @Schema(description = "用户输入")
    private String message;

    @Schema(description = "系统提示词")
    private String systemPrompt;

    @Schema(description = "用户 ID")
    private Long userId;
}
