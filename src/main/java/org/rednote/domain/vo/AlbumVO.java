package org.rednote.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;

@Data
@Accessors(chain = true)
@Schema(name = "专辑VO")
public class AlbumVO implements Serializable {

    @Schema(description = "ID")
    private String id;

    @Schema(description = "标题")
    private String title;

    @Schema(description = "专辑封面")
    private String albumCover;

    @Schema(description = "排序")
    private Integer sort;

    @Schema(description = "图片数量")
    private Long imgCount;

    @Schema(description = "收藏数量")
    private Long collectionCount;

    @Schema(description = "用户 ID")
    private String userId;

    @Schema(description = "用户名")
    private String username;

    @Schema(description = "头像")
    private String avatar;
}
