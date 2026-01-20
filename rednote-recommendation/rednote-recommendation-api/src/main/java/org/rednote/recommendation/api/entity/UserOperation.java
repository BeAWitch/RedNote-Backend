package org.rednote.recommendation.api.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import org.rednote.recommendation.api.enums.UserOperationEnum;

import java.time.LocalDateTime;

@Data
@TableName("web_user_operation")
public class UserOperation {

    @TableId
    private Long id;

    private Long uid;

    private Long nid;

    /**
     * 用户操作
     */
    private UserOperationEnum operation;

    private LocalDateTime createTime;
}
