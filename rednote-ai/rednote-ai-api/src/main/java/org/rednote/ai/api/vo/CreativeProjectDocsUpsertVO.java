package org.rednote.ai.api.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(name = "创作项目文档入库结果")
public class CreativeProjectDocsUpsertVO {

    @Schema(description = "入库文档数")
    private Integer indexedDocs;

    @Schema(description = "入库分片数")
    private Integer indexedChunks;
}
