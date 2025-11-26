package org.rednote.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.rednote.validator.group.DefaultGroup;

import java.io.Serializable;

@Data
@Schema(name = "专辑")
public class AlbumDTO implements Serializable {

    @Schema(description = "专辑 ID")
    private Long id;

    @Schema(description = "专辑名称")
    @NotBlank(message = "内容不能为空", groups = DefaultGroup.class)
    private String name;

    @Schema(description = "专辑发布的用户 ID")
    @NotNull(message = "用户 ID 不能为空", groups = DefaultGroup.class)
    private Long uid;

    @Schema(description = "专辑封面")
    private String albumCover;

    @Schema(description = "专辑排序")
    private Integer sort;
}
