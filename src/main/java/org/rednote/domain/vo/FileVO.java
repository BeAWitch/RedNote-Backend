package org.rednote.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.ToString;

import java.util.List;
import java.util.Map;

@Data
@ToString
@Schema(name = "文件 VO")
public class FileVO extends BaseVO {

    @Schema(description = "用户 UID")
    private String userUid;

    @Schema(description = "管理员 UID")
    private String adminUid;

    @Schema(description = "项目名")
    private String projectName;

    @Schema(description = "模块名")
    private String sortName;

    @Schema(description = "图片 Url 集合")
    private List<String> urlList;

    @Schema(description = "系统配置")
    private Map<String, String> systemConfig;

    @Schema(description = "上传图片时携带的 token 令牌")
    private String token;
}
