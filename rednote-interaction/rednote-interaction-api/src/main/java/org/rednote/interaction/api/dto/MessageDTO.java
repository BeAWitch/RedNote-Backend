package org.rednote.interaction.api.dto;

import lombok.Data;
import org.rednote.interaction.api.enums.ChatTypeEnum;

import java.io.Serializable;

/**
 * 聊天消息
 */
@Data
public class MessageDTO implements Serializable {

    private static final long serialVersionUID = 2405172041950251807L;

    private Long sendUid;

    private Long acceptUid;

    private MessageContentDTO content;

    /**
     * 聊天类型（0：私聊，1：群聊）
     */
    private ChatTypeEnum chatType;
}