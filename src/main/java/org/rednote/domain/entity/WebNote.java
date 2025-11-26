package org.rednote.domain.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;

/**
 * 笔记
 */

@Data
@TableName("web_note")
public class WebNote extends BaseEntity {

    /**
     * 笔记标题
     */
    private String title;

    /**
     * 笔记内容
     */
    private String content;

    /**
     * 笔记封面
     */
    private String noteCover;

    /**
     * 笔记高度
     */
    private Integer noteCoverHeight;

    /**
     * 用户 ID
     */
    private Long uid;

    /**
     * 作者
     */
    @TableField(exist = false)
    private String author;

    /**
     * 笔记二级分类 ID
     */
    private Long cid;

    /**
     * 笔记一级分类 ID
     */
    private Long cpid;

    /**
     * 图片 urls，json 格式
     */
    private String urls;

    /**
     * 图片数量
     */
    private Integer count;

    /**
     * 是否置顶
     */
    private Integer pinned;

    /**
     * 审核状态（0-未过审，1-过审）
     */
    private Integer auditStatus;

    /**
     * 笔记类型（0-图片，1-视频）
     */
    private Integer noteType;

    /**
     * 点赞次数
     */
    private Long likeCount;

    /**
     * 收藏次数
     */
    private Long collectionCount;

    /**
     * 评论次数
     */
    private Long commentCount;

    /**
     * 浏览次数
     */
    private Long viewCount;
}
