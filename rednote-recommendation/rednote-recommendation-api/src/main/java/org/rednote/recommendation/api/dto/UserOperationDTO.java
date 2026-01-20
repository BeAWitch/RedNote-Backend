package org.rednote.recommendation.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.rednote.recommendation.api.enums.UserOperationEnum;

import java.io.Serializable;

@Data
@Schema(name = "用户操作 DTO")
public class UserOperationDTO implements Serializable {

    @Schema(description = "用户 ID")
    private Long uid;

    @Schema(description = "笔记 ID")
    private Long nid;

    @Schema(description = "用户操作")
    private UserOperationEnum operation;
}
