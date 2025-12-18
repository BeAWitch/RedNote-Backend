package org.rednote.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;
import org.rednote.enums.UncheckedMessageEnum;

@Schema(name = "Websocket 消息通知")
@Data
@Accessors(chain = true)
public class WSMessageDTO {

    @Schema(description = "接收方 ID")
    private Long acceptUid;

    @Schema(description = "消息通知类型")
    private UncheckedMessageEnum type;

    @Schema(description = "消息内容")
    private Object content;
}
