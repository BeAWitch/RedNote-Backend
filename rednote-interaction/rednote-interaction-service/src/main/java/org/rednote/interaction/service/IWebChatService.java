package org.rednote.interaction.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import org.rednote.interaction.api.dto.MessageCountDTO;
import org.rednote.interaction.api.dto.MessageDTO;
import org.rednote.interaction.api.entity.WebChatMessage;
import org.rednote.interaction.api.enums.UncheckedMessageEnum;
import org.rednote.interaction.api.vo.ChatConversationVO;
import org.rednote.interaction.api.vo.ChatMessageVO;

import java.util.List;

/**
 * 聊天
 */
public interface IWebChatService extends IService<WebChatMessage> {

    /**
     * 发送消息
     *
     * @param messageDTO 消息
     */
    void sendMessage(MessageDTO messageDTO);

    /**
     * 获取所有聊天记录
     *
     * @param currentPage 分页
     * @param pageSize    分页数
     * @param conversationId 会话 ID
     */
    Page<ChatMessageVO> getMessage(long currentPage, long pageSize, Long conversationId);

    /**
     * 获取当前用户下所有聊天的信息
     */
    List<ChatConversationVO> getConversationList();

    /**
     * 获取所有所有未读消息数量
     */
    MessageCountDTO getUncheckedMessageCount();

    /**
     * 清除未确认消息数量
     *
     * @param type    类型（点赞、评论、关注）
     */
    void clearUncheckedMessageCount(UncheckedMessageEnum type);

    /**
     * 增加未确认消息数量
     *
     * @param type    类型（0：点赞，1：评论，2：关注）
     * @param uid     用户 ID
     * @param count    数量
     */
    void increaseUncheckedMessageCount(UncheckedMessageEnum type, Long uid, long count);

    /**
     * 减少未确认消息数量
     *
     * @param type    类型（0：点赞，1：评论，2：关注）
     * @param uid     用户 ID
     * @param count    数量
     */
    void decreaseUncheckedMessageCount(UncheckedMessageEnum type, Long uid, long count);

    /**
     * 清除未读消息数量，仅用于聊天
     *
     * @param conversationId 会话 ID
     */
    void clearUnreadMessageCount(Long conversationId);

    /**
     * 关闭聊天
     *
     * @param sendUid 发送方用户 ID
     */
    boolean closeChat(Long sendUid);
}
