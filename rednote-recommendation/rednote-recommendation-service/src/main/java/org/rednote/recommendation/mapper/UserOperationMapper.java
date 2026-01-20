package org.rednote.recommendation.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.rednote.recommendation.api.entity.UserOperation;

@Mapper
public interface UserOperationMapper extends BaseMapper<UserOperation> {
}
