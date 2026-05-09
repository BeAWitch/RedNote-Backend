package org.rednote.ai.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
@Schema(name = "创作项目文档入库请求")
public class CreativeProjectDocsUpsertRequestDTO {

    @Schema(description = "文档列表", requiredMode = Schema.RequiredMode.REQUIRED)
    private List<CreativeProjectDocDTO> docs;
}
