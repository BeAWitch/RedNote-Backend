package org.rednote.domain.dto;


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

@Data
@Schema(name = "评论 DTO")
public class CommentDTO implements Serializable {

    @Schema(description = "笔记 id")
    private String nid;

    @Schema(description = "笔记发布的用户 id")
    private String noteUid;

    @Schema(description = "评论的父 id")
    private String pid;

    @Schema(description = "当前评论回复的评论 id")
    private String replyId;

    @Schema(description = "回复的评论的用户 id")
    private String replyUid;

    @Schema(description = "评论等级")
    private Integer level;

    @Schema(description = "评论内容")
    private String content;
}
