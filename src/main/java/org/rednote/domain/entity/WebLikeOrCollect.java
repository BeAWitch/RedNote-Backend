package org.rednote.domain.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 收藏
 */
@TableName("web_like_or_collect")
@Data
public class WebLikeOrCollect extends BaseEntity {

    /**
     * 点赞用户 ID
     */
    private Long uid;

    /**
     * 点赞和收藏的 ID（可能是图片/评论）
     */
    private Long likeOrCollectionId;

    /**
     * 点赞和收藏通知的用户 ID
     */
    @TableField(exist = false)
    private Long publishUid;

    /**
     * 点赞和收藏类型（1：点赞图片 2：点赞评论 3：收藏图片 4：收藏专辑）
     */
    private Integer type;
}
