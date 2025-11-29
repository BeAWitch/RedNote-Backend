package org.rednote.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.rednote.domain.entity.WebLikeOrFavorite;

@Mapper
public interface WebLikeOrCollectMapper extends BaseMapper<WebLikeOrFavorite> {
}
