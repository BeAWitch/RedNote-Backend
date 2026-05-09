package org.rednote.ai.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(name = "生成大纲请求")
public class CreativeOutlineGenerateRequestDTO {

    @Schema(description = "创作项目 ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private String projectId;

    @Schema(description = "用户需求/主题", requiredMode = Schema.RequiredMode.REQUIRED)
    private String requirement;

    @Schema(description = "内容分类（可选）")
    private String category;

    @Schema(description = "风格/人设（可选）")
    private String style;

    @Schema(description = "语气（可选）")
    private String tone;
}
