package org.rednote.interaction.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

@Data
@Schema(name = "评论 DTO")
public class CommentDTO implements Serializable {

    @Schema(description = "笔记 ID")
    private Long nid;

    @Schema(description = "发布笔记的用户 ID")
    private Long noteUid;

    @Schema(description = "评论的父 ID")
    private Long pid;

    @Schema(description = "当前评论回复的评论 ID")
    private Long replyId;

    @Schema(description = "回复的评论的用户 ID")
    private Long replyUid;

    @Schema(description = "评论等级")
    private Integer level;

    @Schema(description = "评论内容")
    private String content;
}
