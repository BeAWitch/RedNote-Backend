package org.rednote.service.impl;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.rednote.constant.RedisConstants;
import org.rednote.domain.dto.Message;
import org.rednote.domain.dto.MessageCount;
import org.rednote.domain.entity.WebChatConversation;
import org.rednote.domain.entity.WebChatConversationUserRelation;
import org.rednote.domain.entity.WebChatMessage;
import org.rednote.domain.entity.WebUser;
import org.rednote.domain.vo.ChatConversationVO;
import org.rednote.domain.vo.ChatMessageVO;
import org.rednote.enums.UncheckedMessageEnum;
import org.rednote.exception.RedNoteException;
import org.rednote.mapper.WebChatConversationMapper;
import org.rednote.mapper.WebChatConversationUserRelationMapper;
import org.rednote.mapper.WebChatMessageMapper;
import org.rednote.mapper.WebUserMapper;
import org.rednote.service.IWebChatService;
import org.rednote.service.IWebOssService;
import org.rednote.utils.UserHolder;
import org.rednote.utils.WebSocketServer;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * 聊天
 */
@Service
@RequiredArgsConstructor
public class WebChatServiceImpl extends ServiceImpl<WebChatMessageMapper, WebChatMessage> implements IWebChatService {

    private final WebUserMapper userMapper;
    private final WebChatConversationMapper conversationMapper;
    private final WebChatConversationUserRelationMapper conversationUserRelationMapper;
    private final WebSocketServer webSocketServer;
    private final IWebOssService ossService;
    private final StringRedisTemplate stringRedisTemplate;

    // 整型到枚举的映射
    private final Map<Integer, UncheckedMessageEnum> uncheckedMessageEnumMap = new java.util.HashMap<>();

    {
        Arrays.stream(UncheckedMessageEnum.values()).forEach(messageEnum ->
                uncheckedMessageEnumMap.put(messageEnum.getCode(), messageEnum));
    }

    /**
     * 发送消息
     *
     * @param message 消息
     */
    @Override
    public void sendMessage(Message message) {
        Long count = conversationUserRelationMapper.selectCount(
                new QueryWrapper<WebChatConversationUserRelation>().eq("user_id", message.getSendUid()));
        WebChatConversation chatConversation = new WebChatConversation();
        if (count == 0) {
            createConversation(message, chatConversation);
        } else {
            // 获取会话
            chatConversation = conversationMapper.selectOne(
                    new QueryWrapper<WebChatConversation>().eq("chat_type", 0));
        }
        // 创建消息
        WebChatMessage chatMessage = new WebChatMessage();
        chatMessage.setConversationId(chatConversation.getId());
        chatMessage.setSendUid(message.getSendUid());
        chatMessage.setMsgType(message.getMsgType());
        setContent(chatMessage, message);
        this.save(chatMessage);

        // Redis 未读数 +1
        String key = RedisConstants.UNCHECKED_MESSAGE_KEY + message.getAcceptUid();
        stringRedisTemplate.opsForHash()
                .increment(key, String.valueOf(chatConversation.getId()), 1L);

        // WebSocket 发送消息
        webSocketServer.sendMessage(message);
    }

    /**
     * 获取所有聊天记录
     *
     * @param currentPage    分页
     * @param pageSize       分页数
     * @param conversationId 会话 ID
     */
    @Override
    public Page<ChatMessageVO> getMessage(long currentPage, long pageSize, Long conversationId) {
        Page<WebChatMessage> page = new Page<>(currentPage, pageSize);
        Page<WebChatMessage> messagePage = this.page(page, new QueryWrapper<WebChatMessage>()
                .eq("conversation_id", conversationId).orderByDesc("create_time"));

        List<ChatMessageVO> messageVOList = messagePage.getRecords().stream()
                .map(message -> new ChatMessageVO()
                        .setSendUid(message.getSendUid())
                        .setContent(message.getContent())
                        .setMsgType(message.getMsgType())
                        .setTimestamp(message.getCreateTime().getTime()))
                .toList();

        return new Page<ChatMessageVO>()
                .setRecords(messageVOList)
                .setTotal(messagePage.getTotal());
    }

