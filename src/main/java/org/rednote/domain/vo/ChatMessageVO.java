package org.rednote.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

@Schema
@Data
@Accessors(chain = true)
public class ChatMessageVO {

    @Schema(description = "发送方用户 ID")
    private Long sendUid;

    @Schema(description = "聊天内容")
    private String content;

    @Schema(description = "信息类型（0：通知 1：文本 2：图片 3：语音 4：视频 5：自定义消息）")
    private Integer msgType;

    @Schema(description = "时间戳")
    private long timestamp;
}
