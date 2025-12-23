package org.rednote.interaction.api.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import org.rednote.common.domain.entity.BaseEntity;

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
    * 消息内容
    */
    private String content;

    /**
     * 更新时间
     */
    @TableField(exist = false)
    private Date updateTime;

}
