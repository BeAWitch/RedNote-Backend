package org.rednote.search.api.dto;


import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.rednote.common.validator.group.DefaultGroup;

import java.io.Serializable;

@Data
@Schema(name = "浏览记录")
public class BrowseRecordDTO implements Serializable {

    @Schema(description = "用户 ID")
    @NotNull(message = "uid 不能为空", groups = DefaultGroup.class)
    private Long uid;

    @Schema(description = "笔记 ID")
    @NotNull(message = "笔记不能为空", groups = DefaultGroup.class)
    private Long nid;
}
