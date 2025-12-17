package org.rednote.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import org.rednote.domain.dto.MessageCount;
import org.rednote.domain.dto.Message;
import org.rednote.domain.entity.WebChatMessage;
import org.rednote.domain.vo.ChatConversationVO;
import org.rednote.domain.vo.ChatMessageVO;
import org.rednote.enums.UncheckedMessageEnum;

import java.util.List;

/**
 * 聊天
 */
public interface IWebChatService extends IService<WebChatMessage> {

    /**
     * 发送消息
     *
     * @param message 消息
     */
    void sendMessage(Message message);

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
    MessageCount getUncheckedMessageCount();

    /**
     * 清除未确认消息数量
     *
     * @param type    类型（0：点赞，1：评论，2：关注）
     */
    void clearUncheckedMessageCount(Integer type);

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
