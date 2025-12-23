package org.rednote.note.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.rednote.common.validator.group.AddGroup;
import org.rednote.common.validator.group.UpdateGroup;

import java.io.Serializable;
import java.util.List;

@Data
@Schema(name = "笔记 DTO")
public class NoteDTO implements Serializable {

    @Schema(description = "ID")
    @NotBlank(message = "ID 不能为空", groups = UpdateGroup.class)
    private Long id;

    @Schema(description = "笔记标题")
    @NotBlank(message = "标题不能为空", groups = AddGroup.class)
    private String title;

    @Schema(description = "笔记内容")
    @NotBlank(message = "内容不能为空", groups = AddGroup.class)
    private String content;

    @Schema(description = "笔记封面")
    private String noteCover;

    @Schema(description = "笔记封面高度")
    private Integer noteCoverHeight;

    @Schema(description = "笔记分类 ID")
    @NotBlank(message = "二级分类不能为空", groups = AddGroup.class)
    private Long cid;

    @Schema(description = "笔记父分类 ID")
    @NotBlank(message = "一级分类不能为空", groups = AddGroup.class)
    private Long cpid;

    @Schema(description = "笔记图片地址")
    private List<String> urls;

    @Schema(description = "笔记标签")
    private List<String> tagList;

    @Schema(description = "笔记图片数量")
    private Integer count;

    @Schema(description = "笔记类型")
    private Integer type;
}
