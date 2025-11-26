package org.rednote.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

@Data
@Schema(name = "聊天用户关系 VO")
public class ChatUserRelationVO implements Serializable {

    @Schema(description = "ID")
    private Long id;

    @Schema(description = "用户 ID")
    private Long uid;

    @Schema(description = "用户名")
    private String username;

    @Schema(description = "头像")
    private String avatar;

    @Schema(description = "内容")
    private String content;

    @Schema(description = "用户未读消息数量")
    private Integer count;

    @Schema(description = "消息类型: 0-文本，1-图片，2-语音，3-视频")
    private Integer msgType;

    @Schema(description = "时间戳")
    private long timestamp;
}