    /**
     * 获取当前用户下所有聊天的信息
     */
    @Override
    public List<ChatConversationVO> getConversationList() {
        Long currentUserId = UserHolder.getUserId();
        // 获取当前用户相关的会话
        List<WebChatConversationUserRelation> chatConversationUserRelationList =
                conversationUserRelationMapper.selectList(
                        new QueryWrapper<WebChatConversationUserRelation>().eq("user_id", currentUserId));
        return chatConversationUserRelationList.stream()
                .map(relation -> {
                    // 获取会话的另一个用户
                    WebChatConversationUserRelation conversationUserRelation = conversationUserRelationMapper.selectOne(
                            new QueryWrapper<WebChatConversationUserRelation>()
                                    .eq("conversation_id", relation.getConversationId())
                                    .ne("user_id", currentUserId)
                    );
                    Long userId = conversationUserRelation.getUserId();
                    WebUser user = userMapper.selectById(userId);
                    // 获取最新消息
                    WebChatMessage latestMessage = getOne(
                            new QueryWrapper<WebChatMessage>()
                                    .eq("conversation_id", conversationUserRelation.getConversationId())
                                    .and(q -> q.eq("send_uid", userId)
                                            .or()
                                            .eq("send_uid", currentUserId))
                                    .orderByDesc("create_time")
                                    .last("LIMIT 1")
                    );

                    // 获取未读消息数量
                    String key = RedisConstants.UNCHECKED_MESSAGE_KEY + currentUserId;
                    String hashKey = relation.getConversationId().toString();

                    Object value = stringRedisTemplate.opsForHash().get(key, hashKey);
                    Long count = ObjectUtil.isNull(value) ? 0L : Long.parseLong(value.toString());


                    return new ChatConversationVO()
                            .setId(conversationUserRelation.getConversationId())
                            .setUid(user.getId())
                            .setUsername(user.getUsername())
                            .setAvatar(user.getAvatar())
                            .setLastMessageId(relation.getLastReadMsgId())
                            .setUnreadCount(count)
                            .setLatestMessage(ObjectUtil.isNull(latestMessage) ? null : new ChatMessageVO()
                                    .setContent(latestMessage.getContent())
                                    .setSendUid(latestMessage.getSendUid())
                                    .setMsgType(latestMessage.getMsgType())
                                    .setTimestamp(latestMessage.getCreateTime().getTime())
                            );
                }).toList();
    }

    /**
     * 获取所有所有未读消息数量
     */
    @Override
    public MessageCount getUncheckedMessageCount() {
        MessageCount messageCount = new MessageCount();

        String key = RedisConstants.UNCHECKED_LIKEORFAVORITE_KEY + UserHolder.getUserId();
        String countStr = stringRedisTemplate.opsForValue().get(key);
        if (StrUtil.isNotBlank(countStr)) {
            Long likeOrFavoriteCount = Long.parseLong(countStr);
            messageCount.setLikeOrFavoriteCount(likeOrFavoriteCount);
        } else {
            messageCount.setLikeOrFavoriteCount(0L);
        }

        key = RedisConstants.UNCHECKED_COMMENT_KEY + UserHolder.getUserId();
        countStr = stringRedisTemplate.opsForValue().get(key);
        if (StrUtil.isNotBlank(countStr)) {
            Long commentCount = Long.parseLong(countStr);
            messageCount.setCommentCount(commentCount);
        } else {
            messageCount.setCommentCount(0L);
        }

        key = RedisConstants.UNCHECKED_FOLLOW_KEY + UserHolder.getUserId();
        countStr = stringRedisTemplate.opsForValue().get(key);
        if (StrUtil.isNotBlank(countStr)) {
            Long followCount = Long.parseLong(countStr);
            messageCount.setFollowCount(followCount);
        } else {
            messageCount.setFollowCount(0L);
        }

        key = RedisConstants.UNCHECKED_MESSAGE_KEY + UserHolder.getUserId();
        messageCount.setChatCount(0L);
        if (stringRedisTemplate.hasKey(key)) {
            stringRedisTemplate.opsForHash().entries(key).forEach((k, v) ->
                    messageCount.setChatCount(messageCount.getChatCount() + Long.parseLong(v.toString()))
            );
        } else {
            messageCount.setChatCount(0L);
        }

        return messageCount;
    }

