package org.rednote.interaction.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.rednote.interaction.api.entity.WebComment;

@Mapper
public interface WebCommentMapper extends BaseMapper<WebComment> {
}
