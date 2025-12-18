package org.rednote.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.rednote.enums.ChatMessageTypeEnum;

import java.util.List;

@Schema(name = "聊天信息内容 DTO")
@Data
public class MessageContentDTO {

    @Schema(description = "内容列表")
    private List<ContentInfo> contents;

    @Schema(description = "聊天信息内容")
    @Data
    public static class ContentInfo {
        @Schema(description = "具体内容")
        private String content;
        @Schema(description = "内容类型")
        private ChatMessageTypeEnum type;
    }

}