    /**
     * 清除未确认消息数量
     *
     * @param type 类型（0：点赞，1：评论，2：关注）
     */
    @Override
    public void clearUncheckedMessageCount(Integer type) {
        UncheckedMessageEnum uncheckedMessageEnum = uncheckedMessageEnumMap.get(type);
        String key = switch (uncheckedMessageEnum) {
            case LIKE_OR_FAVORITE_COUNT -> RedisConstants.UNCHECKED_LIKEORFAVORITE_KEY + UserHolder.getUserId();
            case COMMENT_COUNT -> RedisConstants.UNCHECKED_COMMENT_KEY + UserHolder.getUserId();
            case FOLLOW_COUNT -> RedisConstants.UNCHECKED_FOLLOW_KEY + UserHolder.getUserId();
            default -> throw new RedNoteException("未知类型");
        };
        // 数量重置为 0
        stringRedisTemplate.opsForValue().set(key, "0");
    }

    /**
     * 增加未确认消息数量
     *
     * @param type  类型（0：点赞，1：评论，2：关注）
     * @param uid   用户 ID
     * @param count 数量
     */
    @Override
    public void increaseUncheckedMessageCount(UncheckedMessageEnum type, Long uid, long count) {
        String key = switch (type) {
            case LIKE_OR_FAVORITE_COUNT -> RedisConstants.UNCHECKED_LIKEORFAVORITE_KEY + uid;
            case COMMENT_COUNT -> RedisConstants.UNCHECKED_COMMENT_KEY + uid;
            case FOLLOW_COUNT -> RedisConstants.UNCHECKED_FOLLOW_KEY + uid;
            default -> throw new RedNoteException("未知类型");
        };
        stringRedisTemplate.opsForValue().increment(key, count);
    }

    /**
     * 减少未确认消息数量
     *
     * @param type  类型（0：点赞，1：评论，2：关注）
     * @param uid   用户 ID
     * @param count 数量
     */
    @Override
    public void decreaseUncheckedMessageCount(UncheckedMessageEnum type, Long uid, long count) {
        String key = switch (type) {
            case LIKE_OR_FAVORITE_COUNT -> RedisConstants.UNCHECKED_LIKEORFAVORITE_KEY + uid;
            case COMMENT_COUNT -> RedisConstants.UNCHECKED_COMMENT_KEY + uid;
            case FOLLOW_COUNT -> RedisConstants.UNCHECKED_FOLLOW_KEY + uid;
            default -> throw new RedNoteException("未知类型");
        };
        stringRedisTemplate.opsForValue().decrement(key, count);
    }

    /**
     * 清除未读消息数量，仅用于聊天
     *
     * @param conversationId 会话 ID
     */
    @Override
    public void clearUnreadMessageCount(Long conversationId) {
        String key = RedisConstants.UNCHECKED_MESSAGE_KEY + UserHolder.getUserId();
        stringRedisTemplate.opsForHash().delete(key, String.valueOf(conversationId));
    }

    /**
     * 关闭聊天
     *
     * @param sendUid 发送方用户 ID
     */
    @Override
    public boolean closeChat(Long sendUid) {
        try {
            webSocketServer.onClose(sendUid);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    /**
     * 创建会话
     *
     * @param message 消息
     */
    private void createConversation(Message message, WebChatConversation chatConversation) {
        // 创建会话
        chatConversation.setChatType(0); // 私聊
        conversationMapper.insert(chatConversation);
        // 创建会话成员关系
        WebChatConversationUserRelation relation = new WebChatConversationUserRelation();
        relation.setConversationId(chatConversation.getId());
        relation.setUserId(message.getSendUid());
        conversationUserRelationMapper.insert(relation);
        WebChatConversationUserRelation relation2 = new WebChatConversationUserRelation();
        relation2.setConversationId(chatConversation.getId());
        relation2.setUserId(message.getAcceptUid());
        conversationUserRelationMapper.insert(relation2);
    }

    /**
     * 设置消息内容
     *
     * @param message
     * @param chatMessage
     */
    private void setContent(WebChatMessage chatMessage, Message message) {
        switch (message.getMsgType()) {
            case 1: // 文本消息
                chatMessage.setContent(message.getContent());
                break;
            case 2: // 图片消息
                chatMessage.setContent(ossService.save(message.getContent()));
                break;
            default: // TODO 语音、视频消息
                throw new RedNoteException("不支持的消息类型");
        }
    }

}
