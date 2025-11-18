package org.rednote.domain.dto;


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

@Data
@Schema(name = "点赞收藏 DTO")
public class LikeOrCollectDTO implements Serializable {

    @Schema(description = "点赞或收藏的 id")
    private String likeOrCollectionId;

    @Schema(description = "需要通知的用户 id")
    private String publishUid;

    @Schema(description = "点赞收藏类型:1-点赞图片，2-点赞评论，3-收藏图片，4-收藏专辑")
    private Integer type;
}
