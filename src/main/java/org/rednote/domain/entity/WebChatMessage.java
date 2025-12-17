package org.rednote.domain.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

/**
* 聊天消息表
*/
@Data
@TableName("web_chat_message")
public class WebChatMessage extends BaseEntity {

    /**
    * 会话 id
    */
    private Long conversationId;

    /**
    * 发送者 id
    */
    private Long sendUid;

    /**
    * 消息类型（0：通知，1：文本，2：图片，3：语音，4：视频，5：自定义）
    */
    private Integer msgType;

    /**
    * 消息内容
    */
    private String content;

    /**
     * 更新时间
     */
    @TableField(exist = false)
    private Date updateTime;

}
