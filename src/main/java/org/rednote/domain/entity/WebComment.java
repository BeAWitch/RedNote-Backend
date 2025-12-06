package org.rednote.domain.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 评论
 */
@Data
@TableName("web_comment")
public class WebComment extends BaseEntity {

    /**
     * 笔记 ID
     */
    private Long nid;

    /**
     * 发布评论的用户 ID
     */
    private Long uid;

    /**
     * 根评论 ID
     */
    private Long pid;

    /**
     * 回复的评论 ID
     */
    private Long replyId;

    /**
     * 回复的用户 ID
     */
    private Long replyUid;

    /**
     * 评论等级
     */
    private Integer level;

    /**
     * 评论内容
     */
    private String content;

    /**
     * 评论点赞数量
     */
    private Long likeCount;

    /**
     * 二级评论数量
     */
    private Long levelTwoCommentCount;
}
