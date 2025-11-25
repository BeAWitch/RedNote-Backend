package org.rednote.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(name = "标签")
@Data
public class TagVO {

    @Schema(description = "使用次数")
    private Long likeCount;

    @Schema(description = "标题")
    private String title;
}
