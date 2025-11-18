package org.rednote.domain.vo;


import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.ToString;
import org.rednote.validator.group.Insert;
import org.rednote.validator.group.Update;

@ToString
@Data
@Schema(name = "图片分类 VO")
public class PictureSortVO extends BaseVO<PictureSortVO> {

    @Schema(description = "父 UID")
    private String parentUid;

    @Schema(description = "分类名")
    private String name;

    @Schema(description = "分类图片 Uid")
    private String fileUid;

    @Schema(description = "排序字段，数值越大越靠前")
    private int sort;

    @Schema(description = "是否显示: 1-是，0-否")
    @NotNull(groups = {Insert.class, Update.class})
    private Integer isShow;
}
