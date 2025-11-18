package org.rednote.domain.dto;


import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.rednote.validator.group.DefaultGroup;

import java.io.Serializable;

@Data
@Schema(name = "浏览记录")
public class BrowseRecordDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "用户 id")
    @NotNull(message = "uid不能为空", groups = DefaultGroup.class)
    private String uid;

    @Schema(description = "笔记 id")
    @NotNull(message = "笔记不能为空", groups = DefaultGroup.class)
    private String nid;
}
