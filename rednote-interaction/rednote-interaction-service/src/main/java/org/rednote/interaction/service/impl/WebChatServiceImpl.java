package org.rednote.interaction.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.rednote.common.constant.RedisConstants;
import org.rednote.common.exception.RedNoteException;
import org.rednote.common.utils.UserHolder;
import org.rednote.interaction.api.dto.MessageContentDTO;
import org.rednote.interaction.api.dto.MessageCountDTO;
import org.rednote.interaction.api.dto.MessageDTO;
import org.rednote.interaction.api.dto.WSMessageDTO;
import org.rednote.interaction.api.entity.WebChatConversation;
import org.rednote.interaction.api.entity.WebChatConversationUserRelation;
import org.rednote.interaction.api.entity.WebChatMessage;
import org.rednote.interaction.api.enums.ChatTypeEnum;
import org.rednote.interaction.api.enums.UncheckedMessageEnum;
import org.rednote.interaction.api.util.WebSocketServer;
import org.rednote.interaction.api.vo.ChatConversationVO;
import org.rednote.interaction.api.vo.ChatMessageVO;
import org.rednote.interaction.feign.OssServiceFeign;
import org.rednote.interaction.feign.UserServiceFeign;
import org.rednote.interaction.mapper.WebChatConversationMapper;
import org.rednote.interaction.mapper.WebChatConversationUserRelationMapper;
import org.rednote.interaction.mapper.WebChatMessageMapper;
import org.rednote.interaction.service.IWebChatService;
import org.rednote.user.api.entity.WebUser;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 聊天
 */
@Service
@RequiredArgsConstructor
public class WebChatServiceImpl extends ServiceImpl<WebChatMessageMapper, WebChatMessage> implements IWebChatService {

    private final UserServiceFeign userServiceFeign;
    private final WebChatConversationMapper conversationMapper;
    private final WebChatConversationUserRelationMapper conversationUserRelationMapper;
    private final WebSocketServer webSocketServer;
    private final OssServiceFeign ossServiceFeign;
    private final StringRedisTemplate stringRedisTemplate;

