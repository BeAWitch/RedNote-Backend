package org.rednote.interaction.api.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;

@Data
@Accessors(chain = true)
@Schema(name = "关注 VO")
public class FollowVO implements Serializable {

    @Schema(description = "用户 ID")
    private Long uid;

    @Schema(description = "用户名")
    private String username;

    @Schema(description = "头像")
    private String avatar;

    @Schema(description = "红书 ID")
    private Long hsId;

    @Schema(description = "粉丝数量")
    private Long fanCount;

    @Schema(description = "是否关注")
    private Boolean isFollow;

    @Schema(description = "时间")
    private Long time;
}
