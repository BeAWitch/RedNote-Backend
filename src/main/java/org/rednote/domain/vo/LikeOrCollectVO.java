package org.rednote.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;

@Data
@Accessors(chain = true)
@Schema(name = "点赞收藏 VO")
public class LikeOrCollectVO implements Serializable {

    @Schema(description = "项目 ID")
    private String itemId;

    @Schema(description = "项目封面")
    private String itemCover;

    @Schema(description = "用户 ID")
    private String uid;

    @Schema(description = "用户名")
    private String username;

    @Schema(description = "头像")
    private String avatar;

    @Schema(description = "内容")
    private String content;

    @Schema(description = "时间")
    private Long time;

    @Schema(description = "类型")
    private Integer type;
}
