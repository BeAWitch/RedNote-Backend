package org.rednote.domain.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 收藏
 */
@TableName("web_like_or_favorite")
@Data
public class WebLikeOrFavorite extends BaseEntity {

    /**
     * 点赞用户 ID
     */
    private Long uid;

    /**
     * 点赞和收藏的 ID（可能是笔记/评论）
     */
    private Long likeOrFavoriteId;

    /**
     * 点赞和收藏通知的用户 ID
     */
    @TableField(exist = false)
    private Long publishUid;

    /**
     * 点赞和收藏类型（1：点赞笔记 2：点赞评论 3：收藏笔记 4：收藏专辑）
     */
    private Integer type;
}
