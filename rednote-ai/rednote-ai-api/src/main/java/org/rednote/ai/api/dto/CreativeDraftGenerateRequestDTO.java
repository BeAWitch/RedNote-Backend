package org.rednote.ai.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(name = "生成成稿请求")
public class CreativeDraftGenerateRequestDTO {

    @Schema(description = "创作项目 ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private String projectId;

    @Schema(description = "大纲 ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private String outlineId;

    @Schema(description = "补充要求（可选）")
    private String extraRequirement;
}
