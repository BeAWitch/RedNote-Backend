package org.rednote.domain.entity;

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
    private String uid;

    /**
     * 作者
     */
    private String author;

    /**
     * 笔记二级分类 ID
     */
    private String cid;

    /**
     * 笔记一级分类 ID
     */
    private String cpid;

    /**
     * 笔记 urls
     */
    private String urls;

    /**
     * 图片数量
     */
    private Integer count;

    /**
     * 排序
     */
    private Integer sort;

    /**
     * 是否置顶
     */
    private String pinned;

    /**
     * 笔记状态
     */
    private String status;

    /**
     * 审核状态（0-未过审，1-过审）
     */
    private String auditStatus;

    /**
     * 笔记类型（0-图片，1-视频）
     */
    private String noteType;

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

    /**
     * 时间戳
     */
    private Long time;
}
