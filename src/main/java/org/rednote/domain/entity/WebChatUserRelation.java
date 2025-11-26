package org.rednote.domain.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 聊天用户
 */
@Data
@TableName("web_chat_user_relation")
public class WebChatUserRelation extends BaseEntity {

    /**
     * 发送方用户 ID
     */
    private Long sendUid;

    /**
     * 接收方用户 ID
     */
    private Long acceptUid;

    /**
     * 聊天内容
     */
    private String content;

    /**
     * 用户未读数量
     */
    private Integer count;

    /**
     * 聊天类型（0：私聊 1：群聊）
     */
    private Integer chatType;

    /**
     * 消息类型（0：文本 1：图片 2：语音 3：视频）
     */
    private Integer msgType;
}
