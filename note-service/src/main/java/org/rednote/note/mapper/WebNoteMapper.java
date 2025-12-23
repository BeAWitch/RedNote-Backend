package org.rednote.note.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.rednote.note.domain.entity.WebNote;

@Mapper
public interface WebNoteMapper extends BaseMapper<WebNote> {
}