    /**
     * 发送消息
     *
     * @param messageDTO 消息
     */
    @Override
    public void sendMessage(MessageDTO messageDTO) {
        List<WebChatConversationUserRelation> conversationUserRelationList = conversationUserRelationMapper.selectList(
                new QueryWrapper<WebChatConversationUserRelation>().eq("user_id", messageDTO.getSendUid()));
        WebChatConversation chatConversation = new WebChatConversation();
        if (CollUtil.isEmpty(conversationUserRelationList)) {
            createConversation(messageDTO, chatConversation);
        } else {
            // 获取会话
            chatConversation = conversationMapper.selectById(conversationUserRelationList.get(0).getConversationId());
        }
        // 创建消息
        WebChatMessage chatMessage = new WebChatMessage();
        chatMessage.setConversationId(chatConversation.getId());
        chatMessage.setSendUid(messageDTO.getSendUid());
        setContent(chatMessage, messageDTO);
        this.save(chatMessage);

        // Redis 未读数 +1
        String key = RedisConstants.UNCHECKED_MESSAGE_KEY + messageDTO.getAcceptUid();
        stringRedisTemplate.opsForHash()
                .increment(key, String.valueOf(chatConversation.getId()), 1L);

        // WebSocket 发送消息
        webSocketServer.sendMessage(new WSMessageDTO()
                .setAcceptUid(messageDTO.getAcceptUid())
                .setType(UncheckedMessageEnum.CHAT)
                .setContent(messageDTO)
        );
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
        WebChatConversation chatConversation = conversationMapper.selectById(conversationId);
        Page<WebChatMessage> messagePage = this.page(page, new QueryWrapper<WebChatMessage>()
                .eq("conversation_id", conversationId).orderByDesc("create_time"));

        List<ChatMessageVO> messageVOList = messagePage.getRecords().stream()
                .map(message -> new ChatMessageVO()
                        .setSendUid(message.getSendUid())
                        .setContent(JSON.parseObject(message.getContent(), MessageContentDTO.class))
                        .setChatType(chatConversation.getChatType())
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
                    // 获取会话
                    WebChatConversation chatConversation = conversationMapper.selectById(relation.getConversationId());
                    // 获取会话的另一个用户
                    WebChatConversationUserRelation conversationUserRelation = conversationUserRelationMapper.selectOne(
                            new QueryWrapper<WebChatConversationUserRelation>()
                                    .eq("conversation_id", relation.getConversationId())
                                    .ne("user_id", currentUserId)
                    );
                    Long userId = conversationUserRelation.getUserId();
                    WebUser user = userServiceFeign.getUserById(userId).getData();
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
                                    .setContent(JSON.parseObject(latestMessage.getContent(), MessageContentDTO.class))
                                    .setSendUid(latestMessage.getSendUid())
                                    .setChatType(chatConversation.getChatType())
                                    .setTimestamp(latestMessage.getCreateTime().getTime())
                            );
                }).toList();
    }

    /**
     * 获取所有所有未读消息数量
     */
    @Override
    public MessageCountDTO getUncheckedMessageCount() {
        MessageCountDTO messageCountDTO = new MessageCountDTO();

        String key = RedisConstants.UNCHECKED_LIKEORFAVORITE_KEY + UserHolder.getUserId();
        String countStr = stringRedisTemplate.opsForValue().get(key);
        if (StrUtil.isNotBlank(countStr)) {
            Long likeOrFavoriteCount = Long.parseLong(countStr);
            messageCountDTO.setLikeOrFavoriteCount(likeOrFavoriteCount);
        } else {
            messageCountDTO.setLikeOrFavoriteCount(0L);
        }

        key = RedisConstants.UNCHECKED_COMMENT_KEY + UserHolder.getUserId();
        countStr = stringRedisTemplate.opsForValue().get(key);
        if (StrUtil.isNotBlank(countStr)) {
            Long commentCount = Long.parseLong(countStr);
            messageCountDTO.setCommentCount(commentCount);
        } else {
            messageCountDTO.setCommentCount(0L);
        }

        key = RedisConstants.UNCHECKED_FOLLOW_KEY + UserHolder.getUserId();
        countStr = stringRedisTemplate.opsForValue().get(key);
        if (StrUtil.isNotBlank(countStr)) {
            Long followCount = Long.parseLong(countStr);
            messageCountDTO.setFollowCount(followCount);
        } else {
            messageCountDTO.setFollowCount(0L);
        }

        key = RedisConstants.UNCHECKED_TREND_KEY + UserHolder.getUserId();
        countStr = stringRedisTemplate.opsForValue().get(key);
        if (StrUtil.isNotBlank(countStr)) {
            Long trendCount = Long.parseLong(countStr);
            messageCountDTO.setTrendCount(trendCount);
        } else {
            messageCountDTO.setTrendCount(0L);
        }

        key = RedisConstants.UNCHECKED_MESSAGE_KEY + UserHolder.getUserId();
        messageCountDTO.setChatCount(0L);
        if (stringRedisTemplate.hasKey(key)) {
            stringRedisTemplate.opsForHash().entries(key).forEach((k, v) ->
                    messageCountDTO.setChatCount(messageCountDTO.getChatCount() + Long.parseLong(v.toString()))
            );
        } else {
            messageCountDTO.setChatCount(0L);
        }

        return messageCountDTO;
    }

    /**
     * 清除未确认消息数量
     *
     * @param type 类型（点赞、评论、关注）
     */
    @Override
    public void clearUncheckedMessageCount(UncheckedMessageEnum type) {
        String key = switch (type) {
            case LIKE_OR_FAVORITE -> RedisConstants.UNCHECKED_LIKEORFAVORITE_KEY + UserHolder.getUserId();
            case COMMENT -> RedisConstants.UNCHECKED_COMMENT_KEY + UserHolder.getUserId();
            case FOLLOW -> RedisConstants.UNCHECKED_FOLLOW_KEY + UserHolder.getUserId();
            case TREND -> RedisConstants.UNCHECKED_TREND_KEY + UserHolder.getUserId();
            default -> throw new RedNoteException("未知类型");
        };
        // 数量重置为 0
        stringRedisTemplate.opsForValue().set(key, "0");
    }

    /**
     * 增加未确认消息数量
     *
     * @param type  类型（点赞、评论、关注）
     * @param uid   用户 ID
     * @param count 数量
     */
    @Override
    public void increaseUncheckedMessageCount(UncheckedMessageEnum type, Long uid, long count) {
        // 更新 Redis
        String key = switch (type) {
            case LIKE_OR_FAVORITE -> RedisConstants.UNCHECKED_LIKEORFAVORITE_KEY + uid;
            case COMMENT -> RedisConstants.UNCHECKED_COMMENT_KEY + uid;
            case FOLLOW -> RedisConstants.UNCHECKED_FOLLOW_KEY + uid;
            case TREND -> RedisConstants.UNCHECKED_TREND_KEY + uid;
            default -> throw new RedNoteException("未知类型");
        };
        stringRedisTemplate.opsForValue().increment(key, count);
    }

    /**
     * 减少未确认消息数量
     *
     * @param type  类型（点赞、评论、关注）
     * @param uid   用户 ID
     * @param count 数量
     */
    @Override
    public void decreaseUncheckedMessageCount(UncheckedMessageEnum type, Long uid, long count) {
        // 更新 Redis
        String key = switch (type) {
            case LIKE_OR_FAVORITE -> RedisConstants.UNCHECKED_LIKEORFAVORITE_KEY + uid;
            case COMMENT -> RedisConstants.UNCHECKED_COMMENT_KEY + uid;
            case FOLLOW -> RedisConstants.UNCHECKED_FOLLOW_KEY + uid;
            case TREND -> RedisConstants.UNCHECKED_TREND_KEY + uid;
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
     * @param messageDTO 消息
     */
    private void createConversation(MessageDTO messageDTO, WebChatConversation chatConversation) {
        // 创建会话
        chatConversation.setChatType(ChatTypeEnum.PRIVATE);
        conversationMapper.insert(chatConversation);
        // 创建会话成员关系
        WebChatConversationUserRelation relation = new WebChatConversationUserRelation();
        relation.setConversationId(chatConversation.getId());
        relation.setUserId(messageDTO.getSendUid());
        conversationUserRelationMapper.insert(relation);
        WebChatConversationUserRelation relation2 = new WebChatConversationUserRelation();
        relation2.setConversationId(chatConversation.getId());
        relation2.setUserId(messageDTO.getAcceptUid());
        conversationUserRelationMapper.insert(relation2);
    }

    /**
     * 设置消息内容
     *
     * @param chatMessage
     * @param messageDTO
     */
    private void setContent(WebChatMessage chatMessage, MessageDTO messageDTO) {
        MessageContentDTO messageContentDTO = messageDTO.getContent();
        messageContentDTO.getContents().forEach(content -> {
            switch (content.getType()) {
                case TEXT: // 文字信息不需要处理
                    break;
                case IMAGE: // 处理图片消息：保存并获取路径
                    String path = ossServiceFeign.uploadFile(content.getContent());
                    content.setContent(path);
                    break;
                default: // TODO 语音、视频消息
                    throw new RedNoteException("不支持的消息类型");
            }
        });
        chatMessage.setContent(JSON.toJSONString(messageContentDTO));
    }

}
