package org.rednote.interaction.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.rednote.interaction.api.entity.WebChatConversation;

@Mapper
public interface WebChatConversationMapper extends BaseMapper<WebChatConversation> {
}
