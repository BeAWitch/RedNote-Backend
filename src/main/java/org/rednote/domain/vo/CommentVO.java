package org.rednote.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

@Data
@Accessors(chain = true)
@Schema(name = "评论 VO")
public class CommentVO implements Serializable {

    @Schema(description = "ID")
    private Long id;

    @Schema(description = "父评论 ID")
    private Long pid;

    @Schema(description = "笔记 ID")
    private Long nid;

    @Schema(description = "笔记标题")
    private String title;

    @Schema(description = "笔记封面")
    private String noteCover;

    @Schema(description = "用户 ID")
    private Long uid;

    @Schema(description = "用户名")
    private String username;

    @Schema(description = "头像")
    private String avatar;

    @Schema(description = "笔记 ID")
    private Long noteUid;

    @Schema(description = "推送用户名")
    private String pushUsername;

    @Schema(description = "回复评论 ID")
    private Long replyId;

    @Schema(description = "回复用户 ID")
    private Long replyUid;

    @Schema(description = "回复用户名")
    private String replyUsername;

    @Schema(description = "回复用户头像")
    private String replyAvatar;

    @Schema(description = "评论内容")
    private String content;

    @Schema(description = "被回复的评论的内容")
    private String replyContent;

    @Schema(description = "评论等级")
    private Integer level;

    @Schema(description = "时间")
    private Long time;

    @Schema(description = "创建时间")
    private Date createTime;

    @Schema(description = "点赞数")
    private Long likeCount;

    @Schema(description = "是否点赞")
    private Boolean isLike;

    @Schema(description = "二级评论数量")
    private Long levelTwoCommentCount;

    @Schema(description = "子评论列表")
    private List<CommentVO> children;
}
