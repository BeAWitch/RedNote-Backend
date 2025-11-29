package org.rednote.domain.dto;


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

@Data
@Schema(name = "点赞收藏 DTO")
public class LikeOrFavoriteDTO implements Serializable {

    @Schema(description = "点赞或收藏的 ID")
    private Long likeOrFavoriteId;

    @Schema(description = "需要通知的用户 ID")
    private Long publishUid;

    @Schema(description = "点赞收藏类型：1-点赞笔记，2-点赞评论，3-收藏笔记，4-收藏专辑")
    private Integer type;
}
