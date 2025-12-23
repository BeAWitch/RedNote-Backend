package org.rednote.note.api.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;
import org.rednote.note.api.entity.WebTag;

import java.io.Serializable;
import java.util.List;

@Data
@Accessors(chain = true)
@Schema(name = "笔记 VO")
public class NoteVO implements Serializable {

    @Schema(description = "ID")
    private Long id;

    @Schema(description = "标题")
    private String title;

    @Schema(description = "内容")
    private String content;

    @Schema(description = "笔记封面")
    private String noteCover;

    @Schema(description = "用户 ID")
    private Long uid;

    @Schema(description = "用户名")
    private String username;

    @Schema(description = "头像")
    private String avatar;

    @Schema(description = "图片地址")
    private String urls;

    @Schema(description = "分类 ID")
    private Long cid;

    @Schema(description = "父分类 ID")
    private Long cpid;

    @Schema(description = "图片数量")
    private Integer count;

    @Schema(description = "类型（图片或视频）")
    private Integer type;

    @Schema(description = "点赞数")
    private Long likeCount;

    @Schema(description = "收藏数")
    private Long favoriteCount;

    @Schema(description = "评论数")
    private Long commentCount;

    @Schema(description = "标签列表")
    private List<WebTag> tagList;

    @Schema(description = "时间")
    private Long time;

    @Schema(description = "是否置顶")
    private String pinned;

    @Schema(description = "是否关注")
    private Boolean isFollow;

    @Schema(description = "是否点赞")
    private Boolean isLike;

    @Schema(description = "是否收藏")
    private Boolean isFavorite;
}
