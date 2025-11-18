package org.rednote.domain.vo;


import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.ToString;
import org.rednote.validator.group.GetList;
import org.rednote.validator.group.Insert;
import org.rednote.validator.group.Update;

@ToString
@Data
@Schema(name = "图片 VO")
public class PictureVO extends BaseVO<PictureVO> {

    @Schema(description = "图片 UID")
    private String fileUid;

    @Schema(description = "图片 UIDs")
    @NotBlank(groups = {Insert.class})
    private String fileUids;

    @Schema(description = "图片名称")
    private String picName;

    @Schema(description = "所属相册分类 UID")
    @NotBlank(groups = {Insert.class, Update.class, GetList.class})
    private String pictureSortUid;
}
