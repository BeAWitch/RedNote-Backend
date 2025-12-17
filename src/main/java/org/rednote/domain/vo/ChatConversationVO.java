package org.rednote.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;

@Schema(name = "会话 VO")
@Data
@Accessors(chain = true)
public class ChatConversationVO implements Serializable {

    @Schema(description = "ID")
    private Long id;

    @Schema(description = "聊天对象用户 ID")
    private Long uid;

    @Schema(description = "聊天对象用户名")
    private String username;

    @Schema(description = "聊天对象头像")
    private String avatar;

    @Schema(description = "上次阅读消息 ID")
    private Long lastMessageId;

    @Schema(description = "最新消息")
    private ChatMessageVO latestMessage;

    @Schema(description = "用户未读消息数量")
    private Long unreadCount;
}
