package org.rednote.interaction.api.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;
import org.rednote.interaction.api.dto.MessageContentDTO;
import org.rednote.interaction.api.enums.ChatTypeEnum;

@Schema
@Data
@Accessors(chain = true)
public class ChatMessageVO {

    @Schema(description = "发送方用户 ID")
    private Long sendUid;

    @Schema(description = "聊天内容")
    private MessageContentDTO content;

    @Schema(description = "聊天类型")
    private ChatTypeEnum chatType;

    @Schema(description = "时间戳")
    private long timestamp;
}
