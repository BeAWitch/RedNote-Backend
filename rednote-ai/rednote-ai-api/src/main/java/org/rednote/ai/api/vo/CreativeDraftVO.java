package org.rednote.ai.api.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(name = "生成成稿结果")
public class CreativeDraftVO {

    @Schema(description = "正文（不包含引用）")
    private String draftText;
}
