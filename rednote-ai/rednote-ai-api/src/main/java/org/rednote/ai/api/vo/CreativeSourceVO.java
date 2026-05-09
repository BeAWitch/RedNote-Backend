package org.rednote.ai.api.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(name = "引用来源")
public class CreativeSourceVO {

    @Schema(description = "来源 ID（S1..S8）")
    private String id;

    @Schema(description = "来源类型：doc|note|web")
    private String type;

    @Schema(description = "展示标题：文档名/笔记标题/网页标题")
    private String title;

    @Schema(description = "URL（web 类型可用）")
    private String url;
}
