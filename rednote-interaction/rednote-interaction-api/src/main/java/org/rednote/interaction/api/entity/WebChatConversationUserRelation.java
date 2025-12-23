package org.rednote.interaction.api.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

/**
* 会话成员表
*/
@Data
@TableName("web_chat_conversation_user_relation")
public class WebChatConversationUserRelation {

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
    * 会话 id
    */
    private Long conversationId;

    /**
    * 用户 id
    */
    private Long userId;

    /**
    * 最后已读消息 id
    */
    private Long lastReadMsgId;

    /**
    * 加入会话时间
    */
    private Date joinTime;

}
