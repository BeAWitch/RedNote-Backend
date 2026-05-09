package org.rednote.ai.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(name = "创作项目文档")
public class CreativeProjectDocDTO {

    @Schema(description = "文档名称（用于展示引用来源）", requiredMode = Schema.RequiredMode.REQUIRED)
    private String fileName;

    @Schema(description = "文档 URL（前端先上传 OSS，再把 URL 传过来）", requiredMode = Schema.RequiredMode.REQUIRED)
    private String fileUrl;
}
