package org.rednote.ai.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(name = "创作项目创建响应")
public class CreativeProjectCreateResponseDTO {

    @Schema(description = "创作项目 ID")
    private String projectId;
}
