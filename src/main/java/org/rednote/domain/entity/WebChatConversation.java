package org.rednote.domain.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import org.rednote.enums.ChatTypeEnum;

/**
* 聊天会话表
*/
@Data
@TableName("web_chat_conversation")
public class WebChatConversation extends BaseEntity {

    /**
    * 会话类型（0-私聊 1-群聊）
    */
    private ChatTypeEnum chatType;

}
